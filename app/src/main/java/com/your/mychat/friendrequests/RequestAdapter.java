package com.your.mychat.friendrequests;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.your.mychat.R;
import com.your.mychat.common.Constants;
import com.your.mychat.common.NodeNames;
import com.your.mychat.common.Util;

import java.util.List;
import java.util.Objects;

import static com.your.mychat.common.Constants.IMAGES_FOLDER;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ReguestViewHolder> {


    private Context context;
    private List<RequestModel> requestModelList;
    //deny the request
    private DatabaseReference _databaseReferenceFriendRequests, _databaseReferenceChats;
    private FirebaseUser _currentUser;



    public RequestAdapter(Context context, List<RequestModel> requestModelList) {
        this.context = context;
        this.requestModelList = requestModelList;
    }




    //This constructor  give as a new Object of viewHolder
    @NonNull
    @Override
    public RequestAdapter.ReguestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_request_layout, parent, false);
        return new ReguestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReguestViewHolder holder, int position) {

        RequestModel requestModel = requestModelList.get(position);
        holder._tvFullname.setText(requestModel.get_userName());

        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(IMAGES_FOLDER+"/"+requestModel.get_userId()+".ipg");
        if(fileRef!=null){

            fileRef.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(context)
                    .load(uri)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(holder._ivProfile));
        }
        else holder._ivProfile.setImageResource(R.drawable.default_profile);
        //reference for requests
        _databaseReferenceFriendRequests = FirebaseDatabase.getInstance().getReference().child(NodeNames.FRIEND_REQUESTS);
        //reference for Chats
        _databaseReferenceChats= FirebaseDatabase.getInstance().getReference().child(NodeNames.CHATS);


        //reference of currentUser
        _currentUser= FirebaseAuth.getInstance().getCurrentUser();
          //Accept Friends request
        holder._btnAcceptRequest.setOnClickListener(v -> {

            btnUnVisible(holder);

             final String userId= requestModel.get_userId();

            _databaseReferenceChats.child(_currentUser.getUid()).child(userId).child(NodeNames.TIME_STAMP)
                    .setValue(ServerValue.TIMESTAMP).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    _databaseReferenceChats.child(userId).child(_currentUser.getUid()).child(NodeNames.TIME_STAMP)
                            .setValue(ServerValue.TIMESTAMP).addOnCompleteListener(task12 -> {
                        if(task12.isSuccessful()){
                            _databaseReferenceFriendRequests.child(_currentUser.getUid()).child(userId).child(NodeNames.REQUEST_TYPE)
                                    .setValue(Constants.REQUEST_STATUS_ACCEPTED).addOnCompleteListener(task13 -> {

                                if(task13.isSuccessful()){

                                    _databaseReferenceFriendRequests.child(userId).child(_currentUser.getUid()).child(NodeNames.REQUEST_TYPE)
                                            .setValue(Constants.REQUEST_STATUS_ACCEPTED).addOnCompleteListener(task14 -> {
                                        if(task14.isSuccessful()){

                                            //Send  Notification about friend Request Accepted
                                            String title = "Friend Request Accepted";
                                            String message = "Friend request accepted by"+_currentUser.getDisplayName();
                                            Util.sendNotification(context, title,message, userId,_currentUser.getUid());
                                            //---------------------------------------------------------
                                            btnVisible(holder);
                                        }
                                        else handleException(holder, task14.getException());
                                    });
                                }
                                else handleException(holder, task13.getException());
                            });

                        }
                        else handleException(holder, task12.getException());
                    });


                }
                else handleException(holder, task.getException());

            });

        });





        //Deny Friends request
        holder._btnDenyRequest.setOnClickListener(v -> {

            btnUnVisible(holder);

            final String userId= requestModel.get_userId();

           _databaseReferenceFriendRequests.child(_currentUser.getUid()).child(userId)
                   .child(NodeNames.REQUEST_TYPE).setValue(null).addOnCompleteListener(task -> {
               if(task.isSuccessful()){


                   _databaseReferenceFriendRequests.child(userId).child(_currentUser.getUid()).child(NodeNames.REQUEST_TYPE).setValue(null).addOnCompleteListener(task1 -> {
                       if (!task1.isSuccessful()) {
                           Toast.makeText(context, context.getString(R.string.failed_to_deny_request, task.getException()), Toast.LENGTH_SHORT).show();
                       }

                       //Send  Notification about friend Request Deny
                       String title = "Friend Request Denied";
                       String message = "Friend request accepted denied by "+_currentUser.getDisplayName();
                       Util.sendNotification(context, title,message, userId,_currentUser.getUid());
                       //---------------------------------------------------------

                       btnVisible(holder);
                   });
               }
               else {

                   Toast.makeText(context, context.getString(R.string.failed_to_deny_request, task.getException()),Toast.LENGTH_SHORT).show();
                   btnVisible(holder);
               }
           });

        });
    }

    private void handleException(ReguestViewHolder holder, Exception exception) {

        Toast.makeText(context, context.getString(R.string.failed_accept_request, exception),Toast.LENGTH_SHORT).show();
        btnVisible(holder);
    }
    private void btnVisible(ReguestViewHolder holder){
        holder._pbDecision.setVisibility(View.GONE);
        holder._btnDenyRequest.setVisibility(View.VISIBLE);
        holder._btnAcceptRequest.setVisibility(View.VISIBLE);
    }
    private void btnUnVisible(ReguestViewHolder holder){
        holder._pbDecision.setVisibility(View.VISIBLE);
        holder._btnDenyRequest.setVisibility(View.GONE);
        holder._btnAcceptRequest.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return requestModelList.size();
    }

    public class ReguestViewHolder extends RecyclerView.ViewHolder{

        private TextView _tvFullname;
        private ImageView _ivProfile;
        private Button _btnAcceptRequest, _btnDenyRequest;
        private ProgressBar _pbDecision;

        public ReguestViewHolder(@NonNull View itemView) {
            super(itemView);
            _tvFullname = itemView.findViewById(R.id.tv_full_name_fr);
            _ivProfile = itemView.findViewById(R.id.iv_profile_fr);
            _btnAcceptRequest = itemView.findViewById(R.id.btn_accept_request_fr);
            _btnDenyRequest = itemView.findViewById(R.id.btn_deny_Request_fr);
            _pbDecision = itemView.findViewById(R.id.pb_Decision);
        }
    }
}
