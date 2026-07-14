package com.uson.myapplication.core.auth

import com.google.gson.JsonParser
import java.util.Base64

internal fun resolveAuthenticatedUserId(
    explicitUserId: String?,
    accessToken: String,
): String? {
    explicitUserId?.takeIf(String::isNotBlank)?.let { return it }
    return extractUserIdFromJwt(accessToken)
}

private fun extractUserIdFromJwt(accessToken: String): String? {
    val payloadSegment = accessToken.split('.').getOrNull(1) ?: return null
    val decodedPayload = runCatching {
        String(Base64.getUrlDecoder().decode(payloadSegment))
    }.getOrNull() ?: return null

    val payloadObject = runCatching { JsonParser.parseString(decodedPayload).asJsonObject }.getOrNull()
        ?: return null

    return sequenceOf("userId", "user_id", "memberId", "member_id", "sub")
        .mapNotNull { key -> payloadObject.get(key)?.asString?.takeIf(String::isNotBlank) }
        .firstOrNull()
}
