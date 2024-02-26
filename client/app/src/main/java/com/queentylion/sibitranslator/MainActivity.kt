package com.queentylion.sibitranslator

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.queentylion.sibitranslator.presentation.sign_in.GoogleAuthUiClient
import com.queentylion.sibitranslator.ui.theme.SIBITranslatorTheme
import java.util.Locale
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.queentylion.sibitranslator.database.UsersRepository
import com.queentylion.sibitranslator.presentation.conversation.Conversation
import com.queentylion.sibitranslator.presentation.favorites.FavoritesScreen
import com.queentylion.sibitranslator.presentation.history.HistoryScreen
import com.queentylion.sibitranslator.presentation.profile.ProfileScreen
import com.queentylion.sibitranslator.presentation.sign_in.SignInScreen
import com.queentylion.sibitranslator.presentation.sign_in.SignInViewModel
import com.queentylion.sibitranslator.presentation.sign_in.UserData
import com.queentylion.sibitranslator.presentation.translator.Translator
import com.queentylion.sibitranslator.util.GoogleAuthController

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var recognizerIntent: Intent
    private lateinit var databaseReference: DatabaseReference
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var googleAuthController: GoogleAuthController

    @Inject
    lateinit var bluetoothAdapter: BluetoothAdapter

    private fun checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_AUDIO_PERMISSION_CODE
            )
        }
    }

    companion object {
        private const val REQUEST_AUDIO_PERMISSION_CODE = 1
    }

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    private var isBluetootDialogAlreadyShown = false
    private fun showBluetoothDialog() {
        if (!bluetoothAdapter.isEnabled) {
            if (!isBluetootDialogAlreadyShown) {
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startBluetoothIntentForResult.launch(enableBluetoothIntent)
                isBluetootDialogAlreadyShown = true
            }
        }
    }

    private val startBluetoothIntentForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            isBluetootDialogAlreadyShown = false
            if (result.resultCode != Activity.RESULT_OK) {
                showBluetoothDialog()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        googleAuthController = GoogleAuthController()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
        )
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id")
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.forLanguageTag("id"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "This Language is not supported")
                }
            } else {
                Log.e("TTS", "Initialization Failed!")
            }
        }

        databaseReference =
            Firebase
                .database("https://sibi-translator-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .reference

        googleAuthUiClient.setSignInCallback(object : GoogleAuthUiClient.SignInCallback {
            override fun onSignInSuccess(userData: UserData?) {
                val usersRepository = UsersRepository(databaseReference)
                usersRepository.writeNewUsers(
                    userId = userData?.userId,
                    username = userData?.username
                )
            }
        })

        setContent {
            SIBITranslatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "sign_in") {

                        composable("sign_in") {
                            val viewModel = viewModel<SignInViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()

                            LaunchedEffect(key1 = Unit) {
                                if (googleAuthUiClient.getSignedInUser() != null) {
                                    navController.navigate("profile")
                                }
                            }

                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult = googleAuthUiClient.signInWithIntent(
                                                intent = result.data ?: return@launch
                                            )
                                            viewModel.onSignInResult(signInResult)
                                        }
                                    }
                                }
                            )

                            LaunchedEffect(key1 = state.isSignInSuccessful) {
                                if (state.isSignInSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Sign in successful",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    navController.navigate("profile")
                                    viewModel.resetState()
                                }
                            }

                            SignInScreen(
                                state = state,
                                onSignInClick = {
                                    lifecycleScope.launch {
                                        val signInIntentSender = googleAuthUiClient.signIn()
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signInIntentSender ?: return@launch
                                            ).build()
                                        )
                                    }
                                }
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                userData = googleAuthUiClient.getSignedInUser(),
                                onSignOut = {
                                    lifecycleScope.launch {
                                        googleAuthUiClient.signOut()
                                        Toast.makeText(
                                            applicationContext,
                                            "Signed out",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        navController.navigate("sign_in")
                                    }
                                },
                                onTranslate = {
                                    lifecycleScope.launch {
                                        navController.navigate("translator")
                                    }
                                },
                                onBluetooth = {
                                    lifecycleScope.launch {
                                        navController.navigate("profile_connected")
                                    }
                                },
                                isBluetoothConnected = false,
                                onBluetoothStateChanged = {
                                    showBluetoothDialog()
                                }
                            )
                        }

                        composable("profile_connected") {
                            ProfileScreen(
                                userData = googleAuthUiClient.getSignedInUser(),
                                onSignOut = {
                                    lifecycleScope.launch {
                                        googleAuthUiClient.signOut()
                                        Toast.makeText(
                                            applicationContext,
                                            "Signed out",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        navController.navigate("sign_in")
                                    }
                                },
                                onTranslate = {
                                    lifecycleScope.launch {
                                        navController.navigate("translator")
                                    }
                                },
                                onBluetooth = {
                                    lifecycleScope.launch {
                                        navController.navigate("profile_connected")

                                    }
                                },
                                isBluetoothConnected = true,
                                onBluetoothStateChanged = {
                                    navController.navigate("sign_in")
                                    showBluetoothDialog()

                                }
                            )

                        }

                        fun speakOut(text: String) {
                            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
                        }

                        composable(
                            "conversation?initialText={initialText}",
                            arguments = listOf(navArgument("initialText") {
                                defaultValue = "Speech"
                            })
                        ) { backStackEntry ->
                            backStackEntry.arguments?.getString("initialText")?.let {
                                Conversation(
                                    Modifier
                                        .fillMaxSize(),
                                    onRequestPermission = { checkPermissionAndStart() },
                                    speechRecognizer = speechRecognizer,
                                    recognizerIntent = recognizerIntent,
                                    initialText = it,
                                    databaseReference = databaseReference,
                                    googleAuthController = googleAuthController,
                                    userData = googleAuthUiClient.getSignedInUser(),
                                    onProfile = {
                                        lifecycleScope.launch {
                                            navController.navigate("profile")
                                        }
                                    },
                                    onHistory = {
                                        lifecycleScope.launch {
                                            navController.navigate("history")
                                        }
                                    },
                                    onFavorites = {
                                        lifecycleScope.launch {
                                            navController.navigate("favorite")
                                        }
                                    },
                                    onSpeakerClick = { text ->
                                        speakOut(text)
                                    }
                                )
                            }
                        }

                        composable(
                            "translator?initialText={initialText}",
                            arguments = listOf(navArgument("initialText") {
                                defaultValue = "Say Something"
                            })
                        ) { backStackEntry ->
                            backStackEntry.arguments?.getString("initialText")?.let {
                                Translator(
                                    Modifier
                                        .fillMaxSize(),
                                    onRequestPermission = { checkPermissionAndStart() },
                                    speechRecognizer = speechRecognizer,
                                    recognizerIntent = recognizerIntent,
                                    initialText = it,
                                    databaseReference = databaseReference,
                                    googleAuthController = googleAuthController,
                                    userData = googleAuthUiClient.getSignedInUser(),
                                    onProfile = {
                                        lifecycleScope.launch {
                                            navController.navigate("profile")
                                        }
                                    },
                                    onHistory = {
                                        lifecycleScope.launch {
                                            navController.navigate("history")
                                        }
                                    },
                                    onFavorites = {
                                        lifecycleScope.launch {
                                            navController.navigate("conversation")
                                        }
                                    },
                                    onSpeakerClick = { text ->
                                        speakOut(text)
                                    }
                                )
                            }
                        }

                        composable("history") {
                            HistoryScreen(
                                databaseReference,
                                googleAuthUiClient.getSignedInUser()!!,
                                onBack = {
                                    lifecycleScope.launch {
                                        navController.navigate("translator")
                                    }
                                },
                                navController = navController
                            )
                        }

                        composable("favorite") {
                            FavoritesScreen(
                                databaseReference,
                                googleAuthUiClient.getSignedInUser()!!,
                                onBack = {
                                    lifecycleScope.launch {
                                        navController.navigate("translator")
                                    }
                                },
                                navController = navController
                            )
                        }

                    }
                }
            }
        }
    }

    override fun onDestroy() {
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }


    override fun onStart() {
        super.onStart()
        showBluetoothDialog()
    }
}

@Composable
fun LanguageBox(text: String) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(100.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
    ) {
        BasicTextField(
            value = text,
            modifier = Modifier
                .height(IntrinsicSize.Min),
            onValueChange = {},
            readOnly = true,
            textStyle = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp,
                color = Color(0xFF006780),
                textAlign = TextAlign.Center
            )
        )
    }
}