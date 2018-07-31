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

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Example of a call to a native method
    sample_text.text = stringFromJNI()
  }

  /**
   * A native method that is implemented by the 'native-lib' native library,
   * which is packaged with this application.
   */
  external fun stringFromJNI(): String

  companion object {

    // Used to load the 'native-lib' library on application startup.
    init {
      System.loadLibrary("native-lib")
    }
  }
}
