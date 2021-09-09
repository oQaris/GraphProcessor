package storage

class Logger(var logging: Boolean = true) {
    fun i(info: String) {
        if (logging)
            println(info)
    }

    fun e(error: String) {
        if (logging)
            error(error)
    }
}
