package com.your.mychat.change.password;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.your.mychat.R;

/**
 *
 *
 *
 *  Created by Igor Ferbert 27.12.2020
 *
 */
public class ChangePasswordActivity extends AppCompatActivity {

    private final String  TAG = " ChangePasswordActivity";
    private TextInputEditText  _etCurrentPassword, _etPassword, _etConfirmPassword;
    private String  _password, _confirmPassWord, _currentPassword;
    private Button _btnChangePassword,_bntPutOldPassword;
    private TextInputLayout _txtInput_confirm_password,_txtInput_new_password, _txtInput_current_password;
    private  FirebaseUser _firebaseUser;

    // Reference for Storage
    private FirebaseAuth _firebaseAuth;

    //A View for progressBar
    private View _progressBar;
    /**
     * onCreate method initialise all parameters
     *for UI
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        //Firebase instants to work with the data
        _firebaseAuth = FirebaseAuth.getInstance();
        _firebaseUser = _firebaseAuth.getCurrentUser();
        //fields to work with the text
        _etPassword = findViewById(R.id.etPassword_ch_password_activity);
        _etConfirmPassword = findViewById(R.id.etConfirm_Password_ch_password_activity);
        _etCurrentPassword = findViewById(R.id.etOldPassword_ch_password_activity);
        //buttoms to work with visibility
        _btnChangePassword = findViewById(R.id.btn_ch_password_activity);
        _bntPutOldPassword = findViewById(R.id.btn_ch_old_password_activity);
        //Text Input blocks
        _txtInput_current_password = findViewById(R.id._current_pass_form_ch_password_activity);
        _txtInput_confirm_password = findViewById(R.id._confirm_pass_form_ch_password_activity);
        _txtInput_new_password = findViewById(R.id._new_pass_form_ch_password_activity);
        //A View for progressBar
        _progressBar=findViewById(R.id.progressBar);
    }
    /**
     * Step 1
     * btnPutOldPassword method works with
     * OnClickListener for Button CONTINUE
     * to check the old (current) password from User
     * and then call the reauthoriseUser method
     * */
    public void btnPutOldPassword(View view) {
        _currentPassword = _etCurrentPassword.getText().toString().trim();
        if(_currentPassword.isEmpty()){
            _etCurrentPassword.setError("Please enter your password");
        }
        else if(!isPasswordValid(_currentPassword)){
            _etCurrentPassword.setError("Please enter your valid password");
        }
        else
        {
            _firebaseAuth = FirebaseAuth.getInstance();
            _firebaseUser = _firebaseAuth.getCurrentUser();
            if(_firebaseUser!=null)
            {
                reauthoriseUser(_firebaseUser, _currentPassword);
            }
        }
    }
    /**
     * Step 2
     * Check a new user password if it valid and not null
     *
     * */
    public void btnChangePassword(View v){

        _password =_etPassword.getText().toString().trim();
        _confirmPassWord =_etConfirmPassword.getText().toString().trim();

        if(_password.isEmpty()){
            _etPassword.setError("Please enter your password");
        }
        else if(_confirmPassWord.isEmpty()){
            _etPassword.setError("Please enter your confirm password");
        }
        else if (!isPasswordValid(_password)){
            _etPassword.setError("Please enter password at least 5 characters");

        }
        else if (!_password.equals(_confirmPassWord)){
            _etConfirmPassword.setError("Your password isn't equals confirm password!");
        }
        else {

            if(_firebaseUser!=null)
            {
                _progressBar.setVisibility(View.VISIBLE); //ProgressBar
                updatePassword(_firebaseUser, _confirmPassWord);
            }
            else Log.d(TAG, "User is null! updatePassword");

        }
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    /**
     * the Update user password in the firebase
     * and go back to the SettingsActivity
     *
     * */
    private void updatePassword(FirebaseUser user, String newPassword) {

           user.updatePassword(newPassword).addOnCompleteListener(task -> {

               _progressBar.setVisibility(View.GONE); //ProgressBar

               if (task.isSuccessful())
               {
                   Toast.makeText(ChangePasswordActivity.this
                           ,getString(R.string.your_pass_was_changed),
                           Toast.LENGTH_SHORT).show();
                   Log.d(TAG, "User password updated.");
                   finish();
               }
               else{
                   Log.d(TAG, "User password isn't updated! -> Fail updatePassword");
                   Toast.makeText(ChangePasswordActivity.this
                       , getString(R.string.something_went_wrong, task.getException()),
                       Toast.LENGTH_SHORT).show();}
           });
    }
    /**
     * reauthoriseUser method makes re-authorisation the user
     * (It is very necessary for firebase before we make the user password update)
     *
     * */
    private void reauthoriseUser(FirebaseUser user, String currentPassword) {
        String email = user.getEmail();
        _firebaseAuth.signInWithEmailAndPassword(email, currentPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");

                        uiUpdate ();

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        _etCurrentPassword.setError("The password is wrong!");

                    }
                });
    }

    private void uiUpdate (){

        _txtInput_current_password.setVisibility(View.GONE);
        _txtInput_new_password.setVisibility(View.VISIBLE);
        _txtInput_confirm_password.setVisibility(View.VISIBLE);
        _bntPutOldPassword.setVisibility(View.GONE);
        _btnChangePassword.setVisibility(View.VISIBLE);

    }

}