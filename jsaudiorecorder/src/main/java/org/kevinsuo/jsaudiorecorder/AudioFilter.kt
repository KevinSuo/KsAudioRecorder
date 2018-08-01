package org.kevinsuo.jsaudiorecorder

/**
 *  Create by Kevin.Suo on 2018/8/1
 */
abstract class AudioFilter {

  abstract fun handleFilter(inputPcm: ByteArray): ByteArray

}