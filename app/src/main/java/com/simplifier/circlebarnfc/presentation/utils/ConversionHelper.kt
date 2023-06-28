package com.simplifier.circlebarnfc.presentation.utils

import java.math.BigInteger
import kotlin.experimental.xor

object ConversionHelper {

    //key section
    fun getStaffKeyA(tagId: ByteArray): ByteArray {
        return xorByteArrays(getStaffHexA(tagId), PRIM20)
    }

    private fun getStaffHexA(tagId: ByteArray): ByteArray {

        require(tagId.size >= 4) { "The tagId byte array must have at least 2 elements" }

        val result = ByteArray(tagId.size + 2)
        System.arraycopy(tagId, 0, result, 0, tagId.size)
        result[tagId.size] = tagId[0]
        result[tagId.size + 1] = tagId[1]

        return result
    }

    fun getStaffKeyB(tagId: ByteArray): ByteArray {
        return xorByteArrays(getStaffHexB(tagId), PRIM20)
    }

    private fun getStaffHexB(tagId: ByteArray): ByteArray {
        require(tagId.size >= 4) { "The tagId byte array must have at least 4 elements" }

        val result = ByteArray(tagId.size + 2)
        for (i in tagId.indices) {
            result[i] = tagId[tagId.size - 1 - i]
        }
        result[tagId.size] = tagId[3]
        result[tagId.size + 1] = tagId[2]

        return result
    }

    private fun xorByteArrays(byteOne: ByteArray, byteTwo: ByteArray): ByteArray {
        require(byteOne.size == byteTwo.size) { "Byte arrays must have the same length" }

        val result = ByteArray(byteOne.size)

        for (i in byteOne.indices) {
            result[i] = (byteOne[i] xor byteTwo[i])
        }

        return result
    }

    private val PRIM20 = byteArrayOf(
        0x50.toByte(),
        0x52.toByte(),
        0x49.toByte(),
        0x4D.toByte(),
        0x32.toByte(),
        0x30.toByte()
    )
    //end key section

    //bytes conversion
    fun bytesToHexString(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = "0123456789ABCDEF"[v ushr 4]
            hexChars[i * 2 + 1] = "0123456789ABCDEF"[v and 0x0F]
        }
        return String(hexChars)
    }

    fun bytesToHexStringWithSpace(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 3) // Adjusted size to include spaces
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 3] = "0123456789ABCDEF"[v ushr 4]
            hexChars[i * 3 + 1] = "0123456789ABCDEF"[v and 0x0F]
            hexChars[i * 3 + 2] = ' ' // Add space after each hex byte
        }
        return String(hexChars).trim() // Remove trailing space
    }

    fun hexByteArrayToDecimal(byteArray: ByteArray, byteCount: Int = 4): Int {
        val firstFourBytes = byteArray.take(byteCount).reversed().toByteArray()
        return BigInteger(1, firstFourBytes).toInt()
    }

    fun hexByteArrayToString(input: ByteArray): String {
        val lastIndex = input.indexOfLast { it != 0x00.toByte() } + 1
        val trimmedArray = input.copyOfRange(0, lastIndex)
        return String(trimmedArray)
    }
    //end bytes

    //string conversion
    fun stringToHexByteArray(input: String): ByteArray {
        return BigInteger(1, input.toByteArray()).toByteArray()
    }

    fun fillByteArray(byteArray: ByteArray): ByteArray {
        require(byteArray.size <= 16) { "Input byte array must be 16 bytes or less." }

        val result = ByteArray(16)
        System.arraycopy(byteArray, 0, result, 0, byteArray.size)

        return result
    }

    //end string

    //decimal
    fun decimalToDoubleWithCents(decimalValue: Int): Double {
        val cents = decimalValue % 100
        val dollars = decimalValue / 100
        val doubleValue = dollars.toDouble() + cents.toDouble() / 100.0
        return String.format("%.2f", doubleValue).toDouble() // Format the double value to two decimal places
    }
}