package de.pfh.javauniprojekt;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.Date;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private final RecyclerViewInterface recyclerViewInterface;
    private RecyclerView recyclerView;
    private Context context;
    private static List<Beitrag> items;
    private Activity activity;


    public MyAdapter(Context context, RecyclerViewInterface recyclerViewInterface, RecyclerView recyclerView, Activity activity) {
        this.context = context;
        this.recyclerViewInterface = recyclerViewInterface;
        this.recyclerView = recyclerView;
        this.activity = activity;

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.RIGHT) {


            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Beitrag swipedItem = items.get(position);

                Java.addLike(position, swipedItem.getUserID(), swipedItem.getDate())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                swipedItem.setLikes(swipedItem.getLikes() * -1);
                                items.set(position, swipedItem);
                                notifyItemChanged(position);
                            }
                        });
            }
        }).attachToRecyclerView(recyclerView);
    }

    public void setFilteredList(List<Beitrag> filteredList)
    {
        items = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view, parent, false), recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Date date = items.get(position).getDate();
        holder.content.setText(items.get(position).getContent());
        holder.user.setText(items.get(position).getUsername());
        holder.date.setText(Java.dateCheck(date));

        Java.isLikeVorhanden(items.get(position).getUserID(), items.get(position).getDate()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if(!items.isEmpty() && items.size() > position) {
                    if (items.get(position).getLikes() >= 0) {
                        if (task.getResult()) {
                            if (items.get(position).getLikes() == 0) {
                                String likes = 1 + " ♥";
                                holder.likes.setText(likes);
                            } else {
                                String likes = items.get(position).getLikes() + " ♥";
                                holder.likes.setText(likes);
                            }
                        } else {
                            String likes = items.get(position).getLikes() + " \uD83D\uDDA4";
                            holder.likes.setText(likes);
                        }
                    } else {
                        if (task.getResult()) {
                            String likes = ((items.get(position).getLikes() * -1) + 1) + " ♥";
                            holder.likes.setText(likes);
                        } else {
                            String likes = ((items.get(position).getLikes() * -1) - 1) + " \uD83D\uDDA4";
                            holder.likes.setText(likes);
                        }
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static List<Beitrag> getItems() {
        return items;
    }

    public void updateData() {
    }
}
