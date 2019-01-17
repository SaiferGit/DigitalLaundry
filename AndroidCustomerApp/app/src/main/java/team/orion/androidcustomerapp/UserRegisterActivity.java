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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserRegisterActivity extends AppCompatActivity {

    private EditText UserUserEmail,UserUserPassword,UserUserConfirmPassword;
    private Button UserCreateAccountButton;
    private TextView UserAlreadyHaveAccount;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

        mAuth = FirebaseAuth.getInstance();

        UserAlreadyHaveAccount = (TextView) findViewById(R.id.user_register_textview);
        UserUserEmail = (EditText) findViewById(R.id.user_register_mail);
        UserUserPassword = (EditText) findViewById(R.id.user_register_pass);
        UserUserConfirmPassword = (EditText) findViewById(R.id.user_register_confirm_pass);
        UserCreateAccountButton = (Button) findViewById(R.id.user_register_button);
        loadingBar = new ProgressDialog(this);

        //when user has already an account but wanna go back for login
        UserAlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(UserRegisterActivity.this, UserLoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
            }
        });

        UserCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }





    private void CreateNewAccount() {

        String email = UserUserEmail.getText().toString();
        String pass = UserUserPassword.getText().toString();
        String confirmpass = UserUserConfirmPassword.getText().toString();

        if(TextUtils.isEmpty(email) ){
            UserUserEmail.requestFocus();
            UserUserEmail.setError("This Field is required to sign into your account!");
            return;
        }

        else if(TextUtils.isEmpty(pass) ){
            UserUserPassword.requestFocus();
            UserUserPassword.setError("This Field is required to secure your account");
            return;
        }

        else if (TextUtils.isEmpty(pass) ){
            UserUserConfirmPassword.requestFocus();
            UserUserConfirmPassword.setError("This Field is required to save your password!");
            return;

        }

        else if(!pass.equals(confirmpass)){
            UserUserConfirmPassword.requestFocus();
            UserUserConfirmPassword.setError("Your Passwords don't match!");
            return;

        }

        else{

            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait,while we are creating your new Account..");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            mAuth.createUserWithEmailAndPassword(email,pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                SendUserToSetupActivity();

                                Toast.makeText(UserRegisterActivity.this,"you are Authenticated successfully", Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                            else{
                                String messege = task.getException().getMessage();
                                Toast.makeText(UserRegisterActivity.this, "Attention: "+messege,Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                                return;

                            }
                        }
                    });

        }



    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(UserRegisterActivity.this, UserSetupActivity.class);
        startActivity(setupIntent);
        finish();
    }
}
