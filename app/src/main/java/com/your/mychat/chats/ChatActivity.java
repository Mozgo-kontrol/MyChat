package com.your.mychat.chats;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.your.mychat.R;
import com.your.mychat.common.Constants;
import com.your.mychat.common.Extras;
import com.your.mychat.common.NodeNames;
import com.your.mychat.common.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView _ivSend;
    private EditText _etMessage;
    private DatabaseReference _mRootRef;
    private FirebaseAuth _firebaseAuth;
    private String _currentUserId, _chatUserId;

    private RecyclerView _rvMessages;
    private SwipeRefreshLayout _srlMessages;
    private MessagesAdapter _messagesAdapter;
    private List<MessageModel> _messagesModelList;

    private int _currentPage = 1;
    private static final int RECORD_PER_PAGE = 30;

    private DatabaseReference _databaseReferenceMessages;
    private ChildEventListener childEventListener;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        _ivSend=findViewById(R.id.iv_send_chat_activity);
        _etMessage=findViewById(R.id.et_message_chat_activity);

        _ivSend.setOnClickListener(this);

        _firebaseAuth = FirebaseAuth.getInstance();
        _mRootRef = FirebaseDatabase.getInstance().getReference();
        _currentUserId = _firebaseAuth.getCurrentUser().getUid();
        if(getIntent().hasExtra(Extras.USER_KEY)){

          _chatUserId=getIntent().getStringExtra(Extras.USER_KEY);

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
                    else Toast.makeText(ChatActivity.this,
                        getString(R.string.no_internet),
                        Toast.LENGTH_SHORT).show();

                    break;





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