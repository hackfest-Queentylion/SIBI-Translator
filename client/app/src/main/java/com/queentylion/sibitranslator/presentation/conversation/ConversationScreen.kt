package com.queentylion.sibitranslator.presentation.conversation

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.database.DatabaseReference
import com.queentylion.sibitranslator.R
import com.queentylion.sibitranslator.data.ConnectionState
import com.queentylion.sibitranslator.database.TranslationsRepository
import com.queentylion.sibitranslator.presentation.profile.GloveSensorsViewModel
import com.queentylion.sibitranslator.presentation.sign_in.UserData
import com.queentylion.sibitranslator.util.GoogleAuthController
import com.queentylion.sibitranslator.viewmodel.TranslationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun Conversation(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit,
    speechRecognizer: SpeechRecognizer? = null,
    recognizerIntent: Intent? = null,
    databaseReference: DatabaseReference,
    initialText: String,
    userData: UserData? = null,
    googleAuthController: GoogleAuthController,
    onSpeakerClick: (String) -> Unit,
    onProfile: () -> Unit,
    viewModelTranslation: TranslationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {

    val gloveViewModel: GloveSensorsViewModel =
        hiltViewModel(LocalContext.current as ComponentActivity)
    val selectedLanguage by rememberSaveable {
        mutableStateOf("Speech")
    }
    var isRecording by rememberSaveable {
        mutableStateOf(false)
    }
    var isHandSigning by rememberSaveable {
        mutableStateOf(false)
    }
    var translatedText by rememberSaveable {
        mutableStateOf(initialText)
    }
    var conversationStarting by rememberSaveable {
        mutableStateOf(false)
    }
    val updatedTranslatedText by rememberUpdatedState(translatedText)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value
    val coroutineScope = rememberCoroutineScope()

    var accessToken by rememberSaveable {
        mutableStateOf("")
    }

    fun updateTranslatedText(newText: String) {
        translatedText = newText
    }

    val context = LocalContext.current


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
            updateTranslatedText("Say something")
            isRecording = false
        }

        override fun onResults(results: Bundle?) {
            Log.d("Speech Result", "onResults called")
            try {
                val speechResult =
                    results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                speechResult?.let {
                    Log.d("Speech Result", "Recognized speech: $speechResult")
                    updateTranslatedText(speechResult)
                    isRecording = false
                    val translationsRepository = TranslationsRepository(databaseReference)
                    translationsRepository.writeNewTranslations(userData?.userId, speechResult)
                }
            } catch (e: Exception) {
                Log.e("Speech Result", "Error in onResults: ${e.message}")
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val results = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val partialResult = results?.firstOrNull() ?: ""
            coroutineScope.launch {
                withContext(Dispatchers.Main) {
                    if (partialResult.isNotBlank()) {
                        updateTranslatedText(partialResult)
                    }
                }
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    LaunchedEffect(selectedLanguage) {
        if (selectedLanguage == "Gesture") {
            accessToken = googleAuthController.getAccessToken()
        }
    }

    DisposableEffect(Unit) {
        // Register the recognitionListener when the composable is first created
        speechRecognizer?.setRecognitionListener(recognitionListener)

        onDispose {
            // Unregister the recognitionListener when the composable is disposed
            speechRecognizer?.setRecognitionListener(null)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, start = 12.dp, end = 12.dp)
        ) {
            IconButton(onClick = { onProfile() }) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                Text(
                    text = "SIBI ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
                Text(
                    text = "Translator",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Normal
                )
            }
            IconButton(onClick = {
                /* onSpeakerClick(if(selectedLanguage == "Speech") updatedTranslatedText else viewModelTranslation.getSentencesString()) */}) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_volume),
                    contentDescription = "Start",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(6F)
                .padding(
                    top = 10.dp,
                    start = 10.dp,
                    end = 10.dp,
                    bottom = 10.dp
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .weight(2F)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))

            ) {
                Text(
                    modifier = Modifier
                        .padding(start = 15.dp, end = 15.dp, top = 5.dp),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
    //                text = if(selectedLanguage == "Speech") updatedTranslatedText else viewModelTranslation.getSentencesString(),
                    text = "Speech",
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .weight(2F)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            ) {
                Text(
                    modifier = Modifier
                        .padding(start = 15.dp, end = 15.dp, top = 5.dp),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
    //                text = if(selectedLanguage == "Speech") updatedTranslatedText else viewModelTranslation.getSentencesString(),
                    text = "Gesture",
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1F)
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 53.dp, topEnd = 53.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                shape = CircleShape,
                modifier = Modifier
                    .size(80.dp),
                onClick = {
                    // Turn on both speech recognition and gesture input
                    conversationStarting = !conversationStarting
                    if (conversationStarting) {
                        // TODO: This (I assume) is still really buggy -> debug it
                        onRequestPermission()
                        speechRecognizer?.startListening(recognizerIntent)
                        isHandSigning = true  // Activate gesture input
                        if (gloveViewModel.connectionState == ConnectionState.Connected) {
                            coroutineScope.launch{
                                while(isHandSigning) {
                                    repeat(30) {
                                        delay(80)
                                        viewModelTranslation.beginStreamingGesture(gloveViewModel.flexResistance)
                                    }
                                    viewModelTranslation.dynamicArrayOfFlex.clear()
                                }
                            }
                        } else {
                            isHandSigning = false
                            Toast
                                .makeText(
                                    context,
                                    "Please Connect To Glove",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                    } else {
                        // TODO: Start text to speech when gesturing is complete???
                        speechRecognizer?.stopListening()
                        viewModelTranslation.endStreamingGesture()
                        onSpeakerClick(viewModelTranslation.getSentencesString())
                        val translationsRepository = TranslationsRepository(databaseReference)
                        translationsRepository.writeNewTranslations(userData?.userId, viewModelTranslation.getSentencesString())
                    }

//                        if (selectedLanguage == "Gesture") {
//                            isHandSigning = !isHandSigning
//                            if (isHandSigning) {
//                                // Make sure connected to glove ble gimana
//                                if(gloveViewModel.connectionState == ConnectionState.Connected){
//
//                                    if (userData != null) {
//                                        userData.accessToken?.let {
//                                            viewModelTranslation.beginStreamingGesture(gloveViewModel.calculateMeanFlex(gloveViewModel.dynamicArrayOfFlex),
//                                                it
//                                            )
//                                        }
//                                    }
//                                } else {
//                                    isHandSigning = false
//                                    Toast.makeText(context, "Please Connect To Glove", Toast.LENGTH_SHORT).show()
//                                }
//                            } else {
//                                viewModelTranslation.endStreamingGesture()
//                                onSpeakerClick(viewModelTranslation.getSentencesString())
//                                val translationsRepository = TranslationsRepository(databaseReference)
//                                translationsRepository.writeNewTranslations(userData?.userId, viewModelTranslation.getSentencesString())
//                            }
//                        } else {
//                            isRecording = !isRecording
//                            if (isRecording) {
//                                onRequestPermission()
//                                speechRecognizer?.startListening(recognizerIntent)
//                            } else {
//                                speechRecognizer?.stopListening()
//                            }
//                        }
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_interpreter_mode_24),
                    contentDescription = "Favorite Icon",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

//@Preview
//@Composable
//fun ConversationPreview(
//
//) {
//    SIBITranslatorTheme {
//        Surface(
//            modifier = Modifier.fillMaxSize(),
//        ) {
//            Conversation()
//        }
//    }
//}
//
//@Preview(
//    uiMode = UI_MODE_NIGHT_YES
//)
//@Composable
//fun ConversationPreviewDarkMode(
//
//) {
//    SIBITranslatorTheme {
//        Surface(
//            modifier = Modifier.fillMaxSize(),
//        ) {
//            Conversation()
//        }
//    }
//}
