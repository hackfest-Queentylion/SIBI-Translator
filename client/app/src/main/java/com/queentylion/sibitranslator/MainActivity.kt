package com.queentylion.sibitranslator

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.queentylion.sibitranslator.ui.theme.SIBITranslatorTheme

class MainActivity : ComponentActivity() {

    private fun checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_AUDIO_PERMISSION_CODE)
        } else {
            startSpeechToText()
        }
    }

    private fun startSpeechToText() {
        // Get your ViewModel instance
        val translatorViewModel: TranslatorViewModel by viewModels()
        translatorViewModel.speechToText()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeechToText()
            } else {
                // Permission denied, handle as appropriate
            }
        }
    }

    companion object {
        private const val REQUEST_AUDIO_PERMISSION_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val translatorViewModel: TranslatorViewModel by viewModels {
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            TranslatorViewModelFactory(this)
        }

        setContent {
            SIBITranslatorTheme {
                Translator(
                    Modifier
                        .fillMaxSize(),
                    translatorViewModel,
                    onRequestPermission = { checkPermissionAndStart() }
                )
            }
        }
    }
}

@Composable
fun LanguageBox(text: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(100.dp)
            .height(35.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF191F28))
    ) {
        Text(
            text = text,
            color = Color(0xFFccccb5),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
fun Translator(
    modifier: Modifier = Modifier,
    translatorViewModel: TranslatorViewModel,
    onRequestPermission: () -> Unit
) {

    var isTextToSpeech by rememberSaveable { mutableStateOf(true) }
    var isRecording by rememberSaveable {
        mutableStateOf(false)
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
                    text = translatorViewModel.translatedText
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
                        }
                    }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_microphone),
                        contentDescription = "what",
                        modifier = Modifier
                            .size(28.dp)
                    )
                }
            }
        }
    }
}