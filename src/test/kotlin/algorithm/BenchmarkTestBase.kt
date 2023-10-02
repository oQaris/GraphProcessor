package algorithm

import algorithm.thesis.Event
import graphs.Graph
import storage.Generator
import storage.SetFileGraph
import utils.Timestamps
import java.util.concurrent.TimeUnit

typealias Driver = (Event) -> Unit

open class BenchmarkTestBase {

    protected fun timedEvents(
        expCount: Int,
        graphGetter: (Int) -> Graph,
        timedFunc: (Graph, Driver) -> Unit
    ): Map<Event, List<List<Long>>> {

        val result = Event.values().associateWith { mutableListOf<List<Long>>() }

        repeat(expCount) { numEx ->
            val graph = graphGetter(numEx)

            val timersByEvent = Event.values().associateWith { Timestamps(TimeUnit.SECONDS) }
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

    fun sfgGetter(
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
