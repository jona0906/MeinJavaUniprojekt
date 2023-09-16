package de.pfh.javauniprojekt;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private final RecyclerViewInterface recyclerViewInterface;
    private RecyclerView recyclerView;
    private Context context;
    private List<Beitrag> items;
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


                Log.d("MyAdapter", "onSwiped: "+ items.get(position).getContent() + "   " + items.size());


                Java.addLike(position, items.get(position).getUserID(), items.get(position).getDate());


                restoreItem(viewHolder.itemView, position);
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void restoreItem (View view,int position){
        // Animation, damit das Element zurückkommt.
        view.animate()
                .translationX(0)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {

                        /*
                        Task<List<Beitrag>> loadDataTask = Java.load(recyclerView, MyAdapter.this, activity);

                        loadDataTask.addOnCompleteListener(new OnCompleteListener<List<Beitrag>>() {
                            @Override
                            public void onComplete(Task<List<Beitrag>> task) {
                                if (task.isSuccessful()) {
                                    items = task.getResult();
                                }
                            }
                        });

                         */
                        /*
                        Frage: Wie bekomme ich es hin, dass 1. das Element nach dem wischen nicht ausgeblendet wird und 2., dass
                        das Feld mit Recycler View entweder aktualisiert wird, oder sich einfach nur die Zahl "Likes" um einen
                        erhöht?
                         */
                    }
                });
    }


    public void setFilteredList(List<Beitrag> filteredList)
    {
        items = filteredList;
        Log.d("MyAdapter", "setFilteredList: " + items.size());
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
        //holder.likes.setText(items.get(position).getLikes() + " ♥");
        //holder.likes.setText(items.get(position).getLikes() + " \uD83D\uDDA4");

        Java.isLikeVorhanden(items.get(position).getUserID(), items.get(position).getDate()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult()) {
                    holder.likes.setText(items.get(position).getLikes() + " ♥");
                } else {
                    holder.likes.setText(items.get(position).getLikes() + " \uD83D\uDDA4");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateData() {
    }
}
