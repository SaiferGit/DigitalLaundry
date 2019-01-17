package team.orion.androidlaundryapp;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import team.orion.androidlaundryapp.Common.Common;
import team.orion.androidlaundryapp.Helper.DirectionJSONParser;
import team.orion.androidlaundryapp.Model.FCMResponse;
import team.orion.androidlaundryapp.Model.Notification;
import team.orion.androidlaundryapp.Model.Sender;
import team.orion.androidlaundryapp.Model.Token;
import team.orion.androidlaundryapp.Remote.IFCMService;
import team.orion.androidlaundryapp.Remote.IGoogleAPI;

import static team.orion.androidlaundryapp.Common.Common.mLastLocation;

public class LaundryTrackingActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private GoogleMap mMap;
    double laundryLat, laundryLng;

    String customerId;

    //play services

    private static final int PLAY_SERVICE_RES_REQUEST = 7001;


    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;


    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    private Circle customerMarker;
    private Marker laundryMarker;
    private Polyline direction;

    IGoogleAPI mService;
    IFCMService mFCMService;

    GeoFire geoFire;

    Button btnStartTrip;
    Location pickUpLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laundry_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if(getIntent() != null){
            laundryLat = getIntent().getDoubleExtra("lat", -1.0);
            laundryLng = getIntent().getDoubleExtra("lng",-1.0);
            customerId = getIntent().getStringExtra("customerId");
        }
        mService = Common.getGoogleAPI();
        mFCMService = Common.getFCMService();


        setUpLocation();

        btnStartTrip = (Button)findViewById(R.id.laundry_traking_btnStartTrip);
        btnStartTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnStartTrip.getText().equals("START TRIP")){
                    pickUpLocation = Common.mLastLocation;
                    btnStartTrip.setText("DROP OFF HERE");
                }
            }
        });
    }

    private void setUpLocation() {

            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }

    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval((UPDATE_INTERVAL));
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCOde = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCOde != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCOde)) {
                GooglePlayServicesUtil.getErrorDialog(resultCOde, this, PLAY_SERVICE_RES_REQUEST).show();
            } else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;

        }
        return true;
    }


    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Common.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (Common.mLastLocation != null) {

                final double latitude = Common.mLastLocation.getLatitude();
                final double longitude = Common.mLastLocation.getLongitude();

                if(laundryMarker != null)
                    laundryMarker.remove();
                laundryMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude))
                                    .title("You")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker)));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude), 17.0f));

                if(direction != null)
                    direction.remove();//remove previous direction
                getDirection();
        } else {
            Log.d("ERROR", "Cannot get your Location");
        }

    }

    private void getDirection() {
        LatLng currentPosition = new LatLng(Common.mLastLocation.getLatitude(), mLastLocation.getLongitude());

        String requestApi = null;
        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" +
                    "destination=" +laundryLat+","+laundryLng + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);

            Log.d("Saif", requestApi); // Print URL for Debug
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                new ParserTask().execute(response.body().toString());


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(LaundryTrackingActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        try{
            boolean isSuccess = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this,R.raw.document)
            );
            if(!isSuccess)
                Log.e("ERROR","Map style load failed!!!");
        }catch (Resources.NotFoundException ex){
            ex.printStackTrace();

        }

        mMap = googleMap;

         customerMarker = mMap.addCircle(new CircleOptions()
        .center(new LatLng(laundryLat,laundryLng))
        .radius(50) // 50 => radius is 50m
        .strokeColor(Color.BLUE)
         .fillColor(0x220000FF)
         .strokeWidth(5.0f));

         //create Geo fencing with radius is 50m
        geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.laundry_tbl));
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(laundryLat,laundryLng), 0.05f);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //Because here we will need laundry ID to sent notification
                //so, we will pass it from previous activity(customer call)
                sendArrivedNotification(customerId);
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private void sendArrivedNotification(String customerId) {
        Token token = new Token(customerId);
        //we will send notification with title is Arrived and body is this string
        Notification notification = new Notification("Arrived", String.format("The driver 'atik' you called, has been arrived at your location",Common.currentUser));
        Sender sender = new Sender(notification,token.getToken());

        mFCMService.sendMessege(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if(response.body().success != 1){
                    Toast.makeText(LaundryTrackingActivity.this,"Failed", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Common.mLastLocation = location;
        displayLocation();
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {
        ProgressDialog mDialog = new ProgressDialog(LaundryTrackingActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Please Wait...");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try{
                jObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();
                routes = parser.parse(jObject);
            }catch (JSONException e){
                e.printStackTrace();
            }

            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();
            ArrayList points = null;
            PolylineOptions polylineOptions = null;

            for(int i = 0; i<lists.size();i++){
                points = new ArrayList();
                polylineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = lists.get(i);

                for(int j = 0; j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(Color.RED);
                polylineOptions.geodesic(true);

            }
            direction = mMap.addPolyline(polylineOptions);
        }
    }

}
