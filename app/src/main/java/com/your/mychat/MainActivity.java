package com.your.mychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.your.mychat.adapter.Adapter;
import com.your.mychat.common.NodeNames;
import com.your.mychat.common.Util;
import com.your.mychat.login.LoginActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.your.mychat.R.string.request_sent_successfully;
import static com.your.mychat.R.string.something_went_wrong;

/**
 * Created by Igor Ferbert 26.10.2020
 *
 * */
public class MainActivity extends AppCompatActivity {


    private TabLayout _tabLayout;
    private ViewPager _viewPager;

    private SharedPreferences SysSharedPreferences;
    private SharedPreferences.Editor editor;

    private FirebaseAuth _firebaseAuth;
    private FirebaseUser _firebaseUser;
    private DatabaseReference _databaseReference;
    private String _username;
    private Uri _userPhoto;
    private String intentUserName;
    private SharedPreferences _SharedPreferences;
    private int lastday;
     Uri intentUserPhoto;

     // for onBackPressed method
    private boolean _doubleBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        _SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Initialize Firebase Auth
        _firebaseAuth = FirebaseAuth.getInstance();
        _firebaseUser =   _firebaseAuth.getCurrentUser();
        if (_firebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        else {

            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> {
                Util.updateDeviceToken(MainActivity.this, instanceIdResult.getToken());
            });

              _firebaseUser.getDisplayName();
            if (_firebaseUser.getPhotoUrl() != null) {
                _userPhoto =_firebaseUser.getPhotoUrl();
            }
        }

        // Get the intent sent from MainActivity.
        Intent intent = getIntent();
        // Parameter in Intent, sent from MainActivity
        intentUserName = intent.getStringExtra("userName");

        //init UI TabLayout
        _tabLayout=findViewById(R.id.tabMain);
        _viewPager=findViewById(R.id.vpMain);


        //Status user offline and online
        DatabaseReference databaseReferenceUsers = FirebaseDatabase.getInstance().getReference()
                .child(NodeNames.USERS).child(_firebaseUser.getUid());
        databaseReferenceUsers.child(NodeNames.ONLINE).setValue(true);
        databaseReferenceUsers.child(NodeNames.ONLINE).onDisconnect().setValue(false);


        //init Adapter and viewPager
        set_viewPager();

    }
    //-----------------------------------------------------------------------------------------------
    /**
     * set_viewPager is a method to work with Tablayout and Adapter
     *
     * */
    private void set_viewPager(){

        _tabLayout.addTab(_tabLayout.newTab().setCustomView(R.layout.tab_chat));
        _tabLayout.addTab(_tabLayout.newTab().setCustomView(R.layout.tab_requests));
        _tabLayout.addTab(_tabLayout.newTab().setCustomView(R.layout.tab_find_friends));

        _tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);


        Adapter adapter = new Adapter(getSupportFragmentManager()
                ,FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
                ,_tabLayout);

        _viewPager.setAdapter(adapter);

        _tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                _viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        _viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(_tabLayout));
    }

   //---------------------------------------------------------------------------------------------


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(_tabLayout.getSelectedTabPosition() > 0)
        {

            _tabLayout.selectTab(_tabLayout.getTabAt(0));

        }
        else
        {
            if (_doubleBackPressed)
            { finishAffinity();}

            else {
                _doubleBackPressed = true;
              Toast.makeText(this, getString(R.string.press_back_again)
                      ,Toast.LENGTH_SHORT).show();
                 //wait 2 seconds to press Back
                  android.os.Handler handler =new android.os.Handler();
                  handler.postDelayed((Runnable) () -> _doubleBackPressed = false,2000);

            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.menu_sign_out) {
            _firebaseAuth.signOut();
            //mSignInClient.signOut();
            //mUsername = ANONYMOUS;

            //Delete Notification token
            final FirebaseAuth firebaseAuth =FirebaseAuth.getInstance();
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            DatabaseReference rootRef= FirebaseDatabase.getInstance().getReference();
            DatabaseReference databaseReference = rootRef.child(NodeNames.TOKENS).child(currentUser.getUid());
            databaseReference.setValue(null).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }
                else  Toast.makeText(MainActivity.this, getString(something_went_wrong, task.getException() ), Toast.LENGTH_SHORT).show();
            });


          /*  mDatabase.child("users").child(currentUserAuth.getUid()).child("isUserOnline").setValue(false);
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            MApplication.makeToast(getResources().getString(R.string.signed_out), MainActivity.this);
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        }
                    });*/
        }
        if (item.getItemId() == R.id.menu_settings){
            settingsActivity();
        }
        if (item.getItemId() == R.id.menu_search) {
           // serachActivity();
        }
        return true;
    }
    private void settingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

}