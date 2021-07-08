package mx.com.infotecno.zebracardprinter.ui.main

// This import treat UI file as a class from the XML assigned
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.zebra.sdk.printer.discovery.DiscoveredPrinter
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb
import com.zebra.sdk.printer.discovery.UsbDiscoverer
import com.zebra.zebraui.ZebraPrinterView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mx.com.infotecno.zebracardprinter.R
import mx.com.infotecno.zebracardprinter.action.ZCardTemplatesListAction
import mx.com.infotecno.zebracardprinter.adapter.ZCardTemplatesAdapter
import mx.com.infotecno.zebracardprinter.databinding.MainFragmentBinding
import mx.com.infotecno.zebracardprinter.discovery.PrinterStatusUpdateTask
import mx.com.infotecno.zebracardprinter.discovery.ReconnectPrinterTask
import mx.com.infotecno.zebracardprinter.discovery.SelectedPrinterManager
import mx.com.infotecno.zebracardprinter.util.*
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.coroutines.CoroutineContext
import mx.com.infotecno.zebracardprinter.util.ExecutingDevicesHelper as EDHelper


class MainFragment : Fragment(), ActionMode.Callback, CoroutineScope {
	companion object {
		private const val REQUEST_START_ACTIVITY = 3001
		private const val REQUEST_SELECT_TEMPLATE = 3002
		private const val REQUEST_PERMISSION_MEDIA = 100
	}
	private val viewModel: MainViewModel by viewModels()

	private var permissionDenied = false
	private var actionMode: ActionMode? = null

	private lateinit var binding: MainFragmentBinding
	private lateinit var tracker: SelectionTracker<String>
	private var newTemplates = arrayListOf<String>()

	//For Coroutines
	private var jobPrinterStatusUpdate : Job = Job()
	private var jobReconnectPrinter : Job = Job()
	override val coroutineContext: CoroutineContext
		get() = Job() + Dispatchers.IO
	private var reconnectPrinterTask    : ReconnectPrinterTask? = null
	private var printerStatusUpdateTask : PrinterStatusUpdateTask? = null

	private val zCardAdapter by lazy {
		ZCardTemplatesAdapter(
			clickListener = { clickedTemplate, clickedTemplatePosition ->
				when (clickedTemplatePosition) {
					0 -> {
						DialogHelper.createAddTemplateDialog(requireContext()) { optionSelected ->
							when (optionSelected) {
								0 -> startActivityForResult(
									createTemplateFileSelectIntent(),
									REQUEST_SELECT_TEMPLATE
								)
								1 -> Toast.makeText(context, "Option Unavailable", Toast.LENGTH_SHORT).show()
							}
						}
					}
					//				1 -> startActivityForResult(Intent(context, Send2PrintActivity::class.java ), REQUEST_START_ACTIVITY)
					else ->  {
						Toast.makeText(context, "[$clickedTemplatePosition] ${clickedTemplate.name} \n Option Unavailable", Toast.LENGTH_SHORT).show()
						binding.recViewZcards.findNavController().navigate(MainFragmentDirections.actionMainFragmentToPrintTemplateFragment(clickedTemplate))
					}
					//startActivity(Intent(context, FieldsCaptureActivity::class.java)
					//.putExtra("zcardSelected", -1))
				}
			},
			longClickListener = { clickedTemplatePosition ->
				when (clickedTemplatePosition) {
					0 -> {
						false
					}
					else -> {
						Snackbar.make(binding.root, "Item Clicked", Snackbar.LENGTH_SHORT).show()
						true
					}
				}
			}
		)
	}

