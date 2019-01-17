package team.orion.androidcustomerapp;


import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import team.orion.androidcustomerapp.Model.FCMResponse;
import team.orion.androidcustomerapp.Model.Notification;
import team.orion.androidcustomerapp.Model.Sender;
import team.orion.androidcustomerapp.Model.Token;
import team.orion.androidcustomerapp.Remote.IFCMService;
import team.orion.androidcustomerapp.Common.Common;
import team.orion.androidcustomerapp.Helper.CustomInfoWindow;



/**
 * A simple {@link Fragment} subclass.
 */
public class FAQFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {


    public FAQFragment() {
        // Required empty public constructor
    }


    //Location
    private static final int MY_PERMISSION_REQUEST_CODE = 7192;
    private static final int PLAY_SERVICE_RES_REQUEST = 300193;

    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    FirebaseAuth mAuth;
    DatabaseReference userRef;
    GeoFire geoFire;
    String currentUserID;

    Marker mUserMarker;

    MaterialAnimatedSwitch location_switch;
    SupportMapFragment mapFragment;
    MapView mMapView;

    //BottomsheetView
    ImageView imgExpandable;
    BottomSheetUserFragment mBottomSheet;
    Button btnPickupRequest;

    private Boolean isLaundryFound = false;
    String laundryId = "";
    int radius = 1; // 1km
    int distance = 1;
    private static final int LIMIT = 3;

    //send Alert
    IFCMService mService;

    //presence system
    DatabaseReference driverAvailable;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)  {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_faq, container, false);


        //Geo fire
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference(Common.laundry_tbl);
        geoFire = new GeoFire(userRef);

        mService = Common.getFCMService();



        btnPickupRequest = v.findViewById(R.id.faq_btnPickupRequest);
        btnPickupRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isLaundryFound)
                    requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());
                else
                    sendRequestToLaundry(laundryId);
            }
        });

        SetUpLocation();

        updateFirebaseToken();

        return v;
    }



    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tbl);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(token);
    }

    private void sendRequestToLaundry(String laundryId) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);

        tokens.orderByKey().equalTo(laundryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapShot:dataSnapshot.getChildren()){
                            Token token = postSnapShot.getValue(Token.class);

                            String json_lat_lng = new Gson().toJson(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
                            String riderToken = FirebaseInstanceId.getInstance().getToken();

                            Notification data = new Notification(riderToken, json_lat_lng);
                            Sender content = new Sender(data,token.getToken());

                            mService.sendMessege(content)
                                    .enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            if(response.body().success == 1){
                                                Toast.makeText(getActivity(), "Request sent!!", Toast.LENGTH_LONG).show();
                                            }
                                            else
                                                Toast.makeText(getActivity(), "Request Failed!! please try again!", Toast.LENGTH_LONG).show();

                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                                            Log.e("ERROR", t.getMessage());
                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void requestPickupHere(String uid) {
        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl);
        GeoFire mGeoFire = new GeoFire(dbRequest);
        mGeoFire.setLocation(uid, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
        if(mUserMarker.isVisible())
            mUserMarker.remove();

        //Adding new Marker
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .title("Pickup Here")
                .snippet("")
                .position(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mUserMarker.showInfoWindow();
        btnPickupRequest.setText("Getting your Laundry...");

        findLaundry();
    }

    private void findLaundry() {
        DatabaseReference laundry = FirebaseDatabase.getInstance().getReference(Common.laundry_tbl);
        GeoFire gfLaundry = new GeoFire(laundry);

        GeoQuery geoQuery = gfLaundry.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude())
                ,radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //If found
                if(!isLaundryFound){
                    isLaundryFound = true;
                    laundryId = key;
                    btnPickupRequest.setText("Call Laundry");
                    Toast.makeText(getActivity(),""+key, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                //if still not found driver, increase distance
                if(!isLaundryFound){
                    radius = radius + 1;
                    findLaundry();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();

                    }
                }
        }
    }

    private void SetUpLocation() {
        //copy code from laundry map
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //Request runtime permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();

                /*if (location_switch.isChecked()) {

                }
                */
            }
        }
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {

            //presence system
            driverAvailable = FirebaseDatabase.getInstance().getReference(Common.laundry_tbl);
            driverAvailable.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //If have any change from Drivers table, we will reload all drivers available
                    loadAllAvailableLaundry(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            final double latitude = mLastLocation.getLatitude();
            final double longitude =  mLastLocation.getLongitude();



            loadAllAvailableLaundry(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

        } else {
            Log.d("ERROR", "Cannot get your Location");
        }

    }

    private void loadAllAvailableLaundry(final LatLng location) {

        //add marker
        mMap.clear();
        if (mUserMarker != null)
            mUserMarker.remove(); // Remove already marker
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                .position(location)
                .title(String.format("You")));

        //Move camera to this position
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));
        //draw animation rotate marker
        //rotateMarker(mCurrent, -360, mMap);

        //Load all available laundry
        DatabaseReference laundrylocation = FirebaseDatabase.getInstance().getReference(Common.laundry_tbl);
        GeoFire gf = new GeoFire(laundrylocation);

        GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()),
                distance);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                //use key to get email from table users
                //Table users is table when register account and update information
                //Just open your Laundry to check this table name
                FirebaseDatabase.getInstance().getReference(Common.user_laundry_tbl)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                //Because Laundry and User model is same


                                String phn = (String)  dataSnapshot.child("phoneNo").toString();
                                String name = (String) dataSnapshot.child("fullname").toString();
                                Log.d("ABC",  phn);
                                Log.d("ABC", name);

                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(location.latitude,location.longitude))
                                        .flat(true)
                                        .title(name)
                                        .snippet("Phone: "+phn)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(distance <= LIMIT){
                    //distance just find for 3km
                    distance++;
                    loadAllAvailableLaundry(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval((UPDATE_INTERVAL));
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCOde = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (resultCOde != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCOde)) {
                GooglePlayServicesUtil.getErrorDialog(resultCOde, getActivity(), PLAY_SERVICE_RES_REQUEST).show();
            }
            else {
                Toast.makeText(getActivity(), "This device is not supported", Toast.LENGTH_SHORT).show();
                //finish();
            }
            return false;

        }
        return true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMapView = (MapView) view.findViewById(R.id.faq_map);
        if(mMapView != null){
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        try{
            boolean isSuccess = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(getActivity(),R.raw.document)
            );
            if(!isSuccess)
                Log.e("ERROR","Map style load failed!!!");
        }catch (Resources.NotFoundException ex){
            ex.printStackTrace();

        }

        MapsInitializer.initialize(getContext());

        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(getActivity()));


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,  this);

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
        mLastLocation = location;
        displayLocation();
    }
}

