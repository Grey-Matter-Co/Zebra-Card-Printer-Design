package mx.com.infotecno.zebracardprinter.ui.printtemplate

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.zebra.sdk.common.card.template.ZebraCardTemplate
import com.zebra.sdk.printer.discovery.DiscoveredPrinter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mx.com.infotecno.zebracardprinter.R
import mx.com.infotecno.zebracardprinter.action.ZCardTemplatePrintAction
import mx.com.infotecno.zebracardprinter.databinding.PrinttemplateFragmentBinding
import mx.com.infotecno.zebracardprinter.discovery.SelectedPrinterManager
import mx.com.infotecno.zebracardprinter.print.SendTemplateJobTask
import mx.com.infotecno.zebracardprinter.util.AsyncTaskHelper
import mx.com.infotecno.zebracardprinter.util.UIHelper
import mx.com.infotecno.zebracardprinter.util.XMLMapper
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.coroutines.CoroutineContext
import mx.com.infotecno.zebracardprinter.util.ExecutingDevicesHelper as EDHelper


class PrintTemplateFragment : Fragment(), CoroutineScope {

	companion object {
		private const val REQUEST_CAMERA = 3003
		private const val REQUEST_PERMISSION_CAMERA = 200
	}

	private val args: PrintTemplateFragmentArgs by navArgs()

	private val viewModel: PrintTemplateViewModel by viewModels()

	private val zebraCardTemplate = ZebraCardTemplate(requireContext(), null)
	private var printer: DiscoveredPrinter? = null

	private var permissionDenied = false

	private lateinit var btn: AppCompatButton
	private lateinit var binding: PrinttemplateFragmentBinding
	private lateinit var fields: MutableMap<String, Any>
	private lateinit var mapFieldsViews: Map<String, View>
	private var cameraField: String? = null

	//For Coroutines
	private var jobSendTemplate : Job = Job()
	private var sendTemplateJobTask: SendTemplateJobTask? = null
	override val coroutineContext: CoroutineContext
		get() = Job() + Dispatchers.IO

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = PrinttemplateFragmentBinding.inflate(inflater)
		printer = SelectedPrinterManager.getSelectedPrinter()
		viewModel.action.observe(viewLifecycleOwner, { action -> handleAction(action) })
		return binding.root
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		setupUiComponents()
	}

	override fun onResume() {
		super.onResume()
		if (!this::fields.isInitialized) {
			viewModel.loadTemplate(args.template, binding.printerCard)
			fields = args.template.fields.toMutableMap()
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		jobSendTemplate.cancel()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
			val photo = data!!.extras!!["data"] as Bitmap?
			val bos = ByteArrayOutputStream()
			photo!!.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos)

			mapFieldsViews[cameraField]!!.background = Drawable.createFromStream(ByteArrayInputStream(bos.toByteArray()), cameraField)

			btn.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_add_photo_ready)

		}
		cameraField = null
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
					mapFieldsViews = action.mapFields
					binding.fieldsContainerLlyt.addView(addTemplateFieldView(
						key,
						action.listFieldFormats
					))
				}
			}

			is ZCardTemplatePrintAction.TemplateViewCreated -> {

			}

			is ZCardTemplatePrintAction.CameraCapture ->  // Opening Camera
				startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CAMERA)


			is ZCardTemplatePrintAction.CameraPermissionsRequested -> EDHelper.requestCameraPermission(this, REQUEST_PERMISSION_CAMERA)
		}
	}

	private fun setupUiComponents() {
		binding.btnSend2print.setOnClickListener {
			if (!isWaitingForAsyncTask()) {
				UIHelper.hideSoftKeyboard(requireActivity())
				it.isEnabled = false
				//val templateFragment: TemplateJobFragment = templateFragmentPagerAdapter.getTemplateFragment()
				sendTemplateJobTask = SendTemplateJobTask(requireContext(), printer, zebraCardTemplate, args.template.name, templateFragment.getVariableData())
				sendTemplateJobTask!!.setOnSendTemplateJobListener(templateFragment)
				jobSendTemplate = launch { sendTemplateJobTask!!.execute() }
			}
		}
	}

	private fun addTemplateFieldView(fieldName: String, listFieldFormats: List<String>): View {
		if (fieldName.contains("photo")) {
			return (layoutInflater.inflate( R.layout.item_template_photo_field, binding.printTemplate, false) as AppCompatButton).apply {
				text = fieldName
				setOnClickListener {
					btn = it as AppCompatButton
					cameraField = fieldName
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
					override fun afterTextChanged(editable: Editable) {
						fields[fieldName] = editable.toString()
						val view: TextView =  mapFieldsViews[fieldName] as TextView
						val viewFields = mapFieldsViews.filter { (_, value) -> value == view}.keys

						var format: String = listFieldFormats.find { it.contains("{$fieldName}") }!!

						viewFields.forEach { field ->
							format = XMLMapper.replace(format, field, if (fields[field].toString().isNotEmpty()) fields[field].toString() else "{$field}")
						}

						view.text = format
					}
				})
			}
		}
	}

	private fun isWaitingForAsyncTask(): Boolean {
		return AsyncTaskHelper.isAsyncTaskRunning(sendTemplateJobTask)
	}
}