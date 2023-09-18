package de.pfh.javauniprojekt;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyCommentViewHolder extends RecyclerView.ViewHolder {

    TextView content;
    TextView date;
    TextView user;

    public MyCommentViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
        super(itemView);
        content = itemView.findViewById(R.id.content);
        date = itemView.findViewById(R.id.date);
        user = itemView.findViewById(R.id.user);

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
