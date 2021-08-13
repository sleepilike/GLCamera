package com.example.glcamera.player.render

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.Surface
import com.example.glcamera.filter.CameraOESFilter
import com.example.glcamera.utils.BufferUtil
import com.example.glcamera.utils.GLUtil
import com.example.glcamera.player.view.PlayerSurfaceView
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by zyy on 2021/8/12
 *
 */
class PlayerRender(glSurfaceView: PlayerSurfaceView) : GLSurfaceView.Renderer {

    private var mContext : Context = glSurfaceView.context
    private var mGLSurfaceView = glSurfaceView
    private var mDrawer : CameraOESFilter? = null
    private var mTexture = -1
    private var mSurfaceTexture : SurfaceTexture? = null
    private var mSurface : Surface? = null

    private var mVertexBuffer : FloatBuffer? = null
    private var mTextureBuffer : FloatBuffer? = null

    /**
     * 顶点坐标
     */
    private val VERTEX_COORDS = floatArrayOf(
        -1.0f, -1.0f,
        1.0f, -1.0f,
        -1f,1f,
        1.0f, 1.0f,
    )

    /**
     * 纹理坐标
     */
    private val TEXTURE_COORDS = floatArrayOf(
        0f,1f,
        1f,1f,
        0f,0f,
        1f,0f
    )

    /**
     * 变换矩阵
     */
    private var matrix = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f)

    init {

        initSurfaceTexture()
        initFloatBuffer()

    }


    private fun initSurfaceTexture(){
        mTexture = GLUtil.createOESTexture()
        mSurfaceTexture = SurfaceTexture(mTexture)
        mSurface = Surface(mSurfaceTexture)
        mSurfaceTexture!!.setOnFrameAvailableListener(SurfaceTexture.OnFrameAvailableListener {
            //触发 GLSurfaceView 的render的 onDrawFrame
            mGLSurfaceView.requestRender()
        })

    }

    private fun initFloatBuffer(){
        mVertexBuffer = BufferUtil.toFloatBuffer(VERTEX_COORDS)
        mTextureBuffer = BufferUtil.toFloatBuffer(TEXTURE_COORDS)
    }

    fun getSurface() : Surface{
        return mSurface!!
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {


        Log.d("TAG", "onSurfaceCreated: ${android.os.Process.myTid()}")
        mDrawer = CameraOESFilter(mContext)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

        GLES20.glViewport(0,0,width,height)


    }

    override fun onDrawFrame(gl: GL10?) {

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        mSurfaceTexture?.updateTexImage()
        mDrawer?.onDraw(mVertexBuffer!!,mTextureBuffer!!,matrix,mTexture,2,4)

    }

}