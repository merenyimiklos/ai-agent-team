using UgorjBe.Application;
using UgorjBe.Domain;

namespace UgorjBe.Infrastructure.Services;

internal static class DtoMapper
{
    public static UserDto ToDto(User user) =>
        new(user.Id, user.Email, user.DisplayName, user.Locale, user.Role, user.CreatedAtUtc);

    public static ProviderSummaryDto ToSummary(Provider provider) => new(
        provider.Id,
        provider.Name,
        provider.ShortDescription,
        Address(provider.PostalCode, provider.City, provider.Street, provider.CountryCode, provider.Latitude, provider.Longitude),
        provider.ImageUrl);

    public static OfferSummaryDto ToSummary(Offer offer, decimal? distanceKm = null) => new(
        offer.Id,
        ToSummary(offer.Provider),
        Address(offer.PostalCode, offer.City, offer.Street, offer.CountryCode, offer.Latitude, offer.Longitude),
        offer.Title,
        offer.Category,
        offer.StartsAtUtc,
        offer.EndsAtUtc,
        offer.MinChildAge,
        offer.MaxChildAge,
        new MoneyDto(offer.OriginalUnitPrice, offer.Currency),
        new MoneyDto(offer.DiscountedUnitPrice, offer.Currency),
        DiscountPercent(offer),
        Math.Max(0, offer.AvailablePlaces),
        distanceKm,
        offer.ImageUrl);

    public static OfferDetailDto ToDetail(Offer offer, DateTimeOffset now, decimal? distanceKm = null)
    {
        var unavailableReason = UnavailableReason(offer, now);
        return new OfferDetailDto(
            offer.Id,
            ToSummary(offer.Provider),
            Address(offer.PostalCode, offer.City, offer.Street, offer.CountryCode, offer.Latitude, offer.Longitude),
            offer.Title,
            offer.Description,
            offer.Category,
            offer.StartsAtUtc,
            offer.EndsAtUtc,
            offer.BookingCutoffUtc,
            offer.CancelUntilUtc,
            offer.MinChildAge,
            offer.MaxChildAge,
            offer.AccompanimentRequired,
            offer.AccessibilityInfo,
            new MoneyDto(offer.OriginalUnitPrice, offer.Currency),
            new MoneyDto(offer.DiscountedUnitPrice, offer.Currency),
            DiscountPercent(offer),
            offer.TotalCapacity,
            Math.Max(0, offer.AvailablePlaces),
            unavailableReason is null,
            unavailableReason,
            "PAY_ON_ARRIVAL",
            distanceKm,
            offer.ImageUrl);
    }

    public static BookingDto ToDto(Booking booking, DateTimeOffset now) => new(
        booking.Id,
        BookingRules.ProjectStatus(booking, now),
        booking.Quantity,
        new MoneyDto(booking.UnitPrice, booking.Currency),
        new MoneyDto(booking.TotalPrice, booking.Currency),
        "PAY_ON_ARRIVAL",
        booking.BookingCode,
        booking.QrPayload,
        booking.CreatedAtUtc,
        booking.CancelledAtUtc,
        BookingRules.CanCancel(booking, now),
        booking.CancellationDeadlineUtc,
        new BookingOfferDto(
            booking.OfferId,
            booking.OfferTitle,
            booking.OfferCategory,
            booking.ProviderIdSnapshot,
            booking.ProviderName,
            booking.StartsAtUtc,
            booking.EndsAtUtc,
            Address(booking.PostalCode, booking.City, booking.Street, booking.CountryCode, booking.Latitude, booking.Longitude),
            booking.ImageUrl));

    public static int DiscountPercent(Offer offer) => offer.OriginalUnitPrice == 0
        ? 0
        : (int)decimal.Round(
            (offer.OriginalUnitPrice - offer.DiscountedUnitPrice) / offer.OriginalUnitPrice * 100m,
            0,
            MidpointRounding.AwayFromZero);

    public static decimal DistanceKm(decimal latitude, decimal longitude, Offer offer)
    {
        const double radius = 6371d;
        var lat1 = DegreesToRadians((double)latitude);
        var lat2 = DegreesToRadians((double)offer.Latitude);
        var deltaLat = DegreesToRadians((double)(offer.Latitude - latitude));
        var deltaLon = DegreesToRadians((double)(offer.Longitude - longitude));
        var a = Math.Sin(deltaLat / 2) * Math.Sin(deltaLat / 2) +
                Math.Cos(lat1) * Math.Cos(lat2) * Math.Sin(deltaLon / 2) * Math.Sin(deltaLon / 2);
        var distance = radius * 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1 - a));
        return decimal.Round((decimal)distance, 1, MidpointRounding.AwayFromZero);
    }

    private static AddressDto Address(
        string postalCode,
        string city,
        string street,
        string countryCode,
        decimal latitude,
        decimal longitude) =>
        new(postalCode, city, street, countryCode, latitude, longitude);

    private static string? UnavailableReason(Offer offer, DateTimeOffset now)
    {
        if (offer.Status == OfferStatus.UNPUBLISHED)
        {
            return "UNPUBLISHED";
        }

        if (offer.Status == OfferStatus.ARCHIVED)
        {
            return "ARCHIVED";
        }

        if (now >= offer.StartsAtUtc)
        {
            return "STARTED";
        }

        if (now >= offer.BookingCutoffUtc)
        {
            return "BOOKING_CLOSED";
        }

        return offer.AvailablePlaces <= 0 ? "SOLD_OUT" : null;
    }

    private static double DegreesToRadians(double degrees) => degrees * Math.PI / 180d;
}
