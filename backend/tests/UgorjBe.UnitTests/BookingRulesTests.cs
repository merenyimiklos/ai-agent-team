using UgorjBe.Domain;
using Xunit;

namespace UgorjBe.UnitTests;

public sealed class BookingRulesTests
{
    private static readonly DateTimeOffset Now = new(2026, 7, 14, 10, 0, 0, TimeSpan.Zero);

    [Fact]
    public void Reserve_accepts_published_offer_with_capacity_before_cutoff()
    {
        var offer = Offer(capacity: 3, reserved: 1);

        var result = BookingRules.CanReserve(offer, 2, Now);

        Assert.Equal(BookingRejection.NONE, result);
    }

    [Theory]
    [InlineData(OfferStatus.UNPUBLISHED, 11, 0)]
    [InlineData(OfferStatus.DRAFT, 11, 0)]
    [InlineData(OfferStatus.ARCHIVED, 11, 0)]
    [InlineData(OfferStatus.PUBLISHED, 10, 0)]
    [InlineData(OfferStatus.PUBLISHED, 11, 0)]
    public void Reserve_rejects_withdrawn_cutoff_or_started_offer(OfferStatus status, int cutoffHour, int startHourOffset)
    {
        var offer = Offer(capacity: 3, reserved: 0);
        offer.Status = status;
        offer.BookingCutoffUtc = new DateTimeOffset(2026, 7, 14, cutoffHour, 0, 0, TimeSpan.Zero);
        offer.StartsAtUtc = Now.AddHours(startHourOffset);

        Assert.Equal(BookingRejection.NOT_BOOKABLE, BookingRules.CanReserve(offer, 1, Now));
    }

    [Fact]
    public void Reserve_reports_insufficient_capacity()
    {
        var offer = Offer(capacity: 2, reserved: 1);

        Assert.Equal(BookingRejection.INSUFFICIENT_CAPACITY, BookingRules.CanReserve(offer, 2, Now));
    }

    [Theory]
    [InlineData(0)]
    [InlineData(11)]
    public void Reserve_rejects_quantity_outside_contract(int quantity)
    {
        var offer = Offer(capacity: 20, reserved: 0);

        Assert.Throws<ArgumentOutOfRangeException>(() => BookingRules.CanReserve(offer, quantity, Now));
    }

    [Fact]
    public void Cancellation_is_allowed_at_deadline_but_not_after()
    {
        var booking = Booking();
        booking.CancellationDeadlineUtc = Now;

        Assert.True(BookingRules.CanCancel(booking, Now));
        Assert.False(BookingRules.CanCancel(booking, Now.AddTicks(1)));
    }

    [Theory]
    [InlineData(BookingStatus.CANCELLED, 1, "CANCELLED")]
    [InlineData(BookingStatus.CONFIRMED, -1, "COMPLETED")]
    [InlineData(BookingStatus.CONFIRMED, 1, "CONFIRMED")]
    public void Status_projection_is_derived_without_mutating_persistence(BookingStatus status, int endHourOffset, string expected)
    {
        var booking = Booking();
        booking.Status = status;
        booking.EndsAtUtc = Now.AddHours(endHourOffset);

        Assert.Equal(expected, BookingRules.ProjectStatus(booking, Now));
    }

    [Fact]
    public void Total_uses_decimal_arithmetic()
    {
        Assert.Equal(9600.00m, BookingRules.CalculateTotal(3200.00m, 3));
    }

    private static Offer Offer(int capacity, int reserved) => new()
    {
        Status = OfferStatus.PUBLISHED,
        StartsAtUtc = Now.AddHours(2),
        BookingCutoffUtc = Now.AddHours(1),
        TotalCapacity = capacity,
        ReservedQuantity = reserved
    };

    private static Booking Booking() => new()
    {
        Status = BookingStatus.CONFIRMED,
        StartsAtUtc = Now.AddHours(2),
        EndsAtUtc = Now.AddHours(3),
        CancellationDeadlineUtc = Now.AddHours(1)
    };
}
