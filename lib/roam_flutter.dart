import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

typedef void RoamCallBack({String location});

class RoamFlutter {
  static const String METHOD_INITIALIZE = "initialize";
  static const String METHOD_GET_CURRENT_LOCATION = "getCurrentLocation";
  static const String METHOD_CREATE_USER = "createUser";
  static const String METHOD_UPDATE_CURRENT_LOCATION = "updateCurrentLocation";
  static const String METHOD_START_TRACKING = "startTracking";
  static const String METHOD_STOP_TRACKING = "stopTracking";
  static const String METHOD_LOGOUT_USER = "logoutUser";

  static const String TRACKING_MODE_PASSIVE = "passive";
  static const String TRACKING_MODE_REACTIVE = "reactive";
  static const String TRACKING_MODE_ACTIVE = "active";

  static const MethodChannel _channel = const MethodChannel('roam_flutter');
  static RoamCallBack _callBack;

  static Future<bool> initialize({
    @required String publishKey,
  }) async {
    final Map<String, dynamic> params = <String, dynamic>{
      'publishKey': publishKey
    };
    final bool result = await _channel.invokeMethod(METHOD_INITIALIZE, params);
    _channel.setMethodCallHandler(_methodCallHandler);
    return result;
  }

  static Future<bool> createUser({
    @required String description,
  }) async {
    final Map<String, dynamic> params = <String, dynamic>{
      'description': description
    };
    final bool result = await _channel.invokeMethod(METHOD_CREATE_USER, params);
    _channel.setMethodCallHandler(_methodCallHandler);
    return result;
  }

  static Future<bool> updateCurrentLocation({
    @required int accuracy,
  }) async {
    final Map<String, dynamic> params = <String, dynamic>{'accuracy': accuracy};
    final bool result =
        await _channel.invokeMethod(METHOD_UPDATE_CURRENT_LOCATION, params);
    _channel.setMethodCallHandler(_methodCallHandler);
    return result;
  }

  static Future<void> getCurrentLocation(
      {@required int accuracy, RoamCallBack callBack}) async {
    final Map<String, dynamic> params = <String, dynamic>{'accuracy': accuracy};
    final String result =
        await _channel.invokeMethod(METHOD_GET_CURRENT_LOCATION, params);
    callBack(location: result);
  }

  static Future<bool> startTracking({@required String trackingMode}) async {
    final Map<String, dynamic> params = <String, dynamic>{
      'trackingMode': trackingMode
    };
    final bool result =
        await _channel.invokeMethod(METHOD_START_TRACKING, params);
    return result;
  }

  static Future<bool> logoutUser() async {
    final bool result = await _channel.invokeMethod(METHOD_LOGOUT_USER);
    _channel.setMethodCallHandler(_methodCallHandler);
    return result;
  }

  static Future<bool> stopTracking() async {
    final bool result = await _channel.invokeMethod(METHOD_STOP_TRACKING);
    _channel.setMethodCallHandler(_methodCallHandler);
    return result;
  }

  static Future<void> _methodCallHandler(MethodCall call) async {
    switch (call.method) {
      case 'callback':
        _callBack(location: call.arguments['location']);
        break;
      default:
        print('This normally shouldn\'t happen.');
    }
  }

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}