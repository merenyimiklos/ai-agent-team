using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.Extensions.Configuration;

namespace UgorjBe.IntegrationTests;

public sealed class NoDatabaseApiFactory : WebApplicationFactory<Program>
{
    protected override void ConfigureWebHost(IWebHostBuilder builder)
    {
        builder.UseEnvironment("Testing");
        builder.ConfigureAppConfiguration((_, configuration) =>
        {
            configuration.AddInMemoryCollection(new Dictionary<string, string?>
            {
                ["ConnectionStrings:Default"] = "Host=localhost;Database=unused;Username=unused;Password=unused",
                ["Jwt:Issuer"] = "ugorjbe-api",
                ["Jwt:Audience"] = "ugorjbe-android",
                ["Jwt:SigningKey"] = "no-database-test-signing-key-at-least-thirty-two-bytes",
                ["DatabaseInitialization:Enabled"] = "false",
                ["SeedData:Enabled"] = "false"
            });
        });
    }
}
