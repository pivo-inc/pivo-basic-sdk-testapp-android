package app.pivo.android.basicsdkdemo

import android.app.Application
import app.pivo.android.basicsdk.PivoSdk
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Created by murodjon on 2020/04/01
 */
class App: Application()
{
    override fun onCreate() {
        super.onCreate()

        //initialize PivoSdk
        PivoSdk.init(this)
        PivoSdk.getInstance().unlockWithLicenseKey(getLicenseContent())
    }

    private fun getLicenseContent():String?{
        var inputStream = assets.open("licenceKey.json")
        return readFile(inputStream)
    }

    @Throws(IOException::class)
    fun readFile(inputStream: InputStream?): String? {
        val str = StringBuilder()
        val br: BufferedReader
        br = BufferedReader(InputStreamReader(inputStream))
        var line: String?=null
        while (br.readLine().also { line = it } != null) {
            str.append(line)
        }
        br.close()
        return str.toString()
    }
}