using Xunit;

namespace UgorjBe.IntegrationTests;

public sealed class PostgresFactAttribute : FactAttribute
{
    public PostgresFactAttribute()
    {
        if (string.IsNullOrWhiteSpace(Environment.GetEnvironmentVariable("UGORJBE_TEST_CONNECTION")))
        {
            Skip = "Set UGORJBE_TEST_CONNECTION to an isolated PostgreSQL database to run integration tests.";
        }
    }
}
