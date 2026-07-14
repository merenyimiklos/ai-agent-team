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

public sealed class ApiContractTests
{
    [PostgresFact]
    public async Task Registration_returns_token_usable_for_me()
    {
        using var factory = new ApiFactory();
        await factory.ResetAsync();
        using var client = factory.CreateClient();

        var response = await client.PostAsJsonAsync("/api/auth/register", new
        {
            email = "uj.szulő@example.hu",
            password = "Biztonsagos1",
            displayName = "Teszt Szülő"
        });

        Assert.Equal(HttpStatusCode.Created, response.StatusCode);
        var token = (await JsonDocument.ParseAsync(await response.Content.ReadAsStreamAsync())).RootElement.GetProperty("accessToken").GetString();
        client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);
        var me = await client.GetAsync("/api/auth/me");
        Assert.Equal(HttpStatusCode.OK, me.StatusCode);
    }

    [PostgresFact]
    public async Task Expired_offer_returns_stable_not_bookable_problem()
    {
        using var factory = new ApiFactory();
        await factory.ResetAsync();
        using var client = factory.CreateClient();
        await AuthenticateDemoAsync(client);

        var response = await client.PostAsJsonAsync("/api/bookings", new { offerId = DatabaseSeeder.HistoricalOfferId, quantity = 1 });

        Assert.Equal(HttpStatusCode.Conflict, response.StatusCode);
        var problem = await JsonDocument.ParseAsync(await response.Content.ReadAsStreamAsync());
        Assert.Equal("OFFER_NOT_BOOKABLE", problem.RootElement.GetProperty("code").GetString());
    }

    [PostgresFact]
    public async Task Concurrent_reservations_for_last_place_cannot_overbook()
    {
        using var factory = new ApiFactory();
        await factory.ResetAsync();
        await factory.SetPrimaryOfferToOnePlaceAsync();
        using var firstClient = factory.CreateClient();
        using var secondClient = factory.CreateClient();
        var token = await LoginDemoAsync(firstClient);
        firstClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);
        secondClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);

        var responses = await Task.WhenAll(
            firstClient.PostAsJsonAsync("/api/bookings", new { offerId = DatabaseSeeder.PrimaryOfferId, quantity = 1 }),
            secondClient.PostAsJsonAsync("/api/bookings", new { offerId = DatabaseSeeder.PrimaryOfferId, quantity = 1 }));

        Assert.Single(responses, x => x.StatusCode == HttpStatusCode.Created);
        var conflict = Assert.Single(responses, x => x.StatusCode == HttpStatusCode.Conflict);
        var problem = await JsonDocument.ParseAsync(await conflict.Content.ReadAsStreamAsync());
        Assert.Equal("INSUFFICIENT_CAPACITY", problem.RootElement.GetProperty("code").GetString());
        Assert.Equal(0, problem.RootElement.GetProperty("availablePlaces").GetInt32());
    }

    [PostgresFact]
    public async Task Cancellation_and_favorites_are_idempotent_and_restore_capacity_once()
    {
        using var factory = new ApiFactory();
        await factory.ResetAsync();
        using var client = factory.CreateClient();
        await AuthenticateDemoAsync(client);
        var initialReserved = await GetPrimaryReservedQuantityAsync(factory);
        var create = await client.PostAsJsonAsync("/api/bookings", new { offerId = DatabaseSeeder.PrimaryOfferId, quantity = 1 });
        create.EnsureSuccessStatusCode();
        var bookingId = (await JsonDocument.ParseAsync(await create.Content.ReadAsStreamAsync())).RootElement.GetProperty("id").GetGuid();
        Assert.Equal(initialReserved + 1, await GetPrimaryReservedQuantityAsync(factory));

        var firstCancel = await client.PostAsync($"/api/bookings/{bookingId}/cancel", null);
        Assert.Equal(initialReserved, await GetPrimaryReservedQuantityAsync(factory));
        var secondCancel = await client.PostAsync($"/api/bookings/{bookingId}/cancel", null);
        Assert.Equal(HttpStatusCode.OK, firstCancel.StatusCode);
        Assert.Equal(HttpStatusCode.OK, secondCancel.StatusCode);
        Assert.Equal(initialReserved, await GetPrimaryReservedQuantityAsync(factory));

        Assert.Equal(HttpStatusCode.NoContent, (await client.PutAsync($"/api/favorites/offers/{DatabaseSeeder.HistoricalOfferId}", null)).StatusCode);
        Assert.Equal(HttpStatusCode.NoContent, (await client.PutAsync($"/api/favorites/offers/{DatabaseSeeder.HistoricalOfferId}", null)).StatusCode);
        var favorites = await client.GetFromJsonAsync<JsonElement>("/api/favorites/offers");
        Assert.Contains(favorites.GetProperty("items").EnumerateArray(), item => item.GetProperty("id").GetGuid() == DatabaseSeeder.HistoricalOfferId);
        Assert.Equal(HttpStatusCode.NoContent, (await client.DeleteAsync($"/api/favorites/offers/{DatabaseSeeder.HistoricalOfferId}")).StatusCode);
        Assert.Equal(HttpStatusCode.NoContent, (await client.DeleteAsync($"/api/favorites/offers/{DatabaseSeeder.HistoricalOfferId}")).StatusCode);
    }

    [PostgresFact]
    public async Task Invalid_and_duplicate_auth_requests_return_stable_problem_codes()
    {
        using var factory = new ApiFactory();
        await factory.ResetAsync();
        using var client = factory.CreateClient();

        var invalid = await client.PostAsJsonAsync("/api/auth/register", new
        {
            email = "not-an-email",
            password = "short",
            displayName = "x"
        });
        await AssertProblemAsync(invalid, HttpStatusCode.BadRequest, "VALIDATION_FAILED");

        var registration = new { email = "duplicate@example.hu", password = "ValidPass1", displayName = "Dupla Teszt" };
        Assert.Equal(HttpStatusCode.Created, (await client.PostAsJsonAsync("/api/auth/register", registration)).StatusCode);
        await AssertProblemAsync(
            await client.PostAsJsonAsync("/api/auth/register", registration),
            HttpStatusCode.Conflict,
            "AUTH_EMAIL_EXISTS");
        await AssertProblemAsync(
            await client.PostAsJsonAsync("/api/auth/login", new { registration.email, password = "WrongPass1" }),
            HttpStatusCode.Unauthorized,
            "AUTH_INVALID_CREDENTIALS");
    }

    [PostgresFact]
    public async Task Booking_rejects_quantities_outside_the_contract()
    {
        using var factory = new ApiFactory();
        await factory.ResetAsync();
        using var client = factory.CreateClient();
        await AuthenticateDemoAsync(client);

        foreach (var quantity in new[] { 0, 11 })
        {
            var response = await client.PostAsJsonAsync(
                "/api/bookings",
                new { offerId = DatabaseSeeder.PrimaryOfferId, quantity });
            var problem = await AssertProblemAsync(response, HttpStatusCode.BadRequest, "VALIDATION_FAILED");
            Assert.True(problem.RootElement.GetProperty("errors").TryGetProperty("quantity", out _));
        }
    }

    [PostgresFact]
    public async Task Cancellation_after_deadline_is_rejected_without_restoring_capacity()
    {
        using var factory = new ApiFactory();
        await factory.ResetAsync();
        using var client = factory.CreateClient();
        await AuthenticateDemoAsync(client);
        var create = await client.PostAsJsonAsync("/api/bookings", new { offerId = DatabaseSeeder.PrimaryOfferId, quantity = 2 });
        create.EnsureSuccessStatusCode();
        var bookingId = (await JsonDocument.ParseAsync(await create.Content.ReadAsStreamAsync())).RootElement.GetProperty("id").GetGuid();
        var reservedAfterCreate = await GetPrimaryReservedQuantityAsync(factory);

        await using (var scope = factory.Services.CreateAsyncScope())
        {
            var dbContext = scope.ServiceProvider.GetRequiredService<UgorjBeDbContext>();
            var booking = await dbContext.Bookings.SingleAsync(x => x.Id == bookingId);
            booking.CancellationDeadlineUtc = factory.Clock.GetUtcNow().AddSeconds(-1);
            await dbContext.SaveChangesAsync();
        }

        await AssertProblemAsync(
            await client.PostAsync($"/api/bookings/{bookingId}/cancel", null),
            HttpStatusCode.Conflict,
            "CANCELLATION_NOT_ALLOWED");
        Assert.Equal(reservedAfterCreate, await GetPrimaryReservedQuantityAsync(factory));
    }

    [PostgresFact]
    public async Task Booking_detail_and_cancel_do_not_disclose_another_users_booking()
    {
        using var factory = new ApiFactory();
        await factory.ResetAsync();
        using var ownerClient = factory.CreateClient();
        await AuthenticateDemoAsync(ownerClient);
        var create = await ownerClient.PostAsJsonAsync("/api/bookings", new { offerId = DatabaseSeeder.PrimaryOfferId, quantity = 1 });
        create.EnsureSuccessStatusCode();
        var bookingId = (await JsonDocument.ParseAsync(await create.Content.ReadAsStreamAsync())).RootElement.GetProperty("id").GetGuid();

        using var otherClient = factory.CreateClient();
        var registration = await otherClient.PostAsJsonAsync("/api/auth/register", new
        {
            email = "masik@example.hu",
            password = "ValidPass1",
            displayName = "Másik család"
        });
        registration.EnsureSuccessStatusCode();
        var otherToken = (await JsonDocument.ParseAsync(await registration.Content.ReadAsStreamAsync())).RootElement.GetProperty("accessToken").GetString();
        otherClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", otherToken);

        await AssertProblemAsync(await otherClient.GetAsync($"/api/bookings/{bookingId}"), HttpStatusCode.NotFound, "BOOKING_NOT_FOUND");
        await AssertProblemAsync(await otherClient.PostAsync($"/api/bookings/{bookingId}/cancel", null), HttpStatusCode.NotFound, "BOOKING_NOT_FOUND");
    }

    [PostgresFact]
    public async Task Active_and_previous_scopes_separate_confirmed_and_cancelled_bookings()
    {
        using var factory = new ApiFactory();
        await factory.ResetAsync();
        using var client = factory.CreateClient();
        await AuthenticateDemoAsync(client);
        var activeId = await CreateBookingAsync(client, DatabaseSeeder.PrimaryOfferId, 1);
        var cancelledId = await CreateBookingAsync(client, DatabaseSeeder.PrimaryOfferId, 1);
        (await client.PostAsync($"/api/bookings/{cancelledId}/cancel", null)).EnsureSuccessStatusCode();

        var active = await client.GetFromJsonAsync<JsonElement>("/api/bookings?scope=ACTIVE");
        var previous = await client.GetFromJsonAsync<JsonElement>("/api/bookings?scope=PREVIOUS");
        Assert.Contains(active.GetProperty("items").EnumerateArray(), x => x.GetProperty("id").GetGuid() == activeId);
        Assert.DoesNotContain(active.GetProperty("items").EnumerateArray(), x => x.GetProperty("id").GetGuid() == cancelledId);
        Assert.Contains(previous.GetProperty("items").EnumerateArray(), x => x.GetProperty("id").GetGuid() == cancelledId);
        Assert.DoesNotContain(previous.GetProperty("items").EnumerateArray(), x => x.GetProperty("id").GetGuid() == activeId);
    }

    [PostgresFact]
    public async Task Provider_favorites_are_idempotent_and_user_scoped()
    {
        using var factory = new ApiFactory();
        await factory.ResetAsync();
        using var client = factory.CreateClient();
        await AuthenticateDemoAsync(client);
        var providerId = Guid.Parse("22222222-2222-2222-2222-222222222223");

        Assert.Equal(HttpStatusCode.NoContent, (await client.PutAsync($"/api/favorites/providers/{providerId}", null)).StatusCode);
        Assert.Equal(HttpStatusCode.NoContent, (await client.PutAsync($"/api/favorites/providers/{providerId}", null)).StatusCode);
        var providers = await client.GetFromJsonAsync<JsonElement>("/api/favorites/providers");
        Assert.Single(providers.GetProperty("items").EnumerateArray(), x => x.GetProperty("id").GetGuid() == providerId);
        Assert.Equal(HttpStatusCode.NoContent, (await client.DeleteAsync($"/api/favorites/providers/{providerId}")).StatusCode);
        Assert.Equal(HttpStatusCode.NoContent, (await client.DeleteAsync($"/api/favorites/providers/{providerId}")).StatusCode);
        await AssertProblemAsync(
            await client.PutAsync($"/api/favorites/providers/{Guid.NewGuid()}", null),
            HttpStatusCode.NotFound,
            "PROVIDER_NOT_FOUND");
    }

    [PostgresFact]
    public async Task Catalog_applies_representative_filters_and_price_sort()
    {
        using var factory = new ApiFactory();
        await factory.ResetAsync();
        using var client = factory.CreateClient();
        var startsFrom = Uri.EscapeDataString(factory.Clock.GetUtcNow().ToString("O"));
        var startsTo = Uri.EscapeDataString(factory.Clock.GetUtcNow().AddDays(2).ToString("O"));

        var response = await client.GetFromJsonAsync<JsonElement>(
            $"/api/offers?category=WORKSHOP&childAge=5&minPrice=2500&maxPrice=3500&minAvailablePlaces=2&sort=PRICE&direction=DESC&startsFromUtc={startsFrom}&startsToUtc={startsTo}");
        var items = response.GetProperty("items").EnumerateArray().ToArray();
        Assert.NotEmpty(items);
        Assert.All(items, item =>
        {
            Assert.Equal("WORKSHOP", item.GetProperty("category").GetString());
            Assert.InRange(item.GetProperty("discountedUnitPrice").GetProperty("amount").GetDecimal(), 2500m, 3500m);
            Assert.True(item.GetProperty("availablePlaces").GetInt32() >= 2);
            Assert.True(item.GetProperty("minChildAge").GetInt32() <= 5);
            Assert.True(item.GetProperty("maxChildAge").GetInt32() >= 5);
        });
        var prices = items.Select(x => x.GetProperty("discountedUnitPrice").GetProperty("amount").GetDecimal()).ToArray();
        Assert.Equal(prices.OrderByDescending(x => x), prices);
    }

    private static async Task<Guid> CreateBookingAsync(HttpClient client, Guid offerId, int quantity)
    {
        var response = await client.PostAsJsonAsync("/api/bookings", new { offerId, quantity });
        response.EnsureSuccessStatusCode();
        return (await JsonDocument.ParseAsync(await response.Content.ReadAsStreamAsync())).RootElement.GetProperty("id").GetGuid();
    }

    private static async Task<int> GetPrimaryReservedQuantityAsync(ApiFactory factory)
    {
        await using var scope = factory.Services.CreateAsyncScope();
        var dbContext = scope.ServiceProvider.GetRequiredService<UgorjBeDbContext>();
        return await dbContext.Offers
            .Where(x => x.Id == DatabaseSeeder.PrimaryOfferId)
            .Select(x => x.ReservedQuantity)
            .SingleAsync();
    }

    private static async Task<JsonDocument> AssertProblemAsync(HttpResponseMessage response, HttpStatusCode status, string code)
    {
        Assert.Equal(status, response.StatusCode);
        Assert.Equal("application/problem+json", response.Content.Headers.ContentType?.MediaType);
        var problem = await JsonDocument.ParseAsync(await response.Content.ReadAsStreamAsync());
        Assert.Equal(code, problem.RootElement.GetProperty("code").GetString());
        return problem;
    }

    private static async Task AuthenticateDemoAsync(HttpClient client) =>
        client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", await LoginDemoAsync(client));

    private static async Task<string> LoginDemoAsync(HttpClient client)
    {
        var login = await client.PostAsJsonAsync("/api/auth/login", new { email = "demo@ugorjbe.local", password = "UgorjBe123!" });
        login.EnsureSuccessStatusCode();
        var document = await JsonDocument.ParseAsync(await login.Content.ReadAsStreamAsync());
        return document.RootElement.GetProperty("accessToken").GetString()!;
    }
}
