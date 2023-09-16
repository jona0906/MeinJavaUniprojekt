package de.pfh.javauniprojekt;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.List;

public class MyCommentAdapter extends RecyclerView.Adapter<MyCommentViewHolder> {


    private Context context;
    private List<Beitrag> items;
    private final RecyclerViewInterface recyclerViewInterface;
    private RecyclerView recyclerView;
    private Activity activity;

    public MyCommentAdapter(Context context, RecyclerViewInterface recyclerViewInterface, RecyclerView recyclerView, Activity activity) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.activity = activity;
        this.recyclerViewInterface = recyclerViewInterface;

    }

        @Override
        public MyCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            return new MyCommentViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view_comment, parent, false), recyclerViewInterface);
        }

    @Override
    public void onBindViewHolder(@NonNull MyCommentViewHolder holder, int position) {
        holder.content.setText(items.get(position).getContent());
        holder.user.setText(items.get(position).getUsername());
        holder.date.setText(Java.dateCheck(items.get(position).getDate()));
    }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void updateData() {
        }
    public void setItemList(List<Beitrag> filteredList)
    {
        items = filteredList;
        Log.d("MyAdapter", "setFilteredList: " + items.size());
        notifyDataSetChanged();
    }
}