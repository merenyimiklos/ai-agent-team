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
    public static readonly Guid PrimaryProviderId = Guid.Parse("22222222-2222-2222-2222-222222222222");
    public static readonly Guid PrimaryOfferId = Guid.Parse("33333333-3333-3333-3333-333333333333");

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

        if (!await dbContext.Providers.AnyAsync(cancellationToken))
        {
            dbContext.Providers.AddRange(CreateProviders(now));
            await dbContext.SaveChangesAsync(cancellationToken);
        }

        if (!await dbContext.Offers.AnyAsync(cancellationToken))
        {
            dbContext.Offers.AddRange(CreateOffers(now));
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

    private static IReadOnlyList<Offer> CreateOffers(DateTimeOffset now)
    {
        var localNow = TimeZoneInfo.ConvertTime(now, BudapestTimeZone);
        var candidate = localNow.AddMinutes(75);
        var roundedMinutes = candidate.Minute < 30 ? 30 : 0;
        var roundedHour = candidate.Minute < 30 ? candidate.Hour : candidate.Hour + 1;
        var localBase = localNow.Date.AddHours(roundedHour).AddMinutes(roundedMinutes);
        if (localBase.Date != localNow.Date || localBase.Hour > 20)
        {
            localBase = localNow.Date.AddDays(1).AddHours(9);
        }

        var starts = Enumerable.Range(0, 6)
            .Select(index => Utc(localBase.AddMinutes(index * 90)))
            .ToArray();
        var tomorrowMorning = Utc(localNow.Date.AddDays(1).AddHours(10));
        var expired = now.AddHours(-3);
        var providers = new[]
        {
            PrimaryProviderId,
            Guid.Parse("22222222-2222-2222-2222-222222222223"),
            Guid.Parse("22222222-2222-2222-2222-222222222224"),
            Guid.Parse("22222222-2222-2222-2222-222222222225")
        };

        return
        [
            Offer(PrimaryOfferId, providers[0], "Délutáni agyagozás", "Minden eszközt biztosítunk; kérjük, érkezzetek tíz perccel korábban.", OfferCategory.WORKSHOP, starts[0], 90, 5, 10, 4800m, 3200m, 10, 2, true, "Babakocsival megközelíthető.", now),
            Offer(Guid.Parse("33333333-3333-3333-3333-333333333334"), providers[1], "Játékos családi torna", "Mozgásos pálya és közös játék 3–6 éveseknek.", OfferCategory.MOVEMENT, starts[1], 60, 3, 6, 4200m, 2900m, 12, 5, true, null, now),
            Offer(Guid.Parse("33333333-3333-3333-3333-333333333335"), providers[2], "Első uszodai kaland", "Kiscsoportos vízhez szoktató alkalom szülői kísérettel.", OfferCategory.SWIMMING, starts[2], 45, 2, 5, 5500m, 3900m, 8, 3, true, null, now),
            Offer(Guid.Parse("33333333-3333-3333-3333-333333333336"), providers[3], "Titkos jelek a múzeumban", "Játékos nyomozás a kiállítótérben.", OfferCategory.MUSEUM, starts[3], 75, 7, 12, 3600m, 2400m, 15, 4, true, "Akadálymentes bejárat.", now),
            Offer(Guid.Parse("33333333-3333-3333-3333-333333333337"), providers[0], "Papírszínház készítés", "Saját mesekép és közös történetalkotás.", OfferCategory.WORKSHOP, starts[4], 80, 4, 9, 4000m, 2800m, 10, 1, true, null, now),
            Offer(Guid.Parse("33333333-3333-3333-3333-333333333338"), providers[1], "Mini akadálypálya", "Biztonságos beltéri pálya sok mozgással.", OfferCategory.SPORT, starts[5], 60, 4, 8, 4500m, 3000m, 10, 0, true, null, now),
            Offer(Guid.Parse("33333333-3333-3333-3333-333333333339"), providers[2], "Holnapi babaúszás", "Szülő-gyermek foglalkozás meleg vizű medencében.", OfferCategory.PARENT_CHILD, tomorrowMorning, 45, 0, 3, 6000m, 4200m, 8, 8, true, null, now),
            Offer(Guid.Parse("33333333-3333-3333-3333-333333333340"), providers[3], "Lezárt helytörténeti séta", "Korábbi, részleteiben továbbra is megtekinthető ajánlat.", OfferCategory.MUSEUM, expired, 60, 6, 12, 3000m, 1900m, 10, 0, true, null, now)
        ];
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
        CreatedAtUtc = now,
        UpdatedAtUtc = now
    };

    private static DateTimeOffset Utc(DateTime local) =>
        new(TimeZoneInfo.ConvertTimeToUtc(DateTime.SpecifyKind(local, DateTimeKind.Unspecified), BudapestTimeZone), TimeSpan.Zero);
}
