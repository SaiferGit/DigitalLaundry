package team.orion.androidcustomerapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import team.orion.androidcustomerapp.Common.Common;

public class UserLoginActivity extends AppCompatActivity {

    private Button loginButton;
    private EditText userEmail, userPass;
    private TextView needNewAccount;
    private ProgressDialog loadingBar;
    private Button googleSignInButton;


    private FirebaseAuth mAuth;

    private static final int RC_SIGN_IN = 1;
    private GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        mAuth = FirebaseAuth.getInstance();

        loginButton =(Button) findViewById(R.id.user_login_button);
        userEmail = (EditText) findViewById(R.id.user_login_mail);
        userPass = (EditText) findViewById(R.id.user_login_pass);
        needNewAccount = (TextView) findViewById(R.id.user_login_edit_text);
        loadingBar = new ProgressDialog(this);
        googleSignInButton = (Button) findViewById(R.id.user_login_google_button);

        needNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowingUserToLogin();
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

    }

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

    //signIn method for google sign in
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {

            loadingBar.setTitle("Google Sign In");
            loadingBar.setMessage("Please wait,while we are Logging into your new Account using Google Account..");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
                Toast.makeText(UserLoginActivity.this, "Please wait while we are getting your Auth result..", Toast.LENGTH_SHORT).show();
            } catch (ApiException e) {

                String error = result.toString();
                Toast.makeText(UserLoginActivity.this, "Can't get Auth result.."+ error, Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            SendUserToMainActivity();
                            loadingBar.dismiss();

                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String messege = task.getException().toString();
                            SendUserToLoginActivity();
                            Toast.makeText(UserLoginActivity.this, "Attention: "+messege, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
    }


    private void AllowingUserToLogin() {
        String email = userEmail.getText().toString();
        String pass = userPass.getText().toString();

        if(TextUtils.isEmpty(email) ){
            userEmail.requestFocus();
            userEmail.setError("This Field is required to Sign in your account!");
            return;
        }

        else if(TextUtils.isEmpty(pass) ){
            userPass.requestFocus();
            userPass.setError("This Field is required to Sign in your account!");
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



                                Toast.makeText(UserLoginActivity.this, "you are Loggedin Successfully!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                            }

                            else{
                                String messege = task.getException().toString();
                                Toast.makeText(UserLoginActivity.this, "Attention: "+messege, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                            }
                        }
                    });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(UserLoginActivity.this, UserMainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToRegisterActivity() {
        Intent registerIntent = new Intent(UserLoginActivity.this, UserRegisterActivity.class);
        startActivity(registerIntent);
        //finish();
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(UserLoginActivity.this,UserLoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        //finish();
    }



}