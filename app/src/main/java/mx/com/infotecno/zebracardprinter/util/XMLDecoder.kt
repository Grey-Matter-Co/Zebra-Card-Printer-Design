package mx.com.infotecno.zebracardprinter.util

import android.util.Log
import android.util.Xml
import mx.com.infotecno.zebracardprinter.model.XMLCardDesign
import mx.com.infotecno.zebracardprinter.model.XMLCardDesign.*
import mx.com.infotecno.zebracardprinter.model.XMLCardTemplate
import mx.com.infotecno.zebracardprinter.model.XMLCardTemplate.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import kotlin.jvm.Throws

object XMLDecoder {

    private val ns: String? = null


    @Throws(XmlPullParserException::class, IOException::class)
    fun parseCardDesignProject(inputStream: InputStream): CardDesignProject {
        val parser: XmlPullParser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)
        parser.nextTag()
        return readCardDesignProject(parser)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun parseTemplate(inputStream: InputStream): Template {
        val parser: XmlPullParser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)
        parser.nextTag()
        return readTemplate(parser)
    }

    /**
     * XMLCardDesign
     */
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readCardDesignProject(parser: XmlPullParser): CardDesignProject {
        parser.require(XmlPullParser.START_TAG,
            ns, "CardDesignProject")
        val version: Int = parser.getAttributeValue(null, "Version").toInt()
        val name: String = parser.getAttributeValue(null, "Name")
        val description: String? = parser.getAttributeValue(null, "Description")
        val cardWidth: Double = parser.getAttributeValue(null, "CardWidth").toDouble()
        val cardHeight: Double = parser.getAttributeValue(null, "CardHeight").toDouble()
        val hasIDChip:Boolean = parser.getAttributeValue(null, "hasIDChip").toBoolean()
        val hasMagstripe:Boolean = parser.getAttributeValue(null, "hasMagstripe").toBoolean()
        val hasLaminate: Boolean = parser.getAttributeValue(null, "hasLaminate").toBoolean()
        val hasOverlay: Boolean = parser.getAttributeValue(null, "hasOverlay").toBoolean()
        val hasUV: Boolean = parser.getAttributeValue(null, "hasUV").toBoolean()
        val isSingleSided: Boolean = parser.getAttributeValue(null, "IsSingleSided").toBoolean()
        val writeDirection: String = parser.getAttributeValue(null, "WriteDirection")
        val xmlns: String = parser.getAttributeValue(null, "xmlns")

        var dynamicFields: List<DynamicField> = emptyList()
        var frontRibbon: Ribbon? = null
        var backRibbon: Ribbon? = null
        var frontDocument: Document? = null
        var backDocument: Document? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue
            when (parser.name) {
                "DynamicFields" -> dynamicFields = readDynamicFields(parser)
                "FrontRibbon" -> frontRibbon = readRibbon(parser, "FrontRibbon")
                "BackRibbon" -> backRibbon = readRibbon(parser, "BackRibbon")
                "FrontDocument" -> frontDocument = readDocument(parser, "FrontDocument")
                "BackDocument" -> backDocument = readDocument(parser, "BackDocument")
                else -> skip(parser)
            }
        }
