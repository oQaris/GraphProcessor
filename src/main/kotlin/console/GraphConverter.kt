package console

import graphs.Graph
import picocli.CommandLine

class GraphConverter : CommandLine.ITypeConverter<Graph> {
    override fun convert(value: String?): Graph {
        TODO("Not yet implemented")
    }
}
