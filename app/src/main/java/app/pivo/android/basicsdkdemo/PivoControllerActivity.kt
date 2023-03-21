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
import kotlinx.android.synthetic.main.activity_pivo_controller.*

@SuppressLint("SetTextI18n")
class PivoControllerActivity : AppCompatActivity() {

    private val tag = "PivoControllerActivity"
    private var position = 0
    private val sdkInstance = PivoSdk.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pivo_controller)

        //get Pivo supported speed list
        val speedList = sdkInstance.getSupportedSpeeds().toMutableList()

        //show pivo version
        version_view.text = "Pivo Type: ${sdkInstance.getVersion()?.pivoType}\nVersion: ${sdkInstance.getVersion()?.version}"

        //rotate continuously to left
        btn_left_con_turn.setOnClickListener { sdkInstance.turnLeftContinuously(speedList[position]) }

        //rotate to left
        btn_left_turn.setOnClickListener { sdkInstance.turnLeft(getAngle(), speedList[position]) }

        //rotate continuously to right
        btn_right_con_turn.setOnClickListener { sdkInstance.turnRightContinuously(speedList[position]) }

        //rotate to right
        btn_right_turn.setOnClickListener { sdkInstance.turnRight(getAngle(), speedList[position]) }

        //stop rotating the device
        btn_stop.setOnClickListener { sdkInstance.stop() }

        //change Pivo name
        btn_change_name.setOnClickListener {
            if (input_pivo_name.text.isNotEmpty()){
                sdkInstance.changeName(input_pivo_name.text.toString())
            }
        }

        sdkInstance.getDeviceInfo()?.getName()?.let {
            input_pivo_name.setText(it)
        }

        //speed list view
        speed_list_view.adapter= ArrayAdapter(this, android.R.layout.simple_spinner_item, speedList)
        speed_list_view.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, itemPosition: Int, id: Long) {
                position = itemPosition
                Log.e(tag, "onSpeedChange: ${speedList[position]} save: ${save_speed_view.isChecked}")
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
                is PivoEvent.RCCamera -> notification_view.text =
                    "CAMERA state: ${if (it.state == 0) "Press" else "Release"}"
                is PivoEvent.RCMode -> notification_view.text =
                    "MODE: ${if (it.state == 0) "Press" else "Release"}"
                is PivoEvent.RCStop -> notification_view.text =
                    "STOP: ${if (it.state == 0) "Press" else "Release"}"
                is PivoEvent.RCRightContinuous -> notification_view.text =
                    "RIGHT_CONTINUOUS: ${if (it.state == 0) "Press" else "Release"}"
                is PivoEvent.RCLeftContinuous -> notification_view.text =
                    "LEFT_CONTINUOUS: ${if (it.state == 0) "Press" else "Release"}"
                is PivoEvent.RCLeft -> notification_view.text =
                    "LEFT: ${if (it.state == 0) "Press" else "Release"}"
                is PivoEvent.RCRight -> notification_view.text =
                    "RIGHT: ${if (it.state == 0) "Press" else "Release"}"
                is PivoEvent.RCSpeed -> notification_view.text =
                    "SPEED: : ${if (it.state == 0) "Press" else "Release"} speed: ${it.level}"
            }
        }
        //subscribe to name change event
        PivoEventBus.subscribe(PivoEventBus.NAME_CHANGED, this) {
            if (it is PivoEvent.NameChanged){
                notification_view.text = "Name: ${it.name}"
            }
        }
        //subscribe to mac address event
        PivoEventBus.subscribe(PivoEventBus.MAC_ADDRESS, this) {
            if (it is PivoEvent.MacAddress) {
                notification_view.text = "Mac address: ${it.macAddress}"
            }
        }
        //subscribe to get pivo notifications
        PivoEventBus.subscribe(PivoEventBus.PIVO_NOTIFICATION, this) {
            if (it is PivoEvent.BatteryChanged) {
                notification_view.text = "BatteryLevel: ${it.level}"
            } else {
                notification_view.text = "Notification Received"
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
            val num = angle_view.text.toString()
            num.toInt()
        }catch (e:NumberFormatException){
            90
        }
    }
}
