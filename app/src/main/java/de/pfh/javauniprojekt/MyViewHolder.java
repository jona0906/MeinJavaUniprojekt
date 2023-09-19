package de.pfh.javauniprojekt;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Dieser ViewHolder zeigt die Ansicht für einen Beitrag in dem RecyclerView auf der Startseite an. Er zeigt Infos, wie den Inhalt
 * des Beitrages, das Veröffentlichungsdatum oder die Anzahl der Likes an.
 */
public class MyViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;
    TextView content;
    TextView date;
    TextView user;
    TextView likes;

    /**
     * Konstruktor für den View Holder.
     * @param itemView Die Ansicht für den Beitrag.
     * @param recyclerViewInterface Das Interface für die RecyclerView-Interaktionen.
     */
    public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
        super(itemView);
        imageView = itemView.findViewById(R.id.imageview);
        content = itemView.findViewById(R.id.content);
        date = itemView.findViewById(R.id.date);
        user = itemView.findViewById(R.id.user);
        likes = itemView.findViewById(R.id.likes);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recyclerViewInterface != null){
                    int position = getAbsoluteAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        recyclerViewInterface.onItemClick(position);
                    }
                }
            }
        });
    }
}
