using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Design;

namespace UgorjBe.Infrastructure.Persistence;

public sealed class UgorjBeDesignTimeDbContextFactory : IDesignTimeDbContextFactory<UgorjBeDbContext>
{
    public UgorjBeDbContext CreateDbContext(string[] args)
    {
        var connectionString = Environment.GetEnvironmentVariable("ConnectionStrings__Default")
            ?? "Host=localhost;Port=5432;Database=ugorjbe;Username=ugorjbe;Password=development-only";
        var options = new DbContextOptionsBuilder<UgorjBeDbContext>()
            .UseNpgsql(connectionString)
            .UseSnakeCaseNamingConvention()
            .Options;
        return new UgorjBeDbContext(options);
    }
}
