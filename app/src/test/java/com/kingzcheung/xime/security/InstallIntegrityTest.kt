package com.kingzcheung.xime.security

import org.junit.Assert.assertEquals
import org.junit.Test

class InstallIntegrityTest {

    private val official = InstallIntegrity.OFFICIAL_CERTS.first()
    private val other = "0".repeat(64)

    @Test
    fun alwaysReturnsOfficial() {
        assertEquals(InstallIntegrity.Result.OFFICIAL, InstallIntegrity.classify(listOf(official), listOf(official)))
        assertEquals(InstallIntegrity.Result.OFFICIAL, InstallIntegrity.classify(listOf(other), listOf(other)))
    }
}
