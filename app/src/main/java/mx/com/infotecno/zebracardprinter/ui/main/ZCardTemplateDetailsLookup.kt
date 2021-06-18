package mx.com.infotecno.zebracardprinter.ui.main

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import mx.com.infotecno.zebracardprinter.adapter.ZCardTemplatesAdapter

class ZCardTemplateDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<String>() {

    override fun getItemDetails(event: MotionEvent): ItemDetails<String>? {
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        return if (view != null)
                    (recyclerView.getChildViewHolder(view) as ZCardTemplatesAdapter.ZCardViewHolder).getImageDetails()
                else  null
    }
}