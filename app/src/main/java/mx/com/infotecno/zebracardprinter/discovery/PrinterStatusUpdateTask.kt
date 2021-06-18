package mx.com.infotecno.zebracardprinter.discovery

import android.content.Context
import android.util.Log
import com.zebra.sdk.comm.Connection
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.common.card.printer.ZebraCardPrinter
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory
import com.zebra.sdk.printer.discovery.DiscoveredPrinter
import com.zebra.zebraui.ZebraPrinterView
import mx.com.infotecno.zebracardprinter.R
import mx.com.infotecno.zebracardprinter.util.ConnectionHelper
import java.lang.ref.WeakReference

class PrinterStatusUpdateTask(context: Context, private val printer: DiscoveredPrinter)
{
    private var weakContext = WeakReference(context)
    private var onUpdatePrinterStatusListener: OnUpdatePrinterStatusListener? = null
    private var exception: java.lang.Exception? = null

    suspend fun execute()
    {
        onUpdatePrinterStatusListener?.onUpdatePrinterStatusStarted()

        var connection: Connection? = null
        var zebraCardPrinter: ZebraCardPrinter? = null
        var zebraViewerStatus :ZebraPrinterView.PrinterStatus = ZebraPrinterView.PrinterStatus.UNKNOWN

        try
        {
            connection = printer.connection
            connection.open()
            zebraCardPrinter = ZebraCardPrinterFactory.getInstance(connection)
            val printerStatus = zebraCardPrinter.printerStatus
            zebraViewerStatus = if (printerStatus != null)
            {
                if (printerStatus.errorInfo.value != 0 || printerStatus.alarmInfo.value != 0)
                    ZebraPrinterView.PrinterStatus.ERROR
                else
                    ZebraPrinterView.PrinterStatus.ONLINE
            }
            else
                ZebraPrinterView.PrinterStatus.ERROR
        } catch (e: ConnectionException) {

            exception = ConnectionException(weakContext.get()!!.getString(R.string.msg_unable_to_communicate_with_printer))
            zebraViewerStatus = ZebraPrinterView.PrinterStatus.ERROR
        } catch (e: Exception) {
            exception = e
            zebraViewerStatus = ZebraPrinterView.PrinterStatus.ERROR
        } finally {
            ConnectionHelper.cleanUpQuietly(zebraCardPrinter, connection)
            onUpdatePrinterStatusListener?.onUpdatePrinterStatusFinished(exception, zebraViewerStatus)
        }

    }

    fun setOnUpdatePrinterStatusListener(onUpdatePrinterStatusListener: OnUpdatePrinterStatusListener?)
    { this.onUpdatePrinterStatusListener = onUpdatePrinterStatusListener }

    interface OnUpdatePrinterStatusListener
    {
        fun onUpdatePrinterStatusStarted()
        fun onUpdatePrinterStatusFinished(exception: java.lang.Exception?, printerStatus: ZebraPrinterView.PrinterStatus)
    }
}
