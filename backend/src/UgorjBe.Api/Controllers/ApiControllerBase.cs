using System.IdentityModel.Tokens.Jwt;
using Microsoft.AspNetCore.Mvc;
using UgorjBe.Application;

namespace UgorjBe.Api.Controllers;

[ApiController]
public abstract class ApiControllerBase : ControllerBase
{
    protected Guid CurrentUserId
    {
        get
        {
            var value = User.FindFirst(JwtRegisteredClaimNames.Sub)?.Value;
            if (!Guid.TryParse(value, out var userId))
            {
                throw new AppException(401, "AUTH_REQUIRED", "Bejelentkezés szükséges.", "A hozzáférési token érvénytelen.");
            }

            return userId;
        }
    }
}
