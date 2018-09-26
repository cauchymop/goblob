package com.cauchymop.goblob.ui

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.cauchymop.goblob.R
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_about)
    setSupportActionBar(app_toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    setTitle(R.string.about)
    about_text.text = Html.fromHtml(getString(R.string.about_text))
    about_text.setMovementMethod(LinkMovementMethod.getInstance())
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    when (item?.itemId) {
      android.R.id.home -> {
        finish()
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }
}
