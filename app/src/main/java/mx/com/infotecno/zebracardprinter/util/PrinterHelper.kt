package mx.com.infotecno.zebracardprinter.util

import android.content.Context
import com.zebra.sdk.comm.ConnectionException
import com.zebra.sdk.common.card.exceptions.ZebraCardException
import com.zebra.sdk.common.card.printer.ZebraCardPrinter
import com.zebra.sdk.settings.SettingsException
import mx.com.infotecno.zebracardprinter.R

object PrinterHelper {
	@Throws(ConnectionException::class, SettingsException::class, ZebraCardException::class)
	fun isPrinterReady(context: Context, zebraCardPrinter: ZebraCardPrinter, onPrinterReadyListener: OnPrinterReadyListener?): Boolean {
		val statusInfo = zebraCardPrinter.printerStatus
		onPrinterReadyListener?.onPrinterReadyUpdate(context.getString(R.string.checking_printer_status), false)
		if (statusInfo.errorInfo.value > 0) {
			onPrinterReadyListener?.onPrinterReadyUpdate(context.getString(R.string.printer_not_ready_message, statusInfo.status, statusInfo.errorInfo.description), true)
			return false
		}
		else if (statusInfo.alarmInfo.value > 0) {
			onPrinterReadyListener?.onPrinterReadyUpdate(context.getString(R.string.printer_not_ready_message, statusInfo.status, statusInfo.alarmInfo.description), true)
			return false
		}
		return true
	}

	interface OnPrinterReadyListener {
		fun onPrinterReadyUpdate(message: String?, showDialog: Boolean)
	}
}
