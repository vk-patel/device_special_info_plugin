import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:device_special_info/device_special_info.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String _deviceName = '';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      // var status  = await DeviceSpecialInfo.turnOnBluetooth;
      // print("turnOnBluetooth status : $status");
      platformVersion = await DeviceSpecialInfo.platformVersion ?? 'Unknown platform version';
      /*String? bluetoothName  = await DeviceSpecialInfo.bluetoothName;
      print("bluetoothName : $bluetoothName");

      String? uptime  = await DeviceSpecialInfo.uptime;
      print("uptime : $uptime");
      String? serialNumber = await DeviceSpecialInfo.serialNumber;
      print("serialNumber $serialNumber");
      String? imeiNumber = await DeviceSpecialInfo.imeiNumber;
      print("imeiNumber $imeiNumber");

      await DeviceSpecialInfo.getInstalledApps().then((value){
        value.forEach((element) {
          print("Name : ${element.name} -- Package Name : ${element.packageName}");
        });
      });*/
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text('Running on: $_platformVersion\n'),
              _deviceName.isNotEmpty ? Text('Device Name: $_deviceName\n') : Container(height: 0,),
              RaisedButton(
                onPressed: () async{
                  //get device name from about phone
                  String name="";
                  var deviceName = await DeviceSpecialInfo.deviceName;
                  name = deviceName??"";

                  if(name.isEmpty){
                    String? bluetoothName  = await DeviceSpecialInfo.bluetoothName;
                    name = bluetoothName??"";
                  }
                  setState(() {
                    _deviceName=name;
                  });
                },
                child: Text("Device Name"),
              )
            ],
          ),
        ),
      ),
    );
  }
}
