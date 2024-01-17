package com.queentylion.sibitranslator.data

data class GloveResult(
    val flexDegree: IntArray,
    val connectionState: ConnectionState
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GloveResult

        if (!flexDegree.contentEquals(other.flexDegree)) return false
        return connectionState == other.connectionState
    }

    override fun hashCode(): Int {
        var result = flexDegree.contentHashCode()
        result = 31 * result + connectionState.hashCode()
        return result
    }
}