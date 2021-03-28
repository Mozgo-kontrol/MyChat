package com.your.mychat.chats;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.your.mychat.R;
import com.your.mychat.common.NodeNames;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {

    private RecyclerView _rvChatList;
    private View _progressBar;
    private TextView _tvEmptyChatList;
    private ChatListAdapter _chatListAdapter;
    private List <ChatListModel> _chatListModelList;

    //Firebase
    private DatabaseReference _databaseReferenceChats , _databaseReferenceUsers;
    private FirebaseUser _currentUser;

    //---------------------------------------------------------------------------------
    private ChildEventListener _childEventListener;
    private Query query;
    private ValueEventListener valueEventListener;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        _rvChatList=view.findViewById(R.id.rvChats);

        _tvEmptyChatList=view.findViewById(R.id.tv_empty_chat_list);

        _chatListModelList=new ArrayList<>();

        _chatListAdapter= new ChatListAdapter(getActivity(),_chatListModelList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        //revers List with users and some option
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        _rvChatList.setLayoutManager(linearLayoutManager);

        _rvChatList.setAdapter(_chatListAdapter);
        _progressBar = view.findViewById(R.id.progressbar_fc);

        _databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);
        _currentUser = FirebaseAuth.getInstance().getCurrentUser();
        _databaseReferenceChats = FirebaseDatabase.getInstance().getReference().child(NodeNames.CHATS).child(_currentUser.getUid());

        _tvEmptyChatList.setVisibility(View.VISIBLE);
        query = _databaseReferenceChats.orderByChild(NodeNames.TIME_STAMP);
        _progressBar.setVisibility(View.VISIBLE);
        _childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                updateList(snapshot, true, snapshot.getKey());
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

            }
        };

        query.addChildEventListener(_childEventListener);

    }
    private void updateList(DataSnapshot dataSnapshot, boolean isNew, String userID)
    {
        _progressBar.setVisibility(View.GONE);
        _tvEmptyChatList.setVisibility(View.GONE);
        final String lastMessage,lastMessageTime, unreadCount;

        lastMessage="";
        lastMessageTime="";
        unreadCount="";

        _databaseReferenceUsers.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String fullName = snapshot.child(NodeNames.NAME).getValue().toString()!=null?
                       snapshot.child(NodeNames.NAME).getValue().toString() : "" ;

                String photoName = snapshot.child(NodeNames.PHOTO).getValue().toString()!=null?
                        snapshot.child(NodeNames.PHOTO).getValue().toString() : "" ;


                ChatListModel chatListModel = new ChatListModel(userID, fullName, photoName,unreadCount,lastMessage,lastMessageTime);
                _chatListModelList.add(chatListModel);
              // update adapter
                _chatListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),  getString(R.string.failed_to_fetch_chat_list, error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        query.removeEventListener(_childEventListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        query.removeEventListener(_childEventListener);
    }
}