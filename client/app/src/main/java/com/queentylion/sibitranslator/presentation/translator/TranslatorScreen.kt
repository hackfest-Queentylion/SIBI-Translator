package com.queentylion.sibitranslator.presentation.translator

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.queentylion.sibitranslator.LanguageBox
import com.queentylion.sibitranslator.R

@Composable
fun Translator(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit,
    speechRecognizer: SpeechRecognizer,
    recognizerIntent: Intent
) {

    var isTextToSpeech by rememberSaveable { mutableStateOf(true) }
    var isRecording by rememberSaveable {
        mutableStateOf(false)
    }
    var translatedText by rememberSaveable {
        mutableStateOf("Say Something")
    }
    val updatedTranslatedText by rememberUpdatedState(translatedText)

    fun updateTranslatedText(newText: String) {
        translatedText = newText
    }

    val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            updateTranslatedText("")
        }
        override fun onBeginningOfSpeech() {
            Log.d("Speech Recognition", "Speech started")
        }
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {
            Log.d("Speech Recognition", "Buffer Received")
        }
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) {
            Log.e("Speech Recognition", "Error code: $error")
        }
        override fun onResults(results: Bundle?) {
            Log.d("Speech Result", "onResults called")
            try {
                val speechResult = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                speechResult?.let {
                    // Update the translatedText with the recognized speech
                    Log.d("Speech Result", "Recognized speech: $speechResult")
                    updateTranslatedText(speechResult)
                    isRecording = false
                }
            } catch (e: Exception) {
                Log.e("Speech Result", "Error in onResults: ${e.message}")
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    DisposableEffect(Unit) {
        // Register the recognitionListener when the composable is first created
        speechRecognizer.setRecognitionListener(recognitionListener)

        onDispose {
            // Unregister the recognitionListener when the composable is disposed
            speechRecognizer.setRecognitionListener(null)
        }
    }

    Surface(
        modifier = modifier,
        color = Color(0xFF191f28)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 35.dp,
                        horizontal = 45.dp
                    )
                    .verticalScroll(rememberScrollState())
                    .weight(2F)
            ) {
                Text(
                    style = MaterialTheme.typography.displaySmall,
                    color = Color(0xFF4b5975),
                    text = updatedTranslatedText
                )
            }
            Column(
                modifier = Modifier
                    .weight(1F)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 53.dp, topEnd = 53.dp))
                    .background(Color(0xFF141a22))
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier.padding(top = 25.dp, bottom = 50.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LanguageBox(text = "Speech")
                    FloatingActionButton(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .size(30.dp),
                        containerColor = Color.Transparent,
                        onClick = { isTextToSpeech = !isTextToSpeech }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_switch),
                            contentDescription = "what",
                            modifier = Modifier
                                .size(30.dp),
                            colorFilter = ColorFilter.tint(Color(0xFFc69f68))
                        )
                    }
                    LanguageBox(text = "Text")
                }
                FloatingActionButton(
                    shape = CircleShape,
                    containerColor = Color(0xFFc69f68),
                    contentColor = Color(0xFF141a22),
                    modifier = Modifier
                        .size(80.dp),
                    onClick = {
                        isRecording = !isRecording
                        if (isRecording) {
                            onRequestPermission()
                            speechRecognizer.startListening(recognizerIntent)
                        } else {
                            speechRecognizer.stopListening()
                        }
                    }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_microphone),
                        contentDescription = "Microphone",
                        modifier = Modifier
                            .size(28.dp)
                    )
                }
            }
        }
    }
}