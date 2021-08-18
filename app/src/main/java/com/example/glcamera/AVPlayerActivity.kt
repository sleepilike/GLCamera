package com.example.glcamera

import android.media.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Process
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.glcamera.player.decoder.AVDecoder
import com.example.glcamera.player.decoder.AVSyncClock
import com.example.glcamera.player.view.PlayerSurfaceView
import java.io.File
import java.nio.ByteBuffer


class AVPlayerActivity : AppCompatActivity() {

    /**
     * 音频解码及播放
     */
    private var mAudioTrack : AudioTrack? = null
    private var mBufferSize = 0
    private var mAudioDecoder : AVDecoder? = null

    /**
     * 视频解码及播放
     *
     */

    private var mSurfaceView : PlayerSurfaceView? = null
    private var mVideoDecoder : AVDecoder? = null


    /**
     * 音视频同步
     */
    private var mAVSyncClock : AVSyncClock? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_avplayer)
        if (supportActionBar != null){
            supportActionBar?.hide()
        }
        mSurfaceView = findViewById(R.id.player_view)

        Log.d("TAG", "onCreate: ${Process.myTid()}")
        Thread{
           doDecoder()
        }.start()
    }

    private fun doDecoder(){

        var uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.demo_video)
        //var path = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera/a.mp4"
      //  var file = File(path)
       // var uri = Uri.fromFile(file)
        Log.d("TAG", "doDecoder: $uri")
        mAVSyncClock = AVSyncClock()
        mAVSyncClock?.start()


        /**
         * 视频解码
         */
        Thread {
            mVideoDecoder = AVDecoder(this,uri,"video/")
            mVideoDecoder!!.setDelegate(object : AVDecoder.AVDecoderDelegate{
                override fun newFrameReady(
                    byteBuffer: ByteBuffer?,
                    bufferInfo: MediaCodec.BufferInfo?,
                ) {
                    //锁定时钟
                    mAVSyncClock?.lock(bufferInfo!!.presentationTimeUs,0)

                }

                override fun outputFormatChanged(mediaFormat: MediaFormat?) {

                }

            })
            mVideoDecoder?.startDecoder(mSurfaceView?.mRender!!.getSurface())
        }.start()

        /**
         * 音频解码回调
         */
        Thread{
            mAudioDecoder = AVDecoder(this,uri,"audio/")
            mAudioDecoder!!.setDelegate(object : AVDecoder.AVDecoderDelegate{
                @RequiresApi(Build.VERSION_CODES.M)
                override fun newFrameReady(
                    byteBuffer: ByteBuffer?,
                    bufferInfo: MediaCodec.BufferInfo?,
                ) {
                    mAVSyncClock?.lock(bufferInfo!!.presentationTimeUs,0)
                    mAudioTrack!!.write(byteBuffer!!,
                        bufferInfo!!.size,
                        AudioTrack.WRITE_BLOCKING,
                        bufferInfo.presentationTimeUs)
                }

                override fun outputFormatChanged(mediaFormat: MediaFormat?) {
                    var sampleRate = 44100
                    if (mediaFormat!!.containsKey(MediaFormat.KEY_SAMPLE_RATE)) sampleRate =
                        mediaFormat!!.getInteger(MediaFormat.KEY_SAMPLE_RATE)

                    var channelConfig = AudioFormat.CHANNEL_OUT_MONO

                    if (mediaFormat!!.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) channelConfig =
                        if (mediaFormat!!.getInteger(MediaFormat.KEY_CHANNEL_COUNT) == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO


                    var audioFormat = AudioFormat.ENCODING_PCM_16BIT

                    if (mediaFormat!!.containsKey("bit-width")) audioFormat =
                        if (mediaFormat!!.getInteger("bit-width") == 8) AudioFormat.ENCODING_PCM_8BIT else AudioFormat.ENCODING_PCM_16BIT

                    mBufferSize =
                        AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 2
                    mAudioTrack = AudioTrack(AudioManager.STREAM_MUSIC,
                        sampleRate,
                        channelConfig,
                        audioFormat,
                        mBufferSize,
                        AudioTrack.MODE_STREAM)
                    mAudioTrack!!.play()

                }

            })
            mAudioDecoder?.startDecoder(null)

        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mAudioDecoder?.stopDecoder()
        mVideoDecoder?.stopDecoder()
    }

    override fun onPause() {
        super.onPause()
        mAudioDecoder?.stopDecoder()
        mVideoDecoder?.stopDecoder()
    }



}