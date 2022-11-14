package com.example.camera_card_detection

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import kotlin.reflect.*
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import me.pqpo.smartcropperlib.view.CropImageView
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class CropActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    var ivCrop: CropImageView? = null
    var btnCancel: Button? = null
    var btnOk: Button? = null
    var mFromAlbum = false
    var mCroppedFile: File? = null
    var tempFile: File? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)
        ivCrop = findViewById(R.id.iv_crop) as CropImageView?
        btnCancel = findViewById(R.id.btn_cancel) as Button?
        btnOk = findViewById(R.id.btn_ok) as Button?
        btnCancel!!.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
        btnOk!!.setOnClickListener {
            if (ivCrop!!.canRightCrop()) {
                val crop = ivCrop!!.crop()
                if (crop != null) {
                    saveImage(crop, mCroppedFile)
                    setResult(RESULT_OK)
                } else {
                    setResult(RESULT_CANCELED)
                }
                finish()
            } else {
                Toast.makeText(this@CropActivity, "cannot crop correctly", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        mFromAlbum = getIntent().getBooleanExtra(EXTRA_FROM_ALBUM, true)
        mCroppedFile = getIntent().getSerializableExtra(EXTRA_CROPPED_FILE) as File?
        if (mCroppedFile == null) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }
        tempFile = File(getExternalFilesDir("img"), "temp.jpg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            EasyPermissions.requestPermissions(
                this@CropActivity,
                "申请权限",
                0,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        } else {
            selectPhoto()
        }
    }

    override fun onRequestPermissionsResult(requestCode:Int,
                                            permissions:Array<String>,
                                            grantResults:IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }




    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.d(ContentValues.TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size)

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms))
        {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.d(ContentValues.TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size)


        // Some permissions have been granted
        // ...
        selectPhoto()
    }
    private fun selectPhoto() {
        if (mFromAlbum) {
            val selectIntent = Intent(Intent.ACTION_PICK)
            selectIntent.type = "image/*"
            if (selectIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(selectIntent, REQUEST_CODE_SELECT_ALBUM)
            }
        } else {
            val startCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val uri: Uri
            uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(this, "com.example.camera_card_detection.fileProvider", tempFile!!)
            } else {
                Uri.fromFile(tempFile)
           }
            startCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            if (startCameraIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(startCameraIntent, REQUEST_CODE_TAKE_PHOTO)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }
        var selectedBitmap: Bitmap? = null
        if (requestCode == REQUEST_CODE_TAKE_PHOTO && tempFile!!.exists()) {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(tempFile!!.path, options)
            options.inJustDecodeBounds = false
            options.inSampleSize = calculateSampleSize(options)
            selectedBitmap = BitmapFactory.decodeFile(tempFile!!.path, options)
        } else if (requestCode == REQUEST_CODE_SELECT_ALBUM && data != null && data.data != null) {
            val cr: ContentResolver = getContentResolver()
            val bmpUri = data.data
            try {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(cr.openInputStream(bmpUri!!), Rect(), options)
                options.inJustDecodeBounds = false
                options.inSampleSize = calculateSampleSize(options)
                selectedBitmap =
                    BitmapFactory.decodeStream(cr.openInputStream(bmpUri), Rect(), options)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
        if (selectedBitmap != null) {
            ivCrop!!.setImageToCrop(selectedBitmap)
        }
    }

    private fun saveImage(bitmap: Bitmap, saveFile: File?) {
        try {
            val fos = FileOutputStream(saveFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun calculateSampleSize(options: BitmapFactory.Options): Int {
        val outHeight = options.outHeight
        val outWidth = options.outWidth
        var sampleSize = 1
        val destHeight = 1000
        val destWidth = 1000
        if (outHeight > destHeight || outWidth > destHeight) {
            sampleSize = if (outHeight > outWidth) {
                outHeight / destHeight
            } else {
                outWidth / destWidth
            }
        }
        if (sampleSize < 1) {
            sampleSize = 1
        }
        return sampleSize
    }

    companion object {
        private const val EXTRA_FROM_ALBUM = "extra_from_album"
        private const val EXTRA_CROPPED_FILE = "extra_cropped_file"
        private const val REQUEST_CODE_TAKE_PHOTO = 100
        private const val REQUEST_CODE_SELECT_ALBUM = 200
        fun getJumpIntent(context: Context, fromAlbum: Boolean, croppedFile: File?): Intent {
            val intent = Intent(context, CropActivity::class.java)
            intent.putExtra(EXTRA_FROM_ALBUM, fromAlbum)
            intent.putExtra(EXTRA_CROPPED_FILE, croppedFile)
            return intent
        }
    }
}

