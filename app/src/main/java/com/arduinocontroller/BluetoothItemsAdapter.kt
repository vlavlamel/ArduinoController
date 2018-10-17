package com.arduinocontroller

import android.bluetooth.BluetoothDevice
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class BluetoothItemsAdapter(val bluetoothDevices: List<BluetoothDevice>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        return BluetoothViewHolder(LayoutInflater.from(p0.context)
                .inflate(R.layout.item_bluetooth_device, p0, false))
    }

    override fun getItemCount(): Int {
        return bluetoothDevices.size
    }

    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
        val parent = p0.itemView
        parent.findViewById<AppCompatTextView>(R.id.title).text = bluetoothDevices[p1].name
        parent.findViewById<AppCompatTextView>(R.id.subtitle).text = bluetoothDevices[p1].address
    }

    fun addItem(device: BluetoothDevice) {
        if (!bluetoothDevices.contains(device)) {
            (bluetoothDevices as ArrayList).add(device)
        }
    }

}

class BluetoothViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

class DividerDecoration : RecyclerView.ItemDecoration() {
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        c.save()
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val rectColor = Paint()
            rectColor.color = ContextCompat.getColor(parent.context, android.R.color.darker_gray)
            c.drawRect(child.left.toFloat(),
                    child.bottom.toFloat(),
                    child.right.toFloat(),
                    child.bottom.toFloat() + convertDpToPixel(1),
                    rectColor)
        }
        c.restore()

    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.set(0, 0, 0, convertDpToPixel(1))
    }
}

fun convertDpToPixel(dp: Int): Int {
    val metrics = Resources.getSystem().displayMetrics
    val px = dp * (metrics.densityDpi / 160f)
    return Math.round(px)
}
