package com.uson.myapplication.core.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthUserIdResolverTest {
    @Test
    fun `returns explicit userId when present`() {
        val resolved = resolveAuthenticatedUserId(
            explicitUserId = "explicit-user-id",
            accessToken = "ignored.token.value",
        )

        assertEquals("explicit-user-id", resolved)
    }

    @Test
    fun `extracts sub from jwt payload when explicit userId missing`() {
        val jwt = "header.${base64Url("""{"sub":"550e8400-e29b-41d4-a716-446655440000"}""")}.signature"

        val resolved = resolveAuthenticatedUserId(
            explicitUserId = null,
            accessToken = jwt,
        )

        assertEquals("550e8400-e29b-41d4-a716-446655440000", resolved)
    }

    @Test
    fun `returns null when token cannot be decoded`() {
        val resolved = resolveAuthenticatedUserId(
            explicitUserId = null,
            accessToken = "not-a-jwt",
        )

        assertNull(resolved)
    }
}

private fun base64Url(value: String): String =
    java.util.Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.toByteArray())
