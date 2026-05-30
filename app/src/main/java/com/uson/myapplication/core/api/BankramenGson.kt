package com.uson.myapplication.core.api

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.uson.myapplication.generated.infrastructure.Serializer
import com.uson.myapplication.generated.model.BankramenLocalTime
import java.lang.reflect.Type
import java.time.LocalTime

object BankramenGson {
    val gsonBuilder: GsonBuilder by lazy {
        Serializer.gsonBuilder
            .registerTypeAdapter(BankramenLocalTime::class.java, BankramenLocalTimeJsonAdapter())
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
