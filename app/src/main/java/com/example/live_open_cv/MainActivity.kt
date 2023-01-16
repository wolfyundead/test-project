package com.example.live_open_cv

import android.R.attr
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import info.hannes.liveedgedetection.ScanConstants
import info.hannes.liveedgedetection.activity.ScanActivity
import info.hannes.liveedgedetection.utils.ScanUtils


class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE = 101
    private var scannedImageView: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        scannedImageView = findViewById(R.id.scanned_image)
        startScan();
    }

    private fun startScan() {
        val intent = Intent(this, ScanActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (null != data && null != data.extras) {
                   // val byteArray=data.extras!!.getByteArray("image")
                   // val imgURI = Uri.parse(data.getStringExtra("img_uri"))
                    val filePath =
                        data.extras!!.getString(ScanConstants.SCANNED_RESULT)
                    val baseBitmap: Bitmap = BitmapFactory.decodeFile(filePath)
                       // ScanUtils.decodeBitmapFromFile(filePath, ScanConstants.IMAGE_NAME)
                  //  val bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
                    scannedImageView!!.scaleType = ImageView.ScaleType.FIT_CENTER
                    scannedImageView!!.setImageBitmap(baseBitmap)
                }
            } else if (resultCode == RESULT_CANCELED) {
                finish()
            }
        }
    }
}