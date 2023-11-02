package algorithm

import algorithm.thesis.Event
import graphs.Graph
import storage.Generator
import storage.SetFileGraph
import utils.*
import java.io.File
import java.util.concurrent.TimeUnit

typealias Driver = (Event) -> Unit

open class BenchmarkTestBase {

    protected inner class SfgBenchmark(
        dataPath: String,
        private val expId: String,
        private val renewData: Boolean = false
    ) {
        private val sfg: SetFileGraph = SetFileGraph(File(dataPath))
        private var isFirstCall = true

        init {
            if (renewData) sfg.clear()
        }

        fun printMeasure(
            expCount: Int,
            rowId: String,
            generator: Generator,
            timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
            timedFunc: (Graph, Driver) -> Unit
        ) {
            if (isFirstCall) {
                println(
                    "$expId;ExpCount;Mean;Mode;Median;Max;Min;" +
                            "Mean_RecT;Mode_RecT;Median_RecT;Max_RecT;Min_RecT;" +
                            "Mean_RecC;Mode_RecC;Median_RecC;Max_RecC;Min_RecC;" +
                            "Mean_tree;Mode_tree;Median_tree;Max_tree;Min_tree;"
                )
                isFirstCall = false
            }
            generator.except = sfg.values
            val graphGetter = sfgGetter(rowId, renewData, generator, sfg)

            val times = timedEvents(expCount, graphGetter, timeUnit, timedFunc)

            val startedOn = times[Event.ON]!!.map { it.single() }
            val allTime = times[Event.OFF]!!.zip(startedOn).map { it.first.single() - it.second }
            val recordTimes = times[Event.REC]!!.zip(startedOn).map { it.first.last() - it.second }
            val recordCounts = times[Event.REC]!!.map { it.size.toLong() }
            val deeps = times[Event.EXE]!!.map { it.count().toLong() }
            println(
                "$rowId;$expCount;" +
                        "${allTime.mean()};${allTime.mode()};${allTime.median()};${allTime.max()};${allTime.min()};" +
                        "${recordTimes.mean()};${recordTimes.mode()};${recordTimes.median()};${recordTimes.max()};${recordTimes.min()};" +
                        "${recordCounts.mean()};${recordCounts.mode()};${recordCounts.median()};${recordCounts.max()};${recordCounts.min()};" +
                        "${deeps.mean()};${deeps.mode()};${deeps.median()};${deeps.max()};${deeps.min()};"
            )
        }
    }

    protected fun timedEvents(
        expCount: Int,
        graphGetter: (Int) -> Graph,
        timeUnit: TimeUnit,
        timedFunc: (Graph, Driver) -> Unit
    ): Map<Event, List<List<Long>>> {

        val result = Event.values().associateWith { mutableListOf<List<Long>>() }

        repeat(expCount) { numEx ->
            val graph = graphGetter(numEx)

            val timersByEvent = Event.values().associateWith { Timestamps(timeUnit) }
            val driver: Driver = {
                timersByEvent[it]!!.make()
            }
            timedFunc(graph, driver)

            timersByEvent.forEach { (event, timer) ->
                result[event]!!.add(timer.times)
            }
        }
        return result
    }

    private fun sfgGetter(
        id: String,
        isNewDataGen: Boolean,
        generator: Generator,
        sfg: SetFileGraph
    ): (Int) -> Graph = { numExp ->
        val name = "${id}_${numExp}"
        val graph =
            if (isNewDataGen) {
                generator.name = name
                generator.build().apply {
                    sfg.add(this)
                    sfg.push(true)
                    sfg.clear()
                }
            } else sfg[name]
        graph
    }
}
