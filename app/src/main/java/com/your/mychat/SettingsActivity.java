package com.your.mychat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.your.mychat.change.password.ChangePasswordActivity;
import com.your.mychat.common.NodeNames;
import com.your.mychat.login.LoginActivity;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.lang.String.*;

/**
 * Created by Igor Ferbert 2.11.2020
 *
 *
 * */
public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ACTIVITY = 10222 ;
    private static final int REQUEST_CODE_PERMISSION = 10234 ;
    private final String TAG ="LOG";

    private TextInputEditText _etName, _etAge;

    private Spinner _sp_sex;
    private SwitchMaterial _switch_muteNotification;
    private ImageView _ivProfile;

    private String _name;
    private String _age;
    private String _sex;

    private boolean _preferenceMute;
    private Uri _localFileUri, _serverFileUri;

    // SharedPreferences
    SharedPreferences _mSettings;
    //Firebase
    private  FirebaseAuth _firebaseAuth;
    private  FirebaseUser _firebaseUser;
    private DatabaseReference _databaseReference;

    // Reference for Storage
    private StorageReference _fileStorage;

    private String _userId;

    //A View for progressBar
    private View _progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // initial UI
        _ivProfile = findViewById(R.id.iv_settings_activity);
        _etName = findViewById(R.id.etName_settings_activity);
        _etAge = findViewById(R.id.etAge_settings_activity);
        _sp_sex = findViewById(R.id.user_sex_settings_activity);
        _switch_muteNotification = findViewById(R.id.mute_notification_settings_activity);
        //A View for progressBar
        _progressBar = findViewById(R.id.progressBar);
        _progressBar.setVisibility(View.VISIBLE);
        //------------------------------------------------------------------------------------
        //initial firebase
        _firebaseAuth = FirebaseAuth.getInstance();
        _firebaseUser = _firebaseAuth.getCurrentUser();

         _userId = _firebaseUser.getUid();
        //initial the Storage reference
        _databaseReference = FirebaseDatabase.getInstance().getReference();
        //initial the Storage reference
        _fileStorage = FirebaseStorage.getInstance().getReference();

        // initial SharedPreferences
        //Switch Listener
        //-------------------------------------------------------------------------------------
        //update user information from firebase
        if (_firebaseUser!=null) {   //upload the name from server
            //_etName.setText(_firebaseUser.getDisplayName());
            _serverFileUri = _firebaseUser.getPhotoUrl();

            //upload the Photo from server if user sign up with the photo
            if (_serverFileUri != null) {

                Glide.with(this).load(_serverFileUri)
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .into(_ivProfile);

            }


        }

        _databaseReference.child(NodeNames.USERS).child(_userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name ;
                int userAge;
                boolean userSex;
                try {
                    name = String.valueOf(dataSnapshot.child(NodeNames.NAME).getValue());
                }
                catch (Exception e) {
                    name ="put your name";
                }
                _etName.setText(name);

                try {
                    userAge = Integer.parseInt(String.valueOf(dataSnapshot.child(NodeNames.AGE).getValue()));
                }
                catch (Exception e) {
                    userAge =  19;
                }
                _etAge.setText(String.valueOf(userAge));


                try {
                    userSex = Boolean.parseBoolean(String.valueOf(dataSnapshot.child(NodeNames.SEX).getValue()));
                    Log.d(TAG, "usersex : "+userSex);
                }
                catch (Exception e) {
                    userSex = true;
                }
                if(userSex){
                    _sp_sex.setSelection(1);
                    String s =(String) _sp_sex.getSelectedItem();
                    Log.d(TAG, "Spinner "+ s);
                }
                else _sp_sex.setSelection(0);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "databaseError: SettingsActivity onCreate");
            }
        });

        //Switch Listener
        _databaseReference.child(NodeNames.MUTE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                boolean b;
                try{
                     b = Boolean.parseBoolean(String.valueOf(dataSnapshot.child(_userId).getValue()));
                }
                catch (Exception e){
                    Log.d(TAG, "Mute Exception!");
                    b=false;
                }
                    _switch_muteNotification.setChecked(b);
                    Log.d(TAG, "isChecked "+b);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "databaseError");
            }
        });

        //Switch Listener
        _switch_muteNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            _switch_muteNotification.setChecked(isChecked);
            _preferenceMute = isChecked;

            if (_preferenceMute) {
                Log.d(TAG, "_switch_muteNotification isChecked");
            }
            _databaseReference.child(NodeNames.MUTE).child(_userId).setValue(isChecked);

        });
        _progressBar.setVisibility(View.GONE);//Progressbar
                // _mSettings = getSharedPreferences(NodeNames.APP_PREFERENCES, Context.MODE_PRIVATE);
                //SharedPreferences save the settings if their exist

    }

    @Override
    protected void onStart() {
        super.onStart();
        _progressBar.setVisibility(View.GONE);
    }

    //----------------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.menu_change_Pic)  {
            pickImageSettingsActivity();
        }
         if (item.getItemId()==R.id.menu_remove_Pic){
            removePhoto();
        }
        return true;
    }

     public void changeImage(View view)
     {

         pickImageSettingsActivity();

     }

    private void removePhoto() {
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(null)
                .build();
        _firebaseUser.updateProfile(request).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                //User get the ID
                String userID = _firebaseUser.getUid();
                //Create a tree in DataBase
                _databaseReference = FirebaseDatabase.getInstance()
                        .getReference().child(NodeNames.USERS);
                //Create a tree in DataBase
                HashMap<String, String> hashMap = new HashMap<>();

                hashMap.put(NodeNames.PHOTO, "");

                //if  User tree is removed successfully
                _databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(task1 -> {
                    Log.i(TAG,getString(R.string.user_photo_removed));

                    Glide.with(this).load(R.drawable.default_profile)
                            .into(_ivProfile);

                    Toast.makeText(SettingsActivity.this
                            ,getString( R.string.users_photo_is_removed_sussesfully),
                            Toast.LENGTH_SHORT).show();
                });

            }

            else {
                //if  User tree is not removed failure
                Toast.makeText(SettingsActivity.this
                        , getString(R.string.user_photo_remove_failure),
                        Toast.LENGTH_SHORT).show();
            }
        });

    }
    /**
     * Pick photo from gallery
     *
     * */

    public void pickImageSettingsActivity() {

       if (usePermission()) {

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE_ACTIVITY);
        }
        else
            Toast.makeText(this, R.string.access_permision_is_required,Toast.LENGTH_SHORT).show();

    }



    public void btnSave_settings_activity(View view) {

        String REGEX = "^[0-9]{2}$";
        String name =_etName.getText().toString();
        String age = _etAge.getText().toString();
        int userage = Integer.parseInt(_etAge.getText().toString());
        if (name.isEmpty()) {
            _etName.setError("Please enter name");
        }

        else if (name.length() < 3) {
            _etName.setError("Name must be at least 4 character");
        }

        else if (_etAge.getText().toString().isEmpty()) {
                _etAge.setError("Please, enter your age");
        }
        else if (!(_etAge.getText().toString().matches(REGEX))) {
            _etAge.setError("Please, enter valid age");
        }
        else if  (userage  < 18 &  userage  <= 99){
            _etAge.setError("Please, enter valid age");
        }
        else{
            Log.d(TAG, "Save button");
               _age = age;
               _name= name;
               _sex = convertSex ((String) _sp_sex.getSelectedItem());

            _progressBar.setVisibility(View.VISIBLE);

            if(_localFileUri!= null) {
                updateProfileFirebase(_firebaseUser,_localFileUri, _name, _age, _sex);
            }
            else updateProfileFirebaseWithOutPhoto(_firebaseUser, _name, _age, _sex);
        }

    }
    /**
     * updateProfileFirebase
     * @param  user - Represents a user's profile information in the Firebase project's user database.
     * @param localFileUri - local variable user's Photo, if he picked it
     * @param name - valid user name from Text view _etName
     * @param age - valid user age from Text view _etAge
     * @param sex - valid user sex from Spinner
     *
     * */
    public void updateProfileFirebase(FirebaseUser user, Uri localFileUri, String name , String age, String sex) {

        String strFileName = user.getUid() + ".ipg";

        final StorageReference fileRef = _fileStorage.child("images/" + strFileName);

        fileRef.putFile(localFileUri).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                fileRef.getDownloadUrl().addOnSuccessListener(uri ->
                {
                    _serverFileUri = uri;
                    //make request to update  user name und Photo
                    UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .setPhotoUri(_serverFileUri)
                            .build();
                    //update  user name und Photo
                    user.updateProfile(request).addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()){
                            //User get the ID
                            String userID = user.getUid();
                            //Create a tree in DataBase
                            _databaseReference = FirebaseDatabase.getInstance()
                                    .getReference().child(NodeNames.USERS);
                            //Create a data node to update Data in DataBase
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put(NodeNames.NAME, name);
                            hashMap.put(NodeNames.AGE, age);
                            hashMap.put(NodeNames.SEX, sex);
                            hashMap.put(NodeNames.ONLINE, "true");
                            //with photo
                            hashMap.put(NodeNames.PHOTO, _serverFileUri.getPath());
                            Set<Map.Entry<String, String>> entries = hashMap.entrySet();
                            for(Map.Entry<String, String> entry : entries)
                            {
                                _databaseReference.child(userID).child(entry.getKey()).setValue(entry.getValue()).addOnCompleteListener(task2 -> {
                                    Log.d(TAG, entry.getKey() + " " + entry.getValue()+" was successfully updated");
                                });
                            }

                            goToMainActivity();
                        }
                        else {
                            Toast.makeText(SettingsActivity.this
                                    , getString(R.string.failed_to_update_username,task.getException()),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                });
            }
        });
    }
    /**
     * updateProfileFirebaseWithOutPhoto
     * @param  user - Represents a user's profile information in the Firebase project's user database.
     * @param name - valid user name from Text view _etName
     * @param age - valid user age from Text view _etAge
     * @param sex - valid user sex from Spinner
     *
     * */
    public void updateProfileFirebaseWithOutPhoto(FirebaseUser user, String name , String age, String sex) {
        //A Object to make an request for firebase
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        //update user name

        user.updateProfile(request).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                //User get the ID
                String userID = user.getUid();
                //Create a tree in DataBase
                _databaseReference = FirebaseDatabase.getInstance()
                        .getReference().child(NodeNames.USERS);
                //Create a tree in DataBase
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(NodeNames.NAME, name);
                hashMap.put(NodeNames.AGE, age);
                hashMap.put(NodeNames.SEX, sex);
                Set<Map.Entry<String, String>> entries = hashMap.entrySet();

                for(Map.Entry<String, String> entry : entries)
                {
                    _databaseReference.child(userID).child(entry.getKey()).setValue(entry.getValue()).addOnCompleteListener(task1 -> {
                    Log.d(TAG, entry.getKey() + " " + entry.getValue()+" was successfully updated");
                });
                }

                goToMainActivity();

            }
            else {
                Toast.makeText(SettingsActivity.this
                        , getString(R.string.failed_to_update_username,task.getException()),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * convertSex
     *  convert user Sex
     * */
    private String convertSex(String sex) {
        String result = "false";
        if(sex.equals("Male")) {
           result = "true";
        }
        return result;
    }
    /**
     *  goToMainActivity
     *  with intent
     * */
    private void goToMainActivity(){
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

        _progressBar.setVisibility(View.GONE);

    }

    //------------------------------Permission-----------------------------------------------------
    /**
     * check user's permission
     *  for read from local Storage
     *
     * */
     private boolean usePermission(){
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        boolean result = ActivityCompat.checkSelfPermission(this, permission) == PackageManager
                .PERMISSION_GRANTED;
        if (result)
        {
            return result;
        }
        else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
        }
        return result;
    }

    /**
     * check result user's permission
     *
     *
     * */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_PERMISSION ){

            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE_ACTIVITY);

            }
            else {
                Toast.makeText(this, R.string.access_permision_is_required,Toast.LENGTH_SHORT).show();
            }
        }

    }
    //--------------------------------Activity-for-result-----------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Check requestCode
        if(requestCode == REQUEST_CODE_ACTIVITY)
        {
            //Check user has picked some pictures from gallery
            if(resultCode == RESULT_OK)
            {

                //the picked picture was put in local variable from gallery
                _localFileUri = data.getData();
                // I set the picture to the image view
                _ivProfile.setImageURI(_localFileUri);

            }
        }

    }

    public void tvGoToChangePasswordActivity(View view) {
        Intent intent = new Intent(SettingsActivity.this, ChangePasswordActivity.class);
        startActivity(intent);
    }
}