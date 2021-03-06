package com.arduinocontroller

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.util.Log
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

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
        private val defaultUUID = "00001101-0000-1000-8000-00805F9B34FB"
    }

    private val subscribers = mutableSetOf<BluetoothStateChangeListener>()
    private var socket: BluetoothSocket? = null
    private val exceptionHandler: CoroutineContext = CoroutineExceptionHandler { _, throwable ->
        Log.e("BluetoothHelper", throwable.toString())
    }

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

    fun connectDevice(device: BluetoothDevice) = GlobalScope.launch(Dispatchers.Main + exceptionHandler) {
        lastBluetoothState = BluetoothState.CONNECTING
        bluetoothAdapter?.cancelDiscovery()
        socket = device.createRfcommSocketToServiceRecord(UUID.fromString(defaultUUID))
        withContext(Dispatchers.IO) { socket?.connect() }
        lastBluetoothState = BluetoothState.CONNECTED
    }

    fun isConnected(): Boolean {
        return if (socket == null) false else socket!!.isConnected
    }

    fun disconnect() {
        socket?.close()
    }

    fun sendData(data: String) = GlobalScope.launch(Dispatchers.IO + exceptionHandler) {
        if (socket != null && socket!!.isConnected) {
            socket?.outputStream?.write(data.toByteArray())
        }
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
    READY,
    CONNECTING,
    CONNECTED;
}