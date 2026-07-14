using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using UgorjBe.Application;

namespace UgorjBe.Api.Controllers;

[Route("api/favorites")]
[Authorize]
public sealed class FavoritesController(IFavoriteService favoriteService) : ApiControllerBase
{
    [HttpGet("offers")]
    public async Task<ActionResult<PageDto<OfferSummaryDto>>> OfferList([FromQuery] PageQuery query, CancellationToken cancellationToken) =>
        Ok(await favoriteService.ListOffersAsync(CurrentUserId, query, cancellationToken));

    [HttpPut("offers/{offerId:guid}")]
    public async Task<IActionResult> AddOffer(Guid offerId, CancellationToken cancellationToken)
    {
        await favoriteService.AddOfferAsync(CurrentUserId, offerId, cancellationToken);
        return NoContent();
    }

    [HttpDelete("offers/{offerId:guid}")]
    public async Task<IActionResult> RemoveOffer(Guid offerId, CancellationToken cancellationToken)
    {
        await favoriteService.RemoveOfferAsync(CurrentUserId, offerId, cancellationToken);
        return NoContent();
    }

    [HttpGet("providers")]
    public async Task<ActionResult<PageDto<ProviderSummaryDto>>> ProviderList([FromQuery] PageQuery query, CancellationToken cancellationToken) =>
        Ok(await favoriteService.ListProvidersAsync(CurrentUserId, query, cancellationToken));

    [HttpPut("providers/{providerId:guid}")]
    public async Task<IActionResult> AddProvider(Guid providerId, CancellationToken cancellationToken)
    {
        await favoriteService.AddProviderAsync(CurrentUserId, providerId, cancellationToken);
        return NoContent();
    }

    [HttpDelete("providers/{providerId:guid}")]
    public async Task<IActionResult> RemoveProvider(Guid providerId, CancellationToken cancellationToken)
    {
        await favoriteService.RemoveProviderAsync(CurrentUserId, providerId, cancellationToken);
        return NoContent();
    }
}
