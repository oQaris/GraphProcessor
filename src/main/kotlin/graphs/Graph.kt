package graphs

interface Graph {
    /**
     * Имя графа
     */
    val name: String

    /**
     * Если граф ориентированный - true, иначе - false
     */
    var oriented: Boolean

    /**
     * Количество вершин
     */
    val numVer: Int

    /**
     * Количество рёбер
     */
    val numEdg: Int

    /**
     * Сумма весов всех рёбер в графе
     */
    val sumWeights: Int

    /**
     * Добавить указанное количество вершин в граф.
     *
     * @param count Количество вершин для добавления
     * @throws GraphException Если число вершин меньше 1
     */
    fun addVer(count: Int)

    /**
     * Добавить ребро в граф, либо заменить вес существующего.
     *
     * @param u Начальная вершина добавляемого ребра
     * @param v Конечная вершина добавляемого ребра
     * @param weight Вес добавляемого ребра (1 по-умолчанию)
     */
    fun addEdg(u: Int, v: Int, weight: Int = 1)

    /**
     * Добавить ребро в граф, либо заменить вес существующего.
     *
     * @param edge Пара вершин, из которых состоит добавляемое ребро
     */
    fun addEdg(edge: Edge) = addEdg(edge.first, edge.second, edge.weight)

    /**
     * Получить вес ребра UV.
     *
     * @param u Номер начальной вершины
     * @param v Номер конечной вершины
     * @return null при отсутствии ребра UV
     */
    fun getWeightEdg(u: Int, v: Int): Int?

    /**
     * Получить вес ребра.
     *
     * @param edge Пара смежных вершин
     * @return null при отсутствии ребра UV
     */
    fun getWeightEdg(edge: Pair<Int, Int>) = getWeightEdg(edge.first, edge.second)

    /**
     * Установить вес ребра UV.
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
     * Установить вес ребра.
     *
     * @param edge Пара смежных вершин и вес полученного ребра
     * @throws GraphException При отсутствии ребра edge
     */
    fun setWeightEdg(edge: Edge) = setWeightEdg(edge.first, edge.second, edge.weight)

    /**
     * Удалить инцидентные вершине рёбра из графа.
     *
     * @param ver Вершина, удаляемая из графа
     * @throws GraphException При некорректном значении номера вершины ver
     */
    fun remVer(ver: Int)

    /**
     * Удалить ребро из графа.
     *
     * @param u Начальная вершина удаляемого ребра
     * @param v Конечная вершина удаляемого ребра
     * @throws GraphException При некорректных значениях номеров вершин u или v
     */
    fun remEdg(u: Int, v: Int)

    /**
     * Удалить ребро из графа.
     *
     * @param edge Пара вершин, из которых состоит удаляемое ребро
     * @throws GraphException При отсутствии ребра edge
     */
    fun remEdg(edge: Pair<Int, Int>) = remEdg(edge.first, edge.second)

    /**
     * Удалить ребро из графа.
     *
     * @param edge Удаляемое ребро (вес не учитывается)
     * @throws GraphException При отсутствии ребра edge
     */
    fun remEdg(edge: Edge) = remEdg(edge.first, edge.second)

    /**
     * Подсчитать степень входа/выхода заданной вершины.
     *
     * @param ver   Номер вершины, для которой вычисляется её степень
     * @param isOut Определяет степень входа/выхода (isOut=true: deg-, isOut=false: deg+)
     * @throws GraphException При некорректном значении номера вершины ver
     */
    fun deg(ver: Int, isOut: Boolean): Int

    /**
     * Узнать суммарную степень заданной вершины.
     *
     * @param ver Номер вершины, для которой вычисляется её степень
     * @throws GraphException При некорректном значении номера вершины ver
     */
    fun deg(ver: Int): Int {
        return if (!oriented) deg(ver, true) else deg(ver, true) + deg(ver, false)
    }

    /**
     * Проверить смежность двух вершин.
     *
     * @param u Номер первой вершины
     * @param v Номер второй вершины
     * @throws GraphException При некорректных значениях номеров вершин ver1 или ver2
     */
    fun isCom(u: Int, v: Int): Boolean {
        return getWeightEdg(u, v) != null
    }

    /**
     * Проверить существование ребра в графе.
     *
     * @param edge Пара вершин, для которых проверяется смежность
     * @throws GraphException При некорректных значениях номеров вершин ver1 или ver2
     */
    fun isCom(edge: Pair<Int, Int>) = isCom(edge.first, edge.second)

    /**
     * Получить список смежных вершин.
     *
     * @param ver Вершина, относительно которой вычисляются смежные
     * @return Список int, номеров вершин, смежных с данной
     * @throws GraphException При некорректном значении номера вершины ver
     */
    fun com(ver: Int): List<Int>

    /**
     * Получить список рёбер графа (связанных вершин).
     *
     * @return Список рёбер в графе
     */
    fun getEdges(): List<Edge>

    /**
     * Получить список номеров вершин графа.
     *
     * @return Список вершин в графе
     */
    fun getVertices(): List<Int> {
        return (0 until numVer).toList()
    }

    /**
     * Создать глубокую копию графа (той же имплементации).
     *
     * @return Копия исходного графа
     */
    fun clone(): Graph

    /**
     * Получить все возможные пары вершин в графе (различны для обычного графа и орграфа).
     *
     * @return Список пар всевозможных вершин графа
     */
    fun getPairVer(): List<Pair<Int, Int>> {
        val n = numVer
        val out = ArrayList<Pair<Int, Int>>(n * n - n)
        for (i in 0 until n)
            for (j in (if (oriented) 0 else i + 1) until n)
                if (i != j) out.add(i to j)
        return out
    }

    /**
     * Проверяет, все ли вершины из набора содержатся в данном графе.
     *
     * @param vertices Набор вершин для проверки их допустимости в данном графе
     * @throws GraphException Если хотя бы одна вершина из набора не содержится в графе
     */
    fun checkCorrectVer(vararg vertices: Int) {
        for (ver in vertices) requireG(ver in 0 until numVer) { "Некорректная вершина: $ver" }
    }
}
