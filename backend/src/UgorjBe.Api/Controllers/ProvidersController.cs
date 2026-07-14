using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using UgorjBe.Application;

namespace UgorjBe.Api.Controllers;

[Route("api/providers")]
[AllowAnonymous]
public sealed class ProvidersController(ICatalogService catalogService) : ApiControllerBase
{
    [HttpGet("{providerId:guid}")]
    [ProducesResponseType<ProviderDetailDto>(StatusCodes.Status200OK)]
    public async Task<ActionResult<ProviderDetailDto>> Detail(Guid providerId, CancellationToken cancellationToken) =>
        Ok(await catalogService.GetProviderAsync(providerId, cancellationToken));
}
