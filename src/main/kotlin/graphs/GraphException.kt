package graphs

open class GraphException(s: String) : RuntimeException(s)

val ERR_SIZE_EM = "The adjacency matrix of a graph must not be empty."
val ERR_SIZE_SQ = "The adjacency matrix of the graph must be square."

inline fun requireG(
    value: Boolean,
    lazyMessage: () -> String = { "Failed requirement." }
) {
    if (!value) throw GraphException(lazyMessage())
}
