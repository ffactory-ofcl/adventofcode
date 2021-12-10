import java.io.File
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.pow

object Year2021 {

  object Day1 {
    private val input = File("input/2021/1")

    fun part1() {
      var counter = 0
      var last = Integer.MAX_VALUE
      input.readLines().forEach { line ->
        val value = Integer.parseInt(line)
        if (value > last) {
          counter += 1
        }
        last = value
      }

      println(counter)
    }

    fun part2() {
      var counter = 0

      val lines = input.readLines()
      val lastWindowIndices = listOf(-2, -1, 0)
      val currentWindowIndices = listOf(-1, 0, 1)

      (2..(lines.count() - 2)).forEach { index ->
        val lastWindow = lastWindowIndices.map { Integer.parseInt(lines[index + it]) }
        val currentWindow = currentWindowIndices.map { Integer.parseInt(lines[index + it]) }

        if (currentWindow.sum() > lastWindow.sum()) {
          counter += 1
        }
      }

      println(counter)
    }
  }

  object Day2 {
    private val input = File("input/2021/2")

    fun part1() {
      var depth = 0
      var pos = 0
      input.readLines().forEach { line ->
        val command = line.split(" ")[0]
        val argument = Integer.parseInt(line.split(" ")[1])

        when (command) {
          "forward" -> pos += argument
          "up" -> depth -= argument
          "down" -> depth += argument
          else -> {}
        }
      }
      println(depth * pos)
    }

    fun part2() {
      var depth = 0
      var pos = 0
      var aim = 0
      input.readLines().forEach { line ->
        val command = line.split(" ")[0]
        val argument = Integer.parseInt(line.split(" ")[1])

        when (command) {
          "forward" -> {
            pos += argument
            depth += (aim * argument)
          }
          "up" -> aim -= argument
          "down" -> aim += argument
          else -> {}
        }
      }
      println(depth * pos)
    }
  }

  object Day3 {
    private val input = File("input/2021/3")

    private fun List<Int>.toByteToInt(): Int =
      reversed().foldIndexed(0) { index, acc, digit -> acc + (2.0.pow(index.toDouble()) * digit).toInt() }

    private fun Int.invert0and1(): Int = when (this) {
      1 -> 0
      0 -> 1
      else -> throw Exception()
    }

    fun part1() {
      val gammaRaw = MutableList(12) { 0 }
      input.readLines().forEach { line ->
        line.toCharArray().forEachIndexed { i, char ->
          when (char) {
            '1' -> gammaRaw[i] += 1
            '0' -> gammaRaw[i] -= 1
            else -> {}
          }
        }
      }
      val gamma = gammaRaw.map { it.coerceIn(0, 1) }
      val gammaDec = gamma.toByteToInt()
      val epsilonDec = gamma.reversed().foldIndexed(0) { index, acc, digit ->

        acc + (2.0.pow(index.toDouble()) * digit.invert0and1()).toInt()
      }
      println("Gamma: $gammaDec, epsilon: $epsilonDec, power: ${gammaDec * epsilonDec}")
    }

    fun part2() {

      @Suppress("NonAsciiCharacters", "FunctionName")
      fun lööp(invert: Boolean): Int {
        var lines = input.readLines()

        repeat(lines[0].length) { i ->
          if (lines.count() == 1) {
            return lines.single().map { char ->
              when (char) {
                '1' -> 1
                '0' -> 0
                else -> throw Exception()
              }
            }.toByteToInt()
          }

          var commonValue = 1
          lines.forEach { line ->
            when (line[i]) {
              '1' -> commonValue += 1
              '0' -> commonValue -= 1
              else -> {}
            }
          }
          var filterRaw = commonValue.coerceIn(0, 1)
          if (invert) {
            filterRaw = filterRaw.invert0and1()
          }
          val filter = filterRaw.toString().first()
          lines = lines.filter { line -> line[i] == filter }
        }
        throw Exception()
      }

      val oxygen = lööp(invert = false)
      val co2 = lööp(invert = true)
      println("oxygen: $oxygen, co2: $co2, life: ${oxygen * co2}")
    }
  }

  object Day4 {
    private val input = File("input/2021/4")

