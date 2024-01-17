package com.queentylion.sibitranslator.types

import com.google.firebase.database.Exclude

data class Translation(
    val translationId: String = "",
    val translation: String = "",
    @JvmField var isFavorite: Boolean = false
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "translationId" to translationId,
            "translation" to translation,
            "isFavorite" to isFavorite
        )
    }
}
