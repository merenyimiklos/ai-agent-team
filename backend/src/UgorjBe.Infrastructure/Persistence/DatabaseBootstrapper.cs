using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

namespace UgorjBe.Infrastructure.Persistence;

public static class DatabaseBootstrapper
{
    public static async Task InitializeAsync(IServiceProvider services, IConfiguration configuration, CancellationToken cancellationToken)
    {
        if (!configuration.GetValue("DatabaseInitialization:Enabled", true)) return;

        await using var scope = services.CreateAsyncScope();
        var logger = scope.ServiceProvider.GetRequiredService<ILogger<UgorjBeDbContext>>();
        var dbContext = scope.ServiceProvider.GetRequiredService<UgorjBeDbContext>();
        Exception? lastFailure = null;
        for (var attempt = 1; attempt <= 10; attempt++)
        {
            try
            {
                await dbContext.Database.MigrateAsync(cancellationToken);
                if (configuration.GetValue("SeedData:Enabled", false))
                {
                    await scope.ServiceProvider.GetRequiredService<DatabaseSeeder>().SeedAsync(cancellationToken);
                }

                return;
            }
            catch (Exception exception) when (attempt < 10 && exception is not OperationCanceledException)
            {
                lastFailure = exception;
                logger.LogWarning(exception, "Database initialization attempt {Attempt} failed; retrying.", attempt);
                await Task.Delay(TimeSpan.FromSeconds(2), cancellationToken);
            }
        }

        throw new InvalidOperationException("Database initialization failed after 10 attempts.", lastFailure);
    }
}
