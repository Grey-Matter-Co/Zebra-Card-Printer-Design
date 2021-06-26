package mx.com.infotecno.zebracardprinter.model

sealed class XMLCardTemplate {
	enum class BOOLEAN {
		No, Yes;
		companion object {
			fun valueOf(value: Boolean): BOOLEAN =
				if (value) Yes
				else No
		}
	}
	enum class SOURCE {Feeder, Internal, Atm, Autodetect}
	enum class DESTINATION {Eject, Reject, Hold, Feeder, Lam_top, Lam_bottom, Lam_both, Lam_any, Lam_none}
	enum class SIDETYPE {Front, Back}
	enum class ORIENTATION {Landscape, Portrait}
	enum class SHARPNESS {Off, Low, Normal, High}
	enum class K_MODE {Text, Barcode, Mixed, Picture}
	enum class PRINTYPE {Color, Monochrome, Overlay, Inhibit, Helper}
	enum class GRAPHICFORMAT {bmp, jpeg, png}
	enum class HALINGMENT {Left, Right, Center}
	enum class VALINGMENT {Top, Bottom, Center}
	enum class CODE {Code39, Code128, PDF417, EAN8, EAN13, QRCode}
	enum class COMPACTIONMODE {Auto, Byte, Numeric, Text}
	enum class ECORRECTIONLVL {L, M, Q, H}
	enum class MAGDATAFORMAT {ISO, AAMVA, JIS, Custom, Binary}
	enum class COERCIVITY {High, Low}
	enum class TRACKFORMAT {ASCII, HEX}

	//    <template name="" card_type="" card_thickness="" delete="" source="" destination="">
//        name name of the template
//        card_type identifies the card type
//        card_thickness thickness of card in mm; default is 30
//        delete “no” = job data will be kept until the next job is received
//                “yes” = job will be deleted at the end of processing
//        source “feeder” = load card from feeder (default)
//                “internal” = use card from internal position
//                “atm” = load card from ATM slot
//                “autodetect” = load card from feeder or ATM slot
//        destination “eject” = normal exit path for printer without laminator (default for ZXP 1/3 series printers and ZMotif
//                            series printers without a laminator. For ZMotif printers with a laminator the default will be set based
//                            on installed laminate.)
//                    “reject” = card goes into the reject tray
//                    “hold” = card goes to the home position
//                    “feeder” = card returns to the input location
//                    “lam_top” = card goes to laminator for top side lamination
//                    “lam_bottom” = card goes to laminator for bottom side lamination
//                    “lam_both” = card goes to laminator for top and bottom side lamination
//                    “lam_any” = card goes to laminator without regard for laminate availability. if no laminate is
//                                installed, the card simply passes through the laminator
//                    “lam_none” = card passes through laminator without lamination
	data class Template (val name: String, val card_type: Int, val card_thickness: Int = 30, val delete: BOOLEAN, val source: SOURCE = SOURCE.Feeder, val destination: DESTINATION = DESTINATION.Eject, val Fonts: List<Font>, val Sides: List<Side>, val Magdata: Magdata?)

	//    <font id="" name="" size="" bold="" italic="" underline="" />
//        id font index; used by a text tag
//        name font name; default is Arial
//        size font point size; default is 10
//        bold “yes” or “no”; default is “no”
//        italic “yes” or “no”; default is “no”
//        underline “yes” or “no”; default is “no”
	data class Font (val id: Int, val name: String = "Arial", val size: Int = 10, val bold: BOOLEAN = BOOLEAN.No, val italic: BOOLEAN = BOOLEAN.No, val underline: BOOLEAN = BOOLEAN.No)

	//    <side name="" orientation="" rotation="" sharpness="" k_mode=””>
//        name “front” or “back” default is “front”
//        orientation “landscape” or “portrait” default is “landscape”
//        rotation 0 or 180; default is 0
//        sharpness “off”, “low”, “normal”, “high”; default is “off”
//        k_mode “text”, “barcode”, mixed”, “picture”; default is “mixed”
	data class Side (val name: SIDETYPE = SIDETYPE.Front, val orientation: ORIENTATION = ORIENTATION.Landscape, val rotation: Double = 0.0, val sharpness: SHARPNESS = SHARPNESS.Off, val k_mode: K_MODE = K_MODE.Mixed, val printTypes: List<PrintType>)

