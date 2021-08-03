package mx.com.infotecno.zebracardprinter.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zebra.sdk.common.card.template.ZebraCardTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.com.infotecno.zebracardprinter.MainActivity.Companion.TEMPLATEFILEDIRECTORY
import mx.com.infotecno.zebracardprinter.R
import mx.com.infotecno.zebracardprinter.model.XMLCardTemplate
import mx.com.infotecno.zebracardprinter.model.ZCardTemplate
import java.io.*
import java.util.*

object FileHelper {
	suspend fun saveTemplateResources(context: Context, folderName: String, jsonData: String, frontpreview: Bitmap, fontFilesList: List<Pair<String, ByteArray>>, imageFilesList: List<Pair<String, Bitmap>>) {
		withContext(Dispatchers.IO) {
			val folder = File(context.filesDir.path+ File.separator+ TEMPLATEFILEDIRECTORY, folderName)
			if (!folder.exists())
				folder.mkdir()

			try {
				// Salving jsonData
				FileOutputStream(File(folder, "datafields.json")).also {
					it.write(jsonData.toByteArray(Charsets.UTF_8))
					it.close()
				}

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

	suspend fun queryTemplate(context: Context, templateName: String): XMLCardTemplate.Template {
		return withContext(Dispatchers.IO) {
			val fileDir = "${context.filesDir.path}/$TEMPLATEFILEDIRECTORY/$templateName"
			XMLDecoder.parseTemplate(File("$fileDir.xml").inputStream()).apply {
				this.path = "$fileDir"
			}
		}
	}

	suspend fun queryImage(context: Context, templateName: String, imageName: String): Bitmap {
		return withContext(Dispatchers.IO) {
			val fileDir = "${context.filesDir.path}/$TEMPLATEFILEDIRECTORY/$templateName/$imageName"
			BitmapFactory.decodeFile(fileDir)
		}
	}

	suspend fun queryTemplatesOnDevice(context: Context, zebraCardTemplate: ZebraCardTemplate, newTemplates: List<String>): List<ZCardTemplate> {
		var templates = mutableListOf<ZCardTemplate>()

		withContext(Dispatchers.IO) {
			templates = (zebraCardTemplate.allTemplateNames.mapIndexed { idx, name ->
				val fileDir = "${context.filesDir.path}/$TEMPLATEFILEDIRECTORY/$name"

				return@mapIndexed ZCardTemplate((idx+1).toLong(), name, Uri.fromFile(File("$fileDir.xml"))).apply {
					if (File(fileDir).exists()) {
						if (newTemplates.contains(name))
							idBackground = R.drawable.card_holder
						else
							frontPreview = BitmapFactory.decodeFile("${fileDir}/frontpreview.png")
						val json = File("${fileDir}/datafields.json").inputStream().readBytes().toString(Charsets.UTF_8)
						fields = Gson().fromJson(json, object : TypeToken<Map<String, Any>>() {}.type)
					}
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

				val folder = File("${context.filesDir.path}/$TEMPLATEFILEDIRECTORY/${template.name}")
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
		val uris = media.map { it.templateUri }
		return MediaStore.createDeleteRequest(context.contentResolver, uris).intentSender
	}
}