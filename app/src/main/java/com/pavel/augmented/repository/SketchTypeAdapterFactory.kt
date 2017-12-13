package com.pavel.augmented.repository

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class SketchTypeAdapterFactory : TypeAdapterFactory {
    override fun <T : Any?> create(gson: Gson?, type: TypeToken<T>?): TypeAdapter<T>? {
        val rawType: Class<T> = type?.rawType as Class<T>
        if (rawType != List::class.java) {
            return null
        }

        val typedAdapter = gson?.getDelegateAdapter(this, type)

        return object : TypeAdapter<T>() {
            override fun read(`in`: JsonReader?): T {
                return typedAdapter?.read(`in`)!!
            }

            override fun write(out: JsonWriter?, value: T) {
                typedAdapter?.write(out, value)
            }
        }
    }
}