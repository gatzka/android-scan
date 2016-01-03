# HBM device scanner for Android
[![Build Status](https://travis-ci.org/gatzka/android-scan.svg?branch=master)](https://travis-ci.org/gatzka/android-scan)

## How to build
* ./gradlew clean
* ./gradlew build
* ./gradlew compileDebugJava
* ./gradlew installDebug &amp;&amp; adb shell 'am start -n com.hbm.devices.scan.ui.android/.ScanActivity'
* adb shell svc power stayon true
* adb logcat "Scan:V *:S"
* adb logcat "System.out:V *:S"

Device images must have 56 dp.
