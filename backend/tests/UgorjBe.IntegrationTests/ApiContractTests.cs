using System.Net;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text.Json;
using UgorjBe.Infrastructure.Persistence;
using Xunit;

namespace UgorjBe.IntegrationTests;

public sealed class ApiContractTests
{
    private static readonly Guid ExpiredOfferId = Guid.Parse("33333333-3333-3333-3333-333333333340");

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

        var response = await client.PostAsJsonAsync("/api/bookings", new { offerId = ExpiredOfferId, quantity = 1 });

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
        var create = await client.PostAsJsonAsync("/api/bookings", new { offerId = DatabaseSeeder.PrimaryOfferId, quantity = 1 });
        create.EnsureSuccessStatusCode();
        var bookingId = (await JsonDocument.ParseAsync(await create.Content.ReadAsStreamAsync())).RootElement.GetProperty("id").GetGuid();

        var firstCancel = await client.PostAsync($"/api/bookings/{bookingId}/cancel", null);
        var secondCancel = await client.PostAsync($"/api/bookings/{bookingId}/cancel", null);
        Assert.Equal(HttpStatusCode.OK, firstCancel.StatusCode);
        Assert.Equal(HttpStatusCode.OK, secondCancel.StatusCode);

        Assert.Equal(HttpStatusCode.NoContent, (await client.PutAsync($"/api/favorites/offers/{ExpiredOfferId}", null)).StatusCode);
        Assert.Equal(HttpStatusCode.NoContent, (await client.PutAsync($"/api/favorites/offers/{ExpiredOfferId}", null)).StatusCode);
        var favorites = await client.GetFromJsonAsync<JsonElement>("/api/favorites/offers");
        Assert.Contains(favorites.GetProperty("items").EnumerateArray(), item => item.GetProperty("id").GetGuid() == ExpiredOfferId);
        Assert.Equal(HttpStatusCode.NoContent, (await client.DeleteAsync($"/api/favorites/offers/{ExpiredOfferId}")).StatusCode);
        Assert.Equal(HttpStatusCode.NoContent, (await client.DeleteAsync($"/api/favorites/offers/{ExpiredOfferId}")).StatusCode);
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
