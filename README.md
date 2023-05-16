# Pivo basic SDK Android test application

This is an android test application for basic Pivo SDK. This application will allow to control the Pivo rotator.

## Before you begin

Please visit [Pivo developer website](https://developer.pivo.app/) and generate the license file to include it into your project. 

## Installation

1- In your project-level `build.gradle` file, add the Maven url in allprojects:
```
allprojects {
    repositories {
         mavenCentral()
    }
```
2- In your app-level `build.gradle` file, add dependencies for PivoBasicSdk

```groovy
dependencies {
    /**
     * Pivo pod controller dependencies
     */
    implementation 'app.pivo.android.basicsdk:basicsdk:1.3.6'

    /**
     * RxJava dependencies
     */
    implementation "io.reactivex.rxjava2:rxjava:2.2.19"
    implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
    implementation 'io.reactivex.rxjava2:rxkotlin:2.4.0'

    /**
     * RxLifecycle.
     * This library allows one to automatically complete sequences based on a second lifecycle stream.
     */
    implementation 'com.trello:rxlifecycle:1.0'
    implementation 'com.trello:rxlifecycle-components:1.0'

    /**
     * RxAndroidBle is a powerful painkiller for Android's Bluetooth Low Energy headaches
     * https://github.com/Polidea/RxAndroidBle
     */
    implementation 'com.polidea.rxandroidble2:rxandroidble:1.17.2'
    implementation 'com.jakewharton.rx2:replaying-share-kotlin:2.2.0'

    /**
     * Coroutine dependencies
     */
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.2"
}
```

## Usage

In your custom application class.

```kotlin
//...
import app.pivo.android.basicsdk.PivoSdk

class App: Application()
{
    override fun onCreate() {
        super.onCreate()
        PivoSdk.init(this) // initialize Pivo SDK
        PivoSdk.getInstance().unlockWithLicenseKey("License Contents")
    }

//...
}
```

## Report
If you encounter an issue during setting up the sdk, please contact us at app@3i.ai or open an issue.
