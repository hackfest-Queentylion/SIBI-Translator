package com.queentylion.sibitranslator.presentation.translator

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
import androidx.compose.ui.unit.dp
import com.queentylion.sibitranslator.LanguageBox
import com.queentylion.sibitranslator.R
import com.queentylion.sibitranslator.TranslatorViewModel

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