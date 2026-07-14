using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using UgorjBe.Application;

namespace UgorjBe.Api.Controllers;

[Route("api/bookings")]
[Authorize]
public sealed class BookingsController(IBookingService bookingService) : ApiControllerBase
{
    [HttpPost]
    [ProducesResponseType<BookingDto>(StatusCodes.Status201Created)]
    public async Task<IActionResult> Create([FromBody] CreateBookingRequest request, CancellationToken cancellationToken)
    {
        var booking = await bookingService.CreateAsync(CurrentUserId, request, cancellationToken);
        return Created($"/api/bookings/{booking.Id}", booking);
    }

    [HttpGet]
    [ProducesResponseType<PageDto<BookingDto>>(StatusCodes.Status200OK)]
    public async Task<ActionResult<PageDto<BookingDto>>> List([FromQuery] BookingQuery query, CancellationToken cancellationToken) =>
        Ok(await bookingService.ListAsync(CurrentUserId, query, cancellationToken));

    [HttpGet("{bookingId:guid}")]
    [ProducesResponseType<BookingDto>(StatusCodes.Status200OK)]
    public async Task<ActionResult<BookingDto>> Detail(Guid bookingId, CancellationToken cancellationToken) =>
        Ok(await bookingService.GetAsync(CurrentUserId, bookingId, cancellationToken));

    [HttpPost("{bookingId:guid}/cancel")]
    [ProducesResponseType<BookingDto>(StatusCodes.Status200OK)]
    public async Task<ActionResult<BookingDto>> Cancel(Guid bookingId, CancellationToken cancellationToken) =>
        Ok(await bookingService.CancelAsync(CurrentUserId, bookingId, cancellationToken));
}
