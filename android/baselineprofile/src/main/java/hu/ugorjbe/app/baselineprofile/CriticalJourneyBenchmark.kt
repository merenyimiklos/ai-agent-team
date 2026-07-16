package hu.ugorjbe.app.baselineprofile

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val PackageName = "hu.ugorjbe.app"
private const val WaitMs = 20_000L

@RunWith(AndroidJUnit4::class)
class CriticalJourneyBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun exploreListScroll() = benchmarkRule.measureRepeated(
        packageName = PackageName,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(),
        startupMode = StartupMode.WARM,
        iterations = 5,
        setupBlock = {
            launchAuthenticatedApp()
            openList()
        },
    ) {
        device.wait(Until.hasObject(By.res(PackageName, "explore_list")), WaitMs)
        val width = device.displayWidth
        val height = device.displayHeight
        device.swipe(width / 2, height * 4 / 5, width / 2, height / 4, 24)
        device.waitForIdle()
        device.swipe(width / 2, height / 3, width / 2, height * 4 / 5, 24)
        device.waitForIdle()
    }

    @Test
    fun mapListSwitch() = benchmarkRule.measureRepeated(
        packageName = PackageName,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(),
        startupMode = StartupMode.WARM,
        iterations = 5,
        setupBlock = { launchAuthenticatedApp() },
    ) {
        clickRequiredResource("explore_presentation_switch")
        device.wait(Until.hasObject(By.res(PackageName, "explore_list")), WaitMs)
        clickRequiredResource("explore_presentation_switch")
        device.wait(Until.hasObject(By.res(PackageName, "explore_map")), WaitMs)
    }

    @Test
    fun offerDetailOpening() = benchmarkRule.measureRepeated(
        packageName = PackageName,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(),
        startupMode = StartupMode.WARM,
        iterations = 5,
        setupBlock = {
            launchAuthenticatedApp()
            openList()
        },
    ) {
        val offer = device.wait(
            Until.findObject(By.textContains("Délutáni agyagozás")),
            WaitMs,
        ) ?: error("The seeded offer was not visible in the list.")
        offer.click()
        device.wait(Until.hasObject(By.textContains("Foglal")), WaitMs)
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.launchAuthenticatedApp() {
        pressHome()
        device.executeShellCommand("pm clear $PackageName")
        startActivityAndWait()
        device.waitForIdle()

        val email = device.wait(Until.findObject(By.res(PackageName, "auth_email")), WaitMs)
        if (email != null) {
            email.text = "demo@ugorjbe.local"
            device.findObject(By.res(PackageName, "auth_password"))
                ?.apply { text = "UgorjBe123!" }
                ?: error("Password field not found")
            clickRequiredResource("auth_submit")
        }
        device.wait(Until.hasObject(By.res(PackageName, "nav_discover")), WaitMs)
        device.wait(Until.hasObject(By.res(PackageName, "explore_presentation_switch")), WaitMs)
    }

    private fun MacrobenchmarkScope.openList() {
        if (!device.hasObject(By.res(PackageName, "explore_list"))) {
            clickRequiredResource("explore_presentation_switch")
        }
        device.wait(Until.hasObject(By.res(PackageName, "explore_list")), WaitMs)
    }

    private fun MacrobenchmarkScope.clickRequiredResource(resourceName: String) {
        val objectToClick = device.wait(
            Until.findObject(By.res(PackageName, resourceName)),
            WaitMs,
        ) ?: error("Required resource not found: $resourceName")
        objectToClick.click()
        device.waitForIdle()
    }
}
