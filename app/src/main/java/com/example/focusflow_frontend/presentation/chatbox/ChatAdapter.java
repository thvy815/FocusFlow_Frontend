package com.example.focusflow_frontend.presentation.chatbox;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import io.noties.markwon.Markwon;

import androidx.recyclerview.widget.RecyclerView;
import com.example.focusflow_frontend.R;
import com.example.focusflow_frontend.data.model.ChatBox;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<ChatBox> messages;
    private Markwon markwon;  // Markwon xử lý markdown

    public ChatAdapter(List<ChatBox> messages) {
        this.messages = messages;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;
        public ViewHolder(View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser() ? 0 : 1;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = (viewType == 0) ? R.layout.item_user_message : R.layout.item_ai_message;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String markdownText = messages.get(position).getContent();

        // Khởi tạo Markwon nếu chưa có (chỉ làm 1 lần)
        if (markwon == null) {
            markwon = Markwon.create(holder.itemView.getContext());
        }

        // Gán nội dung markdown vào TextView
        markwon.setMarkdown(holder.textMessage, markdownText);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
