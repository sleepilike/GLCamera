package com.example.glcamera.filter.base

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import com.example.glcamera.utils.GLUtil
import java.nio.FloatBuffer

/**
 * Created by zyy on 2021/7/16
 * 最基本的filter 2D
 */
open class DefaultFilter(context: Context) : AbstractFilter(context) {

    open var VERTEX_FILE : String= "shader/base_vertex_shader.glsl"
    open var FRAGMNET_FILE : String = "shader/base_fragment_shader.glsl"

    override fun createProgram(context: Context): Int {

        return GLUtil.createProgram(context,VERTEX_FILE,FRAGMNET_FILE)
    }

    override fun getTextureType(): Int {
        return GLES20.GL_TEXTURE_2D
    }

    override fun onDraw(
        vertexBuffer: FloatBuffer,
        textureBuffer: FloatBuffer,
        mtx: FloatArray,
        textureId: Int,
        size: Int, //一个点几个坐标
        count: Int, //一共几个点
    ) {

        useProgram()
        getGLSLHandle()
        bindGLSLValues(size,vertexBuffer,textureBuffer,mtx)
        bindTexture(textureId)
        drawArrays(0,count)

        unbindGLSLValues()
        unbindTexture()
        disUseProgram()

    }


    override fun releaseProgram() {
        super.releaseProgram()
    }

}