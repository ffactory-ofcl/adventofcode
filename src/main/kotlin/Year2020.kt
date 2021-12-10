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
      val loopCache = concurrentSetOf<Pair<Int, Int>>()
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

        (100..10_000_000).forEachAsync(threads = 24) { l ->
//          if (loop(loopSize = l) == publicKey) {
//            return@forEachAsync l
//          }
        }
        throw Exception("Could not find number in 0..10000")
      }

      val cardLoopSize = findLoopSize(cardPbk)
      val doorLoopSize = findLoopSize(doorPbk)

      println("Cardloopsize: $cardLoopSize, doorloopsize: $doorLoopSize")
    }
  }
}

inline fun <T, R> Iterable<T>.forEachAsync(threads: Int = 8, crossinline action: (T) -> R) {
  val group = MbAsyncGroup<R>(threads)
  this.forEach { group.background { action(it) } }
  group.wait()
}

class MbAsyncGroup<R>(private val maxThreads: Int = 8, private val threadPool: Executor? = null) {
  var waitSemaphore = Semaphore(maxThreads)
  private var error: Exception? = null
  private val activeBackgroundThreads: MutableSet<Thread> = concurrentSetOf()

  // Warning: Do not call background() on the same group recursively: group.background { group.background() }
  fun  background(task: () -> R): MbAsyncGroup<R> {
    if (Thread.currentThread() in activeBackgroundThreads) {
      error = Exception("background() was called recursively, this could lead to a deadlock") // Save error -> throw when wait() is called
      return this // Do not execute task() because it could lead to a deadlock. Better throw than deadlock.
    }
    waitSemaphore.acquire()

    val caughtTask: () -> Unit = {
      activeBackgroundThreads.add(Thread.currentThread())
      try {
        task()
      } catch (e: Exception) {
        error = e // Don't throw, as we throw (the last) error on wait()
      } finally {
        activeBackgroundThreads.remove(Thread.currentThread())
        waitSemaphore.release()
      }
    }

    if (threadPool == null) {
      thread(block = caughtTask)
    } else {
      threadPool.execute(caughtTask)
    }

    return this
  }

  // Default timeout for web containers is 10 minutes, after that a MbAsyncGroup, AsyncParserGroup or forEachAsync() should have finished.
  // On workers/one-offs it can happen that we use MbAsyncGroup/forEachAsync() for longer operations,
  // but 24h should be a reasonable value where we could consider that the operation resulted in a deadlock.
  fun wait(timeoutMinutes: Int = 10): MbAsyncGroup<R> {
    if (Thread.currentThread() in activeBackgroundThreads) {
      throw Exception("wait() was called in background, this is not supported as it would always lead to a deadlock. Call wait() only once outside background().")
    }
    if (!waitSemaphore.tryAcquire(maxThreads, timeoutMinutes.toLong(), TimeUnit.MINUTES)) {
      error = Exception(
        "Thread leak detected: MbAsyncGroup could not finish all it's background threads within $timeoutMinutes minutes. " +
            "Some background-threads are still running."
      )
    }
    waitSemaphore.release(maxThreads)

    error?.let { error ->
      throw error
    }

    return this
  }
}

fun <T> concurrentSetOf(vararg elements: T): MutableSet<T> = ConcurrentHashMap<T, Any>().keySet(Any()).apply { addAll(elements) }