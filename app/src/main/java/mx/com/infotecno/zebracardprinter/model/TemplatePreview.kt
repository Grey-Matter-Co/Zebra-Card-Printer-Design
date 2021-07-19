package mx.com.infotecno.zebracardprinter.model

import android.graphics.Bitmap

internal class TemplatePreview private constructor(val label: String, val bitmap: Bitmap?, val message: String?) {
	constructor(label: String, bitmap: Bitmap?) : this(label, bitmap, null)
	constructor(label: String, message: String?) : this(label, null, message)
}
