package com.your.mychat.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;


public class MyDialogFragment extends AppCompatDialogFragment {

    private String _email ="";

   public MyDialogFragment (String email){

       this._email=email;

   }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = "The reset password instructions has been sent on your email:  ";
      //  String message = "The reset password instructions has been sent on your email:   ";
        String button1String = "Ok";
        String button2String = "Retry";

        TextView textView = new TextView(getActivity());
            textView.setText(title +_email);
            textView.setTextSize(18.0F);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setGravity(Gravity.CENTER);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setTitle(title)  // Title
        .setCustomTitle(textView)
       // builder.setMessage(message+ _email); //body
        .setPositiveButton(button1String, (dialog, id) ->

                getActivity().finish())

        .setNegativeButton(button2String, (dialog, id) ->

                dialog.cancel());

        return builder.create();
    }
}
