package org.kevinsuo.jsaudiorecorder

/**
 *  Create by Kevin.Suo on 2018/8/1
 */
abstract class AudioEncoder {
  abstract fun start(path:String)
  abstract fun encode(data:ByteArray)
  abstract fun stop()
}