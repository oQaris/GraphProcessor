package utils

import java.util.*

class Timestamps {

    // System.currentTimeMillis() не подходит, поскольку нужна монотонно возрастающая последовательность
    private var start = System.nanoTime()

    // LinkedList, поскольку добавление в конец должно происходить всегда за одно время
    val times = LinkedList<Long>()

    fun make() = times.add(System.nanoTime() - start)
}
