package app.pivo.android.basicsdkdemo

import android.app.Application
import app.pivo.android.basicsdk.PivoSdk

/**
 * Created by murodjon on 2020/04/01
 */
class App: Application()
{
    override fun onCreate() {
        super.onCreate()

        //initialize PivoSdk
        PivoSdk.init(this)
    }
}