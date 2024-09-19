package app.pivo.android.basicsdkdemo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import app.pivo.android.basicsdk.PivoSdk
import app.pivo.android.basicsdk.events.PivoEventBus
import app.pivo.android.basicsdk.events.PivoEvent
import app.pivo.android.basicsdkdemo.databinding.ActivityPivoScanningBinding

class PivoScanningActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var resultAdapter: ScanResultsAdapter
    private val sdkInstance = PivoSdk.getInstance()

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    lateinit var binding: ActivityPivoScanningBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPivoScanningBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initialize device scan adapter
        resultAdapter = ScanResultsAdapter()
        resultAdapter.setOnAdapterItemClickListener(object :
            ScanResultsAdapter.OnAdapterItemClickListener {
            override fun onAdapterViewClick(view: View?) {
                val scanResult = resultAdapter.getItemAtPosition(binding.scanResults.getChildAdapterPosition(view!!))
                if (scanResult!=null){
                    sdkInstance.connectTo(scanResult)
                }
            }
        })

        //prepare scan result listview
        binding.scanResults.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@PivoScanningActivity)
            adapter = resultAdapter
        }
        //start scanning button
        binding.scanButton.setOnClickListener {
            checkPermission()
        }
        //cancel scanning button
        binding.cancelButton.setOnClickListener {
            binding.scanningBar.visibility = View.INVISIBLE
            sdkInstance.stopScan()
            resultAdapter.clearScanResults()
        }

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.all { it.value }) {
                // When all permissions are granted
                allPermissionsGranted()
            } else {
                // Handling when permission is denied
                // You can explain to the user why the permission is needed and request it again, or take action such as restricting the feature.
            }
        }
    }


    override fun onResume() {
        super.onResume()
        //subscibe pivo connection events
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
                resultAdapter.addScanResult(it.device)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        //unregister pivo event bus
        PivoEventBus.unregister(this)
    }

    //open pivo controller screen
    private fun openController(){
        startActivity(Intent(this, PivoControllerActivity::class.java))
    }

    //check permissions if they're granted start scanning, otherwise ask to user to grant permissions
    private fun checkPermission() {
        val ungrantedPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this@PivoScanningActivity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (ungrantedPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(ungrantedPermissions.toTypedArray())
        } else {
            allPermissionsGranted()
        }
    }

    private fun allPermissionsGranted() {
        binding.scanningBar.visibility = View.VISIBLE
        sdkInstance.scan()
    }

    //permissions which are required for bluetooth
    private var requiredPermissions = mutableListOf(
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
}
