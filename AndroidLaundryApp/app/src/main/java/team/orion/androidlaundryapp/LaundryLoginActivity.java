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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import team.orion.androidlaundryapp.Common.Common;
import team.orion.androidlaundryapp.Model.User;

public class LaundryLoginActivity extends AppCompatActivity {

    private Button LaundryloginButton;
    private EditText LaundryUserEmail, LaundryUserPass;
    private TextView LaundryNeedNewAccount;
    private ProgressDialog loadingBar;
    private Button GoogleSignInButton;


    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laundry_login);

        mAuth = FirebaseAuth.getInstance();

        LaundryloginButton =(Button) findViewById(R.id.laundry_login_button);
        LaundryUserEmail = (EditText) findViewById(R.id.laundry_login_mail);
        LaundryUserPass = (EditText) findViewById(R.id.laundry_login_pass);
        LaundryNeedNewAccount = (TextView) findViewById(R.id.laundry_login_edit_text);
        loadingBar = new ProgressDialog(this);
        GoogleSignInButton = (Button) findViewById(R.id.laundry_login_google_button);

        LaundryNeedNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent UserRegisterActivity = new Intent(LaundryLoginActivity.this, LaundryRegisterActivity.class);
                startActivity(UserRegisterActivity);
            }
        });

        LaundryloginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowingUserToLogin();
            }
        });
    }

    /*
    //when a user is already logged in then we don't need to check anything again.So,we will directly
    //send the user to main activity!
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currenUser = mAuth.getCurrentUser();

        if(currenUser != null){

            SendUserToMainActivity();
        }


    }
    */
    private void AllowingUserToLogin() {
        String email = LaundryUserEmail.getText().toString();
        String pass = LaundryUserPass.getText().toString();

        if(TextUtils.isEmpty(email) ){
            LaundryUserEmail.requestFocus();
            LaundryUserEmail.setError("This Field is required to Signin your account!");
            return;
        }

        else if(TextUtils.isEmpty(pass) ){
            LaundryUserPass.requestFocus();
            LaundryUserPass.setError("This Field is required to Signin your account!");
            return;
        }

        else{

            loadingBar.setTitle("Logging into Account");
            loadingBar.setMessage("Please wait,while we are Logging into your new Account..");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);


            mAuth.signInWithEmailAndPassword(email,pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                SendUserToMainActivity();
                                FirebaseDatabase.getInstance().getReference(Common.user_laundry_tbl)
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                Common.currentUser = dataSnapshot.getValue(User.class);
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                Toast.makeText(LaundryLoginActivity.this, "you are Loggedin Successfully!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                            }

                            else{
                                String messege = task.getException().toString();
                                Toast.makeText(LaundryLoginActivity.this, "Attention: "+messege, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                            }
                        }
                    });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LaundryLoginActivity.this, LaundryHome.class);
        startActivity(mainIntent);
        finish();
    }
}
