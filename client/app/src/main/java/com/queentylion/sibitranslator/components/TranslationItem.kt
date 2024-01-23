package com.queentylion.sibitranslator.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(0.25f)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = text,
            maxLines = 2,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(0.8f)
                .padding(top = 15.dp, bottom = 15.dp, start = 20.dp)
        )
        IconButton(
            modifier = Modifier
                .size(24.dp)
                .weight(0.15f),
            onClick = {
                favorite = !favorite
                onFavorite()
            }
        ) {
            Icon(
                imageVector = if (favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Favorite Button",
                tint = if (!isFavorite) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.tertiary
            )
        }
    }
}