package com.example.device_special_info.device_special_info

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.bluetooth.BluetoothAdapter
import android.content.ContentResolver
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.P
import android.os.Handler
import android.os.SystemClock
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.lang.reflect.Field
import java.util.*


/** DeviceSpecialInfoPlugin */
class DeviceSpecialInfoPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private var applicationContext: Context? = null
    private lateinit var myDevice: BluetoothAdapter

    /*override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
      channel = MethodChannel(flutterPluginBinding.binaryMessenger, "device_special_info")
      channel.setMethodCallHandler(this)
    }*/
    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        this.applicationContext = flutterPluginBinding.applicationContext
        Log.v("DeviceSpecialInfoPlugin", "onAttachedToEngine()");
        channel = MethodChannel(flutterPluginBinding.flutterEngine.dartExecutor, "device_special_info")
        channel.setMethodCallHandler(this);
    }

    companion object {
        fun convertAppToMap(
                packageManager: PackageManager,
                app: ApplicationInfo,
        ): HashMap<String, Any?> {
            val map = HashMap<String, Any?>()
            map["name"] = packageManager.getApplicationLabel(app)
            map["package_name"] = app.packageName
            /*map["icon"] =
                    if (withIcon) drawableToByteArray(app.loadIcon(packageManager)) else ByteArray(0)*/
            val packageInfo = packageManager.getPackageInfo(app.packageName, 0)
            map["version_name"] = packageInfo.versionName
            map["version_code"] = getVersionCode(packageInfo)
            return map
        }

        @Suppress("DEPRECATION")
        private fun getVersionCode(packageInfo: PackageInfo): Long {
            return if (SDK_INT < P) packageInfo.versionCode.toLong()
            else packageInfo.longVersionCode
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "getPlatformVersion") {
            result.success("Android ${android.os.Build.VERSION.RELEASE}")
        } else if (call.method == "getSerialNumber") {
            val serial = getSerialNumber();
            if (serial != "") {
                if (result != null) {
                    result.success(serial)
                }
            } else {
                if (result != null) {
                    result.error("UNAVAILABLE", "Serial Number not available.", null)
                }
            }
        } else if (call.method == "isDataRoamingEnabled") {
            val isRoaming = isDataRoamingEnabled(applicationContext!!)
            if (result != null) {
                result.success(isRoaming)
            }
        } else if (call.method == "getIMEI") {
            val imeiNumber = getIMEI(applicationContext!!)
            if (result != null) {
                result.success(imeiNumber)
            }
        } else if (call.method == "getInstalledApps") {
            val includeSystemApps = call.argument("exclude_system_apps") ?: true
            val packageNamePrefix: String = call.argument("package_name_prefix") ?: ""
            Thread {
                val apps: List<Map<String, Any?>> =
                        getInstalledApps(includeSystemApps, packageNamePrefix)
                result.success(apps)
            }.start()
        } else if (call.method == "getBluetoothMacAddress") {
            val bluetoothMac = getBluetoothMacAddress()
            if (result != null) {
                result.success(bluetoothMac)
            }
        } else if (call.method == "getUptime") {
            result?.let { getUptime(result) }
        } else if (call.method == "turnOnBluetooth") {
            myDevice = BluetoothAdapter.getDefaultAdapter()
            /*var deviceName = Settings.System.getString(applicationContext!!.contentResolver, "device_name")
            Log.v("Device Name", "system device_name: " + deviceName);
            result?.let { result.success(Settings.System.getString(applicationContext!!.contentResolver, "device_name")) }*/
            if (!myDevice.isEnabled) {
                var status = myDevice.enable();
                result?.let { result.success(status) }
            }else{
                result?.let { result.success(false) }
            }
        } else if (call.method == "bluetoothName") {
            myDevice = BluetoothAdapter.getDefaultAdapter()
            var deviceName:String=""
            if (myDevice.isEnabled) {
                deviceName = myDevice.name
                Log.v("ON Bluetooth Name", "system device_name: " + deviceName);
                result?.let { result.success(deviceName) }
            } else {
                myDevice.enable()
                deviceName = myDevice.name
                Log.v("OFF Bluetooth Name", "system device_name: " + deviceName);
                result?.let { result.success(deviceName) }
            }
            /*Log.v("Bluetooth Name", "system device_name: " + deviceName);*/
            /*result?.let { result.success(deviceName) }*/
        } else if (call.method == "deviceName") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                Log.i("ITSM", "DEVICE NAME : ${Settings.Global.getString(applicationContext?.contentResolver, Settings.Global.DEVICE_NAME)}")
                var name = Settings.Global.getString(applicationContext?.contentResolver, Settings.Global.DEVICE_NAME)
                result?.let { result.success(name) }
            } else {
                result?.let { result.success("") }
            }
        } else if(call.method == "enrollmentSpecificId"){
            val eId = call.argument("enterpriseID") ?: ""
            if (Build.VERSION.SDK_INT >= 31) {
                val manager = applicationContext?.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                if(manager.isProfileOwnerApp(applicationContext?.packageName)){
                    var enterpriseID = eId
                    manager.setOrganizationId(enterpriseID)
                    if (manager.enrollmentSpecificId.isNotEmpty()) {
                        result?.success(manager.enrollmentSpecificId)
                    } else result?.success("")
                }else result?.success("")
            }else result?.success("")
        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        if (channel != null) channel.setMethodCallHandler(null);
    }

    private fun getUptime(result: MethodChannel.Result) {
        result.success(SystemClock.elapsedRealtime())
    }

    @SuppressLint("MissingPermission")
    private fun getSerialNumber(): String {
        val serialNumber: String
        if (Build.VERSION.SDK_INT >= 26) {
            serialNumber = android.os.Build.getSerial();
        } else /*if(Build.VERSION.SDK_INT <= 25)*/ {
            serialNumber = android.os.Build.SERIAL;
        }
        return serialNumber
    }

    @SuppressLint("HardwareIds", "LongLogTag")
    private fun getIMEI(c: Context): String {
        val telephonyManager: TelephonyManager
        telephonyManager = c.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val deviceId: String
        deviceId = if (telephonyManager.deviceId == null) {
            "returned null"
        } else {
            telephonyManager.deviceId
        }
        return deviceId
    }

    private fun getInstalledApps(
            excludeSystemApps: Boolean,
            packageNamePrefix: String
    ): List<Map<String, Any?>> {
        val packageManager = applicationContext!!.packageManager
        var installedApps = packageManager.getInstalledApplications(0)
        if (excludeSystemApps)
            installedApps =
                    installedApps.filter { app -> !isSystemApp(packageManager, app.packageName) }
        if (packageNamePrefix.isNotEmpty())
            installedApps = installedApps.filter { app ->
                app.packageName.startsWith(
                        packageNamePrefix.lowercase(Locale.ENGLISH)
                )
            }
        return installedApps.map { app -> convertAppToMap(packageManager, app) }
    }

    private fun isSystemApp(packageManager: PackageManager, packageName: String): Boolean {
        return packageManager.getLaunchIntentForPackage(packageName) == null
    }


    private fun isDataRoamingEnabled(application_context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT < 17) {
                Settings.System.getInt(
                        application_context.contentResolver,
                        Settings.Secure.DATA_ROAMING,
                        0
                ) === 1
            } else Settings.Global.getInt(
                    application_context.contentResolver,
                    Settings.Global.DATA_ROAMING,
                    0
            ) === 1
        } catch (exception: Exception) {
            false
        }
    }

    private fun getBluetoothMacAddress(): String {
        var bluetoothMacAddress: String
        try {
            /*var bluetoothManager:BluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothMacAddress=bluetoothManager.adapter.address*/

            /*bluetoothMacAddress = Settings.Secure.getString(context.contentResolver, "bluetooth_address")*/

            val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val mServiceField: Field = bluetoothAdapter.javaClass.getDeclaredField("mService")
            mServiceField.isAccessible = true
            val btManagerService: Any = mServiceField.get(bluetoothAdapter)
            if (btManagerService != null) {
                bluetoothMacAddress = btManagerService.javaClass.getMethod("getAddress")
                        .invoke(btManagerService) as String
            } else {
                bluetoothMacAddress = ""
            }
        } catch (exception: Exception) {
            bluetoothMacAddress = ""
        }
        return bluetoothMacAddress
    }
}
