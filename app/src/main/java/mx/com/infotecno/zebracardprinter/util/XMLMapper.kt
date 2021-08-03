package mx.com.infotecno.zebracardprinter.util

import android.util.Log
import mx.com.infotecno.zebracardprinter.model.XMLCardDesign
import mx.com.infotecno.zebracardprinter.model.XMLCardDesign.*
import mx.com.infotecno.zebracardprinter.model.XMLCardTemplate
import mx.com.infotecno.zebracardprinter.model.XMLCardTemplate.*
import mx.com.infotecno.zebracardprinter.model.XMLCardTemplate.Element.*
import java.util.*
import kotlin.math.roundToInt

object XMLMapper {
	private const val PPM = 3.0 // PPM  Pixel per Mil (inch/100)
	private val fieldRegex = Regex("""\{([\w\s]+)\}""")
	private val mutableFields = mutableMapOf<String, String>()

	private val fontList = mutableListOf<Font>()

	fun map(xmlCardDesign: CardDesignProject): Pair<Template, Map<String,String>> =
		Pair(mapTemplate(xmlCardDesign), mutableFields)

	private fun mapTemplate(cardDesignProject: CardDesignProject): Template {

		with (cardDesignProject) {
			val sides = mapSides(this)
			return Template(name, 2, 30, BOOLEAN.yes, SOURCE.autodetect, DESTINATION.eject,
				mapFonts(),
				sides,
				if(hasMagstripe) mapMagData() else null)
		}
	}
	private fun mapFonts(): List<Font> {
		return fontList
	}

	private fun mapSides(cardDesignProject: CardDesignProject): List<Side> {
		val sides = mutableListOf<Side>()
		with (cardDesignProject) {

			if (frontDocument!!.elements.isNotEmpty())
				sides.add(
					Side(SIDETYPE.front, ORIENTATION.valueOf(frontRibbon!!.orientation.toLowerCase(Locale.ENGLISH)),
						printTypes = mapPrintTypes(frontDocument , frontRibbon.ribbonType)))
			if (backDocument!!.elements.isNotEmpty())
				sides.add(
					Side(SIDETYPE.back, ORIENTATION.valueOf(backRibbon!!.orientation.toLowerCase(Locale.ENGLISH)),
						printTypes = mapPrintTypes(backDocument, backRibbon.ribbonType)))
		}
		return sides
	}

	private fun mapPrintTypes(document: Document, ribbonType: String): List<PrintType> {
		val printTypes = mutableListOf<PrintType>()
		val backgrounds = mutableListOf<String>()
		val elementRGB = mapElements(document).also {
			it.forEach { (_, rgba) ->
				if (!backgrounds.contains(rgba))
					backgrounds.add(rgba)
			}
		}
		backgrounds.forEach {bkgd ->
			printTypes.add(PrintType(PRINTYPE.valueOf(ribbonType.toLowerCase(Locale.ROOT)), elements = elementRGB
				.filter { (_, rgb) -> rgb == bkgd }
				.map { (e, _) -> e }))
		}

		return printTypes
	}

	private fun mapElements(document: Document): List<Pair<XMLCardTemplate.Element, String>> =
		document.elements.map { elem -> when (elem) {
			is XMLCardDesign.Element.XamlImageElement -> {
				Pair(Graphic(null, null, GRAPHICFORMAT.valueOf(elem.source.substringAfterLast(".", "bmp")), elem.transparency, (elem.height*PPM).roundToInt(), (elem.width*PPM).roundToInt(), (elem.left*PPM).roundToInt(), (elem.top*PPM).roundToInt(), elem.angle, reference = elem.source),
					rgba2rgb(elem.backgroundColor))
			}
			is XMLCardDesign.Element.XamlTextElement -> {
				val currentFont = Font(fontList.size, elem.fontFamily, elem.fontSize, BOOLEAN.valueOf(elem.fontWeight.contains("Bold")), BOOLEAN.valueOf(elem.fontWeight.contains("Italic")), BOOLEAN.valueOf(elem.fontWeight.contains("Underline")))
				if (null == fontList.find {font -> (font.name == currentFont.name) and (font.size == currentFont.size) and (font.bold == currentFont.bold) and (font.italic == currentFont.italic) and  (font.underline == currentFont.underline) })
					fontList.add(currentFont)

				if (hasFields(elem.text)) {
					getFields(elem.text).forEach { field -> mutableFields[field] = "" }

					Pair(Text(null, elem.text, currentFont.id, (elem.width*PPM).roundToInt(), (elem.height*PPM).roundToInt(), (elem.left*PPM).roundToInt(), (elem.top*PPM).roundToInt(), elem.textColor, elem.angle, HALIGNMENT.valueOf(elem.alignmentH.toLowerCase(Locale.ENGLISH)), VALIGNMENT.valueOf(elem.alignmentV.toLowerCase(Locale.ENGLISH)), if (elem.wrapText) BOOLEAN.yes else BOOLEAN.no, null),
						rgba2rgb(elem.backgroundColor))
				}
				else
					Pair(Text(null, null, currentFont.id, (elem.width*PPM).roundToInt(), (elem.height*PPM).roundToInt(), (elem.left*PPM).roundToInt(), (elem.top*PPM).roundToInt(), elem.textColor, elem.angle, HALIGNMENT.valueOf(elem.alignmentH.toLowerCase(Locale.ENGLISH)), VALIGNMENT.valueOf(elem.alignmentV.toLowerCase(Locale.ENGLISH)), if (elem.wrapText) BOOLEAN.yes else BOOLEAN.no, elem.text),
						rgba2rgb(elem.backgroundColor))
			}
			is XMLCardDesign.Element.XamlPassportPhotoElement -> {
				val newKey = genKey("photo")
				mutableFields[newKey] = ""
				Pair(Graphic(null, "{$newKey}", GRAPHICFORMAT.jpeg, elem.transparency, (elem.height*PPM).roundToInt(), (elem.width*PPM).roundToInt(), (elem.left*PPM).roundToInt(), (elem.top*PPM).roundToInt(), elem.angle, reference = null),
					rgba2rgb(elem.backgroundColor))
			}
		}}

	private fun mapMagData(): Magdata {
		Log.e("EMBY", "mapMagData: not handle this card")
		TODO("Not yet implemented")
	}

	private fun rgba2rgb(str: String): String =
		if (str.length == 8)
			if (str.startsWith("00"))
				"none"
			else
				str.removeRange(0..1)
		else
			str

	private fun genKey(prefix: String): String {
		var i = 1
		mutableFields.forEach { (field, value) ->
			if (field.contains(prefix))
				i++
		}
		return "$prefix$i"
	}

	private fun hasFields(input: String): Boolean
		= fieldRegex.containsMatchIn(input)

	fun getField(input: String): String
		= fieldRegex.find(input)!!.groupValues[1]

	fun getFields(input: String): List<String>
		= fieldRegex.findAll(input).map { it.groupValues[1] }.toList()

	fun replace(input: String, field: String, replacement: String): String
		= Regex("""\{$field\}""").replace(input, replacement)


}

//private fun <A, B> Iterable<A>.product(other: Iterable<B>)
//		= flatMap{ a: A -> other.map{ b: B -> a to b }}


