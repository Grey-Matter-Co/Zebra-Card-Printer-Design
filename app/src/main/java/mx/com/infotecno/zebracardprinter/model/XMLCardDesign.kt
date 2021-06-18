package mx.com.infotecno.zebracardprinter.model

sealed class XMLCardDesign {
    data class CardDesignProject(val dinamycFields: List<DynamicField>?, val frontRibbon: Ribbon?, val backRibbon: Ribbon?, val frontDocument: Document?, val backDocument: Document?, val version: Int, val name: String, val description: String?, val cardWidth: Double, val cardHeight: Double, val hasIDChip:Boolean, val hasMagstripe:Boolean, val hasLaminate: Boolean, val hasOverlay: Boolean, val hasUV: Boolean, val isSingleSided: Boolean, val writeDirection: String, val xmlns: String)
    data class DynamicField(val name: String, val typeName: String, val textFormat: String, val dataFieldName: String?, val defaultValue: String?, val isFixedLength: Boolean, val fixedLength: Int, val alignment: String, val paddingValue: String)
    data class Layer(val name: String, val type: String)
    data class XamlDesignLayer(var name: String, var elements: List<Element>)
    data class Ribbon (val name: String?, val ribbonType: String, val orientation: String, val layerName: String, val layerType: String)
    data class Document(val name: String?, val nameLayer: String, val elements: List<Element>)
    sealed class Element {
        data class XamlImageElement(val source: String, val transparency: Double, val pixelInterpolationMode: String, val name: String?, val top: Double, val left: Double, val width: Double, val height: Double, val fixedHeightWidthRatio: Boolean, val widthRatio: Double, val heightRatio: Double, val zIndex: Double, val angle: Double, val visible: Boolean, val locked: Boolean, val marginLeft: Double, val marginTop: Double, val marginRight: Double, val marginBottom: Double, val foregroundColor: String, val backgroundColor: String, val border: Border?): Element()
        data class XamlTextElement(val text: String, val textColor: String, val alignmentV: String, val alignmentH: String, val fontFamily: String, val fontStyle: String, val fontWeight: String, val fontSize: Double, val textDecorations: String?, val wrapText: Boolean, val clipContent: Boolean, val fitContentToSize: Boolean, val name: String?, val top: Double, val left: Double, val width: Double, val height: Double, val fixedHeightWidthRatio: Boolean, val widthRatio: Int, val heightRatio: Int, val zIndex: Int, val angle: Double, val visible: Boolean, val locked: Boolean, val marginLeft: Double, val marginTop: Double, val marginRight: Double, val marginBottom: String, val foregroundColor: String, val backgroundColor: String, val border: Border?): Element()
        data class XamlPassportPhotoElement(val transparency: Double, val pixelInterpolationMode: String, val stretchMode: String, val name: String, val top: Double, val left: Double, val width: Double, val height: Double, val fixedHeightWidthRatio: Boolean, val widthRatio: Int, val heightRatio: Int, val zIndex: Int, val angle: Double, val visible: Boolean, val locked: Boolean, val marginLeft: Double, val marginTop: Double, val marginRight: Double, val marginBottom: Double, val foregroundColor: String, val backgroundColor: String, val border: Border?): Element()

    }

    data class Border(val color: String, val thickness: Double, val cornerRadius: Double, val style: String)
}