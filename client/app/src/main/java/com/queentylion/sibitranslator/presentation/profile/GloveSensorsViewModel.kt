package com.queentylion.sibitranslator.presentation.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.queentylion.sibitranslator.data.ConnectionState
import com.queentylion.sibitranslator.data.GloveReceiveManager
import com.queentylion.sibitranslator.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GloveSensorsViewModel @Inject constructor(
    private val temperatureAndHumidityReceiveManager: GloveReceiveManager
) : ViewModel(){

    var initializingMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var flexResistance by mutableStateOf(IntArray(11))
        private set

    private val maxSize = 10
    val dynamicArrayOfFlex: ArrayDeque<IntArray> = ArrayDeque(maxSize)

    var connectionState by mutableStateOf<ConnectionState>(ConnectionState.Uninitialized)

    private fun subscribeToChanges(){
        viewModelScope.launch {
            temperatureAndHumidityReceiveManager.data.collect{ result ->
                when(result){
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        val updatedFlexResistance = flexResistance.copyOf()
                        for ((index, value) in result.data.flexDegree.withIndex()) {
                            if (value != 0) {
                                updatedFlexResistance[index] = value
                            }
                        }
                        flexResistance = updatedFlexResistance
                        addElement(flexResistance)
                    }

                    is Resource.Loading -> {
                        initializingMessage = result.message
                        connectionState = ConnectionState.CurrentlyInitializing
                    }

                    is Resource.Error -> {
                        errorMessage = result.errorMessage
                        connectionState = ConnectionState.Uninitialized
                    }
                }
            }
        }
    }

    fun disconnect(){
        temperatureAndHumidityReceiveManager.disconnect()
    }

    fun reconnect(){
        temperatureAndHumidityReceiveManager.reconnect()
    }

    fun initializeConnection(){
        errorMessage = null
        subscribeToChanges()
        temperatureAndHumidityReceiveManager.startReceiving()
    }

    override fun onCleared() {
        super.onCleared()
        temperatureAndHumidityReceiveManager.closeConnection()
    }

    fun calculateMeanFlex(dynamicArrayOfFlex: ArrayDeque<IntArray>): IntArray {
        val sumFlex = IntArray(dynamicArrayOfFlex.firstOrNull()?.size ?: 0)

        for (flexArray in dynamicArrayOfFlex) {
            for (i in flexArray.indices) {
                sumFlex[i] += flexArray[i]
            }
        }

        val numberOfArrays = dynamicArrayOfFlex.size
        return IntArray(sumFlex.size) { index -> sumFlex[index] / numberOfArrays }
    }

    private fun addElement(element: IntArray) {
        if (dynamicArrayOfFlex.size >= maxSize) {
            dynamicArrayOfFlex.removeLast()
        }
        dynamicArrayOfFlex.addFirst(element)
    }


}