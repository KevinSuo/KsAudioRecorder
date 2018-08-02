/*
 * ***************************************************
 *
 *  Copyright (c) 2018.  Kevin.Suo. All Rights Reserved.
 *  Create By Slackers on 2018-08-01
 *  Email: jinghao530@gmail.com
 *
 * ***************************************************
 */

package com.yuqi.ksaudiorecorder

import android.Manifest
import android.databinding.DataBindingUtil
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView
import com.yuqi.ksaudiorecorder.databinding.ActivityMainBinding
import org.kevinsuo.jsaudiorecorder.AudioRecordManager
import java.io.File

class MainActivity : AppCompatActivity(), OnClickListener {

  val recorder: AudioRecordManager by lazy { AudioRecordManager.newSimpleAacRecorder() }
  val path = File(Environment.getExternalStoragePublicDirectory(
      Environment.DIRECTORY_DOWNLOADS), "record.aac").absolutePath

  lateinit var text: TextView

  override fun onClick(v: View?) {
    when (v?.id) {
      R.id.start_record -> startRecord()
      R.id.stop_record -> stopRecord()
      R.id.play -> play()
    }
  }

  private fun stopRecord() {
    recorder.stopRecord()
    text.text = path
  }

  private fun startRecord() {
    recorder.startRecord(path)
  }

  private fun play() {
    val file = File(path)
    if (file.exists()) {
      val player = MediaPlayer()
      player.setDataSource(path)
      player.prepare()
      player.start()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val binding = DataBindingUtil.setContentView(this,
        R.layout.activity_main) as ActivityMainBinding

    binding.onClick = this
    text = binding.sampleText
    // Example of a call to a native method
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO), 100)
    }
  }
}
