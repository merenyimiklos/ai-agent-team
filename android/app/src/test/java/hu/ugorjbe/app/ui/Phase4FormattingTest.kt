package hu.ugorjbe.app.ui

import hu.ugorjbe.app.domain.Money
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class Phase4FormattingTest {
    @Test
    fun compactMoneyUsesShortHungarianForintLabel() {
        assertEquals(
            "3900 Ft",
            compactMoney(Money(BigDecimal("3900.00"), "HUF")),
        )
    }

    @Test
    fun compactMoneyKeepsForeignCurrencyCode() {
        assertEquals(
            "12.5 EUR",
            compactMoney(Money(BigDecimal("12.50"), "EUR")),
        )
    }
}
