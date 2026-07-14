using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using UgorjBe.Application;

namespace UgorjBe.Api.Controllers;

[Route("api/admin")]
[Authorize(Policy = "AdminOnly")]
public sealed class AdminController(IAdminService adminService) : ApiControllerBase
{
    [HttpGet("dashboard")]
    [ProducesResponseType<AdminDashboardDto>(StatusCodes.Status200OK)]
    public async Task<ActionResult<AdminDashboardDto>> Dashboard(CancellationToken cancellationToken) =>
        Ok(await adminService.GetDashboardAsync(cancellationToken));

    [HttpGet("providers")]
    [ProducesResponseType<PageDto<AdminProviderSummaryDto>>(StatusCodes.Status200OK)]
    public async Task<ActionResult<PageDto<AdminProviderSummaryDto>>> Providers([FromQuery] AdminProviderQuery query, CancellationToken cancellationToken) =>
        Ok(await adminService.GetProvidersAsync(query, cancellationToken));

    [HttpPost("providers")]
    [ProducesResponseType<AdminProviderDetailDto>(StatusCodes.Status201Created)]
    public async Task<ActionResult<AdminProviderDetailDto>> CreateProvider([FromBody] ProviderCreateRequest request, CancellationToken cancellationToken)
    {
        var result = await adminService.CreateProviderAsync(request, cancellationToken);
        return CreatedAtAction(nameof(Provider), new { providerId = result.Id }, result);
    }

    [HttpGet("providers/{providerId:guid}")]
    [ProducesResponseType<AdminProviderDetailDto>(StatusCodes.Status200OK)]
    public async Task<ActionResult<AdminProviderDetailDto>> Provider(Guid providerId, CancellationToken cancellationToken) =>
        Ok(await adminService.GetProviderAsync(providerId, cancellationToken));

    [HttpPut("providers/{providerId:guid}")]
    [ProducesResponseType<AdminProviderDetailDto>(StatusCodes.Status200OK)]
    public async Task<ActionResult<AdminProviderDetailDto>> UpdateProvider(Guid providerId, [FromBody] ProviderUpdateRequest request, CancellationToken cancellationToken) =>
        Ok(await adminService.UpdateProviderAsync(providerId, request, cancellationToken));

    [HttpGet("offers")]
    [ProducesResponseType<PageDto<AdminOfferSummaryDto>>(StatusCodes.Status200OK)]
    public async Task<ActionResult<PageDto<AdminOfferSummaryDto>>> Offers([FromQuery] AdminOfferQuery query, CancellationToken cancellationToken) =>
        Ok(await adminService.GetOffersAsync(query, cancellationToken));

    [HttpPost("offers")]
    [ProducesResponseType<AdminOfferDetailDto>(StatusCodes.Status201Created)]
    public async Task<ActionResult<AdminOfferDetailDto>> CreateOffer([FromBody] OfferWriteRequest request, CancellationToken cancellationToken)
    {
        var result = await adminService.CreateOfferAsync(request, cancellationToken);
        return CreatedAtAction(nameof(Offer), new { offerId = result.Id }, result);
    }

    [HttpGet("offers/{offerId:guid}")]
    [ProducesResponseType<AdminOfferDetailDto>(StatusCodes.Status200OK)]
    public async Task<ActionResult<AdminOfferDetailDto>> Offer(Guid offerId, CancellationToken cancellationToken) =>
        Ok(await adminService.GetOfferAsync(offerId, cancellationToken));

    [HttpPut("offers/{offerId:guid}")]
    [ProducesResponseType<AdminOfferDetailDto>(StatusCodes.Status200OK)]
    public async Task<ActionResult<AdminOfferDetailDto>> UpdateOffer(Guid offerId, [FromBody] OfferUpdateRequest request, CancellationToken cancellationToken) =>
        Ok(await adminService.UpdateOfferAsync(offerId, request, cancellationToken));

    [HttpPost("offers/{offerId:guid}/publish")]
    public async Task<ActionResult<AdminOfferDetailDto>> Publish(Guid offerId, [FromBody] LifecycleRequest request, CancellationToken cancellationToken) =>
        Ok(await adminService.PublishAsync(offerId, request, cancellationToken));

    [HttpPost("offers/{offerId:guid}/unpublish")]
    public async Task<ActionResult<AdminOfferDetailDto>> Unpublish(Guid offerId, [FromBody] LifecycleRequest request, CancellationToken cancellationToken) =>
        Ok(await adminService.UnpublishAsync(offerId, request, cancellationToken));

    [HttpPost("offers/{offerId:guid}/archive")]
    public async Task<ActionResult<AdminOfferDetailDto>> Archive(Guid offerId, [FromBody] LifecycleRequest request, CancellationToken cancellationToken) =>
        Ok(await adminService.ArchiveAsync(offerId, request, cancellationToken));
}
