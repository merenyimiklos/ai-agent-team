using System.ComponentModel.DataAnnotations;

namespace UgorjBe.Api.Infrastructure;

/// <summary>The stable RFC 7807 envelope returned by every UgorjBe API failure.</summary>
public sealed class ApiProblemDetails
{
    [Required]
    public string Type { get; init; } = string.Empty;

    [Required]
    public string Title { get; init; } = string.Empty;

    [Required]
    public int Status { get; init; }

    [Required]
    public string Detail { get; init; } = string.Empty;

    [Required]
    public string Instance { get; init; } = string.Empty;

    [Required]
    public string Code { get; init; } = string.Empty;

    [Required]
    public string TraceId { get; init; } = string.Empty;

    public IReadOnlyDictionary<string, string[]>? Errors { get; init; }

    public int? AvailablePlaces { get; init; }
}
