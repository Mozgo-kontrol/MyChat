package com.your.mychat.findfriends;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.your.mychat.R;
import com.your.mychat.common.Constants;
import com.your.mychat.common.NodeNames;

import java.util.List;
import java.util.Objects;

import static com.your.mychat.R.string.request_sent_successfully;
import static com.your.mychat.common.Constants.IMAGES_FOLDER;

public class FindFriendAdapter extends RecyclerView.Adapter<FindFriendAdapter.FindFriendViewHolder> {
      private static final String TAG = "FindFriendAdapter";
    private Context context;
    private List<FindFriendModel> findFriendModelList;
    //Friend request
    private DatabaseReference _friendRequestDatabaseReference;
    private FirebaseUser _currentUser;
    private String _userId;


    public FindFriendAdapter(Context context, List<FindFriendModel> findFriendModelList) {
        this.context = context;
        this.findFriendModelList = findFriendModelList;
    }


    @NonNull
    @Override
    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.find_friends_layout,parent, false);
        return new FindFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FindFriendViewHolder holder, int position) {

          FindFriendModel findfriendModel = findFriendModelList.get(position);
          holder._tv_full_Name.setText(findfriendModel.get_userName());

        StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(IMAGES_FOLDER+"/"+findfriendModel.get_userId()+".ipg");
        Log.i(TAG, fileRef.toString());

        fileRef.getDownloadUrl().addOnSuccessListener(uri ->
                Glide.with(context).load(uri)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(holder._ivProfile));

       //Friends requests
        _friendRequestDatabaseReference = FirebaseDatabase.getInstance().getReference().child(NodeNames.FRIEND_REQUESTS);
        _currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //Check is friends request send
        if(findfriendModel.is_requestSent()){
            holder._btn_send_request.setVisibility(View.GONE);
            holder._btn_cancel_request.setVisibility(View.VISIBLE);
        }
        else {
            holder._btn_send_request.setVisibility(View.VISIBLE);
            holder._btn_cancel_request.setVisibility(View.GONE);
        }

        //handle with send request button  to make friends request
        holder._btn_send_request.setOnClickListener(v -> {
            holder._btn_send_request.setVisibility(View.GONE);
            holder._pg_Request.setVisibility(View.VISIBLE);

            _userId = findfriendModel.get_userId();
            _friendRequestDatabaseReference.child(_currentUser.getUid()).child(_userId).child(NodeNames.REQUEST_TYPE)
                    .setValue(Constants.REQUEST_STATUS_SENT).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            _friendRequestDatabaseReference.child(_userId).child(_currentUser.getUid()).child(NodeNames.REQUEST_TYPE)
                                    .setValue(Constants.REQUEST_STATUS_RECEIVED).addOnCompleteListener(task1 -> {
                                        if(task1.isSuccessful()){
                                        Toast.makeText(context, request_sent_successfully, Toast.LENGTH_SHORT).show();

                                        holder._btn_send_request.setVisibility(View.GONE);
                                        holder._pg_Request.setVisibility(View.GONE);
                                        holder._btn_cancel_request.setVisibility(View.VISIBLE);
                                        }

                                        else{
                                            Toast.makeText(context, context.getString(R.string.failed_to_send_friends_request,
                                                    Objects.requireNonNull(task.getException()).toString() ), Toast.LENGTH_SHORT)
                                                    .show();
                                            holder._btn_send_request.setVisibility(View.VISIBLE);
                                            holder._pg_Request.setVisibility(View.GONE);
                                            holder._btn_cancel_request.setVisibility(View.GONE);
                                        }

                                    });

                        }
                        else{
                            Toast.makeText(context, context.getString(R.string.failed_to_send_friends_request,
                                    Objects.requireNonNull(task.getException()).toString() ), Toast.LENGTH_SHORT)
                                    .show();
                            holder._btn_send_request.setVisibility(View.VISIBLE);
                            holder._pg_Request.setVisibility(View.GONE);
                            holder._btn_cancel_request.setVisibility(View.GONE);
                        }
                    });
        });
        //handle with cancel request button  to cancel friends request
        holder._btn_cancel_request.setOnClickListener(v -> {
            holder._btn_cancel_request.setVisibility(View.GONE);
            holder._pg_Request.setVisibility(View.VISIBLE);
            _userId = findfriendModel.get_userId();
            _friendRequestDatabaseReference.child(_currentUser.getUid()).child(_userId).child(NodeNames.REQUEST_TYPE)
                    .setValue(null).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    _friendRequestDatabaseReference.child(_userId).child(_currentUser.getUid()).child(NodeNames.REQUEST_TYPE)
                            .setValue(null).addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()){
                            Toast.makeText(context, R.string.request_cancelled_successfully, Toast.LENGTH_SHORT).show();

                            holder._btn_send_request.setVisibility(View.VISIBLE);
                            holder._pg_Request.setVisibility(View.GONE);
                            holder._btn_cancel_request.setVisibility(View.GONE);
                        }

                        else{
                            Toast.makeText(context, context.getString(R.string.failed_to_cancel_friends_request,
                                    Objects.requireNonNull(task.getException()).toString() ), Toast.LENGTH_SHORT)
                                    .show();
                            holder._btn_send_request.setVisibility(View.GONE);
                            holder._pg_Request.setVisibility(View.GONE);
                            holder._btn_cancel_request.setVisibility(View.VISIBLE);
                        }

                    });

                }
                else{
                    Toast.makeText(context, context.getString(R.string.failed_to_cancel_friends_request,
                            Objects.requireNonNull(task.getException()).toString() ), Toast.LENGTH_SHORT)
                            .show();
                    holder._btn_send_request.setVisibility(View.GONE);
                    holder._pg_Request.setVisibility(View.GONE);
                    holder._btn_cancel_request.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return findFriendModelList.size();
    }
    /*
     *  FindfriendViewHolder to hold the Views
     *
     *
     **/
    public class FindFriendViewHolder extends RecyclerView.ViewHolder {
        /*
        * Create the Objects from find friends layout
        *
        *
        **/
        private ImageView _ivProfile;
        private TextView _tv_full_Name;
        private Button _btn_send_request, _btn_cancel_request;
        private ProgressBar _pg_Request;


        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            _ivProfile = itemView.findViewById(R.id.iv_profile_ffl);
            _tv_full_Name = itemView.findViewById(R.id.tv_FullName_ffl);
            _btn_send_request= itemView.findViewById(R.id.btn_send_request_ffl);
            _btn_cancel_request = itemView.findViewById(R.id.btn_cancel_request_ffl);
            _pg_Request= itemView.findViewById(R.id.pb_request_ffl);
        }
    }
}
