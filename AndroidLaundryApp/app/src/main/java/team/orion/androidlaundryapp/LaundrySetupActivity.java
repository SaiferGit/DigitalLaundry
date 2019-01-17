package team.orion.androidlaundryapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import team.orion.androidlaundryapp.Common.Common;

public class LaundrySetupActivity extends AppCompatActivity {

    private EditText LaundryUserName, LaundryFullName, LaundryPhoneNo, LaundryAddress;
    private Button LaundrySaveInformationButton;
    private CircleImageView LaundryprofileImage;
    private TextView LaundrytextView;
    private ProgressDialog loadingBar;



    private FirebaseAuth mAuth;
    private DatabaseReference UserRef,RootRef;
    private StorageReference UserImagesRef;

    String currentUserID;
    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laundry_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserRef = FirebaseDatabase.getInstance().getReference().child(Common.user_laundry_tbl).child(currentUserID);
        UserImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images").child("Laundry");

        LaundryUserName = (EditText) findViewById(R.id.laundry_setup_username);
        LaundryFullName = (EditText) findViewById(R.id.laundry_setup_fullname);
        LaundryPhoneNo = (EditText) findViewById(R.id.laundry_setup_phone_number);
        LaundryAddress = (EditText) findViewById(R.id.laundry_setup_address);
        LaundrySaveInformationButton = (Button) findViewById(R.id.laundry_setup_button);
        LaundryprofileImage = (CircleImageView) findViewById(R.id.laundry_setup_profile_image);
        LaundrytextView = (TextView) findViewById(R.id.laundry_setup_textview);
        loadingBar = new ProgressDialog(this);

        LaundrySaveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInformation();
            }
        });
    }

    private void SaveAccountSetupInformation() {
        String username = LaundryUserName.getText().toString();
        String fullname = LaundryFullName.getText().toString();
        String phone = LaundryPhoneNo.getText().toString();
        String address = LaundryAddress.getText().toString();


        if(TextUtils.isEmpty(username) ){
            LaundryUserName.requestFocus();
            LaundryUserName.setError("A Username is must required!");
            return;
        }

        else if(TextUtils.isEmpty(fullname) ){
            LaundryFullName.requestFocus();
            LaundryFullName.setError("A Full name is required to Continue!");
            return;
        }

        else if(TextUtils.isEmpty(phone) ){
            LaundryPhoneNo.requestFocus();
            LaundryPhoneNo.setError("How we will know it is you? type it now!");
            return;
        }

        else if(TextUtils.isEmpty(address) ){
            LaundryAddress.requestFocus();
            LaundryAddress.setError("From where to pickup you regular? Default Address will save your time!");
            return;
        }
        /*else if(!profileImage.isSelected()){
            //profileImage.requestFocus();
            Toast.makeText(SetupActivity.this,"Please set a profile image first!", Toast.LENGTH_LONG).show();
            //return;
        }*/

        else{

            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please wait,while we are creating your new Account completely..");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            //inserting info into Fdb
            HashMap userMap = new HashMap();
            userMap.put("username",username);
            userMap.put("fullname",fullname);
            userMap.put("phoneNo",phone);
            userMap.put("address",address);
            userMap.put("UID",currentUserID);
            userMap.put("adresstosend","please select");

            UserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        SendUserToMainActivity();

                        Toast.makeText(LaundrySetupActivity.this,"your are ready to gooo!", Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();

                    }
                    else{
                        String messege = task.getException().getMessage();
                        Toast.makeText(LaundrySetupActivity.this,"Attention: "+messege, Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();

                    }
                }
            });
        }



    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LaundrySetupActivity.this, LaundryHome.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
