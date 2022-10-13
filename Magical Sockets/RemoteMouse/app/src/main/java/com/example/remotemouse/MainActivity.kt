package com.example.remotemouse

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.remotemouse.databinding.ActivityMainBinding
import com.example.remotemouse.databinding.IpDialogLayoutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.Socket


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var socketThread: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        getIp()
    }

    private fun getIp() {
        val dialogBinding = DataBindingUtil.inflate<IpDialogLayoutBinding>(
            LayoutInflater.from(this), R.layout.ip_dialog_layout, null, false
        )
        dialogBinding.ipEditText.setText(viewModel.ip)
        MaterialAlertDialogBuilder(this).setTitle("Enter IP").setView(dialogBinding.root)
            .setPositiveButton("Connect") { _, _ ->
                val ip = dialogBinding.ipEditText.text.toString()
                viewModel.ip = ip
                setTouchPad()
                setMouseButtons()
                connectToServer(ip)
            }.setNegativeButton("Exit") { _, _ ->
                finish()
            }.setCancelable(false).show()
    }

    private fun connectToServer(ip: String) {
        socketThread = Thread {
            try {
                viewModel.socket = Socket(ip, 8080)
                viewModel.socket!!.tcpNoDelay = true
                runOnUiThread {
                    Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show()
                }
                viewModel.writer =
                    BufferedWriter(OutputStreamWriter(viewModel.socket!!.getOutputStream()))

                viewModel.event = "move"
                viewModel.sendData()
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Error Connecting to Server", Toast.LENGTH_SHORT).show()
                    getIp()
                    return@runOnUiThread
                }
                e.printStackTrace()
            }
        }
        socketThread.start()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchPad() {

        binding.touchPad.setOnTouchListener { _, event ->
            when (event.action) {
                0 -> {
                    viewModel.x = event.x.toInt()
                    viewModel.y = event.y.toInt()
                    viewModel.event = "down"
                    viewModel.click = true
                }
                2 -> {
                    viewModel.dx = (event.x.toInt() - viewModel.x) / 2
                    viewModel.dy = (event.y.toInt() - viewModel.y) / 2
                    viewModel.x = event.x.toInt()
                    viewModel.y = event.y.toInt()
                    viewModel.event = "move"
                    viewModel.click = false
                }
                1 -> {
                    viewModel.event = "up"
                    if (viewModel.click) {
                        viewModel.sendLeftClick()
                        viewModel.click = false
                    }
                }
            }
            true
        }
    }

    private fun setMouseButtons() {
        binding.leftClick.setOnClickListener {
            viewModel.sendLeftClick()
        }
        binding.rightClick.setOnClickListener {
            viewModel.sendRightClick()
        }
    }


    override fun onStop() {
        super.onStop()
        viewModel.socket!!.close()
        socketThread.join()
    }

}