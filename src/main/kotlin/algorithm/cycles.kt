package algorithm

import graphs.Graph
import java.util.*

/**
 * Возвращает массив вершин, смежных с данной, и, которые ещё не отмечены в массиве pass как true
 * pass.length должен быть >= G.getNumVer()
 */
private fun Graph.comWith(ver: Int, pass: BooleanArray) = com(ver).filter { !pass[it] }

/**
 * Поиск Гамильтонового цикла или цепи (проходящей по всем вершинам)
 *
 * @param g     Граф для поиска
 * @param start Вершина, с которой стартует поиск
 * @param cycle Флаг нахождения цикла или цепи
 * @param tree  Флаг печати дерева поиска
 * @return Список вершин, содержащих Гамильтонов цикл или цепь
 */
fun hamiltonCycle(g: Graph, start: Int, cycle: Boolean, tree: Boolean): List<Int> {
    val pass = BooleanArray(g.numVer)       // Массив пройденных вершин
    pass[start] = true
    var com: List<Int>                      // Массив смежных вершин
    val treeArr = BooleanArray(g.numVer)    // Вспомогательный массив для рисования дерева

    var curV = start                // Текущая вершина
    val hc = mutableListOf(curV)    // Список пройденных вершин

    var block = g.numVer - 1    // Счётчик для разблокировки 1-й вершины
    var back = false            // Если мы вернулись назад
    if (tree) println("Search tree:\n►$start")

    while (!pass.all { it }) {
        require(hc.isNotEmpty()) {
            "The graph does not contain a Hamiltonian " +
                    if (cycle) "cycle." else "chain from a given vertex."
        }
        // Получаем массив допустимых для движения вершин
        com = g.comWith(hc.last(), pass)
        if (com.isNotEmpty()) {
            // Если вернулись назад на ветвление
            if (back) {
                // Непройденное ветвление
                if (com.size > 1 && curV < com[com.size - 1]) {
                    // Переходим на следующую ветку, относительно той, с которой пришли
                    curV = com[com.indexOf(curV) + 1]
                    back = false
                    pass[curV] = true
                    hc.add(curV)
                    block--
                }
                // Пройденное ветвление
                else {
                    // Откатываемся до ближайшего ветвления
                    do {
                        curV = hc.removeLast()
                        pass[curV] = false
                        pass[start] = true
                        block++
                    } while (hc.isNotEmpty() && g.comWith(hc.last(), pass).size < 2)
                    back = true
                }
            } else {
                // Если идём вперёд
                curV = com[0]
                pass[curV] = true
                hc.add(curV)
                block--
            }
        }
        // Если тупик
        else {
            // Откатываемся до ближайшего ветвления
            do {
                curV = hc.removeLast()
                pass[curV] = false
                pass[start] = true
                block++
            } while (hc.isNotEmpty() && g.comWith(hc.last(), pass).size < 2)
            back = true
        }
        if (block == 0 && cycle)
            pass[start] = false

        // Построение дерева поиска
        if (tree && !back) {
            var i = 0
            while (i < hc.size - 2) {
                // Вниз от └► (false) всегда печатаем " ", а снизу от ├► - " │"
                if (!treeArr[i]) print("  ") else print(" │")
                i++
            }
            if (com.last() == curV) {   // Если вершина последняя в ветке
                println(" └►" + (curV + 1))
                treeArr[i] = false
            } else {
                println(" ├►" + (curV + 1))
                treeArr[i] = true
            }
        }
    }
    return hc
}

/**
 * Поиск Эйлерова цикла (проходящего по всем рёбрам)
 *
 * @param g     Граф для поиска
 * @param start Вершина, с которой стартует поиск
 * @return Список вершин, содержащий Эйлеров цикл
 */
fun eulerCycle(g: Graph, start: Int): LinkedList<Int> {
    val gCpy = g.clone()
    val st = LinkedList<Int>()
    val ec = LinkedList<Int>()
    var curV = start - 1
    var nxtV = 0
    st.push(start)
    while (!st.isEmpty()) {
        when {
            // Если существует ребро из curV вершины в nxtV вершину
            gCpy.isCom(curV, nxtV) -> {
                st.push(nxtV + 1)
                gCpy.remEdg(curV, nxtV)     // Удаляем эти рёбра
                curV = nxtV                 // Переходим к следущей вершине
                nxtV = 0
            }
            // Если перебрали все и ничего не подошло
            nxtV == g.numVer - 1 -> {
                ec.push(st.pop())           // Перекладываем вершину в другой стек
                if (!st.isEmpty()) curV = st.peek() - 1
                nxtV = 0
            }
            else -> nxtV++
        }
    }
    return ec
}
