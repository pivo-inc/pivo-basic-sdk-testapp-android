package app.pivo.android.basicsdkdemo.presentation.scan

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import app.pivo.android.basicsdk.PivoSdk
import app.pivo.android.basicsdk.events.PivoEventBus
import app.pivo.android.basicsdk.events.PivoEvent
import app.pivo.android.basicsdkdemo.databinding.ActivityPivoScanningBinding
import app.pivo.android.basicsdkdemo.presentation.pivocontrol.PivoControllerActivity
import app.pivo.android.basicsdkdemo.presentation.scan.model.PivoDeviceItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PivoScanningActivity : AppCompatActivity() {
    private var _binding: ActivityPivoScanningBinding? = null
    private val binding get() = _binding!!

    private lateinit var resultAdapter: ScanResultsAdapter
    private val sdkInstance = PivoSdk.getInstance()

    private var deviceCleanupJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPivoScanningBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeUi()
    }


    override fun onResume() {
        super.onResume()
        subscribePivoEvents()
        removeDeviceCleanup()
    }

    override fun onPause() {
        super.onPause()
        //unregister pivo event bus
        PivoEventBus.unregister(this)
        deviceCleanupJob?.cancel()
    }

    private fun initializeUi() {
        with(binding) {
            //initialize device scan adapter
            resultAdapter = ScanResultsAdapter()
            resultAdapter.setOnAdapterItemClickListener(object :
                ScanResultsAdapter.OnAdapterItemClickListener {
                override fun onAdapterViewClick(view: View?) {
                    val scanResult = resultAdapter.getItemAtPosition(scanResults.getChildAdapterPosition(view!!))
                    if (scanResult!=null){
                        sdkInstance.connectTo(scanResult.toPivoDevice())
                    }
                }
            })

            //prepare scan result listview
            scanResults.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(this@PivoScanningActivity)
                adapter = resultAdapter
            }
            //start scanning button
            scanButton.setOnClickListener {
                checkPermission()
            }
            //cancel scanning button
            cancelButton.setOnClickListener {
                binding.scanningBar.visibility = View.INVISIBLE
                sdkInstance.stopScan()
                resultAdapter.clearScanResults()
            }
        }
    }

    private fun subscribePivoEvents() {
        PivoEventBus.subscribe(
            PivoEventBus.CONNECTION_COMPLETED, this
        ) {
            binding.scanningBar.visibility = View.INVISIBLE
            if (it is PivoEvent.ConnectionComplete) {
                Log.e(TAG, "CONNECTION_COMPLETED")
                openController()
            }
        }
        //subscribe to get scan device
        PivoEventBus.subscribe(
            PivoEventBus.SCAN_DEVICE, this
        ) {
            if (it is PivoEvent.Scanning) {
                val itemResult = PivoDeviceItem.fromPivoDevice(it.device)
                resultAdapter.addScanResult(itemResult)
            }
        }
    }

    private fun removeDeviceCleanup() {
        deviceCleanupJob = lifecycleScope.launch {
            val internal = 500L
            while (isActive) {
                delay(internal)
                resultAdapter.removeStaleDevices(internal)
            }
        }
    }

    //open pivo controller screen
    private fun openController(){
        startActivity(Intent(this, PivoControllerActivity::class.java))
    }

    //check permissions if they're granted start scanning, otherwise ask to user to grant permissions
    private fun checkPermission(){// alternative Permission library Dexter
        Permissions.check(this,
            permissionList.toTypedArray(), null, null,
            object : PermissionHandler() {
                override fun onGranted() {
                    binding.scanningBar.visibility = View.VISIBLE
                    sdkInstance.scan()
                }
            })
    }

    //permissions which are required for bluetooth
    private var permissionList = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).also {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            it.add(Manifest.permission.BLUETOOTH_SCAN)
            it.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            it.add(Manifest.permission.BLUETOOTH)
            it.add(Manifest.permission.BLUETOOTH_ADMIN)
        }
    }

    companion object {
        const val TAG = "PivoScanningActivity"
    }
}
