package mx.com.infotecno.zebracardprinter.model

import android.net.Uri
import mx.com.infotecno.zebracardprinter.R

class ZCardTemplate (val idBackground: Int) {
    var name: String = ""
    var uri: Uri = Uri.EMPTY
    val id: Long = 0

    constructor(templateName: String) : this(R.drawable.card_mamalona) {
        name = templateName
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