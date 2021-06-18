package mx.com.infotecno.zebracardprinter.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

object UsbHelper {
    const val ACTION_USB_PERMISSION_GRANTED = "mx.com.infotecno.zebracardprinter.USB_PERMISSION_GRANTED"

    fun getUsbManager(context: Context): UsbManager {
        return context.getSystemService(Context.USB_SERVICE) as UsbManager
    }

    fun requestUsbPermission(context: Context?, manager: UsbManager, device: UsbDevice?)
    {
        val permissionIntent = PendingIntent.getBroadcast(context, 0, Intent(ACTION_USB_PERMISSION_GRANTED), 0)
        manager.requestPermission(device, permissionIntent)
    }
}
