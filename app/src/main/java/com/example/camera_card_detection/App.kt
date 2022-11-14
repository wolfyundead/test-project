package com.example.camera_card_detection

import android.app.Application
import me.pqpo.smartcropperlib.SmartCropper


class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // 如果使用机器学习代替 Canny 算子，请初始化 ImageDetector
        SmartCropper.buildImageDetector(this)
    }
}