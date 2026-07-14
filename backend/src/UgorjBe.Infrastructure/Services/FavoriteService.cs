using Microsoft.EntityFrameworkCore;
using UgorjBe.Application;
using UgorjBe.Domain;
using UgorjBe.Infrastructure.Persistence;

namespace UgorjBe.Infrastructure.Services;

public sealed class FavoriteService(UgorjBeDbContext dbContext, TimeProvider timeProvider) : IFavoriteService
{
    public async Task<PageDto<OfferSummaryDto>> ListOffersAsync(Guid userId, PageQuery query, CancellationToken cancellationToken)
    {
        QueryValidation.Validate(query);
        var source = dbContext.FavoriteOffers.AsNoTracking().Where(x => x.UserId == userId);
        var totalCount = await source.CountAsync(cancellationToken);
        var favorites = await source
            .Include(x => x.Offer).ThenInclude(x => x.Provider)
            .OrderByDescending(x => x.CreatedAtUtc)
            .ThenBy(x => x.OfferId)
            .Skip((query.Page - 1) * query.PageSize)
            .Take(query.PageSize)
            .ToListAsync(cancellationToken);
        var items = favorites.Select(x => DtoMapper.ToSummary(x.Offer)).ToList();
        return PageDto<OfferSummaryDto>.Create(items, query.Page, query.PageSize, totalCount);
    }

    public async Task AddOfferAsync(Guid userId, Guid offerId, CancellationToken cancellationToken)
    {
        if (!await dbContext.Offers.AsNoTracking().AnyAsync(x => x.Id == offerId && x.PublishedAtUtc != null, cancellationToken))
        {
            throw AppErrors.NotFound("offer");
        }

        await dbContext.Database.ExecuteSqlInterpolatedAsync(
            $"INSERT INTO favorite_offers (user_id, offer_id, created_at_utc) VALUES ({userId}, {offerId}, {timeProvider.GetUtcNow()}) ON CONFLICT DO NOTHING",
            cancellationToken);
    }

    public async Task RemoveOfferAsync(Guid userId, Guid offerId, CancellationToken cancellationToken) =>
        await dbContext.FavoriteOffers
            .Where(x => x.UserId == userId && x.OfferId == offerId)
            .ExecuteDeleteAsync(cancellationToken);

    public async Task<PageDto<ProviderSummaryDto>> ListProvidersAsync(Guid userId, PageQuery query, CancellationToken cancellationToken)
    {
        QueryValidation.Validate(query);
        var source = dbContext.FavoriteProviders.AsNoTracking().Where(x => x.UserId == userId);
        var totalCount = await source.CountAsync(cancellationToken);
        var favorites = await source
            .Include(x => x.Provider)
            .OrderByDescending(x => x.CreatedAtUtc)
            .ThenBy(x => x.ProviderId)
            .Skip((query.Page - 1) * query.PageSize)
            .Take(query.PageSize)
            .ToListAsync(cancellationToken);
        var items = favorites.Select(x => DtoMapper.ToSummary(x.Provider)).ToList();
        return PageDto<ProviderSummaryDto>.Create(items, query.Page, query.PageSize, totalCount);
    }

    public async Task AddProviderAsync(Guid userId, Guid providerId, CancellationToken cancellationToken)
    {
        if (!await dbContext.Providers.AsNoTracking().AnyAsync(x => x.Id == providerId, cancellationToken))
        {
            throw AppErrors.NotFound("provider");
        }

        await dbContext.Database.ExecuteSqlInterpolatedAsync(
            $"INSERT INTO favorite_providers (user_id, provider_id, created_at_utc) VALUES ({userId}, {providerId}, {timeProvider.GetUtcNow()}) ON CONFLICT DO NOTHING",
            cancellationToken);
    }

    public async Task RemoveProviderAsync(Guid userId, Guid providerId, CancellationToken cancellationToken) =>
        await dbContext.FavoriteProviders
            .Where(x => x.UserId == userId && x.ProviderId == providerId)
            .ExecuteDeleteAsync(cancellationToken);
}
