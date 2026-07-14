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
    Task<MapOfferEnvelope> GetMapOffersAsync(MapOfferQuery query, CancellationToken cancellationToken);
    Task<OfferDetailDto> GetOfferAsync(Guid offerId, CoordinateQuery query, CancellationToken cancellationToken);
    Task<ProviderDetailDto> GetProviderAsync(Guid providerId, CancellationToken cancellationToken);
}

public interface IAdminService
{
    Task<AdminDashboardDto> GetDashboardAsync(CancellationToken cancellationToken);
    Task<PageDto<AdminProviderSummaryDto>> GetProvidersAsync(AdminProviderQuery query, CancellationToken cancellationToken);
    Task<AdminProviderDetailDto> GetProviderAsync(Guid id, CancellationToken cancellationToken);
    Task<AdminProviderDetailDto> CreateProviderAsync(ProviderCreateRequest request, CancellationToken cancellationToken);
    Task<AdminProviderDetailDto> UpdateProviderAsync(Guid id, ProviderUpdateRequest request, CancellationToken cancellationToken);
    Task<PageDto<AdminOfferSummaryDto>> GetOffersAsync(AdminOfferQuery query, CancellationToken cancellationToken);
    Task<AdminOfferDetailDto> GetOfferAsync(Guid id, CancellationToken cancellationToken);
    Task<AdminOfferDetailDto> CreateOfferAsync(OfferWriteRequest request, CancellationToken cancellationToken);
    Task<AdminOfferDetailDto> UpdateOfferAsync(Guid id, OfferUpdateRequest request, CancellationToken cancellationToken);
    Task<AdminOfferDetailDto> PublishAsync(Guid id, LifecycleRequest request, CancellationToken cancellationToken);
    Task<AdminOfferDetailDto> UnpublishAsync(Guid id, LifecycleRequest request, CancellationToken cancellationToken);
    Task<AdminOfferDetailDto> ArchiveAsync(Guid id, LifecycleRequest request, CancellationToken cancellationToken);
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
