package com.queentylion.sibitranslator.data.converter

import com.queentylion.sibitranslator.Resource
import com.queentylion.sibitranslator.domain.model.ExportModel
import kotlinx.coroutines.flow.Flow

interface DataConverter {

    fun convertSensorData(
        exportDataList:List<ExportModel>
    ): Flow<Resource<GenerateInfo>>

}