namespace UgorjBe.Domain;

public sealed class User
{
    public Guid Id { get; set; }
    public string Email { get; set; } = string.Empty;
    public string NormalizedEmail { get; set; } = string.Empty;
    public string PasswordHash { get; set; } = string.Empty;
    public string DisplayName { get; set; } = string.Empty;
    public string Locale { get; set; } = "hu-HU";
    public string Role { get; set; } = "customer";
    public DateTimeOffset CreatedAtUtc { get; set; }
    public List<Booking> Bookings { get; set; } = [];
}

public sealed class Provider
{
    public Guid Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string ShortDescription { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public string PostalCode { get; set; } = string.Empty;
    public string City { get; set; } = string.Empty;
    public string Street { get; set; } = string.Empty;
    public string CountryCode { get; set; } = "HU";
    public decimal Latitude { get; set; }
    public decimal Longitude { get; set; }
    public string? Phone { get; set; }
    public string? Email { get; set; }
    public string? WebsiteUrl { get; set; }
    public string? AccessibilityInfo { get; set; }
    public string? ImageUrl { get; set; }
    public DateTimeOffset CreatedAtUtc { get; set; }
    public DateTimeOffset UpdatedAtUtc { get; set; }
    public List<Offer> Offers { get; set; } = [];
}

public sealed class Offer
{
    public Guid Id { get; set; }
    public Guid ProviderId { get; set; }
    public Provider Provider { get; set; } = null!;
    public string Title { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public OfferCategory Category { get; set; }
    public DateTimeOffset StartsAtUtc { get; set; }
    public DateTimeOffset EndsAtUtc { get; set; }
    public DateTimeOffset BookingCutoffUtc { get; set; }
    public DateTimeOffset CancelUntilUtc { get; set; }
    public int MinChildAge { get; set; }
    public int MaxChildAge { get; set; }
    public bool AccompanimentRequired { get; set; }
    public string? AccessibilityInfo { get; set; }
    public string? ImageUrl { get; set; }
    public decimal OriginalUnitPrice { get; set; }
    public decimal DiscountedUnitPrice { get; set; }
    public string Currency { get; set; } = "HUF";
    public int TotalCapacity { get; set; }
    public int ReservedQuantity { get; set; }
    public OfferStatus Status { get; set; } = OfferStatus.PUBLISHED;
    public DateTimeOffset CreatedAtUtc { get; set; }
    public DateTimeOffset UpdatedAtUtc { get; set; }
    public List<Booking> Bookings { get; set; } = [];

    public int AvailablePlaces => TotalCapacity - ReservedQuantity;
}

public sealed class Booking
{
    public Guid Id { get; set; }
    public Guid UserId { get; set; }
    public User User { get; set; } = null!;
    public Guid OfferId { get; set; }
    public Offer Offer { get; set; } = null!;
    public BookingStatus Status { get; set; } = BookingStatus.CONFIRMED;
    public int Quantity { get; set; }
    public decimal UnitPrice { get; set; }
    public decimal TotalPrice { get; set; }
    public string Currency { get; set; } = "HUF";
    public string OfferTitle { get; set; } = string.Empty;
    public OfferCategory OfferCategory { get; set; }
    public Guid ProviderIdSnapshot { get; set; }
    public string ProviderName { get; set; } = string.Empty;
    public DateTimeOffset StartsAtUtc { get; set; }
    public DateTimeOffset EndsAtUtc { get; set; }
    public DateTimeOffset CancellationDeadlineUtc { get; set; }
    public string PostalCode { get; set; } = string.Empty;
    public string City { get; set; } = string.Empty;
    public string Street { get; set; } = string.Empty;
    public string CountryCode { get; set; } = "HU";
    public decimal Latitude { get; set; }
    public decimal Longitude { get; set; }
    public string? ImageUrl { get; set; }
    public string BookingCode { get; set; } = string.Empty;
    public string QrPayload { get; set; } = string.Empty;
    public DateTimeOffset CreatedAtUtc { get; set; }
    public DateTimeOffset? CancelledAtUtc { get; set; }
}

public sealed class FavoriteOffer
{
    public Guid UserId { get; set; }
    public User User { get; set; } = null!;
    public Guid OfferId { get; set; }
    public Offer Offer { get; set; } = null!;
    public DateTimeOffset CreatedAtUtc { get; set; }
}

public sealed class FavoriteProvider
{
    public Guid UserId { get; set; }
    public User User { get; set; } = null!;
    public Guid ProviderId { get; set; }
    public Provider Provider { get; set; } = null!;
    public DateTimeOffset CreatedAtUtc { get; set; }
}
