using System.Text;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.DependencyInjection.Extensions;
using Microsoft.IdentityModel.Tokens;
using UgorjBe.Infrastructure.Persistence;

namespace UgorjBe.IntegrationTests;

public sealed class ApiFactory : WebApplicationFactory<Program>
{
    private const string TestSigningKey = "integration-test-signing-key-at-least-thirty-two-bytes";

    public ApiFactory(DateTimeOffset? utcNow = null)
    {
        Clock = new AdjustableTimeProvider(utcNow ?? DateTimeOffset.UtcNow);
    }

    public AdjustableTimeProvider Clock { get; }

    protected override void ConfigureWebHost(IWebHostBuilder builder)
    {
        builder.UseEnvironment("Testing");
        builder.ConfigureAppConfiguration((_, configuration) =>
        {
            configuration.AddInMemoryCollection(new Dictionary<string, string?>
            {
                ["ConnectionStrings:Default"] = Environment.GetEnvironmentVariable("UGORJBE_TEST_CONNECTION"),
                ["Jwt:Issuer"] = "ugorjbe-api",
                ["Jwt:Audience"] = "ugorjbe-android",
                ["Jwt:SigningKey"] = TestSigningKey,
                ["Jwt:AccessTokenMinutes"] = "120",
                ["DatabaseInitialization:Enabled"] = "true",
                ["SeedData:Enabled"] = "true"
            });
        });
        builder.ConfigureServices(services =>
        {
            var connectionString = Environment.GetEnvironmentVariable("UGORJBE_TEST_CONNECTION")
                ?? throw new InvalidOperationException("UGORJBE_TEST_CONNECTION is required for PostgreSQL integration tests.");
            services.RemoveAll<DbContextOptions<UgorjBeDbContext>>();
            services.AddDbContext<UgorjBeDbContext>(options =>
                options.UseNpgsql(
                        connectionString,
                        npgsql => npgsql.MigrationsAssembly(typeof(UgorjBeDbContext).Assembly.FullName))
                    .UseSnakeCaseNamingConvention());

            services.PostConfigure<JwtBearerOptions>(JwtBearerDefaults.AuthenticationScheme, options =>
            {
                options.TokenValidationParameters.ValidIssuer = "ugorjbe-api";
                options.TokenValidationParameters.ValidAudience = "ugorjbe-android";
                options.TokenValidationParameters.IssuerSigningKey =
                    new SymmetricSecurityKey(Encoding.UTF8.GetBytes(TestSigningKey));
            });
            services.RemoveAll<TimeProvider>();
            services.AddSingleton<TimeProvider>(Clock);
        });
    }

    public async Task ResetAsync()
    {
        _ = CreateClient();
        await using var scope = Services.CreateAsyncScope();
        var dbContext = scope.ServiceProvider.GetRequiredService<UgorjBeDbContext>();
        await dbContext.Database.ExecuteSqlRawAsync(
            "TRUNCATE TABLE favorite_offers, favorite_providers, bookings, offers, providers, users CASCADE");
        await scope.ServiceProvider.GetRequiredService<DatabaseSeeder>().SeedAsync(CancellationToken.None);
    }

    public async Task SetPrimaryOfferToOnePlaceAsync()
    {
        await using var scope = Services.CreateAsyncScope();
        var dbContext = scope.ServiceProvider.GetRequiredService<UgorjBeDbContext>();
        var offer = await dbContext.Offers.SingleAsync(x => x.Id == DatabaseSeeder.PrimaryOfferId);
        offer.ReservedQuantity = offer.TotalCapacity - 1;
        await dbContext.SaveChangesAsync();
    }
}

public sealed class AdjustableTimeProvider(DateTimeOffset utcNow) : TimeProvider
{
    private DateTimeOffset current = utcNow;

    public override DateTimeOffset GetUtcNow() => current;

    public void SetUtcNow(DateTimeOffset value) => current = value;
}
