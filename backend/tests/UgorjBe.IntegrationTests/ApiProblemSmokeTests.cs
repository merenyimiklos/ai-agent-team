using System.Net;
using System.Text.Json;
using Xunit;

namespace UgorjBe.IntegrationTests;

public sealed class ApiProblemSmokeTests
{
    [Fact]
    public async Task Protected_endpoint_without_token_returns_auth_problem()
    {
        using var factory = new NoDatabaseApiFactory();
        using var client = factory.CreateClient();

        var response = await client.GetAsync("/api/bookings");

        Assert.Equal(HttpStatusCode.Unauthorized, response.StatusCode);
        Assert.Equal("application/problem+json", response.Content.Headers.ContentType?.MediaType);
        var body = await JsonDocument.ParseAsync(await response.Content.ReadAsStreamAsync());
        Assert.Equal("AUTH_REQUIRED", body.RootElement.GetProperty("code").GetString());
        Assert.True(body.RootElement.TryGetProperty("traceId", out _));
    }

    [Fact]
    public async Task Invalid_catalog_page_returns_validation_problem_without_querying_database()
    {
        using var factory = new NoDatabaseApiFactory();
        using var client = factory.CreateClient();

        var response = await client.GetAsync("/api/offers?page=0");

        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
        Assert.Equal("application/problem+json", response.Content.Headers.ContentType?.MediaType);
        var body = await JsonDocument.ParseAsync(await response.Content.ReadAsStreamAsync());
        Assert.Equal("VALIDATION_FAILED", body.RootElement.GetProperty("code").GetString());
        Assert.True(body.RootElement.GetProperty("errors").TryGetProperty("page", out _));
    }
}
