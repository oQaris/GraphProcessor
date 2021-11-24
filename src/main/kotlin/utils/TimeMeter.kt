package utils

class TimeMeter {
    private val times = mutableListOf<Long>()

    fun addTimestamp(time: Long) = times.add(time)

    /** @return Среднее арифметическое временных меток */
    fun getMean(): Long {
        if (times.isEmpty()) return 0L
        return times.sum() / times.size
    }

    /** @return Медиану временных меток */
    fun getMedian(): Long {
        if (times.isEmpty()) return 0
        val sortTimes = times.sorted()
        return if (times.size % 2 == 1) sortTimes[(times.size + 1) / 2]
        else (sortTimes[times.size / 2] + sortTimes[(times.size + 1) / 2]) / 2
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
    fun printGraph() {
        val min = getMin()
        for (time in times) {
            val t = time / min
            println("*".repeat(t.toInt()))
        }
    }
}
