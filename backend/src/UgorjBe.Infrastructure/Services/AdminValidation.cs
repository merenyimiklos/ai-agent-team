using System.Net.Mail;
using UgorjBe.Application;
using UgorjBe.Domain;

namespace UgorjBe.Infrastructure.Services;

internal static class AdminValidation
{
    public static void Provider(ProviderCreateRequest request, bool requireVersion = false, string? version = null)
    {
        var errors = new Dictionary<string, string[]>();
        Length(errors, "name", request.Name, 2, 120);
        Length(errors, "shortDescription", request.ShortDescription, 2, 240);
        Length(errors, "description", request.Description, 10, 2000);
        Address(errors, request.Address);
        OptionalLength(errors, "phone", request.Phone, 40);
        OptionalLength(errors, "accessibilityInfo", request.AccessibilityInfo, 500);
        if (!string.IsNullOrWhiteSpace(request.Email) &&
            (request.Email.Trim().Length > 254 || !IsEmail(request.Email.Trim())))
        {
            Add(errors, "email", "Érvényes, legfeljebb 254 karakteres e-mail-cím szükséges.");
        }

        Url(errors, "websiteUrl", request.WebsiteUrl, 500);
        Url(errors, "imageUrl", request.ImageUrl, 1000);
        if (requireVersion) Version(errors, version);
        Throw(errors);
    }

    public static void Offer(OfferWriteRequest request, bool requireVersion = false, string? version = null)
    {
        var errors = new Dictionary<string, string[]>();
        if (request.ProviderId == Guid.Empty) Add(errors, "providerId", "Érvényes szolgáltató-azonosító szükséges.");
        Length(errors, "title", request.Title, 2, 160);
        Length(errors, "description", request.Description, 10, 3000);
        if (!Enum.IsDefined(request.Category)) Add(errors, "category", "A kategória érvénytelen.");
        Address(errors, request.Address);
        if (request.StartsAtUtc == default) Add(errors, "startsAtUtc", "A kezdés kötelező.");
        if (request.EndsAtUtc <= request.StartsAtUtc) Add(errors, "endsAtUtc", "A befejezésnek a kezdés után kell lennie.");
        if (request.BookingCutoffUtc == default || request.BookingCutoffUtc > request.StartsAtUtc) Add(errors, "bookingCutoffUtc", "A foglalási határidő nem lehet a kezdés után.");
        if (request.CancelUntilUtc == default || request.CancelUntilUtc > request.StartsAtUtc) Add(errors, "cancelUntilUtc", "A lemondási határidő nem lehet a kezdés után.");
        if (request.MinChildAge is < 0 or > 18) Add(errors, "minChildAge", "A legkisebb életkor 0 és 18 között lehet.");
        if (request.MaxChildAge is < 0 or > 18 || request.MaxChildAge < request.MinChildAge) Add(errors, "maxChildAge", "A legnagyobb életkor érvénytelen.");
        Money(errors, "originalUnitPrice", request.OriginalUnitPrice);
        Money(errors, "discountedUnitPrice", request.DiscountedUnitPrice);
        if (request.OriginalUnitPrice is not null && request.DiscountedUnitPrice is not null)
        {
            if (!string.Equals(request.OriginalUnitPrice.Currency, request.DiscountedUnitPrice.Currency, StringComparison.Ordinal))
            {
                Add(errors, "discountedUnitPrice.currency", "A pénznemeknek azonosnak kell lenniük.");
            }

            if (request.DiscountedUnitPrice.Amount > request.OriginalUnitPrice.Amount)
            {
                Add(errors, "discountedUnitPrice.amount", "A kedvezményes ár nem lehet magasabb az eredeti árnál.");
            }
        }

        if (request.TotalCapacity is < 1 or > 10000) Add(errors, "totalCapacity", "A kapacitás 1 és 10000 között lehet.");
        OptionalLength(errors, "accessibilityInfo", request.AccessibilityInfo, 500);
        Url(errors, "imageUrl", request.ImageUrl, 1000);
        if (requireVersion) Version(errors, version);
        Throw(errors);
    }

    public static void ProviderQuery(AdminProviderQuery query)
    {
        var errors = new Dictionary<string, string[]>();
        if (query.Q?.Trim().Length > 100) Add(errors, "q", "A keresés legfeljebb 100 karakter lehet.");
        Page(errors, query.Page, query.PageSize);
        Throw(errors);
    }

