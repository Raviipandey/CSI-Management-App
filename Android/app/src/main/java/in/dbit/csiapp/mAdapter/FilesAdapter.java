package in.dbit.csiapp.mAdapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;
import in.dbit.csiapp.R;

import in.dbit.csiapp.mActivityManager.FileItem;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {

    private List<FileItem> fileList;
    private OnItemClickListener listener;


    public interface OnItemClickListener {
        void onItemClick(FileItem item);
    }

    public FilesAdapter(List<FileItem> fileList) {
        this.fileList = fileList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileItem fileItem = fileList.get(position);
        holder.fileNameTextView.setText(fileItem.getFileName());
        holder.fileDescriptionTextView.setText(fileItem.getFileDescription());
        holder.fileDateTextView.setText(fileItem.getFileDate());
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameTextView;
        TextView fileDescriptionTextView;
        TextView fileDateTextView;

        public ViewHolder(View itemView) {
            super(itemView);
//            fileNameTextView = itemView.findViewById(R.id.textViewFileName);
//            fileDescriptionTextView = itemView.findViewById(R.id.textViewFileDescription);
//            fileDateTextView = itemView.findViewById(R.id.textViewFileDate);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(fileList.get(position));
                        }
                    }
                }
            });
        }
    }
}
