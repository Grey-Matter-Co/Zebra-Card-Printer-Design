package mx.com.infotecno.zebracardprinter.adapter

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import mx.com.infotecno.zebracardprinter.R
import mx.com.infotecno.zebracardprinter.adapter.ZCardTemplatesAdapter.ZCardViewHolder
import mx.com.infotecno.zebracardprinter.model.ZCardTemplate

//class ZCardAdapter(private var zCards: List<ZCardTemplate>): RecyclerView.Adapter<ZCardViewHolder>()
class ZCardTemplatesAdapter(val clickListener: (ZCardTemplate, Int) -> Unit, val longClickListener: (Int) -> Boolean): ListAdapter<ZCardTemplate, ZCardViewHolder>(DiffCallback())
{
    var tracker: SelectionTracker<String>? = null

    init { setHasStableIds(false) }

    // Find card layout to get its params
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZCardViewHolder
        = ZCardViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_zcard, parent, false))

    // Configures card view
    override fun onBindViewHolder(holder: ZCardViewHolder, position: Int)
    {
        val card = getItem(position)

        tracker?.let {
            if (it.isSelected(""))
                it.setItemsSelected(listOf(""), false)

            if (position>0)
                holder.card.setImageResource(card.idBackground)
            else
                holder.card.setBackgroundResource(card.idBackground)

            if(it.isSelected(card.name))
                holder.card.setColorFilter(ContextCompat.getColor(holder.card.context, R.color.color65TransparentPrimary), PorterDuff.Mode.SRC_OVER)
            else
                holder.card.clearColorFilter()

            holder.card.setOnClickListener { clickListener(card, position) }
//            holder.card.setOnLongClickListener{ longClickListener(position) }
        }
    }

    override fun getItemId(position: Int): Long
        = currentList[position].id

    private class DiffCallback : DiffUtil.ItemCallback<ZCardTemplate>() {
        override fun areItemsTheSame(oldItem: ZCardTemplate, newItem: ZCardTemplate): Boolean
            = oldItem.name == newItem.name

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: ZCardTemplate, newItem: ZCardTemplate): Boolean
            = oldItem == newItem
    }

    // get params from card layout
    inner class ZCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val card: ImageView = itemView.findViewById(R.id.card_bg)
        fun getImageDetails(): ItemDetailsLookup.ItemDetails<String> =
            object : ItemDetailsLookup.ItemDetails<String>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): String = getItem(adapterPosition).name
            }
    }
}