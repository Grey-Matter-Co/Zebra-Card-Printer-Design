package mx.com.infotecno.zebracardprinter.discovery

import android.hardware.usb.UsbManager
import com.zebra.sdk.common.card.printer.discovery.NetworkCardDiscoverer
import com.zebra.sdk.printer.discovery.*
import java.util.*
import java.util.concurrent.CountDownLatch

class NetworkAndUsbDiscoveryTask internal constructor(private val usbManager: UsbManager) {
    companion object
        { private val INVALID_PRODUCT_ID_LIST = listOf(40, 80) }

    private var onPrinterDiscoveryListener: OnPrinterDiscoveryListener? = null
    private var exception: Exception? = null
    private var isUsbDiscoveryComplete = false
    private var isNetworkDiscoveryComplete = false
    private val discoveryCompleteLatch = CountDownLatch(2)

    suspend fun execute() {
        onPrinterDiscoveryListener!!.onPrinterDiscoveryStarted()
        try {
            UsbDiscoverer.findPrinters(usbManager, object : DiscoveryHandler {
                override fun foundPrinter(discoveredPrinter: DiscoveredPrinter) {
                    if (!INVALID_PRODUCT_ID_LIST.contains((discoveredPrinter as DiscoveredPrinterUsb).device.productId))
                        if (onPrinterDiscoveryListener != null)
                            onPrinterDiscoveryListener!!.onPrinterDiscovered(discoveredPrinter)
                }
                override fun discoveryFinished()
                    { onUsbDiscoveryComplete() }
                override fun discoveryError(s: String)
                    { onUsbDiscoveryComplete() }
            })
            try {
                NetworkCardDiscoverer.findPrinters(object : DiscoveryHandler {
                    override fun foundPrinter(discoveredPrinter: DiscoveredPrinter) {
                        if (!discoveredPrinter.discoveryDataMap["MODEL"]!!.toLowerCase(Locale.ROOT).contains("zxp1")
                            && !discoveredPrinter.discoveryDataMap["MODEL"]!!.toLowerCase(Locale.ROOT).contains("zxp3"))
                            if (onPrinterDiscoveryListener != null)
                                onPrinterDiscoveryListener!!.onPrinterDiscovered(discoveredPrinter)
                    }
                    override fun discoveryFinished()
                        { onNetworkDiscoveryComplete() }
                    override fun discoveryError(s: String)
                        { onNetworkDiscoveryComplete() }
                })
            }
            catch (e: DiscoveryException)
                { onNetworkDiscoveryComplete() }
            discoveryCompleteLatch.await()
        }
        catch (e: Exception)
            { exception = e }
        onPrinterDiscoveryListener!!.onPrinterDiscoveryFinished(exception)
    }

    private fun onUsbDiscoveryComplete() {
        if (!isUsbDiscoveryComplete) {
            isUsbDiscoveryComplete = true
            discoveryCompleteLatch.countDown()
        }
    }

    private fun onNetworkDiscoveryComplete() {
        if (!isNetworkDiscoveryComplete) {
            isNetworkDiscoveryComplete = true
            discoveryCompleteLatch.countDown()
        }
    }

    fun setOnPrinterDiscoveryListener(onPrinterDiscoveryListener: OnPrinterDiscoveryListener?)
        { this.onPrinterDiscoveryListener = onPrinterDiscoveryListener }

    interface OnPrinterDiscoveryListener {
        fun onPrinterDiscoveryStarted()
        fun onPrinterDiscovered(printer: DiscoveredPrinter?)
        fun onPrinterDiscoveryFinished(exception: Exception?)
    }
}
