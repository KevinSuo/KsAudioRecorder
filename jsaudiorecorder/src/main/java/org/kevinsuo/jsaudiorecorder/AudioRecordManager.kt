package org.kevinsuo.jsaudiorecorder

import android.media.AudioRecord
import android.media.MediaFormat
import android.media.MediaRecorder
import android.os.Process
import android.util.Log
import java.io.File
import java.util.concurrent.Executors

/**
 *  Create by Kevin.Suo on 2018/8/1
 */
class AudioRecordManager private constructor(recorderConfig: AudioRecordConfig) {

  private val tag = javaClass.simpleName
  private val mRecordConfig: AudioRecordConfig = recorderConfig
  private val mAudioRecord: AudioRecord
  private var mAudioEncoder: AudioEncoder? = null
  private val threadPool = Executors.newSingleThreadExecutor()

  private var outPutPath: String? = null
  private var isRecording = false
  private val bufferSize: Int = recorderConfig.getMinBufferSize()
  private val mAudioFilters = ArrayList<AudioFilter>()

  init {
    mAudioRecord = AudioRecord(mRecordConfig.audioSource, mRecordConfig.sampleRateInHz,
        mRecordConfig.channelConfig, mRecordConfig.audioFormat, bufferSize)
  }

  private fun setPath(path: String) {
    val outFile = File(path)
    if (outFile.exists()) {
      outFile.delete()
    }
    this.outPutPath = path
  }

  fun addAudioFilter(filter: AudioFilter) {
    mAudioFilters.add(filter)
  }

  fun startRecord(outPutPath: String) {
    Log.i(tag, "start record with output path $outPutPath")
    setPath(outPutPath)
    mAudioEncoder?.start(outPutPath)
    threadPool.execute(recordTask)
  }

  fun stopRecord() {
    Log.i(tag, "stop record")
    isRecording = false
    if (mAudioRecord.state == AudioRecord.STATE_INITIALIZED) {
      mAudioRecord.stop()
//      mAudioRecord.release()
    }
    mAudioEncoder?.stop()
  }

  fun release() {
    stopRecord()
    mAudioRecord.release()
  }

  private val recordTask = Runnable {
    try {
      Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO) // 设置线程优先级
      isRecording = true
      if (mAudioRecord.state != AudioRecord.STATE_INITIALIZED) {
        stopRecord()
        return@Runnable
      }

      mAudioRecord.startRecording()

      var record: Int
      val buffer = ByteArray(bufferSize)
      while (isRecording) {
        record = mAudioRecord.read(buffer, 0, bufferSize)
        if (record == AudioRecord.ERROR_INVALID_OPERATION || record == AudioRecord.ERROR_BAD_VALUE) {
          Log.i(tag, "record continue")
          continue
        }

        if (record > 0) {
          var pcm = buffer.copyOfRange(0, record)
          Log.i(tag, "get pcm data size:${pcm.size}")

          if (mAudioFilters.isNotEmpty()) {
            mAudioFilters.forEach {
              pcm = it.handleFilter(pcm)
            }
          }

          mAudioEncoder?.encode(pcm)
        } else {
          isRecording = false
          Log.i(tag, "record break")
          break
        }
      }

    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  companion object {
    fun newAudioRecorderManager(recordConfig: AudioRecordConfig,
        audioEncoder: AudioEncoder?): AudioRecordManager {
      return AudioRecordManager(recordConfig).apply {
        this.mAudioEncoder = audioEncoder
      }
    }

    fun newSimpleAacRecorder(): AudioRecordManager {
      val recordConfig = AudioRecordConfig(
          audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION)
      val encoder = AacEncoder(
          EncodeConfig(MediaFormat.MIMETYPE_AUDIO_AAC, 96000, 1, recordConfig.getMinBufferSize(),
              recordConfig.sampleRateInHz))
      return newAudioRecorderManager(recordConfig, encoder)
    }

  }

}