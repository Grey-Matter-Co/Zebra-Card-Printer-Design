package mx.com.infotecno.zebracardprinter.ui.printtemplate

import android.app.Application
import android.content.ContentResolver
import android.database.ContentObserver
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zebra.sdk.common.card.template.ZebraCardTemplate
import kotlinx.coroutines.launch
import mx.com.infotecno.zebracardprinter.MainActivity
import mx.com.infotecno.zebracardprinter.R
import mx.com.infotecno.zebracardprinter.action.ZCardTemplatePrintAction
import mx.com.infotecno.zebracardprinter.model.XMLCardTemplate
import mx.com.infotecno.zebracardprinter.model.ZCardTemplate
import mx.com.infotecno.zebracardprinter.util.FileHelper
import java.io.File
import java.util.*
import mx.com.infotecno.zebracardprinter.util.ExecutingDevicesHelper as EDHelper


class PrintTemplateViewModel(application: Application) : AndroidViewModel(application) {

	private val _actions = MutableLiveData<ZCardTemplatePrintAction>()    // This will be operated and changed
	val action: LiveData<ZCardTemplatePrintAction> get() = _actions      // This only returns unmodified list

	private var contentObserver: ContentObserver? = null


//	private val _actions = mutableMapOf<String, Any>()    // This will be operated and changed
//	val action: Map<String, Any> get() = this._actions      // This only returns unmodified list
	var zebraCardTemplate: ZebraCardTemplate = ZebraCardTemplate(getApplication(), null).apply {
		Log.d("EMBY", "zebraCardTemplate: ${getApplication<Application>().filesDir.path+ File.separator + MainActivity.TEMPLATEFILEDIRECTORY}")
		this.setTemplateFileDirectory(getApplication<Application>().filesDir.path+ File.separator + MainActivity.TEMPLATEFILEDIRECTORY)
		this.setTemplateImageFileDirectory(getApplication<Application>().filesDir.path+ File.separator + MainActivity.TEMPLATEIMAGEFILEDIRECTORY)
	}

	fun loadTemplate(zCardTemplate: ZCardTemplate? = null, printerCard: CardView? = null) {
		viewModelScope.launch {
			if (zCardTemplate != null && printerCard != null) {

				val template = FileHelper.queryTemplate(getApplication<Application>(), zCardTemplate.name)
				val listFieldViews = mutableListOf<View>()

				template.Sides.forEach { side ->
					when (side.name) {
						XMLCardTemplate.SIDETYPE.front -> {
							side.printTypes.forEach { printType ->
								printType.elements.forEach { e ->
									when (e) {
										is XMLCardTemplate.Element.Graphic -> {
											printerCard.addView(ImageView(getApplication()).apply {
												val lp = LinearLayout.LayoutParams(e.width, e.height)
												lp.setMargins(e.x, e.y, 0, 0)
												layoutParams = lp
												scaleType = ImageView.ScaleType.CENTER_CROP
												if (!e.reference.isNullOrEmpty())
													viewModelScope.launch {
														this@apply.setImageBitmap(FileHelper.queryImage(getApplication(), zCardTemplate.name, e.reference))
													}
												else
													setImageResource(R.drawable.bg_photo_field)
												if (!e.field.isNullOrEmpty())
													listFieldViews.add(this)
											})
										}
										is XMLCardTemplate.Element.Text -> {
											printerCard.addView(TextView(getApplication()).apply {
												val curFont = template.Fonts[e.font_id]

												File(template.path).also { folder ->
													folder.listFiles { file -> file.name.contains(Regex("""${curFont.name.substring(0..2).toLowerCase(
														Locale.ROOT)}[\w]+.ttf"""))}.also {
														if (it != null)
															typeface = Typeface.createFromFile(it[0])
													}
												}

												if (curFont.bold == XMLCardTemplate.BOOLEAN.yes)
													setTypeface(typeface, Typeface.BOLD)
												if (curFont.italic == XMLCardTemplate.BOOLEAN.yes)
													setTypeface(typeface, Typeface.ITALIC)
												setTextColor(Color.parseColor(e.color))
												textSize = curFont.size.toFloat()
												text = e.field ?: e.data
												layoutParams = LinearLayout.LayoutParams(e.width, e.height).apply {
													setMargins(e.x, e.y, 0, 0)
												}
												gravity = (when (e.alignment) {
													XMLCardTemplate.HALIGNMENT.center -> Gravity.CENTER_HORIZONTAL
													XMLCardTemplate.HALIGNMENT.left -> Gravity.START
													XMLCardTemplate.HALIGNMENT.right -> Gravity.END
												}) or when (e.v_alignment) {
													XMLCardTemplate.VALIGNMENT.center -> Gravity.CENTER_VERTICAL
													XMLCardTemplate.VALIGNMENT.top -> Gravity.TOP
													XMLCardTemplate.VALIGNMENT.bottom -> Gravity.BOTTOM
												}
												if (!e.field.isNullOrEmpty())
													listFieldViews.add(this)
											})
										}
										else -> Log.d("EMBY ERR", "testTemp: UNKNOW ELEMENT")
									}
								}
							}
						}
						XMLCardTemplate.SIDETYPE.back -> {}
					}
				}

				_actions.value = ZCardTemplatePrintAction.TemplateChanged(zCardTemplate, template, listFieldViews)
			}

			if (contentObserver == null)
				contentObserver = getApplication<Application>().contentResolver.registerObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
				{ loadTemplate() }
		}
	}

	fun cameraCapture() {
		if (!EDHelper.hasCameraPermission(getApplication()))
			requestCameraPermission()
		else
			_actions.value = ZCardTemplatePrintAction.CameraCapture
	}

	private fun requestCameraPermission()
		{ _actions.postValue(ZCardTemplatePrintAction.CameraPermissionsRequested) }

	// Adding function to ContentResolver
	private fun ContentResolver.registerObserver(uri: Uri, observer: (selfChange: Boolean) -> Unit): ContentObserver {
		val contentObserver = object : ContentObserver(Handler()) {
			override fun onChange(selfChange: Boolean) {
				observer(selfChange)
			}
		}
		registerContentObserver(uri, true, contentObserver)
		return contentObserver
	}
//	fun testTemp(name: String, template: XMLCardTemplate.Template, printerCard: CardView) {
//		val templateFieldKeys = zebraCardTemplate.getTemplateFields(name)
//
//
//
//		_actions.postValue(ZCardTemplatePrintAction.TemplateViewCreated(listFieldViews))
//	}
}
