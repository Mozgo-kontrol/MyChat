package com.your.mychat.change.password;


import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.your.mychat.R;
import com.your.mychat.login.LoginActivity;
/**
 *
 *
 *
 *  Created by Igor Ferbert 27.12.2020
 *
 */
public class ResetPasswordActivity extends AppCompatActivity {

    private final String  TAG = " ResetPasswordActivity";
    private TextInputEditText _etEmail;
    private TextView _tvMessage;
    private String _email;
    private LinearLayout _llResetPassword, _llMessage;
    private Button _btnRetry;

     //A View for progressBar
   //  private View _progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        _etEmail = findViewById(R.id.et_email_reset_password_activity);

        _llMessage = findViewById(R.id.ll_message_ch_password_activity);
        _llResetPassword = findViewById(R.id.ll_reset_password_activity);

        _tvMessage = findViewById(R.id.tv_massage_ch_password_activity);

        _btnRetry = findViewById(R.id.btn_retry_reset_password_activity);

        //A View for progressBar
        //_progressBar=findViewById(R.id.progressBar);
    }

    public void btnSendOnCklick(View view) {
        _email = _etEmail.getText().toString().trim();
        if(_email.isEmpty()){

            _etEmail.setError("Please enter email id");

        }

        else if(!Patterns.EMAIL_ADDRESS.matcher(_email).matches())
        {

            _etEmail.setError("Please enter correct Email");

        }
        else{

            sendResetPassword(_email);
        }

    }
    private void sendResetPassword(String emailAddress){

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        updateToMessage();
                        Timer();
                        Log.i(TAG, "Email sent!");
                    }
                    else   {
                        _tvMessage.setText(getString(R.string.email_sent_failed, task.getException()));
                        Log.i(TAG, "The Email was not sent Email!");

                    }
                });


    }


    private void Timer(){
         new CountDownTimer(60000,1000){

             @Override
             public void onTick(long l) {
                 _btnRetry.setText(getString(R.string.resend_timer, String.valueOf(l/1000)));
                 _btnRetry.setOnClickListener(null);
             }

             @Override
             public void onFinish() {
                 updateToReset();
             }
         }.start();

    }


    private void updateToMessage()
    {
        _llResetPassword.setVisibility(View.GONE);
        _llMessage.setVisibility(View.VISIBLE);
        _tvMessage.setText(getString(R.string.reset_password_instruction, _email));
    }

    private void updateToReset()
    {
        _btnRetry.setText(R.string.retry);

        _btnRetry.setOnClickListener(v -> {
            _llMessage.setVisibility(View.GONE);
            _llResetPassword.setVisibility(View.VISIBLE);
        });
    }

    public void btnOkPasswordReset(View view) {

        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    private void dialog(){

         //My case with the Dialog
        /*MyDialogFragment myDialogFragment = new MyDialogFragment(_email);
        FragmentManager manager = getSupportFragmentManager();
        myDialogFragment.show(manager, "dialog");
        */

    }


}