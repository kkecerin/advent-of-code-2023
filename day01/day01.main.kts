#!/usr/bin/env kotlin

import java.io.File

data class TxtDigit(val digit: Char, val txt: String)

val txtDigits = listOf(
    TxtDigit(digit = '1', txt = "1"),
    TxtDigit(digit = '1', txt = "one"),
    TxtDigit(digit = '2', txt = "2"),
    TxtDigit(digit = '2', txt = "two"),
    TxtDigit(digit = '3', txt = "3"),
    TxtDigit(digit = '3', txt = "three"),
    TxtDigit(digit = '4', txt = "4"),
    TxtDigit(digit = '4', txt = "four"),
    TxtDigit(digit = '5', txt = "5"),
    TxtDigit(digit = '5', txt = "five"),
    TxtDigit(digit = '6', txt = "6"),
    TxtDigit(digit = '6', txt = "six"),
    TxtDigit(digit = '7', txt = "7"),
    TxtDigit(digit = '7', txt = "seven"),
    TxtDigit(digit = '8', txt = "8"),
    TxtDigit(digit = '8', txt = "eight"),
    TxtDigit(digit = '9', txt = "9"),
    TxtDigit(digit = '9', txt = "nine")
)

fun getDigits(str: String): List<Char> {

    val digits = mutableListOf<Char>()

    for (i in 0 until str.length) {
        val subStr = str.drop(i)
        val txtDigitAtStart = txtDigits.firstOrNull { subStr.startsWith(it.txt) }
        if (txtDigitAtStart !== null) {
            digits.add(txtDigitAtStart.digit)
        }
    }

    return digits
}

fun parseNumber(line: String): Int {
    val digits = getDigits(line)
    var first = digits.first()
    var last = digits.last()
    return Integer.parseInt(first.toString() + last.toString())
}

val inputFile = args.getOrNull(0) ?: "input.txt"
val lines = File(inputFile).readLines().filter { it.isNotBlank() }

val numbers = lines.map(::parseNumber)
println(numbers.sum())