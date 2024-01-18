package com.queentylion.sibitranslator.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TranslationItem(
    text: String,
    isFavorite: Boolean,
    onFavorite: () -> Unit,
    onClicked: () -> Unit
) {
    var favorite by rememberSaveable { mutableStateOf(isFavorite) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClicked() }
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x99CFE6F1)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Box(
            modifier = Modifier
                .width(300.dp)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(vertical = 10.dp, horizontal = 10.dp),
                style = TextStyle(
                    color = Color(0xFF071e26),
                    lineHeight = 27.sp,
                    fontSize = 16.sp
                )
            )
        }
        IconButton(
            modifier = Modifier.width(40.dp),
            onClick = {
                favorite = !favorite
                onFavorite()
            }
        ) {
            Icon(
                imageVector = if (favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Favorite Button",
                tint = if (favorite) Color(0xFF006780) else Color(0xFF70787c)
            )
        }
    }
}