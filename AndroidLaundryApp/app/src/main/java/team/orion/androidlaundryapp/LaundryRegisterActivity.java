package team.orion.androidlaundryapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.provider.ContactsContract;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import team.orion.androidlaundryapp.Common.Common;

public class LaundryRegisterActivity extends AppCompatActivity {

    private EditText LaundryUserEmail,LaundryUserPassword,LaundryUserConfirmPassword;
    private Button LaundryCreateAccountButton;
    private TextView LaundryAlreadyHaveAccount;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laundry_register);

        mAuth = FirebaseAuth.getInstance();
        //currentUserID = mAuth.getCurrentUser().getUid();
        //UserRef = FirebaseDatabase.getInstance().getReference().child(Common.user_laundry_tbl).child(currentUserID);

        LaundryAlreadyHaveAccount = (TextView) findViewById(R.id.Laundry_register_textview);
        LaundryUserEmail = (EditText) findViewById(R.id.Laundry_register_mail);
        LaundryUserPassword = (EditText) findViewById(R.id.Laundry_register_pass);
        LaundryUserConfirmPassword = (EditText) findViewById(R.id.Laundry_register_confirm_pass);
        LaundryCreateAccountButton = (Button) findViewById(R.id.Laundry_register_button);
        loadingBar = new ProgressDialog(this);

        //when user has already an account but wanna go back for login
        LaundryAlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(LaundryRegisterActivity.this, LaundryLoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
            }
        });

        //when a user click's save button
        LaundryCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });

    }





    private void CreateNewAccount() {

        String email = LaundryUserEmail.getText().toString();
        String pass = LaundryUserPassword.getText().toString();
        String confirmpass = LaundryUserConfirmPassword.getText().toString();

        if(TextUtils.isEmpty(email) ){
            LaundryUserEmail.requestFocus();
            LaundryUserEmail.setError("This Field is required to sign into your account!");
            return;
        }

        else if(TextUtils.isEmpty(pass) ){
            LaundryUserPassword.requestFocus();
            LaundryUserPassword.setError("This Field is required to secure your account");
            return;
        }

        else if (TextUtils.isEmpty(pass) ){
            LaundryUserConfirmPassword.requestFocus();
            LaundryUserConfirmPassword.setError("This Field is required to save your password!");
            return;

        }

        else if(!pass.equals(confirmpass)){
            LaundryUserConfirmPassword.requestFocus();
            LaundryUserConfirmPassword.setError("Your Passwords don't match!");
            return;

        }

        else{

            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait,while we are creating your new Account..");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            //inserting info into Fdb
            HashMap userMap = new HashMap();
            userMap.put("email",email);
            userMap.put("pass",pass);

            /*
            UserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){

                        Toast.makeText(LaundryRegisterActivity.this,"Mail and pass collected!!", Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();

                    }
                    else{
                        String messege = task.getException().getMessage();
                        Toast.makeText(LaundryRegisterActivity.this,"Attention: "+messege, Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();

                    }
                }
            });
            */
            mAuth.createUserWithEmailAndPassword(email,pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                SendUserToSetupActivity();

                                Toast.makeText(LaundryRegisterActivity.this,"you are Authenticated successfully", Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                            else{
                                String messege = task.getException().getMessage();
                                Toast.makeText(LaundryRegisterActivity.this, "Attention: "+messege,Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                                return;

                            }
                        }
                    });

        }



    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(LaundryRegisterActivity.this, LaundrySetupActivity.class);
        startActivity(setupIntent);
        finish();
    }
}
