using System.Net.Mail;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Npgsql;
using UgorjBe.Application;
using UgorjBe.Domain;
using UgorjBe.Infrastructure.Auth;
using UgorjBe.Infrastructure.Persistence;

namespace UgorjBe.Infrastructure.Services;

public sealed class AuthService(
    UgorjBeDbContext dbContext,
    IPasswordHasher<User> passwordHasher,
    JwtTokenGenerator tokenGenerator,
    TimeProvider timeProvider) : IAuthService
{
    public async Task<AuthResponse> RegisterAsync(RegisterRequest request, CancellationToken cancellationToken)
    {
        var errors = ValidateRegistration(request);
        if (errors.Count > 0) throw AppErrors.Validation(errors);

        var email = request.Email.Trim();
        var normalizedEmail = NormalizeEmail(email);
        if (await dbContext.Users.AnyAsync(x => x.NormalizedEmail == normalizedEmail, cancellationToken))
        {
            throw EmailExists();
        }

        var user = new User
        {
            Id = Guid.NewGuid(),
            Email = email,
            NormalizedEmail = normalizedEmail,
            DisplayName = request.DisplayName.Trim(),
            Locale = "hu-HU",
            Role = "customer",
            CreatedAtUtc = timeProvider.GetUtcNow()
        };
        user.PasswordHash = passwordHasher.HashPassword(user, request.Password);
        dbContext.Users.Add(user);
        try
        {
            await dbContext.SaveChangesAsync(cancellationToken);
        }
        catch (DbUpdateException exception) when (exception.InnerException is PostgresException { SqlState: PostgresErrorCodes.UniqueViolation })
        {
            throw EmailExists();
        }

        return CreateResponse(user);
    }

    public async Task<AuthResponse> LoginAsync(LoginRequest request, CancellationToken cancellationToken)
    {
        var errors = new Dictionary<string, string[]>();
        if (!IsValidEmail(request.Email)) errors["email"] = ["Érvényes e-mail-cím szükséges."];
        if (string.IsNullOrEmpty(request.Password)) errors["password"] = ["A jelszó megadása kötelező."];
        if (errors.Count > 0) throw AppErrors.Validation(errors);

        var normalizedEmail = NormalizeEmail(request.Email);
        var user = await dbContext.Users.SingleOrDefaultAsync(x => x.NormalizedEmail == normalizedEmail, cancellationToken);
        if (user is null)
        {
            throw InvalidCredentials();
        }

        var verification = passwordHasher.VerifyHashedPassword(user, user.PasswordHash, request.Password);
        if (verification == PasswordVerificationResult.Failed)
        {
            throw InvalidCredentials();
        }

        if (verification == PasswordVerificationResult.SuccessRehashNeeded)
        {
            user.PasswordHash = passwordHasher.HashPassword(user, request.Password);
            await dbContext.SaveChangesAsync(cancellationToken);
        }

        return CreateResponse(user);
    }

    public async Task<UserDto> GetCurrentUserAsync(Guid userId, CancellationToken cancellationToken)
    {
        var user = await dbContext.Users.AsNoTracking().SingleOrDefaultAsync(x => x.Id == userId, cancellationToken);
        if (user is null)
        {
            throw new AppException(401, "AUTH_REQUIRED", "Bejelentkezés szükséges.", "A hozzáférési token érvénytelen.");
        }

        return DtoMapper.ToDto(user);
    }

    public static string NormalizeEmail(string email) => email.Trim().ToUpperInvariant();

    private AuthResponse CreateResponse(User user)
    {
        var token = tokenGenerator.Generate(user);
        return new AuthResponse(token.Token, "Bearer", token.ExpiresAtUtc, DtoMapper.ToDto(user));
    }

    private static Dictionary<string, string[]> ValidateRegistration(RegisterRequest request)
    {
        var errors = new Dictionary<string, string[]>();
        if (!IsValidEmail(request.Email) || request.Email.Trim().Length > 254)
        {
            errors["email"] = ["Érvényes, legfeljebb 254 karakteres e-mail-cím szükséges."];
        }

        var password = request.Password ?? string.Empty;
        if (password.Length is < 8 or > 128 ||
            !password.Any(char.IsUpper) ||
            !password.Any(char.IsLower) ||
            !password.Any(char.IsDigit))
        {
            errors["password"] = ["A jelszó 8–128 karakteres legyen, kis- és nagybetűvel, valamint számmal."];
        }

        var displayName = request.DisplayName?.Trim() ?? string.Empty;
        if (displayName.Length is < 2 or > 80)
        {
            errors["displayName"] = ["A megjelenített név 2–80 karakter lehet."];
        }

        return errors;
    }

    private static bool IsValidEmail(string? value)
    {
        if (string.IsNullOrWhiteSpace(value)) return false;
        try
        {
            var parsed = new MailAddress(value.Trim());
            return string.Equals(parsed.Address, value.Trim(), StringComparison.OrdinalIgnoreCase);
        }
        catch (FormatException)
        {
            return false;
        }
    }

    private static AppException EmailExists() => new(
        409,
        "AUTH_EMAIL_EXISTS",
        "Ez az e-mail-cím már használatban van.",
        "Jelentkezz be, vagy használj másik e-mail-címet.");

    private static AppException InvalidCredentials() => new(
        401,
        "AUTH_INVALID_CREDENTIALS",
        "Sikertelen bejelentkezés.",
        "Az e-mail-cím vagy a jelszó nem megfelelő.");
}
