 package mx.com.infotecno.zebracardprinter.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.zebra.sdk.common.card.template.ZebraCardTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.com.infotecno.zebracardprinter.R
import mx.com.infotecno.zebracardprinter.model.ZCardTemplate
import mx.com.infotecno.zebracardprinter.ui.main.MainViewModel
import java.io.File

 object FileHelper {
    suspend fun queryTemplatesOnDevice(context: Context, zebraCardTemplate: ZebraCardTemplate): List<ZCardTemplate> {
        var templates = mutableListOf<ZCardTemplate>()

        withContext(Dispatchers.IO) {
            templates = (zebraCardTemplate.allTemplateNames.map { name ->
                return@map ZCardTemplate(name).apply {
                    val fileDirPath = context.filesDir.path.removePrefix("/")
                    this.uri = Uri.fromFile(File( fileDirPath+ MainViewModel.TEMPLATEFILEDIRECTORY+name+".xml"))
                }
            }).toMutableList()
            templates.add(0, ZCardTemplate(R.drawable.card_add))

        }

        return templates
    }
     
    @SuppressLint("NewApi") //method only call from API 29 onwards
    suspend fun deleteTemplate(zebraCardTemplate: ZebraCardTemplate, template: ZCardTemplate): Boolean {
        var status = true
        withContext(Dispatchers.IO) {
            try {
                zebraCardTemplate.deleteTemplate(template.name)
//                    context.contentResolver.delete(Uri.parse("file://"+ template.uri.path!!.removePrefix("/")), null, null)
            }
            catch (securityException: SecurityException) {
                status = false
                throw securityException
            }
        }

        return status
    }

    @SuppressLint("NewApi") //method only call from API 30 onwards
    fun deleteTemplateBulk(context: Context, media: List<ZCardTemplate>): IntentSender {
        val uris = media.map { it.uri }
        return MediaStore.createDeleteRequest(context.contentResolver, uris).intentSender
    }
}