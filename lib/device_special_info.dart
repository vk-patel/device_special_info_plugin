import 'dart:async';

import 'package:flutter/services.dart';

import 'app_info.dart';

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

  static Future<String?> get imeiNumber async {
    final String imei = await _channel.invokeMethod('getIMEI');
    return imei;
  }

  static Future<List<AppInfo>> getInstalledApps([
    bool excludeSystemApps = true,
    String packageNamePrefix = "",
  ]) async {
    List<dynamic> apps = await _channel.invokeMethod(
      'getInstalledApps',
      {
        "exclude_system_apps": excludeSystemApps,
        "package_name_prefix": packageNamePrefix,
      },
    );
    List<AppInfo> appInfoList = apps.map((app) => AppInfo.create(app)).toList();
    appInfoList.sort((a, b) => a.name!.compareTo(b.name!));
    return appInfoList;
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

  static Future<bool?> get turnOnBluetooth async {
    final bool? turnOnBluetooth = await _channel.invokeMethod('turnOnBluetooth');
    return turnOnBluetooth;
  }

  static Future<String?> get bluetoothName async {
    final String? bluetoothName = await _channel.invokeMethod('bluetoothName');
    return bluetoothName;
  }
}
