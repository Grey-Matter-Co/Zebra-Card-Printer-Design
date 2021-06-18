package mx.com.infotecno.zebracardprinter.action

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import mx.com.infotecno.zebracardprinter.model.ZCardTemplate

sealed class ZCardTemplateAction {
    data class TemplatesChanged(val templates: List<ZCardTemplate>): ZCardTemplateAction()
    data class TemplatesDeleted(val templates: List<ZCardTemplate>): ZCardTemplateAction()
    data class USBPermissionsRequested(val usbManager: UsbManager, val device: UsbDevice): ZCardTemplateAction()
    object StoragePermissionsRequested: ZCardTemplateAction()
}

