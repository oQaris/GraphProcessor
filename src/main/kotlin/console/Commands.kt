package console

import algorithms.*
import com.udojava.evalex.Expression
import graphs.Graph
import interactive.GPInterface
import picocli.CommandLine.*
import storage.SetFileGraph
import java.io.File
import kotlin.math.roundToInt


@Command(
    name = "gp",
    version = ["GP 0.2.0"],
    subcommands = [New::class, Show::class, Remove::class, Connectivity::class, Planarity::class,
        Isomorphism::class, Path::class, MaxFlow::class, ECycle::class, HCycle::class, Redo::class, HelpCommand::class],
    description = ["sdofigjsdopifgj"],
    mixinStandardHelpOptions = true
)
class BaseCommand

val gfs = SetFileGraph(File("GraphData"))

class GraphConverter : ITypeConverter<Graph> {
    override fun convert(value: String) =
        gfs[value] ?: throw TypeConversionException("Не найден граф с именем $value")
}

open class GraphParameter {
    @Parameters(index = "0", description = [DESCRIPTION_GRAPH], converter = [GraphConverter::class])
    lateinit var graph: Graph
}

// -------------- Конкретные команды -------------- //

@Command(
    name = "new",
    description = ["Создать и сохранить граф"]
)
class New : Runnable {
    override fun run() {
        gfs.add(GPInterface.newGraph())
    }
}

@Command(
    name = "show",
    description = ["Вывести сохранённый граф"]
)
class Show : Runnable {
    @Option(names = ["-a", "--all"], description = ["Вывести все сохранённые графы"])
    var isAll: Boolean = false

    override fun run() {
        if (isAll)
            println(gfs.names.joinToString("\n"))
        else println("Тут будет полный вывод")
    }
}

@Command(
    name = "remove",
    description = ["Удалить граф"]
)
class Remove : GraphParameter(), Runnable {
    override fun run() {
        if (gfs.remove(graph.name) != null) {
            println("Граф успешно удалён")
            gfs.writeAllToFile()
        } else println("Графа с данным именем не существует")
    }
}

@Command(
    name = "connectivity",
    description = ["Вычислить вершинную и рёберную k-связность"]
)
class Connectivity : GraphParameter(), Runnable {
    override fun run() {
        val vc = vertexConnectivity(graph)
        val ec = edgeConnectivity(graph)
        if (vc != 0 && ec != 0)
            println(
                "Граф $vc" +
                        if (ec != vc)
                            ("-вершинно-связен и $ec-рёберно-связен")
                        else
                            "-связен"
            )
        else println("Граф не является связным")
    }
}

@Command(
    name = "planarity",
    description = ["Проверить граф на планарность"]
)
class Planarity : GraphParameter(), Runnable {
    override fun run() {
        if (planarity(graph))
            println("Граф планарен")
        else
            println("Граф не планарен")
    }
}

@Command(
    name = "isomorphism",
    description = ["Проверить два графа на изоморфизм"]
)
class Isomorphism : Runnable {
    @Parameters(description = [DESCRIPTION_GRAPH], converter = [GraphConverter::class])
    lateinit var firstGraph: Graph

    @Parameters(description = [DESCRIPTION_GRAPH], converter = [GraphConverter::class])
    lateinit var secondGraph: Graph

    override fun run() {
        if (isomorphism(firstGraph, secondGraph))
            println("Графы изоморфны")
        else println("Графы не изоморфны")
    }
}

@Command(
    name = "path",
    description = ["Найти кратчайший путь между заданными вершинами"]
)
class Path : GraphParameter(), Runnable {
    @Parameters(description = [DESCRIPTION_VERTEX])
    var firstVertex: Int = -1

    @Parameters(description = [DESCRIPTION_VERTEX])
    var secondVertex: Int = -1

    override fun run() {
        printPath(graph, firstVertex, secondVertex)
    }
}

@Command(
    name = "maxflow",
    description = ["Найти максимальный поток из первой вершины во вторую"]
)
class MaxFlow : GraphParameter(), Runnable {
    @Parameters(description = [DESCRIPTION_VERTEX])
    var firstVertex: Int = -1

    @Parameters(description = [DESCRIPTION_VERTEX])
    var secondVertex: Int = -1

    override fun run() {
        printMaxFlow(graph, firstVertex, secondVertex)
    }
}

@Command(
    name = "ecycle",
    description = ["Найти Эйлеров цикл/цепь в графе"]
)
class ECycle : GraphParameter(), Runnable {
    @Option(names = ["-c", "--chain"], description = ["Цепь"])
    var chain: Int = 0

