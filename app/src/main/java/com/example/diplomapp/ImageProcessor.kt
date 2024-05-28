package com.example.diplomapp

import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder


class ImageProcessor(private val model: Interpreter) {

    private lateinit var interpreter: Interpreter

    init {
        interpreter = model
    }

    fun processImage(bitmap: Bitmap): Int {
        val inputShape = interpreter.getInputTensor(0).shape()
        val inputSize = inputShape[1] * inputShape[2] * inputShape[3]
        val outputShape = interpreter.getOutputTensor(0).shape()
        val outputSize = outputShape[1]
        val inputBuffer = ByteBuffer.allocateDirect(inputSize*4).apply {
            order(ByteOrder.nativeOrder())
            rewind()
        }

        val outputBuffer = ByteBuffer.allocateDirect(outputSize*4).apply {
            order(ByteOrder.nativeOrder())
            rewind()
        }
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputShape[2], inputShape[1], false)
        for (i in 0..223){
            for(j in 0..223){
                val col= scaledBitmap.getColor(i,j)

                inputBuffer.putFloat(col.red())
                inputBuffer.putFloat(col.green())
                inputBuffer.putFloat(col.blue())

            }
        }
        inputBuffer.rewind()
        interpreter.run(inputBuffer, outputBuffer)
        val output= Array<Float>(113){0f}
        outputBuffer.rewind()
        for (i in 0..112){
            output[i]=outputBuffer.getFloat()
        }
        val ans= output.indexOf(output.max())
        return ans
    }
    fun closeModel(){
        interpreter.close()
    }
}