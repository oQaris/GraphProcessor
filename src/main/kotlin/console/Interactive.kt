package console

import graphs.AdjacencyMatrixGraph
import storage.SetFileGraph.Companion.isNewName
import java.util.*

/**
 * Класс включает в себя пользовательский интерфейс
 * Каждый метод выводит, либо запрашивает ввод с консоли
 * Никакие исключения не кидаются, все ошибки выводятся на консоль
 * Функции, помеченные префиксом "_" возвращают введённое значение
 */
object GPInterface {
    const val maxTypeIn = 3
    private var input = Scanner(System.`in`)
    private fun inputInt(inf: Int, sup: Int): Int {
        var buf = if (input.hasNextInt()) input.nextInt() else inf - 1
        while (buf < inf || buf > sup) {
            println("Invalid input, please try again.")
            buf = if (input.hasNextInt()) input.nextInt() else inf - 1
        }
        return buf
    }

    private fun inputName(): String {
        var name = if (input.hasNext()) input.next() else "new"
        while (isNewName(name)) {
            println("Invalid input, please try again.")
            name = if (input.hasNext()) input.next() else "new"
        }
        return name
    }

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
        val typeIn = inputInt(1, maxTypeIn)
        /*Имя*/
        print("Enter the name of the graph:\t")
        val name = inputName()
        /*Вершины*/
        print("Enter the number of vertices:\t")
        val n = inputInt(1, Int.MAX_VALUE)
        return newGraph(name, n, grade == 1, typeIn)
    }

    private fun newGraph(name: String, numVer: Int, weight: Boolean, typeIn: Int): AdjacencyMatrixGraph {
        require(!(typeIn < 1 || typeIn > maxTypeIn || numVer < 1 || isNewName(name))) { "Invalid parameters." }
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
                    val w = inputInt(Int.MIN_VALUE, Int.MAX_VALUE)
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
                if (buf != 0) g[i][buf - 1] = inputInt(Int.MIN_VALUE, Int.MAX_VALUE) else break
            }
        }
    }

    fun entryAdjacencyMatrix(g: Array<Array<Int?>>) {
        println("Введите саму матрицу смежности.\nДля обозначения несмежных вершин введите не число (обычно '-')")
        for (i in g.indices) {
            for (j in g.indices) {
                g[i][j] = if (input.hasNextInt()) input.nextInt() else null
            }
        }
    }

    // Прочие методы, используемые в Main'e
    fun inputParam(param: Array<String>) {
        input = Scanner(System.`in`)
        Arrays.fill(param, "")
        val strArr = input.nextLine().split(" ").toTypedArray()
        for (word in strArr) {
            if (word != "") {
                for (i in param.indices) if (param[i] == "") {
                    param[i] = word.lowercase(Locale.getDefault())
                    break
                }
            }
        }
    }

    fun _chain(): Boolean {
        println("Попробовать найти Гамильтонову цепь?\t0 - нет, 1 - да")
        var buf = input.nextInt()
        while (buf != 0 && buf != 1) {
            println("Неверный ввод, повторите попытку")
            input.nextLine()
            buf = input.nextInt()
        }
        return buf == 1
    }

    fun _multistart(n: Int): Int {
        println("Использовать мультистарт или ввести начальную веришну?")
        println("0 - мультистарт, 1-$n - выбрать вершину с указанным номером для старта")
        var buf = input.nextInt()
        while (buf < 0 || buf > n) {
            println("Неверный ввод, повторите попытку")
            input.nextLine()
            buf = input.nextInt()
        }
        return buf
    }

    fun _tree(): Boolean {
        println("Нарисовать дерево поиска?\t0 - нет, 1 - да")
        var buf = input.nextInt()
        while (buf != 0 && buf != 1) {
            println("Неверный ввод, повторите попытку")
            input.nextLine()
            buf = input.nextInt()
        }
        return buf == 1
    }
}
