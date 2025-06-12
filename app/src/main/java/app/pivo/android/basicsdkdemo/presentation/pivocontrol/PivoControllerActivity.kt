package app.pivo.android.basicsdkdemo.presentation.pivocontrol

import android.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import app.pivo.android.basicsdk.PivoSdk
import app.pivo.android.basicsdk.events.PivoEvent
import app.pivo.android.basicsdk.events.PivoEventBus
import app.pivo.android.basicsdkdemo.databinding.ActivityPivoControllerBinding

@SuppressLint("SetTextI18n")
class PivoControllerActivity : AppCompatActivity() {

    private var _binding: ActivityPivoControllerBinding? = null
    private val binding get() = _binding!!

    private val tag = "PivoControllerActivity"
    private var position = 0
    private val sdkInstance = PivoSdk.Companion.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityPivoControllerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeUi()
    }

    override fun onResume() {
        super.onResume()
        subscribePivoEvents()
    }

    override fun onPause() {
        super.onPause()
        //unregister before stopping the activity
        PivoEventBus.unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        //disconnect pod connection
        sdkInstance.disconnect()
    }

    private fun initializeUi() {
        //get Pivo supported speed list
        val speedList = sdkInstance.getSupportedSpeeds().toMutableList()

        with(binding) {
            //show pivo version
            versionView.text =
                "Pivo Type: ${sdkInstance.getVersion()?.pivoType}\nVersion: ${sdkInstance.getVersion()?.version}"

            //rotate continuously to left

            btnLeftConTurn.setOnClickListener { sdkInstance.turnLeftContinuously(speedList[position]) }

            //rotate to left
            btnLeftTurn.setOnClickListener { sdkInstance.turnLeft(getAngle(), speedList[position]) }

            //rotate continuously to right
            btnRightConTurn.setOnClickListener { sdkInstance.turnRightContinuously(speedList[position]) }

            //rotate to right
            btnRightTurn.setOnClickListener { sdkInstance.turnRight(getAngle(), speedList[position]) }

            //stop rotating the device
            btnStop.setOnClickListener { sdkInstance.stop() }

            //change Pivo name
            btnChangeName.setOnClickListener {
                if (inputPivoName.text.isNotEmpty()) {
                    sdkInstance.changeName(inputPivoName.text.toString())
                }
            }

            sdkInstance.getDeviceInfo()?.getName()?.let {
                inputPivoName.setText(it)
            }

            //speed list view

            speedListView.adapter =
                ArrayAdapter(this@PivoControllerActivity, android.R.layout.simple_spinner_item, speedList)
            speedListView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    itemPosition: Int,
                    id: Long
                ) {
                    position = itemPosition
                    Log.e(
                        TAG,
                        "onSpeedChange: ${speedList[position]} save: ${saveSpeedView.isChecked}"
                    )
                    sdkInstance.setSpeed(speedList[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }


    }

    override fun onResume() {
        super.onResume()
        //subscribe to connection failure event
        PivoEventBus.subscribe(PivoEventBus.CONNECTION_FAILURE, this) {
            if (it is PivoEvent.ConnectionFailure) {
                finish()
            }
        }
        val notification_view = binding.notificationView
        //subscribe pivo remote controller event
        PivoEventBus.subscribe(PivoEventBus.REMOTE_CONTROLLER, this) {
            when (it) {
                is PivoEvent.RCCamera -> notificationView.text =
                    "CAMERA state: ${if (it.state == 0) "Release" else "Press"}"

                is PivoEvent.RCMode -> notificationView.text =
                    "MODE: ${if (it.state == 0) "Release" else "Press"}"

                is PivoEvent.RCStop -> notificationView.text =
                    "STOP: ${if (it.state == 0) "Release" else "Press"}"

                is PivoEvent.RCRightContinuous -> notificationView.text =
                    "RIGHT_CONTINUOUS: ${if (it.state == 0) "Release" else "Press"}"

                is PivoEvent.RCLeftContinuous -> notificationView.text =
                    "LEFT_CONTINUOUS: ${if (it.state == 0) "Release" else "Press"}"

                is PivoEvent.RCLeft -> notificationView.text =
                    "LEFT: ${if (it.state == 0) "Release" else "Press"}"

                is PivoEvent.RCRight -> notificationView.text =
                    "RIGHT: ${if (it.state == 0) "Release" else "Press"}"

                is PivoEvent.RCSpeed -> notificationView.text =
                    "SPEED: : ${if (it.state == 0) "Release" else "Press"} speed: ${it.level}"
            }
        }
        //subscribe to name change event
        PivoEventBus.subscribe(PivoEventBus.NAME_CHANGED, this) {
            if (it is PivoEvent.NameChanged) {
                notificationView.text = "Name: ${it.name}"
            }
        }
        //subscribe to mac address event
        PivoEventBus.subscribe(PivoEventBus.MAC_ADDRESS, this) {
            if (it is PivoEvent.MacAddress) {
                notificationView.text = "Mac address: ${it.macAddress}"
            }
        }
        //subscribe to get pivo notifications
        PivoEventBus.subscribe(PivoEventBus.PIVO_NOTIFICATION, this) {
            if (it is PivoEvent.BatteryChanged) {
                notificationView.text = "BatteryLevel: ${it.level}"
            } else {
                notificationView.text = "Notification Received"
            }
        }
    }

    private fun getAngle(): Int {
        return try {
            val num = binding.angleView.text.toString()
            num.toInt()
        } catch (e: NumberFormatException) {
            90
        }
    }

    companion object {
        const val TAG = "PivoControllerActivity"
    }
}