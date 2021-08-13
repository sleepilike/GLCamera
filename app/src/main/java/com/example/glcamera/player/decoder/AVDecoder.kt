package com.example.glcamera.player.decoder

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodec.*
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.util.Log
import android.view.Surface
import java.io.IOException
import java.nio.ByteBuffer

/**
 * Created by zyy on 2021/8/11
 *
 */
class AVDecoder (context: Context,uri: Uri,mimeType : String){

    private var mDecodeMimeType : String = mimeType
    private var mContext : Context? = context
    private var mUri : Uri? = uri

    private var mDelegate : AVDecoderDelegate? = null

    private var isRunning : Boolean = false

    interface AVDecoderDelegate{
        fun newFrameReady(byteBuffer: ByteBuffer?,bufferInfo: MediaCodec.BufferInfo?)
        fun outputFormatChanged(mediaFormat: MediaFormat?)
    }

    fun setDelegate(delegate: AVDecoderDelegate){
        this.mDelegate = delegate
    }

    /**
     * 喂养数据到解码器
     */
    private fun feedInputBuffer(source : MediaExtractor?,codec : MediaCodec?):Boolean{
        if(source == null ||codec == null) return false

        var index = codec.dequeueInputBuffer(0)
        if(index < 0) return false

        var codecInputBuffer : ByteBuffer? = codec.getInputBuffer(index)
        codecInputBuffer?.position(0)
        var sampleDataSize = 0
        if (codecInputBuffer != null)
            sampleDataSize = source.readSampleData(codecInputBuffer,0)

        if(sampleDataSize <= 0){

            //通知解码器结束
            if (index >= 0)
                codec.queueInputBuffer(index,0,0,0,MediaCodec.BUFFER_FLAG_END_OF_STREAM)
            return false
        }

        var bufferInfo : MediaCodec.BufferInfo? = MediaCodec.BufferInfo()
        bufferInfo?.offset = 0
        bufferInfo?.presentationTimeUs = source.sampleTime
        bufferInfo?.size = sampleDataSize
        bufferInfo?.flags = source.sampleFlags

        when(index){
            INFO_TRY_AGAIN_LATER -> return true
            else -> {
                codec.queueInputBuffer(index,
                    bufferInfo?.offset!!,
                    bufferInfo?.size!!,
                    bufferInfo?.presentationTimeUs!!,
                    bufferInfo?.flags!!)
                source.advance()
                return true
            }
        }
    }

    /**
     * 吐出解码后的数据
     */
    private fun drainOutBuffer(mediaCodec: MediaCodec?): Boolean{
        if (mediaCodec == null) return false

        val bufferInfo = BufferInfo()
        var index = mediaCodec.dequeueOutputBuffer(bufferInfo,0)

        if ((bufferInfo.flags and BUFFER_FLAG_END_OF_STREAM) != 0){
            mediaCodec.releaseOutputBuffer(index,false)
            return false
        }

        when(index){
            INFO_OUTPUT_BUFFERS_CHANGED -> return true
            INFO_TRY_AGAIN_LATER -> return true
            INFO_OUTPUT_FORMAT_CHANGED -> {
                var outputFormat = mediaCodec.outputFormat
                if(mDelegate != null)
                    mDelegate?.outputFormatChanged(outputFormat)
                return true
            }
            else -> {
                if (index >= 0 && bufferInfo.size > 0){
                    val info = BufferInfo()
                    info.presentationTimeUs = bufferInfo.presentationTimeUs
                    info.size = bufferInfo.size
                    info.flags = bufferInfo.flags
                    info.offset = bufferInfo.offset


                    var outputBuffer = mediaCodec.getOutputBuffer(index)
                    outputBuffer?.position(info.offset)
                    outputBuffer?.limit(info.offset+info.size)

                    if (mDelegate != null && mDecodeMimeType.equals("audio/", ignoreCase = true)) {
                        mDelegate!!.newFrameReady(outputBuffer, bufferInfo)
                        mediaCodec.releaseOutputBuffer(index, true)
                    }else{
                        mediaCodec.releaseOutputBuffer(index,true)
                        mDelegate!!.newFrameReady(outputBuffer,bufferInfo)
                    }
                }
                return true
            }
        }
    }


    /**
     * 启动解码器
     */
    fun startDecoder(surface : Surface?){

        //1. 创建一个媒体分离器
        var mediaExtractor = MediaExtractor()

        //2.为媒体分离器装载媒体文件路径
        try {
            mediaExtractor.setDataSource(mContext!!,mUri!!,null)
        }catch (e : IOException){
            e.printStackTrace()
        }

        //3.获取并选中指定类型的轨道
        //媒体文件中的轨道数量 视频、音频、字幕 ···
        var trackCount = mediaExtractor.trackCount
        //指定要分离的轨道类型
        var extractMimeType = mDecodeMimeType
        var trackFormat : MediaFormat? = null
        //记录轨道索引id MediaExtractor读取数据之前需要指定分离的轨道索引
        var trackId = -1
        for (i in 0 until trackCount) {
            trackFormat = mediaExtractor.getTrackFormat(i)
            if(trackFormat.getString(MediaFormat.KEY_MIME)?.startsWith(extractMimeType) == true){
                trackId = i
                break
            }
        }

        //4.选中指定类型的轨道
        if (trackId != -1){
            mediaExtractor.selectTrack(trackId)
        }

        //5.根据mediaFormat 创建解码器
        var mediaCodec : MediaCodec? = null
        try {
            mediaCodec = createDecoderByType(trackFormat?.getString(MediaFormat.KEY_MIME)!!)
            mediaCodec.configure(trackFormat,surface,null,0)
            mediaCodec.start()
        }catch (e : IOException){
            e.printStackTrace()
        }

        isRunning = true

        while (isRunning){

            //6.向解码器喂入数据
            var ret = feedInputBuffer(mediaExtractor,mediaCodec)

            //7.解码器吐出数据
            var decRet = drainOutBuffer(mediaCodec)

            if(!ret && !decRet)
                break
        }

        //8.释放资源
        mediaExtractor.release()
        mediaCodec?.release()
    }

    fun stopDecoder(){
        isRunning = false
    }
}