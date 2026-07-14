using System.Data;
using Microsoft.EntityFrameworkCore;
using UgorjBe.Application;
using UgorjBe.Domain;
using UgorjBe.Infrastructure.Persistence;

namespace UgorjBe.Infrastructure.Services;

public sealed class AdminService(UgorjBeDbContext dbContext, TimeProvider timeProvider) : IAdminService
{
    public async Task<AdminDashboardDto> GetDashboardAsync(CancellationToken cancellationToken)
    {
        var now = timeProvider.GetUtcNow();
        var offerCounts = await dbContext.Offers.AsNoTracking().GroupBy(x => x.Status)
            .Select(x => new { Status = x.Key, Count = x.Count() }).ToDictionaryAsync(x => x.Status, x => x.Count, cancellationToken);
        var next = await dbContext.Offers.AsNoTracking().Include(x => x.Provider)
            .Where(x => x.Status == OfferStatus.PUBLISHED && x.StartsAtUtc > now)
            .OrderBy(x => x.StartsAtUtc).ThenBy(x => x.Id).Take(5).ToListAsync(cancellationToken);
        var within24 = await dbContext.Offers.CountAsync(x => x.Status == OfferStatus.PUBLISHED && x.StartsAtUtc > now && x.StartsAtUtc <= now.AddHours(24), cancellationToken);
        return new AdminDashboardDto(
            await dbContext.Providers.CountAsync(cancellationToken),
            offerCounts.GetValueOrDefault(OfferStatus.DRAFT),
            offerCounts.GetValueOrDefault(OfferStatus.PUBLISHED),
            offerCounts.GetValueOrDefault(OfferStatus.UNPUBLISHED),
            offerCounts.GetValueOrDefault(OfferStatus.ARCHIVED),
            within24,
            next.Select(ToOfferSummary).ToList());
    }

    public async Task<PageDto<AdminProviderSummaryDto>> GetProvidersAsync(AdminProviderQuery query, CancellationToken cancellationToken)
    {
        AdminValidation.ProviderQuery(query);
        var now = timeProvider.GetUtcNow();
        var source = dbContext.Providers.AsNoTracking();
        var search = query.Q?.Trim();
        if (!string.IsNullOrWhiteSpace(search))
        {
            var pattern = $"%{EscapeLike(search)}%";
            source = source.Where(x => EF.Functions.ILike(x.Name, pattern, "\\") ||
                                       EF.Functions.ILike(x.Description, pattern, "\\") ||
                                       EF.Functions.ILike(x.City, pattern, "\\"));
        }

        var total = await source.CountAsync(cancellationToken);
        var providers = await source.OrderBy(x => x.Name).ThenBy(x => x.Id)
            .Skip((query.Page - 1) * query.PageSize).Take(query.PageSize).ToListAsync(cancellationToken);
        var ids = providers.Select(x => x.Id).ToList();
        var counts = await OfferCounts(ids, now, cancellationToken);
        return PageDto<AdminProviderSummaryDto>.Create(
            providers.Select(x => ToProviderSummary(x, counts.GetValueOrDefault(x.Id))).ToList(),
            query.Page, query.PageSize, total);
    }

    public async Task<AdminProviderDetailDto> GetProviderAsync(Guid id, CancellationToken cancellationToken)
    {
        var provider = await dbContext.Providers.AsNoTracking().SingleOrDefaultAsync(x => x.Id == id, cancellationToken)
            ?? throw AppErrors.NotFound("provider");
        var counts = await OfferCounts([id], timeProvider.GetUtcNow(), cancellationToken);
        return ToProviderDetail(provider, counts.GetValueOrDefault(id));
    }

    public async Task<AdminProviderDetailDto> CreateProviderAsync(ProviderCreateRequest request, CancellationToken cancellationToken)
    {
        AdminValidation.Provider(request);
        var now = timeProvider.GetUtcNow();
        var provider = new Provider { Id = Guid.NewGuid(), CreatedAtUtc = now, UpdatedAtUtc = now };
        ApplyProvider(provider, request);
        dbContext.Providers.Add(provider);
        await dbContext.SaveChangesAsync(cancellationToken);
        return ToProviderDetail(provider, default);
    }

