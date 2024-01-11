package com.queentylion.sibitranslator

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.api.gax.rpc.ClientStream
import com.google.api.gax.rpc.ResponseObserver
import com.google.api.gax.rpc.StreamController
import com.google.cloud.speech.v1.RecognitionConfig
import com.google.cloud.speech.v1.SpeechClient
import com.google.cloud.speech.v1.SpeechRecognitionAlternative
import com.google.cloud.speech.v1.StreamingRecognitionConfig
import com.google.cloud.speech.v1.StreamingRecognitionResult
import com.google.cloud.speech.v1.StreamingRecognizeRequest
import com.google.cloud.speech.v1.StreamingRecognizeResponse
import com.google.protobuf.ByteString
import kotlin.jvm.Throws
import android.Manifest

class TranslatorViewModel(@SuppressLint("StaticFieldLeak") private val context: Context)
    : ViewModel() {
    var translatedText by mutableStateOf("Start Speaking")
    private set

    @Throws(Exception::class)
    fun speechToText() {
        var responseObserver: ResponseObserver<StreamingRecognizeResponse>? = null
        try {
            SpeechClient.create().use { client ->
                responseObserver = object: ResponseObserver<StreamingRecognizeResponse> {
                    var responses: ArrayList<StreamingRecognizeResponse> =
                        ArrayList()

                    override fun onStart(controller: StreamController) {}
                    override fun onResponse(response: StreamingRecognizeResponse) {
                        val results = response.resultsList
                        if (results.isNotEmpty()) {
                            val result = results[0]
                            if (result.isFinal) {
                                val alternative = result.alternativesList[0]
                                translatedText += alternative
                            }
                        }
                    }

                    override fun onComplete() {
                        for (response in responses) {
                            val result: StreamingRecognitionResult =
                                response.getResults(0)
                            val alternative: SpeechRecognitionAlternative =
                                result.alternativesList[0]
                            System.out.printf(
                                "Transcript : %s\n",
                                alternative.transcript
                            )
                        }
                    }

                    override fun onError(t: Throwable) {
                        println(t)
                    }
                }
                val clientStream: ClientStream<StreamingRecognizeRequest> =
                    client.streamingRecognizeCallable().splitCall(responseObserver)
                val recognitionConfig: RecognitionConfig = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                    .setLanguageCode("en-US")
                    .setSampleRateHertz(16000)
                    .build()
                val streamingRecognitionConfig: StreamingRecognitionConfig =
                    StreamingRecognitionConfig.newBuilder().setConfig(recognitionConfig).build()
                var request: StreamingRecognizeRequest = StreamingRecognizeRequest.newBuilder()
                    .setStreamingConfig(streamingRecognitionConfig)
                    .build() // The first request in a streaming call has to be a config
                clientStream.send(request)
                // SampleRate:16000Hz, SampleSizeInBits: 16, Number of channels: 1, Signed: true,
                // bigEndian: false

                val sampleRate = 16000 // 16000 Hz
                val channelConfig = AudioFormat.CHANNEL_IN_MONO
                val audioFormat = AudioFormat.ENCODING_PCM_16BIT
                val bufferSize = AudioRecord.getMinBufferSize(
                    sampleRate,
                    channelConfig,
                    audioFormat
                )

                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                    ) {
                    // Permission is not granted
                    return
                }
                val audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )

                audioRecord.startRecording()
                println("Start speaking")

                val startTime = System.currentTimeMillis()
                val audioData = ByteArray(bufferSize)
                while (true) {
                    val estimatedTime = System.currentTimeMillis() - startTime
                    if (estimatedTime > 60000) { // 60 seconds
                        println("Stop speaking.")
                        break
                    }

                    val readSize = audioRecord.read(audioData, 0, bufferSize)
                    if (readSize > 0) {
                        // Process the audio audioData, send to streaming recognize request
                        request = StreamingRecognizeRequest.newBuilder()
                            .setAudioContent(ByteString.copyFrom(audioData, 0, readSize))
                            .build()
                        clientStream.send(request)
                    }
                }
            }
        } catch (e: Exception) {
            println(e)
        }
        responseObserver!!.onComplete()
    }
}