package com.example.camera_card_detection


import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {
    var btnTake: Button? = null
    var btnSelect: Button? = null
    var ivShow: ImageView? = null
    var photoFile: File? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnTake = findViewById<View>(R.id.btn_take) as Button
        btnSelect = findViewById<View>(R.id.btn_select) as Button
        ivShow = findViewById<View>(R.id.iv_show) as ImageView
        photoFile = File(getExternalFilesDir("img"), "scan.jpg")
        btnTake!!.setOnClickListener {
            startActivityForResult(
                CropActivity.getJumpIntent(
                    this@MainActivity,
                    false,
                    photoFile
                ), 100
            )
        }
        btnSelect!!.setOnClickListener {
            startActivityForResult(
                CropActivity.getJumpIntent(
                    this@MainActivity,
                    true,
                    photoFile
                ), 100
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            return
        }
        if (requestCode == 100 && photoFile!!.exists()) {
            val bitmap = BitmapFactory.decodeFile(
                photoFile!!.path
            )
            ivShow!!.setImageBitmap(bitmap)
        }
    }
}