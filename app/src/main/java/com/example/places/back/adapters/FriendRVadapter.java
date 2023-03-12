package com.example.places.back.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.places.R;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FriendRVadapter extends RecyclerView.Adapter<FriendRVadapter.FriendViewHolder> {

    public static class FriendViewHolder extends RecyclerView.ViewHolder{
        ImageView avatar;
        TextView friendName;
        TextView friendPhone;
        FriendViewHolder(@NonNull View itemView)
        {
            super(itemView);
            avatar = itemView.findViewById(R.id.friendImage);
            friendName= itemView.findViewById(R.id.friendName);
            friendPhone= itemView.findViewById(R.id.friendPhone);
        }
    }
View new_card;
    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.friend_card, viewGroup, false);
        new_card = v;
        FriendViewHolder fvh = new FriendViewHolder(v);
        return fvh;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder friendViewHolder, int i) {
        friendViewHolder.friendName.setText("Имя "+i);
        friendViewHolder.friendPhone.setText("Телефон "+i);
    }

    @Override
    public int getItemCount() {
        //todo: Понятно что надо сделать...
        return 10;
    }
}
