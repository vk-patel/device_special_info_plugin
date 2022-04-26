import 'dart:async';

import 'package:flutter/services.dart';

class DeviceSpecialInfo {
  static const MethodChannel _channel = MethodChannel('device_special_info');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String?> get serialNumber async {
    String result = await _channel.invokeMethod('getSerialNumber');
    return result;
  }

  static Future<String?> get bluetoothMacAddress async {
    String getBluetoothMacAddress =
        await _channel.invokeMethod('getBluetoothMacAddress');
    return getBluetoothMacAddress;
  }

  static Future<String?> get isRoamingEnabled async {
    bool isDataRoamingEnabled =
        await _channel.invokeMethod('isDataRoamingEnabled');
    return isDataRoamingEnabled ? "Yes" : "No";
  }

  static Future<String> get uptime async {
    final int uptime = await _channel.invokeMethod('getUptime');
    //Uptime
    var uptimeDuration = Duration(milliseconds: uptime);
    return uptimeDuration.toString();
  }
}
