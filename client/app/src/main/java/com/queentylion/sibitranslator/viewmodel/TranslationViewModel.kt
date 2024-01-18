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
import com.google.gson.Gson
import com.queentylion.sibitranslator.data.gesture.ApiService
import com.queentylion.sibitranslator.data.gesture.PostData
import com.queentylion.sibitranslator.data.gesture.ResponseData
import retrofit2.Response
import com.queentylion.sibitranslator.types.Translation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

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

//    private fun createApiService(): ApiService {
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://your-api-base-url.com/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        return retrofit.create(ApiService::class.java)
//    }

//    private suspend fun performPostRequest(
//        apiService: ApiService,
//        value1: IntArray,
//        onResult: (Response<ResponseData>) -> Unit
//    ) {
//        try {
//            val postData = PostData(instances = listOf(value1.toList()))
//            val response = apiService.postData(postData)
//            onResult(response)
//        } catch (e: Exception) {
//            // Handle network or other exceptions
//        }
//    }
    var currentSentence by mutableStateOf<MutableList<String?>>(mutableListOf())
        private set

    var isStreamingGesture = false

    fun beginStreamingGesture(
        data : IntArray
    ) {
        currentSentence.clear()
        isStreamingGesture = !isStreamingGesture
        viewModelScope.launch {
            while (isStreamingGesture) {
                // Your POST request logic here
                val postData = PostData(
                    listOf(data.toList())
                )
                val response = executePostRequest("267809006279","3932073483252531200", "ya29.a0AfB_byC7ZeRWHhKxWmTlu1jEdaMpsEcee0RWJoteercRlpmALaKXr7LtiMOMjuvtluanpnS4R8RUZCoBC56h0Qb7E8bdhSflB7oqQDm8nCFFpF1bokM66ourkFrKeYgigqALsWOZcSXeLjWEpm72eDbt62q4gOR9ghXYLtLouZxxljIjiN-gFbhx3d5cJ1LvWXzSCrqZijY_PEc4Z4wFQy7e8a7tctTBy3xvfME8ppAG7fYLPb7xF8E42y5qNWOjKSOY1YlP88T9oz8_kBGAF0-Stv3pSW-cbAfvWuyrsA8TfvjEcXYr2JuADJCXxitfMhTmP_oreX2s4zSr5CmKgnEH9G9crqSWqzGjuM7Qm4YGGqwr3OxsSz2uId_m5a6-_RsbuaLwgz7SxrqllvgFa9ZGvJLmygaCgYKAd0SARMSFQHGX2Miw2m1HWADLpXYmcPTtekEOw0421", postData)
                currentSentence.add(response[0])

                // Delay for 3 seconds
                delay(3000)
            }
        }
    }


    fun executePostRequest(projectID: String, endpointID: String, accessToken: String, postData: PostData): List<String> {
        val url = "https://asia-southeast2-aiplatform.googleapis.com/v1/projects/$projectID/locations/asia-southeast2/endpoints/$endpointID:predict"

        val mediaType = "application/json".toMediaType()

        val gson = Gson()
        val requestBody = gson.toJson(postData).toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val responseBody = response.body?.string()
            val predictions = parsePredictions(responseBody)
            return predictions
        }
        return listOf()
    }

    fun parsePredictions(responseBody: String?): List<String> {
        val predictionsList = mutableListOf<String>()

        responseBody?.let {
            try {
                val jsonObject = JSONObject(it)
                val predictionsArray = jsonObject.getJSONArray("predictions")

                for (i in 0 until predictionsArray.length()) {
                    val prediction = predictionsArray.getString(i)
                    predictionsList.add(prediction)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return predictionsList
    }

    fun endStreamingGesture(){
        isStreamingGesture = !isStreamingGesture
    }


}