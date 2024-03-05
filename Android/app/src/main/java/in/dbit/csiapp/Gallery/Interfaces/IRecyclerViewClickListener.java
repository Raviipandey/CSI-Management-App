package in.dbit.csiapp.Gallery.Interfaces;

import android.view.View;

public interface IRecyclerViewClickListener {
    void onClick(View view, int position);
    void onLongClick(View v, int adapterPosition);
    void onItemClick(int position, String imageUrl);
}
