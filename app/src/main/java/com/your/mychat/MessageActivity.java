package com.your.mychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.your.mychat.common.Util;
/**
 *   MessageActivity was made to show the user
 *  that it is no internet Available
 *
 *  Created by Igor Ferbert 28.12.2020
 *
 * */
public class MessageActivity extends AppCompatActivity {
    private TextView _tvMessage;
    private ProgressBar _pbMessage;

    private ConnectivityManager.NetworkCallback networkCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        _tvMessage =findViewById(R.id.tv_message_activity_message);
        _pbMessage=findViewById(R.id.pb_progress_bar_activity_message);

        //Check SDK Version
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){

            //Initialisation the Callback for the internet connection available or lost
            networkCallback = new ConnectivityManager.NetworkCallback(){
                //Wenn Internet connection
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    finish();
                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    _tvMessage.setText(R.string.no_internet);
                }
            };
            //Register the Callback for ConnectivityManager
           ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);

            connectivityManager.registerNetworkCallback(new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build(),networkCallback);
        }

    }


    public void btnCloseMessageActivity(View view) {
        //THIS METHOD CLOSE ALL ACTIVITY THE APP
        finishAffinity();

    }

    public void btnRetryMessageActivity(View view) {
        _pbMessage.setVisibility(View.VISIBLE);
        if(Util. internetConnectionAvailable(this))
        {
            finish();
        }
        else
         {
            new android.os.Handler().postDelayed(() ->
            {
                _pbMessage.setVisibility(View.GONE);
            },1000);

         }
    }

}