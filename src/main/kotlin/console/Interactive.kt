package console

import graphs.impl.AdjacencyMatrixGraph
import java.util.regex.Pattern

/**
 * Класс включает в себя пользовательский интерфейс
 * Каждый метод выводит, либо запрашивает ввод с консоли
 * Никакие исключения не кидаются, все ошибки выводятся на консоль
 * Функции, помеченные префиксом "_" возвращают введённое значение
 */
object GPInterface {
    private val NAME_PATTERN = Pattern.compile("""^[_A-z0-9]*((\s)*[_A-z0-9])*${'$'}""")
    private const val MAX_TYPE_IN = 3

    private fun inputInt(inf: Int = Int.MIN_VALUE, sup: Int = Int.MAX_VALUE): Int {
        while (true) {
            try {
                val num = readLine()!!.trim().toInt()
                if (num in inf..sup)
                    return num
                println("The number must be between $inf and $sup.")
            } catch (e: NumberFormatException) {
                println("Invalid input, please try again.")
            }
        }
    }

    private fun readString() = generateSequence { System.`in`.read().toChar() }
        .dropWhile { it.isWhitespace() }.takeWhile { !it.isWhitespace() }.joinToString("")

    private fun inputName(): String {
        while (true) {
            val str = readLine()!!.trim()
            if (isValidName(str))
                return str
            println("Invalid input, please try again.")
        }
    }

    private fun isValidName(str: String) = NAME_PATTERN.matcher(str).matches()

    fun newGraph(): AdjacencyMatrixGraph {
        /*Тип*/
        println(
            "Select the type of graph:\n" +
                    "1 - Weighted\n" +
                    "2 - Not weighted"
        )
        val grade = inputInt(1, 2)
        /*Метод*/
        println(
            "Select the input method for the graph:\n" +
                    "1 - Step by step (for digraph)\n" +
                    "2 - Step by step (for non digraph)\n" +
                    "3 - Adjacency matrix\n" +
                    "Incident Matrix and Edge List still in development"
        )
        val typeIn = inputInt(1, MAX_TYPE_IN)
        /*Имя*/
        print("Enter the name of the graph:\t")
        val name = inputName()
        /*Вершины*/
        print("Enter the number of vertices:\t")
        val n = inputInt(1, Int.MAX_VALUE)
        return newGraph(name, n, grade == 1, typeIn)
    }

    private fun newGraph(name: String, numVer: Int, weight: Boolean, typeIn: Int): AdjacencyMatrixGraph {
        require(typeIn in 1..MAX_TYPE_IN && numVer > 0 && isValidName(name)) { "Invalid parameters." }
        val g = Array(numVer) { arrayOfNulls<Int>(numVer) }
        when (typeIn) {
            1 -> if (weight) entryOrgraphW(g) else entryOrgraph(g)
            2 -> if (weight) entryNoOrgraphW(g) else entryNoOrgraph(g)
            3 -> entryAdjacencyMatrix(g)
            else -> throw IllegalArgumentException("Undefined graph input method.")
        }
        return AdjacencyMatrixGraph(name, g)
    }

    private fun printOldVer(g: Array<Array<Int?>>, i: Int) {
        var flag = false
        print((i + 1).toString() + ":  ")
        for (j in g.indices) {
            if (g[i][j] != null) {
                print((j + 1).toString() + " ")
                flag = true
            }
        }
        if (flag) print("and ")
    }

    fun entryNoOrgraph(g: Array<Array<Int?>>) {
        println("Формат ввода:\nТекущая_вершина:  Список_Исходящих_Из_Неё...\nДля окончания ввода - 0")
        for (i in g.indices) {
            // Ищем и выводим уже существующие смежные
            printOldVer(g, i)
            // Вводим новые смежные
            while (true) {
                val buf = inputInt(0, g.size)
                if (buf != 0) {
                    g[i][buf - 1] = 0
                    g[buf - 1][i] = 0
                } else break
            }
        }
    }

    fun entryNoOrgraphW(g: Array<Array<Int?>>) {
        println("Формат ввода:\nТекущая_вершина:  Список_Исходящих_Из_Неё_и_Их_Веса...\nПосле каждой вершины пишите вес получившейся дуги через пробел\nДля окончания ввода - 0")
        for (i in g.indices) {
            printOldVer(g, i)
            while (true) {
                val buf = inputInt(0, g.size)
                if (buf != 0) {
                    val w = inputInt()
                    g[i][buf - 1] = w
                    g[buf - 1][i] = w
                } else break
            }
        }
    }

    fun entryOrgraph(g: Array<Array<Int?>>) {
        println("Формат ввода:\nТекущая_вершина:  Список_Исходящих_Из_Неё...\nДля окончания ввода - 0")
        for (i in g.indices) {
            printOldVer(g, i)
            while (true) {
                val buf = inputInt(0, g.size)
                if (buf != 0) g[i][buf - 1] = 0 else break
            }
        }
    }

    fun entryOrgraphW(g: Array<Array<Int?>>) {
        println("Формат ввода:\nТекущая_вершина:  Список_Исходящих_Из_Неё_и_Их_Веса...\nПосле каждой вершины пишите вес получившейся дуги через пробел\nДля окончания ввода - 0")
        for (i in g.indices) {
            printOldVer(g, i)
            while (true) {
                val buf = inputInt(0, g.size)
                if (buf != 0) g[i][buf - 1] = inputInt() else break
            }
        }
    }

    fun entryAdjacencyMatrix(g: Array<Array<Int?>>) {
        println("Введите саму матрицу смежности.\nДля обозначения несмежных вершин введите не число (обычно '-')")
        for (i in g.indices) {
            for (j in g.indices) {
                g[i][j] = try {
                    readLine()!!.trim().toInt()
                } catch (e: NumberFormatException) {
                    null
                }
            }
        }
    }
}
