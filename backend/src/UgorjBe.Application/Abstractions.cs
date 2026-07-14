namespace UgorjBe.Application;

public interface IAuthService
{
    Task<AuthResponse> RegisterAsync(RegisterRequest request, CancellationToken cancellationToken);
    Task<AuthResponse> LoginAsync(LoginRequest request, CancellationToken cancellationToken);
    Task<UserDto> GetCurrentUserAsync(Guid userId, CancellationToken cancellationToken);
}

public interface ICatalogService
{
    Task<PageDto<OfferSummaryDto>> GetOffersAsync(OfferQuery query, CancellationToken cancellationToken);
    Task<OfferDetailDto> GetOfferAsync(Guid offerId, CoordinateQuery query, CancellationToken cancellationToken);
    Task<ProviderDetailDto> GetProviderAsync(Guid providerId, CancellationToken cancellationToken);
}

public interface IBookingService
{
    Task<BookingDto> CreateAsync(Guid userId, CreateBookingRequest request, CancellationToken cancellationToken);
    Task<PageDto<BookingDto>> ListAsync(Guid userId, BookingQuery query, CancellationToken cancellationToken);
    Task<BookingDto> GetAsync(Guid userId, Guid bookingId, CancellationToken cancellationToken);
    Task<BookingDto> CancelAsync(Guid userId, Guid bookingId, CancellationToken cancellationToken);
}

public interface IFavoriteService
{
    Task<PageDto<OfferSummaryDto>> ListOffersAsync(Guid userId, PageQuery query, CancellationToken cancellationToken);
    Task AddOfferAsync(Guid userId, Guid offerId, CancellationToken cancellationToken);
    Task RemoveOfferAsync(Guid userId, Guid offerId, CancellationToken cancellationToken);
    Task<PageDto<ProviderSummaryDto>> ListProvidersAsync(Guid userId, PageQuery query, CancellationToken cancellationToken);
    Task AddProviderAsync(Guid userId, Guid providerId, CancellationToken cancellationToken);
    Task RemoveProviderAsync(Guid userId, Guid providerId, CancellationToken cancellationToken);
}
