package console

import picocli.CommandLine.*
import storage.SetFileGraph
import java.io.File

@Command(
    name = "gp",
    version = ["GP 0.2.0"],
    subcommands = [New::class, Show::class, Remove::class, Connectivity::class, Planarity::class,
        Isomorphism::class, Path::class, MaxFlow::class, ECycle::class, HCycle::class, Redo::class, HelpCommand::class],
    description = ["sdofigjsdopifgj"],
    mixinStandardHelpOptions = true
)
class BaseCommand

open class GraphParameter {
    @Parameters(index = "0", description = [DESCRIPTION_GRAPH], converter = [GraphConverter::class])
    lateinit var graphName: String
}

val gfs = SetFileGraph(File("GraphData"))

@Command(
    name = "new",
    description = ["Создать и сохранить граф"]
)
class New : Runnable {
    override fun run() {
        TODO("Not yet implemented")
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
    }
}

@Command(
    name = "remove",
    description = ["Удалить граф"]
)
class Remove : GraphParameter(), Runnable {
    override fun run() {
        TODO("Not yet implemented")
    }
}

@Command(
    name = "connectivity",
    description = ["Вычислить вершинную и рёберную k-связность"]
)
class Connectivity : GraphParameter(), Runnable {
    override fun run() {
        TODO("Not yet implemented")
    }
}

@Command(
    name = "planarity",
    description = ["Проверить граф на планарность"]
)
class Planarity : GraphParameter(), Runnable {
    override fun run() {
        TODO("Not yet implemented")
    }
}

@Command(
    name = "isomorphism",
    description = ["Проверить два графа на изоморфизм"]
)
class Isomorphism : Runnable {
    override fun run() {
        TODO("Not yet implemented")
    }
}

@Command(
    name = "path",
    description = ["Найти кратчайший путь между заданными вершинами"]
)
class Path : GraphParameter(), Runnable {
    override fun run() {
        TODO("Not yet implemented")
    }
}

@Command(
    name = "maxflow",
    description = ["Найти максимальный поток из первой вершины во вторую"]
)
class MaxFlow : GraphParameter(), Runnable {
    override fun run() {
        TODO("Not yet implemented")
    }
}

@Command(
    name = "ecycle",
    description = ["Найти Эйлеров цикл/цепь в графе"]
)
class ECycle : GraphParameter(), Runnable {
    override fun run() {
        TODO("Not yet implemented")
    }
}

@Command(
    name = "hcycle",
    description = ["Найти Гамильтонов цикл/цепь в графе"]
)
class HCycle : GraphParameter(), Runnable {
    @Option(names = ["-t", "--tree"], description = [DESCRIPTION_WEIGHT])
    var tree: Boolean = false

    @Option(names = ["-c", "--chain"], description = ["Цепь"])
    var chain: Int = 0

    override fun run() {
        TODO("Not yet implemented")
    }
}

@Command(
    name = "redo",
    description = ["Изменить или добавить веса. Не создаёт новые рёбра/дуги"]
)
class Redo : Runnable {
    @Parameters(index = "0", description = [DESCRIPTION_GRAPH])
    lateinit var graphName: String

    @Option(names = ["-e", "--expression"], description = [DESCRIPTION_WEIGHT])
    var expression: String? = null

    override fun run() {
        println("Граф $graphName")
        if (expression == null)
            println("Пусто")
        else println(expression)
    }
}
