package com.arduinocontroller

import android.Manifest
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import kotlinx.android.synthetic.main.fragment_bluetooth.*

class BluetoothFragment : Fragment() {
    companion object {
        val REQUEST_ENABLE_BT = 101
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bluetooth, container, false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(DividerDecoration())
        initActionBar()
        initBluetooth()
    }

    private fun initActionBar() {
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(toolBar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setHomeButtonEnabled(true)

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.findItem(R.id.bluetooth)?.setVisible(false)
        menu?.findItem(R.id.discovery)?.setVisible(true)
    }

    private fun initBluetooth() {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null) {
            if (ContextCompat.checkSelfPermission(context!!,
                            Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context!!,
                            Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(activity!!,
                        arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN),
                        REQUEST_ENABLE_BT)

            } else {
                if (bluetoothAdapter.isEnabled) {
                    viewFlipper.displayedChild = 0
                    initPairedDevices(bluetoothAdapter)
                } else {
                    startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT)
                }
            }
        } else {
            viewFlipper.displayedChild = 1
            errorText.text = getString(R.string.not_supported_bluetooth)
        }
    }

    private fun initPairedDevices(bluetoothAdapter: BluetoothAdapter) {
        recyclerView.adapter = BluetoothItemsAdapter(bluetoothAdapter.bondedDevices.toList())
        recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                if (resultCode == RESULT_OK) {
                    initBluetooth()
                } else {
                    viewFlipper.displayedChild = 1
                    errorText.text = getString(R.string.turn_on_bluetooth)
                    errorText.setOnClickListener { initBluetooth() }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    initBluetooth()
                } else {
                    viewFlipper.displayedChild = 1
                    errorText.text = getString(R.string.no_permission_bluetooth)
                }
                return
            }
        }
    }
}