#!/usr/bin/env kotlin

import java.io.File

fun main() {
    val inputFile = args.getOrNull(0) ?: "input.txt"

    val races1 = parseRacesFromFile(inputFile, ::part1Parser)
    val races2 = parseRacesFromFile(inputFile, ::part2Parser)

    println(races1.map(::countWinningAttempts).product())
    println(races2.map(::countWinningAttempts).product())
}

data class Race(val time: Long, val recordDistance: Long)

data class RaceAttempt(val buttonTime: Long, val travelTime: Long) {

    fun wins(race: Race): Boolean {
        val speed = buttonTime
        val distanceTraveled = speed * travelTime
        return distanceTraveled > race.recordDistance
    }

}

fun countWinningAttempts(race: Race) = generateAttempts(race).count { attempt -> attempt.wins(race) }

fun generateAttempts(race: Race) = (0..race.time).map {
    RaceAttempt(buttonTime = it, travelTime = race.time - it)
}

fun parseRacesFromFile(inputFile: String, parseNumbers: (String) -> List<Long>): List<Race> {
    val lines = File(inputFile).readLines().filter { it.isNotBlank() }
    val times = parseNumbers(lines[0])
    val recordDistances = parseNumbers(lines[1])
    return times.zip(recordDistances).map { (time, recordDistance) -> Race(time, recordDistance) }
}

fun part1Parser(line: String): List<Long> {
    return line.substringAfter(":")
        .split("\\s+".toRegex())
        .filter { it.isNotBlank() }
        .map { it.toLong() }
}

fun part2Parser(line: String): List<Long> {
    return line.substringAfter(":")
        .filter { it != ' ' }
        .toLong()
        .let { listOf(it) }
}

fun List<Int>.product() = this.fold(initial = 1) { a, b -> a * b}

main()
