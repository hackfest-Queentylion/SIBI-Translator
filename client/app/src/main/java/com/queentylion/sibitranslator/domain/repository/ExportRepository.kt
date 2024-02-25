package com.queentylion.sibitranslator.domain.repository

import com.queentylion.sibitranslator.Resource
import com.queentylion.sibitranslator.domain.model.ExportModel
import com.queentylion.sibitranslator.domain.model.PathInfo
import kotlinx.coroutines.flow.Flow

interface ExportRepository {

    fun startExportData(
        exportList:List<ExportModel>
    ): Flow<Resource<PathInfo>>

}