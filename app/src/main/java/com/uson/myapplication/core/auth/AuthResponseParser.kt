package com.uson.myapplication.core.auth

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import java.time.OffsetDateTime

internal fun parseLoginUrlResponse(rawBody: String): String {
    val normalized = rawBody.trim()
    authLogDebug("parseLoginUrlResponse raw=$normalized")
    if (normalized.isBlank()) error("서버 로그인 URL 응답이 비어 있어요")

    val json = normalized.toJsonElementOrNull()
    val directValue = json?.extractStringLikeValue()
    if (!directValue.isNullOrBlank() && directValue.startsWith("http")) return directValue

    val objectFields = json.asLooseObjectMap()
    return objectFields.stringValue("loginUrl", "login_url", "url")
        .ifBlank { normalized.removeSurrounding("\"") }
        .also { require(it.isNotBlank()) { "서버 로그인 URL 응답이 올바르지 않아요" } }
}

internal fun parseAuthSessionResponse(rawBody: String): AuthSession {
    val normalized = rawBody.trim()
    authLogDebug("parseAuthSessionResponse raw=$normalized")
    require(normalized.isNotBlank()) { "인증 응답이 비어 있어요" }

    val json = normalized.toJsonElementOrNull()
    val objectFields = json.asLooseObjectMap()
    val nestedFields = json.extractNestedJsonString().toJsonElementOrNull().asLooseObjectMap()
    val accessToken = objectFields.deepStringValue("accessToken", "access_token", "access")
        .ifBlank { nestedFields.deepStringValue("accessToken", "access_token", "access") }
        .ifBlank { normalized.findNamedValue("accessToken", "access_token", "access") }
        .ifBlank { normalized.findJwtLikeToken() }
    val refreshToken = objectFields.deepStringValue("refreshToken", "refresh_token", "refresh")
        .ifBlank { nestedFields.deepStringValue("refreshToken", "refresh_token", "refresh") }
        .ifBlank { normalized.findNamedValue("refreshToken", "refresh_token", "refresh") }

    authLogDebug(
        "parseAuthSessionResponse objectKeys=${objectFields.keys} nestedKeys=${nestedFields.keys} accessTokenPresent=${accessToken.isNotBlank()} refreshTokenPresent=${refreshToken.isNotBlank()}",
    )
    require(accessToken.isNotBlank()) { "Auth response does not include accessToken" }

    return AuthSession(
        accessToken = accessToken,
        refreshToken = refreshToken,
        accessTokenExpiresAtMillis = objectFields.deepExpiryMillis()
            ?: nestedFields.deepExpiryMillis()
            ?: defaultTokenExpiryMillis(),
        userId = resolveAuthenticatedUserId(
            explicitUserId = objectFields.deepStringValue("userId", "user_id", "memberId")
                .ifBlank { nestedFields.deepStringValue("userId", "user_id", "memberId") }
                .ifBlank { null },
            accessToken = accessToken,
        ),
    )
}

private fun String.toJsonElementOrNull(): JsonElement? = runCatching { JsonParser.parseString(this) }.getOrNull()

private fun JsonElement?.extractStringLikeValue(): String? = when {
    this == null || isJsonNull -> null
    isJsonPrimitive -> asJsonPrimitive.asString
    isJsonObject -> null
    isJsonArray && asJsonArray.size() == 1 -> asJsonArray.first().extractStringLikeValue()
    else -> null
}

private fun JsonElement?.extractNestedJsonString(): String = extractStringLikeValue()
    ?.takeIf { it.trim().startsWith("{") && it.trim().endsWith("}") }
    .orEmpty()

private fun JsonElement?.asLooseObjectMap(): Map<String, Any?> = when {
    this == null || isJsonNull -> emptyMap()
    isJsonObject -> asJsonObject.entrySet().associate { (key, value) -> key to value.toLooseValue() }
    else -> emptyMap()
}

private fun JsonElement.toLooseValue(): Any? = when {
    isJsonNull -> null
    isJsonPrimitive -> asJsonPrimitive.asString
    isJsonObject -> asJsonObject.entrySet().associate { (key, value) -> key to value.toLooseValue() }
    isJsonArray -> asJsonArray.map { it.toLooseValue() }
    else -> null
}

private fun Map<String, Any?>.stringValue(vararg keys: String): String =
    keys.firstNotNullOfOrNull { key -> this[key]?.toString()?.takeIf(String::isNotBlank) }.orEmpty()

private fun Map<String, Any?>.deepStringValue(vararg keys: String): String {
    stringValue(*keys).takeIf(String::isNotBlank)?.let { return it }

    values.forEach { value ->
        when (value) {
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                (value as? Map<String, Any?>)?.deepStringValue(*keys)?.takeIf(String::isNotBlank)?.let { return it }
            }
            is List<*> -> {
                value.forEach { item ->
                    @Suppress("UNCHECKED_CAST")
                    (item as? Map<String, Any?>)?.deepStringValue(*keys)?.takeIf(String::isNotBlank)?.let { return it }
                }
            }
        }
    }

    return ""
}

private fun Map<String, Any?>.deepExpiryMillis(): Long? {
    expiryMillis()?.let { return it }

    values.forEach { value ->
        when (value) {
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                (value as? Map<String, Any?>)?.deepExpiryMillis()?.let { return it }
            }
            is List<*> -> {
                value.forEach { item ->
                    @Suppress("UNCHECKED_CAST")
                    (item as? Map<String, Any?>)?.deepExpiryMillis()?.let { return it }
                }
            }
        }
    }

    return null
}

private fun Map<String, Any?>.expiryMillis(): Long? {
    val expiresAt = stringValue("accessTokenExpiresAt", "expiresAt", "expiredAt")
    if (expiresAt.isNotBlank()) {
        runCatching { return OffsetDateTime.parse(expiresAt).toInstant().toEpochMilli() }
    }

    val expiresInSeconds = sequenceOf("expiresIn", "expires_in", "accessTokenExpiresIn")
        .firstNotNullOfOrNull { key -> this[key]?.toString()?.toDoubleOrNull() }

    return if (expiresInSeconds != null) {
        System.currentTimeMillis() + (expiresInSeconds * 1000).toLong()
    } else {
        null
    }
}

private const val DefaultTokenTtlSeconds = 3600.0
private fun defaultTokenExpiryMillis(): Long =
    System.currentTimeMillis() + (DefaultTokenTtlSeconds * 1000).toLong()

private fun String.findNamedValue(vararg names: String): String {
    names.forEach { name ->
        val regex = Regex("""["']?$name["']?\s*[:=]\s*["']([^"']+)["']""")
        regex.find(this)?.groupValues?.getOrNull(1)?.takeIf(String::isNotBlank)?.let { return it }
    }
    return ""
}

private fun String.findJwtLikeToken(): String {
    val jwtRegex = Regex("""eyJ[a-zA-Z0-9_-]+\.[a-zA-Z0-9._-]+\.[a-zA-Z0-9._-]+""")
    return jwtRegex.find(this)?.value.orEmpty()
}

internal const val AuthDebugTag = "AuthDebug"
