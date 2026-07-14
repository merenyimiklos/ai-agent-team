namespace UgorjBe.Domain;

public enum BookingRejection
{
    NONE,
    NOT_BOOKABLE,
    INSUFFICIENT_CAPACITY
}

public static class BookingRules
{
    public static BookingRejection CanReserve(Offer offer, int quantity, DateTimeOffset now)
    {
        ArgumentOutOfRangeException.ThrowIfLessThan(quantity, 1);

        if (offer.Status != OfferStatus.PUBLISHED ||
            now >= offer.BookingCutoffUtc ||
            now >= offer.StartsAtUtc)
        {
            return BookingRejection.NOT_BOOKABLE;
        }

        return offer.AvailablePlaces >= quantity
            ? BookingRejection.NONE
            : BookingRejection.INSUFFICIENT_CAPACITY;
    }

    public static bool CanCancel(Booking booking, DateTimeOffset now) =>
        booking.Status == BookingStatus.CONFIRMED &&
        now <= booking.CancellationDeadlineUtc &&
        now < booking.StartsAtUtc;

    public static string ProjectStatus(Booking booking, DateTimeOffset now) => booking.Status switch
    {
        BookingStatus.CANCELLED => "CANCELLED",
        BookingStatus.CONFIRMED when booking.EndsAtUtc <= now => "COMPLETED",
        _ => "CONFIRMED"
    };

    public static decimal CalculateTotal(decimal unitPrice, int quantity)
    {
        ArgumentOutOfRangeException.ThrowIfNegative(unitPrice);
        ArgumentOutOfRangeException.ThrowIfLessThan(quantity, 1);
        return decimal.Round(unitPrice * quantity, 2, MidpointRounding.AwayFromZero);
    }
}