    public async Task<AdminProviderDetailDto> UpdateProviderAsync(Guid id, ProviderUpdateRequest request, CancellationToken cancellationToken)
    {
        AdminValidation.Provider(request, true, request.Version);
        var expected = AdminValidation.ParseVersion(request.Version);
        var provider = await dbContext.Providers.SingleOrDefaultAsync(x => x.Id == id, cancellationToken)
            ?? throw AppErrors.NotFound("provider");
        EnsureVersion(provider.Version, expected);
        ApplyProvider(provider, request);
        provider.UpdatedAtUtc = timeProvider.GetUtcNow();
        await SaveConcurrencyAsync(cancellationToken);
        var counts = await OfferCounts([id], timeProvider.GetUtcNow(), cancellationToken);
        return ToProviderDetail(provider, counts.GetValueOrDefault(id));
    }

    public async Task<PageDto<AdminOfferSummaryDto>> GetOffersAsync(AdminOfferQuery query, CancellationToken cancellationToken)
    {
        AdminValidation.OfferQuery(query);
        var source = dbContext.Offers.AsNoTracking().Include(x => x.Provider).AsQueryable();
        var search = query.Q?.Trim();
        if (!string.IsNullOrWhiteSpace(search))
        {
            var pattern = $"%{EscapeLike(search)}%";
            source = source.Where(x => EF.Functions.ILike(x.Title, pattern, "\\") ||
                                       EF.Functions.ILike(x.Description, pattern, "\\") ||
                                       EF.Functions.ILike(x.Provider.Name, pattern, "\\"));
        }

        if (query.ProviderId is not null) source = source.Where(x => x.ProviderId == query.ProviderId);
        if (query.Category is { Count: > 0 }) source = source.Where(x => query.Category.Contains(x.Category));
        if (query.Status is { Count: > 0 }) source = source.Where(x => query.Status.Contains(x.Status));
        if (query.StartsFromUtc is not null) source = source.Where(x => x.StartsAtUtc >= query.StartsFromUtc);
        if (query.StartsToUtc is not null) source = source.Where(x => x.StartsAtUtc < query.StartsToUtc);
        var total = await source.CountAsync(cancellationToken);
        var offers = await source.OrderBy(x => x.StartsAtUtc).ThenBy(x => x.Id)
            .Skip((query.Page - 1) * query.PageSize).Take(query.PageSize).ToListAsync(cancellationToken);
        return PageDto<AdminOfferSummaryDto>.Create(offers.Select(ToOfferSummary).ToList(), query.Page, query.PageSize, total);
    }

    public async Task<AdminOfferDetailDto> GetOfferAsync(Guid id, CancellationToken cancellationToken)
    {
        var offer = await dbContext.Offers.AsNoTracking().Include(x => x.Provider).SingleOrDefaultAsync(x => x.Id == id, cancellationToken)
            ?? throw AppErrors.NotFound("offer");
        return ToOfferDetail(offer);
    }

    public async Task<AdminOfferDetailDto> CreateOfferAsync(OfferWriteRequest request, CancellationToken cancellationToken)
    {
        AdminValidation.Offer(request);
        var provider = await dbContext.Providers.SingleOrDefaultAsync(x => x.Id == request.ProviderId, cancellationToken)
            ?? throw AppErrors.NotFound("provider");
        var now = timeProvider.GetUtcNow();
        var offer = new Offer
        {
            Id = Guid.NewGuid(),
            Provider = provider,
            ProviderId = provider.Id,
            Status = OfferStatus.DRAFT,
            CreatedAtUtc = now,
            UpdatedAtUtc = now
        };
        ApplyOffer(offer, request);
        dbContext.Offers.Add(offer);
        await dbContext.SaveChangesAsync(cancellationToken);
        return ToOfferDetail(offer);
    }

