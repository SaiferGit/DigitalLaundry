package team.orion.androidcustomerapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import team.orion.androidcustomerapp.Common.Common;

public class UserMainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccesorsAdapter myTabAccessorAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private CircleImageView navProfileImage;
    private TextView navProfileUserName;

    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference(Common.user_customer_tbl);

        //adding toolbar in main activity
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Digital Laundry");

        //adding navigation view in drawer layout
        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout2);
        actionBarDrawerToggle = new ActionBarDrawerToggle(UserMainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        navProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        navProfileUserName = (TextView) navView.findViewById(R.id.nav_user_fullname);

        myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        myTabAccessorAdapter = new TabsAccesorsAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabAccessorAdapter);




        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("fullname"))
                {
                    String fullname = dataSnapshot.child("fullname").getValue().toString();
                    navProfileUserName.setText(fullname);
                }

                else
                {
                    Toast.makeText(UserMainActivity.this, "Profile name do not exists...", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //adding the left functionalities with button
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                UserMenuSelector(menuItem);

                return false;
            }
        });

        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
    }
    /*
    protected void onStart() {
        super.onStart();

        //current user authentication checking
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            SendUserToLoginActivity();
        }

        //checking either user has any database recorded for our two step verification
        else{
            CheckUserExistance();
        }

    }
    */
    private void CheckUserExistance() {
        final String current_user_id = mAuth.getCurrentUser().getUid();
        //referencing Fdatabase
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_user_id)){
                    SendUserToSetupActivity();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(UserMainActivity.this, UserSetupActivity.class);
        startActivity(setupIntent);
        finish();
    }

    private void UserMenuSelector(MenuItem menuitem) {
        switch (menuitem.getItemId()){

            case R.id.nav_home:
                Toast.makeText(this,"Home",Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_request_pickup:
                Toast.makeText(this,"Request pickup",Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_profile:
                Toast.makeText(this,"Profile",Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_contract:
                Toast.makeText(this,"Contract",Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_settings:
                Toast.makeText(this,"Settings",Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_prices:
                Toast.makeText(this,"prices",Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_faq:
                Toast.makeText(this,"FAQ's",Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_logout:
                mAuth.signOut();
                SendUserToLoginActivity();
                break;




        }

    }

    private void SendUserToLoginActivity() {

        Intent loginIntent = new Intent(UserMainActivity.this,UserLoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        //finish();
    }
}
