package com.queentylion.sibitranslator

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TranslatorViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TranslatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TranslatorViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}