package mx.com.infotecno.zebracardprinter.model

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import mx.com.infotecno.zebracardprinter.R

class ZCardTemplate (): Parcelable {
    var fields: Map<String, Any> = mapOf()
    var id: Long = 0
    var name: String = "[NO_NAME]"
    var templateUri: Uri = Uri.EMPTY
    var frontPreview: Bitmap? = null
    var idBackground: Int = R.drawable.card_mamalona

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        name = parcel.readString()!!
        templateUri = parcel.readParcelable(Uri::class.java.classLoader)!!
        frontPreview = parcel.readParcelable(Bitmap::class.java.classLoader)
        idBackground = parcel.readInt()
    }

    constructor(id: Long, templateName: String, uriXml: Uri) : this() {
        name = templateName
        templateUri = uriXml
        this.id = id
    }

    constructor(idBg: Int) : this() {
        idBackground = idBg
        name = "[ADD_TEMPLATE]"
    }

    override fun toString(): String =
        "ZCardTemplate (" +
                "id: $id, " +
                "name: $name, " +
                "uri: $templateUri, " +
                "idBackground: $idBackground, " +
                "frontPreview: $frontPreview " +
                ")"

    override fun describeContents(): Int {
        toString()
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        if (dest != null) {
            dest.writeMap(fields)
            dest.writeLong(id)
            dest.writeString(name)
            dest.writeParcelable(templateUri, flags)
            dest.writeParcelable(frontPreview, flags)
            dest.writeInt(idBackground)
        }

    }

//    companion object
//    {
//        fun createCardsList(numCards: Int, idBg: Int): ArrayList<ZCardTemplate>
//        {
//            val cards = ArrayList<ZCardTemplate>()
//            for (i in 1..numCards)
//                cards.add(ZCardTemplate(idBg))
//            return cards
//        }
//    }

    companion object CREATOR : Parcelable.Creator<ZCardTemplate> {
        override fun createFromParcel(parcel: Parcel): ZCardTemplate =
            ZCardTemplate(parcel)

        override fun newArray(size: Int): Array<ZCardTemplate?> =
            arrayOfNulls(size)
    }
}