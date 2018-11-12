package com.arduinocontroller

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_controller.*
import kotlinx.android.synthetic.main.item_arrow_control.*

class ControllerFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_controller, container, false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initActionBar()
        initClickListeners()
        initDragAndDrop()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.findItem(R.id.bluetooth)?.setVisible(true)
        menu?.findItem(R.id.discovery)?.setVisible(false)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun initActionBar() {
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(toolBar)
    }

    private fun initClickListeners() {
        arrowUp.setOnClickListener {
            if (BluetoothHelper.instance.isConnected()) {
                BluetoothHelper.instance.sendData("0")
            } else {
                Toast.makeText(context, "Arrow Up", Toast.LENGTH_SHORT).show()
            }
        }
        arrowDown.setOnClickListener {
            if (BluetoothHelper.instance.isConnected()) {
                BluetoothHelper.instance.sendData("1")
            } else {
                Toast.makeText(context, "Arrow Down", Toast.LENGTH_SHORT).show()
            }
        }
        arrowRight.setOnClickListener {
            if (BluetoothHelper.instance.isConnected()) {
                BluetoothHelper.instance.sendData("2")
            } else {
                Toast.makeText(context, "Arrow Right", Toast.LENGTH_SHORT).show()
            }
        }
        arrowLeft.setOnClickListener {
            if (BluetoothHelper.instance.isConnected()) {
                BluetoothHelper.instance.sendData("3")
            } else {
                Toast.makeText(context, "Arrow Left", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initDragAndDrop() {
        arrowController.setOnLongClickListener {
            it.startDrag(null, View.DragShadowBuilder(it), null, 0)
        }

        pool.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    arrowController.visibility = View.GONE
                    true
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    true
                }
                DragEvent.ACTION_DRAG_LOCATION ->
                    // Ignore the event
                    true
                DragEvent.ACTION_DRAG_EXITED -> {
                    true
                }
                DragEvent.ACTION_DROP -> {
                    setCoordinates(arrowController, event.x, event.y)
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    arrowController.visibility = View.VISIBLE
                    true
                }
                else -> {
                    // An unknown action type was received.
                    Log.e("DragDrop Example", "Unknown action type received by OnDragListener.")
                    false
                }
            }
        }
    }

    private fun setCoordinates(view: View, x: Float, y: Float) {
        var viewX = x - view.width / 2
        var viewY = y - view.height / 2

        if (viewX < 0) {
            viewX = 0f;
        } else if (viewX > pool.width - view.width) {
            viewX = (pool.width - view.width).toFloat()
        }
        if (viewY < 0) {
            viewY = 0f;
        } else if (viewY > pool.height - view.height) {
            viewY = (pool.height - view.height).toFloat()
        }
        view.x = viewX
        view.y = viewY
    }
}