//            Log.d("EMBY", "dynamicFields> $dynamicFields\n\n\n")
//            Log.d("EMBY", "frontRibbon> ${frontRibbon.toString()}\n\n\n")
//            Log.d("EMBY", "backRibbon> ${backRibbon.toString()}\n\n\n")
//            Log.d("EMBY", "frontDocument> ${frontDocument.toString()}\n\n\n")
//            Log.d("EMBY", "backDocument> ${backDocument.toString()}\n\n\n")

        return CardDesignProject(dynamicFields, frontRibbon, backRibbon, frontDocument, backDocument, version, name, description, cardWidth, cardHeight, hasIDChip, hasMagstripe, hasLaminate, hasOverlay, hasUV, isSingleSided, writeDirection, xmlns)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readDynamicFields(parser: XmlPullParser): List<DynamicField> {
        val dynamicFields = mutableListOf<DynamicField>()

        parser.require(XmlPullParser.START_TAG,
            ns, "DynamicFields")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue

            if (parser.name == "DynamicField")
                dynamicFields.add(
                    readDynamicField(
                        parser
                    )
                )
            else skip(parser)
        }
        return dynamicFields
    }

    private fun readDynamicField(parser: XmlPullParser): DynamicField {
        parser.require(XmlPullParser.START_TAG,
            ns, "DynamicField")

        val name: String = parser.getAttributeValue(null, "Name")
        val typeName: String = parser.getAttributeValue(null, "TypeName")
        val textFormat: String = parser.getAttributeValue(null, "TextFormat")
        val dataFieldName: String? = parser.getAttributeValue(null, "DataFieldName")
        val defaultValue: String? = parser.getAttributeValue(null, "DefaultValue")
        val isFixedLength: Boolean = parser.getAttributeValue(null, "IsFixedLength").toBoolean()
        val fixedLength: Int = parser.getAttributeValue(null, "FixedLength").toInt()
        val alignment: String = parser.getAttributeValue(null, "Alignment")
        val paddingValue: String = parser.getAttributeValue(null, "PaddingValue")

        if (parser.name == "DynamicField")
            parser.nextTag()

        return DynamicField(name, typeName, textFormat, dataFieldName, defaultValue, isFixedLength, fixedLength, alignment, paddingValue)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readRibbon(parser: XmlPullParser, nameTag: String): Ribbon {
        parser.require(XmlPullParser.START_TAG,
            ns, nameTag)

        val name: String? = parser.getAttributeValue(null, "Name")
        val ribbonType: String = parser.getAttributeValue(null, "RibbonType")
        val orientation: String = parser.getAttributeValue(null, "Orientation")
        var layer = Layer("", "")

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue

            if (parser.name == "Layers")
                layer =
                    readRibbonLayer(
                        parser
                    )
            else skip(parser)
        }

        return Ribbon(name, ribbonType, orientation, layer.name, layer.type)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readRibbonLayer(parser: XmlPullParser): Layer {
        parser.require(XmlPullParser.START_TAG,
            ns, "Layers")

        var layer = Layer("", "")

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue
            if (parser.name == "Layer")
                layer =
                    readLayer(
                        parser
                    )
            else skip(parser)
        }
        return layer
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readLayer(parser: XmlPullParser): Layer {
        parser.require(XmlPullParser.START_TAG,
            ns, "Layer")

        val name: String = parser.getAttributeValue(null, "Name")
        val layerType: String = parser.getAttributeValue(null, "LayerType")

        while (parser.next() != XmlPullParser.END_TAG);

        return Layer(name, layerType)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readDocument(parser: XmlPullParser, nameTag: String): Document {
        parser.require(XmlPullParser.START_TAG,
            ns, nameTag)

        val name: String? = parser.getAttributeValue(null, "Name")
        var xamlDesignLayer = XamlDesignLayer("", listOf())

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue

            if (parser.name == "Layers")
                xamlDesignLayer =
                    readDocumentLayer(
                        parser
                    )
            else skip(parser)
        }
        return Document(name, xamlDesignLayer.name, xamlDesignLayer.elements)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readDocumentLayer(parser: XmlPullParser): XamlDesignLayer {
        parser.require(XmlPullParser.START_TAG,
            ns, "Layers")

        val xamlDesignLayer = XamlDesignLayer("", listOf())

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue

            if (parser.name == "XamlDesignLayer") {
                parser.require(XmlPullParser.START_TAG,
                    ns, "XamlDesignLayer")

                xamlDesignLayer.name = parser.getAttributeValue(null, "Name")

                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.eventType != XmlPullParser.START_TAG)
                        continue

                    if (parser.name == "Elements")
                        xamlDesignLayer.elements =
                            readElements(
                                parser
                            )
                    else skip(
                        parser
                    )
                }
            }
            else skip(parser)
        }
        return xamlDesignLayer
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readElements(parser: XmlPullParser): List<XMLCardDesign.Element> {
        val elements = mutableListOf<XMLCardDesign.Element>()
        parser.require(XmlPullParser.START_TAG,
            ns, "Elements")

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue
            when (parser.name) {
                "XamlImageElement" -> elements.add(readXamlImageElement(parser))
                "XamlTextElement" -> elements.add(readXamlTextElement(parser))
                "XamlPassportPhotoElement" -> elements.add(readXamlPassportPhotoElement(parser))
                else -> { Log.e("EMBY ERROR", "unhandled Tag"); skip(parser) }
            }
        }
        return  elements
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readXamlImageElement(parser: XmlPullParser): XMLCardDesign.Element.XamlImageElement {
        parser.require(XmlPullParser.START_TAG,
            ns, "XamlImageElement")

        val source: String = parser.getAttributeValue(null, "Source")
        val transparency: Double = parser.getAttributeValue(null, "Transparency").toDouble()
        val pixelInterpolationMode: String = parser.getAttributeValue(null, "PixelInterpolationMode")
        val name: String? = parser.getAttributeValue(null, "Name")
        val top: Double = parser.getAttributeValue(null, "Top").toDouble()
        val left: Double = parser.getAttributeValue(null, "Left").toDouble()
        val width: Double = parser.getAttributeValue(null, "Width").toDouble()
        val height: Double = parser.getAttributeValue(null, "Height").toDouble()
        val fixedHeightWidthRatio: Boolean = parser.getAttributeValue(null, "FixedHeightWidthRatio")!!.toBoolean()
        val widthRatio: Double = parser.getAttributeValue(null, "WidthRatio").toDouble()
        val heightRatio: Double = parser.getAttributeValue(null, "HeightRatio").toDouble()
        val zIndex: Double = parser.getAttributeValue(null, "ZIndex").toDouble()
        val angle: Int = parser.getAttributeValue(null, "Angle").toInt().fixAngle4()
        val visible: Boolean = parser.getAttributeValue(null, "Visible").toBoolean()
        val locked: Boolean = parser.getAttributeValue(null, "Locked").toBoolean()
        val marginLeft: Double = parser.getAttributeValue(null, "MarginLeft").toDouble()
        val marginTop: Double = parser.getAttributeValue(null, "MarginTop").toDouble()
        val marginRight: Double = parser.getAttributeValue(null, "MarginRight").toDouble()
        val marginBottom: Double = parser.getAttributeValue(null, "MarginBottom").toDouble()
        val foregroundColor: String = parser.getAttributeValue(null, "ForegroundColor")
        val backgroundColor: String = parser.getAttributeValue(null, "BackgroundColor")
        var border: Border? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue
            if (parser.name == "Border")
                border = readBorder(parser)
        }

        return XMLCardDesign.Element.XamlImageElement( source, transparency, pixelInterpolationMode, name, top, left, width, height, fixedHeightWidthRatio, widthRatio, heightRatio, zIndex, angle, visible, locked, marginLeft, marginTop, marginRight, marginBottom, foregroundColor, backgroundColor, border)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readXamlTextElement(parser: XmlPullParser): XMLCardDesign.Element.XamlTextElement {
        parser.require(XmlPullParser.START_TAG,
            ns, "XamlTextElement")

        val text: String = parser.getAttributeValue(null, "Text")
        val textColor: String = parser.getAttributeValue(null, "TextColor")
        val alignmentV: String = parser.getAttributeValue(null, "AlignmentV")
        val alignmentH: String = parser.getAttributeValue(null, "AlignmentH")
        val fontFamily: String = parser.getAttributeValue(null, "FontFamily")
        val fontStyle: String = parser.getAttributeValue(null, "FontStyle")
        val fontWeight: String = parser.getAttributeValue(null, "FontWeight")
        val fontSize: Double = parser.getAttributeValue(null, "FontSize").toDouble()
        val textDecorations: String? = parser.getAttributeValue(null, "TextDecorations")
        val wrapText: Boolean = parser.getAttributeValue(null, "WrapText").toBoolean()
        val clipContent: Boolean = parser.getAttributeValue(null, "ClipContent").toBoolean()
        val fitContentToSize: Boolean = parser.getAttributeValue(null, "FitContentToSize").toBoolean()
        val name: String? = parser.getAttributeValue(null, "Name")
        val top: Double = parser.getAttributeValue(null, "Top").toDouble()
        val left: Double = parser.getAttributeValue(null, "Left").toDouble()
        val width: Double = parser.getAttributeValue(null, "Width").toDouble()
        val height: Double = parser.getAttributeValue(null, "Height").toDouble()
        val fixedHeightWidthRatio: Boolean = parser.getAttributeValue(null, "FixedHeightWidthRatio").toBoolean()
        val widthRatio: Int = parser.getAttributeValue(null, "WidthRatio").toInt()
        val heightRatio: Int = parser.getAttributeValue(null, "HeightRatio").toInt()
        val zIndex: Int = parser.getAttributeValue(null, "ZIndex").toInt()
        val angle: Int = parser.getAttributeValue(null, "Angle").toInt().fixAngle4()
        val visible: Boolean = parser.getAttributeValue(null, "Visible").toBoolean()
        val locked: Boolean = parser.getAttributeValue(null, "Locked").toBoolean()
        val marginLeft: Double = parser.getAttributeValue(null, "MarginLeft").toDouble()
        val marginTop: Double = parser.getAttributeValue(null, "MarginTop").toDouble()
        val marginRight: Double = parser.getAttributeValue(null, "MarginRight").toDouble()
        val marginBottom: String = parser.getAttributeValue(null, "MarginBottom")
        val foregroundColor: String = parser.getAttributeValue(null, "ForegroundColor")
        val backgroundColor: String = parser.getAttributeValue(null, "BackgroundColor")
        var border: Border? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue
            if (parser.name == "Border")
                border =
                    readBorder(
                        parser
                    )
        }

        return XMLCardDesign.Element.XamlTextElement(text, textColor, alignmentV, alignmentH, fontFamily, fontStyle, fontWeight, fontSize, textDecorations, wrapText, clipContent, fitContentToSize, name, top, left, width, height, fixedHeightWidthRatio, widthRatio, heightRatio, zIndex, angle, visible, locked, marginLeft, marginTop, marginRight, marginBottom, foregroundColor, backgroundColor, border)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readXamlPassportPhotoElement(parser: XmlPullParser): XMLCardDesign.Element.XamlPassportPhotoElement {
        parser.require(XmlPullParser.START_TAG,
            ns, "XamlPassportPhotoElement")

        val transparency: Double = parser.getAttributeValue(null, "Transparency").toDouble()
        val pixelInterpolationMode: String = parser.getAttributeValue(null, "PixelInterpolationMode")
        val stretchMode: String = parser.getAttributeValue(null, "StretchMode")
        val name: String = parser.getAttributeValue(null, "Name")
        val top: Double = parser.getAttributeValue(null, "Top").toDouble()
        val left: Double = parser.getAttributeValue(null, "Left").toDouble()
        val width: Double = parser.getAttributeValue(null, "Width").toDouble()
        val height: Double = parser.getAttributeValue(null, "Height").toDouble()
        val fixedHeightWidthRatio: Boolean = parser.getAttributeValue(null, "FixedHeightWidthRatio").toBoolean()
        val widthRatio: Int = parser.getAttributeValue(null, "WidthRatio").toInt()
        val heightRatio: Int = parser.getAttributeValue(null, "HeightRatio").toInt()
        val zIndex: Int = parser.getAttributeValue(null, "ZIndex").toInt()
        val angle: Int = parser.getAttributeValue(null, "Angle").toInt().fixAngle4()
        val visible: Boolean = parser.getAttributeValue(null, "Visible").toBoolean()
        val locked: Boolean = parser.getAttributeValue(null, "Locked").toBoolean()
        val marginLeft: Double = parser.getAttributeValue(null, "MarginLeft").toDouble()
        val marginTop: Double = parser.getAttributeValue(null, "MarginTop").toDouble()
        val marginRight: Double = parser.getAttributeValue(null, "MarginRight").toDouble()
        val marginBottom: Double = parser.getAttributeValue(null, "MarginBottom").toDouble()
        val foregroundColor: String = parser.getAttributeValue(null, "ForegroundColor")
        val backgroundColor: String = parser.getAttributeValue(null, "BackgroundColor")
        var border: Border? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue
            if (parser.name == "Border")
                border =readBorder(parser)
        }

        return XMLCardDesign.Element.XamlPassportPhotoElement(transparency, pixelInterpolationMode, stretchMode, name, top, left, width, height, fixedHeightWidthRatio, widthRatio, heightRatio, zIndex, angle, visible, locked, marginLeft, marginTop, marginRight, marginBottom, foregroundColor, backgroundColor, border)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readBorder(parser: XmlPullParser): Border {
        parser.require(XmlPullParser.START_TAG,
            ns, "Border")

        val color: String = parser.getAttributeValue(null, "Color")
        val thickness: Double = parser.getAttributeValue(null, "Thickness").toDouble()
        val cornerRadius: Double = parser.getAttributeValue(null, "CornerRadius").toDouble()
        val style: String = parser.getAttributeValue(null, "Style")

        while (parser.next() != XmlPullParser.END_TAG);

        return  Border(color, thickness, cornerRadius, style)
    }

    /**
     * XMLCardTemplate
     */
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readTemplate(parser: XmlPullParser): Template {
        parser.require(XmlPullParser.START_TAG, ns, "template")
        val name: String = parser.getAttributeValue(null, "name")
        val cardType: Int = parser.getAttributeValue(null, "card_type").toInt()
        val cardThickness: Int = parser.getAttributeValue(null, "card_thickness").toInt()
        val delete: BOOLEAN? = safeValueOf<BOOLEAN>(parser.getAttributeValue(null, "delete"))
        val source: SOURCE? = safeValueOf<SOURCE>(parser.getAttributeValue(null, "source"))
        val destination: DESTINATION? = safeValueOf<DESTINATION>(parser.getAttributeValue(null, "destination"))

        var fonts: List<Font> = emptyList()
        var sides: List<Side> = emptyList()
        var magdata: Magdata? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue
            when (parser.name) {
                "fonts" -> fonts = readFonts(parser)
                "sides" -> sides = readSides(parser)
                "magdata" -> magdata = readMagdata(parser)
                else -> skip(parser)
            }
        }
