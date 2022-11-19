package graphs

fun cloneArray(src: Array<Array<Int?>>): Array<Array<Int?>> {
    val target = Array<Array<Int?>>(src.size) { arrayOfNulls(src[0].size) }
    for (i in src.indices)
        System.arraycopy(src[i], 0, target[i], 0, src[i].size)
    return target
}

fun <T> checkSize(srcData: List<List<T>>) {
    requireG(srcData.isNotEmpty()) { ERR_SIZE_EM }
    srcData.forEach {
        requireG(srcData.size == it.size) { ERR_SIZE_SQ }
    }
}

fun <T> checkSize(srcData: Array<Array<T>>) {
    checkSize(srcData.map { it.asList() })
}


fun isOriented(data: List<List<Int?>>): Boolean {
    for (i in data.indices)
        for (j in 0 until i)
            if (data[j][i] != data[i][j])
                return true
    return false
}
