package org.kevinsuo.jsaudiorecorder

/**
 *  Create by Kevin.Suo on 2018/8/1
 */
data class EncodeConfig(val mime: String, val bitRate: Int, val channel: Int, val bufferSize: Int,
    val sampleRateInHz: Int = 44100)