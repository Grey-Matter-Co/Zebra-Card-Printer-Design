package mx.com.infotecno.zebracardprinter.util

import android.util.Xml
import com.google.gson.GsonBuilder
import mx.com.infotecno.zebracardprinter.model.XMLCardTemplate
import org.xmlpull.v1.XmlSerializer
import java.io.StringWriter

object XMLEncoder {
	private val ns: String? = null

	fun parse(template: XMLCardTemplate.Template): String {
		val serializer = Xml.newSerializer()
		val writer = StringWriter()

		serializer.setOutput(writer)
		serializer.startDocument("UTF-8", true)

		writeTemplate(serializer, template)

		serializer.endDocument()

		return writer.toString()
	}

	private fun writeTemplate(serializer: XmlSerializer, template: XMLCardTemplate.Template) {
		serializer.startTag(ns, "template")

		serializer.attribute(ns, "name", template.name)
		serializer.attribute(ns, "card_type", template.card_type.toString())
		serializer.attribute(ns, "card_thickness", template.card_thickness.toString())
		serializer.attribute(ns, "delete", template.delete.toString())
		serializer.attribute(ns, "source", template.source.toString())
		serializer.attribute(ns, "destination", template.destination.toString())

		writeFonts(serializer, template.Fonts)
		writeSides(serializer, template.Sides)
		if (template.Magdata != null)
			writeMagdata(serializer, template.Magdata)

		serializer.endTag(ns, "template")
	}

	private fun writeFonts(serializer: XmlSerializer, fonts: List<XMLCardTemplate.Font>) {
		serializer.startTag(ns, "fonts")
		fonts.forEach { font ->
			serializer.startTag(ns, "font")
			serializer.attribute(ns, "id",  font.id.toString())
			serializer.attribute(ns, "name", font.name)
			serializer.attribute(ns, "size",  font.size.toString())
			serializer.attribute(ns, "bold",  font.bold.toString())
			serializer.attribute(ns, "italic",  font.italic.toString())
			serializer.attribute(ns, "underline",  font.underline.toString())
			serializer.endTag(ns, "font")
		}
		serializer.endTag(ns, "fonts")
	}

	private fun writeSides(serializer: XmlSerializer, sides: List<XMLCardTemplate.Side>) {
		serializer.startTag(ns, "sides")
		sides.forEach { side ->
			serializer.startTag(ns, "side")

			serializer.attribute(ns, "name", side.name.toString())
			serializer.attribute(ns, "orientation", side.orientation.toString())
			serializer.attribute(ns, "rotation", side.rotation.toString())
			serializer.attribute(ns, "sharpness", side.sharpness.toString())
			serializer.attribute(ns, "k_mode", side.k_mode.toString())

			writePrintTypes(serializer, side.printTypes)

			serializer.endTag(ns, "side")
		}
		serializer.endTag(ns, "sides")
	}

	private fun writePrintTypes(serializer: XmlSerializer, printTypes: List<XMLCardTemplate.PrintType>) {
		serializer.startTag(ns, "print_types")

		printTypes.forEach { printType ->
			serializer.startTag(ns, "print_type")

			serializer.attribute(ns, "type", printType.type.toString())
			serializer.attribute(ns, "fill", printType.fill)
			serializer.attribute(ns, "preheat", printType.preheat.toString())

			writeElements(serializer, printType.elements)

			serializer.endTag(ns, "print_type")
		}

		serializer.endTag(ns, "print_types")
	}

