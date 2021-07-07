package mx.com.infotecno.zebracardprinter.ui.printtemplate

import android.app.Application
import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import mx.com.infotecno.zebracardprinter.action.ZCardTemplatePrintAction
import mx.com.infotecno.zebracardprinter.model.ZCardTemplate
import java.io.File
import mx.com.infotecno.zebracardprinter.util.ExecutingDevicesHelper as EDHelper

class PrintTemplateViewModel(application: Application) : AndroidViewModel(application) {

	private val _actions = MutableLiveData<ZCardTemplatePrintAction>()    // This will be operated and changed
	val action: LiveData<ZCardTemplatePrintAction> get() = _actions      // This only returns unmodified list

	private var contentObserver: ContentObserver? = null


//	private val _actions = mutableMapOf<String, Any>()    // This will be operated and changed
//	val action: Map<String, Any> get() = this._actions      // This only returns unmodified list
//	val zebraCardTemplate: ZebraCardTemplate = ZebraCardTemplate(getApplication(), null).apply {
//		this.setTemplateFileDirectory(getApplication<Application>().filesDir.path+ File.separator + MainActivity.TEMPLATEFILEDIRECTORY)
//		this.setTemplateImageFileDirectory(getApplication<Application>().filesDir.path+ File.separator + MainActivity.TEMPLATEIMAGEFILEDIRECTORY)
//	}

	fun loadTemplate(zCardTemplate: ZCardTemplate? = null) {
		_actions.value = ZCardTemplatePrintAction.TemplateChanged(zCardTemplate!!)

		if (contentObserver == null)
			contentObserver = getApplication<Application>().contentResolver.registerObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
			{ loadTemplate() }
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
}
