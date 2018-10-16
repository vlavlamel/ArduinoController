package com.arduinocontroller

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat

class BluetoothHelper private constructor() {
    private val bluetoothAdapter: BluetoothAdapter?

    init {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    private object Holder {
        val INSTANCE = BluetoothHelper()
    }

    companion object {
        val instance: BluetoothHelper by lazy { Holder.INSTANCE }
    }

    fun checkState(context: Context): BluetoothState {
        if (bluetoothAdapter != null) {
            if (ContextCompat.checkSelfPermission(context,
                            Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context,
                            Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                return BluetoothState.NEED_PERMISSION
            }
            if (bluetoothAdapter.isEnabled) {
                return BluetoothState.READY
            } else {
                return BluetoothState.DISABLED
            }
        } else {
            return BluetoothState.NOT_SUPPORTED
        }
    }

    fun getPairedDevices(): Set<BluetoothDevice> {
        if (bluetoothAdapter != null) {
            return bluetoothAdapter.bondedDevices
        } else {
            throw IllegalAccessException("Check state before use this method")
        }
    }
}

enum class BluetoothState {
    NOT_SUPPORTED,
    DISABLED,
    NEED_PERMISSION,
    READY
}