	//    <print_type type="" fill="" preheat="">
//        type “color”, “monochrome”, “overlay”, “inhibit”, “helper”; defaultis color
//        fill background fill color (RGB) for the fill layer; default is none
//        preheat valid range -50 to 50 for color, mono front, or mono back only
	data class PrintType (val type: PRINTYPE = PRINTYPE.Color, val fill: String = "none", val preheat: Double = 0.0, val elements: List<Element>) //TODO("find out data type of preheat")
	
	abstract class Element
	//    <graphic order_id="" field="" format="" opacity="" height="" width="" x="" y="" rotation="" delete="" />
//        order_id processing order, 1 thru x with 1 being the bottom layer
//        field reference name for data binding
//        format “bmp”, “jpeg”; default is “bmp”
//        opacity image opacity level; default is 100
//        height height of the image in pixels
//        width width of the image in pixels
//        x x axis location in pixels
//        y y axis location in pixels
//        rotation clockwise angle of rotation 0, 90, 180, 270; default is 0
//        delete “yes” or “no” delete image after processing; default=”no”
//    <graphic>reference</graphic> reference specifies the name of a stored image
	data class Graphic (val order_id: Int?, val field: String?, val format: GRAPHICFORMAT = GRAPHICFORMAT.bmp, val opacity: Double = 100.0, val height: Int, val width: Int, val x: Int, val y: Int, val rotation: Double, val delete: BOOLEAN = BOOLEAN.No, val reference: String?): Element() // TODO("find out how to set in tag references and not like as attribute")

//	<text order_id="" field="" font_id="" x="" y="" color="" angle="" height="" width="" alignment="" v_alignment="" shrink=""/>
//		order_id processing order, 1 thru x with 1 being the bottom layer
//		field reference name for data binding
//		font_id font reference
//		x x axis location in pixels
//		y y axis location in pixels
//		color RGB text color
//				red “FF0000”
//				green “00FF00”
//				blue “0000FF”
//		angle clockwise angle of rotation
//		width width of the text box; optional
//		height height of the text box; optional
//		alignment horizontal alignment within the text box; only valid if height and width have been defined
//					“left”, “right”, “center”; default is “left”
//		v_alignment vertical alignment within the text box; only valid if height and width have been defined
//					“top”, “bottom”, “center”; default is “left”
//		shrink “yes” or “no”; “yes” indicates if the text is to fit within the widthspecification
//	<text>data</text> data specifies the text data to print
	data class Text (val order_id: Int?, val field: String?, val font_id: Int, val width: Int, val height: Int, val x: Int, val y: Int, val color: String, val angle: Double, val alingment: HALINGMENT = HALINGMENT.Left, val v_alignment: VALINGMENT = VALINGMENT.Top, val shrink: BOOLEAN = BOOLEAN.Yes, val data: String?): Element() // TODO("find out how to set in tag references and not like as attribute")

//	<barcode order_id="" field="" font_id="" x="" y="" rotation="" code="" height="" width="" quiet_zone_width="" show_text="" correction_level="" minColumns="" columns="" minRows="" rows="" compact="" compactionMode="" error_correction_level="" encoding_name="" />
//		order_id processing order, 1 thru x with 1 being the bottom layer
//		field reference name for data binding
//		font_id font used to display barcode text
//		x x axis location
//		y y axis location
//		rotation clockwise angle of rotation; “0”, “90”, “180”, or “270”
//		code “code39”, “code128”, “pdf417”, “ean8”, “ean13”, “qrcode”
//		height sets the height of the barcode
//		width sets the width of the barcode
//		encoding_name sets the message encoding. The value must conform to one of Java's encodings and have a
//						mapping in the ECI registry
//		quiet_zone_width area around bar code that serves to isolate it from surrounding text and graphics
//		show_text indicates if text is to be shown under the barcode; “yes” or“no”
//		correction_level pdf417 - sets the error correction level for the barcode, a value between 0and 8
//		minColumns pdf417 - sets the minimum number of data columns for the barcode
//		columns pdf417 - sets the maximum number of data columns for the barcode
//		minRows pdf417 - sets the minimum number of data rows for the barcode
//		rows pdf417 - sets the maximum number of data rows for the barcode
//		compact pdf417 - indicates whether to apply compact mode; “yes” or “no”
//		compactionMode pdf417 - sets the compaction mode; “auto”, “byte”, “numeric”, or “text”
//		version qrcode - sets the version of the qr code to be encoded
//		error_correction_level qrcode - “l”: approximately 7% of codewords can be restored. Error correction level L is appropriate
//											for high symbol quality and/or the need for the smallest possible symbol
//										“m”: approximately 15% of codewords can be restored. Level M is described as Standard
//											level and offers a good compromise between small size and increased reliability
//										“q”: approximately 25% of codewords can be restored. Level Q is a High reliability level
//											and suitable for more critical or poor print quality applications
//										“h”: approximately 30% of codewords can be restored. Level H offers the maximum
//											achievable reliability
//	<barcode>data</barcode> data specifies the barcode to print
	data class Barcode(val order_id: Int?, val field: String?, val font_id: Int, val width: Int, val height: Int, val x: Int, val y: Int, val rotation: Double, val code: CODE, val encoding_name: String, val quiet_zone_width: Double, val show_text: BOOLEAN, val correction_level: Double?, val minColumns: Int?, val columns: Int?, val  minRows: Int?, val rows: Int?, val compact: BOOLEAN?, val compactionMode: COMPACTIONMODE?, val error_correction_level: ECORRECTIONLVL?, val data: String): Element() // TODO("find out how to set in tag references and not like as attribute")

//	<line order_id="" x1="" y1="" x2="" y2="" thickness="" color="" />
//		order_id processing order, 1 thru x with 1 being the bottom layer
//		x1 start x axis location
//		y1 start y axis location
//		x2 end x axis location
//		y2 end y axis location
//		thickness line thickness in pixels
//		color RGB text color
//				red “FF0000”
//				green “00FF00”
//				blue “0000FF”
	data class Line (val order_id: Int?, val x1: Int, val y1: Int, val x2: Int, val y2: Int, val thickness: Double, val color: String): Element()

//	<ellipse order_id="" x="" y="" height="" width="" thickness="" color="" fill_color="" />
//		order_id processing order, 1 thru x with 1 being the bottom layer
//		x x axis location in pixels
//		y y axis location
//		width width in number of pixels
//		height height in number of pixels
//		thickness line thickness in number of pixels
//		color line color in RGB
//		fill_color fill color in RGB; if attribute does not exist indicates no fill ortransparent
	data class Ellipse (val order_id: Int?, val x: Int, val y: Int, val height: Int, val width: Int, val thickness: Double, val color: String, val fill_color: String): Element()

//	<rectangle order_id="" x="" y="" height="" width="" thickness="" color="" fill_color="" radius="" />
//		order_id processing order, 1 thru x with 1 being the bottom layer
//		x x axis location
//		y y axis location
//		width width in number of pixels
//		height height in number of pixels
//		thickness line thickness in number of pixels
//		color line color in RGB
//		fill_color fill color in RGB; if attribute does not exist indicates no fillor transparent
//		radius for rounded corners; numeric value: default is 0
	data class Rectangle (val order_id: Int?, val x: Int, val y: Int, val height: Int, val width: Int, val thickness: Double, val color: String, val fill_color: String, val radius: Double = 0.0): Element()

//	<magdata format="" coercivity="" verify="">
//		format “iso”, “aamva”, “jis”, “custom”, “binary”; default is “iso”. iso only for ZXP 1/3series printers
//		coercivity “high” or “low”; default is “high”
//		verify “yes” or “no”; default is “yes”
	data class Magdata (val format: MAGDATAFORMAT = MAGDATAFORMAT.ISO, val coercivity: COERCIVITY = COERCIVITY.High, val verify: BOOLEAN = BOOLEAN.Yes, val track: Track)

//	<track field="" number="" format="" />
//		field reference name for data binding
//		number track number to encode
//		format “ascii” or “hex”; default is asci. ascii only for ZXP 1/3printers
	data class Track (val field: String, val number: Double, val format: TRACKFORMAT = TRACKFORMAT.ASCII)
}