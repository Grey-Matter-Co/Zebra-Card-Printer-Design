package mx.com.infotecno.zebracardprinter.util

import android.util.Log
import mx.com.infotecno.zebracardprinter.model.XMLCardDesign
import mx.com.infotecno.zebracardprinter.model.XMLCardDesign.*
import mx.com.infotecno.zebracardprinter.model.XMLCardTemplate
import mx.com.infotecno.zebracardprinter.model.XMLCardTemplate.*
import kotlin.math.roundToInt

object XMLMapper {
    val PPM = 3.0 // PPM  Pixel per Mil (inch/100)
    val RGXFIELD = Regex("""\{([\w\s]+)}""")
    val data = mutableListOf<Pair<String, String>>()

    fun map(xmlCardDesign: CardDesignProject): Template? {
        return mapTemplate(xmlCardDesign).also {
            Log.d("EMBY", "map: $it")
        }
    }

    fun mapTemplate(cardDesignProject: CardDesignProject): Template {
        with (cardDesignProject) {
            return Template(name, 2, 30, BOOLEAN.yes, SOURCE.autodetect, DESTINATION.eject,
                mapFonts(),
                mapSides(this),
                if(hasMagstripe) mapMagData() else null)
        }
    }
    private fun mapFonts(): List<Font> {
        return listOf()
    }

    private fun mapSides(cardDesignProject: CardDesignProject): List<Side> {
        val sides = mutableListOf<Side>()
        with (cardDesignProject) {

            if (frontDocument!!.elements.isNotEmpty())
                sides.add(
                    Side(SIDETYPE.front, ORIENTATION.valueOf(frontRibbon!!.orientation),
                        printTypes = mapPrintTypes(frontDocument , frontRibbon.ribbonType)))
            if (backDocument!!.elements.isNotEmpty())
                sides.add(
                    Side(SIDETYPE.back, ORIENTATION.valueOf(backRibbon!!.orientation),
                        printTypes = mapPrintTypes(backDocument, backRibbon.ribbonType)))
        }
        return sides
    }

    private fun mapPrintTypes(document: Document, ribbonType: String): List<PrintType> {
        val printTypes = mutableListOf<PrintType>()
        val ptElements = mutableListOf<MutableList<XMLCardTemplate.Element>>()
        val totalElements = mapElements(document)
        printTypes.add(PrintType(PRINTTYPE.valueOf(ribbonType), elements = mapElements(document)))

        return printTypes
    }

    private fun mapElements(document: Document): List<XMLCardTemplate.Element>
        = document.elements.map { when (it) {
            is XMLCardDesign.Element.XamlImageElement -> {
                Pair(Graphic(null, null, GRAPHICFORMAT.valueOf(it.source.substringAfterLast(".", "bmp")), it.transparency, (it.height*PPM).roundToInt(), (it.width*PPM).roundToInt(), it.left.roundToInt(), it.top.roundToInt(), it.angle, reference = it.source),
                    rgba2rgb(it.backgroundColor))
            }
            is XMLCardDesign.Element.XamlTextElement -> {
                if (hasVariable(it.text)) {
                    val newKey = genKey("text")
                    data.add(Pair(newKey, it.text))
//                    TODO("skip font family and font style, maybe they must be added at an extra list")
                    Pair(Text(null, newKey, 1, (it.width*PPM).roundToInt(), (it.height*PPM).roundToInt(), (it.left*PPM).roundToInt(), (it.top*PPM).roundToInt(), it.textColor, it.angle, HALINGMENT.valueOf(it.alignmentH), VALINGMENT.valueOf(it.alignmentV), if (it.wrapText) BOOLEAN.yes else BOOLEAN.no, null),
                        rgba2rgb(it.backgroundColor))
                }
                else
                    Pair(Text(null, null, 1, (it.width*PPM).roundToInt(), (it.height*PPM).roundToInt(), (it.left*PPM).roundToInt(), (it.top*PPM).roundToInt(), it.textColor, it.angle, HALINGMENT.valueOf(it.alignmentH), VALINGMENT.valueOf(it.alignmentV), if (it.wrapText) BOOLEAN.yes else BOOLEAN.no, it.text),
                        rgba2rgb(it.backgroundColor))
            }
            is XMLCardDesign.Element.XamlPassportPhotoElement -> {
                val newKey = genKey("photo")
                data.add(Pair(newKey, ""))
                Pair(Graphic(null, "", GRAPHICFORMAT.jpeg, it.transparency, (it.height*PPM).roundToInt(), (it.width*PPM).roundToInt(), it.left.roundToInt(), it.top.roundToInt(), it.angle, reference = null),
                    rgba2rgb(it.backgroundColor))
            }
        } } as List<XMLCardTemplate.Element>

    private fun rgba2rgb(str: String): String
        = if (str.length > 7 && str.substring(1,2) == "00")
            "none"
            else str.removeRange(str.length-6, str.length)


    private fun mapMagData(): Magdata {
        TODO("Not yet implemented")
    }

    private fun genKey(pre: String): String {
        var i = 1
        data.forEach { pair ->
            if (pair.first.contains(pre))
                i++
        }
        return "$pre$i"
    }

    private fun hasVariable(input: String): Boolean
        = RGXFIELD.containsMatchIn(input)

    private fun getVariables(input: String): List<String>
        = RGXFIELD.findAll(input).map { it.groupValues[1] }.toList()

    private fun replace(input: String, field: String, replacement: String): String
        = Regex("""\{$field}""").replace(input, replacement)
}
