package mx.com.infotecno.zebracardprinter.ui.printerdiscover

import android.app.Application
import android.content.ContentResolver
import android.database.ContentObserver
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zebra.sdk.printer.discovery.DiscoveredPrinter
import kotlinx.coroutines.launch
import mx.com.infotecno.zebracardprinter.action.DiscoveredPrinterAction
import mx.com.infotecno.zebracardprinter.adapter.DiscoveredPrinterAdapter

class PrinterDiscoverViewModel(application: Application) : AndroidViewModel(application) {
    private val _actions = MutableLiveData<DiscoveredPrinterAction>()    // This will be operated and changed
    val actions: LiveData<DiscoveredPrinterAction> get() = _actions      // This only returns unmodified list

    private var contentObserver: ContentObserver? = null
    var isApplicationBusy = false

    fun startDiscover() {
        _actions.postValue(DiscoveredPrinterAction.DiscoveredPrintersStarted)
        if (contentObserver == null)
            contentObserver = getApplication<Application>().contentResolver.registerObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                { startDiscover() }
    }

    /*	NetworkAndUsbDiscoveryTask.OnPrinterDiscoveryListener Implements	*/
    fun onPrinterDiscovered(printer: DiscoveredPrinter?, adapter: DiscoveredPrinterAdapter) {
        viewModelScope.launch {
            val newList = adapter.currentList.toMutableList()
            newList.add(printer)
            _actions.value = DiscoveredPrinterAction.DiscoveredPrintersChanged(newList)
        }
    }
    /*	END NetworkAndUsbDiscoveryTask.OnPrinterDiscoveryListener Implements	*/

    fun usbPrinterDisconnected(currentList: MutableList<DiscoveredPrinter>, disconectedUSBPrinter: DiscoveredPrinter) {
        viewModelScope.launch {
            val newDiscoveredPrinterList = currentList
                .filter { it.address != disconectedUSBPrinter.discoveryDataMap["ADDRESS"] }

            _actions.postValue(DiscoveredPrinterAction.DiscoveredPrintersChanged(newDiscoveredPrinterList))
        }
    }

    fun requestUSBPermission(usbManager: UsbManager, device: UsbDevice)
            = _actions.postValue(DiscoveredPrinterAction.USBPermissionsRequested(usbManager, device))

    // Adding function to ContentResolver
    private fun ContentResolver.registerObserver(uri: Uri, observer: (selfChange: Boolean) -> Unit): ContentObserver {
        val contentObserver = object : ContentObserver(Handler()) {
            override fun onChange(selfChange: Boolean)
                = observer(selfChange)
        }
        registerContentObserver(uri, true, contentObserver)
        return contentObserver
    }
}