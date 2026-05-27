package com.uson.myapplication.core.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TemporaryAuthSessionFactoryTest {
    @Test
    fun `create returns long-lived temporary auth session`() {
        val nowMillis = 1_000L

        val session = TemporaryAuthSessionFactory.create(nowMillis = nowMillis)

        assertTrue(session.accessToken.startsWith("debug-access-"))
        assertTrue(session.refreshToken.startsWith("debug-refresh-"))
        assertEquals("debug-kakao-user", session.userId)
        assertEquals(nowMillis + 30L * 24L * 60L * 60L * 1000L, session.accessTokenExpiresAtMillis)
    }
}
