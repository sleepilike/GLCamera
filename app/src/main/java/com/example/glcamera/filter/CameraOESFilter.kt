package com.example.glcamera.filter

import android.content.Context
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.util.Log
import com.example.glcamera.filter.base.DefaultFilter
import com.example.glcamera.utils.GLUtil
import java.nio.FloatBuffer

/**
 * Created by zyy on 2021/7/14
 *
 * 相机 外部纹理 oes 实现预览
 */
class CameraOESFilter(context: Context) : DefaultFilter(context) {
    override var VERTEX_FILE : String = "shader/oes_vertex_shader.glsl"
    override var FRAGMNET_FILE : String= "shader/oes_fragment_shader.glsl"


    override fun createProgram(context: Context): Int {


        var fragmentShaderCode : String = GLUtil.readRawShaderCode(context,"shader/oes_fragment_shader.glsl")
        var vertexShaderCode : String = GLUtil.readRawShaderCode(context,"shader/oes_vertex_shader.glsl")

        //编译出错 已经出现OpenGL context错误
        var vertexShaderId : Int = GLUtil.compileShaderCode(GLES20.GL_VERTEX_SHADER,vertexShaderCode)
        var fragmentShaderId : Int = GLUtil.compileShaderCode(GLES20.GL_FRAGMENT_SHADER,fragmentShaderCode)
        var mProgramId : Int = GLUtil.linkProgram(vertexShaderId,fragmentShaderId)
        return mProgramId


//       return GLUtil.createProgram(context,"shader/oes_fragment_shader.glsl","shader/oes_vertex_shader.glsl")
    }


    override fun getTextureType(): Int {
        return GLES11Ext.GL_SAMPLER_EXTERNAL_OES
    }


}