    public async Task<AdminOfferDetailDto> UpdateOfferAsync(Guid id, OfferUpdateRequest request, CancellationToken cancellationToken)
    {
        AdminValidation.Offer(request, true, request.Version);
        var expected = AdminValidation.ParseVersion(request.Version);
        await using var transaction = await dbContext.Database.BeginTransactionAsync(IsolationLevel.ReadCommitted, cancellationToken);
        var offer = await dbContext.Offers.Include(x => x.Provider).SingleOrDefaultAsync(x => x.Id == id, cancellationToken)
            ?? throw AppErrors.NotFound("offer");
        EnsureVersion(offer.Version, expected);
        if (offer.Status == OfferStatus.ARCHIVED) throw InvalidTransition(offer.Status);
        if (!await dbContext.Providers.AnyAsync(x => x.Id == request.ProviderId, cancellationToken)) throw AppErrors.NotFound("provider");
        if (request.TotalCapacity < offer.ReservedQuantity)
        {
            throw new AppException(409, "OFFER_UPDATE_CONFLICT", "Az ajánlat nem módosítható.", "A kapacitás nem lehet a foglalt helyek száma alatt.",
                new Dictionary<string, object?> { ["conflictingFields"] = new[] { "totalCapacity" } });
        }

        var hasConfirmed = await dbContext.Bookings.AnyAsync(x => x.OfferId == id && x.Status == BookingStatus.CONFIRMED, cancellationToken);
        if (hasConfirmed)
        {
            var conflicts = ProtectedFieldChanges(offer, request);
            if (conflicts.Count > 0)
            {
                throw new AppException(409, "OFFER_UPDATE_CONFLICT", "Az ajánlat védett adatai nem módosíthatók.", "Az ajánlathoz megerősített foglalás tartozik.",
                    new Dictionary<string, object?> { ["conflictingFields"] = conflicts });
            }
        }

        ApplyOffer(offer, request);
        offer.Provider = await dbContext.Providers.SingleAsync(x => x.Id == request.ProviderId, cancellationToken);
        offer.UpdatedAtUtc = timeProvider.GetUtcNow();
        await SaveConcurrencyAsync(cancellationToken);
        await transaction.CommitAsync(cancellationToken);
        return ToOfferDetail(offer);
    }

    public Task<AdminOfferDetailDto> PublishAsync(Guid id, LifecycleRequest request, CancellationToken cancellationToken) =>
        TransitionAsync(id, request, OfferStatus.PUBLISHED, [OfferStatus.DRAFT, OfferStatus.UNPUBLISHED], cancellationToken);

    public Task<AdminOfferDetailDto> UnpublishAsync(Guid id, LifecycleRequest request, CancellationToken cancellationToken) =>
        TransitionAsync(id, request, OfferStatus.UNPUBLISHED, [OfferStatus.PUBLISHED], cancellationToken);

    public Task<AdminOfferDetailDto> ArchiveAsync(Guid id, LifecycleRequest request, CancellationToken cancellationToken) =>
        TransitionAsync(id, request, OfferStatus.ARCHIVED, [OfferStatus.DRAFT, OfferStatus.PUBLISHED, OfferStatus.UNPUBLISHED], cancellationToken);

    private async Task<AdminOfferDetailDto> TransitionAsync(Guid id, LifecycleRequest request, OfferStatus target, OfferStatus[] allowed, CancellationToken cancellationToken)
    {
        var expected = AdminValidation.ParseVersion(request.Version);
        await using var transaction = await dbContext.Database.BeginTransactionAsync(IsolationLevel.ReadCommitted, cancellationToken);
        var offer = await dbContext.Offers.Include(x => x.Provider).SingleOrDefaultAsync(x => x.Id == id, cancellationToken)
            ?? throw AppErrors.NotFound("offer");
        EnsureVersion(offer.Version, expected);
        if (!allowed.Contains(offer.Status)) throw InvalidTransition(offer.Status);
        var now = timeProvider.GetUtcNow();
        if (target == OfferStatus.PUBLISHED)
        {
            var errors = new Dictionary<string, string[]>();
            if (offer.BookingCutoffUtc <= now) errors["bookingCutoffUtc"] = ["A foglalási határidőnek a jövőben kell lennie."];
            if (offer.StartsAtUtc <= now) errors["startsAtUtc"] = ["A kezdésnek a jövőben kell lennie."];
            if (offer.AvailablePlaces <= 0) errors["totalCapacity"] = ["Legalább egy szabad hely szükséges."];
            if (errors.Count > 0)
            {
                throw new AppException(409, "OFFER_PUBLISH_NOT_READY", "Az ajánlat nem publikálható.", "Javítsd a publikálást akadályozó mezőket.",
                    new Dictionary<string, object?> { ["errors"] = errors });
            }

            offer.PublishedAtUtc ??= now;
        }

        if (target == OfferStatus.ARCHIVED) offer.ArchivedAtUtc = now;
        offer.Status = target;
        offer.UpdatedAtUtc = now;
        await SaveConcurrencyAsync(cancellationToken);
        await transaction.CommitAsync(cancellationToken);
        return ToOfferDetail(offer);
    }

