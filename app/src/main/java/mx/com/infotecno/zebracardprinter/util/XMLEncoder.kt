package mx.com.infotecno.zebracardprinter.util

import android.util.Log
import android.util.Xml
import mx.com.infotecno.zebracardprinter.model.XMLCardDesign
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.jvm.Throws

object XMLEncoder {
	private val ns: String? = null

	@Throws(XmlPullParserException::class, IOException::class)
	fun parse() {
		val serializer = Xml.newSerializer()
		val parser: XmlPullParser = Xml.newPullParser()
//		serializer.
//		serializer.startDocument("xml", true)
//		serializer.endDocument()
//		val os = ByteArrayOutputStream()
//		serializer.setOutput(os, "UTF-8")
//		Log.d("EMBY", "parse: ${os.toString()}")
	}
}