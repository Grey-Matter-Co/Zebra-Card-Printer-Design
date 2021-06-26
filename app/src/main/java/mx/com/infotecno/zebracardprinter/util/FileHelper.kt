package mx.com.infotecno.zebracardprinter.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import java.io.FileOutputStream
import java.util.*

object FileHelper {
	suspend fun saveTemplateResources(context: Context, folderName: String, frontpreview: Bitmap, fontFilesList: List<Pair<String, ByteArray>>, imageFilesList: List<Pair<String, Bitmap>>) {
		withContext(Dispatchers.IO) {
			val folder = File(context.filesDir.path+ MainViewModel.TEMPLATEFILEDIRECTORY, folderName)
			if (!folder.exists())
				folder.mkdir()

			try {
				// Salving frontpreview
				FileOutputStream(File(folder, "frontpreview.png")).also {
					frontpreview.compress(Bitmap.CompressFormat.PNG, 100, it)
					it.close()
				}
				// Salving fonts
				fontFilesList.forEach { (fontName, fontData) ->
					FileOutputStream(File(folder, fontName)).also {
						it.write(fontData)
						it.close()
					}
				}
				// Salving images
				imageFilesList.forEach { (imageName, imageData) ->
					FileOutputStream(File(folder, imageName)).also {
						val format = if (imageName.substringAfterLast(".", "PNG").toUpperCase(Locale.ROOT) == "PNG")
							"PNG"
							else "JPEG"
						imageData.compress(Bitmap.CompressFormat.valueOf(format), 100, it)
						it.close()
					}

				}
			}
			catch (e: Exception)
				{ e.printStackTrace() }

		}
	}

	suspend fun queryTemplatesOnDevice(context: Context, zebraCardTemplate: ZebraCardTemplate): List<ZCardTemplate> {
		var templates = mutableListOf<ZCardTemplate>()

		withContext(Dispatchers.IO) {
			templates = (zebraCardTemplate.allTemplateNames.map { name ->
				return@map ZCardTemplate(name).apply {
					val fileDir = "${context.filesDir.path}/${MainViewModel.TEMPLATEFILEDIRECTORY}/$name"
					uri = Uri.fromFile(File( "$fileDir.xml"))
					this.name = name

					if (File(fileDir).exists())
						frontPreview = BitmapFactory.decodeFile("$fileDir/frontpreview.png")
				}
			}).toMutableList()
			templates.add(0, ZCardTemplate(R.drawable.card_add))
		}

		return templates
	}

	@SuppressLint("NewApi") //method only call from API 29 onwards
	suspend fun deleteTemplate(context: Context, zebraCardTemplate: ZebraCardTemplate, template: ZCardTemplate): Boolean {
		var status = true
		withContext(Dispatchers.IO) {
			try {
				zebraCardTemplate.deleteTemplate(template.name)

				val folder = File("${context.filesDir.path}/${MainViewModel.TEMPLATEFILEDIRECTORY}/${template.name}")
				if (folder.exists())    // Delete all extra sources like fonts, images, etc
					status = folder.deleteRecursively()
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