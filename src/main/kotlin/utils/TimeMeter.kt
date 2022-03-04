package utils

import kotlin.math.abs

class TimeMeter(private val times: List<Long>) {

    /** @return Среднее арифметическое временных меток */
    fun getMean(): Long {
        if (times.isEmpty()) return 0L
        return times.sum() / times.size
    }

    /** @return Медиану временных меток */
    fun getMedian(): Long {
        if (times.isEmpty()) return 0
        val sortTimes = times.sorted()
        val n = times.size
        return if (n % 2 == 1) sortTimes[(n - 1) / 2]
        else (sortTimes[n / 2 - 1] + sortTimes[n / 2]) / 2
    }

    /** @return Моду временных меток */
    fun getMode(): Long {
        return times.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: 0L
    }

    /** @return Максимальное из временных меток*/
    fun getMax() = times.maxOrNull() ?: 0L

    /** @return Минимальное из временных меток */
    fun getMin() = times.minOrNull() ?: 0L

    /** Выводит на консоль схематичный график временных меток */
    fun printGraph(segmentLen: Int = 128) {
        require(segmentLen > 0) { "Длина сегмента для вывода должна быть больше 0, но было $segmentLen" }
        val maxAbsOr1 = times.maxOfOrNull { abs(it) }.let { if (it == null || it == 0L) 1 else it }
        val newTimes = times.map { (abs(it) * segmentLen / maxAbsOr1).toInt() }.zip(times)
        val maxShift = newTimes.maxOfOrNull { (abs, orig) -> if (orig < 0) abs else 0 } ?: 0
        newTimes.forEach { (abs, orig) ->
            if (orig > 0) print(" ".repeat(maxShift))
            if (orig < 0) print(" ".repeat(maxShift - abs))
            println("*".repeat(abs))
        }
    }
}
