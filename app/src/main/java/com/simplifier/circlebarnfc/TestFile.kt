package com.simplifier.circlebarnfc

import java.math.BigInteger

fun main() {
//    println("testValue expectedOutput: 0 input: 0.00 output: ${checkValues(transformValue("0.00".filter { it.isDigit() }))}")
//    println("testValue expectedOutput: 1 input: 0.01 output: ${checkValues(transformValue("0.01".filter { it.isDigit() }))}")
//    println("testValue expectedOutput: 12 input: 0.12 output: ${checkValues(transformValue("0.12".filter { it.isDigit() }))}")
//    println("testValue expectedOutput: 123 input: 1.23 output: ${checkValues(transformValue("1.23".filter { it.isDigit() }))}")
//    println("testValue expectedOutput: 1234 input: 12.34 output: ${checkValues(transformValue("12.34".filter { it.isDigit() }))}")
//    println("testValue expectedOutput: 12345 input: 123.45 output: ${checkValues(transformValue("123.45".filter { it.isDigit() }))}")
//    println("testValue expectedOutput: 123456 input: 1234.56 output: ${checkValues(transformValue("1234.56".filter { it.isDigit() }))}")
//    println("testValue expectedOutput: 1234567 input: 12345.67 output: ${checkValues(transformValue("12345.67".filter { it.isDigit() }))}")


//    println("transformValue 0 input: 0.00 output: ${transformValue("0.00".filter { it.isDigit() })}")
//    println("transformValue 1 input: 0.01 output: ${transformValue("0.01".filter { it.isDigit() })}")
//    println("transformValue 12 input: 0.12 output: ${transformValue("0.12".filter { it.isDigit() })}")
//    println("transformValue 123 input: 1.23 output: ${transformValue("1.23".filter { it.isDigit() })}")
//    println("transformValue 1234 input: 12.34 output: ${transformValue("12.34".filter { it.isDigit() })}")
//    println("transformValue 12345 input: 123.45 output: ${transformValue("123.45".filter { it.isDigit() })}")
//    println("transformValue 123456 input: 1234.56 output: ${transformValue("1234.56".filter { it.isDigit() })}")
//    println("transformValue 1234567 input: 12345.67 output: ${transformValue("12345.67".filter { it.isDigit() })}")
}

//fun transformValue(input: String): String {
//    if (input.isEmpty()) {
//        return "0.00"
//    }
//
//    val numericValue = input.toLongOrNull() ?: return "0.00"
//    val transformedValue = numericValue.toDouble() / 100
//
//    return "%.2f".format(transformedValue)
//}
//
//fun checkValues(newValue: String): Int {
//    val numericValue = newValue.toDoubleOrNull() ?: 0.0
//    return (numericValue * 100).toInt()
//}