    private fun List<MutableMap<Int, Boolean>>.checkWin(): Boolean {
      return this.any { line -> line.values.all { it } } ||
          (0 until this[0].count()).any { x -> (0 until this.count()).all { y -> this[x][y] == true } }
    }

    private fun readInput(): Pair<List<Int>, List<List<MutableMap<Int, Boolean>>>> {
      val lines = input.readLines()
      return lines[0].split(",").map { it.toInt() } to
          lines.drop(1).chunked(6)
              .mapNotNull { field ->
                field.takeIf { it.count() > 1 }
                    ?.drop(1)
                    ?.map { line ->
                      line.chunked(3)
                          .associate { it.trim().toInt() to false }
                          .toMutableMap()
                    }
              }
    }

    fun part1() {
      val (numbersToDraw, fields) = readInput()

      numbersToDraw.forEach { number ->
        fields.forEachIndexed { i, field ->
          field.forEach { line ->
            if (line[number] == false) {
              line[number] = true
            }
          }
          if (field.checkWin()) {
            val uncheckedCells = field.flatMap { line -> line.filterValues { !it }.keys }
            println("Field $i won with number $number. Score: ${number * uncheckedCells.sum()}")
            return
          }
        }
      }
    }

    fun part2() {
      val (numbersToDraw, fields) = readInput()
      val wonFields = mutableListOf<Int>()

      numbersToDraw.forEach { number ->
        fields.forEachIndexed { i, field ->
          if (i in wonFields) return@forEachIndexed

          field.forEach { line ->
            if (line[number] == false) {
              line[number] = true
            }
          }
          if (field.checkWin()) {
            val uncheckedCells = field.flatMap { line -> line.filterValues { !it }.keys }
            wonFields += i
            if (wonFields.count() == fields.count() - 1) {
              println("Field $i won last with number $number. Score: ${number * uncheckedCells.sum()}")
            }
          }
        }
      }
    }
  }

  object Day5 {
    private val lines = File("input/2021/5").readLines()

    data class Point(val x: Int, val y: Int)

    fun part1() {
      val points: MutableMap<Point, Int> = mutableMapOf()
      lines.map { line ->
        val (from, to) = line.split(" -> ").map { pointStr ->
          val (x, y) = pointStr.split(",").map { it.toInt() }
          Point(x, y)
        }
        //val vent: Pair<Point, Point> = from to to
        if (from.x == to.x) {
          // Vertical
          val (lowY, highY) = listOf(from.y, to.y).sorted()
          (lowY..highY).forEach { y ->
            val point = Point(from.x, y)
            points[point] = (points[point] ?: 0) + 1
          }
        } else if (from.y == to.y) {
          // Horizontal
          val (lowX, highX) = listOf(from.x, to.x).sorted()
          (lowX..highX).forEach { x ->
            val point = Point(x, from.y)
            points[point] = (points[point] ?: 0) + 1
          }
        }
      }

      println("At least two overlap count: ${points.count { (_, count) -> count >= 2 }}")
    }

    fun part2() {
      val points: MutableMap<Point, Int> = mutableMapOf()
      lines.map { line ->
        val (from, to) = line.split(" -> ").map { pointStr ->
          val (x, y) = pointStr.split(",").map { it.toInt() }
          Point(x, y)
        }
        //val vent: Pair<Point, Point> = from to to
        if (from.x == to.x) {
          val (lowY, highY) = listOf(from.y, to.y).sorted()
          // Vertical
          (lowY..highY).forEach { y ->
            val point = Point(from.x, y)
            points[point] = (points[point] ?: 0) + 1
          }
        } else if (from.y == to.y) {
          // Horizontal
          val (lowX, highX) = listOf(from.x, to.x).sorted()
          (lowX..highX).forEach { x ->
            val point = Point(x, from.y)
            points[point] = (points[point] ?: 0) + 1
          }
        } else {
          val diff = to.x - from.x
          val range = (if (diff >= 0) (0..diff) else 0 downTo diff).toList()
          val yMultiply = if (to.y - from.y == to.x - from.x) 1 else -1
          (0 until range.count()).forEach { i ->
            val point = Point(from.x + range[i], from.y + (range[i] * yMultiply))
            points[point] = (points[point] ?: 0) + 1
          }
        }
      }

      println("At least two overlap count: ${points.count { (_, count) -> count >= 2 }}")
    }
  }

