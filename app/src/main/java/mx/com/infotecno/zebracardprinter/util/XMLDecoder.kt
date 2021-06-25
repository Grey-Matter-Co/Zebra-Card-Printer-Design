package mx.com.infotecno.zebracardprinter.util

import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import kotlin.jvm.Throws
import mx.com.infotecno.zebracardprinter.model.XMLCardDesign.*

object XMLDecoder {
    private val ns: String? = null

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): CardDesignProject {
        val parser: XmlPullParser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)
        parser.nextTag()
        return readFeed(parser)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readFeed(parser: XmlPullParser): CardDesignProject
        = readCardDesignProject(parser)

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

        return CardDesignProject(dynamicFields, frontRibbon, backRibbon, frontDocument, backDocument, version, name, description, cardWidth, cardHeight, hasIDChip!!, hasMagstripe!!, hasLaminate!!, hasOverlay!!, hasUV!!, isSingleSided!!, writeDirection, xmlns)
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

        return DynamicField(name, typeName, textFormat, dataFieldName, defaultValue, isFixedLength!!, fixedLength, alignment, paddingValue)
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
    private fun readElements(parser: XmlPullParser): List<Element> {
        val elements = mutableListOf<Element>()
        parser.require(XmlPullParser.START_TAG,
            ns, "Elements")

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue
            when (parser.name) {
                "XamlImageElement" -> elements.add(
                    readXamlImageElement(
                        parser
                    )
                )
                "XamlTextElement" -> elements.add(
                    readXamlTextElement(
                        parser
                    )
                )
                "XamlPassportPhotoElement" -> elements.add(
                    readXamlPassportPhotoElement(
                        parser
                    )
                )
                else -> { Log.d("EMBY ERROR", "unhandled Tag"); skip(parser) }
            }
        }
        return  elements
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readXamlImageElement(parser: XmlPullParser): Element.XamlImageElement {
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
        val fixedHeightWidthRatio: Boolean = parser.getAttributeValue(null, "FixedHeightWidthRatio")!!
            .toBoolean()
        val widthRatio: Double = parser.getAttributeValue(null, "WidthRatio").toDouble()
        val heightRatio: Double = parser.getAttributeValue(null, "HeightRatio").toDouble()
        val zIndex: Double = parser.getAttributeValue(null, "ZIndex").toDouble()
        val angle: Double = parser.getAttributeValue(null, "Angle").toDouble()
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

        return Element.XamlImageElement( source, transparency, pixelInterpolationMode, name, top, left, width, height, fixedHeightWidthRatio, widthRatio, heightRatio, zIndex, angle, visible!!, locked!!, marginLeft, marginTop, marginRight, marginBottom, foregroundColor, backgroundColor, border)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readXamlTextElement(parser: XmlPullParser): Element.XamlTextElement {
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
        val angle: Double = parser.getAttributeValue(null, "Angle").toDouble()
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

        return Element.XamlTextElement(text, textColor, alignmentV, alignmentH, fontFamily, fontStyle, fontWeight, fontSize, textDecorations, wrapText!!, clipContent!!, fitContentToSize!!, name, top, left, width, height, fixedHeightWidthRatio!!, widthRatio, heightRatio, zIndex, angle, visible!!, locked!!, marginLeft, marginTop, marginRight, marginBottom, foregroundColor, backgroundColor, border)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readXamlPassportPhotoElement(parser: XmlPullParser): Element.XamlPassportPhotoElement {
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
        val angle: Double = parser.getAttributeValue(null, "Angle").toDouble()
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

        return Element.XamlPassportPhotoElement(transparency, pixelInterpolationMode, stretchMode, name, top, left, width, height, fixedHeightWidthRatio!!, widthRatio, heightRatio, zIndex, angle, visible!!, locked!!, marginLeft, marginTop, marginRight, marginBottom, foregroundColor, backgroundColor, border)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readBorder(parser: XmlPullParser): Border? {
        parser.require(XmlPullParser.START_TAG,
            ns, "Border")

        val color: String = parser.getAttributeValue(null, "Color")
        val thickness: Double = parser.getAttributeValue(null, "Thickness").toDouble()
        val cornerRadius: Double = parser.getAttributeValue(null, "CornerRadius").toDouble()
        val style: String = parser.getAttributeValue(null, "Style")

        while (parser.next() != XmlPullParser.END_TAG);

        return  Border(color, thickness, cornerRadius, style)
    }

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
}