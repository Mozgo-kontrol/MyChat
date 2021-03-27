package com.your.mychat.friendrequests;

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
import com.google.firebase.database.ValueEventListener;
import com.your.mychat.R;
import com.your.mychat.common.Constants;
import com.your.mychat.common.NodeNames;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RequestsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestsFragment extends Fragment {
/*
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    */
    private RecyclerView _rvRequest;
    private RequestAdapter _adapter;
    private List<RequestModel> _requestModelList;
    private TextView _tvEmptyRequestsList;
    private DatabaseReference _databaseReferenceRequests, _databaseReferenceUsers;
    private FirebaseUser _currentUser;
    private View _progressBar;


    public RequestsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RequestsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RequestsFragment newInstance(String param1, String param2) {
        /*
        RequestsFragment fragment = new RequestsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);*/
        return null;
    }

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
        return inflater.inflate(R.layout.fragment_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        _rvRequest=view.findViewById(R.id.tvRequests);
        _tvEmptyRequestsList=view.findViewById(R.id.tv_empty_requests_list);
        _progressBar=view.findViewById(R.id.progressbar_fr);

        _rvRequest.setLayoutManager(new LinearLayoutManager(getActivity()));

        _requestModelList = new ArrayList<>();

        _adapter =  new RequestAdapter(getActivity(),_requestModelList);
        _rvRequest.setAdapter(_adapter);

        _currentUser = FirebaseAuth.getInstance().getCurrentUser();

        _databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);
        _databaseReferenceRequests = FirebaseDatabase.getInstance().getReference().child(NodeNames.FRIEND_REQUESTS).child(_currentUser.getUid());

        _progressBar.setVisibility(View.VISIBLE);

        _tvEmptyRequestsList.setVisibility(View.VISIBLE);


        _databaseReferenceRequests.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               _progressBar.setVisibility(View.GONE);
               _requestModelList.clear();

                _adapter.notifyDataSetChanged();

               for(DataSnapshot ds : snapshot.getChildren()){

                   if(ds.exists()){

                       String requestType = ds.child(NodeNames.REQUEST_TYPE).getValue().toString();

                       if(requestType.equals(Constants.REQUEST_STATUS_RECEIVED))
                       {
                           String userId = ds.getKey();

                           assert userId != null;

                           _databaseReferenceUsers.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                               @Override
                               public void onDataChange(@NonNull DataSnapshot snapshot) {

                                   String userName = snapshot.child(NodeNames.NAME).getValue().toString();
                                   String photoName="";
                                   if(snapshot.child(NodeNames.PHOTO).getValue() != null){

                                      photoName = snapshot.child(NodeNames.PHOTO).getValue().toString();

                                   }
                                   RequestModel requestModel = new RequestModel(userId,userName, photoName);
                                   _requestModelList.add(requestModel);
                                   _adapter.notifyDataSetChanged();
                                   _tvEmptyRequestsList.setVisibility(View.GONE);

                               }

                               @Override
                               public void onCancelled(@NonNull DatabaseError error) {
                                   _progressBar.setVisibility(View.GONE);
                                   Toast.makeText(getContext(),  getString(R.string.failed_to_fetch_friends_request, error.getMessage()), Toast.LENGTH_SHORT).show();


                               }
                           });

                       }

                   }


               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                _progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(),  getString(R.string.failed_to_fetch_friends_request, error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });



    }
}