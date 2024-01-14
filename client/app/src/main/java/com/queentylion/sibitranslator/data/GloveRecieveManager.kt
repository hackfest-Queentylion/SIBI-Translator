package com.queentylion.sibitranslator.data

import com.queentylion.sibitranslator.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow

interface GloveReceiveManager {

    val data: MutableSharedFlow<Resource<GloveResult>>

    fun reconnect()

    fun disconnect()

    fun startReceiving()

    fun closeConnection()

}