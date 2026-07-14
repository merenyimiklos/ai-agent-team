using UgorjBe.Domain;

namespace UgorjBe.Application;

public sealed record AdminProviderSummaryDto(
    Guid Id,
    string Name,
    string ShortDescription,
    AddressDto Address,
    string? ImageUrl,
    int ActiveOfferCount,
    int TotalOfferCount,
    DateTimeOffset UpdatedAtUtc,
    string Version);

public sealed record AdminProviderDetailDto(
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
    int ActiveOfferCount,
    int TotalOfferCount,
    DateTimeOffset CreatedAtUtc,
    DateTimeOffset UpdatedAtUtc,
    string Version);

public class ProviderCreateRequest
{
    public string Name { get; init; } = string.Empty;
    public string ShortDescription { get; init; } = string.Empty;
    public string Description { get; init; } = string.Empty;
    public AddressDto? Address { get; init; }
    public string? Phone { get; init; }
    public string? Email { get; init; }
    public string? WebsiteUrl { get; init; }
    public string? AccessibilityInfo { get; init; }
    public string? ImageUrl { get; init; }
}

public sealed class ProviderUpdateRequest : ProviderCreateRequest
{
    public string Version { get; init; } = string.Empty;
}

public sealed record AdminOfferSummaryDto(
    Guid Id,
    Guid ProviderId,
    string ProviderName,
    string Title,
    OfferCategory Category,
    OfferStatus Status,
    DateTimeOffset StartsAtUtc,
    DateTimeOffset EndsAtUtc,
    AddressDto Address,
    MoneyDto DiscountedUnitPrice,
    int TotalCapacity,
    int ReservedQuantity,
    int AvailablePlaces,
    DateTimeOffset UpdatedAtUtc,
    string Version);

public sealed record AdminOfferDetailDto(
    Guid Id,
    Guid ProviderId,
    string ProviderName,
    string Title,
    string Description,
    OfferCategory Category,
    OfferStatus Status,
    AddressDto Address,
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
    int TotalCapacity,
    int ReservedQuantity,
    int AvailablePlaces,
    string? ImageUrl,
    DateTimeOffset? PublishedAtUtc,
    DateTimeOffset? ArchivedAtUtc,
    DateTimeOffset CreatedAtUtc,
    DateTimeOffset UpdatedAtUtc,
    string Version);

public class OfferWriteRequest
{
    public Guid ProviderId { get; init; }
    public string Title { get; init; } = string.Empty;
    public string Description { get; init; } = string.Empty;
    public OfferCategory Category { get; init; }
    public AddressDto? Address { get; init; }
    public DateTimeOffset StartsAtUtc { get; init; }
    public DateTimeOffset EndsAtUtc { get; init; }
    public DateTimeOffset BookingCutoffUtc { get; init; }
    public DateTimeOffset CancelUntilUtc { get; init; }
    public int MinChildAge { get; init; }
    public int MaxChildAge { get; init; }
    public bool AccompanimentRequired { get; init; }
    public string? AccessibilityInfo { get; init; }
    public MoneyDto? OriginalUnitPrice { get; init; }
    public MoneyDto? DiscountedUnitPrice { get; init; }
    public int TotalCapacity { get; init; }
    public string? ImageUrl { get; init; }
}

public sealed class OfferUpdateRequest : OfferWriteRequest
{
    public string Version { get; init; } = string.Empty;
}

public sealed class LifecycleRequest
{
    public string Version { get; init; } = string.Empty;
}

public sealed class AdminProviderQuery
{
    public string? Q { get; init; }
    public int Page { get; init; } = 1;
    public int PageSize { get; init; } = 20;
}

public sealed class AdminOfferQuery
{
    public string? Q { get; init; }
    public Guid? ProviderId { get; init; }
    public List<OfferCategory>? Category { get; init; }
    public List<OfferStatus>? Status { get; init; }
    public DateTimeOffset? StartsFromUtc { get; init; }
    public DateTimeOffset? StartsToUtc { get; init; }
    public int Page { get; init; } = 1;
    public int PageSize { get; init; } = 20;
}

public sealed record AdminDashboardDto(
    int ProviderCount,
    int DraftOfferCount,
    int PublishedOfferCount,
    int UnpublishedOfferCount,
    int ArchivedOfferCount,
    int StartingWithin24HoursCount,
    IReadOnlyList<AdminOfferSummaryDto> NextOffers);