    private async Task<Dictionary<Guid, OfferCount>> OfferCounts(IReadOnlyCollection<Guid> ids, DateTimeOffset now, CancellationToken cancellationToken)
    {
        if (ids.Count == 0) return [];
        return await dbContext.Offers.AsNoTracking().Where(x => ids.Contains(x.ProviderId)).GroupBy(x => x.ProviderId)
            .Select(x => new OfferCount(x.Key, x.Count(y => y.Status == OfferStatus.PUBLISHED && y.StartsAtUtc > now && y.BookingCutoffUtc > now && y.TotalCapacity > y.ReservedQuantity), x.Count()))
            .ToDictionaryAsync(x => x.ProviderId, cancellationToken);
    }

    private static void ApplyProvider(Provider provider, ProviderCreateRequest request)
    {
        var address = request.Address!;
        provider.Name = request.Name.Trim(); provider.ShortDescription = request.ShortDescription.Trim(); provider.Description = request.Description.Trim();
        provider.PostalCode = address.PostalCode.Trim(); provider.City = address.City.Trim(); provider.Street = address.Street.Trim(); provider.CountryCode = address.CountryCode;
        provider.Latitude = address.Latitude; provider.Longitude = address.Longitude;
        provider.Phone = TrimNullable(request.Phone); provider.Email = TrimNullable(request.Email); provider.WebsiteUrl = TrimNullable(request.WebsiteUrl);
        provider.AccessibilityInfo = TrimNullable(request.AccessibilityInfo); provider.ImageUrl = TrimNullable(request.ImageUrl);
    }

    private static void ApplyOffer(Offer offer, OfferWriteRequest request)
    {
        var address = request.Address!;
        offer.ProviderId = request.ProviderId; offer.Title = request.Title.Trim(); offer.Description = request.Description.Trim(); offer.Category = request.Category;
        offer.PostalCode = address.PostalCode.Trim(); offer.City = address.City.Trim(); offer.Street = address.Street.Trim(); offer.CountryCode = address.CountryCode;
        offer.Latitude = address.Latitude; offer.Longitude = address.Longitude;
        offer.StartsAtUtc = request.StartsAtUtc; offer.EndsAtUtc = request.EndsAtUtc; offer.BookingCutoffUtc = request.BookingCutoffUtc; offer.CancelUntilUtc = request.CancelUntilUtc;
        offer.MinChildAge = request.MinChildAge; offer.MaxChildAge = request.MaxChildAge; offer.AccompanimentRequired = request.AccompanimentRequired;
        offer.AccessibilityInfo = TrimNullable(request.AccessibilityInfo); offer.OriginalUnitPrice = request.OriginalUnitPrice!.Amount;
        offer.DiscountedUnitPrice = request.DiscountedUnitPrice!.Amount; offer.Currency = request.OriginalUnitPrice.Currency;
        offer.TotalCapacity = request.TotalCapacity; offer.ImageUrl = TrimNullable(request.ImageUrl);
    }

    private static List<string> ProtectedFieldChanges(Offer offer, OfferWriteRequest request)
    {
        var a = request.Address!;
        var conflicts = new List<string>();
        if (offer.ProviderId != request.ProviderId) conflicts.Add("providerId");
        if (offer.PostalCode != a.PostalCode.Trim() || offer.City != a.City.Trim() || offer.Street != a.Street.Trim() || offer.CountryCode != a.CountryCode || offer.Latitude != a.Latitude || offer.Longitude != a.Longitude) conflicts.Add("address");
        if (offer.StartsAtUtc != request.StartsAtUtc) conflicts.Add("startsAtUtc");
        if (offer.EndsAtUtc != request.EndsAtUtc) conflicts.Add("endsAtUtc");
        if (offer.CancelUntilUtc != request.CancelUntilUtc) conflicts.Add("cancelUntilUtc");
        if (!offer.Currency.Equals(request.OriginalUnitPrice!.Currency, StringComparison.Ordinal)) conflicts.Add("currency");
        return conflicts;
    }

