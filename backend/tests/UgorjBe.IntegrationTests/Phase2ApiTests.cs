using System.Net;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text.Json;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using UgorjBe.Domain;
using UgorjBe.Infrastructure.Persistence;
using Xunit;

namespace UgorjBe.IntegrationTests;

public sealed class Phase2ApiTests
{
    [PostgresFact]
    public async Task Optional_time_filters_can_be_omitted_from_public_and_admin_catalogs()
    {
        using var factory = new ApiFactory();
        await factory.ResetAsync();

        using var publicClient = factory.CreateClient();
        Assert.Equal(HttpStatusCode.OK, (await publicClient.GetAsync("/api/offers")).StatusCode);
        Assert.Equal(
            HttpStatusCode.OK,
            (await publicClient.GetAsync("/api/offers/map?south=47.4&west=18.9&north=47.6&east=19.2")).StatusCode);

        using var admin = factory.CreateClient();
        await Authenticate(admin, "admin@ugorjbe.local", "UgorjBeAdmin123!");
        Assert.Equal(HttpStatusCode.OK, (await admin.GetAsync("/api/admin/offers?page=1&pageSize=20")).StatusCode);
    }

    [PostgresFact]
    public async Task Map_requires_bounded_box_filters_geographically_and_reports_truncation()
    {
        using var factory = new ApiFactory();
        await factory.ResetAsync();
        using var client = factory.CreateClient();

        await AssertProblem(await client.GetAsync("/api/offers/map"), HttpStatusCode.BadRequest, "VALIDATION_FAILED");
        await AssertProblem(await client.GetAsync("/api/offers/map?south=45&west=18&north=48&east=19"), HttpStatusCode.BadRequest, "VALIDATION_FAILED");

        var from = Uri.EscapeDataString(factory.Clock.GetUtcNow().UtcDateTime.ToString("O"));
        var to = Uri.EscapeDataString(factory.Clock.GetUtcNow().AddDays(2).UtcDateTime.ToString("O"));
        var map = await client.GetFromJsonAsync<JsonElement>(
            $"/api/offers/map?south=47&west=18.9&north=48&east=19.3&limit=1&startsFromUtc={from}&startsToUtc={to}");
        Assert.Equal(1, map.GetProperty("limit").GetInt32());
        Assert.True(map.GetProperty("isTruncated").GetBoolean());
        var item = Assert.Single(map.GetProperty("items").EnumerateArray());
        var address = item.GetProperty("address");
        Assert.InRange(address.GetProperty("latitude").GetDecimal(), 47m, 48m);
        Assert.InRange(address.GetProperty("longitude").GetDecimal(), 18.9m, 19.3m);

        var empty = await client.GetFromJsonAsync<JsonElement>(
            $"/api/offers/map?south=46&west=18&north=46.5&east=18.5&startsFromUtc={from}&startsToUtc={to}");
        Assert.Empty(empty.GetProperty("items").EnumerateArray());
        var list = await client.GetFromJsonAsync<JsonElement>(
            $"/api/offers?south=46&west=18&north=46.5&east=18.5&startsFromUtc={from}&startsToUtc={to}");
        Assert.Equal(0, list.GetProperty("totalCount").GetInt32());
    }

    [PostgresFact]
    public async Task Admin_endpoints_return_exact_401_and_customer_403_problems()
    {
        using var factory = new ApiFactory();
        await factory.ResetAsync();
        using var anonymous = factory.CreateClient();
        foreach (var path in new[] { "/api/admin/dashboard", "/api/admin/providers", "/api/admin/offers" })
        {
            await AssertProblem(await anonymous.GetAsync(path), HttpStatusCode.Unauthorized, "AUTH_REQUIRED");
        }

        using var customer = factory.CreateClient();
        await Authenticate(customer, "demo@ugorjbe.local", "UgorjBe123!");
        foreach (var path in new[] { "/api/admin/dashboard", "/api/admin/providers", "/api/admin/offers" })
        {
            await AssertProblem(await customer.GetAsync(path), HttpStatusCode.Forbidden, "AUTH_FORBIDDEN");
        }
    }

    [PostgresFact]
    public async Task Seeded_admin_can_create_update_provider_and_stale_version_conflicts()
    {
        using var factory = new ApiFactory();
        await factory.ResetAsync();
        using var admin = factory.CreateClient();
        var login = await Authenticate(admin, "admin@ugorjbe.local", "UgorjBeAdmin123!");
        Assert.Equal("admin", login.GetProperty("user").GetProperty("role").GetString());

        var invalid = ProviderBody("x", "Rövid", "túl rövid");
        var invalidResponse = await admin.PostAsJsonAsync("/api/admin/providers", invalid);
        var invalidProblem = await AssertProblem(invalidResponse, HttpStatusCode.BadRequest, "VALIDATION_FAILED");
        Assert.True(invalidProblem.GetProperty("errors").TryGetProperty("name", out _));

        var create = await admin.PostAsJsonAsync("/api/admin/providers", ProviderBody("Csillagkert Stúdió", "Családi alkotótér.", "Barátságos, modern családi alkotótér Budapest szívében."));
        Assert.Equal(HttpStatusCode.Created, create.StatusCode);
        var created = await Read(create);
        var id = created.GetProperty("id").GetGuid();
        var version = created.GetProperty("version").GetString()!;

        var firstUpdate = ProviderBody("Csillagkert Stúdió 2", "Családi alkotótér.", "Barátságos, modern családi alkotótér Budapest szívében.", version);
        var updatedResponse = await admin.PutAsJsonAsync($"/api/admin/providers/{id}", firstUpdate);
        Assert.Equal(HttpStatusCode.OK, updatedResponse.StatusCode);
        var updated = await Read(updatedResponse);
        Assert.NotEqual(version, updated.GetProperty("version").GetString());

        var stale = await admin.PutAsJsonAsync($"/api/admin/providers/{id}", firstUpdate);
        await AssertProblem(stale, HttpStatusCode.Conflict, "CONCURRENCY_CONFLICT");
    }

