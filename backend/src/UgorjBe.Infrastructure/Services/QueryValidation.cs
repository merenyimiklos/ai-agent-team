using UgorjBe.Application;

namespace UgorjBe.Infrastructure.Services;

internal static class QueryValidation
{
    public static void Validate(OfferQuery query, DateTimeOffset startsFrom, DateTimeOffset startsTo)
    {
        var errors = new Dictionary<string, string[]>();
        ValidateUtc(query.StartsFromUtc, "startsFromUtc", errors);
        ValidateUtc(query.StartsToUtc, "startsToUtc", errors);
        if (query.Q?.Trim().Length > 100) Add(errors, "q", "A keresés legfeljebb 100 karakter lehet.");
        if (query.ChildAge is < 0 or > 18) Add(errors, "childAge", "A gyermek életkora 0 és 18 között lehet.");
        if (startsTo <= startsFrom) Add(errors, "startsToUtc", "A felső időhatárnak későbbinek kell lennie.");
        if (query.MinPrice < 0) Add(errors, "minPrice", "Az ár nem lehet negatív.");
        if (query.MaxPrice < 0 || query.MaxPrice < query.MinPrice) Add(errors, "maxPrice", "A maximális ár érvénytelen.");
        if (query.MinAvailablePlaces is < 1 or > 10) Add(errors, "minAvailablePlaces", "Az elérhető helyek száma 1 és 10 között lehet.");
        ValidateCoordinates(query.Latitude, query.Longitude, errors);
        if (query.MaxDistanceKm is <= 0 or > 100) Add(errors, "maxDistanceKm", "A távolság 0 és 100 km között lehet.");
        if (query.MaxDistanceKm is not null && (query.Latitude is null || query.Longitude is null)) Add(errors, "maxDistanceKm", "A távolságszűréshez koordináták szükségesek.");
        if (query.Sort == Domain.OfferSort.DISTANCE && (query.Latitude is null || query.Longitude is null)) Add(errors, "sort", "Távolság szerinti rendezéshez koordináták szükségesek.");
        ValidateBounds(query.South, query.West, query.North, query.East, false, errors);
        ValidatePage(query.Page, query.PageSize, errors);
        ThrowIfAny(errors);
    }

    public static void Validate(MapOfferQuery query, DateTimeOffset startsFrom, DateTimeOffset startsTo)
    {
        var errors = new Dictionary<string, string[]>();
        ValidateUtc(query.StartsFromUtc, "startsFromUtc", errors);
        ValidateUtc(query.StartsToUtc, "startsToUtc", errors);
        if (query.Q?.Trim().Length > 100) Add(errors, "q", "A keresés legfeljebb 100 karakter lehet.");
        if (query.ChildAge is < 0 or > 18) Add(errors, "childAge", "A gyermek életkora 0 és 18 között lehet.");
        if (startsTo <= startsFrom) Add(errors, "startsToUtc", "A felső időhatárnak későbbinek kell lennie.");
        if (query.MinPrice < 0) Add(errors, "minPrice", "Az ár nem lehet negatív.");
        if (query.MaxPrice < 0 || query.MaxPrice < query.MinPrice) Add(errors, "maxPrice", "A maximális ár érvénytelen.");
        if (query.MinAvailablePlaces is < 1 or > 10) Add(errors, "minAvailablePlaces", "Az elérhető helyek száma 1 és 10 között lehet.");
        ValidateCoordinates(query.Latitude, query.Longitude, errors);
        if (query.MaxDistanceKm is <= 0 or > 100) Add(errors, "maxDistanceKm", "A távolság 0 és 100 km között lehet.");
        if (query.MaxDistanceKm is not null && (query.Latitude is null || query.Longitude is null)) Add(errors, "maxDistanceKm", "A távolságszűréshez koordináták szükségesek.");
        if (query.Sort == Domain.OfferSort.DISTANCE && (query.Latitude is null || query.Longitude is null)) Add(errors, "sort", "Távolság szerinti rendezéshez koordináták szükségesek.");
        ValidateBounds(query.South, query.West, query.North, query.East, true, errors);
        if (query.Limit is < 1 or > 200) Add(errors, "limit", "A térképes korlát 1 és 200 között lehet.");
        ThrowIfAny(errors);
    }

