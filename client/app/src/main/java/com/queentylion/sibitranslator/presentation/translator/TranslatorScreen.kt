package com.queentylion.sibitranslator.presentation.translator

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.queentylion.sibitranslator.presentation.LanguageBox
import com.queentylion.sibitranslator.R
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.firebase.database.DatabaseReference
import com.queentylion.sibitranslator.components.ExposedDropdownMenuBox
import com.queentylion.sibitranslator.data.ConnectionState
import com.queentylion.sibitranslator.database.TranslationsRepository
import com.queentylion.sibitranslator.presentation.profile.GloveSensorsViewModel
import com.queentylion.sibitranslator.presentation.sign_in.UserData
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
    onHistory: () -> Unit,
    onFavorites: () -> Unit,
    onSpeakerClick: (String) -> Unit,
    onProfile: () -> Unit,
    gloveViewModel: GloveSensorsViewModel = hiltViewModel(),
    viewModelTranslation: TranslationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
) {
    val gloveViewModel: GloveSensorsViewModel = hiltViewModel()
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

    DisposableEffect(Unit) {
        // Register the recognitionListener when the composable is first created
        speechRecognizer?.setRecognitionListener(recognitionListener)

        onDispose {
            // Unregister the recognitionListener when the composable is disposed
            speechRecognizer?.setRecognitionListener(null)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF191F28),
                    titleContentColor = Color(0xFFc69f68),
                    actionIconContentColor = Color(0xFF4b5975),
                    navigationIconContentColor = Color(0xFF4b5975)
                ),
                navigationIcon = {
                    IconButton(
                        onClick = { onProfile() },
                    ) {
                        if (userData?.profilePictureUrl != null) {
                            AsyncImage(
                                model = userData.profilePictureUrl,
                                contentDescription = "Profile picture",
                                modifier = Modifier
                                    .size(29.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Localized description",
                                modifier = Modifier.size(29.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onSpeakerClick(updatedTranslatedText) },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_speaker),
                            contentDescription = "Localized description",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                title = {
                    Row {
                        Text(
                            text = "SIBI ",
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Text(
                            text = "Translator",
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            )
        },
        backgroundColor = Color(0xFF191F28),
        contentColor = Color(0xFF4b5975)
    ) { innerPadding ->
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = innerPadding.calculateTopPadding() + 20.dp,
                        horizontal = 45.dp
                    )
                    .verticalScroll(rememberScrollState())
                    .weight(2F)
            ) {
                Text(
                    style = MaterialTheme.typography.displaySmall,
                    color = Color(0xFF4b5975),
//                    text = updatedTranslatedText
                    text = gloveViewModel.flexResistance[0].toString()
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
                    ExposedDropdownMenuBox(menuItem = arrayOf("Speech", "Gesture")) { newText ->
                        selectedLanguage = newText
                    }
                    FloatingActionButton(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .size(30.dp),
                        containerColor = Color.Transparent,
                        onClick = {  }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowForward,
                            contentDescription = "Arrow",
                            modifier = Modifier
                                .size(30.dp),
                            tint = Color(0xFFc69f68)
                        )
                    }
                    LanguageBox(text = "Text")
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 75.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { onFavorites() }) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Favorite Icon",
                            tint = Color(0xFF4b5975)
                        )
                    }

                    if(selectedLanguage != "Gesture") {
                        FloatingActionButton(
                            shape = CircleShape,
                            containerColor = if (isPressed) Color(0xFFccccb5) else Color(0xFFc69f68),
                            contentColor = Color(0xFF141a22),
                            modifier = Modifier
                                .size(80.dp),
                            interactionSource = interactionSource,
                            onClick = {
                                isRecording = !isRecording
                                if (isRecording) {
                                    onRequestPermission()
                                    speechRecognizer?.startListening(recognizerIntent)
                                } else {
                                    speechRecognizer?.stopListening()
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
                    } else {
                        FloatingActionButton(
                            shape = CircleShape,
                            containerColor = if (isPressed) Color(0xFFccccb5) else Color(0xFFc69f68),
                            contentColor = Color(0xFF141a22),
                            modifier = Modifier
                                .size(80.dp),
                            interactionSource = interactionSource,
                            onClick = {
                                isHandSigning = !isHandSigning
                                if (isHandSigning) {
                                    // Make sure connected to glove ble gimana
                                    if(gloveViewModel.connectionState == ConnectionState.Connected){

                                        viewModelTranslation.beginStreamingGesture(gloveViewModel.calculateMeanFlex(gloveViewModel.dynamicArrayOfFlex))
                                    } else {
                                        isHandSigning = false
                                        Toast.makeText(context, "Please Connect To Glove", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    viewModelTranslation.endStreamingGesture()
                                }
                            }
                        ) {
                            if(!isHandSigning) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_handbegin),
                                    contentDescription = "HandBegin",
                                    modifier = Modifier
                                        .size(28.dp)
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_handend),
                                    contentDescription = "HandEnd",
                                    modifier = Modifier
                                        .size(28.dp)
                                )
                            }
                        }
                    }

                    IconButton(onClick = { onHistory() }) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "History Icon",
                            tint = Color(0xFF4b5975)
                        )
                    }
                }
            }
        }
    }
