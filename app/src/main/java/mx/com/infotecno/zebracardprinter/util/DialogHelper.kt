package mx.com.infotecno.zebracardprinter.util

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import mx.com.infotecno.zebracardprinter.R

object DialogHelper {
//    Importing Template Section
    fun createAddTemplateDialog(context: Context, cb: (Int) -> Unit)
    {
        val items = arrayOf("Archivo Existente", "Diseño De Plantilla Nuevo")
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.adding_template_tittle))
            .setItems(items) { _, which ->
                cb(which)
            }
            .show()
    }
//    Printer Connection Section
    fun createManuallyConnectDialog(context: Context?, onPositiveButtonClickListener: DialogInterface.OnClickListener?): AlertDialog
            = AlertDialog.Builder(context!!).setTitle(R.string.dialog_title_manually_connect)
        .setView(R.layout.dialog_manually_connect)
        .setPositiveButton(R.string.connect, onPositiveButtonClickListener)
        .setNegativeButton(android.R.string.cancel)
        { dialog, _ -> dialog.dismiss() }
        .create()
//    Printer Disconnection Section
    fun createDisconnectDialog(context: Context?, onPositiveButtonClickListener: (Any, Any) -> Unit): AlertDialog
            = AlertDialog.Builder(context!!).setTitle(R.string.dialog_title_disconnect_printer)
        .setMessage(R.string.dialog_message_disconnect_printer)
        .setPositiveButton(R.string.disconnect, onPositiveButtonClickListener)
        .setNegativeButton(android.R.string.cancel)
        { dialog, _ -> dialog.dismiss() }
        .create()

//    General Error Dialog Section
    fun showErrorDialog(activity: Activity, message: String?) {
        showErrorDialog(activity, "Error", message)
    }

    fun showErrorDialog(activity: Activity, title: String?, message: String?) {
        activity.runOnUiThread {
            val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
            val dialog: AlertDialog = builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok )
                    { dialog, _ -> dialog.dismiss() }
                .setCancelable(false)
                .create()
            dialog.show()
        }
    }
}
