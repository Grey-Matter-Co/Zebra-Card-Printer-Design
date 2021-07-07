package mx.com.infotecno.zebracardprinter.action

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import mx.com.infotecno.zebracardprinter.model.ZCardTemplate

sealed class ZCardTemplatesListAction {
    data class TemplatesChanged(val templates: List<ZCardTemplate>): ZCardTemplatesListAction()
    data class TemplatesDeleted(val templates: List<ZCardTemplate>): ZCardTemplatesListAction()
    data class USBPermissionsRequested(val usbManager: UsbManager, val device: UsbDevice): ZCardTemplatesListAction()
    object StoragePermissionsRequested: ZCardTemplatesListAction()
}

