using System.Net;
using System.Text.Json;
using Xunit;

namespace UgorjBe.IntegrationTests;

public sealed class OpenApiTests
{
    [Fact]
    public async Task Generated_document_describes_typed_problem_contract_for_booking_failures()
    {
        using var factory = new NoDatabaseApiFactory();
        using var client = factory.CreateClient();

        var response = await client.GetAsync("/swagger/v1/swagger.json");

        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        using var document = await JsonDocument.ParseAsync(await response.Content.ReadAsStreamAsync());
        var schemas = document.RootElement.GetProperty("components").GetProperty("schemas");
        var problem = schemas.GetProperty("ApiProblemDetails");
        var properties = problem.GetProperty("properties");
        foreach (var property in new[] { "type", "title", "status", "detail", "instance", "code", "traceId", "errors", "availablePlaces", "reason", "currentStatus", "conflictingFields" })
        {
            Assert.True(properties.TryGetProperty(property, out _), $"Missing ApiProblemDetails.{property} schema property.");
        }


        var adminResponses = document.RootElement
            .GetProperty("paths")
            .GetProperty("/api/admin/offers/{offerId}/publish")
            .GetProperty("post")
            .GetProperty("responses");
        foreach (var status in new[] { "400", "401", "403", "404", "409", "500" })
        {
            Assert.True(adminResponses.TryGetProperty(status, out _), $"Missing admin publish {status} response.");
        }

        var required = problem.GetProperty("required").EnumerateArray().Select(x => x.GetString()).ToHashSet();
        foreach (var property in new[] { "type", "title", "status", "detail", "instance", "code", "traceId" })
        {
            Assert.Contains(property, required);
        }

        var responses = document.RootElement
            .GetProperty("paths")
            .GetProperty("/api/bookings")
            .GetProperty("post")
            .GetProperty("responses");
        foreach (var status in new[] { "400", "401", "404", "409", "500" })
        {
            var schema = responses.GetProperty(status)
                .GetProperty("content")
                .GetProperty("application/problem+json")
                .GetProperty("schema");
            Assert.Equal("#/components/schemas/ApiProblemDetails", schema.GetProperty("$ref").GetString());
        }
    }
}
