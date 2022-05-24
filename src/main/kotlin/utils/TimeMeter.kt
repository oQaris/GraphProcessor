package utils

import kotlin.math.abs

/** Среднее арифметическое временных меток */
fun List<Long>.mean(): Long {
    if (this.isEmpty()) return 0L
    return this.sum() / this.size
}

/** Медиана временных меток */
fun List<Long>.median(): Long {
    if (this.isEmpty()) return 0
    val sorted = this.sorted()
    val n = this.size
    return if (n % 2 == 1) sorted[(n - 1) / 2]
    else (sorted[n / 2 - 1] + sorted[n / 2]) / 2
}

/** Моды временных меток
 * @param dif Допустимый разброс значения для включения в одно множество */
fun List<Long>.modes(dif: Long = 0): List<Long> {
    //TODO учитывать dif
    return this.groupingBy { it }.eachCount().entries
        .maxsBy { it.value }.map { it.key }
}

/** Одна из мод временных меток
 * @param dif Допустимый разброс значения для включения в одно множество */
fun List<Long>.mode(dif: Long = 0): Long {
    return this.modes(dif).firstOrNull() ?: 0
}

/** Максимальное из временных меток*/
fun List<Long>.max() = this.maxOrNull() ?: 0L

/** Минимальное из временных меток */
fun List<Long>.min() = this.minOrNull() ?: 0L

/** Выводит на консоль схематичный график соотношений временных меток
 * @param segmentLen максимальная длина одной строки при выводе (0 - оригинальное соотношение)
 * @param compression сжатие высоты графика (0 - без сжатия, 1 - сжатие до 1 строки)*/
fun List<Long>.printGraph(segmentLen: Int = 0, compression: Float = 0f) {
    //TODO учитывать compression
    require(segmentLen >= 0) { "Длина сегмента для вывода должна быть не меньше 0, но было $segmentLen" }
    val maxAbsOr1 = this.maxOfOrNull { abs(it) }.let { if (it == null || it == 0L) 1 else it }
    //TODO рефакторинг
    val newTimes = this.map { time ->
        (abs(time) * (segmentLen
            .takeIf { it > 0 }
            ?.toLong() ?: maxAbsOr1) / maxAbsOr1
                ).toInt()
    }.zip(this)
    val maxShift = newTimes.maxOfOrNull { (abs, orig) -> if (orig < 0) abs else 0 } ?: 0
    newTimes.forEach { (abs, orig) ->
        if (orig > 0) print(" ".repeat(maxShift))
        if (orig < 0) print(" ".repeat(maxShift - abs))
        println("*".repeat(abs))
    }
}

private inline fun <T, R : Comparable<R>> Iterable<T>.maxsBy(selector: (T) -> R): List<T> {
    val iterator = this.iterator()
    if (!iterator.hasNext()) return emptyList()
    val maxElems = mutableListOf(iterator.next())
    if (!iterator.hasNext()) return maxElems
    var maxValue = selector(maxElems[0])
    do {
        val e = iterator.next()
        val v = selector(e)
        if (maxValue < v) {
            maxElems.clear()
            maxElems.add(e)
            maxValue = v
        } else if (maxValue == v) {
            maxElems.add(e)
        }
    } while (iterator.hasNext())
    return maxElems
}
