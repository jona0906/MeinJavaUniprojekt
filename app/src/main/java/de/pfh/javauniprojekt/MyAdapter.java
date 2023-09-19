package de.pfh.javauniprojekt;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.Date;
import java.util.List;

/**
 * Diese Klasse dient als Adapter für den Recycler View auf der Startseite. Sie bindet die Daten an die Ansichtselemente
 * im View und ermöglicht somit deren Anzeige.
 */
public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private final RecyclerViewInterface recyclerViewInterface;
    private RecyclerView recyclerView;
    private Context context;
    private static List<Beitrag> items;
    private Activity activity;

    /**
     * Es handelt sich hierbei um den Konstruktor für einen neunen Adapter.
     * @param context Der Kontext der Aufrufenden Activity oder Anwendung.
     * @param recyclerViewInterface Das Interface für die RecyclerView-Ereignisse.
     * @param recyclerView Der RecyclerView, der von diesem Adapter verwaltet wird.
     * @param activity Die Aktivität, welche das Objekt erstellt.
     */
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

            /**
             * Die Methode word aufgerufen, wenn ein Recycler View Item nach rechts gewischt wird. Sie dient dazu die Position des
             * Items zu ermitteln und zu diesem einen Like hinzuzufügen oder zu entfernen.
             * @param viewHolder Der ViewHolder, welcher von dem Nutzer weggewischt wurde.
             * @param direction  Die Reichtung, wohin der VieHolder gewischt wurde.
             */
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

    /**
     * Die Methode aktualisiert die Liste und teilt dies dem RecyclerView mit.
     * @param filteredList Liste, auf welche der RecyclerView aktualisiert werden soll.
     */
    public void setFilteredList(List<Beitrag> filteredList)
    {
        items = filteredList;
        notifyDataSetChanged();
    }

    /**
     * Erstellt einen neuen ViewHolder.
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     */
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view, parent, false), recyclerViewInterface);
    }

    /**
     * Aktualisiert den angegebenen ViewHolder, um den Inhalt des Elements in der angegebenen Position im Datensatz darzustellen.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
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

    /**
     * Methode zum zurückgeben der Anzahl an Beiträgen.
     * @return Anzahl Beiträge der Liste Items.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Mit dieser Methode wird die Liste bestehend aus allen aktuellen Beiträgen zurückgegeben.
     * @return Liste von Beiträgen.
     */
    public static List<Beitrag> getItems() {
        return items;
    }


    public void updateData() {
    }
}
