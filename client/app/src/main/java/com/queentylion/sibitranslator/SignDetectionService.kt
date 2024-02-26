package com.queentylion.sibitranslator

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import com.queentylion.sibitranslator.appContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.nnapi.NnApiDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class SignDetectionService(
    private var numThreads: Int = 2,
    private var currentDelegate: Delegate = Delegate.CPU,
    private var currentModel: Int = MODEL_INT8,
    private val context: Context = appContext,
) {
    private var interpreterPredict: Interpreter? = null
    private var inputPredictTargetWidth = 0
    private var inputPredictTargetHeight = 0
    private var inputTransformTargetWidth = 0
    private var inputTransformTargetHeight = 0
    private var outputPredictShape = intArrayOf()

    private val feature0Labels =
        arrayOf(
            "halo",
            "saudara",
        )

    init {
        setupStyleTransfer()
        interpreterPredict!!.let { interpreter ->
            inputPredictTargetHeight = interpreter.getInputTensor(0).shape()[1]
            inputPredictTargetWidth = interpreter.getInputTensor(0).shape()[2]
            outputPredictShape = interpreter.getOutputTensor(0).shape()
        }

    }

    private fun setupStyleTransfer() {
        val tfliteOption = Interpreter.Options()
//        tfliteOption.numThreads = numThreads
        tfliteOption.setNumThreads(numThreads)

        when (currentDelegate) {
            Delegate.CPU -> Unit        // Default
            Delegate.GPU -> {
//                if (CompatibilityList().isDelegateSupportedOnThisDevice) {
//                    tfliteOption.addDelegate(GpuDelegate())
//                } else {
//                    error("GPU is not supported on this device")
//                }
            }

            Delegate.NNAPI -> tfliteOption.addDelegate(NnApiDelegate())
        }
        val modelPredict: String
        if (currentModel == MODEL_INT8) {
            modelPredict = "model_sibi.tflite"
        } else {
            modelPredict = "model_sibi.tflite"
        }

        try {
            interpreterPredict = Interpreter(
                FileUtil.loadMappedFile(context, modelPredict), tfliteOption
            )

        } catch (e: Exception) {
            Log.e(TAG, "TFLite failed to load model with error: " + e.message)
            error("Style transfer failed to initialize. See error logs for details")
        }
    }

    fun transfer(gloveKeypoints: List<List<Int>>): String {
        if (interpreterPredict == null) {
            setupStyleTransfer()
        }

        val inferenceTime = SystemClock.uptimeMillis()

        val flatInputArray = gloveKeypoints.flatten().map { it.toFloat() }.toFloatArray()

        val inputShape = intArrayOf(inputTransformTargetHeight, inputTransformTargetWidth)
        val inputDataType = DataType.INT32
        val inputTensorBuffer = TensorBuffer.createFixedSize(inputShape, inputDataType)

        inputTensorBuffer.loadArray(flatInputArray)

        val predictOutput = TensorBuffer.createFixedSize(outputPredictShape, DataType.FLOAT32)

        interpreterPredict?.run(inputTensorBuffer.buffer, predictOutput.buffer)

//        return outputBitmap to SystemClock.uptimeMillis() - inferenceTime
        return getOutputString(predictOutput)
    }

    fun clearStyleTransferUtil() {
        interpreterPredict = null
    }

    private fun getOutputString(
        output: TensorBuffer,
    ) : String {
        val maxIndexFeature0 = indexOfMaxValue(output.floatArray)
        return feature0Labels[maxIndexFeature0]
    }

    private fun indexOfMaxValue(arr: FloatArray): Int {
        var maxIndex = -1
        var maxValue = Float.MIN_VALUE

        for (i in arr.indices) {
            if (arr[i] > maxValue) {
                maxValue = arr[i]
                maxIndex = i
            }
        }

        return maxIndex
    }

    enum class Delegate {
        CPU, GPU, NNAPI
    }

    companion object {
        const val MODEL_INT8 = 0

        private const val TAG = "Sign Classifier"
    }
}