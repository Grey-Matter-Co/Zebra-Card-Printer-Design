package mx.com.infotecno.zebracardprinter.ui.printerdiscover

import android.content.*
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.zebra.sdk.printer.discovery.DiscoveredPrinter
import com.zebra.sdk.printer.discovery.DiscoveredPrinterUsb
import com.zebra.sdk.printer.discovery.UsbDiscoverer
import com.zebra.zebraui.ZebraEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mx.com.infotecno.zebracardprinter.R
import mx.com.infotecno.zebracardprinter.action.DiscoveredPrinterAction
import mx.com.infotecno.zebracardprinter.adapter.DiscoveredPrinterAdapter
import mx.com.infotecno.zebracardprinter.databinding.PrinterDiscoverFragmentBinding
import mx.com.infotecno.zebracardprinter.discovery.ManualConnectionTask
import mx.com.infotecno.zebracardprinter.discovery.NetworkAndUsbDiscoveryTask
import mx.com.infotecno.zebracardprinter.discovery.SelectedPrinterManager
import mx.com.infotecno.zebracardprinter.util.DialogHelper
import mx.com.infotecno.zebracardprinter.util.ProgressOverlayHelper
import mx.com.infotecno.zebracardprinter.util.UsbHelper
import kotlin.coroutines.CoroutineContext

class PrinterDiscoverFragment : Fragment(), CoroutineScope {

    private val viewModel: PrinterDiscoverViewModel by viewModels()

    private lateinit var binding: PrinterDiscoverFragmentBinding

    //For Coroutines
    private var jobManualConnection: Job = Job()
    private var jobNetworkAndUsbDiscovery: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Job() + Dispatchers.IO

    private lateinit var manualConnectionTask: ManualConnectionTask
    private lateinit var networkAndUsbDiscoveryTask: NetworkAndUsbDiscoveryTask

    private val discoveredPrinterAdapter by lazy {
        DiscoveredPrinterAdapter(
            clickListener = { clickedPrinter, _ ->
                if (!viewModel.isApplicationBusy) {
                    viewModel.isApplicationBusy = true
                    ProgressOverlayHelper.showProgressOverlay(binding.overlayContainter.progressMessage, binding.overlayContainter.progressOverlay, getString(R.string.connecting_to_printer))
                    if (clickedPrinter is DiscoveredPrinterUsb) {
                        val usbManager = UsbHelper.getUsbManager(requireContext())
                        val device = clickedPrinter.device
                        if (!usbManager.hasPermission(device)) {
                            ProgressOverlayHelper.showProgressOverlay(binding.overlayContainter.progressMessage, binding.overlayContainter.progressOverlay, getString(R.string.msg_waiting_requesting_usb_permission))
                            viewModel.requestUSBPermission(usbManager, device)
                            return@DiscoveredPrinterAdapter
                        }
                    }
                    SelectedPrinterManager.setSelectedPrinter(clickedPrinter)

                    binding.recViewDiscoveredPrinters.findNavController()
                        .navigate(R.id.action_printerDiscoverFragment_to_mainFragment)
                }
            }
        ).apply { this.context = requireContext() }
    }

