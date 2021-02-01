import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:roam_flutter/roam_flutter.dart';

import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  bool isTracking = false;
  String myLocation;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await RoamFlutter.platformVersion;
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
          title: const Text('Roam Plugin Example App'),
        ),
        body: Center(
            child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text('Running on: $_platformVersion\n'),
            Text('Tracking status: $isTracking\n'),
            Text(
              'Received Location:\n $myLocation\n',
              textAlign: TextAlign.center,
            ),
            RaisedButton(
                child: Text('Request Location Permissions'),
                onPressed: () async {
                  try {
                    await Permission.locationAlways.request();
                  } on PlatformException {
                    print('Error getting location permissions');
                  }
                }),
            RaisedButton(
                child: Text('Initialize SDK'),
                onPressed: () async {
                  try {
                    await RoamFlutter.initialize(
                        publishKey:
                            'fd7bd6d1b1ecbfbd456bf9ccd3f4157323eb184d919e5cd341ad0fad216d0b06');
                  } on PlatformException {
                    print('Initialization Error');
                  }
                }),
            RaisedButton(
                child: Text('Create User'),
                onPressed: () async {
                  try {
                    await RoamFlutter.createUser(description: 'Joe');
                  } on PlatformException {
                    print('Create User Error');
                  }
                }),
            RaisedButton(
                child: Text('Update Current Location'),
                onPressed: () async {
                  try {
                    await RoamFlutter.updateCurrentLocation(accuracy: 100);
                  } on PlatformException {
                    print('Update Current Location Error');
                  }
                }),
            RaisedButton(
                child: Text('Get Current Location'),
                onPressed: () async {
                  setState(() {
                    myLocation = "fetching location..";
                  });
                  try {
                    await RoamFlutter.getCurrentLocation(
                      accuracy: 50,
                      callBack: ({location}) {
                        setState(() {
                          myLocation = location;
                        });
                        print(location);
                      },
                    );
                  } on PlatformException {
                    print('Get Current Location Error');
                  }
                }),
            RaisedButton(
                child: Text('Start Tracking'),
                onPressed: () async {
                  try {
                    await RoamFlutter.startTracking(trackingMode: 'active');
                  } on PlatformException {
                    print('Start Tracking Error');
                  }
                }),
            RaisedButton(
                child: Text('Stop Tracking'),
                onPressed: () async {
                  try {
                    await RoamFlutter.stopTracking();
                  } on PlatformException {
                    print('Stop Tracking Error');
                  }
                }),
            RaisedButton(
                child: Text('Logout User'),
                onPressed: () async {
                  try {
                    await RoamFlutter.logoutUser();
                  } on PlatformException {
                    print('Logout User Error');
                  }
                }),
          ],
        )),
      ),
    );
  }
}