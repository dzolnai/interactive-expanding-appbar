@file:JvmName("ObfuscatorUtils")

package com.egeniq.interactiveexpandingappbar.util

import android.util.Base64
import java.io.UnsupportedEncodingException
import kotlin.experimental.and
import kotlin.experimental.inv

/**
 * A helper class for obfuscating and deobfuscating important data.
 */
object Obfuscator {
    internal const val OFFSET: Byte = -17

    /**
     * With this method you can test if the obfuscator truly works on any string.
     *
     * @param input The string to test.
     * @return True if the obfuscator works.
     */
    fun test(input: String): Boolean {
        val probe = input.obfuscate().deObfuscate()
        return input.trim() == probe.trim()
    }

    @JvmStatic
    fun obfuscate(value: String) = value.obfuscate()

    @JvmStatic
    fun deObfuscate(value: String) = value.deObfuscate()
}

/**
 * Obfuscates the input into an unidentifiable text.
 */
fun String.obfuscate(): String {
    // create bytes from the string
    val bytes: ByteArray = try {
        this.toByteArray(Charsets.UTF_8)
    } catch (ex: UnsupportedEncodingException) {
        this.toByteArray()
    }

    // offset
    val offsetted = ByteArray(bytes.size)
    bytes.forEachIndexed { index, current ->
        if (current + Obfuscator.OFFSET < 0) {
            offsetted[index] = (0xff + (current + Obfuscator.OFFSET)).toByte()
        } else {
            offsetted[index] = (current + Obfuscator.OFFSET).toByte()
        }
    }

    // byte value and order invert
    val unordered = ByteArray(offsetted.size)
    offsetted.forEachIndexed { index, _ ->
        unordered[unordered.size - index - 1] = (offsetted[index].inv() and 0xff.toByte())
    }

    // base64 encode
    val result = Base64.encode(unordered, Base64.DEFAULT)

    return try {
        String(result, Charsets.UTF_8)
    } catch (ex: UnsupportedEncodingException) {
        String(result)
    }
}

/**
 * Deobfuscates the string using our own methods
 */
fun String.deObfuscate(): String {
    // Input should be first Base64 decoded.
    val base64Decoded = Base64.decode(this, Base64.DEFAULT)
    // Bytes are inverted in value and also order
    val ordered = ByteArray(base64Decoded.size)
    ordered.forEachIndexed { index, _ ->
        ordered[base64Decoded.size - index - 1] = (base64Decoded[index].inv() and 0xff.toByte())
    }

    // they also have an offset
    val result = ByteArray(ordered.size)
    ordered.forEachIndexed { index, current ->
        if (current - Obfuscator.OFFSET > 0xff) {
            result[index] = (current - Obfuscator.OFFSET - 0xff).toByte()
        } else {
            result[index] = (current - Obfuscator.OFFSET).toByte()
        }
    }

    return try {
        String(result, Charsets.UTF_8)
    } catch (ex: UnsupportedEncodingException) {
        String(result)
    }
}