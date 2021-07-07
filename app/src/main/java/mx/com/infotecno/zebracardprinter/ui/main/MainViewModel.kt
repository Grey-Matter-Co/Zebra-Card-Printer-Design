package mx.com.infotecno.zebracardprinter.ui.main

import android.app.Activity
import android.app.Application
import android.content.ContentResolver
import android.database.ContentObserver
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zebra.sdk.common.card.exceptions.ZebraCardException
import com.zebra.sdk.common.card.template.ZebraCardTemplate
import kotlinx.coroutines.launch
import mx.com.infotecno.zebracardprinter.MainActivity.Companion.TEMPLATEFILEDIRECTORY
import mx.com.infotecno.zebracardprinter.MainActivity.Companion.TEMPLATEIMAGEFILEDIRECTORY
import mx.com.infotecno.zebracardprinter.action.ZCardTemplatesListAction
import mx.com.infotecno.zebracardprinter.model.ZCardTemplate
import mx.com.infotecno.zebracardprinter.util.DialogHelper
import mx.com.infotecno.zebracardprinter.util.FileHelper
import mx.com.infotecno.zebracardprinter.util.UriHelper
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.IOException
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _actions = MutableLiveData<ZCardTemplatesListAction>()    // This will be operated and changed
    val action: LiveData<ZCardTemplatesListAction> get() = _actions      // This only returns unmodified list

    private var contentObserver: ContentObserver? = null

    private var zebraCardTemplate: ZebraCardTemplate = ZebraCardTemplate(getApplication(), null).apply {
        Log.d("EMBY", "zebraCardTemplate: ${getApplication<Application>().filesDir.path+ File.separator + TEMPLATEFILEDIRECTORY}")
        this.setTemplateFileDirectory(getApplication<Application>().filesDir.path+ File.separator + TEMPLATEFILEDIRECTORY)
        this.setTemplateImageFileDirectory(getApplication<Application>().filesDir.path+ File.separator + TEMPLATEIMAGEFILEDIRECTORY)
    }

    fun loadTemplates(newTemplates: List<String>) {
        viewModelScope.launch {
            // Query all saved templates
            var templateList = FileHelper.queryTemplatesOnDevice(getApplication<Application>(), zebraCardTemplate, newTemplates)
            _actions.postValue(ZCardTemplatesListAction.TemplatesChanged(templateList))

            if (contentObserver == null)
                contentObserver = getApplication<Application>().contentResolver.registerObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    { loadTemplates(emptyList()) }
            else if (newTemplates.isNotEmpty())
                Timer("waiting for writting", false).schedule(object : TimerTask() {
                    override fun run() {
                        viewModelScope.launch {
                            templateList = FileHelper.queryTemplatesOnDevice(getApplication<Application>(), zebraCardTemplate, emptyList())
                            _actions.postValue(ZCardTemplatesListAction.TemplatesChanged(templateList))
                        }
                    }
                }, 1000)
        }
    }

    fun saveTemplate(activity: Activity, uri: Uri) {
        try {
            val templateName = FilenameUtils.removeExtension(UriHelper.getFilename(getApplication<Application>(), uri)!!)
            val storedTemplateNames: List<String> = zebraCardTemplate.allTemplateNames

            if (storedTemplateNames.contains(templateName))
                zebraCardTemplate.deleteTemplate(templateName)
            zebraCardTemplate.saveTemplate(templateName, getTemplateDataString(uri))
        }
        catch (e: Exception) {
            val errorMessage = if (e is ZebraCardException) "invalid template file selected"
            else e.message!!
            DialogHelper.showErrorDialog(activity, "error_selecting_template_message: $errorMessage")
        }
    }

    fun saveTemplate(activity: Activity, templateName: String, xmlTemplate: String, jsonData: String, frontpreview: Bitmap, fontFilesList: List<Pair<String, ByteArray>>, imageFilesList: List<Pair<String, Bitmap>>) {
        val storedTemplateNames: List<String> = zebraCardTemplate.allTemplateNames
        viewModelScope.launch {
            try {
                //val templateName = FilenameUtils.removeExtension(UriHelper.getFilename(getApplication<Application>(), uri)!!)

                if (storedTemplateNames.contains(templateName))
                    zebraCardTemplate.deleteTemplate(templateName)
                zebraCardTemplate.saveTemplate(templateName, xmlTemplate)
                FileHelper.saveTemplateResources(getApplication<Application>(), templateName, jsonData, frontpreview, fontFilesList, imageFilesList)
            }
            catch (e: Exception) {
                val errorMessage = if (e is ZebraCardException) "invalid template file selected"
                else e.message!!
                DialogHelper.showErrorDialog(activity, "error_selecting_template_message: $errorMessage")
            }
        }
    }

    fun alreadyExistsTemplate(templateName: String) =
        zebraCardTemplate.allTemplateNames.contains(templateName)

    fun requestStoragePermissions()
        = _actions.postValue(ZCardTemplatesListAction.StoragePermissionsRequested)

    fun requestUSBPermission(usbManager: UsbManager, device: UsbDevice)
        = _actions.postValue(ZCardTemplatesListAction.USBPermissionsRequested(usbManager, device))

    fun deleteTemplates(templates: List<ZCardTemplate>) {
        viewModelScope.launch {
            for (template in templates) {
                val completed = FileHelper.deleteTemplate(getApplication<Application>(), zebraCardTemplate, template)
                if (completed)
                    _actions.postValue(ZCardTemplatesListAction.TemplatesDeleted(FileHelper.queryTemplatesOnDevice(getApplication<Application>(), zebraCardTemplate, emptyList())))
            }
        }
    }

    @Throws(IOException::class)
    private fun getTemplateDataString(uri: Uri): String? {
        val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
        if (inputStream != null)
            return IOUtils.toString(inputStream)
        throw IOException("could not open input stream for uri")
    }

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
