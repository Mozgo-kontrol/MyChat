package com.your.mychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.your.mychat.login.LoginActivity;

/**
 * Created by Igor Ferbert 26.10.2020
 *
 * */
public class MainActivity extends AppCompatActivity {
    private FirebaseAuth _firebaseAuth;
    private FirebaseUser _firebaseUser;
    private DatabaseReference _databaseReference;
    private String _username;
    private Uri _userPhoto;
    private String intentUserName;

    private SharedPreferences _SharedPreferences;

     Uri intentUserPhoto;

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
              _firebaseUser.getDisplayName();
            if (_firebaseUser.getPhotoUrl() != null) {
                _userPhoto =_firebaseUser.getPhotoUrl();
            }
        }

        // Get the intent sent from MainActivity.
        Intent intent = getIntent();
        // Parameter in Intent, sent from MainActivity
        intentUserName = intent.getStringExtra("userName");


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
            startActivity(new Intent(this, LoginActivity.class));
            finish();
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