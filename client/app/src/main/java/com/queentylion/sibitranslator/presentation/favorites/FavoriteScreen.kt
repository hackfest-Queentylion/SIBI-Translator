package com.queentylion.sibitranslator.presentation.favorites

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.queentylion.sibitranslator.components.TranslationItem
import com.queentylion.sibitranslator.database.TranslationsRepository
import com.queentylion.sibitranslator.presentation.sign_in.UserData
import com.queentylion.sibitranslator.ui.theme.SIBITranslatorTheme
import com.queentylion.sibitranslator.viewmodel.TranslationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    databaseReference: DatabaseReference,
    userData: UserData,
    viewModel: TranslationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onBack: () -> Unit,
    navController: NavController
) {

    val translations by viewModel.translations.observeAsState(listOf())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                ),
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = Color.Black
                        )
                    }
                },
                title = {
                    Text(text = "History", fontWeight = FontWeight.Medium)
                }
            )
        },
        backgroundColor = Color.White,
        contentColor = Color(0xFF071e26),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(
                    vertical = innerPadding.calculateTopPadding() + 16.dp,
                    horizontal = innerPadding.calculateTopPadding() + 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            translations.forEach { item ->
                if (item.isFavorite) {
                    TranslationItem(
                        text = item.translation,
                        isFavorite = true,
                        onFavorite = {
                            val translationsRepository = TranslationsRepository(databaseReference)
                            translationsRepository.toggleTranslationsIsFavorite(item.translationId)
                            translationsRepository.toggleUserTranslationsIsFavorite(
                                userData.userId,
                                item.translationId
                            )
                        },
                        onClicked = {
                            val route = "translator?initialText=" + Uri.encode(item.translation)
                            navController.navigate(route)
                        }
                    )
                }
            }
        }
    }
}