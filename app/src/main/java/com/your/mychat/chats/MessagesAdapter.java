package com.your.mychat.chats;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.your.mychat.R;
import com.your.mychat.common.NodeNames;

import org.jetbrains.annotations.NotNull;
import android.content.Intent;
import android.content.SharedPreferences;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int VIEW_TYPE_DATE = 4;
    private static final int VIEW_TYPE_ITEM_SENDER = 0;
    private Context context;
    private List<MessageModel> messageModelList;
    //we need this to indetificate firebaseuser
    private FirebaseAuth firebaseAuth;
    private static boolean dayViewWasPrinted=false;
    private SharedPreferences messageSP;
    private SharedPreferences.Editor editor;


    public MessagesAdapter(Context context, List<MessageModel> messageModelList) {
        this.context = context;
        this.messageModelList = messageModelList;

    }


    @Override
    public int getItemViewType(int position) {
        MessageModel messageModel = messageModelList.get(position);
      /*
        if(position!= 0){
            MessageModel previosMessageModel = messageModelList.get(position-1);
            dayViewWasPrinted = istLastDayCurrentDay(messageModel, previosMessageModel);

        }

        if (!dayViewWasPrinted)
        {
            return VIEW_TYPE_DATE;
        }**/
        return VIEW_TYPE_ITEM_SENDER;



    }


    @Override
    public MessagesAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_DATE)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.item_date, parent, false);
            return new MessageViewHolder(view);

        }
         if (viewType == VIEW_TYPE_ITEM_SENDER)
         {
             View view = LayoutInflater.from(context).inflate(R.layout.message_layout, parent, false);
        return new MessageViewHolder(view);
         }
      return null;

    }



    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MessageModel message = messageModelList.get(position);
        firebaseAuth = FirebaseAuth.getInstance();
        String currentUserId = firebaseAuth.getCurrentUser().getUid();

        String fromUserId =message.getMessageFrom();

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String dateTime = sfd.format(new Date(message.getMessageTime()));

        String []splitString = dateTime.split(" ");
        String messageTime = splitString[1];


        if (holder instanceof DateViewHolder) {
            MessageModel messageModel = messageModelList.get(position);
            DateViewHolder dateViewHolder = (DateViewHolder) holder;
            dateViewHolder.date.setText(android.text.format.DateFormat.format("d MMMM, yyyy", messageModel.getMessageTime()));
        }


        if (holder instanceof MessageViewHolder) {
            if (fromUserId.equals(currentUserId))
            {
                     ((MessageViewHolder) holder).llSent.setVisibility(View.VISIBLE);
                     ((MessageViewHolder) holder).llReceived.setVisibility(View.GONE);
                     ((MessageViewHolder) holder).tvSentMessage.setText(message.getMessage());
                     ((MessageViewHolder) holder).tvSentMessageTime.setText(messageTime);
            }
            else
                {
                     ((MessageViewHolder) holder).llReceived.setVisibility(View.VISIBLE);
                     ((MessageViewHolder) holder).llSent.setVisibility(View.GONE);
                     ((MessageViewHolder) holder).tvReceivedMessage.setText(message.getMessage());
                     ((MessageViewHolder) holder).tvReceivedMessageTime.setText(messageTime);
                }
        }

    }

    @Override
    public int getItemCount() {
        return messageModelList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout llSent, llReceived,clMessage;
        private TextView tvSentMessage, tvSentMessageTime, tvReceivedMessage, tvReceivedMessageTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            clMessage=itemView.findViewById(R.id.cl_message);
            llSent= itemView.findViewById(R.id.llSent);
            llReceived=itemView.findViewById(R.id.llReceived);
            tvSentMessage=itemView.findViewById(R.id.tvSentMessage);
            tvSentMessageTime=itemView.findViewById(R.id.tvSentMessageTime);
            tvReceivedMessage=itemView.findViewById(R.id.tvReceivedMessage);
            tvReceivedMessageTime=itemView.findViewById(R.id.tvReceivedMessageTime);


        }
    }
    public class DateViewHolder extends RecyclerView.ViewHolder {
        public TextView date;

        public DateViewHolder(View view) {
            super(view);
            date = (TextView)view.findViewById(R.id.item_date_text_view);
        }
    }

    private boolean istLastDayCurrentDay(MessageModel previos, MessageModel current){
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy");
        String dateTime = sfd.format(new Date(current.getMessageTime()));
        String []splitString = dateTime.split("-");
        int currentDay = Integer.parseInt(splitString[0]);

        @SuppressLint("SimpleDateFormat")
        String previosDate = sfd.format(new Date(previos.getMessageTime()));
        String []splitString1 = previosDate.split("-");
        int previosDay = Integer.parseInt(splitString1[0]);

        if(previosDay == currentDay){
            return true;
        }
        else return false;



    }

}
