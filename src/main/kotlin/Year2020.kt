import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


object Year2020 {
  object Day25 {
    private val input = File("input/2020/25")

    fun part1() {
      val loopCache = mutableSetOf<Pair<Int, Int>>()
      val (cardPbk, doorPbk) = input.readLines().map { it.toInt() }

      fun findLoopSize(publicKey: Int): Int {

        fun loop(loopSize: Int): Int { //subjectNumber: Int
          val (greatestCachedLoopSize, greatestCachedValue) = loopCache.reversed().firstOrNull() ?: (0 to 1)
          if (greatestCachedLoopSize == loopSize) return greatestCachedValue

          var value = greatestCachedValue
          repeat(loopSize - greatestCachedLoopSize) {
            value *= 7 // subjectNumber
            value %= 20201227
          }
          loopCache.add(loopSize to value)
          return value
        }

        var loopSize: Int? = null
        for (l in 100..10_000_000) {
          if (loop(loopSize = l) == publicKey) {
            loopSize = l
            break
          }
        }
        if (loopSize == null) {
          throw Exception("Could not find number in 0..10000")
        } else {
          return loopSize
        }
      }

      val cardLoopSize = findLoopSize(cardPbk)
      val doorLoopSize = findLoopSize(doorPbk)

      println("Cardloopsize: $cardLoopSize, doorloopsize: $doorLoopSize")
    }
  }
}
