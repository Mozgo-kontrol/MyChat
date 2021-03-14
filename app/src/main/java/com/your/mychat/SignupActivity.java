package com.your.mychat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.material.textfield.TextInputEditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.your.mychat.common.NodeNames;


import java.io.IOException;
import java.util.HashMap;

/**
 *
 * Created by Igor Ferbert 30.10.2020
 * */
public class SignupActivity extends AppCompatActivity {

    private final String  TAG = "SignupActivity";

    private TextInputEditText _etEmail, _etName, _etPassword, _etConfirmPassword;
    private String _email, _name, _password, _confirmPassWord;
    private ImageView _ivProfile;
    private Uri _localFileUri, _serverFileUri;
    private Bitmap _userPhoto;

    private  FirebaseUser _firebaseUser;
    private DatabaseReference _databaseReference;
    private FirebaseAuth _firebaseAuth;
    // Reference for Storage
    private StorageReference _fileStorage;

    //A View for progressBar
    private View _progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        _etEmail = findViewById(R.id.etEmail_signup_activity);
        _etName = findViewById(R.id.etName_signup_activity);
        _etPassword = findViewById(R.id.etPassword_signup_activity);
        _etConfirmPassword = findViewById(R.id.etConfirm_Password_sign_activity);
        //The photo of user
        _ivProfile = findViewById(R.id.iv_signup_activity);


