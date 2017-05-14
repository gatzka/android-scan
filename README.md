# HBM device scanner for Android
[![Build Status](https://travis-ci.org/gatzka/android-scan.svg?branch=master)](https://travis-ci.org/gatzka/android-scan)
[![codebeat badge](https://codebeat.co/badges/e849bb1c-791b-4c2e-9ee1-5e04bb601ca1)](https://codebeat.co/projects/github-com-gatzka-android-scan-master)

## How to build
* ./gradlew clean
* ./gradlew build
* ./gradlew compileDebugJava
* ./gradlew installDebug &amp;&amp; adb shell 'am start -n com.hbm.devices.scan.ui.android/.ScanActivity'
* adb shell svc power stayon true
* adb logcat "Scan:V *:S"
* adb logcat "System.out:V *:S"

Device images must have 56 dp.
Scale images using http://romannurik.github.io/AndroidAssetStudio/