    private static AdminProviderSummaryDto ToProviderSummary(Provider p, OfferCount counts) => new(
        p.Id, p.Name, p.ShortDescription, ProviderAddress(p), p.ImageUrl, counts.Active, counts.Total, p.UpdatedAtUtc, Version(p.Version));

    private static AdminProviderDetailDto ToProviderDetail(Provider p, OfferCount counts) => new(
        p.Id, p.Name, p.ShortDescription, p.Description, ProviderAddress(p), p.Phone, p.Email, p.WebsiteUrl, p.AccessibilityInfo, p.ImageUrl,
        counts.Active, counts.Total, p.CreatedAtUtc, p.UpdatedAtUtc, Version(p.Version));

    private static AdminOfferSummaryDto ToOfferSummary(Offer o) => new(
        o.Id, o.ProviderId, o.Provider.Name, o.Title, o.Category, o.Status, o.StartsAtUtc, o.EndsAtUtc, OfferAddress(o),
        new MoneyDto(o.DiscountedUnitPrice, o.Currency), o.TotalCapacity, o.ReservedQuantity, Math.Max(0, o.AvailablePlaces), o.UpdatedAtUtc, Version(o.Version));

    private static AdminOfferDetailDto ToOfferDetail(Offer o) => new(
        o.Id, o.ProviderId, o.Provider.Name, o.Title, o.Description, o.Category, o.Status, OfferAddress(o), o.StartsAtUtc, o.EndsAtUtc,
        o.BookingCutoffUtc, o.CancelUntilUtc, o.MinChildAge, o.MaxChildAge, o.AccompanimentRequired, o.AccessibilityInfo,
        new MoneyDto(o.OriginalUnitPrice, o.Currency), new MoneyDto(o.DiscountedUnitPrice, o.Currency), o.TotalCapacity, o.ReservedQuantity,
        Math.Max(0, o.AvailablePlaces), o.ImageUrl, o.PublishedAtUtc, o.ArchivedAtUtc, o.CreatedAtUtc, o.UpdatedAtUtc, Version(o.Version));

    private static AddressDto ProviderAddress(Provider p) => new(p.PostalCode, p.City, p.Street, p.CountryCode, p.Latitude, p.Longitude);
    private static AddressDto OfferAddress(Offer o) => new(o.PostalCode, o.City, o.Street, o.CountryCode, o.Latitude, o.Longitude);
    private static string Version(uint version) => version.ToString(System.Globalization.CultureInfo.InvariantCulture);
    private static string? TrimNullable(string? value) => string.IsNullOrWhiteSpace(value) ? null : value.Trim();
    private static string EscapeLike(string value) => value.Replace("\\", "\\\\", StringComparison.Ordinal).Replace("%", "\\%", StringComparison.Ordinal).Replace("_", "\\_", StringComparison.Ordinal);

    private static void EnsureVersion(uint actual, uint expected)
    {
        if (actual != expected) throw ConcurrencyConflict();
    }

    private async Task SaveConcurrencyAsync(CancellationToken cancellationToken)
    {
        try { await dbContext.SaveChangesAsync(cancellationToken); }
        catch (DbUpdateConcurrencyException) { throw ConcurrencyConflict(); }
    }

    private static AppException ConcurrencyConflict() => new(409, "CONCURRENCY_CONFLICT", "Az adat időközben megváltozott.", "Töltsd újra az adatlapot, majd ismételd meg a módosítást.");
    private static AppException InvalidTransition(OfferStatus status) => new(409, "OFFER_STATE_TRANSITION_INVALID", "Az állapotváltás nem engedélyezett.", "Az ajánlat jelenlegi állapotából ez a művelet nem hajtható végre.",
        new Dictionary<string, object?> { ["currentStatus"] = status.ToString() });

    private readonly record struct OfferCount(Guid ProviderId, int Active, int Total);
}
