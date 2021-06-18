package mx.com.infotecno.zebracardprinter.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.zebra.sdk.common.card.printer.discovery.DiscoveredCardPrinterNetwork
import com.zebra.sdk.printer.discovery.DiscoveredPrinter
import com.zebra.sdk.printer.discovery.DiscoveredPrinterBluetooth
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb
import mx.com.infotecno.zebracardprinter.R
import kotlin.math.roundToInt

class DiscoveredPrinterAdapter(val clickListener: (DiscoveredPrinter, Int) -> Unit): ListAdapter<DiscoveredPrinter, DiscoveredPrinterAdapter.DiscoveredPrinterViewHolder>(DiffCallback()) {
    lateinit var context: Context

    init { setHasStableIds(false) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscoveredPrinterViewHolder
        = DiscoveredPrinterViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_zprinter, parent, false))

    override fun onBindViewHolder(holder: DiscoveredPrinterViewHolder, position: Int)
    {
        val printer = getItem(position)

        val model = printer.discoveryDataMap["MODEL"]
        val address = printer.discoveryDataMap["ADDRESS"]
//        val cardVw = holder.card
        holder.icon.setImageResource( when (printer) {
            is DiscoveredPrinterUsb ->
                R.drawable.ic_usb
            is DiscoveredPrinterBluetooth ->
                R.drawable.ic_bluetooth
            is DiscoveredCardPrinterNetwork ->
                R.drawable.ic_wifi
            else -> R.drawable.ic_no_printer
        })
        holder.model.apply {
            this.visibility   = if (model != null && model.isNotEmpty()) View.VISIBLE else View.GONE
            this.text = model
        }
        holder.reference.apply {
            this.visibility = if (address != null && address.isNotEmpty()) View.VISIBLE else View.GONE
            this.text = address
        }

        // To fix view margin with layout
        if (holder.card.layoutParams is ViewGroup.MarginLayoutParams) {
            val marginLateral   = holder.card.marginLeft
            var marginTop       = holder.card.marginTop
            var marginBottom    = holder.card.marginBottom
            val toAdd: Int      = context.resources.getDimension(R.dimen.spacing_middle).roundToInt()

            // Add margin to first 2 elements and for last 2|1 elements
            if (position<2)
                marginTop += toAdd
            else if (currentList.size < position + if (currentList.size%2==0) 1 else 2)
                marginBottom += toAdd

            if (position%2==0) {
                (holder.card.layoutParams as ViewGroup.MarginLayoutParams).setMargins(marginLateral*2, marginTop, holder.card.marginRight, marginBottom)
                holder.card.requestLayout()
            }
            else
                (holder.card.layoutParams as ViewGroup.MarginLayoutParams).setMargins(holder.card.marginLeft, marginTop, holder.card.marginRight, marginBottom)
//            when {
//                position%2==0 -> (holder.card.layoutParams as ViewGroup.MarginLayoutParams).setMargins(marginLateral*2, marginTop, holder.card.marginRight, marginBottom)
//                else -> (holder.card.layoutParams as ViewGroup.MarginLayoutParams).setMargins(holder.card.marginLeft, marginTop, holder.card.marginRight, marginBottom)
//            }
//            if (position%2==0)
//                holder.card.requestLayout()
        }
        holder.card.setOnClickListener { clickListener(printer, position) }
    }

    override fun getItemId(position: Int): Long
            = currentList[position].address.substringAfterLast(".", "0").toLong()

    private class DiffCallback : DiffUtil.ItemCallback<DiscoveredPrinter>() {
        override fun areItemsTheSame(oldItem: DiscoveredPrinter, newItem: DiscoveredPrinter): Boolean
            = oldItem.address == newItem.address

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: DiscoveredPrinter, newItem: DiscoveredPrinter): Boolean
            = oldItem == newItem
    }

    fun getIndexUsingAddress(address: String): Int
    {
        currentList.forEachIndexed { index, discoveredPrinter ->
            if (address == discoveredPrinter.discoveryDataMap["ADDRESS"])
                return index
        }
        return -1
    }

    // get params from card layout
//    inner class DiscoveredPrinterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
//    {
//        lateinit var onClickDiscoveredPrinterListener: OnClickDiscoveredPrinterListener
//        val card: CardView = itemView.findViewById(R.id.printer_card)
//        val icon: ImageView = itemView.findViewById(R.id.printer_icon)
//        val model: TextView = itemView.findViewById(R.id.printer_model)
//        val reference: TextView = itemView.findViewById(R.id.printer_reference)
//
//    }

    inner class DiscoveredPrinterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val card: CardView = itemView.findViewById(R.id.printer_card)
        val icon: ImageView = itemView.findViewById(R.id.printer_icon)
        val model: TextView = itemView.findViewById(R.id.printer_model)
        val reference: TextView = itemView.findViewById(R.id.printer_reference)
        fun getImageDetails(): ItemDetailsLookup.ItemDetails<String> =
            object : ItemDetailsLookup.ItemDetails<String>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): String = getItem(adapterPosition).address
            }
    }
}