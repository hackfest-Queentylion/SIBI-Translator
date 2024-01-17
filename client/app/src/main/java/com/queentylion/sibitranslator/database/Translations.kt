package com.queentylion.sibitranslator.database

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.queentylion.sibitranslator.types.Translation

class TranslationsRepository(
    private val database: DatabaseReference
) {

    private val TAG = "TranslationsRepository"

    fun writeNewTranslations(userId: String? = null, text: String) {
        val key = database.child("translations").push().key

        if (key == null) {
            Log.w(TAG, "Couldn't get push key for translations")
        } else {
            val translation = Translation(key, text)
            val translationValues = translation.toMap()

            val childUpdates = hashMapOf<String, Any>(
                "/translations/$key" to translationValues,
                "/user-translations/$userId/$key" to translationValues
            )

            database.updateChildren(childUpdates)
        }

    }

    fun toggleTranslationsIsFavorite(translationId: String) {

        // Update translations
        val translationRef = database
            .child("translations")
            .child(translationId)

        translationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val translation = snapshot.getValue(Translation::class.java)

                // Toggle the isFavorite property
                val currentIsFavorite = translation?.isFavorite
                translationRef.child("isFavorite").setValue(!currentIsFavorite!!)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TranslationsVM", "Database Error: $error")
            }
        })

    }

    fun toggleUserTranslationsIsFavorite(userId: String, translationId: String) {
        val userTranslationRef = database
            .child("user-translations")
            .child(userId)
            .child(translationId)

        userTranslationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val translation = snapshot.getValue(Translation::class.java)

                // Toggle the isFavorite property
                val currentIsFavorite = translation?.isFavorite
                userTranslationRef.child("isFavorite").setValue(!currentIsFavorite!!)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TranslationsVM", "Database Error: $error")
            }
        })
    }
}