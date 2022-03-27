package graphs

fun cloneArray(src: Array<Array<Int?>>): Array<Array<Int?>> {
    val target = Array<Array<Int?>>(src.size) { arrayOfNulls(src[0].size) }
    for (i in src.indices)
        System.arraycopy(src[i], 0, target[i], 0, src[i].size)
    return target
}

fun <T> checkSize(srcData: Array<Array<T>>) {
    checkSize(srcData.asList().map { it.asList() })
}

fun <T> checkSize(srcData: List<List<T>>) {
    require(srcData.isNotEmpty())
    srcData.forEach {
        require(srcData.size == it.size)
    }
}