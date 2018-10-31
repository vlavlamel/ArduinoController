package com.arduinocontroller

import android.animation.ValueAnimator
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView

class MainActivity : AppCompatActivity(), BluetoothStateChangeListener {
    val animator = ValueAnimator.ofFloat(0f, 360f)

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    BluetoothHelper.instance.discoveredDevice(device)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, ControllerFragment())
                .addToBackStack(null)
                .commit()
        initAnimator()
        registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        BluetoothHelper.instance.subscribe(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
        BluetoothHelper.instance.disconnect()
        BluetoothHelper.instance.unsubscribe(this)
    }

    override fun onPause() {
        super.onPause()
        stopDiscovering()
    }

    private fun initAnimator() {
        animator.repeatCount = ValueAnimator.INFINITE
        animator.duration = 500
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.controller_menu, menu)
        val imageView = ImageView(this)
        imageView.setImageResource(R.drawable.ic_discovery)
        imageView.setPadding(15, 15, 15, 15)
        imageView.setOnClickListener { actionDiscovery(it) }
        menu?.findItem(R.id.discovery)?.actionView = imageView
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (BluetoothHelper.instance.isConnected()) {
            menu?.findItem(R.id.bluetooth)?.icon = ContextCompat.getDrawable(this, R.drawable.ic_bluetooth_cn)
        } else {
            menu?.findItem(R.id.bluetooth)?.icon = ContextCompat.getDrawable(this, R.drawable.ic_bluetooth)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.bluetooth -> {
                supportFragmentManager.beginTransaction()
                        .add(android.R.id.content, BluetoothFragment())
                        .addToBackStack(null)
                        .commit()
                return true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun actionDiscovery(view: View) {
        if (BluetoothHelper.instance.lastBluetoothState == BluetoothState.READY) {
            if (BluetoothHelper.instance.isDiscovering()) {
                stopDiscovering()
            } else {
                if (!animator.isStarted) {
                    animator.addUpdateListener {
                        view.rotation = it.animatedValue as Float
                        view.requestLayout()
                    }
                    animator.start()
                } else {
                    animator.resume()
                }
                BluetoothHelper.instance.startDiscovery()
            }
        } else {
            BluetoothHelper.instance.checkState(this)
        }
    }

    private fun stopDiscovering() {
        animator.pause()
        BluetoothHelper.instance.cancelDiscovery()
    }

    override fun stateChange(state: BluetoothState) {
        when (state) {
            BluetoothState.CONNECTING -> {
                animator.cancel()
            }
            BluetoothState.CONNECTED -> {
                if (supportFragmentManager.backStackEntryCount > 1) supportFragmentManager.popBackStack()
                invalidateOptionsMenu()
            }
        }
    }

    override fun discoveredDevice(device: BluetoothDevice) {
    }

    override fun onBackPressed() {
        animator.cancel()
        if (supportFragmentManager.backStackEntryCount == 1) {
            finish()
        } else {
            super.onBackPressed()
        }
    }
}
