using System.Data;
using Microsoft.EntityFrameworkCore;
using UgorjBe.Application;
using UgorjBe.Domain;
using UgorjBe.Infrastructure.Persistence;

namespace UgorjBe.Infrastructure.Services;

public sealed class BookingService(UgorjBeDbContext dbContext, TimeProvider timeProvider) : IBookingService
{
    public async Task<BookingDto> CreateAsync(Guid userId, CreateBookingRequest request, CancellationToken cancellationToken)
    {
        var errors = new Dictionary<string, string[]>();
        if (request.OfferId == Guid.Empty) errors["offerId"] = ["Érvényes ajánlatazonosító szükséges."];
        if (request.Quantity is < 1 or > 10) errors["quantity"] = ["A foglalható helyek száma 1 és 10 között lehet."];
        if (errors.Count > 0) throw AppErrors.Validation(errors);

        await using var transaction = await dbContext.Database.BeginTransactionAsync(IsolationLevel.ReadCommitted, cancellationToken);
        var offer = await dbContext.Offers
            .FromSqlInterpolated($"SELECT * FROM offers WHERE id = {request.OfferId} FOR UPDATE")
            .Include(x => x.Provider)
            .SingleOrDefaultAsync(cancellationToken)
            ?? throw AppErrors.NotFound("offer");

        var now = timeProvider.GetUtcNow();
        var decision = BookingRules.CanReserve(offer, request.Quantity, now);
        if (decision == BookingRejection.NOT_BOOKABLE)
        {
            throw new AppException(
                409,
                "OFFER_NOT_BOOKABLE",
                "Az ajánlat már nem foglalható.",
                "A program elkezdődött, a foglalási határidő lejárt, vagy az ajánlatot visszavonták.");
        }

        if (decision == BookingRejection.INSUFFICIENT_CAPACITY)
        {
            var available = Math.Max(0, offer.AvailablePlaces);
            throw new AppException(
                409,
                "INSUFFICIENT_CAPACITY",
                "Nincs elegendő szabad hely.",
                $"A kért {request.Quantity} helyből jelenleg {available} foglalható.",
                new Dictionary<string, object?> { ["availablePlaces"] = available });
        }

        offer.ReservedQuantity += request.Quantity;
        offer.UpdatedAtUtc = now;
        var bookingId = Guid.NewGuid();
        var bookingCode = await GenerateUniqueCodeAsync(bookingId, cancellationToken);
        var booking = new Booking
        {
            Id = bookingId,
            UserId = userId,
            OfferId = offer.Id,
            Status = BookingStatus.CONFIRMED,
            Quantity = request.Quantity,
            UnitPrice = offer.DiscountedUnitPrice,
            TotalPrice = BookingRules.CalculateTotal(offer.DiscountedUnitPrice, request.Quantity),
            Currency = offer.Currency,
            OfferTitle = offer.Title,
            OfferCategory = offer.Category,
            ProviderIdSnapshot = offer.ProviderId,
            ProviderName = offer.Provider.Name,
            StartsAtUtc = offer.StartsAtUtc,
            EndsAtUtc = offer.EndsAtUtc,
            CancellationDeadlineUtc = offer.CancelUntilUtc,
            PostalCode = offer.Provider.PostalCode,
            City = offer.Provider.City,
            Street = offer.Provider.Street,
            CountryCode = offer.Provider.CountryCode,
            Latitude = offer.Provider.Latitude,
            Longitude = offer.Provider.Longitude,
            ImageUrl = offer.ImageUrl,
            BookingCode = bookingCode,
            QrPayload = $"ugorjbe://booking/{bookingId}?code={bookingCode}",
            CreatedAtUtc = now
        };
        dbContext.Bookings.Add(booking);
        await dbContext.SaveChangesAsync(cancellationToken);
        await transaction.CommitAsync(cancellationToken);
        return DtoMapper.ToDto(booking, now);
    }

