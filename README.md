# HBM device scanner for Android
## How to build
* ./gradlew clean
* ./gradlew build
* ./gradlew compileDebugJava
* ./gradlew installDebug &amp;&amp; adb shell 'am start -n com.hbm.devices.scan.ui.android/.ScanActivity'
* adb shell svc power stayon true
* adb logcat "Scan:V *:S"
* adb logcat "System.out:V *:S"

