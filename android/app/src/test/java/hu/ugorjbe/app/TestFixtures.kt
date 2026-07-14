package hu.ugorjbe.app

import hu.ugorjbe.app.domain.Address
import hu.ugorjbe.app.domain.Booking
import hu.ugorjbe.app.domain.BookingOffer
import hu.ugorjbe.app.domain.Money
import hu.ugorjbe.app.domain.OfferDetail
import hu.ugorjbe.app.domain.OfferSummary
import hu.ugorjbe.app.domain.ProviderSummary
import java.math.BigDecimal

val testAddress = Address("1137", "Budapest", "Pozsonyi út 12.", "HU", BigDecimal("47.5"), BigDecimal("19.0"))
val testProvider = ProviderSummary("provider", "Kerek Erdő", "Családi műhely", testAddress, null)
val testMoney = Money(BigDecimal("3200.00"), "HUF")
val testOfferSummary = OfferSummary(
    "offer", testProvider, "Agyagozás", "WORKSHOP", "2026-07-14T14:00:00Z",
    "2026-07-14T15:00:00Z", 5, 10, Money(BigDecimal("4800"), "HUF"),
    testMoney, 33, 4, null, null,
)
val testOfferDetail = OfferDetail(
    "offer", testProvider, "Agyagozás", "Leírás", "WORKSHOP", "2026-07-14T14:00:00Z",
    "2026-07-14T15:00:00Z", "2026-07-14T13:30:00Z", "2026-07-14T12:00:00Z",
    5, 10, true, null, Money(BigDecimal("4800"), "HUF"), testMoney, 33, 10, 4,
    true, null, "PAY_ON_ARRIVAL", null, null,
)
val testBooking = Booking(
    "booking", "CONFIRMED", 2, testMoney, Money(BigDecimal("6400"), "HUF"),
    "PAY_ON_ARRIVAL", "UGB-7K3M9Q", "ugorjbe://booking/booking?code=UGB-7K3M9Q",
    "2026-07-14T10:00:00Z", null, true, "2026-07-14T12:00:00Z",
    BookingOffer("offer", "Agyagozás", "WORKSHOP", "provider", "Kerek Erdő", "2026-07-14T14:00:00Z", "2026-07-14T15:00:00Z", testAddress, null),
)
