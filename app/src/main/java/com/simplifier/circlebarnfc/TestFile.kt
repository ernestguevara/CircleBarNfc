package com.simplifier.circlebarnfc

import java.math.BigInteger

fun main() {
    val byteArray: ByteArray = byteArrayOf(
        0x74.toByte(),
        0x68.toByte(),
        0x6F.toByte(),
        0x72.toByte(),
        0x00.toByte(),
        0x00.toByte(),
        0x00.toByte(),
        0x00.toByte(),
    )
//    val decimalInt = byteArrayToDecimal(byteArray)
//    val doubleVal = decimalToDoubleWithCents(decimalInt)
//
//    println("byte to int $decimalInt")
//    println("decimal to double $doubleVal")
//    println("decimal to double ${decimalToDoubleWithCents(123445)}")
//    println("decimal to double ${decimalToDoubleWithCents(52000)}")

    println("remove trailing byte ${hexByteArrayToString(byteArray)}")
}

fun byteArrayToDecimal(byteArray: ByteArray): Int {
    val firstFourBytes = byteArray.take(4).reversed().toByteArray()
    return BigInteger(1, firstFourBytes).toInt()
}

fun decimalToDoubleWithCents(decimalValue: Int): Double {
    val cents = decimalValue % 100
    val dollars = decimalValue / 100
    val doubleValue = dollars.toDouble() + cents.toDouble() / 100.0
    return String.format("%.2f", doubleValue).toDouble() // Format the double value to two decimal places
}

fun hexByteArrayToString(input: ByteArray): String {
    val lastIndex = input.indexOfLast { it != 0x00.toByte() } + 1
    val trimmedArray = input.copyOfRange(0, lastIndex)
    return String(trimmedArray)
}