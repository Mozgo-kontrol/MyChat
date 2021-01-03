package com.your.mychat.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.your.mychat.MainActivity;
import com.your.mychat.MessageActivity;
import com.your.mychat.R;
import com.your.mychat.SignupActivity;
import com.your.mychat.change.password.ResetPasswordActivity;
import com.your.mychat.common.Util;

import java.util.Objects;

/**
 * Created by Igor Ferbert 30.10.2020
 *
 **/
public class LoginActivity extends Activity {

    private final String TAG = "SignupActivity";
    private boolean isEmailverified = false;

    //local variable
    private TextInputEditText _etEmail, _etPassword;
    Button _btnLogin;
    private String _email, _password;

    //local variable Firebase
    private FirebaseAuth _mAuth;
    private FirebaseUser _user;

    //A View for progressBar
    private View _progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        _etEmail = findViewById(R.id.etEmail_login_activity);
        _etPassword = findViewById(R.id.etPassword_login_activity);
        _btnLogin = findViewById(R.id.btn_login_activity);

        //A View for progressBar
        _progressBar=findViewById(R.id.progressBar);
    }


    /**
     *  I verify with Click on the Button
     * und check Email and password for a user be FireBase
     * */

    public void btnLoginClick(View v) {
        _email = Objects.requireNonNull(_etEmail.getText()).toString();
        _password = Objects.requireNonNull(_etPassword.getText()).toString();

        if (_email.isEmpty()) {
            _etEmail.setError("Please enter email id");
            _etEmail.requestFocus();
        } else if (_password.isEmpty()) {
            _etPassword.setError("Please enter password");
            _etPassword.requestFocus();
        } else if (_email.isEmpty() && _password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Fields are Empty", Toast.LENGTH_SHORT)
                    .show();
        }
        else if (!isPasswordValid(_password)){
            _etPassword.setError("Please enter password at least 5 characters");

        }
        else if (!(_email.isEmpty() && _password.isEmpty())) {

            //when the internet connection available
            if(Util. internetConnectionAvailable(this))
            {
                _progressBar.setVisibility(View.VISIBLE);
                //Check Email and password for a user be FireBase
                signInWithEmailAndPassword(_email, _password);

            }
            else {
                    goToMessageActivity();
            }

        }


    }
    /** goToMessageActivity method
     *  if the internet is not available show the user Message NO INTERNET
     *
     */
    private void goToMessageActivity(){
        startActivity(new Intent(this, MessageActivity.class));
    }


    /** signIn method
    *  which takes in an email address and password, validates them,
    *  and then signs a user in with the signInWithEmailAndPassword method.
    */
    private void signInWithEmailAndPassword(String email, String password){
        _mAuth = FirebaseAuth.getInstance();
        _mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        _user = _mAuth.getCurrentUser();

                        updateUI(_user);

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();

                    }
                });
    }
    /**
     *   If User is
     * */

    private void updateUI(FirebaseUser currentUser) {
              //TODO must be implemented
        startActivity(new Intent(this, MainActivity.class));
        finish();
        _progressBar.setVisibility(View.GONE);
                }


    /**
     * If user is not  registered go to Sign Up Activity
     *
     * */
    public  void tvGoToSignUpClick(View v){
        startActivity(new Intent(LoginActivity.this, SignupActivity.class));
    }
    /**
     * If user is signed in send Email to Verification
     *    auth.setLanguageCode("fr");
     *  To apply the default app language instead of explicitly setting it.
     *     auth.useAppLanguage();
     * */
    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        isEmailverified = true;
                        Log.d(TAG, "Email for sendEmailVerification is sent!");
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

    public void resetPassword(View view) {

        startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));

    }
}