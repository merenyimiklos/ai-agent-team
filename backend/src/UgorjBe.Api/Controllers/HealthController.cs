using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using UgorjBe.Application;
using UgorjBe.Infrastructure.Persistence;

namespace UgorjBe.Api.Controllers;

[Route("health")]
[AllowAnonymous]
public sealed class HealthController(UgorjBeDbContext dbContext) : ApiControllerBase
{
    [HttpGet]
    public async Task<IActionResult> Get(CancellationToken cancellationToken)
    {
        try
        {
            if (await dbContext.Database.CanConnectAsync(cancellationToken)) return Ok(new { status = "Healthy" });
        }
        catch (Exception exception) when (exception is not OperationCanceledException)
        {
            // The standard dependency problem below intentionally hides connection details.
        }

        throw new AppException(
            503,
            "DEPENDENCY_UNAVAILABLE",
            "Az adatbázis nem érhető el.",
            "A szolgáltatás átmenetileg nem elérhető.");
    }
}
