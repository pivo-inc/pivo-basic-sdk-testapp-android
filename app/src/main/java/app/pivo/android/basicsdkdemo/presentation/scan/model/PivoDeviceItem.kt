package app.pivo.android.basicsdkdemo.presentation.scan.model

import app.pivo.android.sdk.model.PivoDevice
import app.pivo.android.sdk.model.PivoType

data class PivoDeviceItem(
    val name:String?,
    var macAddress:String,
    val pivoType: PivoType,
    var lastScanTime: Long = System.currentTimeMillis()
) {


    fun toPivoDevice() = PivoDevice(
        name = name,
        macAddress = macAddress,
        pivoType = pivoType,
        uuid = ""
    )

    companion object {
        fun fromPivoDevice(pivoDevice: PivoDevice) = PivoDeviceItem(
            name = pivoDevice.name,
            macAddress = pivoDevice.macAddress,
            pivoType = pivoDevice.pivoType
        )
    }
}