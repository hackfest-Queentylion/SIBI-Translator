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
import com.queentylion.sibitranslator.types.Translation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.queentylion.sibitranslator.Resource
import com.queentylion.sibitranslator.data.gesture.ApiService
import com.queentylion.sibitranslator.data.gesture.PostData
import com.queentylion.sibitranslator.data.gesture.ResponseData
import com.queentylion.sibitranslator.domain.model.ExportModel
import com.queentylion.sibitranslator.domain.repository.ExportRepository
import com.queentylion.sibitranslator.presentation.state.FileExportState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@HiltViewModel
class TranslationViewModel @Inject constructor(
    private val exportRepository: ExportRepository
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
//
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

    private val maxSize = 30

    val dynamicArrayOfFlex: MutableList<Map<String, Int>> = mutableListOf()


    fun beginStreamingGesture(
        data : IntArray,
    ) {
//        isStreamingGesture = !isStreamingGesture
//        dynamicArrayOfFlex.clear()

        viewModelScope.launch {
//            while (isStreamingGesture) {
//                delay(70)
                // Your POST request logic here
                if (dynamicArrayOfFlex.size == 30 ) {
//                    val postData = PostData(
//                        dynamicArrayOfFlex.toList()
//                    )
                    executePostRequest( dynamicArrayOfFlex.toList())
                    dynamicArrayOfFlex.clear()
                } else if (dynamicArrayOfFlex.size < 30) {
                    val map = mutableMapOf<String, Int>()
                    data.forEachIndexed { index, value ->
                        map[index.toString()] = value
                    }
                    dynamicArrayOfFlex.add(map)
                }
                // Delay for 3 seconds
//            }
        }
//        generateExportFile()

//        viewModelScope.launch {
//            while (isStreamingGesture) {
//                generateExportFile()
//                delay(40000)
//            }
//        }

    }

    fun executePostRequest(postData: List<Map<String, Int>>) {
        val url = "http://194.61.28.18/csv/save-to-excel"

        val mediaType = "application/json".toMediaType()

        val gson = Gson()
        val requestBody = gson.toJson(postData).toRequestBody(mediaType)
        // Convert requestBody to a String
        val requestBodyString = gson.toJson(postData)

        // Log the request body
        Log.d("Request", requestBodyString)


        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        val thread = Thread {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                Log.d("Response", responseBody ?: "Response body is null")
//                    return@Thread predictions
            } catch (e:Exception) {
                e.printStackTrace()
            }
        }
        thread.start()

//        viewModelScope.launch {
//            try {
//                val response = client.newCall(request).execute()
//            } catch (e:Exception) {
//                e.printStackTrace()
//            }
//        }
    }

    fun endStreamingGesture(){
        isStreamingGesture = !isStreamingGesture
    }

    fun getSentencesString(): String {
        return currentSentence.joinToString(separator = " ")
    }

    // EXPORT CSV

    private var collectingJob: Job? = null

    private var exportList = mutableListOf<ExportModel>()

    var collectedDataAmount by mutableStateOf(0)
        private set

    var fileExportState by mutableStateOf(FileExportState())
        private set

    init {
        collectingJob = viewModelScope.launch {
            while (true){
                delay(4000)
                collectedDataAmount += 160
                exportList.addAll(
                    listOf(
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                        ExportModel(Random.nextFloat()*10000,System.currentTimeMillis()),
                    )
                )
            }
        }
    }

    fun generateExportFile(){
        collectingJob?.cancel()
        fileExportState = fileExportState.copy(isGeneratingLoading = true)
        exportRepository.startExportData(
            exportList.toList()
        ).onEach { pathInfo ->
            when(pathInfo){
                is Resource.Success -> {
                    fileExportState = fileExportState.copy(
                        isSharedDataReady = true,
                        isGeneratingLoading = false,
                        shareDataUri = pathInfo.data.path,
                        generatingProgress = 100
                    )
                }
                is Resource.Loading ->{
                    pathInfo.data?.let {
                        fileExportState = fileExportState.copy(
                            generatingProgress = pathInfo.data.progressPercentage
                        )
                    }
                }
                is Resource.Error -> {
                    fileExportState = fileExportState.copy(
                        failedGenerating = true,
                        isGeneratingLoading = false
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onShareDataClick(){
        fileExportState = fileExportState.copy(isShareDataClicked = true)
    }

    fun onShareDataOpen(){
        fileExportState = fileExportState.copy(isShareDataClicked = false)
    }


}