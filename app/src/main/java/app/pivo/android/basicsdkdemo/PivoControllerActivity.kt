package app.pivo.android.basicsdkdemo

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import app.pivo.android.basicsdk.PivoSdk
import app.pivo.android.basicsdk.events.PivoEvent
import app.pivo.android.basicsdk.events.PivoEventBus
import app.pivo.android.basicsdkdemo.databinding.ActivityPivoControllerBinding

@SuppressLint("SetTextI18n")
class PivoControllerActivity : AppCompatActivity() {

    private val tag = "PivoControllerActivity"
    private var position = 0
    private val sdkInstance = PivoSdk.getInstance()

    lateinit var binding: ActivityPivoControllerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPivoControllerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get Pivo supported speed list
        val speedList = sdkInstance.getSupportedSpeeds().toMutableList()

        //show pivo version
        binding.versionView.text = "Pivo Type: ${sdkInstance.getVersion()?.pivoType}\nVersion: ${sdkInstance.getVersion()?.version}"

        //rotate continuously to left
        binding.btnLeftConTurn.setOnClickListener { sdkInstance.turnLeftContinuously(speedList[position]) }

        //rotate to left
        binding.btnLeftTurn.setOnClickListener { sdkInstance.turnLeft(getAngle(), speedList[position]) }

        //rotate continuously to right
        binding.btnRightConTurn.setOnClickListener { sdkInstance.turnRightContinuously(speedList[position]) }

        //rotate to right
        binding.btnRightTurn.setOnClickListener { sdkInstance.turnRight(getAngle(), speedList[position]) }

        //stop rotating the device
        binding.btnStop.setOnClickListener { sdkInstance.stop() }

        //change Pivo name
        binding.btnChangeName.setOnClickListener {
            if (binding.inputPivoName.text.isNotEmpty()){
                sdkInstance.changeName(binding.inputPivoName.text.toString())
            }
        }

        sdkInstance.getDeviceInfo()?.getName()?.let {
            binding.inputPivoName.setText(it)
        }

        //speed list view
        binding.speedListView.adapter= ArrayAdapter(this, android.R.layout.simple_spinner_item, speedList)
        binding.speedListView.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, itemPosition: Int, id: Long) {
                position = itemPosition
                Log.e(tag, "onSpeedChange: ${speedList[position]} save: ${binding.saveSpeedView.isChecked}")
                sdkInstance.setSpeed(speedList[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onResume() {
        super.onResume()
        //subscribe to connection failure event
        PivoEventBus.subscribe(PivoEventBus.CONNECTION_FAILURE,this) {
            if (it is PivoEvent.ConnectionFailure) {
                finish()
            }
        }
        //subscribe pivo remote controller event
        PivoEventBus.subscribe(PivoEventBus.REMOTE_CONTROLLER, this) {
            when (it) {
                is PivoEvent.RCCamera -> binding.notificationView.text =
                    "CAMERA state: ${if (it.state == 0) "Press" else "Release"}"
                is PivoEvent.RCMode -> binding.notificationView.text =
                    "MODE: ${if (it.state == 0) "Press" else "Release"}"
                is PivoEvent.RCStop -> binding.notificationView.text =
                    "STOP: ${if (it.state == 0) "Press" else "Release"}"
                is PivoEvent.RCRightContinuous -> binding.notificationView.text =
                    "RIGHT_CONTINUOUS: ${if (it.state == 0) "Press" else "Release"}"
                is PivoEvent.RCLeftContinuous -> binding.notificationView.text =
                    "LEFT_CONTINUOUS: ${if (it.state == 0) "Press" else "Release"}"
                is PivoEvent.RCLeft -> binding.notificationView.text =
                    "LEFT: ${if (it.state == 0) "Press" else "Release"}"
                is PivoEvent.RCRight -> binding.notificationView.text =
                    "RIGHT: ${if (it.state == 0) "Press" else "Release"}"
                is PivoEvent.RCSpeed -> binding.notificationView.text =
                    "SPEED: : ${if (it.state == 0) "Press" else "Release"} speed: ${it.level}"
            }
        }
        //subscribe to name change event
        PivoEventBus.subscribe(PivoEventBus.NAME_CHANGED, this) {
            if (it is PivoEvent.NameChanged){
                binding.notificationView.text = "Name: ${it.name}"
            }
        }
        //subscribe to mac address event
        PivoEventBus.subscribe(PivoEventBus.MAC_ADDRESS, this) {
            if (it is PivoEvent.MacAddress) {
                binding.notificationView.text = "Mac address: ${it.macAddress}"
            }
        }
        //subscribe to get pivo notifications
        PivoEventBus.subscribe(PivoEventBus.PIVO_NOTIFICATION, this) {
            if (it is PivoEvent.BatteryChanged) {
                binding.notificationView.text = "BatteryLevel: ${it.level}"
            } else {
                binding.notificationView.text = "Notification Received"
            }
        }
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

    private fun getAngle():Int{
        return try {
            val num = binding.angleView.text.toString()
            num.toInt()
        }catch (e:NumberFormatException){
            90
        }
    }
}
