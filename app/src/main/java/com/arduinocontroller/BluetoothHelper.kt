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

    private val subscribers = mutableSetOf<BluetoothStateChangeListener>()

    var lastBluetoothState = BluetoothState.DEFAULT
        private set(value) {
            field = value
            subscribers.forEach { it.stateChange(value) }
        }

    fun subscribe(subscriber: BluetoothStateChangeListener) {
        subscribers.add(subscriber)
    }

    fun unsubscribe(subscriber: BluetoothStateChangeListener) {
        subscribers.remove(subscriber)
    }

    fun checkState(context: Context): BluetoothState {
        if (bluetoothAdapter != null) {
            if (ContextCompat.checkSelfPermission(context,
                            Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context,
                            Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                lastBluetoothState = BluetoothState.NEED_PERMISSION
            }
            if (bluetoothAdapter.isEnabled) {
                lastBluetoothState = BluetoothState.READY
            } else {
                lastBluetoothState = BluetoothState.DISABLED
            }
        } else {
            lastBluetoothState = BluetoothState.NOT_SUPPORTED
        }
        return lastBluetoothState
    }

    fun getPairedDevices(): Set<BluetoothDevice> {
        if (bluetoothAdapter != null) {
            return bluetoothAdapter.bondedDevices
        } else {
            throw IllegalAccessException("Check state before use this method")
        }
    }

    fun cancelDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
    }

    fun startDiscovery() {
        bluetoothAdapter?.startDiscovery()
    }

    fun isDiscovering(): Boolean {
        if (bluetoothAdapter != null) {
            return bluetoothAdapter.isDiscovering
        } else {
            throw IllegalAccessException("Check state before use this method")
        }
    }

    fun discoveredDevice(device: BluetoothDevice) {
        subscribers.forEach { it.discoveredDevice(device) }
    }
}

interface BluetoothStateChangeListener {
    fun stateChange(state: BluetoothState)
    fun discoveredDevice(device: BluetoothDevice)
}

enum class BluetoothState {
    DEFAULT,
    NOT_SUPPORTED,
    NEED_PERMISSION,
    DISABLED,
    READY
}