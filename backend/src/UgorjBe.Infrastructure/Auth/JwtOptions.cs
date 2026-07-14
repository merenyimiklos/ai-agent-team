namespace UgorjBe.Infrastructure.Auth;

public sealed class JwtOptions
{
    public const string SectionName = "Jwt";
    public string Issuer { get; init; } = "ugorjbe-api";
    public string Audience { get; init; } = "ugorjbe-android";
    public string SigningKey { get; init; } = string.Empty;
    public int AccessTokenMinutes { get; init; } = 120;
}
