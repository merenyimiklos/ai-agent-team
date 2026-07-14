using System.ComponentModel.DataAnnotations;
using UgorjBe.Domain;

namespace UgorjBe.Application;

public sealed record MoneyDto(decimal Amount, string Currency);

public sealed record AddressDto(
    string PostalCode,
    string City,
    string Street,
    string CountryCode,
    decimal Latitude,
    decimal Longitude);

public sealed record UserDto(
    Guid Id,
    string Email,
    string DisplayName,
    string Locale,
    DateTimeOffset CreatedAtUtc);

public sealed record AuthResponse(
    string AccessToken,
    string TokenType,
    DateTimeOffset ExpiresAtUtc,
    UserDto User);

public sealed record ProviderSummaryDto(
    Guid Id,
    string Name,
    string ShortDescription,
    AddressDto Address,
    string? ImageUrl);

public sealed record ProviderDetailDto(
    Guid Id,
    string Name,
    string ShortDescription,
    string Description,
    AddressDto Address,
    string? Phone,
    string? Email,
    string? WebsiteUrl,
    string? AccessibilityInfo,
    string? ImageUrl,
    int ActiveOfferCount);

public sealed record OfferSummaryDto(
    Guid Id,
    ProviderSummaryDto Provider,
    string Title,
    OfferCategory Category,
    DateTimeOffset StartsAtUtc,
    DateTimeOffset EndsAtUtc,
    int MinChildAge,
    int MaxChildAge,
    MoneyDto OriginalUnitPrice,
    MoneyDto DiscountedUnitPrice,
    int DiscountPercent,
    int AvailablePlaces,
    decimal? DistanceKm,
    string? ImageUrl);

public sealed record OfferDetailDto(
    Guid Id,
    ProviderSummaryDto Provider,
    string Title,
    string Description,
    OfferCategory Category,
    DateTimeOffset StartsAtUtc,
    DateTimeOffset EndsAtUtc,
    DateTimeOffset BookingCutoffUtc,
    DateTimeOffset CancelUntilUtc,
    int MinChildAge,
    int MaxChildAge,
    bool AccompanimentRequired,
    string? AccessibilityInfo,
    MoneyDto OriginalUnitPrice,
    MoneyDto DiscountedUnitPrice,
    int DiscountPercent,
    int TotalCapacity,
    int AvailablePlaces,
    bool IsBookable,
    string? UnavailableReason,
    string PaymentMethod,
    decimal? DistanceKm,
    string? ImageUrl);

public sealed record BookingOfferDto(
    Guid Id,
    string Title,
    OfferCategory Category,
    Guid ProviderId,
    string ProviderName,
    DateTimeOffset StartsAtUtc,
    DateTimeOffset EndsAtUtc,
    AddressDto Address,
    string? ImageUrl);

public sealed record BookingDto(
    Guid Id,
    string Status,
    int Quantity,
    MoneyDto UnitPrice,
    MoneyDto TotalPrice,
    string PaymentMethod,
    string BookingCode,
    string QrPayload,
    DateTimeOffset CreatedAtUtc,
    DateTimeOffset? CancelledAtUtc,
    bool CancellationAllowed,
    DateTimeOffset CancellationDeadlineUtc,
    BookingOfferDto Offer);

public sealed record PageDto<T>(
    IReadOnlyList<T> Items,
    int Page,
    int PageSize,
    int TotalCount,
    int TotalPages)
{
    public static PageDto<T> Create(IReadOnlyList<T> items, int page, int pageSize, int totalCount) =>
        new(items, page, pageSize, totalCount,
            totalCount == 0 ? 0 : (int)Math.Ceiling(totalCount / (double)pageSize));
}

public sealed class RegisterRequest
{
    [Required]
    public string Email { get; init; } = string.Empty;

    [Required]
    public string Password { get; init; } = string.Empty;

    [Required]
    public string DisplayName { get; init; } = string.Empty;
}

public sealed class LoginRequest
{
    [Required]
    public string Email { get; init; } = string.Empty;

    [Required]
    public string Password { get; init; } = string.Empty;
}

public sealed class CreateBookingRequest
{
    [Required]
    public Guid OfferId { get; init; }

    public int Quantity { get; init; }
}

public sealed class OfferQuery
{
    public string? Q { get; init; }
    public Guid? ProviderId { get; init; }
    public List<OfferCategory>? Category { get; init; }
    public int? ChildAge { get; init; }
    public DateTimeOffset? StartsFromUtc { get; init; }
    public DateTimeOffset? StartsToUtc { get; init; }
    public decimal? MinPrice { get; init; }
    public decimal? MaxPrice { get; init; }
    public int MinAvailablePlaces { get; init; } = 1;
    public decimal? Latitude { get; init; }
    public decimal? Longitude { get; init; }
    public decimal? MaxDistanceKm { get; init; }
    public OfferSort Sort { get; init; } = OfferSort.START_TIME;
    public SortDirection Direction { get; init; } = SortDirection.ASC;
    public int Page { get; init; } = 1;
    public int PageSize { get; init; } = 20;
}

public sealed class CoordinateQuery
{
    public decimal? Latitude { get; init; }
    public decimal? Longitude { get; init; }
}

public sealed class BookingQuery
{
    public BookingScope Scope { get; init; } = BookingScope.ACTIVE;
    public int Page { get; init; } = 1;
    public int PageSize { get; init; } = 20;
}

public sealed class PageQuery
{
    public int Page { get; init; } = 1;
    public int PageSize { get; init; } = 20;
}
