package com.emm.mybest.core.coroutines

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AttemptTest {

    @Test
    fun `returns success with the block value`() = runTest {
        val result = attempt { 5 }

        assertEquals(Result.success(5), result)
    }

    @Test
    fun `returns success when the block suspends`() = runTest {
        val result = attempt {
            yield()
            "ok"
        }

        assertEquals(Result.success("ok"), result)
    }

    @Test
    fun `returns the thrown exception as failure`() = runTest {
        val error = IllegalStateException("disk full")

        val result = attempt<Unit> { throw error }

        assertSame(error, result.exceptionOrNull())
    }

    @Test
    fun `returns an Error as failure`() = runTest {
        val result = attempt<Unit> { throw NotImplementedError() }

        assertTrue(result.exceptionOrNull() is NotImplementedError)
    }

    @Test
    fun `rethrows cancellation and skips onFailure`() = runTest {
        val cancellation = CancellationException("stop")
        var hasFailureRun = false

        try {
            attempt<Unit> { throw cancellation }.onFailure { hasFailureRun = true }
            fail("expected CancellationException")
        } catch (thrown: CancellationException) {
            assertSame(cancellation, thrown)
        }

        assertFalse(hasFailureRun)
    }
}
