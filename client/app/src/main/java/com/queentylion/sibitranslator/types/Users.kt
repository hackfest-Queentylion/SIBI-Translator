package com.queentylion.sibitranslator.types

import com.google.firebase.database.Exclude

data class Users(
    private val userId: String? = "",
    private val username: String? = "",
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "username" to username
        )
    }
}
