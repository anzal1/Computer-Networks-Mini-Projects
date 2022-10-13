package com.example.remotemouse

import androidx.lifecycle.ViewModel
import java.io.BufferedWriter
import java.net.Socket

class MainViewModel : ViewModel() {
    private val TAG = "MainViewModel"
    var ip: String = ""
    var socket: Socket? = null
    var writer: BufferedWriter? = null
    var x = 0
    var y = 0
    var dx = 0
    var dy = 0
    var event: String = ""
    var click = false

    fun sendData() {
        while (socket!!.isConnected) {

            writer!!.write("$event $dx $dy")
            writer!!.newLine()
            writer!!.flush()

            Thread.sleep(3)
        }
    }

    fun sendLeftClick() {
        Thread {
            writer!!.write("leftClick $dx $dy")
            writer!!.newLine()
            writer!!.flush()
            Thread.currentThread().join()
        }.start()
    }

    fun sendRightClick() {
        Thread {
            writer!!.write("rightClick $dx $dy")
            writer!!.newLine()
            writer!!.flush()
            Thread.currentThread().join()
        }.start()
    }
}