package com.uson.myapplication.core.api

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.TypeAdapter
import com.uson.myapplication.generated.infrastructure.Serializer
import com.uson.myapplication.generated.model.BankramenLocalTime
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

object BankramenGson {
    val gsonBuilder: GsonBuilder by lazy {
        Serializer.gsonBuilder
            .registerTypeAdapter(BankramenLocalTime::class.java, BankramenLocalTimeJsonAdapter())
            .registerTypeAdapter(OffsetDateTime::class.java, LenientOffsetDateTimeAdapter())
    }
}

private class LenientOffsetDateTimeAdapter : TypeAdapter<OffsetDateTime?>() {
    override fun write(out: JsonWriter, value: OffsetDateTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value))
        }
    }

    override fun read(out: JsonReader): OffsetDateTime? {
        if (out.peek() == JsonToken.NULL) {
            out.nextNull()
            return null
        }

        val value = out.nextString().takeIf(String::isNotBlank) ?: return null
        return runCatching { OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME) }
            .recoverCatching {
                LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .atZone(ZoneId.systemDefault())
                    .toOffsetDateTime()
            }
            .getOrThrow()
    }
}

private class BankramenLocalTimeJsonAdapter : JsonDeserializer<BankramenLocalTime?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): BankramenLocalTime? {
        if (json == null || json.isJsonNull) return null

        if (json.isJsonPrimitive) {
            val value = json.asString.takeIf(String::isNotBlank) ?: return null
            val parsed = LocalTime.parse(value)
            return BankramenLocalTime(
                hour = parsed.hour,
                minute = parsed.minute,
                second = parsed.second,
                nano = parsed.nano,
            )
        }

        val jsonObject = json.asJsonObject
        return BankramenLocalTime(
            hour = jsonObject.get("hour")?.takeUnless(JsonElement::isJsonNull)?.asInt,
            minute = jsonObject.get("minute")?.takeUnless(JsonElement::isJsonNull)?.asInt,
            second = jsonObject.get("second")?.takeUnless(JsonElement::isJsonNull)?.asInt,
            nano = jsonObject.get("nano")?.takeUnless(JsonElement::isJsonNull)?.asInt,
        )
    }
}
