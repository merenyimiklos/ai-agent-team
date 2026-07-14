using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace UgorjBe.Infrastructure.Persistence.Migrations
{
    /// <inheritdoc />
    public partial class Phase2MapAndAdmin : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<DateTimeOffset>(
                name: "archived_at_utc",
                table: "offers",
                type: "timestamp with time zone",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "city",
                table: "offers",
                type: "character varying(100)",
                maxLength: 100,
                nullable: false,
                defaultValue: "");

            migrationBuilder.AddColumn<string>(
                name: "country_code",
                table: "offers",
                type: "character varying(2)",
                maxLength: 2,
                nullable: false,
                defaultValue: "");

            migrationBuilder.AddColumn<decimal>(
                name: "latitude",
                table: "offers",
                type: "numeric(9,6)",
                precision: 9,
                scale: 6,
                nullable: false,
                defaultValue: 0m);

            migrationBuilder.AddColumn<decimal>(
                name: "longitude",
                table: "offers",
                type: "numeric(9,6)",
                precision: 9,
                scale: 6,
                nullable: false,
                defaultValue: 0m);

            migrationBuilder.AddColumn<string>(
                name: "postal_code",
                table: "offers",
                type: "character varying(16)",
                maxLength: 16,
                nullable: false,
                defaultValue: "");

            migrationBuilder.AddColumn<DateTimeOffset>(
                name: "published_at_utc",
                table: "offers",
                type: "timestamp with time zone",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "street",
                table: "offers",
                type: "character varying(200)",
                maxLength: 200,
                nullable: false,
                defaultValue: "");

            // xmin is a PostgreSQL system column and already exists on every table.
            // EF maps it as the opaque concurrency version; it must never be added explicitly.
            migrationBuilder.Sql("""
                UPDATE offers AS o
                SET postal_code = p.postal_code,
                    city = p.city,
                    street = p.street,
                    country_code = p.country_code,
                    latitude = p.latitude,
                    longitude = p.longitude,
                    status = CASE WHEN o.status = 'WITHDRAWN' THEN 'UNPUBLISHED' ELSE o.status END,
                    published_at_utc = CASE WHEN o.status = 'PUBLISHED' THEN COALESCE(o.updated_at_utc, o.created_at_utc) ELSE NULL END
                FROM providers AS p
                WHERE p.id = o.provider_id;
                """);

            migrationBuilder.CreateIndex(
                name: "ix_providers_updated_at_utc_id",
                table: "providers",
                columns: new[] { "updated_at_utc", "id" });

            migrationBuilder.CreateIndex(
                name: "ix_offers_status_latitude_starts_at_utc",
                table: "offers",
                columns: new[] { "status", "latitude", "starts_at_utc" });

            migrationBuilder.CreateIndex(
                name: "ix_offers_status_longitude_starts_at_utc",
                table: "offers",
                columns: new[] { "status", "longitude", "starts_at_utc" });

            migrationBuilder.CreateIndex(
                name: "ix_offers_updated_at_utc_id",
                table: "offers",
                columns: new[] { "updated_at_utc", "id" });
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropIndex(
                name: "ix_providers_updated_at_utc_id",
                table: "providers");

            migrationBuilder.DropIndex(
                name: "ix_offers_status_latitude_starts_at_utc",
                table: "offers");

            migrationBuilder.DropIndex(
                name: "ix_offers_status_longitude_starts_at_utc",
                table: "offers");

            migrationBuilder.DropIndex(
                name: "ix_offers_updated_at_utc_id",
                table: "offers");

            migrationBuilder.DropColumn(
                name: "archived_at_utc",
                table: "offers");

            migrationBuilder.DropColumn(
                name: "city",
                table: "offers");

            migrationBuilder.DropColumn(
                name: "country_code",
                table: "offers");

            migrationBuilder.DropColumn(
                name: "latitude",
                table: "offers");

            migrationBuilder.DropColumn(
                name: "longitude",
                table: "offers");

            migrationBuilder.DropColumn(
                name: "postal_code",
                table: "offers");

            migrationBuilder.DropColumn(
                name: "published_at_utc",
                table: "offers");

            migrationBuilder.DropColumn(
                name: "street",
                table: "offers");

            migrationBuilder.Sql("UPDATE offers SET status = 'WITHDRAWN' WHERE status <> 'PUBLISHED';");
        }
    }
}
