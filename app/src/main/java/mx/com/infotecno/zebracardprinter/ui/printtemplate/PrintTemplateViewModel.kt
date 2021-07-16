package mx.com.infotecno.zebracardprinter.ui.printtemplate

import android.app.Application
import android.content.ClipData.Item
import android.content.ContentResolver
import android.database.ContentObserver
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zebra.sdk.common.card.template.ZebraCardTemplate
import kotlinx.coroutines.launch
import mx.com.infotecno.zebracardprinter.MainActivity
import mx.com.infotecno.zebracardprinter.action.ZCardTemplatePrintAction
import mx.com.infotecno.zebracardprinter.model.ZCardTemplate
import mx.com.infotecno.zebracardprinter.util.FileHelper
import java.io.File
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

	fun loadTemplate(zCardTemplate: ZCardTemplate? = null) {
		viewModelScope.launch {
			if (zCardTemplate != null)
				_actions.value = ZCardTemplatePrintAction.TemplateChanged(zCardTemplate,
					FileHelper.queryTemplate(getApplication<Application>(), zCardTemplate.name)
				)

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

	fun requestCameraPermission()
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

	fun testTemp(name: String, fields: Map<String, Any>) {
		val templateFieldKeys = zebraCardTemplate.getTemplateFields(name)


		var list: List<Item>
		val map: MutableMap<String, String> = HashMap()
		for (i in templateFieldKeys) map[i] = ""

		zebraCardTemplate.getTemplateFields(name).forEach {
			Log.d("EMBY", "testTemp var: $it")
		}

		val graphicsData = zebraCardTemplate.generateTemplateJob(name, map).graphicsData
		for (info in graphicsData) {
			if (info.graphicData != null) {
				val imageData = info.graphicData.imageData
				val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
				templatePreviewList.add(
					TemplatePreview(
						weakContext.get().getString(R.string.card_side_and_type, info.side, info.printType),
						bitmap
					)
				)
				bitmap.recycle()
			} else {
				templatePreviewList.add(
					TemplatePreview(
						weakContext.get().getString(R.string.card_side_and_type, info.side, info.printType),
						weakContext.get().getString(R.string.no_image_data_found)
					)
				)
			}
		}



	}
}
