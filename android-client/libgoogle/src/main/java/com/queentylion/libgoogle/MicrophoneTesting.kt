package com.queentylion.libgoogle

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
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine
import kotlin.system.exitProcess

@Throws(Exception::class)
fun main() {
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
                            println(alternative.transcript)
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
            val audioFormat =
                AudioFormat(16000f, 16, 1, true, false)
            val targetInfo: DataLine.Info = DataLine.Info(
                TargetDataLine::class.java,
                audioFormat
            ) // Set the system information to read from the microphone audio stream
            if (!AudioSystem.isLineSupported(targetInfo)) {
                println("Microphone not supported")
                exitProcess(0)
            }
            // Target data line captures the audio stream the microphone produces.
            val targetDataLine = AudioSystem.getLine(targetInfo) as TargetDataLine
            targetDataLine.open(audioFormat)
            targetDataLine.start()
            println("Start speaking")
            val startTime = System.currentTimeMillis()
            // Audio Input Stream
            val audio = AudioInputStream(targetDataLine)
            while (true) {
                val estimatedTime = System.currentTimeMillis() - startTime
                val data = ByteArray(6400)
                audio.read(data)
                if (estimatedTime > 60000) { // 60 seconds
                    println("Stop speaking.")
                    targetDataLine.stop()
                    targetDataLine.close()
                    break
                }
                request = StreamingRecognizeRequest.newBuilder()
                    .setAudioContent(ByteString.copyFrom(data))
                    .build()
                clientStream.send(request)
            }
        }
    } catch (e: Exception) {
        println(e)
    }
    responseObserver!!.onComplete()
}