    [PostgresFact]
    public async Task Admin_created_offer_lifecycle_controls_public_discovery_and_booking()
    {
        using var factory = new ApiFactory();
        await factory.ResetAsync();
        using var admin = factory.CreateClient();
        await Authenticate(admin, "admin@ugorjbe.local", "UgorjBeAdmin123!");
        var body = OfferBody(DatabaseSeeder.PrimaryProviderId, factory.Clock.GetUtcNow(), "Fényfestő családi műhely");

        var create = await admin.PostAsJsonAsync("/api/admin/offers", body);
        Assert.Equal(HttpStatusCode.Created, create.StatusCode);
        var draft = await Read(create);
        Assert.Equal("DRAFT", draft.GetProperty("status").GetString());
        var id = draft.GetProperty("id").GetGuid();
        var version = draft.GetProperty("version").GetString()!;

        var before = await PublicOffers(client: admin, factory, id);
        Assert.False(before);
        var publish = await admin.PostAsJsonAsync($"/api/admin/offers/{id}/publish", new { version });
        Assert.Equal(HttpStatusCode.OK, publish.StatusCode);
        var published = await Read(publish);
        version = published.GetProperty("version").GetString()!;
        Assert.True(await PublicOffers(admin, factory, id));

        var unpublish = await admin.PostAsJsonAsync($"/api/admin/offers/{id}/unpublish", new { version });
        Assert.Equal(HttpStatusCode.OK, unpublish.StatusCode);
        var unpublished = await Read(unpublish);
        version = unpublished.GetProperty("version").GetString()!;
        Assert.False(await PublicOffers(admin, factory, id));

        using var customer = factory.CreateClient();
        await Authenticate(customer, "demo@ugorjbe.local", "UgorjBe123!");
        await AssertProblem(await customer.PostAsJsonAsync("/api/bookings", new { offerId = id, quantity = 1 }), HttpStatusCode.Conflict, "OFFER_NOT_BOOKABLE");

        var archive = await admin.PostAsJsonAsync($"/api/admin/offers/{id}/archive", new { version });
        Assert.Equal(HttpStatusCode.OK, archive.StatusCode);
        var archived = await Read(archive);
        await AssertProblem(
            await admin.PostAsJsonAsync($"/api/admin/offers/{id}/publish", new { version = archived.GetProperty("version").GetString() }),
            HttpStatusCode.Conflict,
            "OFFER_STATE_TRANSITION_INVALID");
    }

    [PostgresFact]
    public async Task Confirmed_booking_protects_capacity_location_and_schedule_during_admin_update()
    {
        using var factory = new ApiFactory();
        await factory.ResetAsync();
        using var admin = factory.CreateClient();
        await Authenticate(admin, "admin@ugorjbe.local", "UgorjBeAdmin123!");
        var body = OfferBody(DatabaseSeeder.PrimaryProviderId, factory.Clock.GetUtcNow(), "Foglalásvédett műhely");
        var create = await admin.PostAsJsonAsync("/api/admin/offers", body);
        var draft = await Read(create);
        var id = draft.GetProperty("id").GetGuid();
        var publish = await admin.PostAsJsonAsync($"/api/admin/offers/{id}/publish", new { version = draft.GetProperty("version").GetString() });
        var published = await Read(publish);

        using var customer = factory.CreateClient();
        await Authenticate(customer, "demo@ugorjbe.local", "UgorjBe123!");
        Assert.Equal(HttpStatusCode.Created, (await customer.PostAsJsonAsync("/api/bookings", new { offerId = id, quantity = 2 })).StatusCode);
        var afterBooking = await admin.GetFromJsonAsync<JsonElement>($"/api/admin/offers/{id}");
        var currentVersion = afterBooking.GetProperty("version").GetString();

        var capacityConflictBody = OfferBody(DatabaseSeeder.PrimaryProviderId, factory.Clock.GetUtcNow(), "Foglalásvédett műhely", currentVersion, totalCapacity: 1);
        var capacityProblem = await AssertProblem(await admin.PutAsJsonAsync($"/api/admin/offers/{id}", capacityConflictBody), HttpStatusCode.Conflict, "OFFER_UPDATE_CONFLICT");
        Assert.Contains("totalCapacity", capacityProblem.GetProperty("conflictingFields").EnumerateArray().Select(x => x.GetString()));

        var protectedBody = OfferBody(DatabaseSeeder.PrimaryProviderId, factory.Clock.GetUtcNow().AddHours(1), "Foglalásvédett műhely", currentVersion);
        var protectedProblem = await AssertProblem(await admin.PutAsJsonAsync($"/api/admin/offers/{id}", protectedBody), HttpStatusCode.Conflict, "OFFER_UPDATE_CONFLICT");
        Assert.Contains("startsAtUtc", protectedProblem.GetProperty("conflictingFields").EnumerateArray().Select(x => x.GetString()));

        await using var scope = factory.Services.CreateAsyncScope();
        var stored = await scope.ServiceProvider.GetRequiredService<UgorjBeDbContext>().Offers.AsNoTracking().SingleAsync(x => x.Id == id);
        Assert.Equal(2, stored.ReservedQuantity);
        Assert.Equal(12, stored.TotalCapacity);
    }

