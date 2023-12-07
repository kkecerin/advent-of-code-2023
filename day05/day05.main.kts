#!/usr/bin/env kotlin

import java.io.File
import java.lang.RuntimeException

val DEBUG = false

fun main() {
    val inputFile = args.getOrNull(0) ?: "input.txt"
    val lines = File(inputFile).readLines().filter { it.isNotBlank() }

    val (seedValues, itemConverters) = parseFile(lines)
    val seeds = seedValues.map { value -> Item(Category.SEED, value) }

    val conversionSequencesForSeeds = seeds.map { seed ->
        generateConversionSequence(
            start = seed,
            to = Category.LOCATION,
            itemConverters = itemConverters
        )
    }

    if (DEBUG) {
        for (conversionSequence in conversionSequencesForSeeds) {
            println(conversionSequence.joinToString(" -> "))
        }
    }

    val locations = conversionSequencesForSeeds.map { it.last() }
    val locationValues = locations.map { it.value }
    println(locationValues.min())
}

fun generateConversionSequence(
    start: Item, itemConverters: List<ItemConverter>, to: Category
) = generateSequence(start) { currentItem ->
    itemConverters.findConverterFor(currentItem)?.let { it.convert(currentItem) }
}

enum class Category {
    SEED,
    SOIL,
    FERTILIZER,
    WATER,
    LIGHT,
    TEMPERATURE,
    HUMIDITY,
    LOCATION
}

data class Item(val category: Category, val value: Long) {
    override fun toString() = "${category}(${value})"
}

data class CategoryMapping(val from: Category, val to: Category) {
    fun canMap(category: Category) = from === category
}

data class ValueConverter(
    val dstRangeStart: Long,
    val srcRangeStart: Long,
    val rangeLen: Long
) {
    fun canConvertValue(value: Long) = value in srcRange

    fun convertValue(value: Long) = dstRangeStart + (value - srcRangeStart)

    private val srcRange = buildRange(srcRangeStart, rangeLen)
}

class ItemConverter(
    val categoryMapping: CategoryMapping,
    val valueConverters: MutableList<ValueConverter> = mutableListOf()
) {
    fun canConvert(item: Item) = categoryMapping.canMap(item.category)

    fun convert(item: Item) = Item(
        category = categoryMapping.to,
        value = convertValue(item.value)
    )
    private fun convertValue(value: Long): Long {
        val valueConverter = valueConverters.firstOrNull { it.canConvertValue(value) }
        return valueConverter?.convertValue(value) ?: value
    }
}

fun List<ItemConverter>.findConverterFor(item: Item) = firstOrNull() { it.canConvert(item) }

enum class LineType {
    SEED_DEFINITION,
    CATEGORY_MAPPING_HEADER,
    VALUE_CONVERSION
}

fun getLineType(line: String) = when {
    line.startsWith("seeds") -> LineType.SEED_DEFINITION
    line in headerToCategoryMapping.keys -> LineType.CATEGORY_MAPPING_HEADER
    line[0].isDigit() -> LineType.VALUE_CONVERSION
    else -> throw RuntimeException("Unknown line type: '${line}'")
}

val headerToCategoryMapping = mapOf(
    "seed-to-soil map:" to CategoryMapping(from = Category.SEED, to = Category.SOIL),
    "soil-to-fertilizer map:" to CategoryMapping(from = Category.SOIL, to = Category.FERTILIZER),
    "fertilizer-to-water map:" to CategoryMapping(from = Category.FERTILIZER, to = Category.WATER),
    "water-to-light map:" to CategoryMapping(from = Category.WATER, to = Category.LIGHT),
    "light-to-temperature map:" to CategoryMapping(from = Category.LIGHT, to = Category.TEMPERATURE),
    "temperature-to-humidity map:" to CategoryMapping(from = Category.TEMPERATURE, to = Category.HUMIDITY),
    "humidity-to-location map:" to CategoryMapping(from = Category.HUMIDITY, to = Category.LOCATION),
)

data class ParsedFile(val seedValues: Sequence<Long>, val itemConverters: List<ItemConverter>)

fun parseFile(lines: List<String>): ParsedFile {
    val seedValues = lines[0].substringAfter(": ").split(" ").map { it.toLong() }
        .windowed(size = 2, step = 2)
        .asSequence()
        .map { (rangeStart, rangeLen) -> buildRange(rangeStart, rangeLen).asSequence() }
        .flatten()

    val conversionSectionLines = lines.drop(1).filter { it.isNotBlank() }
    val converters = parseConversionSections(conversionSectionLines)

    return ParsedFile(seedValues, converters)
}

fun parseConversionSections(lines: List<String>): List<ItemConverter> {

    val itemConverters = mutableListOf<ItemConverter>()

    for (line in lines) {
        val lineType = getLineType(line)
        when (lineType) {
            LineType.CATEGORY_MAPPING_HEADER -> {
                val mapping = headerToCategoryMapping.getValue(line)
                itemConverters.add(ItemConverter(mapping))
            }
            LineType.VALUE_CONVERSION -> {
                val valueConverter = parseValueConversion(line)
                itemConverters.last().valueConverters.add(valueConverter)
            }
            else -> throw RuntimeException("Unexpected line type ${lineType}: '${line}'")
        }
    }

    return itemConverters
}

fun parseValueConversion(valueConversionLine: String): ValueConverter {
    val parts = valueConversionLine.split(" ").map { it.toLong() }
    return ValueConverter(dstRangeStart = parts[0], srcRangeStart = parts[1], rangeLen = parts[2])
}

fun buildRange(start: Long, len: Long) = start until start + len

main()