  object Day6 {
    private val fishInput = File("input/2021/6").readLines()[0]
        .split(",").map { it.toInt() }
        .toList()

    private fun calc(root: Int, days: Int): List<Int> {
      val fishDescendants = LinkedList(listOf(root))
      repeat(days) {
        val newFish = LinkedList<Int>()
        fishDescendants.replaceAll { f ->
          if (f == 0) {
            newFish.add(8)
            6
          } else {
            f - 1
          }
        }
        fishDescendants.addAll(newFish)
      }
      return fishDescendants
    }

    fun part1() {
      val fish = fishInput.toMutableList()
      var fishCount = 0
      fish.forEach { f ->
        fishCount += calc(f, 80).count()
      }

      println("After 80 days: $fishCount fish")
    }

    fun part2() {
      val fishDescendantsMap = mutableMapOf<Int, List<Int>>()
      val days = 256
      val parts = 4
      //var totalFishCount = 0L

      fun loop(fish: List<Int>, partIndex: Int): Long {
        var fishCount = 0L
        val shouldRecurse = partIndex > 0
        fish.mapIndexed { i, root ->
          val fishDescendants = fishDescendantsMap
              .getOrPut(root) { calc(root, days / parts) }

          /*if ((partIndex == 1 && i % 10 == 0) || partIndex > 1) {
            println("${" ".repeat(4 - partIndex)}P${partIndex}F${i.toString().padStart(3, '0')}: ${fishDescendants.count()}")
          }*/
          val totalFishDescendantsCount = if (shouldRecurse) {
            loop(fishDescendants, partIndex - 1)
          } else {
            fishDescendants.count().toLong()
          }
          fishCount += totalFishDescendantsCount

        }
        if (partIndex == 3) {
          println("\nPart ${partIndex + 1} of $parts completed. $fishCount")
        } else if (partIndex == 2) {
          print(".")
        }
        return fishCount
      }

      val totalFishCount = loop(fishInput, parts - 1)
      /*repeat(parts) { partIndex ->

      }*/

      println("After $days days: $totalFishCount fish")
    }
  }

  object Day7 {
    private val crabPositions = File("input/2021/7").readLines()[0].split(",").map { it.toInt() }

    fun part1() {
      val distances = (0..crabPositions.count())
          .map { i -> crabPositions.sumOf { (it - i).absoluteValue } }
          .minOrNull()
      println(distances)
    }

    fun part2() {
      /*
      1: 1
      2: 3
      3: 6
      4: 10
      5: 15
      6: 21
       */
      fun fib(x: Int): Int {
        return if (x == 0) 0
        else fib(x - 1) + x
      }

      val distances = (0..crabPositions.count())
          .map { i -> crabPositions.sumOf { fib((it - i).absoluteValue) } }
          .minOrNull()
      println(distances)
    }
  }

  object Day8 {
    private val input = File("input/2021/8").readLines()

    fun part1() {
      val sizes = listOf(2, 3, 4, 7)
      println(input.sumOf { line ->
        line.split(" | ")[1].split(" ").count { it.length in sizes }
      })
    }

    fun part2() {

      println(input.sumOf { line ->
        val (calibration, depthDigits) = line.split(" | ").map { it.split(" ") }
        val digitMap = mutableMapOf<SortedSet<Char>, Int>()


        val one = calibration.single { it.length == 2 }
        val four = calibration.single { it.length == 4 }
        val seven = calibration.single { it.length == 3 }
        val eight = calibration.single { it.length == 7 }

        val nine = calibration.single { it.length == 6 && four.all { c -> c in it } }
        val six = calibration.single { it.length == 6 && it != nine && four.filter { c -> c !in one }.all { c -> c in it } }
        val zero = calibration.single { it.length == 6 && it != six && it != nine }

        val c = nine.single { it !in six }
        val three = calibration.single { it.length == 5 && one.toCharArray().all { c -> c in it } }
        val two = calibration.single { it.length == 5 && it != three && it.contains(c) }
        val five = calibration.single { it.length == 5 && it != three && it != two }

        listOf(zero, one, two, three, four, five, six, seven, eight, nine).forEachIndexed { i, d -> digitMap[d.toSortedSet()] = i }

        var depth = 0
        depthDigits.map { digitMap[it.toSortedSet()]!! }.asReversed().forEachIndexed { i, d ->
          depth += (10.0.pow(i)).toInt() * d
        }
        depth
      })

    }
  }

