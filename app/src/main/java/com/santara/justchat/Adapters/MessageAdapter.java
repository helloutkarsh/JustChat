package com.santara.justchat.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.santara.justchat.Models.Message;
import com.santara.justchat.R;
import com.santara.justchat.databinding.ItemReceiveBinding;
import com.santara.justchat.databinding.ItemSendBinding;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter {
    Context context;
    ArrayList<Message> messages;
    String senderRoom, receiverRoom;
    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;
    public MessageAdapter(Context context, ArrayList<Message> messages , String senderRoom, String receiverRoom){
        this.context = context;
        this.messages = messages;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == ITEM_SENT){
            View view = LayoutInflater.from(context).inflate(R.layout.item_send,parent, false);
            return new SentViewHolder(view);
        }

        else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message  message = messages.get(position);
        if(FirebaseAuth.getInstance().getUid().equals((message.getSenderId()))){
            return ITEM_SENT;
        }
        else return  ITEM_RECEIVE;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Message message = messages.get(position);
        if(holder.getClass() == SentViewHolder.class){
            SentViewHolder viewHolder = (SentViewHolder)holder;
            if(message.getMessage().equals("Image")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.sentMsg.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .into(viewHolder.binding.image);

            }
            viewHolder.binding.sentMsg.setText(message.getMessage());
        }

        else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;

            if (message.getMessage().equals("Image")) {
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.receiveMsg.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .into(viewHolder.binding.image);
            }
            viewHolder.binding.receiveMsg.setText(message.getMessage());
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder{

        ItemSendBinding binding;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSendBinding.bind(itemView);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder{
        ItemReceiveBinding binding;
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveBinding.bind((itemView));
        }
    }
}
