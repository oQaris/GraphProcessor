package graphs

interface Graph {
    /**
     * Имя графа
     */
    var name: String

    /**
     * Если граф ориентированный, равна true, иначе - false
     */
    var oriented: Boolean

    /**
     * Количество вершин
     */
    var numVer: Int

    /**
     * Количество рёбер
     */
    var numEdg: Int

    /**
     * Добавить указанное количество вершин в граф
     *
     * @param count Количество вершин для добавления
     * @throws GraphException Если число вершин меньше 1
     */
    fun addVer(count: Int)

    /**
     * Добавить указанное количество вершин в граф
     *
     * @param u Начальная вершина добавляемого ребра
     * @param v Конечная вершина добавляемого ребра
     * @throws GraphException Если число вершин меньше 1
     */
    fun addEdg(u: Int, v: Int, weight: Int = 1)

    /**
     * Добавить указанное количество вершин в граф
     *
     * @param edge Пара вершин, из которых состоит добавляемое ребро
     * @param weight Вес добавляемого ребра (1 по-умолчанию)
     * @throws GraphException Если число вершин меньше 1
     */
    fun addEdg(edge: Pair<Int, Int>, weight: Int = 1) = addEdg(edge.first, edge.second, weight)

    /**
     * Получить вес ребра UV
     *
     * @param u Номер начальной вершины
     * @param v Номер конечной вершины
     * @return null при отсутствии ребра UV
     */
    fun getWeightEdg(u: Int, v: Int): Int?

    /**
     * Получить вес ребра
     *
     * @param edge Пара смежных вершин
     * @return null при отсутствии ребра UV
     */
    fun getWeightEdg(edge: Pair<Int, Int>) = getWeightEdg(edge.first, edge.second)

    /**
     * Получить вес ребра UV
     *
     * @param u Номер начальной вершины
     * @param v Номер конечной вершины
     * @param weight Вес добавляемого ребра
     * @throws GraphException При отсутствии ребра UV
     */
    fun setWeightEdg(u: Int, v: Int, weight: Int) {
        if (isCom(u, v))
            addEdg(u, v, weight)
        else throw GraphException("Отсутствует ребро UV")
    }

    /**
     * Получить вес ребра
     *
     * @param edge Пара смежных вершин
     * @param weight Вес добавляемого ребра
     * @throws GraphException При отсутствии ребра UV
     */
    fun setWeightEdg(edge: Pair<Int, Int>, weight: Int) = setWeightEdg(edge.first, edge.second, weight)

    /**
     * Полностью удаляет вершину и инцидентные ей рёбра из графа
     *
     * @param ver Вершина, удяляемая из графа
     * @throws GraphException При некорректном значении номера вершины ver
     */
    fun remVer(ver: Int)

    /**
     * Удаляет ребро из графа
     *
     * @param u Начальная вершина удаляемого ребра
     * @param v Конечная вершина удаляемого ребра
     * @throws GraphException При некорректных значениях номеров вершин u или v
     */
    fun remEdg(u: Int, v: Int)

    /**
     * Удаляет ребро из графа
     *
     * @param edge Пара вершин, из которых состоит удаляемое ребро
     * @throws GraphException При некорректных значениях номеров вершин u или v
     */
    fun remEdg(edge: Pair<Int, Int>) = remEdg(edge.first, edge.second)

    /**
     * Узнать степень входа/выхода заданной вершины
     *
     * @param ver   Номер вершины, для которой вычисляется её степень
     * @param isOut Какую степень считать? (isOut=true: deg-, isOut=false: deg+)
     * @throws GraphException При некорректном значении номера вершины ver
     */
    fun deg(ver: Int, isOut: Boolean): Int

    /**
     * Узнать суммарную степень заданной вершины
     *
     * @param ver Номер вершины, для которой вычисляется её степень
     * @throws GraphException При некорректном значении номера вершины ver
     */
    fun deg(ver: Int): Int {
        return if (!oriented) deg(ver, true) else deg(ver, true) + deg(ver, false)
    }

    /**
     * Проверяет смежность двух вершин
     *
     * @param u Номер первой вершины
     * @param v Номер второй вершины
     * @throws GraphException При некорректных значениях номеров вершин ver1 или ver2
     */
    fun isCom(u: Int, v: Int): Boolean {
        return getWeightEdg(u, v) != null
    }

    /**
     * Проверяет существование ребра в графе
     *
     * @param edge Пара вершин, для которых проверяется смежность
     * @throws GraphException При некорректных значениях номеров вершин ver1 или ver2
     */
    fun isCom(edge: Pair<Int, Int>) = isCom(edge.first, edge.second)

    /**
     * Получить массив смежных вершин
     *
     * @param ver Вершина, относительно которой вычисляются смежные
     * @return Одномерный массив int, номеров вершин, смежных с данной
     * @throws GraphException При некорректном значении номера вершины ver
     */
    fun com(ver: Int): MutableList<Int>

    /**
     * Получить пары смежных вершин (рёбра) графа
     *
     * @return Список пар смежных вершин графа
     */
    fun getEdges(): MutableList<Pair<Int, Int>>

    /**
     * Получить все возможные пары вершин в графе (различны для обычного графа и орграфа).
     *
     * @return Список пар всевозможных вершин графа.
     */
    fun getPairVer(): MutableList<Pair<Int, Int>> {
        val n = numVer
        val out = ArrayList<Pair<Int, Int>>(n * n - n)
        for (i in 0 until n) for (j in (if (oriented) 0 else i + 1) until n) if (i != j) out.add(i to j)
        return out
    }

    /**
     * Проверяет, все ли вершины из набора содержатся в данном графе
     *
     * @param vertex Набор вершин для проверки их допустимости в данном графе
     * @throws GraphException Если хотя бы одна вершина из набора не содержится в графе
     */
    fun checkCorrectVer(vararg vertex: Int) {
        for (ver in vertex) require(ver in 0 until numVer) { "Некорректная вершина!" }
    }
}
