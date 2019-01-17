package team.orion.androidcustomerapp;


import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;


import team.orion.androidcustomerapp.Common.Common;
import team.orion.androidcustomerapp.Model.Token;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {



    private EditText elocation;
    private Button schedulePickup;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    String currentUser;

    public HomeFragment() {


        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();

        userRef = FirebaseDatabase.getInstance().getReference().child(Common.user_customer_tbl).child(currentUser);

        elocation = v.findViewById(R.id.home_pickup_address);
        schedulePickup = v.findViewById(R.id.btn_schedule_pickup);

        schedulePickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveUserLocation();


            }
        });


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

    private void SaveUserLocation() {
        String location = elocation.getText().toString();

        if(TextUtils.isEmpty(location) ){
            elocation.requestFocus();
            elocation.setError("location is required to know from where to pickup");
            return;
        }

        else{
            //inserting info into Fdb
            HashMap userMap = new HashMap();
            userMap.put("location",location);

            userRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Intent nextIntent = new Intent(getActivity(), UserRequestActivity.class);
                        startActivity(nextIntent);

                        Toast.makeText(getActivity(),"wait for the next step!", Toast.LENGTH_LONG).show();


                    }
                    else{
                        String messege = task.getException().getMessage();
                        Toast.makeText(getActivity(),"Attention: "+messege, Toast.LENGTH_LONG).show();


                    }
                }
            });
        }

    }
}
