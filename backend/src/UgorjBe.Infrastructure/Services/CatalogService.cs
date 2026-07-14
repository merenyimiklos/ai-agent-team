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

        var source = ApplyDiscoveryFilters(
            BaseDiscovery(now, startsFrom, startsTo, query.MinAvailablePlaces),
            query.Q, query.ProviderId, query.Category, query.ChildAge, query.MinPrice, query.MaxPrice,
            query.South, query.West, query.North, query.East,
            query.Latitude, query.Longitude, query.MaxDistanceKm);

        if (query.Sort == OfferSort.DISTANCE || query.MaxDistanceKm is not null)
        {
            return await MaterializeDistancePageAsync(source, query, cancellationToken);
        }

        var totalCount = await source.CountAsync(cancellationToken);
        var offers = await ApplyDatabaseSort(source, query.Sort, query.Direction)
            .ThenBy(x => x.Id)
            .Skip((query.Page - 1) * query.PageSize)
            .Take(query.PageSize)
            .ToListAsync(cancellationToken);
        return PageDto<OfferSummaryDto>.Create(
            offers.Select(x => DtoMapper.ToSummary(x, query.Latitude is null
                ? null
                : DtoMapper.DistanceKm(query.Latitude.Value, query.Longitude!.Value, x))).ToList(),
            query.Page,
            query.PageSize,
            totalCount);
    }

    public async Task<MapOfferEnvelope> GetMapOffersAsync(MapOfferQuery query, CancellationToken cancellationToken)
    {
        var now = timeProvider.GetUtcNow();
        var startsFrom = query.StartsFromUtc?.ToUniversalTime() ?? now;
        var startsTo = query.StartsToUtc?.ToUniversalTime() ?? EndOfBudapestDay(now);
        QueryValidation.Validate(query, startsFrom, startsTo);
        var source = ApplyDiscoveryFilters(
            BaseDiscovery(now, startsFrom, startsTo, query.MinAvailablePlaces),
            query.Q, query.ProviderId, query.Category, query.ChildAge, query.MinPrice, query.MaxPrice,
            query.South, query.West, query.North, query.East,
            query.Latitude, query.Longitude, query.MaxDistanceKm);

        // Distance sorting/radius needs exact Haversine after the indexed bounding predicates.
        if (query.Sort == OfferSort.DISTANCE || query.MaxDistanceKm is not null)
        {
            var candidates = await source.ToListAsync(cancellationToken);
            var projected = candidates.Select(x => new OfferWithDistance(
                x,
                query.Latitude is null ? null : DtoMapper.DistanceKm(query.Latitude.Value, query.Longitude!.Value, x)));
            if (query.MaxDistanceKm is not null)
            {
                projected = projected.Where(x => x.DistanceKm <= query.MaxDistanceKm);
            }

            var ordered = ApplyMemorySort(projected, query.Sort, query.Direction).ThenBy(x => x.Offer.Id);
            var materialized = ordered.Take(query.Limit + 1).ToList();
            return new MapOfferEnvelope(
                materialized.Take(query.Limit).Select(x => DtoMapper.ToSummary(x.Offer, x.DistanceKm)).ToList(),
                materialized.Count > query.Limit,
                query.Limit);
        }

        var offers = await ApplyDatabaseSort(source, query.Sort, query.Direction)
            .ThenBy(x => x.Id)
            .Take(query.Limit + 1)
            .ToListAsync(cancellationToken);
        return new MapOfferEnvelope(
            offers.Take(query.Limit).Select(x => DtoMapper.ToSummary(x, query.Latitude is null
                ? null
                : DtoMapper.DistanceKm(query.Latitude.Value, query.Longitude!.Value, x))).ToList(),
            offers.Count > query.Limit,
            query.Limit);
    }

    public async Task<OfferDetailDto> GetOfferAsync(Guid offerId, CoordinateQuery query, CancellationToken cancellationToken)
    {
        QueryValidation.Validate(query);
        var offer = await dbContext.Offers.AsNoTracking().Include(x => x.Provider)
            .SingleOrDefaultAsync(x => x.Id == offerId, cancellationToken)
            ?? throw AppErrors.NotFound("offer");
        if (offer.PublishedAtUtc is null)
        {
            throw AppErrors.NotFound("offer");
        }

        decimal? distance = query.Latitude is null
            ? null
            : DtoMapper.DistanceKm(query.Latitude.Value, query.Longitude!.Value, offer);
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
            x.StartsAtUtc > now && x.StartsAtUtc < endOfDay &&
            x.BookingCutoffUtc > now && x.TotalCapacity > x.ReservedQuantity, cancellationToken);
        var summary = DtoMapper.ToSummary(provider);
        return new ProviderDetailDto(
            summary.Id, summary.Name, summary.ShortDescription, provider.Description, summary.Address,
            provider.Phone, provider.Email, provider.WebsiteUrl, provider.AccessibilityInfo, provider.ImageUrl,
            activeOfferCount);
    }

    internal static DateTimeOffset EndOfBudapestDay(DateTimeOffset now)
    {
        var localNow = TimeZoneInfo.ConvertTime(now, BudapestTimeZone);
        var nextLocalMidnight = DateTime.SpecifyKind(localNow.Date.AddDays(1), DateTimeKind.Unspecified);
        return new DateTimeOffset(TimeZoneInfo.ConvertTimeToUtc(nextLocalMidnight, BudapestTimeZone), TimeSpan.Zero);
    }

    private IQueryable<Offer> BaseDiscovery(DateTimeOffset now, DateTimeOffset startsFrom, DateTimeOffset startsTo, int minimumAvailable) =>
        dbContext.Offers.AsNoTracking().Include(x => x.Provider).Where(x =>
            x.Status == OfferStatus.PUBLISHED &&
            x.BookingCutoffUtc > now && x.StartsAtUtc > now &&
            x.StartsAtUtc >= startsFrom && x.StartsAtUtc < startsTo &&
            x.TotalCapacity - x.ReservedQuantity >= minimumAvailable);

    private static IQueryable<Offer> ApplyDiscoveryFilters(
        IQueryable<Offer> source,
        string? q,
        Guid? providerId,
        List<OfferCategory>? categories,
        int? childAge,
        decimal? minPrice,
        decimal? maxPrice,
        decimal? south,
        decimal? west,
        decimal? north,
        decimal? east,
        decimal? latitude,
        decimal? longitude,
        decimal? maxDistanceKm)
    {
        var search = q?.Trim();
        if (!string.IsNullOrWhiteSpace(search))
        {
            var pattern = $"%{EscapeLike(search)}%";
            source = source.Where(x =>
                EF.Functions.ILike(x.Title, pattern, "\\") ||
                EF.Functions.ILike(x.Description, pattern, "\\") ||
                EF.Functions.ILike(x.Provider.Name, pattern, "\\"));
        }

        if (providerId is not null) source = source.Where(x => x.ProviderId == providerId);
        if (categories is { Count: > 0 }) source = source.Where(x => categories.Contains(x.Category));
        if (childAge is not null) source = source.Where(x => x.MinChildAge <= childAge && x.MaxChildAge >= childAge);
        if (minPrice is not null) source = source.Where(x => x.DiscountedUnitPrice >= minPrice);
        if (maxPrice is not null) source = source.Where(x => x.DiscountedUnitPrice <= maxPrice);
        if (south is not null)
        {
            source = source.Where(x =>
                x.Latitude >= south && x.Latitude < north &&
                x.Longitude >= west && x.Longitude < east);
        }

        // Radius queries first use a conservative indexed rectangle, then exact Haversine.
        if (maxDistanceKm is not null && latitude is not null && longitude is not null)
        {
            var latDelta = maxDistanceKm.Value / 110.574m;
            var cos = (decimal)Math.Max(0.01d, Math.Cos((double)latitude.Value * Math.PI / 180d));
            var lonDelta = maxDistanceKm.Value / (111.320m * cos);
            var minLat = latitude.Value - latDelta;
            var maxLat = latitude.Value + latDelta;
            var minLon = longitude.Value - lonDelta;
            var maxLon = longitude.Value + lonDelta;
            source = source.Where(x =>
                x.Latitude >= minLat && x.Latitude <= maxLat &&
                x.Longitude >= minLon && x.Longitude <= maxLon);
        }

        return source;
    }

    private static IOrderedQueryable<Offer> ApplyDatabaseSort(IQueryable<Offer> source, OfferSort sort, SortDirection direction) =>
        (sort, direction) switch
        {
            (OfferSort.PRICE, SortDirection.DESC) => source.OrderByDescending(x => x.DiscountedUnitPrice),
            (OfferSort.PRICE, _) => source.OrderBy(x => x.DiscountedUnitPrice),
            (OfferSort.DISCOUNT, SortDirection.DESC) => source.OrderByDescending(x => x.OriginalUnitPrice == 0 ? 0 : (x.OriginalUnitPrice - x.DiscountedUnitPrice) / x.OriginalUnitPrice),
            (OfferSort.DISCOUNT, _) => source.OrderBy(x => x.OriginalUnitPrice == 0 ? 0 : (x.OriginalUnitPrice - x.DiscountedUnitPrice) / x.OriginalUnitPrice),
            (_, SortDirection.DESC) => source.OrderByDescending(x => x.StartsAtUtc),
            _ => source.OrderBy(x => x.StartsAtUtc)
        };

    private static IOrderedEnumerable<OfferWithDistance> ApplyMemorySort(
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
        return direction == SortDirection.DESC ? source.OrderByDescending(selector) : source.OrderBy(selector);
    }

    private async Task<PageDto<OfferSummaryDto>> MaterializeDistancePageAsync(
        IQueryable<Offer> source,
        OfferQuery query,
        CancellationToken cancellationToken)
    {
        var offers = await source.ToListAsync(cancellationToken);
        var projected = offers.Select(x => new OfferWithDistance(
            x,
            query.Latitude is null ? null : DtoMapper.DistanceKm(query.Latitude.Value, query.Longitude!.Value, x)));
        if (query.MaxDistanceKm is not null) projected = projected.Where(x => x.DistanceKm <= query.MaxDistanceKm);
        var ordered = ApplyMemorySort(projected, query.Sort, query.Direction).ThenBy(x => x.Offer.Id).ToList();
        return PageDto<OfferSummaryDto>.Create(
            ordered.Skip((query.Page - 1) * query.PageSize).Take(query.PageSize)
                .Select(x => DtoMapper.ToSummary(x.Offer, x.DistanceKm)).ToList(),
            query.Page, query.PageSize, ordered.Count);
    }

    private static string EscapeLike(string value) =>
        value.Replace("\\", "\\\\", StringComparison.Ordinal)
            .Replace("%", "\\%", StringComparison.Ordinal)
            .Replace("_", "\\_", StringComparison.Ordinal);

    private sealed record OfferWithDistance(Offer Offer, decimal? DistanceKm);
}
