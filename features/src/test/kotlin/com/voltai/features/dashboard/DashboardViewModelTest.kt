package com.voltai.features.dashboard

import com.voltai.domain.DomainConstants
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardViewModelTest {
    @Test
    fun domain_constants_are_accessible() {
        assertEquals(50, DomainConstants.CURRENT_WINDOW_SIZE)
    }
}