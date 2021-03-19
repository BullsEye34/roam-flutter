package ai.roam.roam_flutter;

import android.app.Activity;
import android.content.Context;
import android.location.Location;

import com.geospark.lib.GeoSpark;
import com.geospark.lib.GeoSparkTrackingMode;
import com.geospark.lib.callback.GeoSparkCallback;
import com.geospark.lib.callback.GeoSparkCreateTripCallback;
import com.geospark.lib.callback.GeoSparkLocationCallback;
import com.geospark.lib.callback.GeoSparkLogoutCallback;
import com.geospark.lib.callback.GeoSparkTripCallback;
import com.geospark.lib.callback.GeoSparkTripDetailCallback;
import com.geospark.lib.callback.GeoSparkTripStatusCallback;
import com.geospark.lib.callback.GeoSparkTripSummaryCallback;
import com.geospark.lib.models.GeoSparkError;
import com.geospark.lib.models.GeoSparkTripStatus;
import com.geospark.lib.models.GeoSparkUser;
import com.geospark.lib.models.createtrip.GeoSparkCreateTrip;
import com.geospark.lib.models.gettrip.GeoSparkTripDetail;
import com.geospark.lib.models.tripsummary.GeoSparkTripSummary;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/** RoamFlutterPlugin */
public class RoamFlutterPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private Activity activity;

  private static final String METHOD_INITIALIZE = "initialize";
  private static final String METHOD_GET_CURRENT_LOCATION = "getCurrentLocation";
  private static final String METHOD_CREATE_USER = "createUser";
  private static final String METHOD_UPDATE_CURRENT_LOCATION = "updateCurrentLocation";
  private static final String METHOD_START_TRACKING = "startTracking";
  private static final String METHOD_STOP_TRACKING = "stopTracking";
  private static final String METHOD_LOGOUT_USER = "logoutUser";
  private static final String METHOD_GET_USER = "getUser";
  private static final String METHOD_TOGGLE_LISTENER = "toggleListener";
  private static final String METHOD_GET_LISTENER_STATUS = "getListenerStatus";
  private static final String METHOD_TOGGLE_EVENTS = "toggleEvents";
  private static final String METHOD_SUBSCRIBE_LOCATION = "subscribeLocation";
  private static final String METHOD_SUBSCRIBE_USER_LOCATION = "subscribeUserLocation";
  private static final String METHOD_SUBSCRIBE_EVENTS = "subscribeEvents";
  private static final String METHOD_ENABLE_ACCURACY_ENGINE = "enableAccuracyEngine";
  private static final String METHOD_DISABLE_ACCURACY_ENGINE = "disableAccuracyEngine";
  private static final String METHOD_CREATE_TRIP = "createTrip";
  private static final String METHOD_GET_TRIP_DETAILS = "getTripDetails";
  private static final String METHOD_GET_TRIP_STATUS = "getTripStatus";
  private static final String METHOD_SUBSCRIBE_TRIP_STATUS = "subscribeTripStatus";
  private static final String METHOD_UNSUBSCRIBE_TRIP_STATUS = "unSubscribeTripStatus";
  private static final String METHOD_START_TRIP = "startTrip";
  private static final String METHOD_PAUSE_TRIP = "pauseTrip";
  private static final String METHOD_RESUME_TRIP = "resumeTrip";
  private static final String METHOD_END_TRIP = "endTrip";
  private static final String METHOD_GET_TRIP_SUMMARY = "getTripSummary";
  private static final String METHOD_DISABLE_BATTERY_OPTIMIZATION = "disableBatteryOptimization";

  private static final String TRACKING_MODE_PASSIVE = "passive";
  private static final String TRACKING_MODE_REACTIVE = "reactive";
  private static final String TRACKING_MODE_ACTIVE = "active";
  private static final String TRACKING_MODE_CUSTOM = "custom";

  private Context context;

  private void setContext(Context context) {
    this.context = context;
  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding activityPluginBinding) {
    this.activity = activityPluginBinding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    this.activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding activityPluginBinding) {
    this.activity = activityPluginBinding.getActivity();
  }

  @Override
  public void onDetachedFromActivity() {
    this.activity = null;
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    this.context = flutterPluginBinding.getApplicationContext();
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "roam_flutter");
    channel.setMethodCallHandler(this);
  }

  public static void registerWith(PluginRegistry.Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_radar_io");
    final RoamFlutterPlugin plugin = new RoamFlutterPlugin();
    plugin.setContext(registrar.context());
    channel.setMethodCallHandler(plugin);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull final Result result) {
     try {
       switch (call.method) {
         case "getPlatformVersion":
           result.success("Android " + android.os.Build.VERSION.RELEASE);
           break;

         case METHOD_INITIALIZE:
           final String publishKey = call.argument("publishKey");
           GeoSpark.initialize(this.context, publishKey);
           break;
         case METHOD_GET_CURRENT_LOCATION:
           final Integer accuracy = call.argument("accuracy");
           GeoSpark.getCurrentLocation(GeoSparkTrackingMode.DesiredAccuracy.MEDIUM, accuracy, new GeoSparkLocationCallback() {
             @Override
             public void location(Location location) {
               JSONObject coordinates = new JSONObject();
               JSONObject roamLocation = new JSONObject();
               try {
                 coordinates.put("latitude", location.getLatitude());
                 coordinates.put("longitude", location.getLongitude());
                 roamLocation.put("coordinate", coordinates);
                 roamLocation.put("altitude", location.getAltitude());
                 roamLocation.put("accuracy", location.getAccuracy());
                 String locationText = roamLocation.toString().substring(1, roamLocation.toString().length() - 1);
                 result.success(locationText);
               } catch (JSONException e) {
                 e.printStackTrace();
               }
             }

             @Override
             public void onFailure(GeoSparkError geoSparkError) {
               geoSparkError.getCode();
               geoSparkError.getMessage();
             }
           });
           break;

         case METHOD_CREATE_USER:
           final String description = call.argument("description");
           GeoSpark.createUser(description, new GeoSparkCallback() {
             @Override
             public void onSuccess(GeoSparkUser geoSparkUser) {
               JSONObject user = new JSONObject();
               try {
                 user.put("userId", geoSparkUser.getUserId());
                 user.put("description", geoSparkUser.getDescription());
                 String userText = user.toString().substring(1, user.toString().length() - 1);
                 result.success(userText);
               } catch (JSONException e) {
                 e.printStackTrace();
               }
             }

             @Override
             public void onFailure(GeoSparkError geoSparkError) {
               geoSparkError.getMessage();
               geoSparkError.getCode();
             }
           });
           break;

         case METHOD_GET_USER:
           final String userId = call.argument("userId");
           GeoSpark.getUser(userId, new GeoSparkCallback() {
             @Override
             public void onSuccess(GeoSparkUser geoSparkUser) {
               JSONObject user = new JSONObject();
               try {
                 user.put("userId", geoSparkUser.getUserId());
                 user.put("description", geoSparkUser.getDescription());
                 String userText = user.toString().substring(1, user.toString().length() - 1);
                 result.success(userText);
               } catch (JSONException e) {
                 e.printStackTrace();
               }
             }

             @Override
             public void onFailure(GeoSparkError geoSparkError) {
               geoSparkError.getMessage();
               geoSparkError.getCode();
             }
           });
           break;

         case METHOD_TOGGLE_LISTENER:
           final Boolean Events = call.argument("events");
           final Boolean Locations = call.argument("locations");
           GeoSpark.toggleListener(Events, Locations, new GeoSparkCallback() {
             @Override
             public void onSuccess(GeoSparkUser geoSparkUser) {
               JSONObject user = new JSONObject();
               try {
                 user.put("userId", geoSparkUser.getUserId());
                 user.put("events", geoSparkUser.getEventListenerStatus());
                 user.put("locations", geoSparkUser.getLocationListenerStatus());
                 String userText = user.toString().substring(1, user.toString().length() - 1);
                 result.success(userText);
               } catch (JSONException e) {
                 e.printStackTrace();
               }
             }

             @Override
             public void onFailure(GeoSparkError geoSparkError) {
               geoSparkError.getMessage();
               geoSparkError.getCode();
             }
           });
           break;

         case METHOD_TOGGLE_EVENTS:
           final Boolean Geofence = call.argument("geofence");
           final Boolean Location = call.argument("location");
           final Boolean Trips = call.argument("trips");
           final Boolean MovingGeofence = call.argument("movingGeofence");
           GeoSpark.toggleEvents(Geofence, Location, Trips, MovingGeofence, new GeoSparkCallback() {
             @Override
             public void onSuccess(GeoSparkUser geoSparkUser) {
               JSONObject user = new JSONObject();
               try {
                 user.put("userId", geoSparkUser.getUserId());
                 user.put("locationEvents", geoSparkUser.getLocationEvents());
                 user.put("geofenceEvents", geoSparkUser.getGeofenceEvents());
                 user.put("tripsEvents", geoSparkUser.getTripsEvents());
                 user.put("movingGeofenceEvents", geoSparkUser.getMovingGeofenceEvents());
                 String userText = user.toString().substring(1, user.toString().length() - 1);
                 result.success(userText);
               } catch (JSONException e) {
                 e.printStackTrace();
               }
             }

             @Override
             public void onFailure(GeoSparkError geoSparkError) {
               geoSparkError.getMessage();
               geoSparkError.getCode();
             }
           });
           break;

         case METHOD_GET_LISTENER_STATUS:
           GeoSpark.getListenerStatus(new GeoSparkCallback() {
             @Override
             public void onSuccess(GeoSparkUser geoSparkUser) {
               JSONObject user = new JSONObject();
               try {
                 user.put("userId", geoSparkUser.getUserId());
                 user.put("events", geoSparkUser.getEventListenerStatus());
                 user.put("locations", geoSparkUser.getLocationListenerStatus());
                 user.put("locationEvents", geoSparkUser.getLocationEvents());
                 user.put("geofenceEvents", geoSparkUser.getGeofenceEvents());
                 user.put("tripsEvents", geoSparkUser.getTripsEvents());
                 user.put("movingGeofenceEvents", geoSparkUser.getMovingGeofenceEvents());
                 String userText = user.toString().substring(1, user.toString().length() - 1);
                 result.success(userText);
               } catch (JSONException e) {
                 e.printStackTrace();
               }
             }

             @Override
             public void onFailure(GeoSparkError geoSparkError) {
               geoSparkError.getMessage();
               geoSparkError.getCode();
             }
           });
           break;

         case METHOD_LOGOUT_USER:
           GeoSpark.logout(new GeoSparkLogoutCallback() {
             @Override
             public void onSuccess(String s) {

             }

             @Override
             public void onFailure(GeoSparkError geoSparkError) {
               geoSparkError.getMessage();
               geoSparkError.getCode();
             }
           });
           break;

         case METHOD_SUBSCRIBE_LOCATION:
           GeoSpark.subscribeLocation();
           break;

         case METHOD_SUBSCRIBE_USER_LOCATION:
           final String otherUserId = call.argument("userId");
           GeoSpark.subscribeUserLocation(otherUserId);
           break;

         case METHOD_SUBSCRIBE_EVENTS:
           GeoSpark.subscribeEvents();
           break;

         case METHOD_ENABLE_ACCURACY_ENGINE:
           GeoSpark.enableAccuracyEngine();
           break;
         case METHOD_DISABLE_ACCURACY_ENGINE:
           GeoSpark.disableAccuracyEngine();
           break;

         case METHOD_UPDATE_CURRENT_LOCATION:
           final Integer updateAccuracy = call.argument("accuracy");
           GeoSpark.updateCurrentLocation(GeoSparkTrackingMode.DesiredAccuracy.MEDIUM, updateAccuracy);
           break;

         case METHOD_START_TRACKING:
           final String trackingMode = call.argument("trackingMode");

           switch (trackingMode) {
             case TRACKING_MODE_PASSIVE:
               GeoSpark.startTracking(GeoSparkTrackingMode.PASSIVE);
               break;

             case TRACKING_MODE_REACTIVE:
               GeoSpark.startTracking(GeoSparkTrackingMode.REACTIVE);
               break;

             case TRACKING_MODE_ACTIVE:
               GeoSpark.startTracking(GeoSparkTrackingMode.ACTIVE);
               break;

             case TRACKING_MODE_CUSTOM:
               GeoSparkTrackingMode customTrackingMode;
               final Map customMethods = call.argument("customMethods");
               if(customMethods.containsKey("distanceInterval")){
                 final int distanceInterval = (int) customMethods.get("distanceInterval");
                 customTrackingMode = new GeoSparkTrackingMode.Builder(distanceInterval, 30).setDesiredAccuracy(GeoSparkTrackingMode.DesiredAccuracy.HIGH).build();
                 GeoSpark.startTracking(customTrackingMode);
               } else if(customMethods.containsKey("timeInterval")){
                 final int timeInterval = (int) customMethods.get("timeInterval");
                 customTrackingMode = new GeoSparkTrackingMode.Builder(timeInterval).setDesiredAccuracy(GeoSparkTrackingMode.DesiredAccuracy.HIGH).build();
                 GeoSpark.startTracking(customTrackingMode);
               } else {
                 customTrackingMode = new GeoSparkTrackingMode.Builder(15, 30).setDesiredAccuracy(GeoSparkTrackingMode.DesiredAccuracy.HIGH).build();
                 GeoSpark.startTracking(customTrackingMode);
               }
               break;

             default:
               break;
           }
           break;

         case METHOD_STOP_TRACKING:
           GeoSpark.stopTracking();
           break;

         case METHOD_CREATE_TRIP:
           final Boolean isOffline = call.argument("isOffline");
           GeoSpark.createTrip(null, null, isOffline, new GeoSparkCreateTripCallback() {
             @Override
             public void onSuccess(GeoSparkCreateTrip geoSparkCreateTrip) {
               JSONObject trip = new JSONObject();
               try {
                 trip.put("userId", geoSparkCreateTrip.getUser_id());
                 trip.put("tripId", geoSparkCreateTrip.getId());

                 String tripText = trip.toString().substring(1, trip.toString().length() - 1);
                 result.success(tripText);
               } catch (JSONException e) {
                 e.printStackTrace();
               }
             }

             @Override
             public void onFailure(GeoSparkError geoSparkError) {
               geoSparkError.getMessage();
               geoSparkError.getCode();
             }
           });
           break;

         case METHOD_GET_TRIP_DETAILS:
           final String tripId = call.argument("tripId");
           GeoSpark.getTripDetails(tripId, new GeoSparkTripDetailCallback() {
             @Override
             public void onSuccess(GeoSparkTripDetail geoSparkTripDetail) {
               JSONObject trip = new JSONObject();
               try {
                 trip.put("userId", geoSparkTripDetail.getUser_id());
                 trip.put("tripId", geoSparkTripDetail.getId());

                 String tripText = trip.toString().substring(1, trip.toString().length() - 1);
                 result.success(tripText);
               } catch (JSONException e) {
                 e.printStackTrace();
               }
             }

             @Override
             public void onFailure(GeoSparkError geoSparkError) {
               geoSparkError.getMessage();
               geoSparkError.getCode();
             }
           });
           break;

         case METHOD_GET_TRIP_STATUS:
           final String statusTripId = call.argument("tripId");
           GeoSpark.getTripStatus(statusTripId, new GeoSparkTripStatusCallback() {
             @Override
             public void onSuccess(GeoSparkTripStatus geoSparkTripStatus) {
               JSONObject trip = new JSONObject();
               try {
                 trip.put("distance", geoSparkTripStatus.getDistance());
                 trip.put("speed", geoSparkTripStatus.getSpeed());
                 trip.put("duration", geoSparkTripStatus.getDuration());
                 trip.put("tripId", geoSparkTripStatus.getTripId());

                 String tripText = trip.toString().substring(1, trip.toString().length() - 1);
                 result.success(tripText);
               } catch (JSONException e) {
                 e.printStackTrace();
               }
             }

             @Override
             public void onFailure(GeoSparkError geoSparkError) {
               geoSparkError.getMessage();
               geoSparkError.getCode();
             }
           });
           break;

         case METHOD_SUBSCRIBE_TRIP_STATUS:
           final String subscribeTripId = call.argument("tripId");
           GeoSpark.subscribeTripStatus(subscribeTripId);
           break;

         case METHOD_UNSUBSCRIBE_TRIP_STATUS:
           final String unSubscribeTripId = call.argument("tripId");
           GeoSpark.unSubscribeTripStatus(unSubscribeTripId);
           break;

         case METHOD_START_TRIP:
           final String startTripId = call.argument("tripId");
           GeoSpark.startTrip(startTripId, null, new GeoSparkTripCallback() {
             @Override
             public void onSuccess(String s) {

             }

             @Override
             public void onFailure(GeoSparkError geoSparkError) {
               geoSparkError.getMessage();
               geoSparkError.getCode();
             }
           });
           break;

         case METHOD_PAUSE_TRIP:
           final String pauseTripId = call.argument("tripId");
           GeoSpark.pauseTrip(pauseTripId, new GeoSparkTripCallback() {
             @Override
             public void onSuccess(String s) {

             }

             @Override
             public void onFailure(GeoSparkError geoSparkError) {
               geoSparkError.getMessage();
               geoSparkError.getCode();
             }
           });
           break;

         case METHOD_RESUME_TRIP:
           final String resumeTripId = call.argument("tripId");
           GeoSpark.resumeTrip(resumeTripId, new GeoSparkTripCallback() {
             @Override
             public void onSuccess(String s) {

             }

             @Override
             public void onFailure(GeoSparkError geoSparkError) {
               geoSparkError.getMessage();
               geoSparkError.getCode();
             }
           });
           break;

         case METHOD_END_TRIP:
           final String endTripId = call.argument("tripId");
           GeoSpark.stopTrip(endTripId, new GeoSparkTripCallback() {
             @Override
             public void onSuccess(String s) {

             }

             @Override
             public void onFailure(GeoSparkError geoSparkError) {
               geoSparkError.getMessage();
               geoSparkError.getCode();
             }
           });
           break;

         case METHOD_GET_TRIP_SUMMARY:
           final String summaryTripId = call.argument("tripId");
           GeoSpark.getTripSummary(summaryTripId, new GeoSparkTripSummaryCallback() {
             @Override
             public void onSuccess(GeoSparkTripSummary geoSparkTripSummary) {
               JSONObject trip = new JSONObject();
               try {
                 trip.put("distance", geoSparkTripSummary.getDistance_covered());
                 trip.put("duration", geoSparkTripSummary.getDuration());
                 trip.put("tripId", geoSparkTripSummary.getTrip_id());
                 trip.put("route", geoSparkTripSummary.getRoute());

                 String tripText = trip.toString().substring(1, trip.toString().length() - 1);
                 result.success(tripText);
               } catch (JSONException e) {
                 e.printStackTrace();
               }
             }

             @Override
             public void onFailure(GeoSparkError geoSparkError) {
               geoSparkError.getMessage();
               geoSparkError.getCode();
             }
           });
           break;

         case METHOD_DISABLE_BATTERY_OPTIMIZATION:
           GeoSpark.disableBatteryOptimization();
           break;

         default:
           result.notImplemented();
           break;
       }

     } catch (Error e) {
       result.error(e.toString(), e.getMessage(), e.getCause());
     }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    this.context = null;
    channel.setMethodCallHandler(null);
  }
}