package de.pfh.javauniprojekt;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * Diese Klasse dient als Adapter für den Recycler View, welcher die Kommentare anzeigt. Sie bindet die Daten an die Ansichtselemente
 * im View und ermöglicht somit deren Anzeige.
 */
public class MyCommentAdapter extends RecyclerView.Adapter<MyCommentViewHolder> {

    private Context context;
    private List<Beitrag> items;
    private final RecyclerViewInterface recyclerViewInterface;
    private RecyclerView recyclerView;
    private Activity activity;

    /**
     * Konstruktor für einen neuen Adapter.
     * @param context
     * @param recyclerViewInterface
     * @param recyclerView
     * @param activity
     */
    public MyCommentAdapter(Context context, RecyclerViewInterface recyclerViewInterface, RecyclerView recyclerView, Activity activity) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.activity = activity;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    /**
     * Erstellt einen neuen ViewHolder.
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     */
        @Override
        public MyCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            return new MyCommentViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view_comment, parent, false), recyclerViewInterface);
        }

    /**
     * Aktualisiert den angegebenen ViewHolder, um den Inhalt des Elements in der angegebenen Position im Datensatz darzustellen.
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull MyCommentViewHolder holder, int position) {
        holder.content.setText(items.get(position).getContent());
        holder.user.setText(items.get(position).getUsername());
        holder.date.setText(Java.dateCheck(items.get(position).getDate()));
    }

    /**
     * Methode zum zurückgeben der Anzahl an Beiträgen.
     * @return Anzahl Beiträge der Liste Items.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateData() {
    }

    /**
     * Methode zum setzen der Liste mit Kommentaren.
     * @param filteredList
     */
    public void setItemList(List<Beitrag> filteredList)
    {
        items = filteredList;
        notifyDataSetChanged();
    }
}