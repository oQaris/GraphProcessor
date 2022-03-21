package graphs

class GraphException(s: String) : RuntimeException(s) {

    companion object {
        val ERR_SIZE_EM = "The adjacency matrix of a graph must not be empty."
        val ERR_SIZE_SQ = "The adjacency matrix of the graph must be square."
    }
}