    public async Task<PageDto<BookingDto>> ListAsync(Guid userId, BookingQuery query, CancellationToken cancellationToken)
    {
        QueryValidation.Validate(query);
        var now = timeProvider.GetUtcNow();
        var source = dbContext.Bookings.AsNoTracking().Where(x => x.UserId == userId);
        source = query.Scope switch
        {
            BookingScope.ACTIVE => source.Where(x => x.Status == BookingStatus.CONFIRMED && x.EndsAtUtc > now),
            BookingScope.PREVIOUS => source.Where(x => x.Status == BookingStatus.CANCELLED ||
                                                        (x.Status == BookingStatus.CONFIRMED && x.EndsAtUtc <= now)),
            _ => source
        };
        var totalCount = await source.CountAsync(cancellationToken);
        var bookings = await source
            .OrderByDescending(x => x.CreatedAtUtc)
            .ThenBy(x => x.Id)
            .Skip((query.Page - 1) * query.PageSize)
            .Take(query.PageSize)
            .ToListAsync(cancellationToken);
        return PageDto<BookingDto>.Create(
            bookings.Select(x => DtoMapper.ToDto(x, now)).ToList(),
            query.Page,
            query.PageSize,
            totalCount);
    }

    public async Task<BookingDto> GetAsync(Guid userId, Guid bookingId, CancellationToken cancellationToken)
    {
        var booking = await dbContext.Bookings.AsNoTracking()
            .SingleOrDefaultAsync(x => x.Id == bookingId && x.UserId == userId, cancellationToken)
            ?? throw AppErrors.NotFound("booking");
        return DtoMapper.ToDto(booking, timeProvider.GetUtcNow());
    }

    public async Task<BookingDto> CancelAsync(Guid userId, Guid bookingId, CancellationToken cancellationToken)
    {
        await using var transaction = await dbContext.Database.BeginTransactionAsync(IsolationLevel.ReadCommitted, cancellationToken);
        var booking = await dbContext.Bookings
            .FromSqlInterpolated($"SELECT * FROM bookings WHERE id = {bookingId} AND user_id = {userId} FOR UPDATE")
            .SingleOrDefaultAsync(cancellationToken)
            ?? throw AppErrors.NotFound("booking");
        var now = timeProvider.GetUtcNow();

        if (booking.Status == BookingStatus.CANCELLED)
        {
            await transaction.CommitAsync(cancellationToken);
            return DtoMapper.ToDto(booking, now);
        }

        if (!BookingRules.CanCancel(booking, now))
        {
            throw new AppException(
                409,
                "CANCELLATION_NOT_ALLOWED",
                "A foglalás már nem mondható le.",
                "A lemondási határidő lejárt, vagy a program már elkezdődött.");
        }

        var offer = await dbContext.Offers
            .FromSqlInterpolated($"SELECT * FROM offers WHERE id = {booking.OfferId} FOR UPDATE")
            .SingleAsync(cancellationToken);
        if (offer.ReservedQuantity < booking.Quantity)
        {
            throw new InvalidOperationException("Stored offer capacity is inconsistent with the booking.");
        }

        booking.Status = BookingStatus.CANCELLED;
        booking.CancelledAtUtc = now;
        offer.ReservedQuantity -= booking.Quantity;
        offer.UpdatedAtUtc = now;
        await dbContext.SaveChangesAsync(cancellationToken);
        await transaction.CommitAsync(cancellationToken);
        return DtoMapper.ToDto(booking, now);
    }

    private async Task<string> GenerateUniqueCodeAsync(Guid bookingId, CancellationToken cancellationToken)
    {
        var bytes = bookingId.ToByteArray();
        for (var offset = 0; offset < 10; offset++)
        {
            var code = $"UGB-{Convert.ToHexString(bytes)[offset..(offset + 6)]}";
            if (!await dbContext.Bookings.AnyAsync(x => x.BookingCode == code, cancellationToken)) return code;
        }

        throw new InvalidOperationException("Could not generate a unique booking code.");
    }
}
