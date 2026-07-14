using Microsoft.OpenApi.Models;
using Swashbuckle.AspNetCore.SwaggerGen;

namespace UgorjBe.Api.Infrastructure;

public sealed class ProblemResponsesOperationFilter : IOperationFilter
{
    public void Apply(OpenApiOperation operation, OperationFilterContext context)
    {
        var path = context.ApiDescription.RelativePath?.TrimEnd('/') ?? string.Empty;
        var method = context.ApiDescription.HttpMethod ?? string.Empty;

        Add(operation, context, 500, "INTERNAL_ERROR");
        if (path.StartsWith("api/", StringComparison.Ordinal))
        {
            Add(operation, context, 400, "VALIDATION_FAILED");
        }

        if (path == "health")
        {
            Add(operation, context, 503, "DEPENDENCY_UNAVAILABLE");
        }
        else if (path == "api/auth/register")
        {
            Add(operation, context, 409, "AUTH_EMAIL_EXISTS");
        }
        else if (path == "api/auth/login")
        {
            Add(operation, context, 401, "AUTH_INVALID_CREDENTIALS");
        }
        else if ((path.StartsWith("api/offers/{", StringComparison.Ordinal) ||
                  path.StartsWith("api/providers/{", StringComparison.Ordinal)) && method == "GET")
        {
            Add(operation, context, 404, path.StartsWith("api/offers/", StringComparison.Ordinal)
                ? "OFFER_NOT_FOUND"
                : "PROVIDER_NOT_FOUND");
        }
        else if (path == "api/bookings" && method == "POST")
        {
            Add(operation, context, 404, "OFFER_NOT_FOUND");
            Add(operation, context, 409, "OFFER_NOT_BOOKABLE or INSUFFICIENT_CAPACITY");
        }
        else if (path.StartsWith("api/bookings/{", StringComparison.Ordinal))
        {
            Add(operation, context, 404, "BOOKING_NOT_FOUND");
            if (path.EndsWith("/cancel", StringComparison.Ordinal))
            {
                Add(operation, context, 409, "CANCELLATION_NOT_ALLOWED");
            }
        }
        else if (path.StartsWith("api/favorites/offers/{", StringComparison.Ordinal) && method == "PUT")
        {
            Add(operation, context, 404, "OFFER_NOT_FOUND");
        }
        else if (path.StartsWith("api/favorites/providers/{", StringComparison.Ordinal) && method == "PUT")
        {
            Add(operation, context, 404, "PROVIDER_NOT_FOUND");
        }
    }

    internal static void Add(
        OpenApiOperation operation,
        OperationFilterContext context,
        int status,
        string description)
    {
        operation.Responses[status.ToString()] = new OpenApiResponse
        {
            Description = description,
            Content = new Dictionary<string, OpenApiMediaType>
            {
                ["application/problem+json"] = new()
                {
                    Schema = context.SchemaGenerator.GenerateSchema(typeof(ApiProblemDetails), context.SchemaRepository)
                }
            }
        };
    }
}