        _firebaseAuth = FirebaseAuth.getInstance();
        //initial the Storage reference
        _fileStorage = FirebaseStorage.getInstance().getReference();
        //A View for progressBar
        _progressBar=findViewById(R.id.progressBar);
    }

    //------------------------------Permission-----------------------------------------------------
    /**
     * check user's permission
     *  for read from local Storage
     *
     * */
    private boolean usePermission(){
        String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        boolean result = false;
         result = ActivityCompat.checkSelfPermission(this, permission) == PackageManager
                 .PERMISSION_GRANTED;
        if (result)
        {
           return result;
        }
        else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 102);
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
        if(requestCode == 102){

            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 101);

            }
            else {
                Toast.makeText(this, R.string.access_permision_is_required,Toast.LENGTH_SHORT).show();
            }
        }

    }
    //--------------------------------Activity-for-result-----------------------------------------

    public void pickImageSignUpActivity(View v){

        if(usePermission()) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 101);
        }
        else
        Toast.makeText(this, R.string.access_permision_is_required,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Check requestCode
        if(requestCode == 101)
        {
            //Check user has picked some pictures from gallery
          if(resultCode == RESULT_OK)
          {

              //the picked picture was put in local variable from gallery
                 _localFileUri = data.getData();
              // I set the picture to the image view
                 PickedUserPhoto(_localFileUri);
                 _ivProfile.setImageBitmap(_userPhoto);

          }
        }

    }
    //----------------------------------------------------------------------------------------------
    /**
     *  PickedUserPhoto take user photo from Aktivityforresult
     *  an make from uri Bitmap
     *  compress it
     * @param uri
     *
     * */

    private void PickedUserPhoto(Uri uri){

        try {
             _userPhoto = compressImage(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri));
        }
        catch (IOException e){

            Log.w(TAG, "PickedUserPhoto(Uri uri): IOException", e);

        }
    }
    /**
     *   compress the photo
     **/
    private Bitmap compressImage(Bitmap bitmapImage) {
        int nh = (int) ( bitmapImage.getHeight() * (512.0 / bitmapImage.getWidth()) );
        Bitmap scaled = Bitmap.createScaledBitmap(bitmapImage, 512, nh, true);
        return scaled;
    }
    //----------------------------------------------------------------------------------------------


    public void btnSignupCklick(View v){
        _email =_etEmail.getText().toString();
        _name = _etName.getText().toString();
        _password =_etPassword.getText().toString();
        _confirmPassWord =_etConfirmPassword.getText().toString();

        if(_email.isEmpty()){

            _etEmail.setError("Please enter email id");
        }
        else if(_password.isEmpty()){
            _etPassword.setError("Please enter your password");
        }
        else if(_confirmPassWord.isEmpty()){
            _etPassword.setError("Please enter your password");
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(_email).matches()){
            _etEmail.setError("Please enter correct Email");
        }
        else if (!isPasswordValid(_password)){
            _etPassword.setError("Please enter password at least 5 characters");

        }
        else if (!_password.equals(_confirmPassWord)){
            _etConfirmPassword.setError("Please enter correct Password");
        }
        else {

            createAccount(_email, _password);

        }
    }

    /**
     * createAccount method takes in an email address and password,
     * validates them and then creates a new user
     * with the createUserWithEmailAndPassword method.
     *
     * */
    private void createAccount(String email, String password){

        _firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        _firebaseUser = _firebaseAuth.getCurrentUser();

                        // update UI with the signed-in user's information
                        updateUI(_firebaseUser);
                        Toast.makeText(this,"User is Successful",Toast.LENGTH_SHORT).show();

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "User is not created : failure", task.getException());

                        Toast.makeText(SignupActivity.this,getString(R.string.Sign_up_failed,
                                task.getException()), Toast.LENGTH_SHORT).show();

                    }
                });
    }


    private void updateUI(FirebaseUser user) {

        //if user has picked the photo
        if(_localFileUri!= null)
        {
          updateNameAndPhoto(user);
         }
        else updateName(user);
    }
    /**
     * Update user name and photo!
     *
     * */
    public void updateNameAndPhoto(FirebaseUser user) {

        String strFileName = user.getUid() + ".ipg";

        final StorageReference fileRef = _fileStorage.child("images/" + strFileName);

        _progressBar.setVisibility(View.VISIBLE);   //ProgressBar

        fileRef.putFile(_localFileUri).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                fileRef.getDownloadUrl().addOnSuccessListener(uri ->
                {
                    _serverFileUri = uri;
                    UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                            .setDisplayName(_etName.getText().toString().trim())
                            .setPhotoUri(_serverFileUri)
                            .build();

                    user.updateProfile(request).addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()){
                            //User get the ID
                            String userID = user.getUid();
                            //Create a tree in DataBase
                            _databaseReference = FirebaseDatabase.getInstance()
                                    .getReference().child(NodeNames.USERS);
                            //Create a tree in DataBase
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put(NodeNames.NAME, _etName.getText().toString().trim());
                            hashMap.put(NodeNames.EMAIL, _etEmail.getText().toString().trim());

                             hashMap.put(NodeNames.AGE, "18");
                             hashMap.put(NodeNames.SEX, "true");

                            hashMap.put(NodeNames.ONLINE, "true");
                            //with photo
                            hashMap.put(NodeNames.PHOTO, _serverFileUri.getPath());

                            //if  User tree is created successfully
                            _databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(task2 -> {
                                Log.d(TAG, getString(R.string.user_created_successfully));

                                _progressBar.setVisibility(View.GONE);  //ProgressBar

                                goToMainActivity();

                            });

                        }
                        else {
                            Toast.makeText(SignupActivity.this
                                    , getString(R.string.failed_to_update_username,task.getException()),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                });
            }
        });
    }


    private void goToMainActivity(){
        Intent intent =new Intent(SignupActivity.this, MainActivity.class);
        intent.putExtra("userName", _name);
        if(_localFileUri!=null)
        { intent.putExtra("userPhoto", _localFileUri);}
        startActivity(intent);
        finish();
    }

    /**
     * Update only user name!
     *
     * */
    public void updateName(FirebaseUser user) {
        //A Object to make an request for firebase
        _progressBar.setVisibility(View.VISIBLE);  //ProgressBar

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(_etName.getText().toString().trim())
                .build();
        //update user name



        user.updateProfile(request).addOnCompleteListener(task -> {

              if(task.isSuccessful()){
                  //User get the ID
                  String userID = user.getUid();
                  //Create a tree in DataBase
                  _databaseReference = FirebaseDatabase.getInstance()
                          .getReference().child(NodeNames.USERS);
                  //Create a tree in DataBase
                  HashMap<String, String> hashMap = new HashMap<>();
                  hashMap.put(NodeNames.NAME, _etName.getText().toString().trim());
                  hashMap.put(NodeNames.EMAIL, _etEmail.getText().toString().trim());

                  hashMap.put(NodeNames.AGE, "18");
                  hashMap.put(NodeNames.SEX, "2");

                  hashMap.put(NodeNames.ONLINE, "true");
                  hashMap.put(NodeNames.PHOTO, "");

                  //if  User tree is created successfully
                  _databaseReference.child(userID).setValue(hashMap).addOnCompleteListener(task1 -> {

                      _progressBar.setVisibility(View.GONE);  //ProgressBar

                      Log.i(TAG, getString(R.string.user_created_successfully));
                      goToMainActivity();

                  });

              }
              else {
                  Toast.makeText(SignupActivity.this
                          , getString(R.string.failed_to_update_username,task.getException()),
                          Toast.LENGTH_SHORT).show();
              }
        });
    }
    /**
     *  A placeholder password validation check
     * */
    private boolean isPasswordValid(String password) {
        boolean result = (password != null && password.trim().length() > 5);
        return result;
    }

}