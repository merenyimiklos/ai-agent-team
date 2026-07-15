package hu.ugorjbe.app.baselineprofile

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldStartupWithBaselineProfile() = benchmarkRule.measureRepeated(
        packageName = "hu.ugorjbe.app",
        metrics = listOf(StartupTimingMetric(), FrameTimingMetric()),
        compilationMode = CompilationMode.Partial(
            baselineProfileMode = CompilationMode.Partial.BaselineProfileMode.Require,
        ),
        startupMode = StartupMode.COLD,
        iterations = 5,
        setupBlock = MacrobenchmarkScope::pressHome,
    ) {
        startActivityAndWait()
        device.waitForIdle()
    }
}
