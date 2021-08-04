package mx.com.infotecno.zebracardprinter.print

import android.content.Context
import com.zebra.sdk.comm.Connection
import com.zebra.sdk.common.card.enumerations.CardDestination
import com.zebra.sdk.common.card.enumerations.CardSource
import com.zebra.sdk.common.card.exceptions.ZebraCardException
import com.zebra.sdk.common.card.printer.ZebraCardPrinter
import com.zebra.sdk.common.card.printer.ZebraCardPrinterFactory
import com.zebra.sdk.common.card.template.ZebraCardTemplate
import com.zebra.sdk.printer.discovery.DiscoveredPrinter
import mx.com.infotecno.zebracardprinter.R
import mx.com.infotecno.zebracardprinter.util.ConnectionHelper
import mx.com.infotecno.zebracardprinter.util.PrinterHelper
import java.lang.ref.WeakReference

class SendTemplateJobTask(context: Context, private var printer: DiscoveredPrinter?, private var zebraCardTemplate: ZebraCardTemplate?, private var templateName: String?, private var variableData: Map<String, String>?) {
	private var weakContext: WeakReference<Context>? = null
	private var onSendTemplateJobListener: OnSendTemplateJobListener? = null
	private var exception: Exception? = null
	private var cardSource: CardSource? = null

	interface OnSendTemplateJobListener : PrinterHelper.OnPrinterReadyListener {
		fun onSendTemplateJobStarted()
		fun onSendTemplateJobFinished(exception: Exception?, jobId: Int?, cardSource: CardSource?)
	}

	init
		{ weakContext = WeakReference(context) }

	suspend fun execute() {
		if (onSendTemplateJobListener != null)
			onSendTemplateJobListener!!.onSendTemplateJobStarted()

		var zebraCardPrinter: ZebraCardPrinter? = null
		var connection: Connection? = null
		var jobId: Int? = null

		try {
			connection = printer!!.connection
			connection.open()
			zebraCardPrinter = ZebraCardPrinterFactory.getInstance(connection)
			if (PrinterHelper.isPrinterReady(weakContext!!.get()!!, zebraCardPrinter, onSendTemplateJobListener)) {
				val templateJob = zebraCardTemplate!!.generateTemplateJob(templateName, variableData)
				if (templateJob.jobInfo.cardDestination != null) {
					if (templateJob.jobInfo.cardDestination == CardDestination.Eject && zebraCardPrinter.hasLaminator()) {
						templateJob.jobInfo.cardDestination = CardDestination.LaminatorAny
					}
				}
				cardSource = templateJob.jobInfo.cardSource
				if (templateJob.magInfo != null && templateJob.magData != null) {
					val hasTrack1Data = templateJob.magData.track1Data != null && templateJob.magData.track1Data.isNotEmpty()
					val hasTrack2Data = templateJob.magData.track2Data != null && templateJob.magData.track2Data.isNotEmpty()
					val hasTrack3Data = templateJob.magData.track3Data != null && templateJob.magData.track3Data.isNotEmpty()
					require(!(templateJob.magInfo.verify && !hasTrack1Data && !hasTrack2Data && !hasTrack3Data)) {
						weakContext!!.get()!!.getString(R.string.no_magnetic_track_data_to_encode)
					}
				}
				jobId = zebraCardPrinter.printTemplate(1, templateJob)
			}
		}
		catch (e: ZebraCardException)
			{ exception = ZebraCardException(weakContext!!.get()!!.getString(R.string.invalid_template_file_selected), e) }
		catch (e: Exception)
			{ exception = e }
		finally {
			ConnectionHelper.cleanUpQuietly(zebraCardPrinter, connection)
			if (onSendTemplateJobListener != null)
				onSendTemplateJobListener!!.onSendTemplateJobFinished(exception, jobId, cardSource)
		}
	}

	fun setOnSendTemplateJobListener(onSendTemplateJobListener: OnSendTemplateJobListener?) {
		this.onSendTemplateJobListener = onSendTemplateJobListener
	}
}