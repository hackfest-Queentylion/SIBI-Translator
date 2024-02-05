package com.queentylion.sibitranslator.presentation.translator

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.updateTransition
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.queentylion.sibitranslator.R
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.firebase.database.DatabaseReference
import com.queentylion.sibitranslator.LanguageBox
import com.queentylion.sibitranslator.components.CustomTextField
import com.queentylion.sibitranslator.components.ExposedDropdownMenuBox
import com.queentylion.sibitranslator.data.ConnectionState
import com.queentylion.sibitranslator.database.TranslationsRepository
import com.queentylion.sibitranslator.presentation.profile.GloveSensorsViewModel
import com.queentylion.sibitranslator.presentation.sign_in.UserData
import com.queentylion.sibitranslator.ui.theme.SIBITranslatorTheme
import com.queentylion.sibitranslator.util.GoogleAuthController
import com.queentylion.sibitranslator.viewmodel.TranslationViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Translator(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit,
    speechRecognizer: SpeechRecognizer? = null,
    recognizerIntent: Intent? = null,
    databaseReference: DatabaseReference,
    initialText: String,
    userData: UserData? = null,
    googleAuthController: GoogleAuthController,
    onHistory: () -> Unit,
    onFavorites: () -> Unit,
    onSpeakerClick: (String) -> Unit,
    onProfile: () -> Unit,
    viewModelTranslation: TranslationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {
    val gloveViewModel: GloveSensorsViewModel =
        hiltViewModel(LocalContext.current as ComponentActivity)
    var selectedLanguage by rememberSaveable {
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
            androidx.compose.material3.IconButton(onClick = { onProfile() }) {
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
            androidx.compose.material3.IconButton(onClick = { onSpeakerClick(if(selectedLanguage == "Speech") updatedTranslatedText else viewModelTranslation.getSentencesString()) }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_volume),
                    contentDescription = "Start",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    bottom = 10.dp,
                    start = 45.dp,
                    end = 45.dp
                )
                .verticalScroll(rememberScrollState())
                .weight(2F)
        ) {
            Text(
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                text = if(selectedLanguage == "Speech") updatedTranslatedText else viewModelTranslation.getSentencesString(),
                modifier = Modifier.padding(top = 20.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1F)
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 53.dp, topEnd = 53.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier.padding(top = 25.dp, bottom = 50.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExposedDropdownMenuBox(menuItem = arrayOf("Speech", "Gesture")) { newText ->
                    selectedLanguage = newText
                    if (userData != null) {
                        val googleAuthController = GoogleAuthController()
                        userData.accessToken = googleAuthController.getAccessToken()
                    }
                }
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = "Arrow",
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                CustomTextField(text = "Text")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 75.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                androidx.compose.material3.IconButton(onClick = { onFavorites() }) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Favorite Icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    shape = CircleShape,
                    modifier = Modifier
                        .size(80.dp),
                    onClick = {
                        if (selectedLanguage == "Gesture") {
                            isHandSigning = !isHandSigning
                            if (isHandSigning) {
                                // Make sure connected to glove ble gimana
                                if(gloveViewModel.connectionState == ConnectionState.Connected){

                                    if (userData != null) {
                                        userData.accessToken?.let {
                                            viewModelTranslation.beginStreamingGesture(gloveViewModel.calculateMeanFlex(gloveViewModel.dynamicArrayOfFlex),
                                                it
                                            )
                                        }
                                    }
                                } else {
                                    isHandSigning = false
                                    Toast.makeText(context, "Please Connect To Glove", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                viewModelTranslation.endStreamingGesture()
                                onSpeakerClick(viewModelTranslation.getSentencesString())
                                val translationsRepository = TranslationsRepository(databaseReference)
                                translationsRepository.writeNewTranslations(userData?.userId, viewModelTranslation.getSentencesString())
                            }
                        } else {
                            isRecording = !isRecording
                            if (isRecording) {
                                onRequestPermission()
                                speechRecognizer?.startListening(recognizerIntent)
                            } else {
                                speechRecognizer?.stopListening()
                            }
                        }
                    }
                ) {
                    if (selectedLanguage == "Gesture") {
                        if (!isHandSigning) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_handbegin),
                                contentDescription = "Favorite Icon",
                                modifier = Modifier.size(28.dp)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_handend),
                                contentDescription = "Favorite Icon",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_microphone),
                            contentDescription = "Favorite Icon",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                androidx.compose.material3.IconButton(onClick = { onHistory() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_history),
                        contentDescription = "Favorite Icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

}