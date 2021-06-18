package mx.com.infotecno.zebracardprinter.model

class ZPrinter(val model: String, val references: String)
{
    companion object
    {
        fun createPrintersList(numPrinters: Int, model: String): ArrayList<ZPrinter>
        {
            val printers = ArrayList<ZPrinter>()
            for (i in 1..numPrinters)
                printers.add(ZPrinter(model, "255.255.255."+i.toShort()))
            return printers
        }
    }
}