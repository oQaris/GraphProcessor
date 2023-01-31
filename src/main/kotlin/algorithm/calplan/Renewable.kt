package console.algorithm.calplan

import com.github.shiguruikai.combinatoricskt.CartesianProductGenerator
import com.github.shiguruikai.combinatoricskt.Combinatorics
import graphs.impl.AdjacencyMatrixGraph

val g = AdjacencyMatrixGraph("cal", 9).apply {
    oriented = true
    addEdg(1, 2)
    addEdg(1, 3)
    addEdg(2, 4)
    addEdg(3, 5)
    addEdg(3, 6)
    addEdg(2, 3)
    addEdg(4, 7)
    addEdg(5, 8)
    addEdg(6, 8)
}

val chains = arrayOf(
    listOf(1, 2, 3, 5, 8),
    listOf(4, 7),
    listOf(6),
)

//Никита
/*val g = AdjacencyMatrixGraph("cal", 9).apply {
    oriented = true
    addEdg(1, 2)
    addEdg(1, 3)
    addEdg(1, 4)
    addEdg(2, 5)
    addEdg(3, 6)
    addEdg(4, 8)
    addEdg(5, 7)
    addEdg(6, 7)
    addEdg(6, 8)
}

val chains = arrayOf(
    listOf(1, 2, 5, 7),
    listOf(3, 6),
    listOf(4, 8),
)*/

// Ника
/*val g = AdjacencyMatrixGraph("cal", 9).apply {
    oriented = true
    addEdg(1, 2)
    addEdg(1, 3)
    addEdg(2, 4)
    addEdg(3, 5)
    addEdg(3, 6)
    addEdg(4, 5)
    addEdg(4, 7)
    addEdg(5, 8)
    addEdg(6, 8)
}

val chains = arrayOf(
    listOf(1, 2, 4, 7),
    listOf(3, 5, 8),
    listOf(6),
)*/

//Влад
/*val g = AdjacencyMatrixGraph("cal", 9).apply {
    oriented = true
    addEdg(1, 2)
    addEdg(1, 3)
    addEdg(2, 4)
    addEdg(3, 5)
    addEdg(3, 6)
    addEdg(4, 5)
    addEdg(4, 7)
    addEdg(5, 8)
    addEdg(6, 8)
}

val chains = arrayOf(
    listOf(1, 2, 4, 7),
    listOf(3, 5, 8),
    listOf(6),
)*/

// Егор
/*val g = AdjacencyMatrixGraph("cal", 9).apply {
    oriented = true
    addEdg(1, 2)
    addEdg(1, 3)
    addEdg(2, 4)
    addEdg(3, 5)
    addEdg(5, 6)
    addEdg(4, 5)
    addEdg(4, 7)
    addEdg(5, 8)
    addEdg(6, 8)
}

val chains = arrayOf(
    listOf(1, 2, 4, 7),
    listOf(3, 5, 6, 8)
)*/


const val Q = 2

fun main() {
    val chainSizeParams = chains.map { (0..it.size).toList() }.toTypedArray()
    // Декартово произведение по размерам цепей
    val allTrips = Combinatorics.cartesianProduct(*chainSizeParams).toList()

    val allowedTrips = allTrips.filter { numDoneWorksBySize ->

        val doneWorks = chains.zip(numDoneWorksBySize)
            .flatMap { it.first.take(it.second) }

        doneWorks.flatMap { g.inVer(it) }
            .all { it in doneWorks }
    }

    println("Допустимые (${allowedTrips.size} шт):")
    println(allowedTrips.joinToString("\n") {
        val (f, d) = f(it, allowedTrips)
        "f$it = $f by $d"
    })
    println()

    println("Обратный ход:")
    val deltas = mutableListOf<List<Int>>()
    var curTrips = allowedTrips.last()
    while (!curTrips.all { it == 0 }) {
        print(curTrips)

        val delta = f(curTrips, allowedTrips).second
        println(" - $delta")

        deltas += delta
        curTrips = minus(curTrips, delta)
    }
    println()

    val dirList = deltas.reversed().map { delta ->
        delta.flatMapIndexed { idx, d ->
            if (d != 0)
                setOf(chains[idx].first())
                    .also { chains[idx] = chains[idx].drop(1) }
            else setOf()
        }.toSet()
    }
    println(dirList.joinToString("->"))

    println()
    val notAllowed = allTrips - allowedTrips.toSet()
    println("НЕ допустимые (${notAllowed.size} шт):")
    println(notAllowed.joinToString("\n"))
}

fun f(trips: List<Int>, allowedTrips: List<List<Int>>): Pair<Int, List<Int>> {
    if (trips !in allowedTrips)
        return Pair(Int.MAX_VALUE, listOf())
    val def = Pair(0, listOf<Int>())
    if (trips.all { it == 0 })
        return def
    val deltas = CartesianProductGenerator
        .indices(2, repeat = allowedTrips.first().size).toList()
        .myRemoved(Q).mySorted()

    val minDelta = deltas.filter { dlt ->
        val locTrips = minus(trips, dlt.toList())
        allowedTrips.contains(locTrips)
                && allowedTrips.containsAll(split(dlt).map { minus(trips, it) })
    }.minByOrNull { 1 + f(minus(trips, it.toList()), allowedTrips).first }!!

    val res = minus(trips, minDelta.toList())
    return 1 + f(res, allowedTrips).first to minDelta.toList()
}

fun split(trips: IntArray): List<List<Int>> {
    val res = mutableListOf<List<Int>>()
    trips.forEachIndexed { idx, item ->
        if (item == 1)
            res += IntArray(trips.size).also { it[idx] = 1 }.toList()
    }
    return res
}

fun List<IntArray>.mySorted() = sortedWith(Comparator.comparingInt<IntArray?> { it.sum() }
    .thenComparingInt { it.reversed().joinToString("").toInt() })

fun List<IntArray>.myRemoved(q: Int) = filter { it.sum() <= q && it.sum() != 0 }

fun minus(c1: List<Int>, c2: List<Int>) = c1.zip(c2.toList()).map { (a, b) -> a - b }
