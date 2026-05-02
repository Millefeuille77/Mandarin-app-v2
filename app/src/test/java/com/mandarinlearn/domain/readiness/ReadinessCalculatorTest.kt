// ReadinessCalculatorTest.kt — Mandarin Learn
// Unit tests for ReadinessCalculator.
// IMPLEMENTATION_PLAN.md Phase 8 §E: formula + edge cases.

package com.mandarinlearn.domain.readiness

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for [ReadinessCalculator].
 *
 * Formula: 0.4 * vocab + 0.2 * reading + 0.4 * exam, clamped to 0..100.
 * All input percentages are 0..100 (not fractions).
 */
class ReadinessCalculatorTest {

    // Tolerance for floating-point comparisons.
    private val delta = 0.01f

    // ---- Basic formula correctness ----

    @Test
    fun `all zeros returns zero`() {
        val result = ReadinessCalculator.calculate(0f, 0f, 0f)
        assertEquals(0f, result, delta)
    }

    @Test
    fun `all 100 returns 100`() {
        val result = ReadinessCalculator.calculate(100f, 100f, 100f)
        assertEquals(100f, result, delta)
    }

    @Test
    fun `equal weights sum correctly at 50 percent each`() {
        // 0.4*50 + 0.2*50 + 0.4*50 = 20 + 10 + 20 = 50
        val result = ReadinessCalculator.calculate(50f, 50f, 50f)
        assertEquals(50f, result, delta)
    }

    @Test
    fun `vocab only contributes 40 percent of total`() {
        // 0.4*100 + 0.2*0 + 0.4*0 = 40
        val result = ReadinessCalculator.calculate(100f, 0f, 0f)
        assertEquals(40f, result, delta)
    }

    @Test
    fun `reading only contributes 20 percent of total`() {
        // 0.4*0 + 0.2*100 + 0.4*0 = 20
        val result = ReadinessCalculator.calculate(0f, 100f, 0f)
        assertEquals(20f, result, delta)
    }

    @Test
    fun `exam only contributes 40 percent of total`() {
        // 0.4*0 + 0.2*0 + 0.4*100 = 40
        val result = ReadinessCalculator.calculate(0f, 0f, 100f)
        assertEquals(40f, result, delta)
    }

    @Test
    fun `fractional inputs produce correct weighted result`() {
        // 0.4*60 + 0.2*80 + 0.4*70 = 24 + 16 + 28 = 68
        val result = ReadinessCalculator.calculate(60f, 80f, 70f)
        assertEquals(68f, result, delta)
    }

    @Test
    fun `asymmetric inputs compute correctly`() {
        // 0.4*100 + 0.2*0 + 0.4*80 = 40 + 0 + 32 = 72
        val result = ReadinessCalculator.calculate(100f, 0f, 80f)
        assertEquals(72f, result, delta)
    }

    // ---- Clamping edge cases ----

    @Test
    fun `negative inputs clamped to zero`() {
        val result = ReadinessCalculator.calculate(-10f, -5f, -20f)
        assertEquals(0f, result, delta)
    }

    @Test
    fun `inputs above 100 clamped to 100`() {
        // All clamped to 100 first, then formula = 0.4*100 + 0.2*100 + 0.4*100 = 100
        val result = ReadinessCalculator.calculate(120f, 150f, 200f)
        assertEquals(100f, result, delta)
    }

    @Test
    fun `partially over-range inputs clamped individually`() {
        // vocab=150 → clamped to 100; reading=0; exam=50
        // 0.4*100 + 0.2*0 + 0.4*50 = 40 + 0 + 20 = 60
        val result = ReadinessCalculator.calculate(150f, 0f, 50f)
        assertEquals(60f, result, delta)
    }

    @Test
    fun `single negative input does not underflow`() {
        // vocab=-50 → clamped to 0; reading=100; exam=100
        // 0.4*0 + 0.2*100 + 0.4*100 = 0 + 20 + 40 = 60
        val result = ReadinessCalculator.calculate(-50f, 100f, 100f)
        assertEquals(60f, result, delta)
    }

    // ---- Boundary values ----

    @Test
    fun `exactly at 100 boundary is not clamped`() {
        val result = ReadinessCalculator.calculate(100f, 100f, 100f)
        assertEquals(100f, result, delta)
    }

    @Test
    fun `exactly at 0 boundary is not clamped`() {
        val result = ReadinessCalculator.calculate(0f, 0f, 0f)
        assertEquals(0f, result, delta)
    }
}
