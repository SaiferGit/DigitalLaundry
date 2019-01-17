package team.orion.androidcustomerapp;

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
import team.orion.androidcustomerapp.Common.Common;
import team.orion.androidcustomerapp.Model.User;

public class UserSetupActivity extends AppCompatActivity {

    private EditText UserUserName,UserFullName,UserPhoneNo,UserAddress;
    private Button UserSaveInformationButton;
    private CircleImageView UserprofileImage;
    private TextView UsertextView;
    private ProgressDialog loadingBar;



    private FirebaseAuth mAuth;
    private DatabaseReference UserRef,RootRef;
    private StorageReference UserImagesRef;

    String currentUserID;
    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserRef = FirebaseDatabase.getInstance().getReference().child(Common.user_customer_tbl).child(currentUserID);
        //UserImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images").child("Customers");

        UserUserName = (EditText) findViewById(R.id.user_setup_username);
        UserFullName = (EditText) findViewById(R.id.user_setup_fullname);
        UserPhoneNo = (EditText) findViewById(R.id.user_setup_phone_number);
        UserAddress = (EditText) findViewById(R.id.user_setup_address);
        UserSaveInformationButton = (Button) findViewById(R.id.user_setup_button);
        UserprofileImage = (CircleImageView) findViewById(R.id.user_setup_profile_image);
        UsertextView = (TextView) findViewById(R.id.user_setup_textview);
        loadingBar = new ProgressDialog(this);

        UserSaveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInformation();
            }
        });


    }

    private void SaveAccountSetupInformation() {
        final String username = UserUserName.getText().toString();
        final String fullname = UserFullName.getText().toString();
        final String phone = UserPhoneNo.getText().toString();
        final String address = UserAddress.getText().toString();


        if(TextUtils.isEmpty(username) ){
            UserUserName.requestFocus();
            UserUserName.setError("A Username is must required!");
            return;
        }

        else if(TextUtils.isEmpty(fullname) ){
            UserFullName.requestFocus();
            UserFullName.setError("A Full name is required to Continue!");
            return;
        }

        else if(TextUtils.isEmpty(phone) ){
            UserPhoneNo.requestFocus();
            UserPhoneNo.setError("How we will know it is you? type it now!");
            return;
        }

        else if(TextUtils.isEmpty(address) ){
            UserAddress.requestFocus();
            UserAddress.setError("From where to pickup you regular? Default Address will save your time!");
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

            UserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        SendUserToMainActivity();
                        User user = new User();
                        user.setFullname(fullname);
                        user.setUsername(username);
                        user.setAddress(address);
                        user.setPhoneNo(phone);

                        Toast.makeText(UserSetupActivity.this,"your are ready to gooo!", Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();

                    }
                    else{
                        String messege = task.getException().getMessage();
                        Toast.makeText(UserSetupActivity.this,"Attention: "+messege, Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();

                    }
                }
            });
        }



    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(UserSetupActivity.this, UserMainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
