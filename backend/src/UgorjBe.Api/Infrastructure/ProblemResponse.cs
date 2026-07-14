using System.Diagnostics;
using System.Text.Json;
using UgorjBe.Application;

namespace UgorjBe.Api.Infrastructure;

public static class ProblemResponse
{
    private static readonly JsonSerializerOptions JsonOptions = new(JsonSerializerDefaults.Web);

    public static async Task WriteAsync(HttpContext context, AppException exception)
    {
        if (context.Response.HasStarted) return;
        context.Response.Clear();
        context.Response.StatusCode = exception.Status;
        context.Response.ContentType = "application/problem+json";
        var payload = Create(
            context,
            exception.Status,
            exception.Code,
            exception.Title,
            exception.Detail,
            exception.Extensions);
        await context.Response.WriteAsync(JsonSerializer.Serialize(payload, JsonOptions), context.RequestAborted);
    }

    public static Dictionary<string, object?> Create(
        HttpContext context,
        int status,
        string code,
        string title,
        string detail,
        IReadOnlyDictionary<string, object?>? extensions = null)
    {
        var slug = code.ToLowerInvariant().Replace('_', '-');
        var payload = new Dictionary<string, object?>
        {
            ["type"] = $"urn:ugorjbe:problem:{slug}",
            ["title"] = title,
            ["status"] = status,
            ["detail"] = detail,
            ["instance"] = context.Request.Path.Value,
            ["code"] = code,
            ["traceId"] = Activity.Current?.Id ?? context.TraceIdentifier
        };
        if (extensions is not null)
        {
            foreach (var extension in extensions) payload[extension.Key] = extension.Value;
        }

        return payload;
    }
}

public sealed class ExceptionMiddleware(RequestDelegate next, ILogger<ExceptionMiddleware> logger)
{
    public async Task InvokeAsync(HttpContext context)
    {
        try
        {
            await next(context);
        }
        catch (AppException exception)
        {
            await ProblemResponse.WriteAsync(context, exception);
        }
        catch (OperationCanceledException) when (context.RequestAborted.IsCancellationRequested)
        {
            throw;
        }
        catch (Exception exception)
        {
            logger.LogError(exception, "Unhandled request failure with trace identifier {TraceId}.", Activity.Current?.Id ?? context.TraceIdentifier);
            await ProblemResponse.WriteAsync(context, new AppException(
                500,
                "INTERNAL_ERROR",
                "Váratlan hiba történt.",
                "A kérés feldolgozása nem sikerült."));
        }
    }
}
