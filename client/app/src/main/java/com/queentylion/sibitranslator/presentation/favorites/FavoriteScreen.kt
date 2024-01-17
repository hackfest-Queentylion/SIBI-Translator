package com.queentylion.sibitranslator.presentation.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.queentylion.sibitranslator.components.TranslationItem
import com.queentylion.sibitranslator.database.TranslationsRepository
import com.queentylion.sibitranslator.presentation.sign_in.UserData
import com.queentylion.sibitranslator.types.Translation
import com.queentylion.sibitranslator.ui.theme.SIBITranslatorTheme
import com.queentylion.sibitranslator.viewmodel.TranslationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    databaseReference: DatabaseReference,
    userData: UserData,
    viewModel: TranslationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit,
) {

    val translations by viewModel.translations.observeAsState(listOf())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = topAppBarColors(
                    containerColor = Color(0xFF141a22),
                    titleContentColor = Color(0xFFc69f68),
                ),
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = Color(0xFF4b5975)
                        )
                    }
                },
                title = {
                    Text(text = "Favorites", fontWeight = FontWeight.Medium)
                }
            )
        },
        backgroundColor = Color(0xFF191F28),
        contentColor = Color(0xFF4b5975)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(vertical = innerPadding.calculateTopPadding() + 16.dp, horizontal = 6.dp),
        ) {
            items(translations) { item ->
                if (item.isFavorite) {
                    TranslationItem(
                        text = item.translation,
                        isFavorite = true
                    ) {
                        val translationsRepository = TranslationsRepository(databaseReference)
                        translationsRepository.toggleTranslationsIsFavorite(item.translationId)
                        translationsRepository.toggleUserTranslationsIsFavorite(
                            userData.userId,
                            item.translationId
                        )
                    }
                    Spacer(modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun HistoryPreview() {
    SIBITranslatorTheme {
        Surface {
            FavoritesScreen(
                databaseReference = Firebase.database.reference,
                userData = UserData("a", "b", "c")
            ) {}
        }
    }
}