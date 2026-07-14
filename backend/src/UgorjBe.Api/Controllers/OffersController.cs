using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using UgorjBe.Application;

namespace UgorjBe.Api.Controllers;

[Route("api/offers")]
[AllowAnonymous]
public sealed class OffersController(ICatalogService catalogService) : ApiControllerBase
{
    [HttpGet]
    [ProducesResponseType<PageDto<OfferSummaryDto>>(StatusCodes.Status200OK)]
    public async Task<ActionResult<PageDto<OfferSummaryDto>>> List([FromQuery] OfferQuery query, CancellationToken cancellationToken) =>
        Ok(await catalogService.GetOffersAsync(query, cancellationToken));

    [HttpGet("map")]
    [ProducesResponseType<MapOfferEnvelope>(StatusCodes.Status200OK)]
    public async Task<ActionResult<MapOfferEnvelope>> Map([FromQuery] MapOfferQuery query, CancellationToken cancellationToken) =>
        Ok(await catalogService.GetMapOffersAsync(query, cancellationToken));

    [HttpGet("{offerId:guid}")]
    [ProducesResponseType<OfferDetailDto>(StatusCodes.Status200OK)]
    public async Task<ActionResult<OfferDetailDto>> Detail(Guid offerId, [FromQuery] CoordinateQuery query, CancellationToken cancellationToken) =>
        Ok(await catalogService.GetOfferAsync(offerId, query, cancellationToken));
}
