package com.queentylion.sibitranslator.data.file

import com.queentylion.sibitranslator.Resource

interface FileWriter {

    suspend fun writeFile(byteArray: ByteArray):Resource<String>

    companion object{
        const val FILE_NAME = "FileExportApp"
    }

}