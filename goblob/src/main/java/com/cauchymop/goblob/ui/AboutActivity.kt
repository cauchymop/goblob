package com.cauchymop.goblob.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.method.LinkMovementMethod
import com.cauchymop.goblob.R
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_about)
    about_text.text = Html.fromHtml(getString(R.string.about_text))
    about_text.setMovementMethod(LinkMovementMethod.getInstance())
  }
}
