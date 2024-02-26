package com.queentylion.sibitranslator.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.queentylion.sibitranslator.types.Translation
import kotlinx.coroutines.launch
import com.queentylion.sibitranslator.SignDetectionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.withContext


class TranslationViewModel(
) : ViewModel() {



    private val databaseReference: DatabaseReference = Firebase
        .database("https://sibi-translator-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .reference
    private val userId = Firebase.auth.currentUser?.uid

    private val _translations = MutableLiveData<List<Translation>>()
    val translations: LiveData<List<Translation>> = _translations

    private val detectionService = SignDetectionService()

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

    private var currentSentence by mutableStateOf<MutableList<String?>>(mutableListOf())
        private set

    val resultChannel = Channel<String>()

    var isStreamingGesture = false

    val dynamicArrayOfFlex: MutableList<IntArray> = mutableListOf()

    fun beginStreamingGesture(
        data : IntArray,
    ) {
        viewModelScope.launch {
            if (dynamicArrayOfFlex.size == 30 ) {
                val listOfLists: List<List<Int>> = dynamicArrayOfFlex.map { intArray ->
                    intArray.toList()
                }

                val predictionString = detectionService.transfer(listOfLists)

//                resultChannel.send(detectionService.transfer(listOfLists))
                withContext(Dispatchers.Main) {
                    currentSentence.add(predictionString)
                }
//                val result = resultChannel.trySend(predictionString)
//                if (result.isSuccess) {
//                    // Value was sent successfully
//                    println("Value sent successfully")
//                } else {
//                    // Sending failed
//                    println("Failed to send value: ${result.exceptionOrNull()}")
//                }
                dynamicArrayOfFlex.clear()
            } else if (dynamicArrayOfFlex.size < 30) {
                dynamicArrayOfFlex.add(data)
            }
        }

    }


    fun endStreamingGesture(){
        isStreamingGesture = !isStreamingGesture
    }


    suspend fun collectPartialResults(): String {
        val resultStringBuilder = StringBuilder()
        for (result in resultChannel) {
            resultStringBuilder.append("$result ")
        }
        return resultStringBuilder.toString().trim()
    }

    fun getSentencesString(): String {
        return currentSentence.joinToString(separator = " ")
    }

    fun clearSentences() {
        currentSentence.clear()
    }


}