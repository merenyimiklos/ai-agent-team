package hu.ugorjbe.app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import hu.ugorjbe.app.domain.Address
import hu.ugorjbe.app.domain.Money
import hu.ugorjbe.app.domain.OfferSummary
import hu.ugorjbe.app.domain.ProviderSummary
import hu.ugorjbe.app.ui.theme.UgorjBeTheme
import java.math.BigDecimal
import org.junit.Rule
import org.junit.Test

class Phase3OfferCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun offerCardShowsCriticalBookingInformationWithoutRemoteImage() {
        composeRule.setContent {
            UgorjBeTheme {
                OfferCard(offer = fixture, onClick = {})
            }
        }

        composeRule.onNodeWithText("Délutáni agyagozás").assertIsDisplayed()
        composeRule.onNodeWithText("3 900 Ft").assertIsDisplayed()
        composeRule.onNodeWithText("8 hely maradt").assertIsDisplayed()
    }
}

private val fixtureAddress = Address(
    postalCode = "1075",
    city = "Budapest",
    street = "Király utca 10.",
    countryCode = "HU",
    latitude = BigDecimal("47.4991"),
    longitude = BigDecimal("19.0577"),
)

private val fixtureProvider = ProviderSummary(
    id = "22222222-2222-2222-2222-222222222222",
    name = "Városi Alkotóműhely",
    shortDescription = "Kreatív programok családoknak",
    address = fixtureAddress,
    imageUrl = null,
)

private val fixture = OfferSummary(
    id = "33333333-3333-3333-3333-333333333333",
    provider = fixtureProvider,
    title = "Délutáni agyagozás",
    category = "WORKSHOP",
    startsAtUtc = "2026-07-15T15:00:00Z",
    endsAtUtc = "2026-07-15T16:30:00Z",
    minChildAge = 5,
    maxChildAge = 12,
    originalUnitPrice = Money(BigDecimal("6000"), "HUF"),
    discountedUnitPrice = Money(BigDecimal("3900"), "HUF"),
    discountPercent = 35,
    availablePlaces = 8,
    distanceKm = BigDecimal("1.4"),
    imageUrl = null,
    address = fixtureAddress,
)
