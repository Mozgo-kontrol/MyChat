package com.your.mychat.chats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.your.mychat.R;
import com.your.mychat.common.Constants;
import com.your.mychat.common.Extras;
import com.your.mychat.common.NodeNames;
import com.your.mychat.common.Util;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.provider.MediaStore.ACTION_IMAGE_CAPTURE;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private final String  TAG = "ChatActivity";

    private ImageView _ivSend, _ivAttachment, _ivProfile;
    private TextView _tvUserName;

    private EditText _etMessage;
    private DatabaseReference _mRootRef;
    private FirebaseAuth _firebaseAuth;
    private String _currentUserId, _chatUserId;

    private View _dialogView;
    private RecyclerView _rvMessages;
    private SwipeRefreshLayout _srlMessages;
    private MessagesAdapter _messagesAdapter;
    private List<MessageModel> _messagesModelList;

    private int _currentPage = 1;
    private static final int RECORD_PER_PAGE = 30;



    private DatabaseReference _databaseReferenceMessages;
    private ChildEventListener childEventListener;

    //dialog
    private BottomSheetDialog _bottomSheetDialog;

    //Activity for result
    private static final int REQUEST_CODE_PICK_IMAGE=101;
    private static final int REQUEST_CODE_CAPTURE_IMAGE=102;
    private static final int REQUEST_CODE_PICK_VIDEO=103;

    //Progressbar
    private LinearLayout _llProgress;

    //
    private String _userName,_photoName;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        _ivSend=findViewById(R.id.iv_send_chat_activity);
        _etMessage=findViewById(R.id.et_message_chat_activity);

        //custom actionbar in ChatActivity
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle("");
            View actionBarLayout =(ViewGroup)getLayoutInflater().inflate(R.layout.custom_actionbar,null);

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setElevation(0);

            actionBar.setCustomView(actionBarLayout);
            actionBar.setDisplayOptions(actionBar.getDisplayOptions()|ActionBar.DISPLAY_SHOW_CUSTOM);
        }
        //------------------------------------------------
        //custombar
        _tvUserName=findViewById(R.id.tvUserCustomBar);
        _ivProfile=findViewById(R.id.ivProfileCustoBar);
        //------------------------------------------------

        _ivSend.setOnClickListener(this);

          //open the dialog
        _ivAttachment = findViewById(R.id.iv_attachment);
        _ivAttachment.setOnClickListener(this);
        // -----------------------------------------------
        //Progressbar
        _llProgress=findViewById(R.id.llProgress);


        _firebaseAuth = FirebaseAuth.getInstance();
        _mRootRef = FirebaseDatabase.getInstance().getReference();
        _currentUserId = _firebaseAuth.getCurrentUser().getUid();

        if(getIntent().hasExtra(Extras.USER_KEY)){
          _chatUserId = getIntent().getStringExtra(Extras.USER_KEY);

        }
        if(getIntent().hasExtra(Extras.USER_NAME)){
            _userName=getIntent().getStringExtra(Extras.USER_NAME);
        }
        if(getIntent().hasExtra(Extras.USER_NAME)){
            _photoName=getIntent().getStringExtra(Extras.PHOTO_NAME);
        }
        _tvUserName.setText(_userName);

        if(!TextUtils.isEmpty(_photoName)) {
            StorageReference photoRef = FirebaseStorage.getInstance().getReference().child(Constants.IMAGES_FOLDER).child(_photoName);
            photoRef.getDownloadUrl().addOnSuccessListener(uri -> {

                Glide.with(ChatActivity.this)
                        .load(uri)
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .into(_ivProfile);
            });
        }

        _rvMessages=findViewById(R.id.rvMessages);
        _srlMessages=findViewById(R.id.srl_messages);
         _messagesModelList =new ArrayList<>();
         _messagesAdapter =new MessagesAdapter(this, _messagesModelList);
         _rvMessages.setLayoutManager(new LinearLayoutManager(this));
         _rvMessages.setAdapter(_messagesAdapter);

        //go to last position if the keyboard up
        _rvMessages.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                _rvMessages.post(() -> _rvMessages.scrollToPosition(
                        _messagesModelList.size()-1));
            }
        });

         loadMessages();
         _rvMessages.scrollToPosition(_messagesModelList.size()-1);
        _srlMessages.setOnRefreshListener(() -> {
            _currentPage++;
            loadMessages();
        });

        //dialog
        _bottomSheetDialog =new BottomSheetDialog(this);
        _dialogView = getLayoutInflater().inflate(R.layout.chat_file_options,null);
        _dialogView.findViewById(R.id.llCamera).setOnClickListener(this);
        _dialogView.findViewById(R.id.llGallery).setOnClickListener(this);
        _dialogView.findViewById(R.id.llVideo).setOnClickListener(this);
        _dialogView.findViewById(R.id.id_close).setOnClickListener(this);
        _bottomSheetDialog.setContentView( _dialogView);


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId=item.getItemId();
        switch (itemId)
            {
            case android.R.id.home:
                finish();
                break;

            default:
                break;
            }
        return super.onOptionsItemSelected(item);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
            switch (v.getId()){
                case R.id.iv_send_chat_activity:
                    //check if connection Available we can send message
                    if(Util.internetConnectionAvailable(this))
                    {
                        //generate  pushId
                    DatabaseReference userMessagePush =_mRootRef.child(NodeNames.MESSAGES).child(_currentUserId).child(_chatUserId).push();
                    String pushId=userMessagePush.getKey();

                    sendMessage(_etMessage.getText().toString(), Constants.MESSAGE_TYPE_TEXT, pushId);
                    }
                    else {Toast.makeText(ChatActivity.this,
                        getString(R.string.no_internet),
                        Toast.LENGTH_SHORT).show();}

                    break;
                //check the permission for user to open dialog
                case R.id.iv_attachment:
                    if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)
                    {
                        if(_bottomSheetDialog!=null)
                            _bottomSheetDialog.show();

                    }
                    else {ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);}


                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if(inputMethodManager != null){
                        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(),0);}
                    break;

                case R.id.llCamera:
                    _bottomSheetDialog.dismiss();
                    Intent intentCamera= new Intent(ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intentCamera,REQUEST_CODE_CAPTURE_IMAGE);
                    break;

                case R.id.llGallery:
                    _bottomSheetDialog.dismiss();
                    Intent intentImage= new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intentImage,REQUEST_CODE_PICK_IMAGE);
                    break;
                case R.id.llVideo:
                    _bottomSheetDialog.dismiss();
                    Intent intentVideo= new Intent(Intent.ACTION_PICK,
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intentVideo,REQUEST_CODE_PICK_VIDEO);
                    break;
                case R.id.id_close:

                    break;

            }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "1: requestCode: "+requestCode+"; resultCode: "+resultCode);
        if(resultCode==RESULT_OK){
            if(requestCode==REQUEST_CODE_CAPTURE_IMAGE){ //Camera

                Bitmap bitmap = (Bitmap)data.getExtras().get("data");
                ByteArrayOutputStream bytes =new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes);
                Log.d(TAG, "requestCode: "+requestCode+"; resultCode: "+resultCode);
                uploadBytes(bytes, Constants.MESSAGE_TYPE_IMAGE);
            }
            else if (requestCode==REQUEST_CODE_PICK_IMAGE){
                Log.d(TAG, "requestCode: "+requestCode+"; resultCode: "+resultCode);
                Uri uri = data.getData();
                uploadFile(uri, Constants.MESSAGE_TYPE_IMAGE);
            }
            else if (requestCode==REQUEST_CODE_PICK_VIDEO){
                Log.d(TAG, "requestCode: "+requestCode+"; resultCode: "+resultCode);
                Uri uri = data.getData();
                uploadFile(uri , Constants.MESSAGE_TYPE_VIDEO);
            }

        }

    }

    private void uploadFile(Uri uri, String messageType){

        DatabaseReference databaseReference =_mRootRef.child(NodeNames.MESSAGES).child(_currentUserId).child(_chatUserId).push();
        String pushId = databaseReference.getKey();


        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        String folderName = messageType.equals(Constants.MESSAGE_TYPE_VIDEO)? Constants.MESSAGE_VIDEOS:Constants.MESSAGE_IMAGES;
        String filename = messageType.equals(Constants.MESSAGE_TYPE_VIDEO)?pushId+".mp4": pushId+".jpg";

        StorageReference fileRef = storageReference.child(folderName).child(filename);

       // fileRef.putFile(uri);

        UploadTask uploadTask = fileRef.putFile(uri);
        uploadProgress(uploadTask, fileRef, pushId, messageType);
    }

    private void uploadProgress(UploadTask task, StorageReference filePath, String pushId, String messageType){

      View view = getLayoutInflater().inflate(R.layout.file_progress,null);

        ProgressBar progressBar = view.findViewById(R.id.pbProgress);
        TextView tvProgress=view.findViewById(R.id.tvfileProgress);
       final ImageView ivPlay=view.findViewById(R.id.iv_Play);
       final ImageView ivPause=view.findViewById(R.id.ivPause);
       final ImageView ivCancel=view.findViewById(R.id.iv_Cancel);

        ivPause.setOnClickListener(v -> {
            task.pause();
            ivPlay.setVisibility(View.VISIBLE);
            ivPause.setVisibility(View.GONE);
        });

        ivPlay.setOnClickListener(v -> {
            task.resume();
            ivPlay.setVisibility(View.GONE);
            ivPause.setVisibility(View.VISIBLE);
        });
        ivCancel.setOnClickListener(v -> task.cancel());


        _llProgress.addView(view);
        tvProgress.setText(getString(R.string.upload_progress, messageType, "0"));
        task.addOnProgressListener(snapshot -> {
            double progress =(100.0*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
            progressBar.setProgress((int)progress);
            tvProgress.setText(getString(R.string.upload_progress, messageType, String.valueOf(progressBar.getProgress())));
        });

        task.addOnCompleteListener(task1 -> {
            _llProgress.removeView(view);
            if(task1.isSuccessful()){

                filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                     String downloadURl =uri.toString();
                     sendMessage(downloadURl, messageType, pushId);
                });

            }
        });
        task.addOnFailureListener(e -> {
            _llProgress.removeView(view);
            Toast.makeText(ChatActivity.this, getString(R.string.failed_to_upload_the_file, e.getMessage()), Toast.LENGTH_SHORT).show();
        });

    }

    private void uploadBytes(ByteArrayOutputStream bytes, String messageType){

        DatabaseReference databaseReference =_mRootRef.child(NodeNames.MESSAGES).child(_currentUserId).child(_chatUserId).push();
        String pushId = databaseReference.getKey();


        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        String folderName = messageType.equals(Constants.MESSAGE_TYPE_VIDEO)? Constants.MESSAGE_VIDEOS:Constants.MESSAGE_IMAGES;
        String filename = messageType.equals(Constants.MESSAGE_TYPE_VIDEO)?pushId+".mp4": pushId+".jpg";

        StorageReference fileRef = storageReference.child(folderName).child(filename);
       //fileRef.putBytes(bytes.toByteArray());


        UploadTask uploadTask = fileRef.putBytes(bytes.toByteArray());

        uploadProgress(uploadTask, fileRef, pushId, messageType);

    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1){
            if(grantResults.length>0&& grantResults[0]==PackageManager.PERMISSION_GRANTED){

                if(_bottomSheetDialog!=null)
                    _bottomSheetDialog.show();
                else {
                    Toast.makeText(this, getString(R.string.permission_required_to_access), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void sendMessage(String msg, String msgType, String pushId)
    {
        try {
            if(!msg.equals("")){
                HashMap <String, Object> messageMap = new HashMap<>();

                messageMap.put(NodeNames.MESSAGE_ID, pushId);
                messageMap.put(NodeNames.MESSAGE, msg);
                messageMap.put(NodeNames.MESSAGE_TYPE, msgType);
                messageMap.put(NodeNames.MESSAGE_FROM,_currentUserId);
                messageMap.put(NodeNames.MESSAGE_TIME, ServerValue.TIMESTAMP);
                 //track to current user
                String currentUserRef = NodeNames.MESSAGES+"/"+_currentUserId+"/"+_chatUserId;
                 //track to chatUser user
                String chatUserRef = NodeNames.MESSAGES+"/"+_chatUserId+"/"+_currentUserId;

                HashMap <String, Object> messageUserMap = new HashMap<>();

                messageUserMap.put(currentUserRef+"/"+pushId, messageMap);
                messageUserMap.put(chatUserRef+"/"+pushId, messageMap);

                _etMessage.setText("");

                _mRootRef.updateChildren(messageUserMap, (error, ref) -> {
                    if(error!=null)
                    {
                        Toast.makeText(ChatActivity.this,
                                getString(R.string.failed_to_send_message, error.getMessage()),
                                Toast.LENGTH_SHORT).show();
                    }
                    else  Toast.makeText(ChatActivity.this,
                            getString(R.string.send_succesfully), Toast.LENGTH_SHORT).show();
                });

            }

        }catch (Exception ex){

            Toast.makeText(ChatActivity.this,
                    getString(R.string.failed_to_send_message, ex.getMessage()),
                    Toast.LENGTH_SHORT).show();

        }


    }

    private void loadMessages(){
        _messagesModelList.clear();
        _databaseReferenceMessages =_mRootRef.child(NodeNames.MESSAGES).child(_currentUserId).child(_chatUserId);

        Query messageQuery =_databaseReferenceMessages.limitToLast(_currentPage*RECORD_PER_PAGE);

        if(childEventListener!=null){
            messageQuery.removeEventListener(childEventListener);
        }

             childEventListener =new ChildEventListener() {
                 @Override
                 public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                     MessageModel message = snapshot.getValue(MessageModel.class);

                     _messagesModelList.add(message);
                     _messagesAdapter.notifyDataSetChanged();

                     _rvMessages.scrollToPosition(_messagesModelList.size()-1);

                        _srlMessages.setRefreshing(false);

                 }

                 @Override
                 public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                 }

                 @Override
                 public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                 }

                 @Override
                 public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                 }

                 @Override
                 public void onCancelled(@NonNull DatabaseError error) {
                   _srlMessages.setRefreshing(false);
                 }
             };
        messageQuery.addChildEventListener(childEventListener);
    }

}