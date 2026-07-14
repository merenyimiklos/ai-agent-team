using Microsoft.EntityFrameworkCore;
using UgorjBe.Domain;

namespace UgorjBe.Infrastructure.Persistence;

public sealed class UgorjBeDbContext(DbContextOptions<UgorjBeDbContext> options) : DbContext(options)
{
    public DbSet<User> Users => Set<User>();
    public DbSet<Provider> Providers => Set<Provider>();
    public DbSet<Offer> Offers => Set<Offer>();
    public DbSet<Booking> Bookings => Set<Booking>();
    public DbSet<FavoriteOffer> FavoriteOffers => Set<FavoriteOffer>();
    public DbSet<FavoriteProvider> FavoriteProviders => Set<FavoriteProvider>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<User>(entity =>
        {
            entity.ToTable("users");
            entity.HasKey(x => x.Id);
            entity.Property(x => x.Email).HasMaxLength(254).IsRequired();
            entity.Property(x => x.NormalizedEmail).HasMaxLength(254).IsRequired();
            entity.Property(x => x.PasswordHash).HasMaxLength(1024).IsRequired();
            entity.Property(x => x.DisplayName).HasMaxLength(80).IsRequired();
            entity.Property(x => x.Locale).HasMaxLength(16).IsRequired();
            entity.Property(x => x.Role).HasMaxLength(32).IsRequired();
            entity.HasIndex(x => x.NormalizedEmail).IsUnique();
        });

        modelBuilder.Entity<Provider>(entity =>
        {
            entity.ToTable("providers");
            entity.HasKey(x => x.Id);
            entity.Property(x => x.Name).HasMaxLength(120).IsRequired();
            entity.Property(x => x.ShortDescription).HasMaxLength(240).IsRequired();
            entity.Property(x => x.Description).HasMaxLength(2000).IsRequired();
            entity.Property(x => x.PostalCode).HasMaxLength(16).IsRequired();
            entity.Property(x => x.City).HasMaxLength(100).IsRequired();
            entity.Property(x => x.Street).HasMaxLength(200).IsRequired();
            entity.Property(x => x.CountryCode).HasMaxLength(2).IsRequired();
            entity.Property(x => x.Latitude).HasPrecision(9, 6);
            entity.Property(x => x.Longitude).HasPrecision(9, 6);
            entity.Property(x => x.Phone).HasMaxLength(40);
            entity.Property(x => x.Email).HasMaxLength(254);
            entity.Property(x => x.WebsiteUrl).HasMaxLength(500);
            entity.Property(x => x.AccessibilityInfo).HasMaxLength(500);
            entity.Property(x => x.ImageUrl).HasMaxLength(1000);
        });

        modelBuilder.Entity<Offer>(entity =>
        {
            entity.ToTable("offers", table =>
            {
                table.HasCheckConstraint("ck_offers_time", "ends_at_utc > starts_at_utc AND booking_cutoff_utc <= starts_at_utc AND cancel_until_utc <= starts_at_utc");
                table.HasCheckConstraint("ck_offers_age", "min_child_age >= 0 AND max_child_age <= 18 AND min_child_age <= max_child_age");
                table.HasCheckConstraint("ck_offers_price", "original_unit_price >= 0 AND discounted_unit_price >= 0 AND discounted_unit_price <= original_unit_price");
                table.HasCheckConstraint("ck_offers_capacity", "total_capacity > 0 AND reserved_quantity >= 0 AND reserved_quantity <= total_capacity");
            });
            entity.HasKey(x => x.Id);
            entity.HasOne(x => x.Provider).WithMany(x => x.Offers).HasForeignKey(x => x.ProviderId).OnDelete(DeleteBehavior.Restrict);
            entity.Property(x => x.Title).HasMaxLength(160).IsRequired();
            entity.Property(x => x.Description).HasMaxLength(3000).IsRequired();
            entity.Property(x => x.Category).HasConversion<string>().HasMaxLength(32);
            entity.Property(x => x.Status).HasConversion<string>().HasMaxLength(16);
            entity.Property(x => x.AccessibilityInfo).HasMaxLength(500);
            entity.Property(x => x.ImageUrl).HasMaxLength(1000);
            entity.Property(x => x.OriginalUnitPrice).HasPrecision(12, 2);
            entity.Property(x => x.DiscountedUnitPrice).HasPrecision(12, 2);
            entity.Property(x => x.Currency).HasMaxLength(3).IsRequired();
            entity.Ignore(x => x.AvailablePlaces);
            entity.HasIndex(x => new { x.Status, x.StartsAtUtc, x.BookingCutoffUtc });
            entity.HasIndex(x => new { x.ProviderId, x.StartsAtUtc });
            entity.HasIndex(x => new { x.Category, x.StartsAtUtc });
        });

