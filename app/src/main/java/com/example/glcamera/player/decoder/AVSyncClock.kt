package com.example.glcamera.player.decoder

import android.os.SystemClock
import java.util.concurrent.TimeUnit

/**
 * Created by zyy on 2021/8/11
 *
 * 同步器 实现音视频同步
 * 音视频同步通常有三种方式：一种是参考视频，第二种是参考音频，第三种时互相参考。
 * 使用的为第一种和第二种，音视频自身完成同步
 */
class AVSyncClock {
    private val TIME_UNSET : Long = Long.MIN_VALUE+1
    private val TIME_END_OF_SOURCE = Long.MIN_VALUE

    /**
     * 帧基准时间
     */
    private var mBasePositionUs : Long = 0
    /**
     * 播放速度
     */
    private var mSpeed = 1f
    /**
     * 运行基准时间
     */
    private var mBaseElapsedMs : Long = 0
    /**
     * 是否开始计时
     */
    private var mStarted : Boolean = false

    /**
     * 启动时钟
     */
    fun start(){
        if(mStarted) return
        reset()
        mStarted = true
    }

    private fun reset(){
        mBasePositionUs = 0
        mBaseElapsedMs = SystemClock.elapsedRealtime()
    }

    /**
     * 停止时钟
     */
    fun stop(){
        mBasePositionUs = 0
        mStarted = false
        mBaseElapsedMs = 0
    }

    /**
     * 锁定
     */
    fun lock(positionUs : Long,diff : Long){
        if (!mStarted) {
            return
        }

        if(mBasePositionUs == 0L)
            mBasePositionUs = positionUs

        var speedPositionUs =((positionUs - mBasePositionUs) * (1f / mSpeed)).toLong()
        var durationMs = usToMs(speedPositionUs) + diff
        var endTimeMs = mBaseElapsedMs + durationMs
        var sleepTimeMs = endTimeMs - SystemClock.elapsedRealtime()

        if (sleepTimeMs > 0){
            try {
                //睡眠 锁定线程
                TimeUnit.MILLISECONDS.sleep(sleepTimeMs)
            }catch (e : InterruptedException){
                e.printStackTrace()
            }
        }
    }

    fun setSpeed(speed : Float){
        reset()
        this.mSpeed = speed
    }
    fun getSpeed() : Float{
        return mSpeed
    }


    private fun usToMs(timeUs : Long) : Long{
        if(timeUs == TIME_UNSET || timeUs == TIME_END_OF_SOURCE)
            return timeUs
        else
            return timeUs / 1000
    }



}