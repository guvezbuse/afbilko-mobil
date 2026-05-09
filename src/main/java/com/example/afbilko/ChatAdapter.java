package com.example.afbilko;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ChatMessage> messageList;

    public ChatAdapter(List<ChatMessage> messageList) { this.messageList = messageList; }

    @Override
    public int getItemViewType(int position) { return messageList.get(position).isUser() ? 1 : 0; }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_bot, parent, false);
            return new BotViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessage = messageList.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).tv.setText(chatMessage.getMessage());
        } else {
            BotViewHolder botHolder = (BotViewHolder) holder;
            botHolder.tv.setText(chatMessage.getMessage());

            // Acil durum kontrolü ve renk değişimi
            if (chatMessage.isEmergency()) {
                botHolder.tv.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                botHolder.tv.setTextColor(Color.WHITE);
            } else {
                botHolder.tv.getBackground().setColorFilter(Color.parseColor("#E0E0E0"), PorterDuff.Mode.SRC_IN);
                botHolder.tv.setTextColor(Color.BLACK);
            }
        }
    }

    @Override
    public int getItemCount() { return messageList.size(); }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tv; UserViewHolder(View v) { super(v); tv = v.findViewById(R.id.tvUserMsg); }
    }
    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView tv; BotViewHolder(View v) { super(v); tv = v.findViewById(R.id.tvBotMsg); }
    }
}