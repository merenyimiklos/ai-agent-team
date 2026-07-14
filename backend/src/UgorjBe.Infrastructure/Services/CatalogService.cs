using Microsoft.EntityFrameworkCore;
using UgorjBe.Application;
using UgorjBe.Domain;
using UgorjBe.Infrastructure.Persistence;

namespace UgorjBe.Infrastructure.Services;

public sealed class CatalogService(UgorjBeDbContext dbContext, TimeProvider timeProvider) : ICatalogService
{
    private static readonly TimeZoneInfo BudapestTimeZone = TimeZoneInfo.FindSystemTimeZoneById("Europe/Budapest");

    public async Task<PageDto<OfferSummaryDto>> GetOffersAsync(OfferQuery query, CancellationToken cancellationToken)
    {
        var now = timeProvider.GetUtcNow();
        var startsFrom = query.StartsFromUtc?.ToUniversalTime() ?? now;
        var startsTo = query.StartsToUtc?.ToUniversalTime() ?? EndOfBudapestDay(now);
        QueryValidation.Validate(query, startsFrom, startsTo);

        var databaseQuery = dbContext.Offers
            .AsNoTracking()
            .Include(x => x.Provider)
            .Where(x => x.Status == OfferStatus.PUBLISHED &&
                        x.BookingCutoffUtc > now &&
                        x.StartsAtUtc > now &&
                        x.StartsAtUtc >= startsFrom &&
                        x.StartsAtUtc < startsTo &&
                        x.TotalCapacity - x.ReservedQuantity >= query.MinAvailablePlaces);

        var search = query.Q?.Trim();
        if (!string.IsNullOrWhiteSpace(search))
        {
            var pattern = $"%{EscapeLike(search)}%";
            databaseQuery = databaseQuery.Where(x =>
                EF.Functions.ILike(x.Title, pattern, "\\") ||
                EF.Functions.ILike(x.Description, pattern, "\\") ||
                EF.Functions.ILike(x.Provider.Name, pattern, "\\"));
        }

        if (query.ProviderId is not null) databaseQuery = databaseQuery.Where(x => x.ProviderId == query.ProviderId);
        if (query.Category is { Count: > 0 }) databaseQuery = databaseQuery.Where(x => query.Category.Contains(x.Category));
        if (query.ChildAge is not null) databaseQuery = databaseQuery.Where(x => x.MinChildAge <= query.ChildAge && x.MaxChildAge >= query.ChildAge);
        if (query.MinPrice is not null) databaseQuery = databaseQuery.Where(x => x.DiscountedUnitPrice >= query.MinPrice);
        if (query.MaxPrice is not null) databaseQuery = databaseQuery.Where(x => x.DiscountedUnitPrice <= query.MaxPrice);

        var offers = await databaseQuery.ToListAsync(cancellationToken);
        var projected = offers.Select(x => new OfferWithDistance(
            x,
            query.Latitude is not null
                ? DtoMapper.DistanceKm(query.Latitude.Value, query.Longitude!.Value, x.Provider)
                : null));

        if (query.MaxDistanceKm is not null) projected = projected.Where(x => x.DistanceKm <= query.MaxDistanceKm);

        var sorted = ApplySort(projected, query.Sort, query.Direction);
        var ordered = sorted.ThenBy(x => x.Offer.Id).ToList();
        var totalCount = ordered.Count;
        var items = ordered
            .Skip((query.Page - 1) * query.PageSize)
            .Take(query.PageSize)
            .Select(x => DtoMapper.ToSummary(x.Offer, x.DistanceKm))
            .ToList();

        return PageDto<OfferSummaryDto>.Create(items, query.Page, query.PageSize, totalCount);
    }

    public async Task<OfferDetailDto> GetOfferAsync(Guid offerId, CoordinateQuery query, CancellationToken cancellationToken)
    {
        QueryValidation.Validate(query);
        var offer = await dbContext.Offers.AsNoTracking().Include(x => x.Provider)
            .SingleOrDefaultAsync(x => x.Id == offerId, cancellationToken)
            ?? throw AppErrors.NotFound("offer");
        decimal? distance = query.Latitude is null
            ? null
            : DtoMapper.DistanceKm(query.Latitude.Value, query.Longitude!.Value, offer.Provider);
        return DtoMapper.ToDetail(offer, timeProvider.GetUtcNow(), distance);
    }

    public async Task<ProviderDetailDto> GetProviderAsync(Guid providerId, CancellationToken cancellationToken)
    {
        var provider = await dbContext.Providers.AsNoTracking()
            .SingleOrDefaultAsync(x => x.Id == providerId, cancellationToken)
            ?? throw AppErrors.NotFound("provider");
        var now = timeProvider.GetUtcNow();
        var endOfDay = EndOfBudapestDay(now);
        var activeOfferCount = await dbContext.Offers.CountAsync(x =>
            x.ProviderId == providerId &&
            x.Status == OfferStatus.PUBLISHED &&
            x.StartsAtUtc > now &&
            x.StartsAtUtc < endOfDay &&
            x.BookingCutoffUtc > now &&
            x.TotalCapacity > x.ReservedQuantity, cancellationToken);
        var summary = DtoMapper.ToSummary(provider);
        return new ProviderDetailDto(
            summary.Id,
            summary.Name,
            summary.ShortDescription,
            provider.Description,
            summary.Address,
            provider.Phone,
            provider.Email,
            provider.WebsiteUrl,
            provider.AccessibilityInfo,
            provider.ImageUrl,
            activeOfferCount);
    }

    internal static DateTimeOffset EndOfBudapestDay(DateTimeOffset now)
    {
        var localNow = TimeZoneInfo.ConvertTime(now, BudapestTimeZone);
        var nextLocalMidnight = DateTime.SpecifyKind(localNow.Date.AddDays(1), DateTimeKind.Unspecified);
        return new DateTimeOffset(TimeZoneInfo.ConvertTimeToUtc(nextLocalMidnight, BudapestTimeZone), TimeSpan.Zero);
    }

    private static IOrderedEnumerable<OfferWithDistance> ApplySort(
        IEnumerable<OfferWithDistance> source,
        OfferSort sort,
        SortDirection direction)
    {
        Func<OfferWithDistance, object> selector = sort switch
        {
            OfferSort.PRICE => x => x.Offer.DiscountedUnitPrice,
            OfferSort.DISTANCE => x => x.DistanceKm ?? decimal.MaxValue,
            OfferSort.DISCOUNT => x => DtoMapper.DiscountPercent(x.Offer),
            _ => x => x.Offer.StartsAtUtc
        };
        return direction == SortDirection.DESC
            ? source.OrderByDescending(selector)
            : source.OrderBy(selector);
    }

    private static string EscapeLike(string value) =>
        value.Replace("\\", "\\\\", StringComparison.Ordinal)
            .Replace("%", "\\%", StringComparison.Ordinal)
            .Replace("_", "\\_", StringComparison.Ordinal);

    private sealed record OfferWithDistance(Offer Offer, decimal? DistanceKm);
}
