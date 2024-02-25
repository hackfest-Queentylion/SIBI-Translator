package com.queentylion.sibitranslator.di

import android.content.Context
import com.queentylion.sibitranslator.data.converter.DataConverter
import com.queentylion.sibitranslator.data.converter.csv.DataConverterCSV
import com.queentylion.sibitranslator.data.file.AndroidInternalStorageFileWriter
import com.queentylion.sibitranslator.data.file.FileWriter
import com.queentylion.sibitranslator.data.repository.ExportRepositoryImpl
import com.queentylion.sibitranslator.domain.repository.ExportRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExportModule {

    @Provides
    @Singleton
    fun provideFileWrite(@ApplicationContext context:Context):FileWriter{
        return AndroidInternalStorageFileWriter(context)
    }

    @Provides
    @Singleton
    fun provideDataConverter():DataConverter{
        return DataConverterCSV()
    }

    @Provides
    @Singleton
    fun provideExportRepository(
        fileWriter: FileWriter,
        dataConverter:DataConverter
    ):ExportRepository{
        return ExportRepositoryImpl(fileWriter,dataConverter)
    }

}