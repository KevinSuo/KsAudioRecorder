package org.kevinsuo.jsaudiorecorder

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder

/**
 *  Create by Kevin.Suo on 2018/8/1
 */
data class AudioRecordConfig(val audioSource: Int = MediaRecorder.AudioSource.MIC,
    val sampleRateInHz: Int = 44100,
    val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT) {

  fun getMinBufferSize(): Int {
    return AudioRecord.getMinBufferSize(sampleRateInHz,
        channelConfig, audioFormat)
  }
}