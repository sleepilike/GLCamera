package com.example.glcamera.player.view

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import com.example.glcamera.player.render.PlayerRender

/**
 * Created by zyy on 2021/8/12
 *
 */
class PlayerSurfaceView : GLSurfaceView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    var mRender : PlayerRender? = null
    init {
        setEGLContextClientVersion(2)
        mRender = PlayerRender(this,)
        setRenderer(mRender)
        renderMode = RENDERMODE_WHEN_DIRTY;

        Log.d("TAG", "xiancheng id: ${android.os.Process.myTid()}")
    }
}