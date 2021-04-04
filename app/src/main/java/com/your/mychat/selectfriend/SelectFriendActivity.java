package com.your.mychat.selectfriend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.your.mychat.R;
import com.your.mychat.common.Extras;
import com.your.mychat.common.NodeNames;

import java.util.ArrayList;
import java.util.List;

public class SelectFriendActivity extends AppCompatActivity {


    private RecyclerView _rvSelectFriend;
    private SelectFriendAdapter _selectFriendAdapter;
    private List<SelectFriendModel> _selectFriendModelsList;
    private View _progressbar;

    private String _selectedMessage,_selectedMessageId,_selectedMessageType;
    private DatabaseReference _databaseReferenceUsers, _databaseReferenceChats;

    private FirebaseUser _currentUser;
    private ValueEventListener _valueEventListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);


        if(getIntent().hasExtra(Extras.SELECTED_MESSAGE)){
            _selectedMessage = getIntent().getStringExtra(Extras.SELECTED_MESSAGE);
            _selectedMessageId = getIntent().getStringExtra(Extras.SELECTED_MESSAGE_ID);
            _selectedMessageType = getIntent().getStringExtra(Extras.SELECTED_MESSAGE_TYPE);
        }

        _rvSelectFriend = findViewById(R.id.rvSelectFriend);
        _progressbar=findViewById(R.id.progressBar_sf);

        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(this);
       _rvSelectFriend.setLayoutManager(linearLayoutManager);

       _selectFriendModelsList = new ArrayList<>();
       _selectFriendAdapter = new SelectFriendAdapter(this, _selectFriendModelsList);
       _rvSelectFriend.setAdapter(_selectFriendAdapter);
       _progressbar.setVisibility(View.VISIBLE);

       _currentUser = FirebaseAuth.getInstance().getCurrentUser();
       _databaseReferenceChats = FirebaseDatabase.getInstance().getReference().child(NodeNames.CHATS).child(_currentUser.getUid());
       _databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);

       _valueEventListener= new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren())
                {
                    String userId = ds.getKey();
                    _databaseReferenceUsers.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String userName = snapshot.child(NodeNames.NAME).getValue()!=null ?
                                    (String) snapshot.child(NodeNames.NAME).getValue() : "" ;

                            SelectFriendModel friendModel = new SelectFriendModel(userId, userName, userId+".jpg");
                            _selectFriendModelsList.add(friendModel);
                            _selectFriendAdapter.notifyDataSetChanged();
                            _progressbar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(SelectFriendActivity.this, getString(R.string.failed_to_fetch_friends, error.getMessage()),Toast.LENGTH_SHORT).show();
                        }
                    });


                }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {
               Toast.makeText(SelectFriendActivity.this, getString(R.string.failed_to_fetch_friends, error.getMessage()),Toast.LENGTH_SHORT).show();
           }
       };
       _databaseReferenceChats.addValueEventListener(_valueEventListener);
    }
    public void returnSelectedFriend(String userId,String userName,String photoName)
    {
        _databaseReferenceUsers.removeEventListener(_valueEventListener);
        Intent intent = new Intent();

        intent.putExtra(Extras.USER_KEY,userId);
        intent.putExtra(Extras.USER_NAME,userName);
        intent.putExtra(Extras.PHOTO_NAME,photoName);

        intent.putExtra(Extras.SELECTED_MESSAGE,_selectedMessage);
        intent.putExtra(Extras.SELECTED_MESSAGE_ID,_selectedMessageId);
        intent.putExtra(Extras.SELECTED_MESSAGE_TYPE,_selectedMessageType);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}