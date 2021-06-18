package mx.com.infotecno.zebracardprinter.discovery

import android.content.Context
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.comm.TcpConnection
import com.zebra.sdk.common.card.printer.discovery.DiscoveredCardPrinterNetwork
import com.zebra.sdk.common.card.printer.discovery.DiscoveryUtilCard
import com.zebra.sdk.printer.discovery.DiscoveredPrinter
import com.zebra.sdk.settings.SettingsException
import mx.com.infotecno.zebracardprinter.R
import mx.com.infotecno.zebracardprinter.util.ConnectionHelper
import java.lang.ref.WeakReference
import java.util.*

class ManualConnectionTask internal constructor(context: Context, private val ipAddress: String)
{
    private val weakContext: WeakReference<Context> = WeakReference(context)
    private var printer: DiscoveredPrinter? = null
    private var onManualConnectionListener: OnManualConnectionListener? = null
    private var exception: Exception? = null

    interface OnManualConnectionListener {
        fun onManualConnectionStarted()
        fun onManualConnectionFinished(exception: Exception?, printer: DiscoveredPrinter?)
    }

    suspend fun execute()
    {
        if (onManualConnectionListener != null){
            onManualConnectionListener!!.onManualConnectionStarted()
        }
        var connection: TcpConnection? = null
        try
        {
            connection = getTcpConnection(ipAddress)
            connection.open()
            val discoveryDataMap = DiscoveryUtilCard.getDiscoveryDataMap(connection)
            val model = discoveryDataMap["MODEL"]
            printer = if (model != null)
            {
                val discoveredCardPrinterNetwork =
                    if (!model.toLowerCase(Locale.ROOT).contains("zxp1") && !model.toLowerCase(
                            Locale.ROOT).contains("zxp3"))
                        DiscoveredCardPrinterNetwork(discoveryDataMap)
                    else
                        throw ConnectionException(weakContext.get()!!.getString(R.string.printer_model_not_supported))
                discoveredCardPrinterNetwork
            }
            else
                throw SettingsException(weakContext.get()!!.getString(R.string.no_printer_model_found))

        }
        catch (e: Exception)
        { exception = e }
        finally
        { ConnectionHelper.cleanUpQuietly(null, connection) }
        if (onManualConnectionListener != null)
            onManualConnectionListener!!.onManualConnectionFinished(exception, printer)
    }

    private fun getTcpConnection(connectionText: String): TcpConnection {
        val colonIndex = connectionText.indexOf(":")
        return if (colonIndex != -1) {
            val ipAddress = connectionText.substring(0, colonIndex)
            val portNumber = connectionText.substring(colonIndex + 1).toInt()
            TcpConnection(ipAddress, portNumber)
        } else {
            TcpConnection(connectionText, 9100)
        }
    }

    fun setOnManualConnectionListener(onManualConnectionListener: OnManualConnectionListener?) {
        this.onManualConnectionListener = onManualConnectionListener
    }
}
