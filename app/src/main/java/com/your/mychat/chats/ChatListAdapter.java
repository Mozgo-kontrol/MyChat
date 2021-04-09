package com.your.mychat.chats;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.your.mychat.R;
import com.your.mychat.common.Extras;
import com.your.mychat.common.Util;

import java.util.List;

import static com.your.mychat.common.Constants.IMAGES_FOLDER;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {

    private static final String TAG = "ChatListAdapter";

    private Context context;
    private List<ChatListModel> chatListModelList;

    public ChatListAdapter(Context context, List<ChatListModel> chatListModelList) {
        this.context = context;
        this.chatListModelList = chatListModelList;
    }



    @NonNull
    @Override
    public ChatListAdapter.ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.chat_list_layout,parent, false);
        return new ChatListViewHolder(view);

    }
    @Override
    public void onBindViewHolder(@NonNull ChatListAdapter.ChatListViewHolder holder, int position) {

         final  ChatListModel chatListModel = chatListModelList.get(position);

         holder._tvFullName.setText(chatListModel.get_userName());


         StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(IMAGES_FOLDER).child(chatListModel.get_userId()+".ipg");

         //last message and lastMessageTime
         String lastMessage = chatListModel.get_lastMessage();

         lastMessage = lastMessage.length()>30? lastMessage.substring(0, 27)+"...":lastMessage;

         holder._tv_lastMessage.setText(lastMessage);

         String lastMessageTime = chatListModel.get_lastMessageTime();

        if(lastMessageTime==null) {lastMessageTime="";}

        if(!TextUtils.isEmpty(lastMessageTime)) {

            Log.i(TAG, "Message time "+ Util.getTimeAgo(Long.parseLong(lastMessageTime)));

            holder._tv_LastMessageTime.setText(Util.getTimeAgo(Long.parseLong(lastMessageTime)));
        }

        Log.i(TAG,  "PhotoName" + chatListModel.get_photoName());
        //-------------------------------------------------------------
       if(chatListModel.get_photoName()!= null) {

           fileRef.getDownloadUrl().addOnSuccessListener(url ->
                   Glide.with(context).load(url)
                           .placeholder(R.drawable.default_profile)
                           .error(R.drawable.default_profile)
                           .into(holder._ivProfile));

       }
       else {
           holder._ivProfile.setImageResource(R.drawable.default_profile);
       }
        //unread count
        if(!chatListModel.get_unreadCount().equals("0"))
        {
            holder._tv_unreadCount.setVisibility(View.VISIBLE);
            holder._tv_unreadCount.setText(chatListModel.get_unreadCount());

        }
        else {
            holder._tv_unreadCount.setVisibility(View.GONE);
        }


         holder._llChatlist.setOnClickListener(v -> {
             Intent intent= new Intent (context, ChatActivity.class);
             intent.putExtra(Extras.USER_KEY, chatListModel.get_userId());
             intent.putExtra(Extras.USER_NAME, chatListModel.get_userName());
             intent.putExtra(Extras.PHOTO_NAME, chatListModel.get_photoName());
             context.startActivity(intent);
         });


    }

    @Override
    public int getItemCount() {
        return chatListModelList.size();
    }

    public static class ChatListViewHolder extends RecyclerView.ViewHolder{

        private LinearLayout _llChatlist;
        private TextView _tvFullName, _tv_lastMessage,_tv_LastMessageTime,_tv_unreadCount;
        private ImageView _ivProfile;

        public ChatListViewHolder(@NonNull View itemView) {
            super(itemView);
            _llChatlist=itemView.findViewById(R.id.llChatList);
            _tvFullName = itemView.findViewById(R.id.tv_fullName_chat_list);
            _tv_lastMessage = itemView.findViewById(R.id.tv_last_message_chat_list);
            _tv_LastMessageTime=itemView.findViewById(R.id.tv_last_message_time);
            _tv_unreadCount=itemView.findViewById(R.id.tv_ureaded_count_chat_list);
            _ivProfile=itemView.findViewById(R.id.iv_Profile_chat);
        }

    }
}