	private fun writeElements(serializer: XmlSerializer, elements: List<XMLCardTemplate.Element>) {
		elements.forEach { element ->
			when (element) {
				is XMLCardTemplate.Element.Graphic -> {
					serializer.startTag(ns, "graphic")

					if (element.order_id != null)
						serializer.attribute(ns, "order_id", element.order_id.toString())
					serializer.attribute(ns, "format", element.format.toString())
					serializer.attribute(ns, "opacity", element.opacity.toString())
					serializer.attribute(ns, "height", element.height.toString())
					serializer.attribute(ns, "width", element.width.toString())
					serializer.attribute(ns, "x", element.x.toString())
					serializer.attribute(ns, "y", element.y.toString())
					serializer.attribute(ns, "rotation", element.rotation.toString())
					serializer.attribute(ns, "delete", element.delete.toString())

					if (element.field != null)
						serializer.attribute(ns, "field", element.field)
					else
						serializer.text(element.reference)

					serializer.endTag(ns, "graphic")
				}
				is XMLCardTemplate.Element.Text -> {
					serializer.startTag(ns, "text")

					if (element.order_id != null)
						serializer.attribute(ns, "order_id", element.order_id.toString())
					serializer.attribute(ns, "font_id", element.font_id.toString())
					serializer.attribute(ns, "x", element.x.toString())
					serializer.attribute(ns, "y", element.y.toString())
					serializer.attribute(ns, "color", element.color)
					serializer.attribute(ns, "angle", element.angle.toString())
					serializer.attribute(ns, "height", element.height.toString())
					serializer.attribute(ns, "width", element.width.toString())
					serializer.attribute(ns, "alignment", element.alignment.toString())
					serializer.attribute(ns, "v_alignment", element.v_alignment.toString())
					serializer.attribute(ns, "shrink", element.shrink.toString())

					if (element.field != null)
						serializer.attribute(ns, "field", element.field)
					else
						serializer.text(element.data)

					serializer.endTag(ns, "text")
				}
				is XMLCardTemplate.Element.Barcode -> {
					serializer.startTag(ns, "barcode")

					if (element.order_id != null)
						serializer.attribute(ns, "order_id", element.order_id.toString())
					serializer.attribute(ns, "font_id", element.font_id.toString())
					serializer.attribute(ns, "x", element.x.toString())
					serializer.attribute(ns, "y", element.y.toString())
					serializer.attribute(ns, "rotation", element.rotation.toString())
					serializer.attribute(ns, "code", element.code.toString())
					serializer.attribute(ns, "height", element.height.toString())
					serializer.attribute(ns, "width", element.width.toString())
					serializer.attribute(ns, "quiet_zone_width", element.quiet_zone_width.toString())
					serializer.attribute(ns, "show_text", element.show_text.toString())
					serializer.attribute(ns, "correction_level", element.correction_level.toString())
					serializer.attribute(ns, "minColumns", element.minColumns.toString())
					serializer.attribute(ns, "columns", element.columns.toString())
					serializer.attribute(ns, "minRows", element.minRows.toString())
					serializer.attribute(ns, "rows", element.rows.toString())
					serializer.attribute(ns, "compact", element.compact.toString())
					serializer.attribute(ns, "compactionMode", element.compactionMode.toString())
					serializer.attribute(ns, "error_correction_level", element.error_correction_level.toString())
					serializer.attribute(ns, "encoding_name", element.encoding_name)

					if (element.field != null)
						serializer.attribute(ns, "field", element.field)
					else
						serializer.text(element.data)

					serializer.endTag(ns, "barcode")
				}
				is XMLCardTemplate.Element.Line -> {
					serializer.startTag(ns, "line")

					if (element.order_id != null)
						serializer.attribute(ns, "order_id", element.order_id.toString())
					serializer.attribute(ns, "x1", element.x1.toString())
					serializer.attribute(ns, "y1", element.y1.toString())
					serializer.attribute(ns, "x2", element.x2.toString())
					serializer.attribute(ns, "y2", element.y2.toString())
					serializer.attribute(ns, "thickness", element.thickness.toString())
					serializer.attribute(ns, "color", element.color)

					serializer.endTag(ns, "line")
				}
				is XMLCardTemplate.Element.Ellipse -> {
					serializer.startTag(ns, "ellipse")

					if (element.order_id != null)
						serializer.attribute(ns, "order_id", element.order_id.toString())
					serializer.attribute(ns, "x", element.x.toString())
					serializer.attribute(ns, "y", element.y.toString())
					serializer.attribute(ns, "height", element.height.toString())
					serializer.attribute(ns, "width", element.width.toString())
					serializer.attribute(ns, "thickness", element.thickness.toString())
					serializer.attribute(ns, "color", element.color)
					serializer.attribute(ns, "fill_color", element.fill_color)

					serializer.endTag(ns, "ellipse")
				}
				is XMLCardTemplate.Element.Rectangle -> {
					serializer.startTag(ns, "rectangle")

					if (element.order_id != null)
						serializer.attribute(ns, "order_id", element.order_id.toString())
					serializer.attribute(ns, "x", element.x.toString())
					serializer.attribute(ns, "y", element.y.toString())
					serializer.attribute(ns, "height", element.height.toString())
					serializer.attribute(ns, "width", element.width.toString())
					serializer.attribute(ns, "thickness", element.thickness.toString())
					serializer.attribute(ns, "color", element.color)
					serializer.attribute(ns, "fill_color", element.fill_color)
					serializer.attribute(ns, "radius", element.radius.toString())

					serializer.endTag(ns, "rectangle")
				}
			}
		}
	}

	private fun writeMagdata(serializer: XmlSerializer, magdata: XMLCardTemplate.Magdata) {
		serializer.startTag(ns, "magdata")

		serializer.attribute(ns, "format", magdata.format.toString())
		serializer.attribute(ns, "coercivity", magdata.coercivity.toString())
		serializer.attribute(ns, "verify", magdata.verify.toString())

		writeTrack(serializer, magdata.track)

		serializer.endTag(ns, "magdata")
	}

	private fun writeTrack(serializer: XmlSerializer, track: XMLCardTemplate.Track) {
		serializer.startTag(ns, "track")

		serializer.attribute(ns, "field", track.field)
		serializer.attribute(ns, "number", track.number.toString())
		serializer.attribute(ns, "format", track.format.toString())

		serializer.endTag(ns, "track")
	}

	fun parseFields(fields: Map<String, String>): String =
		GsonBuilder().setPrettyPrinting().create().toJson(fields)
}