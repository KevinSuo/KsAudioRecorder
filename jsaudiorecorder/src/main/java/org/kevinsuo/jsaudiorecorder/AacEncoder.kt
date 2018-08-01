package org.kevinsuo.jsaudiorecorder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

/**
 *  Create by Kevin.Suo on 2018/8/1
 */
class AacEncoder(private val config: EncodeConfig) : AudioEncoder() {
  private var mBufferInfo: MediaCodec.BufferInfo? = null
  private var inputBufferArray: Array<ByteBuffer>? = null
  private var outputBufferArray: Array<ByteBuffer>? = null
  private var fileOutputStream: FileOutputStream? = null
  private var mMediaCodec: MediaCodec? = null

  override
  fun start(path: String) {
    try {
      fileOutputStream = FileOutputStream(path)
      mMediaCodec = MediaCodec.createEncoderByType(config.mime)
      val mediaFormat = MediaFormat()
      mediaFormat.setString(MediaFormat.KEY_MIME, config.mime)
      mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
          MediaCodecInfo.CodecProfileLevel.AACObjectLC)
      mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, config.bitRate)
      mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, config.channel)
      mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, config.bufferSize)
      mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, config.sampleRateInHz)
      /*
               第四个参数 编码的时候是MediaCodec.CONFIGURE_FLAG_ENCODE
                          解码的时候是0
             */
      mMediaCodec?.apply {
        configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        start() //start（）后进入执行状态，才能做后续的操作
        //获取输入缓存，输出缓存
        inputBufferArray = inputBuffers
        outputBufferArray = outputBuffers
      }

      mBufferInfo = MediaCodec.BufferInfo()
    } catch (e: Exception) {
      e.printStackTrace()
    }

  }

  override
  fun encode(data: ByteArray) {

    //dequeueInputBuffer（time）需要传入一个时间值，-1表示一直等待，0表示不等待有可能会丢帧，其他表示等待多少毫秒
    val inputIndex = mMediaCodec!!.dequeueInputBuffer(-1)//获取输入缓存的index
    if (inputIndex >= 0) {
      val inputByteBuf = inputBufferArray!![inputIndex]
      inputByteBuf.clear()
      inputByteBuf.put(data)//添加数据
      inputByteBuf.limit(data.size)//限制ByteBuffer的访问长度
      mMediaCodec!!.queueInputBuffer(inputIndex, 0, data.size, 0, 0)//把输入缓存塞回去给MediaCodec
    }

    var outputIndex = mMediaCodec!!.dequeueOutputBuffer(mBufferInfo!!, 0)//获取输出缓存的index
    while (outputIndex >= 0) {
      //获取缓存信息的长度
      val byteBufSize = mBufferInfo!!.size
      //添加ADTS头部后的长度
      val bytePacketSize = byteBufSize + 7

      val outPutBuf = outputBufferArray!![outputIndex]
      outPutBuf.position(mBufferInfo!!.offset)
      outPutBuf.limit(mBufferInfo!!.offset + mBufferInfo!!.size)

      val targetByte = ByteArray(bytePacketSize)
      //添加ADTS头部
      addADTStoPacket(targetByte, bytePacketSize)
      /*
            get（byte[] dst,int offset,int length）:ByteBuffer从position位置开始读，读取length个byte，并写入dst下
            标从offset到offset + length的区域
             */
      outPutBuf.get(targetByte, 7, byteBufSize)

      outPutBuf.position(mBufferInfo!!.offset)

      try {
        fileOutputStream!!.write(targetByte)
      } catch (e: IOException) {
        e.printStackTrace()
      }

      //释放
      mMediaCodec!!.releaseOutputBuffer(outputIndex, false)
      outputIndex = mMediaCodec!!.dequeueOutputBuffer(mBufferInfo!!, 0)
    }
  }

  override
  fun stop() {
    try {
      if (mMediaCodec != null) {
        mMediaCodec!!.stop()
        mMediaCodec!!.release()
        mMediaCodec = null
      }
      if (fileOutputStream != null) {
        fileOutputStream!!.flush()
        fileOutputStream!!.close()
      }
    } catch (e: IOException) {
      e.printStackTrace()
    }

  }

  /**
   * 给编码出的aac裸流添加adts头字段
   *
   * @param packet 要空出前7个字节，否则会搞乱数据
   */
  private fun addADTStoPacket(packet: ByteArray, packetLen: Int) {
    val profile = 2  //AAC LC
    val freqIdx = 4  //44.1KHz
    val chanCfg = 2  //CPE
    packet[0] = 0xFF.toByte()
    packet[1] = 0xF9.toByte()
    packet[2] = ((profile - 1 shl 6) + (freqIdx shl 2) + (chanCfg shr 2)).toByte()
    packet[3] = ((chanCfg and 3 shl 6) + (packetLen shr 11)).toByte()
    packet[4] = (packetLen and 0x7FF shr 3).toByte()
    packet[5] = ((packetLen and 7 shl 5) + 0x1F).toByte()
    packet[6] = 0xFC.toByte()
  }
}