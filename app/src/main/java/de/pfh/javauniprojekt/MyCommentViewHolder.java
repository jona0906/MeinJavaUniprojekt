package de.pfh.javauniprojekt;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Dieser ViewHolder zeigt den Inhalt eines Kommentares in einem RecyclerView an. Er zeigt dabei so etwas, wie den Inhalt, das Datum
 * und den veröffentlichenden Nutzer an.
 */
public class MyCommentViewHolder extends RecyclerView.ViewHolder {

    TextView content;
    TextView date;
    TextView user;

    /**
     * Konstruktor für den ViewHolder.
     * @param itemView Die Ansicht für den Kommentar
     * @param recyclerViewInterface Das Interface für die RecyclerView-Interaktionen.
     */
    public MyCommentViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
        super(itemView);
        content = itemView.findViewById(R.id.content);
        date = itemView.findViewById(R.id.date);
        user = itemView.findViewById(R.id.user);

        itemView.setOnClickListener(new View.OnClickListener() {
            /**
             * Ruft die Position ab, auf welches Item geklickt wurde und startet damit neue Methoden.
             */
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