    private static Dictionary<string, object?> ProviderBody(string name, string shortDescription, string description, string? version = null)
    {
        var body = new Dictionary<string, object?>
        {
            ["name"] = name,
            ["shortDescription"] = shortDescription,
            ["description"] = description,
            ["address"] = new { postalCode = "1137", city = "Budapest", street = "Pozsonyi út 24.", countryCode = "HU", latitude = 47.518m, longitude = 19.051m },
            ["phone"] = "+3615550200",
            ["email"] = "hello@csillagkert.example",
            ["websiteUrl"] = "https://example.invalid/csillagkert",
            ["accessibilityInfo"] = "Babakocsival megközelíthető.",
            ["imageUrl"] = null
        };
        if (version is not null) body["version"] = version;
        return body;
    }

    private static Dictionary<string, object?> OfferBody(Guid providerId, DateTimeOffset now, string title, string? version = null, int totalCapacity = 12)
    {
        var starts = now.AddHours(4);
        var body = new Dictionary<string, object?>
        {
            ["providerId"] = providerId,
            ["title"] = title,
            ["description"] = "Közös, vezetett alkotóprogram családoknak minden szükséges eszközzel.",
            ["category"] = "WORKSHOP",
            ["address"] = new { postalCode = "1137", city = "Budapest", street = "Pozsonyi út 12.", countryCode = "HU", latitude = 47.5182m, longitude = 19.0504m },
            ["startsAtUtc"] = Utc(starts),
            ["endsAtUtc"] = Utc(starts.AddMinutes(90)),
            ["bookingCutoffUtc"] = Utc(starts.AddMinutes(-30)),
            ["cancelUntilUtc"] = Utc(starts.AddHours(-1)),
            ["minChildAge"] = 4,
            ["maxChildAge"] = 12,
            ["accompanimentRequired"] = true,
            ["accessibilityInfo"] = "Babakocsival megközelíthető.",
            ["originalUnitPrice"] = new { amount = 5000m, currency = "HUF" },
            ["discountedUnitPrice"] = new { amount = 3200m, currency = "HUF" },
            ["totalCapacity"] = totalCapacity,
            ["imageUrl"] = null
        };
        if (version is not null) body["version"] = version;
        return body;
    }

    private static string Utc(DateTimeOffset value) => value.UtcDateTime.ToString("yyyy-MM-dd'T'HH:mm:ss.fff'Z'", System.Globalization.CultureInfo.InvariantCulture);

    private static async Task<bool> PublicOffers(HttpClient client, ApiFactory factory, Guid id)
    {
        var from = Uri.EscapeDataString(factory.Clock.GetUtcNow().UtcDateTime.ToString("O"));
        var to = Uri.EscapeDataString(factory.Clock.GetUtcNow().AddDays(2).UtcDateTime.ToString("O"));
        var response = await client.GetFromJsonAsync<JsonElement>($"/api/offers/map?south=47&west=18.9&north=48&east=19.3&startsFromUtc={from}&startsToUtc={to}");
        return response.GetProperty("items").EnumerateArray().Any(x => x.GetProperty("id").GetGuid() == id);
    }

    private static async Task<JsonElement> Authenticate(HttpClient client, string email, string password)
    {
        var response = await client.PostAsJsonAsync("/api/auth/login", new { email, password });
        response.EnsureSuccessStatusCode();
        var document = await JsonDocument.ParseAsync(await response.Content.ReadAsStreamAsync());
        var root = document.RootElement.Clone();
        client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", root.GetProperty("accessToken").GetString());
        return root;
    }

    private static async Task<JsonElement> Read(HttpResponseMessage response)
    {
        var document = await JsonDocument.ParseAsync(await response.Content.ReadAsStreamAsync());
        return document.RootElement.Clone();
    }

    private static async Task<JsonElement> AssertProblem(HttpResponseMessage response, HttpStatusCode status, string code)
    {
        Assert.Equal(status, response.StatusCode);
        Assert.Equal("application/problem+json", response.Content.Headers.ContentType?.MediaType);
        var problem = await Read(response);
        Assert.Equal(code, problem.GetProperty("code").GetString());
        return problem;
    }
}
