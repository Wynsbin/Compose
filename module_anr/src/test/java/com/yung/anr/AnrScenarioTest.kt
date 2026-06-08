package com.yung.anr

import com.yung.anr.scenario.AnrScenario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnrScenarioTest {

    @Test
    fun allScenarios_haveUniqueTitles() {
        val titles = AnrScenario.entries.map { it.title }
        assertEquals(titles.size, titles.toSet().size)
    }

    @Test
    fun allScenarios_haveDescriptionAndAnrType() {
        AnrScenario.entries.forEach { scenario ->
            assertTrue(scenario.description.isNotBlank())
            assertTrue(scenario.anrType.isNotBlank())
        }
    }
}