    public static void Validate(CoordinateQuery query)
    {
        var errors = new Dictionary<string, string[]>();
        ValidateCoordinates(query.Latitude, query.Longitude, errors);
        ThrowIfAny(errors);
    }

    public static void Validate(PageQuery query)
    {
        var errors = new Dictionary<string, string[]>();
        ValidatePage(query.Page, query.PageSize, errors);
        ThrowIfAny(errors);
    }

    public static void Validate(BookingQuery query)
    {
        var errors = new Dictionary<string, string[]>();
        ValidatePage(query.Page, query.PageSize, errors);
        ThrowIfAny(errors);
    }

    private static void ValidateCoordinates(decimal? latitude, decimal? longitude, Dictionary<string, string[]> errors)
    {
        if (latitude.HasValue != longitude.HasValue)
        {
            Add(errors, "latitude", "A szélességi és hosszúsági koordinátát együtt kell megadni.");
        }

        if (latitude is < -90 or > 90) Add(errors, "latitude", "A szélesség -90 és 90 között lehet.");
        if (longitude is < -180 or > 180) Add(errors, "longitude", "A hosszúság -180 és 180 között lehet.");
    }

    private static void ValidatePage(int page, int pageSize, Dictionary<string, string[]> errors)
    {
        if (page < 1) Add(errors, "page", "Az oldalszám legalább 1.");
        if (pageSize is < 1 or > 50) Add(errors, "pageSize", "Az oldalméret 1 és 50 között lehet.");
    }

    private static void ValidateUtc(DateTimeOffset? value, string field, Dictionary<string, string[]> errors)
    {
        if (value?.Offset != TimeSpan.Zero) Add(errors, field, "UTC időpont szükséges.");
    }

    private static void ValidateBounds(
        decimal? south,
        decimal? west,
        decimal? north,
        decimal? east,
        bool required,
        Dictionary<string, string[]> errors)
    {
        var supplied = new[] { south.HasValue, west.HasValue, north.HasValue, east.HasValue }.Count(x => x);
        if ((required && supplied != 4) || (!required && supplied is > 0 and < 4))
        {
            foreach (var field in new[] { "south", "west", "north", "east" })
            {
                Add(errors, field, "A befoglaló téglalap mind a négy határa szükséges.");
            }

            return;
        }

        if (supplied == 0) return;
        if (south is < -90 or > 90) Add(errors, "south", "A déli szélesség -90 és 90 között lehet.");
        if (north is < -90 or > 90) Add(errors, "north", "Az északi szélesség -90 és 90 között lehet.");
        if (west is < -180 or > 180) Add(errors, "west", "A nyugati hosszúság -180 és 180 között lehet.");
        if (east is < -180 or > 180) Add(errors, "east", "A keleti hosszúság -180 és 180 között lehet.");
        if (north <= south) Add(errors, "north", "Az északi határnak a déli fölött kell lennie.");
        if (east <= west) Add(errors, "east", "A keleti határnak a nyugatitól keletre kell lennie.");
        if (north - south > 2) Add(errors, "north", "A szélességi tartomány legfeljebb 2 fok lehet.");
        if (east - west > 3) Add(errors, "east", "A hosszúsági tartomány legfeljebb 3 fok lehet.");
    }

    private static void Add(Dictionary<string, string[]> errors, string field, string message) => errors[field] = [message];

    private static void ThrowIfAny(Dictionary<string, string[]> errors)
    {
        if (errors.Count > 0) throw AppErrors.Validation(errors);
    }
}