    private val usbDisconnectReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (UsbManager.ACTION_USB_DEVICE_DETACHED == intent.action) {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                if (device != null && UsbDiscoverer.isZebraUsbDevice(device))

                    viewModel.usbPrinterDisconnected(
                        discoveredPrinterAdapter.currentList,
                        DiscoveredPrinterUsb(device.deviceName, UsbHelper.getUsbManager(requireContext()), device)
                    )
            }
        }
    }
    private val usbPermissionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (UsbHelper.ACTION_USB_PERMISSION_GRANTED == intent.action)
                synchronized(this) {
                    val permissionGranted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    if (device != null && UsbDiscoverer.isZebraUsbDevice(device))
                        if (permissionGranted) {
                            SelectedPrinterManager.setSelectedPrinter(DiscoveredPrinterUsb(device.deviceName, UsbHelper.getUsbManager(requireContext()), device))
                            binding.recViewDiscoveredPrinters.findNavController()
                                .navigate(R.id.action_printerDiscoverFragment_to_mainFragment)
                        }
                        else
                            DialogHelper.showErrorDialog(requireActivity(), getString(R.string.msg_warning_usb_permissions_denied))
                    viewModel.isApplicationBusy = false
                    ProgressOverlayHelper.hideProgressOverlay(binding.overlayContainter.progressMessage, binding.overlayContainter.progressOverlay)
                }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel.actions.observe(viewLifecycleOwner) { handleAction(it) }
        binding = PrinterDiscoverFragmentBinding.inflate(inflater)
        binding.recViewDiscoveredPrinters.adapter = discoveredPrinterAdapter
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        registerReceivers()
        setupToolbar()
        setupUiComponents()
        automaticPrinterDiscover()
    }

    override fun onResume() { super.onResume(); registerReceivers() }
    override fun onPause()  { super.onPause(); unregisterReceivers() }
    override fun onDestroy() { super.onDestroy(); jobNetworkAndUsbDiscovery.cancel(); jobManualConnection.cancel() }

    private fun setupToolbar() {
        val appCompatActivity = activity as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
        appCompatActivity.title = "Impresoras"
    }

    private fun setupUiComponents() {
//        rotation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_rotate)
        binding.fab.setOnClickListener { manualPrinterDiscover() }
        binding.pullToRefreshPrinters.setOnRefreshListener { automaticPrinterDiscover() }
        binding.recViewDiscoveredPrinters.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    /*	Handle changes on ZCardTemplates List	*/
    private fun handleAction(action: DiscoveredPrinterAction) {
        when (action) {
            is DiscoveredPrinterAction.DiscoveredPrintersChanged -> {
                discoveredPrinterAdapter.submitList(action.discoveredPrinters) {
                    if (discoveredPrinterAdapter.currentList.size==1) {
                        binding.noPrintersFoundContainer.visibility = View.GONE
                        binding.recViewDiscoveredPrinters.visibility = View.VISIBLE
                    }
                }

            }
            is DiscoveredPrinterAction.USBPermissionsRequested ->  UsbHelper.requestUsbPermission(requireContext(), action.usbManager, action.device)
            DiscoveredPrinterAction.DiscoveredPrintersStarted -> {
                binding.noPrintersFoundContainer.visibility = View.VISIBLE
                binding.recViewDiscoveredPrinters.visibility = View.GONE

                discoveredPrinterAdapter.submitList(emptyList())

                jobNetworkAndUsbDiscovery.cancel()
                networkAndUsbDiscoveryTask = NetworkAndUsbDiscoveryTask(UsbHelper.getUsbManager(requireContext()))
                networkAndUsbDiscoveryTask.setOnPrinterDiscoveryListener(object: NetworkAndUsbDiscoveryTask.OnPrinterDiscoveryListener {
                    override fun onPrinterDiscoveryStarted() {}
                    override fun onPrinterDiscovered(printer: DiscoveredPrinter?) = viewModel.onPrinterDiscovered(printer, discoveredPrinterAdapter)
                    override fun onPrinterDiscoveryFinished(exception: Exception?) {
                        binding.pullToRefreshPrinters.isRefreshing = false
                        if (exception != null)
                            DialogHelper.showErrorDialog(requireActivity(), getString(R.string.error_discovering_printers_message, exception.message))
                    }
                })
                jobNetworkAndUsbDiscovery = launch { networkAndUsbDiscoveryTask.execute() }
            }
        }
    }


    private fun manualPrinterDiscover() {
        if (!viewModel.isApplicationBusy)
        {
            DialogHelper.createManuallyConnectDialog(requireContext()) { dialog, _ ->
                val printerDnsIpAddressInput: ZebraEditText? = (dialog as AlertDialog).findViewById(R.id.printerDnsIpAddressInput)
                val ipAddress = printerDnsIpAddressInput!!.text

                jobManualConnection.cancel()
                manualConnectionTask = ManualConnectionTask(requireContext(), ipAddress)
                manualConnectionTask.setOnManualConnectionListener(object :
                    ManualConnectionTask.OnManualConnectionListener {
                    override fun onManualConnectionStarted() {
                        viewModel.isApplicationBusy = true
                        ProgressOverlayHelper.showProgressOverlay(
                            binding.overlayContainter.progressMessage,
                            binding.overlayContainter.progressOverlay,
                            getString(R.string.connecting_to_printer)
                        )
                    }

                    override fun onManualConnectionFinished(exception: Exception?, printer: DiscoveredPrinter?) {
                        viewModel.isApplicationBusy = false
                        ProgressOverlayHelper.hideProgressOverlay(binding.overlayContainter.progressMessage, binding.overlayContainter.progressOverlay)
                        if (exception != null)
                            DialogHelper.showErrorDialog(requireActivity(), getString(R.string.error_manually_connecting_message, exception.message))
                        else if (printer != null) {
                            SelectedPrinterManager.setSelectedPrinter(printer)
                            binding.recViewDiscoveredPrinters.findNavController()
                                .navigate(R.id.action_printerDiscoverFragment_to_mainFragment)
                        }
                    }
                })
                jobManualConnection = launch { manualConnectionTask.execute() }
            }.show()
        }
    }

    private fun automaticPrinterDiscover()
    {
        binding.pullToRefreshPrinters.isRefreshing = true

        viewModel.startDiscover()
    }

    private fun registerReceivers() {
        var filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        requireActivity().registerReceiver(usbDisconnectReceiver, filter)
        filter = IntentFilter()
        filter.addAction(UsbHelper.ACTION_USB_PERMISSION_GRANTED)
        requireActivity().registerReceiver(usbPermissionReceiver, filter)
    }

    private fun unregisterReceivers() {
        requireActivity().unregisterReceiver(usbDisconnectReceiver)
        requireActivity().unregisterReceiver(usbPermissionReceiver)
    }
}