//        Log.d("EMBY", "fonts> $fonts\n\n\n")
//        Log.d("EMBY", "sides> $sides\n\n\n")
//        Log.d("EMBY", "magdata> ${magdata.toString()}\n\n\n")

        return Template(name, cardType, cardThickness, delete?:BOOLEAN.no, source?:SOURCE.feeder, destination?:DESTINATION.eject, fonts, sides, magdata)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readFonts(parser: XmlPullParser): List<Font> {
        val fonts = mutableListOf<Font>()

        parser.require(XmlPullParser.START_TAG, ns, "fonts")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue

            if (parser.name == "font")
                fonts.add(readFont(parser))
            else skip(parser)
        }
        return fonts
    }

    private fun readFont(parser: XmlPullParser): Font {
        parser.require(XmlPullParser.START_TAG, ns, "font")

        val id: Int = parser.getAttributeValue(null, "id").toInt()
        val name: String = parser.getAttributeValue(null, "name")
        val size: Double = parser.getAttributeValue(null, "size").toDouble()
        val bold: BOOLEAN = BOOLEAN.valueOf(parser.getAttributeValue(null, "bold"))
        val italic: BOOLEAN = BOOLEAN.valueOf(parser.getAttributeValue(null, "italic"))
        val underline: BOOLEAN = BOOLEAN.valueOf(parser.getAttributeValue(null, "underline"))

        if (parser.name == "font")
            parser.nextTag()

        return Font(id, name, size, bold, italic, underline)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readSides(parser: XmlPullParser): List<Side> {
        val sides = mutableListOf<Side>()

        parser.require(XmlPullParser.START_TAG, ns, "sides")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue

            if (parser.name == "side")
                sides.add(readSide(parser))
            else skip(parser)
        }
        return sides
    }

    private fun readSide(parser: XmlPullParser): Side {
        parser.require(XmlPullParser.START_TAG, ns, "side")

        val name: SIDETYPE = SIDETYPE.valueOf(parser.getAttributeValue(null, "name"))
        val orientation: ORIENTATION? = safeValueOf<ORIENTATION>(parser.getAttributeValue(null, "orientation"))
        val rotation: Int = parser.getAttributeValue(null, "rotation").toInt()
            .fixAngle2()
        val sharpness: SHARPNESS? = safeValueOf<SHARPNESS>(parser.getAttributeValue(null, "sharpness"))
        val kMode: K_MODE? = safeValueOf<K_MODE>(parser.getAttributeValue(null, "k_mode"))

        var printTypes: List<PrintType> = listOf()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue
            when (parser.name) {
                "print_types" -> printTypes = readPrintTypes(parser)
                else -> skip(parser)
            }
        }

        return Side(name, orientation?:ORIENTATION.landscape, rotation, sharpness?:SHARPNESS.off, kMode?:K_MODE.mixed, printTypes)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readPrintTypes(parser: XmlPullParser): List<PrintType> {
        val printTypes = mutableListOf<PrintType>()

        parser.require(XmlPullParser.START_TAG, ns, "print_types")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue

            if (parser.name == "print_type")
                printTypes.add(readPrintType(parser))
            else skip(parser)
        }
        return printTypes
    }

    private fun readPrintType(parser: XmlPullParser): PrintType {
        parser.require(XmlPullParser.START_TAG, ns, "print_type")

        val type: PRINTYPE = PRINTYPE.valueOf(parser.getAttributeValue(null, "type"))
        val fill: String? = parser.getAttributeValue(null, "fill")
        val preheat: Double? = parser.getAttributeValue(null, "preheat").let { it?:"" }.toDoubleOrNull()

        val elements: MutableList<XMLCardTemplate.Element> = mutableListOf()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue
            when (parser.name) {
                "graphic" -> elements.add(readGraphic(parser))
                "text" -> elements.add(readText(parser))
                "barcode" -> elements.add(readBarcode(parser))
                "line" -> elements.add(readLine(parser))
                "ellipse" -> elements.add(readEllipse(parser))
                "rectangle" -> elements.add(readRectangle(parser))
                else -> skip(parser)
            }
        }

        return PrintType(type, fill?:"none", preheat?:0.0, elements)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readGraphic(parser: XmlPullParser): XMLCardTemplate.Element.Graphic {
        parser.require(XmlPullParser.START_TAG, ns, "graphic")

        val orderId: Int? = parser.getAttributeValue(null, "order_id").let {it?:""}.toIntOrNull()
        val field: String? = parser.getAttributeValue(null, "field")
        val format: GRAPHICFORMAT = GRAPHICFORMAT.valueOf(parser.getAttributeValue(null, "format"))
        val opacity: Double? = parser.getAttributeValue(null, "opacity").let { it?:"" }.toDoubleOrNull()
        val height: Int = parser.getAttributeValue(null, "height").toInt()
        val width: Int = parser.getAttributeValue(null, "width").toInt()
        val x: Int = parser.getAttributeValue(null, "x").toInt()
        val y: Int = parser.getAttributeValue(null, "y").toInt()
        val rotation: Int = parser.getAttributeValue(null, "rotation").let { it?:"0" }.toInt().fixAngle4()
        val delete: BOOLEAN? = safeValueOf<BOOLEAN>(parser.getAttributeValue(null, "delete"))

        var reference: String? = null

        if (field.isNullOrEmpty()) {
            reference = readTextInTag(parser)
            parser.require(XmlPullParser.END_TAG, ns, "graphic")
        }
        else
            while (parser.next() != XmlPullParser.END_TAG);

        return XMLCardTemplate.Element.Graphic(orderId, field, format, opacity?:100.0, height, width, x, y, rotation, delete?:BOOLEAN.no, reference)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readText(parser: XmlPullParser): XMLCardTemplate.Element.Text {
        parser.require(XmlPullParser.START_TAG, ns, "text")

        val orderId: Int? = parser.getAttributeValue(null, "order_id").let {it?:""}.toIntOrNull()
        val field: String? = parser.getAttributeValue(null, "field")
        val fontId: Int = parser.getAttributeValue(null, "font_id").toInt()
        val width: Int? = parser.getAttributeValue(null, "width").let { it?:"" }.toIntOrNull()
        val height: Int? = parser.getAttributeValue(null, "height").let { it?:"" }.toIntOrNull()
        val x: Int = parser.getAttributeValue(null, "x").toInt()
        val y: Int = parser.getAttributeValue(null, "y").toInt()
        val color: String? = parser.getAttributeValue(null, "color")
        val angle: Int = parser.getAttributeValue(null, "angle").toInt()
        val alignment: HALIGNMENT? = safeValueOf<HALIGNMENT>(parser.getAttributeValue(null, "alignment"))
        val vAlignment: VALIGNMENT? = safeValueOf<VALIGNMENT>(parser.getAttributeValue(null, "v_alignment"))
        val shrink: BOOLEAN? = safeValueOf<BOOLEAN>(parser.getAttributeValue(null, "shrink"))

        var data: String? = null

        if (field.isNullOrEmpty()) {
            data = readTextInTag(parser)
            parser.require(XmlPullParser.END_TAG, ns, "text")
        }
        else
            while (parser.next() != XmlPullParser.END_TAG);


        return XMLCardTemplate.Element.Text(orderId, field, fontId, width?:0, height?:0, x, y, color?:"0x000000", angle, alignment?:HALIGNMENT.left, vAlignment?:VALIGNMENT.top, shrink?:BOOLEAN.yes, data)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readBarcode(parser: XmlPullParser): XMLCardTemplate.Element.Barcode {
        parser.require(XmlPullParser.START_TAG, ns, "barcode")

        val orderId: Int? = parser.getAttributeValue(null, "order_id").let {it?:""}.toIntOrNull()
        val field: String? = parser.getAttributeValue(null, "field")
        val fontId: Int? = parser.getAttributeValue(null, "font_id").let {it?:""}.toIntOrNull()
        val x: Int = parser.getAttributeValue(null, "x").toInt()
        val y: Int = parser.getAttributeValue(null, "y").toInt()
        val rotation: Int = parser.getAttributeValue(null, "rotation").let {it?:"0"}.toInt().fixAngle4()
        val code: CODE = CODE.valueOf(parser.getAttributeValue(null, "code"))
        val height: Int = parser.getAttributeValue(null, "height").toInt()
        val width: Int = parser.getAttributeValue(null, "width").toInt()
        val encodingName: String? = parser.getAttributeValue(null, "encoding_name")
        val quietZoneWidth: Double? = parser.getAttributeValue(null, "quiet_zone_width").toDoubleOrNull()
//      PDF417
        val showText: BOOLEAN? = safeValueOf<BOOLEAN>(parser.getAttributeValue(null, "show_text"))
        val correctionLevel: Double? = parser.getAttributeValue(null, "correction_level").let {it?:""}.toDoubleOrNull()
        val minColumns: Int? = parser.getAttributeValue(null, "minColumns").let { it?:"" }.toIntOrNull()
        val columns: Int? = parser.getAttributeValue(null, "columns").let { it?:"" }.toIntOrNull()
        val minRows: Int? = parser.getAttributeValue(null, "minRows").let { it?:"" }.toIntOrNull()
        val rows: Int? = parser.getAttributeValue(null, "rows").let { it?:"" }.toIntOrNull()
        val compact: BOOLEAN? = safeValueOf<BOOLEAN>(parser.getAttributeValue(null, "compact"))
        val compactionMode: COMPACTIONMODE? = safeValueOf<COMPACTIONMODE>(parser.getAttributeValue(null, "compactionMode"))
//      QRCODE
        val version: String? = parser.getAttributeValue(null, "version")
        val errorCorrectionLevel: ERRCORRECTIONLVL? = safeValueOf<ERRCORRECTIONLVL>(parser.getAttributeValue(null, "error_correction_level"))

        var data: String? = null

        if (field.isNullOrEmpty()) {
            data = readTextInTag(parser)
            parser.require(XmlPullParser.END_TAG, ns, "barcode")
        }
        else
            while (parser.next() != XmlPullParser.END_TAG);


        return XMLCardTemplate.Element.Barcode(orderId, field, fontId, width, height, x, y, rotation, code, encodingName, quietZoneWidth, showText, correctionLevel, minColumns, columns, minRows, rows, compact, compactionMode, version, errorCorrectionLevel, data)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readLine(parser: XmlPullParser): XMLCardTemplate.Element.Line {
        parser.require(XmlPullParser.START_TAG, ns, "line")

        val orderId: Int? = parser.getAttributeValue(null, "order_id").let {it?:""}.toIntOrNull()
        val x1: Int = parser.getAttributeValue(null, "x1").toInt()
        val y1: Int = parser.getAttributeValue(null, "y1").toInt()
        val x2: Int = parser.getAttributeValue(null, "x2").toInt()
        val y2: Int = parser.getAttributeValue(null, "y2").toInt()
        val thickness: Int = parser.getAttributeValue(null, "thickness").toInt()
        val color: String = parser.getAttributeValue(null, "color")

        while (parser.next() != XmlPullParser.END_TAG);

        return XMLCardTemplate.Element.Line(orderId, x1, y1, x2, y2, thickness, color)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readEllipse(parser: XmlPullParser): XMLCardTemplate.Element.Ellipse {
        parser.require(XmlPullParser.START_TAG, ns, "ellipse")

        val orderId: Int? = parser.getAttributeValue(null, "order_id").let {it?:""}.toIntOrNull()
        val x: Int = parser.getAttributeValue(null, "x").toInt()
        val y: Int = parser.getAttributeValue(null, "y").toInt()
        val height: Int = parser.getAttributeValue(null, "height").toInt()
        val width: Int = parser.getAttributeValue(null, "width").toInt()
        val thickness: Int = parser.getAttributeValue(null, "thickness").toInt()
        val color: String = parser.getAttributeValue(null, "color")
        val fillColor: String = parser.getAttributeValue(null, "fill_color")

        while (parser.next() != XmlPullParser.END_TAG);

        return XMLCardTemplate.Element.Ellipse(orderId, x, y, height, width, thickness, color, fillColor)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readRectangle(parser: XmlPullParser): XMLCardTemplate.Element.Rectangle {
        parser.require(XmlPullParser.START_TAG, ns, "rectangle")

        val orderId: Int? = parser.getAttributeValue(null, "order_id").let {it?:""}.toIntOrNull()
        val x: Int = parser.getAttributeValue(null, "x").toInt()
        val y: Int = parser.getAttributeValue(null, "y").toInt()
        val height: Int = parser.getAttributeValue(null, "height").toInt()
        val width: Int = parser.getAttributeValue(null, "width").toInt()
        val thickness: Int = parser.getAttributeValue(null, "thickness").toInt()
        val color: String = parser.getAttributeValue(null, "color")
        val fillColor: String = parser.getAttributeValue(null, "fill_color")
        val radius: Double = parser.getAttributeValue(null, "radius").toDouble()

        while (parser.next() != XmlPullParser.END_TAG);

        return XMLCardTemplate.Element.Rectangle(orderId, x, y, height, width, thickness, color, fillColor, radius)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readMagdata(parser: XmlPullParser): Magdata {
        parser.require(XmlPullParser.START_TAG, ns, "magdata")

        val format: MAGDATAFORMAT = MAGDATAFORMAT.valueOf(parser.getAttributeValue(null, "format"))
        val coercivity: COERCIVITY = COERCIVITY.valueOf(parser.getAttributeValue(null, "coercivity"))
        val verify: BOOLEAN = BOOLEAN.valueOf(parser.getAttributeValue(null, "verify"))

        var track: Track? = null

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue
            when (parser.name) {
                "track" -> track = readTrack(parser)
                else -> skip(parser)
            }
        }

        return Magdata(format, coercivity, verify, track!!)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readTrack(parser: XmlPullParser): Track {
        parser.require(XmlPullParser.START_TAG, ns, "track")

        val field: String = parser.getAttributeValue(null, "field")
        val number: Double = parser.getAttributeValue(null, "number").toDouble()
        val format: TRACKFORMAT = TRACKFORMAT.valueOf(parser.getAttributeValue(null, "format"))

        while (parser.next() != XmlPullParser.END_TAG);

        return Track(field, number, format)
    }

    /**
     * GENERIC
     */
    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTextInTag(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    private fun Int.fixAngle2(): Int {
        var angle = this
        when (angle % 360) {
            XMLCardTemplate.ROTATION_0,
            XMLCardTemplate.ROTATION_180 -> {}
            in (Int.MIN_VALUE..135) -> angle = XMLCardTemplate.ROTATION_0
            in (135..Int.MAX_VALUE) -> angle = XMLCardTemplate.ROTATION_180
        }
        return angle
    }
    private fun Int.fixAngle4(): Int {
        var angle = this
        when (angle % 360) {
            XMLCardTemplate.ROTATION_0,
            XMLCardTemplate.ROTATION_90,
            XMLCardTemplate.ROTATION_180,
            XMLCardTemplate.ROTATION_270 -> {}
            in (Int.MIN_VALUE..45) -> angle = XMLCardTemplate.ROTATION_0
            in (45..135) -> angle = XMLCardTemplate.ROTATION_90
            in (135..225) -> angle = XMLCardTemplate.ROTATION_180
            in (225..Int.MAX_VALUE) -> angle = XMLCardTemplate.ROTATION_270
        }
        return angle
    }

    private inline fun <reified T : Enum<T>> safeValueOf(type: String?): T? =
         if (!type.isNullOrEmpty())
             java.lang.Enum.valueOf(T::class.java, type)
         else
             null
}