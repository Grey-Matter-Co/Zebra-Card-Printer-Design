package mx.com.infotecno.zebracardprinter.ui.printtemplate

import android.R.attr.bitmap
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import mx.com.infotecno.zebracardprinter.R
import mx.com.infotecno.zebracardprinter.action.ZCardTemplatePrintAction
import mx.com.infotecno.zebracardprinter.databinding.PrinttemplateFragmentBinding
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import mx.com.infotecno.zebracardprinter.util.ExecutingDevicesHelper as EDHelper


class PrintTemplateFragment : Fragment() {

	companion object {
		private const val REQUEST_CAMERA = 3003
		private const val REQUEST_PERMISSION_CAMERA = 200
	}

	private val args: PrintTemplateFragmentArgs by navArgs()

	private val viewModel: PrintTemplateViewModel by viewModels()

	private var permissionDenied = false

	private lateinit var btn: AppCompatButton
	private lateinit var binding: PrinttemplateFragmentBinding
	private lateinit var fields: MutableMap<String, Any>

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = PrinttemplateFragmentBinding.inflate(inflater)
		viewModel.action.observe(viewLifecycleOwner, { action -> handleAction(action) })
		return binding.root
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)


//		setupUiComponents()
	}

	override fun onResume() {
		super.onResume()
		if (!this::fields.isInitialized) {
			viewModel.loadTemplate(args.template)
			fields = args.template.fields.toMutableMap()
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
			val photo = data!!.extras!!["data"] as Bitmap?
//			imageView.setImageBitmap(photo)
			Snackbar.make(binding.root, "GOT IMAGE! $photo", Snackbar.LENGTH_SHORT).show()


			val bos = ByteArrayOutputStream()
			photo!!.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
			val bitmapdata: ByteArray = bos.toByteArray()
			val bs = ByteArrayInputStream(bitmapdata)

			btn.background = Drawable.createFromStream(bs, "photo")

//			val curPaddingV = it.paddingTop
//			val curPaddingH = it.paddingLeft
//			val colorGreen = Color.argb(242,139,195,74)
//			val bgDraw = ContextCompat.getDrawable(context, R.drawable.bg_add_photo_ready)!!
//			DrawableCompat.setTint(bgDraw, colorGreen)
//			it.background = bgDraw
//			it.setTextColor(colorGreen)
//			it.setPadding(curPaddingH, curPaddingV, curPaddingH, curPaddingV)
		}
	}

	override fun onRequestPermissionsResult(code: Int, permission: Array<out String>, res: IntArray) {
		when (code) {
			REQUEST_PERMISSION_CAMERA -> {
				when {
					res.isEmpty() -> { /* Do nothing, app is resuming */ }
					res[0] == PackageManager.PERMISSION_GRANTED ->
						viewModel.cameraCapture()
					else -> {
						permissionDenied = true
						Snackbar.make(binding.root, R.string.missing_permission, Snackbar.LENGTH_SHORT ).show()
					}
				}
			}
		}
	}

	/*	Handle changes on data	*/
	private fun handleAction(action: ZCardTemplatePrintAction) {
		when (action) {
			is ZCardTemplatePrintAction.TemplateChanged -> { //Template Added
				action.zCardTemplate.fields.forEach { (key, _) ->
					binding.fieldsContainerLlyt.addView(addTemplateFieldView(key))
				}
			}

			is ZCardTemplatePrintAction.CameraCapture -> // Opening Camera
				startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CAMERA)

			is ZCardTemplatePrintAction.CameraPermissionsRequested -> EDHelper.requestCameraPermission(this, REQUEST_PERMISSION_CAMERA)
		}
	}

	private fun setupUiComponents() {}

	private fun addTemplateFieldView(fieldName: String): View {
		if (fieldName.contains("photo")) {
			return (layoutInflater.inflate( R.layout.item_template_photo_field, binding.printTemplate, false) as AppCompatButton).apply {
				text = fieldName
				setOnClickListener {
					btn = it as AppCompatButton
					viewModel.cameraCapture()
				}
			}
		}
		else {
			return (layoutInflater.inflate(R.layout.item_template_text_field, binding.printTemplate, false) as TextInputLayout).apply {
				hint = fieldName
				findViewById<TextInputEditText>(R.id.inputText).addTextChangedListener(object : TextWatcher {
					override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
					override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
					override fun afterTextChanged(editable: Editable)
						{ fields[fieldName] = editable.toString() }
				})
			}
		}
	}
}