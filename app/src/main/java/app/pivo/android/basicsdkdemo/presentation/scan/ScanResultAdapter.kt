package app.pivo.android.basicsdkdemo.presentation.scan

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.pivo.android.basicsdkdemo.R
import app.pivo.android.basicsdkdemo.presentation.scan.model.PivoDeviceItem
import java.util.*

/**
 * Created by murodjon on 2020/03/12
 */

class ScanResultsAdapter : RecyclerView.Adapter<ScanResultsAdapter.ViewHolder?>() {
    private val data = mutableListOf<PivoDeviceItem>()
    private var onAdapterItemClickListener: OnAdapterItemClickListener? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var deviceNAme: TextView = itemView.findViewById(R.id.device_item_view)
    }

    interface OnAdapterItemClickListener {
        fun onAdapterViewClick(view: View?)
    }

    private val onClickListener = View.OnClickListener { v ->
        onAdapterItemClickListener?.onAdapterViewClick(v)
    }

    fun addScanResult(scanDevice: PivoDeviceItem) {
        Log.e(TAG, "Found: ${scanDevice.name}")
        val now = System.currentTimeMillis()
        scanDevice.lastScanTime = now

        val index = data.indexOfFirst { it.macAddress == scanDevice.macAddress }

        if (index >= 0) {
            data[index] = scanDevice
            notifyItemChanged(index)
            return
        }

        data.add(scanDevice)
        data.sortWith(SORTING_COMPARATOR)
        notifyItemInserted(data.indexOf(scanDevice))
    }

    fun removeStaleDevices(timeoutMillis: Long = 3000L) {
        val now = System.currentTimeMillis()
        val iterator = data.listIterator()
        while (iterator.hasNext()) {
            val index = iterator.nextIndex()
            val device = iterator.next()
            if (now - device.lastScanTime > timeoutMillis) {
                iterator.remove()
                notifyItemRemoved(index)
            }
        }
    }

    fun clearScanResults() {
        data.clear()
        notifyDataSetChanged()
    }

    fun getItemAtPosition(childAdapterPosition: Int): PivoDeviceItem? {
        return data.getOrNull(childAdapterPosition)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val result = data[position]
        holder.deviceNAme.text = result.name ?: "Unknown"
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.device_item, parent, false)
        itemView.setOnClickListener(onClickListener)
        return ViewHolder(
            itemView
        )
    }

    fun setOnAdapterItemClickListener(onAdapterItemClickListener: OnAdapterItemClickListener) {
        this.onAdapterItemClickListener = onAdapterItemClickListener
    }

    companion object {
        private const val TAG = "ScanResultsAdapter"
        private val SORTING_COMPARATOR =
            Comparator { lhs: PivoDeviceItem, rhs: PivoDeviceItem ->
                lhs.macAddress.compareTo(rhs.macAddress)
            }
    }
}
