using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using UgorjBe.Infrastructure.Persistence;

namespace UgorjBe.IntegrationTests;

public sealed class ApiFactory : WebApplicationFactory<Program>
{
    private const string TestSigningKey = "integration-test-signing-key-at-least-thirty-two-bytes";

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
