package com.example.csi.Gallery.DisplayImageAdapter;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.csi.Gallery.Interfaces.IRecyclerViewClickListener;
import com.example.csi.R;

import java.util.ArrayList;
import java.util.List;

public class GalleryImageAdapter extends RecyclerView.Adapter<GalleryImageAdapter.ImageViewHolder> {

    private Context context;
    private boolean isDeleteButtonVisible = false;
    private ArrayList<String> urlList;
    private IRecyclerViewClickListener clickListener;
    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    private String parentPath;
    private ImageButton deleteButton;



    public GalleryImageAdapter(Context context, ArrayList<String> urlList, IRecyclerViewClickListener clickListener, String parentPath, ImageButton deleteButton) {
        this.context = context;
        this.urlList = urlList;
        this.clickListener = clickListener;
        this.parentPath = parentPath;
        this.deleteButton = deleteButton;

    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.gallery_item, viewGroup, false);
        return new ImageViewHolder(v, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder imageViewHolder, int position) {
        String currentImage = urlList.get(position);
        ImageView imageView = imageViewHolder.imageView;
        CheckBox checkBox = imageViewHolder.checkBox;

        checkBox.setChecked(selectedItems.get(position, false));

        // Set click listener for the checkbox
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int adapterPosition = imageViewHolder.getAdapterPosition();
                toggleSelection(adapterPosition);
            }
        });

        // Set visibility of checkbox based on whether the item is selected or not
        checkBox.setVisibility(isDeleteButtonVisible ? View.VISIBLE : View.GONE);

        Glide.with(context)
                .load(currentImage)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.i("LoadFailed", "Failure");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Log.i("ResourceReady", "Success");
                        return false;
                    }
                }).into(imageView);
    }


    // Toggle selection for the given position
    public void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }

        // Check if there are any selected items
        setDeleteButtonVisibility();

        // Notify adapter to refresh the UI
        notifyDataSetChanged();
    }

    // Set the visibility of the delete button
    private void setDeleteButtonVisibility() {
        boolean isVisible = selectedItems.size() > 0;
        isDeleteButtonVisible = isVisible;
        deleteButton.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return urlList.size();
    }

    // Method to get the selected items
    SparseBooleanArray getSelectedItems() {
        return selectedItems;
    }

    // Additional method to clear selected items
    void clearSelectedItems() {
        selectedItems.clear();
        notifyDataSetChanged();  // Notify adapter to refresh the UI
    }

    // Helper method to get the image file name from its URL
    private String getImageFileNameFromUrl(String imageUrl) {
        Uri uri = Uri.parse(imageUrl);
        return uri.getLastPathSegment();
    }

    // Method to delete an image on the server using Volley
    private void deleteImageOnServer(String folder, String imageUrl) {
        // Construct your delete URL based on the server logic
        String deleteUrl = context.getApplicationContext().getResources().getString(R.string.server_url) + "/gallery/delete/" +  folder + getImageFileNameFromUrl(imageUrl);

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, deleteUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Handle successful deletion
                Log.i("DeleteResponse", response);

                // Remove the deleted item from the list
                int position = urlList.indexOf(imageUrl);
                if (position != -1) {
                    urlList.remove(position);
                    notifyItemRemoved(position);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle error
                Log.e("DeleteError", "Error deleting image: " + error.toString());
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    public void deleteSelectedImages() {
        SparseBooleanArray selectedItems = getSelectedItems();
        for (int i = 0; i < selectedItems.size(); i++) {
            int position = selectedItems.keyAt(i);
            String imageUrl = urlList.get(position);
            String folderName = parentPath; // Use the stored parentPath
            // Call your delete API here using Volley for each selected item
            deleteImageOnServer(folderName, imageUrl);
        }
        // Clear the selection after deletion
        clearSelectedItems();

        // Notify the adapter about the changes
        notifyDataSetChanged();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ImageView imageView;
        CheckBox checkBox;


        public ImageViewHolder(View itemView, IRecyclerViewClickListener clickListener) {

            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            checkBox = itemView.findViewById(R.id.checkbox);


            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (selectedItems.get(position, false)) {
                // Item is already selected - deselect it
                selectedItems.delete(position);

                // Notify adapter to refresh the UI
                notifyDataSetChanged();

                // Make the delete button invisible

            } else {
                // Item is not selected - open image preview
                String imageUrl = urlList.get(position);
                clickListener.onItemClick(position, imageUrl); // Pass the image URL
            }
        }

        @Override
        public boolean onLongClick(View v) {
            int position = getAdapterPosition();
            // Handle long press (select item)
            if (!selectedItems.get(position, false)) {
                selectedItems.put(position, true);
                notifyDataSetChanged();  // Notify adapter to refresh the UI
                setDeleteButtonVisibility();
                deleteButton.setVisibility(View.VISIBLE);

            }
            return true;
        }



        // Method to delete selected images
        public void deleteSelectedImages() {

        }







        // Helper method to get the folder name from the image URL
        private String getFolderNameFromUrl(String imageUrl) {
            Uri uri = Uri.parse(imageUrl);
            List<String> pathSegments = uri.getPathSegments();

            // Find the index of 'gallery' in the path segments
            int galleryIndex = pathSegments.indexOf("gallery");

            // Check if 'gallery' is found and if there is a segment after it
            if (galleryIndex != -1 && galleryIndex < pathSegments.size() - 1) {
                return pathSegments.get(galleryIndex + 1);
            } else {
                // Handle the case where the URL structure doesn't match expectations
                return "";
            }
        }





    }


}