    public static void OfferQuery(AdminOfferQuery query)
    {
        var errors = new Dictionary<string, string[]>();
        if (query.StartsFromUtc.HasValue && query.StartsFromUtc.Value.Offset != TimeSpan.Zero) Add(errors, "startsFromUtc", "UTC időpont szükséges.");
        if (query.StartsToUtc.HasValue && query.StartsToUtc.Value.Offset != TimeSpan.Zero) Add(errors, "startsToUtc", "UTC időpont szükséges.");
        if (query.Q?.Trim().Length > 100) Add(errors, "q", "A keresés legfeljebb 100 karakter lehet.");
        if (query.StartsFromUtc is not null && query.StartsToUtc <= query.StartsFromUtc) Add(errors, "startsToUtc", "A felső időhatárnak későbbinek kell lennie.");
        Page(errors, query.Page, query.PageSize);
        Throw(errors);
    }

    public static uint ParseVersion(string? version)
    {
        var errors = new Dictionary<string, string[]>();
        Version(errors, version);
        Throw(errors);
        return uint.Parse(version!, System.Globalization.CultureInfo.InvariantCulture);
    }

    private static void Address(Dictionary<string, string[]> errors, AddressDto? address)
    {
        if (address is null)
        {
            Add(errors, "address", "A cím megadása kötelező.");
            return;
        }

        Length(errors, "address.postalCode", address.PostalCode, 1, 16);
        Length(errors, "address.city", address.City, 2, 100);
        Length(errors, "address.street", address.Street, 2, 200);
        if (address.CountryCode.Length != 2 || address.CountryCode.Any(x => x is < 'A' or > 'Z'))
        {
            Add(errors, "address.countryCode", "Két nagybetűs országkód szükséges.");
        }

        if (address.Latitude is < -90 or > 90) Add(errors, "address.latitude", "A szélesség -90 és 90 között lehet.");
        if (address.Longitude is < -180 or > 180) Add(errors, "address.longitude", "A hosszúság -180 és 180 között lehet.");
    }

    private static void Money(Dictionary<string, string[]> errors, string field, MoneyDto? money)
    {
        if (money is null)
        {
            Add(errors, field, "Az ár megadása kötelező.");
            return;
        }

        if (money.Amount is < 0 or > 9999999999.99m || decimal.Round(money.Amount, 2) != money.Amount)
        {
            Add(errors, $"{field}.amount", "Az ár 0 és 9999999999,99 között, legfeljebb két tizedes lehet.");
        }

        if (money.Currency.Length != 3 || money.Currency.Any(x => x is < 'A' or > 'Z'))
        {
            Add(errors, $"{field}.currency", "Három nagybetűs ISO pénznemkód szükséges.");
        }
    }

    private static void Length(Dictionary<string, string[]> errors, string field, string? value, int min, int max)
    {
        var length = value?.Trim().Length ?? 0;
        if (length < min || length > max) Add(errors, field, $"A mező hossza {min} és {max} karakter között lehet.");
    }

    private static void OptionalLength(Dictionary<string, string[]> errors, string field, string? value, int max)
    {
        if (value?.Trim().Length > max) Add(errors, field, $"A mező legfeljebb {max} karakter lehet.");
    }

    private static void Url(Dictionary<string, string[]> errors, string field, string? value, int max)
    {
        if (string.IsNullOrWhiteSpace(value)) return;
        if (value.Trim().Length > max || !Uri.TryCreate(value.Trim(), UriKind.Absolute, out var uri) ||
            (uri.Scheme != Uri.UriSchemeHttp && uri.Scheme != Uri.UriSchemeHttps))
        {
            Add(errors, field, "Abszolút HTTP vagy HTTPS URL szükséges.");
        }
    }

    private static bool IsEmail(string value)
    {
        try { return new MailAddress(value).Address.Equals(value, StringComparison.OrdinalIgnoreCase); }
        catch (FormatException) { return false; }
    }

    private static void Version(Dictionary<string, string[]> errors, string? version)
    {
        if (!uint.TryParse(version, System.Globalization.NumberStyles.None, System.Globalization.CultureInfo.InvariantCulture, out _))
        {
            Add(errors, "version", "Érvényes verzió szükséges.");
        }
    }

    private static void Page(Dictionary<string, string[]> errors, int page, int pageSize)
    {
        if (page < 1) Add(errors, "page", "Az oldalszám legalább 1.");
        if (pageSize is < 1 or > 50) Add(errors, "pageSize", "Az oldalméret 1 és 50 között lehet.");
    }

    private static void Add(Dictionary<string, string[]> errors, string field, string message) => errors[field] = [message];
    private static void Throw(Dictionary<string, string[]> errors)
    {
        if (errors.Count > 0) throw AppErrors.Validation(errors);
    }
}
