package com.queentylion.sibitranslator.data.converter

data class GenerateInfo(
    val byteArray: ByteArray? = null,
    val progressPercentage:Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GenerateInfo

        if (byteArray != null) {
            if (other.byteArray == null) return false
            if (!byteArray.contentEquals(other.byteArray)) return false
        } else if (other.byteArray != null) return false
        return progressPercentage == other.progressPercentage
    }

    override fun hashCode(): Int {
        var result = byteArray?.contentHashCode() ?: 0
        result = 31 * result + progressPercentage
        return result
    }
}