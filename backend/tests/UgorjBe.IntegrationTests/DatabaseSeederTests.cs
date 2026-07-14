using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using UgorjBe.Application;
using UgorjBe.Infrastructure.Persistence;
using Xunit;

namespace UgorjBe.IntegrationTests;

public sealed class DatabaseSeederTests
{
    private static readonly TimeZoneInfo BudapestTimeZone = TimeZoneInfo.FindSystemTimeZoneById("Europe/Budapest");

    [PostgresFact]
    public async Task Evening_initialization_keeps_primary_id_and_seeds_a_future_batch()
    {
        var eveningUtc = DateTimeOffset.Parse("2026-07-14T20:30:00Z");
        using var factory = new ApiFactory(eveningUtc);

        await factory.ResetAsync();

        await using var scope = factory.Services.CreateAsyncScope();
        var dbContext = scope.ServiceProvider.GetRequiredService<UgorjBeDbContext>();
        var primary = await dbContext.Offers.SingleAsync(x => x.Id == DatabaseSeeder.PrimaryOfferId);
        Assert.True(primary.BookingCutoffUtc > eveningUtc);
        Assert.Equal(
            new DateOnly(2026, 7, 15),
            DateOnly.FromDateTime(TimeZoneInfo.ConvertTime(primary.StartsAtUtc, BudapestTimeZone).Date));
        Assert.Equal(8, await dbContext.Offers.CountAsync());
    }

    [PostgresFact]
    public async Task Following_day_restarts_are_idempotent_and_append_without_rewriting_history()
    {
        var eveningUtc = DateTimeOffset.Parse("2026-07-14T20:30:00Z");
        using var factory = new ApiFactory(eveningUtc);
        await factory.ResetAsync();

        Guid bookingId;
        DateTimeOffset primaryStart;
        await using (var scope = factory.Services.CreateAsyncScope())
        {
            var bookingService = scope.ServiceProvider.GetRequiredService<IBookingService>();
            var booking = await bookingService.CreateAsync(
                DatabaseSeeder.DemoUserId,
                new CreateBookingRequest { OfferId = DatabaseSeeder.PrimaryOfferId, Quantity = 1 },
                CancellationToken.None);
            bookingId = booking.Id;
            primaryStart = booking.Offer.StartsAtUtc;
        }

        factory.Clock.SetUtcNow(DateTimeOffset.Parse("2026-07-15T08:00:00Z"));
        await SeedAgainAsync(factory);
        Assert.Equal(8, await OfferCountAsync(factory));

        factory.Clock.SetUtcNow(DateTimeOffset.Parse("2026-07-15T18:00:00Z"));
        await SeedAgainAsync(factory);
        Assert.Equal(15, await OfferCountAsync(factory));
        await SeedAgainAsync(factory);
        Assert.Equal(15, await OfferCountAsync(factory));

        await using var verificationScope = factory.Services.CreateAsyncScope();
        var dbContext = verificationScope.ServiceProvider.GetRequiredService<UgorjBeDbContext>();
        var primary = await dbContext.Offers.AsNoTracking().SingleAsync(x => x.Id == DatabaseSeeder.PrimaryOfferId);
        var bookingStillLinked = await dbContext.Bookings.AsNoTracking().SingleAsync(x => x.Id == bookingId);
        Assert.Equal(primaryStart, primary.StartsAtUtc);
        Assert.Equal(DatabaseSeeder.PrimaryOfferId, bookingStillLinked.OfferId);
        Assert.Equal(primaryStart, bookingStillLinked.StartsAtUtc);

        var nextDayOffers = (await dbContext.Offers.AsNoTracking()
                .Where(x => x.Id != DatabaseSeeder.HistoricalOfferId && x.Id != DatabaseSeeder.PrimaryOfferId)
                .ToListAsync())
            .Count(x => DateOnly.FromDateTime(TimeZoneInfo.ConvertTime(x.StartsAtUtc, BudapestTimeZone).Date) == new DateOnly(2026, 7, 16));
        Assert.Equal(7, nextDayOffers);
    }

    private static async Task SeedAgainAsync(ApiFactory factory)
    {
        await using var scope = factory.Services.CreateAsyncScope();
        await scope.ServiceProvider.GetRequiredService<DatabaseSeeder>().SeedAsync(CancellationToken.None);
    }

    private static async Task<int> OfferCountAsync(ApiFactory factory)
    {
        await using var scope = factory.Services.CreateAsyncScope();
        return await scope.ServiceProvider.GetRequiredService<UgorjBeDbContext>().Offers.CountAsync();
    }
}
