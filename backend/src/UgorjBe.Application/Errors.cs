namespace UgorjBe.Application;

public sealed class AppException : Exception
{
    public AppException(
        int status,
        string code,
        string title,
        string detail,
        IReadOnlyDictionary<string, object?>? extensions = null)
        : base(detail)
    {
        Status = status;
        Code = code;
        Title = title;
        Detail = detail;
        Extensions = extensions ?? new Dictionary<string, object?>();
    }

    public int Status { get; }
    public string Code { get; }
    public string Title { get; }
    public string Detail { get; }
    public IReadOnlyDictionary<string, object?> Extensions { get; }
}

public static class AppErrors
{
    public static AppException Validation(IReadOnlyDictionary<string, string[]> errors) => new(
        400,
        "VALIDATION_FAILED",
        "A kérés érvénytelen.",
        "Egy vagy több mező hibás.",
        new Dictionary<string, object?> { ["errors"] = errors });

    public static AppException NotFound(string target) => target switch
    {
        "offer" => new(404, "OFFER_NOT_FOUND", "Az ajánlat nem található.", "A kért ajánlat nem érhető el."),
        "provider" => new(404, "PROVIDER_NOT_FOUND", "A szolgáltató nem található.", "A kért szolgáltató nem érhető el."),
        _ => new(404, "BOOKING_NOT_FOUND", "A foglalás nem található.", "A kért foglalás nem érhető el.")
    };
}
