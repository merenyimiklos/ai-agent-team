package hu.ugorjbe.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomerNavigationTest {
    @Test fun `four core destinations remain top-level and details do not`() {
        assertEquals(listOf("discover", "bookings", "favorites", "profile"), CustomerNavigation.topLevelRoutes)
        assertTrue(CustomerNavigation.isTopLevel("discover"))
        assertFalse(CustomerNavigation.isTopLevel("offer/id"))
    }
}
