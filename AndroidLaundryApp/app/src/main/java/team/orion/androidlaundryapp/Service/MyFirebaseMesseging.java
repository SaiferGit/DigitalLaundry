package team.orion.androidlaundryapp.Service;

import android.content.Intent;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import team.orion.androidlaundryapp.CustomerCall;

public class MyFirebaseMesseging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //cuz i will send the FM with contain lat and lng from customer app
        //

        LatLng customer_location = new Gson().fromJson(remoteMessage.getNotification().getBody(),
                LatLng.class);
        Intent intent = new Intent(getBaseContext(), CustomerCall.class);
        intent.putExtra("lat", customer_location.latitude);
        intent.putExtra("lng", customer_location.longitude);
        intent.putExtra("customer", remoteMessage.getNotification().getTitle());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
}
