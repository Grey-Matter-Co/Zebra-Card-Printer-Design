package mx.com.infotecno.zebracardprinter.model

import android.graphics.Bitmap
import android.net.Uri
import mx.com.infotecno.zebracardprinter.R

class ZCardTemplate (val idBackground: Int) {
    val id: Long = 0
    var name: String = ""
    var uri: Uri = Uri.EMPTY
    var frontPreview: Bitmap? = null

    constructor(templateName: String) : this(R.drawable.card_mamalona) {
        name = templateName
    }

    override fun toString(): String {
        return "ZCardTemplate (" +
                "id: $id, " +
                "name: $name, " +
                "uri: $uri, " +
                "idBackground: $idBackground, " +
                "frontPreview: $frontPreview " +
                ")"
    }

    companion object
    {
        fun createCardsList(numCards: Int, idBg: Int): ArrayList<ZCardTemplate>
        {
            val cards = ArrayList<ZCardTemplate>()
            for (i in 1..numCards)
                cards.add(ZCardTemplate(idBg))
            return cards
        }
    }
}