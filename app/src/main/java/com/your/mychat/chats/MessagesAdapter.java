package com.your.mychat.chats;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.view.ActionMode;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.your.mychat.R;
import com.your.mychat.common.Constants;
import com.your.mychat.selectfriend.SelectFriendActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


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
    //menu
    private ActionMode actionMode;
    private LinearLayout _selectedView;


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
                    if(message.getMessageType().equals(Constants.MESSAGE_TYPE_TEXT))
                    {

                        ((MessageViewHolder) holder).llSent.setVisibility(View.VISIBLE);
                        ((MessageViewHolder) holder).llSentImage.setVisibility(View.GONE);

                    }
                    else
                    {
                        ((MessageViewHolder) holder).llSent.setVisibility(View.GONE);
                        ((MessageViewHolder) holder).llSentImage.setVisibility(View.VISIBLE);

                    }
                        ((MessageViewHolder) holder).llReceived.setVisibility(View.GONE);
                        ((MessageViewHolder) holder).llReceivedImage.setVisibility(View.GONE);



                        ((MessageViewHolder) holder).tvSentMessage.setText(message.getMessage());
                        ((MessageViewHolder) holder).tvSentMessageTime.setText(messageTime);
                        ((MessageViewHolder) holder).tvSentImageTime.setText(messageTime);

                        Glide.with(context)
                             .load(message.getMessage())
                             .placeholder(R.drawable.ic_image)
                             .into(((MessageViewHolder) holder).ivSentImage);

            }
            else
                {
                    if(message.getMessageType().equals(Constants.MESSAGE_TYPE_TEXT))
                    {
                        ((MessageViewHolder) holder).llReceived.setVisibility(View.VISIBLE);
                        ((MessageViewHolder) holder).llReceivedImage.setVisibility(View.GONE);
                    }
                    else
                    {
                        ((MessageViewHolder) holder).llReceived.setVisibility(View.GONE);
                        ((MessageViewHolder) holder).llReceivedImage.setVisibility(View.VISIBLE);
                    }

                    ((MessageViewHolder) holder).llSent.setVisibility(View.GONE);
                    ((MessageViewHolder) holder).llSentImage.setVisibility(View.GONE);


                     ((MessageViewHolder) holder).tvReceivedMessage.setText(message.getMessage());
                     ((MessageViewHolder) holder).tvReceivedMessageTime.setText(messageTime);

                    ((MessageViewHolder) holder).tvReceivedImageTime.setText(messageTime);

                    Glide.with(context)
                            .load(message.getMessage())
                            .placeholder(R.drawable.ic_image)
                            .into(((MessageViewHolder) holder).ivReceivedImage);
                }

            ((MessageViewHolder) holder).clMessage.setTag(R.id.TAG_MESSAGE,message.getMessage());
            ((MessageViewHolder) holder).clMessage.setTag(R.id.TAG_MESSAGE_ID,message.getMessageId());
            ((MessageViewHolder) holder).clMessage.setTag(R.id.TAG_MESSAGE_TYPE,message.getMessageType());

            ((MessageViewHolder) holder).clMessage.setOnClickListener(v -> {
                String messageType =v.getTag(R.id.TAG_MESSAGE_TYPE).toString();
                Uri uri = Uri.parse(v.getTag(R.id.TAG_MESSAGE).toString());
                if(messageType.equals(Constants.MESSAGE_TYPE_VIDEO))
                {
                    Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                    intent.setDataAndType(uri,"video/mp4");
                    context.startActivity(intent);
                }
                else if(messageType.equals(Constants.MESSAGE_TYPE_IMAGE)){
                    Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                    intent.setDataAndType(uri,"image/jpg");
                    context.startActivity(intent);
                }
            });
              //menu
            ((MessageViewHolder) holder).clMessage.setOnLongClickListener(v -> {
                if(actionMode!=null)
                {
                    return false;
                }
                //this layout ist to access tagMessage, messageId, messageType
                _selectedView=((MessageViewHolder) holder).clMessage;

                actionMode=((AppCompatActivity)context).startSupportActionMode(actionModeCallback);
                ((MessageViewHolder) holder).clMessage.setBackgroundColor(context.getResources().getColor(R.color.orange));

                return true;

            });
        }


    }

    @Override
    public int getItemCount() {
        return messageModelList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout llSent, llReceived,clMessage, llSentImage, llReceivedImage;
        private TextView tvSentMessage, tvSentMessageTime, tvReceivedMessage, tvReceivedMessageTime,tvSentImageTime, tvReceivedImageTime ;
        private ImageView ivSentImage, ivReceivedImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            clMessage=itemView.findViewById(R.id.cl_message);
            llSent= itemView.findViewById(R.id.llSent);
            llReceived=itemView.findViewById(R.id.llReceived);
            tvSentMessage=itemView.findViewById(R.id.tvSentMessage);
            tvSentMessageTime=itemView.findViewById(R.id.tvSentMessageTime);
            tvReceivedMessage=itemView.findViewById(R.id.tvReceivedMessage);
            tvReceivedMessageTime=itemView.findViewById(R.id.tvReceivedMessageTime);

            llSentImage=itemView.findViewById(R.id.llSentImage);
            ivSentImage=itemView.findViewById(R.id.ivSentImage);
            ivReceivedImage=itemView.findViewById(R.id.ivReceivedImage);
            llReceivedImage=itemView.findViewById(R.id.llReceivedImage);
            tvSentImageTime=itemView.findViewById(R.id.tvSentImageTime);
            tvReceivedImageTime=itemView.findViewById(R.id.tvReceivedImageTime);



        }
    }
    //support menu
    public ActionMode.Callback actionModeCallback= new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_chat_options, menu);

            //we need to hide the uDownload option if user long press on text message
          String selectedMessageType = String.valueOf(_selectedView.getTag(R.id.TAG_MESSAGE_TYPE));
          if(selectedMessageType.equals(Constants.MESSAGE_TYPE_TEXT)){

            MenuItem itemDownload =menu.findItem(R.id.uDownload);
            itemDownload.setVisible(false);
          }
          //---------------------------------------------------------------------------------------


            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            String selectedMessageId = String.valueOf(_selectedView.getTag(R.id.TAG_MESSAGE_ID));
            String selectedMessageType = String.valueOf(_selectedView.getTag(R.id.TAG_MESSAGE_TYPE));
            String selectedMessage = String.valueOf(_selectedView.getTag(R.id.TAG_MESSAGE));

            int itemId = item.getItemId();

            switch (itemId){
                case R.id.mnuDelete:
                     if(context instanceof ChatActivity){

                        ((ChatActivity) context).deleteMessage(selectedMessageId, selectedMessageType);

                     }
                    //Toast.makeText(context, "Delete Option Clicked", Toast.LENGTH_SHORT).show();
                    actionMode.finish();
                    break;
                case R.id.uDownload:

                    ((ChatActivity) context).downloadFile(selectedMessageId, selectedMessageType, false);
                    //Toast.makeText(context, "Download Option Clicked", Toast.LENGTH_SHORT).show();
                    actionMode.finish();
                    break;
                case R.id.mnuForward:

                   if(context instanceof ChatActivity){

                       ((ChatActivity) context).forwardMessage(selectedMessageId, selectedMessage, selectedMessageType);
                   }



                    //context.startActivity(new Intent(context, SelectFriendActivity.class));
                   // Toast.makeText(context, "Forward Option Clicked", Toast.LENGTH_SHORT).show();
                    actionMode.finish();
                    break;
                case R.id.mnuShare:
                      if(selectedMessageType.equals(Constants.MESSAGE_TYPE_TEXT))
                      {
                          Intent intentShare =new Intent();
                          intentShare.setAction(Intent.ACTION_SEND);
                          intentShare.putExtra(Intent.EXTRA_TEXT, selectedMessage);
                          intentShare.setType("text/plain");
                          context.startActivity(intentShare);
                      }
                      else {

                          ((ChatActivity) context).downloadFile(selectedMessageId, selectedMessageType, true);

                      }
                   // Toast.makeText(context, "Share Option Clicked", Toast.LENGTH_SHORT).show();
                    actionMode.finish();
                    break;

            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

           mode=null;
           _selectedView.setBackgroundColor(context.getResources().getColor(R.color.chat_background));
        }
    };

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
