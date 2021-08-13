package com.example.glcamera.filter.base

import android.opengl.Matrix
import java.nio.FloatBuffer

/**
 * Created by zyy on 2021/7/14
 *
 */
interface IFilter {

    fun getTextureType() : Int
    fun onDraw(vertexBuffer: FloatBuffer,textureBuffer: FloatBuffer,
               mtx : FloatArray, textureId :Int,size : Int,count : Int)

    fun releaseProgram()

}