package com.emmanuel.go4lunch.utils

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.reflect.Type

class Parser {

    companion object {
        private val gson = Gson()

        fun <T> parseJsonFile(type: Type, fileName: String): T {
            val json = parseJsonFile(fileName)
            return gson.fromJson(json, type)
        }

        private fun parseJsonFile(resourceName: String): JsonElement {
            val jsonElement: JsonElement
            try {
                val jsonString = readAsString(resourceName)
                jsonElement = convertStringToJson(jsonString)
            } catch (ioe: Exception) {
                throw RuntimeException("Parse failed", ioe)
            }

            return jsonElement
        }

        @Throws(IOException::class) fun readAsString(resourceName: String): String {
            try {
                val `in` = javaClass.classLoader!!.getResourceAsStream(resourceName)
                val outputStream = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var length = `in`.read(buffer)
                while (length != -1) {
                    outputStream.write(buffer, 0, length)
                    length = `in`.read(buffer)
                }
                `in`.close()
                return outputStream.toString()
            } catch (ioe: IOException) {
                throw AssertionError("Failed loading resource " + resourceName + " from " + Parser::class.java)
            }
        }

        @Throws(IOException::class) private fun convertStringToJson(jsonString: String): JsonElement {
            return JsonParser.parseString(jsonString)
        }
    }
}