        modelBuilder.Entity<Booking>(entity =>
        {
            entity.ToTable("bookings", table =>
            {
                table.HasCheckConstraint("ck_bookings_quantity", "quantity > 0");
                table.HasCheckConstraint("ck_bookings_price", "unit_price >= 0 AND total_price >= 0");
            });
            entity.HasKey(x => x.Id);
            entity.HasOne(x => x.User).WithMany(x => x.Bookings).HasForeignKey(x => x.UserId).OnDelete(DeleteBehavior.Restrict);
            entity.HasOne(x => x.Offer).WithMany(x => x.Bookings).HasForeignKey(x => x.OfferId).OnDelete(DeleteBehavior.Restrict);
            entity.Property(x => x.Status).HasConversion<string>().HasMaxLength(16);
            entity.Property(x => x.UnitPrice).HasPrecision(12, 2);
            entity.Property(x => x.TotalPrice).HasPrecision(12, 2);
            entity.Property(x => x.Currency).HasMaxLength(3).IsRequired();
            entity.Property(x => x.OfferTitle).HasMaxLength(160).IsRequired();
            entity.Property(x => x.OfferCategory).HasConversion<string>().HasMaxLength(32);
            entity.Property(x => x.ProviderName).HasMaxLength(120).IsRequired();
            entity.Property(x => x.PostalCode).HasMaxLength(16).IsRequired();
            entity.Property(x => x.City).HasMaxLength(100).IsRequired();
            entity.Property(x => x.Street).HasMaxLength(200).IsRequired();
            entity.Property(x => x.CountryCode).HasMaxLength(2).IsRequired();
            entity.Property(x => x.Latitude).HasPrecision(9, 6);
            entity.Property(x => x.Longitude).HasPrecision(9, 6);
            entity.Property(x => x.ImageUrl).HasMaxLength(1000);
            entity.Property(x => x.BookingCode).HasMaxLength(10).IsRequired();
            entity.Property(x => x.QrPayload).HasMaxLength(200).IsRequired();
            entity.HasIndex(x => new { x.UserId, x.CreatedAtUtc }).IsDescending(false, true);
            entity.HasIndex(x => x.BookingCode).IsUnique();
        });

        modelBuilder.Entity<FavoriteOffer>(entity =>
        {
            entity.ToTable("favorite_offers");
            entity.HasKey(x => new { x.UserId, x.OfferId });
            entity.HasOne(x => x.User).WithMany().HasForeignKey(x => x.UserId).OnDelete(DeleteBehavior.Cascade);
            entity.HasOne(x => x.Offer).WithMany().HasForeignKey(x => x.OfferId).OnDelete(DeleteBehavior.Restrict);
            entity.HasIndex(x => x.OfferId);
        });

        modelBuilder.Entity<FavoriteProvider>(entity =>
        {
            entity.ToTable("favorite_providers");
            entity.HasKey(x => new { x.UserId, x.ProviderId });
            entity.HasOne(x => x.User).WithMany().HasForeignKey(x => x.UserId).OnDelete(DeleteBehavior.Cascade);
            entity.HasOne(x => x.Provider).WithMany().HasForeignKey(x => x.ProviderId).OnDelete(DeleteBehavior.Restrict);
            entity.HasIndex(x => x.ProviderId);
        });
    }
}