    @Option(names = ["-m", "--multistart"], description = ["Цепь"])
    var multistart: Boolean = false

    override fun run() {
        printECycle(graph)
    }
}

@Command(
    name = "hcycle",
    description = ["Найти Гамильтонов цикл/цепь в графе"]
)
class HCycle : GraphParameter(), Runnable {

    @Option(names = ["-t", "--tree"], description = [DESCRIPTION_WEIGHT])
    var tree: Boolean = false

    override fun run() {
        printHCycle(graph, tree)
    }
}

@Command(
    name = "redo",
    description = ["Изменить или добавить веса. Не создаёт новые рёбра/дуги"]
)
class Redo : GraphParameter(), Runnable {
    @Option(
        names = ["-r", "--rounding-enable"],
        description = ["Включает округление до целого числа, если в результате вычисления выражения получилось дробное число"]
    )
    var isRoundingEnable: Boolean = false

    @Parameters(description = [DESCRIPTION_WEIGHT])
    var expression: String? = null

    override fun run() {
        val result = Expression(expression)
        redo(graph) { u, v, w ->
            val res = result.with("u", u.toString())
                .and("v", v.toString())
                .and("w", w.toString())
                .eval()
            if (isRoundingEnable)
                res.toDouble().roundToInt()
            else res.intValueExact()
        }
        gfs.writeAllToFile()
    }
}


private fun printPath(g: Graph, u: Int, v: Int) {
    g.checkCorrectVer(u, v)
    val path = route(g, u, v)
    if (path.isEmpty())
        println("Вершина $v не достижима из $u")
    else
        println(path.joinToString(" "))
}

private fun printMaxFlow(g: Graph, u: Int, v: Int) {
    g.checkCorrectVer(u, v)
    val maxFlow = maxFlow(g, u, v)
    if (maxFlow.value == 0)
        print("Вершина $v не достижима из $u")
    else {
        println("Величина максимального потока из $u в $v:  ${maxFlow.value}")
        println("Список увеличивающих путей:")
        for (path in maxFlow.flow) {
            print("(${path.value})  ")
            println(path.path.joinToString(" -> "))
        }
    }
}

private fun printHCycle(g: Graph, tree: Boolean) {
    if (vertexConnectivity(g) < 2) {
        println("В данном графе не существует Гамильтонового цикла")
        return
    }
    try {
        println(hamiltonCycle(g, 1, true, tree))
    } catch (ex: IllegalArgumentException) {
        println(ex.message)
    }
}

private fun printHChain(g: Graph, start: Int, tree: Boolean) {
    require(start >= 0 && start < g.numVer) { "Некорректный ввод вершины для старта" }
    if (start == 0) {
        // Мультистарт
        var cc = 0
        for (i in 1..g.numVer) try {
            hamiltonCycle(g, i, cycle = false, tree = false)
            break
        } catch (e: IllegalArgumentException) {
            cc++
        }
        if (cc == g.numVer) println("Граф не содержит Гамильтоновой цепи!")
        else println(
            hamiltonCycle(g, cc + 1, false, tree)
        )
    } else  // Старт с заданной вершины
        try {
            println(hamiltonCycle(g, start, false, tree))
        } catch (e: IllegalArgumentException) {
            println("Граф не содержит Гамильтоновой цепи от заданной вершины!")
        }
}

private fun printECycle(g: Graph) {
    if (vertexConnectivity(g) < 2) {
        println("В данном графе не существует Эйлерова цикла!")
        return
    }
    var numEvenVer = 0
    for (i in g.numVer - 1 downTo 0) if (g.deg(i) % 2 === 0) numEvenVer++
    if (g.numVer - numEvenVer === 0) println(
        "ЭЦ:  " + eulerCycle(
            g,
            1
        )
    ) else println("В данном графе не существует Эйлерова цикла!")
}

private fun printEChain(g: Graph, start: Int) {
    var numEvenVer = 0
    var oddVer = 0
    for (i in g.numVer - 1 downTo 0) if (g.deg(i) % 2 === 0) numEvenVer++ else oddVer = i
    if (g.numVer - numEvenVer === 2) {
        println("Существует Эйлерова цепь:")
        if (start == 0) println(
            eulerCycle(
                g,
                oddVer + 1
            )
        ) else if (g.deg(start) % 2 === 1) println(
            eulerCycle(
                g,
                start
            )
        ) else println("В данном графе нет Эйлеровой цепи от заданной вершины!")
    } else println("В данном графе не существует Эйлеровой цепи!")
}
