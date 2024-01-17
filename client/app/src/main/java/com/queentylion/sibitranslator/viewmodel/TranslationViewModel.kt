package com.queentylion.sibitranslator.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.queentylion.sibitranslator.presentation.sign_in.UserData
import com.queentylion.sibitranslator.types.Translation

class TranslationViewModel(
) : ViewModel() {

    private val databaseReference: DatabaseReference = Firebase
        .database("https://sibi-translator-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .reference
    private val userId = Firebase.auth.currentUser?.uid

    private val _translations = MutableLiveData<List<Translation>>()
    val translations: LiveData<List<Translation>> = _translations

    init {
        if (userId != null) {
            loadTranslations()
        }
    }

    private fun loadTranslations(onResult: (List<Translation>) -> Unit) {
        databaseReference.child("user-translations").child(userId!!).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val translations =
                    snapshot.children.mapNotNull { it.getValue(Translation::class.java) }
                onResult(translations)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("loadTranslations", "Failed to read value.", error.toException())
            }
        })
    }

    private fun loadTranslations() {
        loadTranslations { data ->
            _translations.postValue(data)
        }
    }
}