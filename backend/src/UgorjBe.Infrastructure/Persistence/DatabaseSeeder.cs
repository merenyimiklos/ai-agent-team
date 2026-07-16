using System.Security.Cryptography;
using System.Text;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using UgorjBe.Domain;

namespace UgorjBe.Infrastructure.Persistence;

public sealed class DatabaseSeeder(
    UgorjBeDbContext dbContext,
    IPasswordHasher<User> passwordHasher,
    TimeProvider timeProvider)
{
    public static readonly Guid DemoUserId = Guid.Parse("11111111-1111-1111-1111-111111111111");
    public static readonly Guid DemoAdminId = Guid.Parse("11111111-1111-1111-1111-111111111112");
    public static readonly Guid PrimaryProviderId = Guid.Parse("22222222-2222-2222-2222-222222222222");
    public static readonly Guid PrimaryOfferId = Guid.Parse("33333333-3333-3333-3333-333333333333");
    public static readonly Guid HistoricalOfferId = Guid.Parse("33333333-3333-3333-3333-333333333340");

    private static readonly Guid[] InitialBatchOfferIds =
    [
        PrimaryOfferId,
        Guid.Parse("33333333-3333-3333-3333-333333333334"),
        Guid.Parse("33333333-3333-3333-3333-333333333335"),
        Guid.Parse("33333333-3333-3333-3333-333333333336"),
        Guid.Parse("33333333-3333-3333-3333-333333333337"),
        Guid.Parse("33333333-3333-3333-3333-333333333338"),
        Guid.Parse("33333333-3333-3333-3333-333333333339")
    ];

    private static readonly TimeZoneInfo BudapestTimeZone = TimeZoneInfo.FindSystemTimeZoneById("Europe/Budapest");

    public async Task SeedAsync(CancellationToken cancellationToken)
    {
        var now = timeProvider.GetUtcNow();
        var user = await dbContext.Users.SingleOrDefaultAsync(x => x.Id == DemoUserId, cancellationToken);
        if (user is null)
        {
            user = new User
            {
                Id = DemoUserId,
                Email = "demo@ugorjbe.local",
                NormalizedEmail = "DEMO@UGORJBE.LOCAL",
                DisplayName = "Demó Család",
                Locale = "hu-HU",
                Role = "customer",
                CreatedAtUtc = now
            };
            user.PasswordHash = passwordHasher.HashPassword(user, "UgorjBe123!");
            dbContext.Users.Add(user);
            await dbContext.SaveChangesAsync(cancellationToken);
        }

        if (!await dbContext.Users.AnyAsync(x => x.Id == DemoAdminId || x.NormalizedEmail == "ADMIN@UGORJBE.LOCAL", cancellationToken))
        {
            var admin = new User
            {
                Id = DemoAdminId,
                Email = "admin@ugorjbe.local",
                NormalizedEmail = "ADMIN@UGORJBE.LOCAL",
                DisplayName = "UgorjBe Admin",
                Locale = "hu-HU",
                Role = "admin",
                CreatedAtUtc = now
            };
            admin.PasswordHash = passwordHasher.HashPassword(admin, "UgorjBeAdmin123!");
            dbContext.Users.Add(admin);
            await dbContext.SaveChangesAsync(cancellationToken);
        }

        var providers = CreateProviders(now);
        var providerIds = providers.Select(x => x.Id).ToArray();
        var existingProviderIds = await dbContext.Providers
            .Where(x => providerIds.Contains(x.Id))
            .Select(x => x.Id)
            .ToListAsync(cancellationToken);
        var missingProviders = providers.Where(x => !existingProviderIds.Contains(x.Id)).ToList();
        if (missingProviders.Count > 0)
        {
            dbContext.Providers.AddRange(missingProviders);
            await dbContext.SaveChangesAsync(cancellationToken);
        }

        var primaryOffer = await dbContext.Offers.AsNoTracking()
            .SingleOrDefaultAsync(x => x.Id == PrimaryOfferId, cancellationToken);
        var hasUpcomingBatch = await dbContext.Offers.AnyAsync(
            x => x.Status == OfferStatus.PUBLISHED &&
                 x.BookingCutoffUtc > now &&
                 x.StartsAtUtc < now.AddHours(24),
            cancellationToken);
        var missingOffers = new List<Offer>();
        if (!hasUpcomingBatch)
        {
            var localBatchStart = GetLocalBatchStart(now);
            var batch = CreateOfferBatch(now, localBatchStart, primaryOffer is null);
            var batchIds = batch.Select(x => x.Id).ToArray();
            var existingOfferIds = await dbContext.Offers
                .Where(x => batchIds.Contains(x.Id))
                .Select(x => x.Id)
                .ToListAsync(cancellationToken);
            missingOffers.AddRange(batch.Where(x => !existingOfferIds.Contains(x.Id)));
        }

        if (!await dbContext.Offers.AnyAsync(x => x.Id == HistoricalOfferId, cancellationToken))
        {
            missingOffers.Add(CreateHistoricalOffer(now));
        }

        if (missingOffers.Count > 0)
        {
            dbContext.Offers.AddRange(missingOffers);
            await dbContext.SaveChangesAsync(cancellationToken);
        }

        if (!await dbContext.FavoriteOffers.AnyAsync(x => x.UserId == DemoUserId, cancellationToken))
        {
            dbContext.FavoriteOffers.Add(new FavoriteOffer
            {
                UserId = DemoUserId,
                OfferId = PrimaryOfferId,
                CreatedAtUtc = now
            });
        }

        if (!await dbContext.FavoriteProviders.AnyAsync(x => x.UserId == DemoUserId, cancellationToken))
        {
            dbContext.FavoriteProviders.Add(new FavoriteProvider
            {
                UserId = DemoUserId,
                ProviderId = PrimaryProviderId,
                CreatedAtUtc = now
            });
        }

        await dbContext.SaveChangesAsync(cancellationToken);
    }

    private static IReadOnlyList<Provider> CreateProviders(DateTimeOffset now) =>
    [
        Provider(PrimaryProviderId, "Kerek Erdő Műhely", "Kézműves programok családoknak.", "Barátságos alkotóműhely minden szükséges eszközzel.", "1137", "Budapest", "Pozsonyi út 12.", 47.518200m, 19.050400m, "Babakocsival megközelíthető.", now),
        Provider(Guid.Parse("22222222-2222-2222-2222-222222222223"), "Pöttöm Mozgástér", "Játékos mozgás és családi torna.", "Kis létszámú, korosztályos mozgásprogramok képzett oktatókkal.", "1117", "Budapest", "Karinthy Frigyes út 18.", 47.475900m, 19.052000m, "Liftes épület és akadálymentes mosdó.", now),
        Provider(Guid.Parse("22222222-2222-2222-2222-222222222224"), "Hullámka Tanuszoda", "Családi és kezdő úszásfoglalkozások.", "Meleg vizű tanmedence kezdő és szülő-gyermek alkalmakhoz.", "1146", "Budapest", "Istvánmezei út 6.", 47.507400m, 19.093600m, null, now),
        Provider(Guid.Parse("22222222-2222-2222-2222-222222222225"), "Városi Felfedező", "Múzeumi játék és helytörténeti kaland.", "Interaktív városi és múzeumi foglalkozások iskoláskorú gyerekeknek.", "1088", "Budapest", "Múzeum körút 14.", 47.490700m, 19.061400m, "Akadálymentes bejárat.", now)
    ];

    internal static DateTime GetLocalBatchStart(DateTimeOffset now)
    {
        var localNow = TimeZoneInfo.ConvertTime(now, BudapestTimeZone);
        var candidate = localNow.AddMinutes(75);
        var roundedMinutes = candidate.Minute < 30 ? 30 : 0;
        var roundedHour = candidate.Minute < 30 ? candidate.Hour : candidate.Hour + 1;
        var localBase = candidate.Date.AddHours(roundedHour).AddMinutes(roundedMinutes);
        if (localBase.Date != localNow.Date || localBase.Hour > 20)
        {
            localBase = localNow.Date.AddDays(1).AddHours(9);
        }

        // Keep a batch on one Budapest calendar date so its deterministic IDs can be
        // regenerated on restart without moving or replacing booking-linked history.
        if (localBase.AddHours(6).Date != localBase.Date)
        {
            localBase = localNow.Date.AddDays(1).AddHours(9);
        }

        return localBase;
    }

    internal static IReadOnlyList<Offer> CreateOfferBatch(
        DateTimeOffset now,
        DateTime localBatchStart,
        bool useInitialIds)
    {
        var ids = useInitialIds
            ? InitialBatchOfferIds
            : Enumerable.Range(0, InitialBatchOfferIds.Length)
                .Select(index => BatchScopedOfferId(localBatchStart, index))
                .ToArray();

        var starts = Enumerable.Range(0, InitialBatchOfferIds.Length)
            .Select(index => Utc(localBatchStart.AddHours(index)))
            .ToArray();
        var providers = new[]
        {
            PrimaryProviderId,
            Guid.Parse("22222222-2222-2222-2222-222222222223"),
            Guid.Parse("22222222-2222-2222-2222-222222222224"),
            Guid.Parse("22222222-2222-2222-2222-222222222225")
        };

        return
        [
            Offer(ids[0], providers[0], "Délutáni agyagozás", "Minden eszközt biztosítunk; kérjük, érkezzetek tíz perccel korábban.", OfferCategory.WORKSHOP, starts[0], 90, 5, 10, 4800m, 3200m, 10, 2, true, "Babakocsival megközelíthető.", now),
            Offer(ids[1], providers[1], "Játékos családi torna", "Mozgásos pálya és közös játék 3–6 éveseknek.", OfferCategory.MOVEMENT, starts[1], 60, 3, 6, 4200m, 2900m, 12, 5, true, null, now),
            Offer(ids[2], providers[2], "Első uszodai kaland", "Kiscsoportos vízhez szoktató alkalom szülői kísérettel.", OfferCategory.SWIMMING, starts[2], 45, 2, 5, 5500m, 3900m, 8, 3, true, null, now),
            Offer(ids[3], providers[3], "Titkos jelek a múzeumban", "Játékos nyomozás a kiállítótérben.", OfferCategory.MUSEUM, starts[3], 75, 7, 12, 3600m, 2400m, 15, 4, true, "Akadálymentes bejárat.", now),
            Offer(ids[4], providers[0], "Papírszínház készítés", "Saját mesekép és közös történetalkotás.", OfferCategory.WORKSHOP, starts[4], 80, 4, 9, 4000m, 2800m, 10, 1, true, null, now),
            Offer(ids[5], providers[1], "Mini akadálypálya", "Biztonságos beltéri pálya sok mozgással.", OfferCategory.SPORT, starts[5], 60, 4, 8, 4500m, 3000m, 10, 0, true, null, now),
            Offer(ids[6], providers[2], "Babaúszás", "Szülő-gyermek foglalkozás meleg vizű medencében.", OfferCategory.PARENT_CHILD, starts[6], 45, 0, 3, 6000m, 4200m, 8, 0, true, null, now)
        ];
    }

    private static Offer CreateHistoricalOffer(DateTimeOffset now) =>
        Offer(
            HistoricalOfferId,
            Guid.Parse("22222222-2222-2222-2222-222222222225"),
            "Lezárt helytörténeti séta",
            "Korábbi, részleteiben továbbra is megtekinthető ajánlat.",
            OfferCategory.MUSEUM,
            now.AddHours(-3),
            60,
            6,
            12,
            3000m,
            1900m,
            10,
            0,
            true,
            null,
            now);

    private static Guid BatchScopedOfferId(DateTime localBatchStart, int index)
    {
        var bytes = SHA256.HashData(Encoding.UTF8.GetBytes(
            $"ugorjbe-demo-offer:{localBatchStart:yyyy-MM-ddTHH:mm}:{index}"));
        bytes[6] = (byte)((bytes[6] & 0x0F) | 0x50);
        bytes[8] = (byte)((bytes[8] & 0x3F) | 0x80);
        return new Guid(bytes.AsSpan(0, 16));
    }

    private static Provider Provider(Guid id, string name, string shortDescription, string description, string postalCode, string city, string street, decimal latitude, decimal longitude, string? accessibility, DateTimeOffset now) => new()
    {
        Id = id,
        Name = name,
        ShortDescription = shortDescription,
        Description = description,
        PostalCode = postalCode,
        City = city,
        Street = street,
        CountryCode = "HU",
        Latitude = latitude,
        Longitude = longitude,
        Phone = "+3615550100",
        Email = $"hello@{id.ToString()[..8]}.example",
        WebsiteUrl = "https://example.invalid/ugorjbe-demo",
        AccessibilityInfo = accessibility,
        CreatedAtUtc = now,
        UpdatedAtUtc = now
    };

    private static Offer Offer(Guid id, Guid providerId, string title, string description, OfferCategory category, DateTimeOffset startsAt, int durationMinutes, int minAge, int maxAge, decimal originalPrice, decimal discountedPrice, int capacity, int reserved, bool accompaniment, string? accessibility, DateTimeOffset now) => new()
    {
        Id = id,
        ProviderId = providerId,
        Title = title,
        Description = description,
        Category = category,
        PostalCode = ProviderAddress(providerId).PostalCode,
        City = ProviderAddress(providerId).City,
        Street = ProviderAddress(providerId).Street,
        CountryCode = "HU",
        Latitude = ProviderAddress(providerId).Latitude,
        Longitude = ProviderAddress(providerId).Longitude,
        StartsAtUtc = startsAt,
        EndsAtUtc = startsAt.AddMinutes(durationMinutes),
        BookingCutoffUtc = startsAt.AddMinutes(-30),
        CancelUntilUtc = startsAt.AddMinutes(-15),
        MinChildAge = minAge,
        MaxChildAge = maxAge,
        AccompanimentRequired = accompaniment,
        AccessibilityInfo = accessibility,
        OriginalUnitPrice = originalPrice,
        DiscountedUnitPrice = discountedPrice,
        Currency = "HUF",
        TotalCapacity = capacity,
        ReservedQuantity = reserved,
        Status = OfferStatus.PUBLISHED,
        PublishedAtUtc = now,
        CreatedAtUtc = now,
        UpdatedAtUtc = now
    };

    private static (string PostalCode, string City, string Street, decimal Latitude, decimal Longitude) ProviderAddress(Guid providerId) =>
        providerId == PrimaryProviderId
            ? ("1137", "Budapest", "Pozsonyi út 12.", 47.518200m, 19.050400m)
            : providerId == Guid.Parse("22222222-2222-2222-2222-222222222223")
                ? ("1117", "Budapest", "Karinthy Frigyes út 18.", 47.475900m, 19.052000m)
                : providerId == Guid.Parse("22222222-2222-2222-2222-222222222224")
                    ? ("1146", "Budapest", "Istvánmezei út 6.", 47.507400m, 19.093600m)
                    : ("1088", "Budapest", "Múzeum körút 14.", 47.490700m, 19.061400m);

    private static DateTimeOffset Utc(DateTime local) =>
        new(TimeZoneInfo.ConvertTimeToUtc(DateTime.SpecifyKind(local, DateTimeKind.Unspecified), BudapestTimeZone), TimeSpan.Zero);
}
