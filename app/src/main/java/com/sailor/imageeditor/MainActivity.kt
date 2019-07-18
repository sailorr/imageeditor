package com.sailor.imageeditor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.sailor.imaging.IMGEditActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private val CODE_SELECT_IMAGE = 2
    private val CODE_CAPTURE_PICTURE = 3
    private val CODE_IMG_EDIT = 4;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        orginal.setOnClickListener {
            val albumIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(albumIntent, CODE_SELECT_IMAGE)
        }

        Thread(Runnable {
            var url = Environment.getExternalStorageDirectory().absolutePath + "/testaa"

            path = "$url/a.jpg"
            var file = File(url)
            if (!file.exists()) {
                file.mkdirs()
            }
        }).start()

        edit.setOnClickListener {
//            val intent = Intent(this@MainActivity, IMGEditActivity::class.java)
//                .putExtra(IMGEditActivity.EXTRA_IMAGE_URI, uri)
//                .putExtra(IMGEditActivity.EXTRA_IMAGE_SAVE_PATH, path)
//            startActivityForResult(intent, CODE_IMG_EDIT)
//            Log.d("MainActivity", "onCreate: setOnClickListener")
//            var intent = Intent(this@MainActivity,IMGEditActivity::class.java)
//            startActivity(intent)


            startActivityForResult(Intent(this, IMGEditActivity::class.java)
                .putExtra(IMGEditActivity.EXTRA_IMAGE_URI, uri)
                .putExtra(IMGEditActivity.EXTRA_IMAGE_SAVE_PATH, path), CODE_IMG_EDIT)
        }
    }

    var path: String? = null
    var uri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("MainActivity", "onActivityResult: "+data?.data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CODE_SELECT_IMAGE -> {
                    uri = data?.data
                    orginal.setImageURI(uri)
                }
                CODE_IMG_EDIT -> {

                }
            }

        }
    }
}


