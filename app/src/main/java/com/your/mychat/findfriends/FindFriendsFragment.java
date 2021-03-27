package com.your.mychat.findfriends;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.your.mychat.R;
import com.your.mychat.common.Constants;
import com.your.mychat.common.NodeNames;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FindFriendsFragment#} factory method to
 * create an instance of this fragment.
 */
public class FindFriendsFragment extends Fragment {

    //UI
    private View _progressBar;
    private TextView _vtEmptyFriendsList;


    //Adapter
    private RecyclerView _rvFindFriends;
    private FindFriendAdapter findFriendAdapter;

    //Data must be matched from database with Query
    private List<FindFriendModel> findFriendModelList;

    //Firebase
    private DatabaseReference _databaseReference, _databaseReferenceFriendRequests;
    private FirebaseUser _currentUser;

    public FindFriendsFragment() {
        // Required empty public constructor
    }
/*
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FindFriendsFragment.

    // TODO: Rename and change types and number of parameters
    public static FindFriendsFragment newInstance(String param1, String param2) {
        FindFriendsFragment fragment = new FindFriendsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       /* if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_find_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
         _progressBar=view.findViewById(R.id.progressbar_fff);
         _vtEmptyFriendsList=view.findViewById(R.id.tv_empty_find_fiends_list);

        _rvFindFriends=view.findViewById(R.id.rvFindFiends);
        _rvFindFriends.setLayoutManager(new LinearLayoutManager(getActivity()));

        findFriendModelList= new ArrayList<>();

        findFriendAdapter =new FindFriendAdapter(getActivity(),findFriendModelList);
        _rvFindFriends.setAdapter(findFriendAdapter);

        //The Reference for all Users
        _databaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);
        _currentUser = FirebaseAuth.getInstance().getCurrentUser();
         //The Reference for friends Requests
        _databaseReferenceFriendRequests = FirebaseDatabase.getInstance().getReference()
                .child(NodeNames.FRIEND_REQUESTS)
                .child(_currentUser.getUid());



        _vtEmptyFriendsList.setVisibility(View.VISIBLE);
        _progressBar.setVisibility(View.VISIBLE);
        _vtEmptyFriendsList.setVisibility(View.VISIBLE);
        //Add the users from Firebase
        Query query = _databaseReference.orderByChild(NodeNames.NAME);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                findFriendModelList.clear();
                for(DataSnapshot ds : datasnapshot.getChildren()){

                     String userId = ds.getKey();

                     if(userId.equals(_currentUser.getUid())){
                         continue;
                     }
                     if(ds.child(NodeNames.NAME).getValue()!=null){

                          final   String fullName = ds.child(NodeNames.NAME).getValue().toString();
                          final   String photoName = ds.child(NodeNames.PHOTO).getValue().toString();

                          //The Logic for friends Requests
                         _databaseReferenceFriendRequests.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot snapshot) {
                                 if(snapshot.exists()){
                                     String requestType = snapshot.child(NodeNames.REQUEST_TYPE).getValue().toString();
                                     if(requestType.equals(Constants.REQUEST_STATUS_SENT)){

                                         findFriendModelList.add(new FindFriendModel(fullName,photoName, userId,true));
                                         findFriendAdapter.notifyDataSetChanged();

                                     }
                                 }
                                 else {
                                     findFriendModelList.add(new FindFriendModel(fullName,photoName, userId,false));
                                     findFriendAdapter.notifyDataSetChanged();
                                 }
                             }

                             @Override
                             public void onCancelled(@NonNull DatabaseError error) {
                                 _progressBar.setVisibility(View.GONE);
                             }
                         });
                         _vtEmptyFriendsList.setVisibility(View.GONE);
                         _progressBar.setVisibility(View.GONE);
                     }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                _progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(),  getString(R.string.failed_to_fetch_friends, error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });

    }
}