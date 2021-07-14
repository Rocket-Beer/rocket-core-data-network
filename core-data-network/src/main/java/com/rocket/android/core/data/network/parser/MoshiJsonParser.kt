package com.rocket.android.core.data.network.parser

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class MoshiJsonParser : JsonParser {
    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    override fun <T> fromJson(json: String, type: Class<T>): T? =
        moshi.adapter(type).lenient().fromJson(json)

    override fun <T : Any> toJson(value: T): String =
        moshi.adapter<T>(value::class.java).toJson(value)
}
