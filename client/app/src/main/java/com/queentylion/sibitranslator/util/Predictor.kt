package com.queentylion.sibitranslator.util

import com.queentylion.sibitranslator.ml.ModelSibi
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.model.Model

val compatList = CompatibilityList()

val options: Model.Options = if(compatList.isDelegateSupportedOnThisDevice) {
    // if the device has a supported GPU, add the GPU delegate
    Model.Options.Builder().setDevice(Model.Device.GPU).build()
} else {
    // if the GPU is not supported, run on 4 threads
    Model.Options.Builder().setNumThreads(4).build()
}

// Initialize the model as usual feeding in the options object
val sibiModel = ModelSibi.newInstance(context, options)
