package mx.com.infotecno.zebracardprinter.ui.main

import androidx.recyclerview.selection.ItemKeyProvider
import mx.com.infotecno.zebracardprinter.adapter.ZCardTemplatesAdapter

class ZCardTemplateKeyProvider(private val templatesAdapter: ZCardTemplatesAdapter) : ItemKeyProvider<String>(SCOPE_CACHED) {
    override fun getKey(position: Int): String =
        templatesAdapter.currentList[position].name

    override fun getPosition(key: String): Int =
        templatesAdapter.currentList.indexOfFirst { it.name == key}
}