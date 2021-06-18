    package mx.com.infotecno.zebracardprinter.action

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.zebra.sdk.printer.discovery.DiscoveredPrinter

sealed class DiscoveredPrinterAction {
    object DiscoveredPrintersStarted: DiscoveredPrinterAction()
    data class DiscoveredPrintersChanged(val discoveredPrinters: List<DiscoveredPrinter>) : DiscoveredPrinterAction()
    data class USBPermissionsRequested(val usbManager: UsbManager, val device: UsbDevice) : DiscoveredPrinterAction()
}
