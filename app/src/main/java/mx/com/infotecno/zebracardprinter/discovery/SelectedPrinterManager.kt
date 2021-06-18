package mx.com.infotecno.zebracardprinter.discovery

import com.zebra.sdk.printer.discovery.DiscoveredPrinter

object SelectedPrinterManager {
    private var selectedPrinter: DiscoveredPrinter? = null

    fun getSelectedPrinter(): DiscoveredPrinter?
        = selectedPrinter

    fun setSelectedPrinter(printer: DiscoveredPrinter?)
        { selectedPrinter = printer }
}
