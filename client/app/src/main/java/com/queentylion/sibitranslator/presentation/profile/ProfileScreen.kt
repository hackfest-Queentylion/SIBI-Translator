package com.queentylion.sibitranslator.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.queentylion.sibitranslator.presentation.sign_in.UserData

@Composable
fun ProfileScreen(
        userData: UserData?,
        onSignOut: () -> Unit,
        onTranslate: () -> Unit
) {
    Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF191F28)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(userData?.profilePictureUrl != null) {
            AsyncImage(
                    model = userData.profilePictureUrl,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        if(userData?.username != null) {
            Text(
                    text = userData.username,
                    textAlign = TextAlign.Center,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = onTranslate,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFFc69f68),
                contentColor = Color(0xFF191F28)
            )
        ) {
            Text(text = "Start Translation", color = Color(0xFF191F28))
        }

        Button(
            onClick = onSignOut,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFFc69f68),
                contentColor = Color(0xFF191F28)
            )
        ) {
            Text(text = "Sign out", color = Color(0xFF191F28))
        }
    }
}