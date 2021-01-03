package com.your.mychat;

import android.Manifest;
import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

public class PickImageService extends IntentService {


    String name;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param PickImageService Used to name the worker thread, important only for debugging.
     */
    public PickImageService(String PickImageService, String s) {
        super(PickImageService);
        name = s;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {


    }


}