  object Day9 {
    private val field: List<List<Int>> = File("input/2021/9").readLines().map { line ->
      line.split("").mapNotNull { it.takeIf { it.isNotEmpty() }?.toInt() }
    }

    data class Point(val x: Int, val y: Int) {
      val value get() = field[y][x]
      val valueNullable get() = field.getOrNull(y)?.getOrNull(x)
      val adjacents
        get() =
          listOf(-1 to 0, 0 to -1, 1 to 0, 0 to 1)
              .map { (xOffset, yOffset) -> Point(x + xOffset, y + yOffset) }
    }

    fun part1() {
      var sum = 0
      repeat(field.size) { y ->
        repeat(field[0].size) { x ->
          val cell = field[y][x]
          val adjacent = listOf(Point(-1, 0), Point(0, -1), Point(1, 0), Point(0, 1))
              .mapNotNull { (xOffset, yOffset) ->
                field.getOrNull(y + yOffset)?.getOrNull(x + xOffset)
              }
          if (adjacent.all { it > cell }) {
            sum += cell + 1
          }
        }
      }
      println("Sum: $sum")
    }

    fun part2() {


      fun getSmaller(point: Point): List<Point>? {
        val cell = point.value
        val adjacents = point.adjacents
        val smaller = adjacents.filter { adj ->
          adj.valueNullable?.let { it < cell } == true
        }
        return if (smaller.isEmpty()) {
          null
        } else {
          smaller.flatMap { getSmaller(it) ?: listOf(it) }
        }
      }
      /*
        data class BasinPoint(val p: Point, val smaller: List<Point>)

        val roots = field
            .flatMapIndexed { y, row -> row.mapIndexedNotNull { x, cell -> Point(x, y).takeIf { cell == 9 } } }
            .map { root ->
          BasinPoint(root, getSmaller(root) ?: emptyList())
        }
        val sum = basins
            .map { it.smaller }
            .sortedDescending()
            .take(3)
            .fold(1) { acc, it -> acc * it }
        println("Sum: $sum")*/

      val lowPoints = field.flatMapIndexed { y, row ->
        row.mapIndexedNotNull { x, cell ->
          val point = Point(x, y)
          point.takeIf { point.adjacents.all { adj -> adj.valueNullable?.let { it > cell } == true } }
        }
      }

      @Suppress("NonAsciiCharacters", "FunctionName")
      fun lööp(p: Point, visited: MutableList<Point>): List<Point> {
        if (visited.contains(p)) return emptyList()

        visited.add(p)
        return (
            p.adjacents
                .filter { it.valueNullable != null && it.value != 9 }
                .takeIf { it.isNotEmpty() }
                ?.flatMap { lööp(it, visited) }
              ?: emptyList()
            ) + p
      }

      val basins = lowPoints.map { p ->
        val visited = mutableListOf<Point>()
        lööp(p, visited)
      }

      val sum = basins
          .map { it.size }
          .sortedDescending()
          .take(3)
          .fold(1) { acc, it -> acc * it }
      println("Sum: $sum")
    }
  }

  object Day10 {
    val lines = File("input/2021/10").readLines()

    private val opening = listOf('[', '(', '<', '{')

    private fun corresponding(c: Char): Char = when (c) {
      ']' -> '['
      '}' -> '{'
      '>' -> '<'
      ')' -> '('
      else -> throw Exception()
    }

    private val values = mapOf(
      ')' to 3,
      ']' to 57,
      '}' to 1197,
      '>' to 25137,
    )

    fun part1() {

      val sum = lines
          .map { line ->
            val stack = mutableListOf<Char>()

            line.toCharArray().firstOrNull { c ->
              if (c in opening) {
                stack.add(c)
              } else {
                when (stack.last()) {
                  corresponding(c) -> stack.removeLast()
                  else -> return@firstOrNull true
                }
              }
              return@firstOrNull false
            }
          }
          .sumOf { values[it]!! }
      println("Sum: $sum")
    }
  }
}