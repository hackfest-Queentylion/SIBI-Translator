package com.queentylion.sibitranslator.presentation.history

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.database.DatabaseReference
import com.queentylion.sibitranslator.components.TranslationItem
import com.queentylion.sibitranslator.database.TranslationsRepository
import com.queentylion.sibitranslator.presentation.sign_in.UserData
import com.queentylion.sibitranslator.viewmodel.TranslationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
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
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                title = {
                    Text(text = "History", fontWeight = FontWeight.Medium)
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(
                    vertical = innerPadding.calculateTopPadding() + 16.dp,
                    horizontal = 18.dp
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            translations.forEach { item ->
                TranslationItem(
                    text = item.translation,
                    isFavorite = item.isFavorite,
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
