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
 implementation 'app.pivo.android.basicsdk:basicsdk:1.0.6'
 implementation 'com.polidea.rxandroidble:rxandroidble:1.5.0'
 /**
  * RxJava dependencies
  */
 implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
 implementation 'io.reactivex.rxjava2:rxjava:2.2.17'
 implementation 'com.trello:rxlifecycle:1.0'
 implementation 'com.trello:rxlifecycle-components:1.0
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