//    Surface(
//        modifier = modifier,
//        color = Color(0xFF191f28)
//    ) {
//        Column {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(
//                        vertical = 35.dp,
//                        horizontal = 45.dp
//                    )
//                    .verticalScroll(rememberScrollState())
//                    .weight(2F)
//            ) {
//                Text(
//                    style = MaterialTheme.typography.displaySmall,
//                    color = Color(0xFF4b5975),
//                    text = updatedTranslatedText
//                )
//            }
//            Column(
//                modifier = Modifier
//                    .weight(1F)
//                    .fillMaxSize()
//                    .clip(RoundedCornerShape(topStart = 53.dp, topEnd = 53.dp))
//                    .background(Color(0xFF141a22))
//                    .padding(top = 10.dp),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Top
//            ) {
//                Row(
//                    modifier = Modifier.padding(top = 25.dp, bottom = 50.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    ExposedDropdownMenuBox(menuItem = arrayOf("Speech", "Gesture"))
//                    FloatingActionButton(
//                        modifier = Modifier
//                            .padding(horizontal = 20.dp)
//                            .size(30.dp),
//                        containerColor = Color.Transparent,
//                        onClick = { isTextToSpeech = !isTextToSpeech }
//                    ) {
//                        Image(
//                            painter = painterResource(id = R.drawable.ic_switch),
//                            contentDescription = "what",
//                            modifier = Modifier
//                                .size(30.dp),
//                            colorFilter = ColorFilter.tint(Color(0xFFc69f68))
//                        )
//                    }
//                    LanguageBox(text = "Text")
//                }
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 75.dp),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    IconButton(onClick = { onFavorites() }) {
//                        Icon(
//                            imageVector = Icons.Filled.Favorite,
//                            contentDescription = "Favorite Icon",
//                            tint = Color(0xFF4b5975)
//                        )
//                    }
//                    FloatingActionButton(
//                        shape = CircleShape,
//                        containerColor = if (isPressed) Color(0xFFccccb5) else Color(0xFFc69f68),
//                        contentColor = Color(0xFF141a22),
//                        modifier = Modifier
//                            .size(80.dp),
//                        interactionSource = interactionSource,
//                        onClick = {
//                            isRecording = !isRecording
//                            if (isRecording) {
//                                onRequestPermission()
//                                speechRecognizer?.startListening(recognizerIntent)
//                            } else {
//                                speechRecognizer?.stopListening()
//                            }
//                        }
//                    ) {
//                        Image(
//                            painter = painterResource(id = R.drawable.ic_microphone),
//                            contentDescription = "Microphone",
//                            modifier = Modifier
//                                .size(28.dp)
//                        )
//                    }
//                    IconButton(onClick = { onHistory() }) {
//                        Icon(
//                            imageVector = Icons.Filled.DateRange,
//                            contentDescription = "History Icon",
//                            tint = Color(0xFF4b5975)
//                        )
//                    }
//                }
//            }
//        }
//    }
}