	private val usbPermissionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {
			if (UsbHelper.ACTION_USB_PERMISSION_GRANTED == intent.action)
				synchronized(this) {
					val permissionGranted = intent.getBooleanExtra(
						UsbManager.EXTRA_PERMISSION_GRANTED,
						false
					)
					val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
					if (device != null && UsbDiscoverer.isZebraUsbDevice(device))
						if (permissionGranted)
							SelectedPrinterManager
								.setSelectedPrinter(
									DiscoveredPrinterUsb(
										device.deviceName, UsbHelper.getUsbManager(
											requireContext()
										), device
									)
								)
						else
							DialogHelper.showErrorDialog(
								requireActivity(),
								getString(R.string.msg_warning_usb_permissions_denied)
							)
					ProgressOverlayHelper.hideProgressOverlay(
						binding.bannerProgContainer.bannerProgTxt,
						binding.bannerProgContainer.bannerProg
					)
					refreshSelectedPrinterBanner()
				}
		}
	}
	private val usbDeviceAttachedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {
			if (UsbManager.ACTION_USB_DEVICE_ATTACHED == intent.action) {
				val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
				if (device != null && UsbDiscoverer.isZebraUsbDevice(device)) {
					SelectedPrinterManager.setSelectedPrinter(null)
					val usbManager: UsbManager = UsbHelper.getUsbManager(requireContext())
					if (!usbManager.hasPermission(device)) {
						ProgressOverlayHelper.showProgressOverlay(binding.bannerProgContainer.bannerProgTxt, binding.bannerProgContainer.bannerProg, getString(R.string.msg_waiting_requesting_usb_permission))
						requireActivity().window.navigationBarColor = ResourcesCompat.getColor(resources, R.color.grey_5, null)
						viewModel.requestUSBPermission(usbManager, device)
//						UsbHelper.requestUsbPermission(requireContext(), usbManager, device)
					}
					else {
						SelectedPrinterManager
							.setSelectedPrinter(DiscoveredPrinterUsb(device.deviceName, UsbHelper.getUsbManager(requireContext()), device))
						refreshSelectedPrinterBanner()
					}
				}
			}
		}
	}
	private val usbDeviceDetachedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {
			if (UsbManager.ACTION_USB_DEVICE_DETACHED == intent.action) {
				val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
				val printer = SelectedPrinterManager.getSelectedPrinter()
				if (printer is DiscoveredPrinterUsb && UsbDiscoverer.isZebraUsbDevice(device) && printer.device == device) {
					SelectedPrinterManager.setSelectedPrinter(null)
					refreshSelectedPrinterBanner()
				}
			}
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		viewModel.action.observe(viewLifecycleOwner, { action -> handleAction(action) })
		binding = MainFragmentBinding.inflate(inflater)
		binding.recViewZcards.adapter = zCardAdapter
		return binding.root
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		setupToolbar()
		registerReceivers()
		setupUiComponents()
//		refreshSelectedPrinterBanner()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (data != null)
			when (requestCode) {
				REQUEST_SELECT_TEMPLATE ->
					if (resultCode == Activity.RESULT_OK) {
						val clipData = data.clipData
						if (clipData == null) // Handle one file selected
							if (UriHelper.isXmlFile(requireContext(), data.data!!))
								viewModel.saveTemplate(requireActivity(), data.data!!)
							else
								loadzip(requireActivity().contentResolver.openInputStream(data.data!!)!!).let {
									if (it != null)
										newTemplates.add(it)
								}

						else { // Handle multiple files selected
							// Handle multiple files selected
							val xmlFiles: MutableList<Uri> = ArrayList()
							val zipFiles: MutableList<Uri> = ArrayList()

							for (i in 0 until clipData.itemCount) {
								val uri = clipData.getItemAt(i).uri
								if (UriHelper.isXmlFile(requireContext(), uri))
									xmlFiles.add(uri)
								else
									zipFiles.add(uri)
							}
							for (uri in xmlFiles)
								viewModel.saveTemplate(requireActivity(), uri)

							for (uri in zipFiles)
								newTemplates.add(loadzip(requireActivity().contentResolver.openInputStream(uri)!!)!!)
						}
					}
				REQUEST_START_ACTIVITY ->
					if (resultCode == AppCompatActivity.RESULT_OK) {
						/*
							* Save "KEY_RESET_PRINNTER" as public static constant of SettingsDemoActivity
							* to request it as "SettingsDemoActivity.KEY_RESET_PRINTER"
							* */
						val resetPrinter = data.getBooleanExtra("KEY_RESET_PRINNTER", false)
						if (reconnectPrinterTask != null)
							jobReconnectPrinter.cancel()
						reconnectPrinterTask = ReconnectPrinterTask(
							SelectedPrinterManager.getSelectedPrinter()!!,
							resetPrinter
						)
						reconnectPrinterTask!!.setOnPrinterDiscoveryListener(object :
							ReconnectPrinterTask.OnReconnectPrinterListener {
							override fun onReconnectPrinterStarted() {
								refreshSelectedPrinterBanner()
								ProgressOverlayHelper.showProgressOverlay(
									binding.bannerProgContainer.bannerProgTxt,
									binding.bannerProgContainer.bannerProg,
									getString(
										R.string.msg_waiting_reconnecting_to_printer
									)
								)
								requireActivity().window.navigationBarColor =
									ResourcesCompat.getColor(
										resources,
										R.color.grey_5,
										null
									)
							}

							override fun onReconnectPrinterFinished(exception: java.lang.Exception?) {
								ProgressOverlayHelper.hideProgressOverlay(
									binding.bannerProgContainer.bannerProgTxt,
									binding.bannerProgContainer.bannerProg
								)
								if (exception != null) {
									DialogHelper.showErrorDialog(
										requireActivity(), getString(
											R.string.msg_error_reconnecting_to_printer,
											exception.message
										)
									)
									SelectedPrinterManager.setSelectedPrinter(null)
								}
								refreshSelectedPrinterBanner()
							}

						})
						jobReconnectPrinter = launch { reconnectPrinterTask!!.execute() }
					}
//					TODO("find out how work the next code section, is a requesting storage permission which comes from a next fragment )?")
//					else if (resultCode == AppCompatActivity.RESULT_CANCELED) {
//						val permissionResult = data.getIntExtra(StorageHelper.KEY_STORAGE_PERMISSIONS_RESULT, -1)
//						if (permissionResult == StorageHelper.PERMISSION_DENIED)
//							UIHelper.showSnackbar(this, getString(R.string.storage_permissions_denied))
//						else if (permissionResult == StorageHelper.PERMISSION_NEVER_ASK_AGAIN_SET)
//							UIHelper.showSnackbar(
//								this,
//								getString(R.string.storage_permissions_request_enable_message)
//							)
//					}
			}
	}

	override fun onResume() {
		super.onResume()
		if (!EDHelper.hasStoragePermission(requireContext())) {
			if (!permissionDenied)
				viewModel.requestStoragePermissions()
			return
		}
		viewModel.loadTemplates(newTemplates)
		registerReceivers()
		refreshSelectedPrinterBanner()
	}

	override fun onPause() {
		permissionDenied = false
		unregisterReceivers()
		super.onPause()
	}

	// Just for cancel any coroutine in process
	override fun onDestroy() {
		super.onDestroy()
		jobPrinterStatusUpdate.cancel()
		jobReconnectPrinter.cancel()
	}


	override fun onRequestPermissionsResult(code: Int, permission: Array<out String>, res: IntArray) {
		when (code) {
			REQUEST_PERMISSION_MEDIA -> {
				when {
					res.isEmpty() -> { /* Do nothing, app is resuming */
					}
					res[0] == PackageManager.PERMISSION_GRANTED -> {
						setupUiComponents()
					}
					else -> {
						permissionDenied = true
						Snackbar.make(
							binding.root,
							R.string.missing_permission,
							Snackbar.LENGTH_SHORT
						).show()
					}
				}
			}
		}
	}

	private fun setupToolbar() {
		val appCompatActivity = activity as AppCompatActivity
		appCompatActivity.setSupportActionBar(binding.topToolbar)
		appCompatActivity.setTitle(R.string.app_name)
	}

	private fun setupUiComponents() {
		val colums = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 2 else 1
		binding.recViewZcards.layoutManager = GridLayoutManager(requireContext(), colums)//LinearLayoutManager(requireContext())

		binding.bannerPrnSelContainer.bannerPrnSel.setOnClickListener { promptDisconnectPrinter() }
		binding.bannerNoPrnSelContainer.bannerNoPrnSel.setOnClickListener {
			it.findNavController().navigate(R.id.action_mainFragment_to_printerDiscoverFragment)
		}

		tracker = SelectionTracker.Builder(
			"imagesSelection",
			binding.recViewZcards,
			ZCardTemplateKeyProvider(zCardAdapter),
			ZCardTemplateDetailsLookup(binding.recViewZcards),
			StorageStrategy.createStringStorage()
		)
			.withSelectionPredicate(SelectionPredicates.createSelectAnything())
			.build()

		tracker.addObserver(
			object : SelectionTracker.SelectionObserver<String>() {
				override fun onSelectionChanged() {
					super.onSelectionChanged()
					if (actionMode == null)
						actionMode = activity?.startActionMode(this@MainFragment)
					val items = tracker.selection.size()
					if (items > 0)
						actionMode?.title = getString(R.string.action_selected, items)
					else
						actionMode?.finish()
				}
			}
		)

		zCardAdapter.tracker = tracker
	}

	private fun refreshSelectedPrinterBanner() {
		val printer: DiscoveredPrinter? = SelectedPrinterManager.getSelectedPrinter()
		val isPrinterSelected = printer!=null
		if (isPrinterSelected) {

			val address = printer!!.discoveryDataMap["ADDRESS"]
			val model   = printer.discoveryDataMap["MODEL"]

			binding.bannerPrnSelContainer.bannerPrnSelAddr.apply {
				this.visibility = if (address!=null && address.isNotEmpty()) View.VISIBLE else View.GONE
				this.text = address
			}
			binding.bannerPrnSelContainer.bannerPrnSelModel.apply {
				this.visibility = if (model!=null && model.isNotEmpty()) View.VISIBLE else View.GONE
				this.text = model
			}
			binding.bannerPrnSelContainer.bannerPrnSelIcon.setPrinterModel(model)

			if (reconnectPrinterTask==null || reconnectPrinterTask!!.isBackgroundTaskFinished())
			{
				jobPrinterStatusUpdate.cancel()
				printerStatusUpdateTask = PrinterStatusUpdateTask(requireActivity(), printer)
				printerStatusUpdateTask!!.setOnUpdatePrinterStatusListener(object :
					PrinterStatusUpdateTask.OnUpdatePrinterStatusListener {
					override fun onUpdatePrinterStatusStarted() {
						binding.bannerPrnSelContainer.bannerPrnSelIcon.printerStatus =
							ZebraPrinterView.PrinterStatus.REFRESHING
					}

					override fun onUpdatePrinterStatusFinished(
						exception: java.lang.Exception?,
						printerStatus: ZebraPrinterView.PrinterStatus
					) {
						requireActivity().runOnUiThread {
							if (exception != null) {
								DialogHelper.showErrorDialog(requireActivity(), getString(R.string.msg_error_updating_printer_status, exception.message))
								SelectedPrinterManager.setSelectedPrinter(null)
								refreshSelectedPrinterBanner()
							}
							else
								binding.bannerPrnSelContainer.bannerPrnSelIcon.printerStatus = printerStatus
						}
					}
				})
				jobPrinterStatusUpdate = launch{ printerStatusUpdateTask!!.execute() }
			}
		}

		binding.bannerPrnSelContainer.bannerPrnSel.visibility   = if (isPrinterSelected) View.VISIBLE else View.GONE
		binding.bannerNoPrnSelContainer.bannerNoPrnSel.visibility = if (isPrinterSelected) View.GONE else View.VISIBLE
		binding.bannerProgContainer.bannerProg.visibility = View.GONE
		requireActivity().window.navigationBarColor = ResourcesCompat.getColor(
			resources,
			if (!isPrinterSelected) R.color.red_600 else R.color.grey_5,
			null
		)
	}

	/*	ActionMode.Callback Implements	*/
	override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
		mode?.menuInflater?.inflate(R.menu.action_main, menu)
		return true
	}
	override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = true
	override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
		return when (item!!.itemId) {
			R.id.action_delete -> {
				val templates = zCardAdapter.currentList
					.filter { tracker.selection.contains(it.name) }
				viewModel.deleteTemplates(templates)
				true
			}
			else -> false
		}
	}
	override fun onDestroyActionMode(mode: ActionMode?) {
		tracker.clearSelection()
		actionMode = null
	}
	/*	END ActionMode.Callback Implements	*/

	/*	Handle changes on ZCardTemplates List	*/
	private fun handleAction(action: ZCardTemplatesListAction) {
		when (action) {
			is ZCardTemplatesListAction.TemplatesChanged -> {
				zCardAdapter.submitList(action.templates)
				newTemplates.clear()

				if (action.templates.size <= 1)
					Snackbar.make(binding.root, "No hay plantillas cargadas aún", Snackbar.LENGTH_SHORT).show()
			}
			is ZCardTemplatesListAction.TemplatesDeleted -> zCardAdapter.submitList(action.templates)
			is ZCardTemplatesListAction.USBPermissionsRequested -> UsbHelper.requestUsbPermission(
				requireContext(),
				action.usbManager,
				action.device
			)
			ZCardTemplatesListAction.StoragePermissionsRequested -> EDHelper.requestStoragePermission(this, REQUEST_PERMISSION_MEDIA)
		}
	}

	private fun promptDisconnectPrinter() {
		DialogHelper.createDisconnectDialog(requireContext()) { _, _ ->
			SelectedPrinterManager.setSelectedPrinter(null)
			refreshSelectedPrinterBanner()
		}.show()
	}

	private fun registerReceivers() {
		var filter = IntentFilter()
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
		requireActivity().registerReceiver(usbDeviceDetachedReceiver, filter)
		filter = IntentFilter()
		filter.addAction(UsbHelper.ACTION_USB_PERMISSION_GRANTED)
		requireActivity().registerReceiver(usbPermissionReceiver, filter)
		filter = IntentFilter()
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
		requireActivity().registerReceiver(usbDeviceAttachedReceiver, filter)
	}
	private fun unregisterReceivers() {
		requireActivity().unregisterReceiver(usbDeviceDetachedReceiver)
		requireActivity().unregisterReceiver(usbPermissionReceiver)
		requireActivity().unregisterReceiver(usbDeviceAttachedReceiver)
	}

	private fun createTemplateFileSelectIntent(): Intent? {
		val mimeTypes = arrayOf("text/xml", "application/zip")
		val intent = Intent(Intent.ACTION_GET_CONTENT)
			.setType("*/*")
			.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
			.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
		return Intent.createChooser(
			intent, getString(R.string.select_template_file)
		)
	}

	private fun createTemplateFileSelectIntentZIP(): Intent? {
		val mimeTypes = arrayOf("application/zip")
		val intent = Intent(Intent.ACTION_GET_CONTENT)
			.setType("*/*")
			.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
//			.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
		return Intent.createChooser(
			intent, getString(R.string.select_template_file)
		)
	}

	@Throws(IOException::class)
	private fun loadzip(inputStream: InputStream): String? {
		var frontpreview: Bitmap? = null
		var xmlTemplate = ""
		var jsonData = ""
		var templateName = ""
		val fontFilesList = mutableListOf<Pair<String, ByteArray>>()
		val imageFilesList = mutableListOf<Pair<String, Bitmap>>()

		var validZip = false

		val zipIs = ZipInputStream(inputStream)
		var ze: ZipEntry?

		while (zipIs.nextEntry.also { ze = it } != null) {
			if (ze != null) {
				val fileName = ze!!.name.toLowerCase().substringAfterLast("\\")
				when {
					fileName.contains(".scd") ->  {
						validZip = true
						templateName = ze!!.name.substringBeforeLast(".")
						if (viewModel.alreadyExistsTemplate(templateName))
							break
						XMLMapper.map(XMLDecoder.parseCardDesignProject(ByteArrayInputStream(zipIs.readBytes()))).also { (cardTemplate, fields) ->
							xmlTemplate = XMLEncoder.parse(cardTemplate)
							jsonData = XMLEncoder.parseFields(fields)
						}

					}
					fileName == "frontpreview.png" ->
						zipIs.readBytes().also {
							frontpreview = BitmapFactory.decodeByteArray(it, 0, it.size)
						}
					fileName.contains(".ttf") ->
						fontFilesList.add(Pair(fileName, zipIs.readBytes()))
					fileName.contains(".png") or fileName.contains("jpg") or fileName.contains("jpeg") ->
						zipIs.readBytes().also {
							imageFilesList.add(Pair(fileName, BitmapFactory.decodeByteArray(it, 0, it.size)))
						}
					else -> Log.e("EMBY", "loadzip: UNKOWN FILES" )
				}
			}
		}
		if (!validZip)
			DialogHelper.showErrorDialog(requireActivity(), getString(R.string.msg_error_invalid_zip))
		else if (xmlTemplate.isEmpty()) {
			DialogHelper.showErrorDialog(requireActivity(), getString(R.string.msg_error_template_already_exists))
		}
		else
			viewModel.saveTemplate(requireActivity(), templateName, xmlTemplate, jsonData, frontpreview!!, fontFilesList, imageFilesList)


		zipIs.close()
		return if (xmlTemplate.isEmpty()) null else templateName
	}
}