package com.example.device_special_info.device_special_info

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.lang.reflect.Field
import kotlin.coroutines.coroutineContext


/** DeviceSpecialInfoPlugin */
class DeviceSpecialInfoPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "device_special_info")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    }
    else if (call.method == "getSerialNumber") {
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
    }
    /*else if (call.method == "isDataRoamingEnabled") {
      val isRoaming = isDataRoamingEnabled(this)
      if (result != null) {
        result.success(isRoaming)
      }
    }*/
    else if (call.method == "getBluetoothMacAddress") {
      val bluetoothMac = getBluetoothMacAddress()
      if (result != null) {
        result.success(bluetoothMac)
      }
    }
    else if (call.method == "getUptime") {
      result?.let { getUptime(result) }
    }
    else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
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
