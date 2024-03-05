package in.dbit.csiapp.mActivityManager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;
import in.dbit.csiapp.R;

class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> {

    private Context context; // Store the context
    private List<String> fileNames;

    public FileListAdapter(Context context, List<String> fileNames) {
        this.context = context;
        this.fileNames = fileNames;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String fileName = fileNames.get(position);
        holder.fileNameTextView.setText(fileName);

        // You can use 'context' here if needed
        // Example: context.startActivity(new Intent(context, AnotherActivity.class));
    }

    @Override
    public int getItemCount() {
        return fileNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            fileNameTextView = itemView.findViewById(R.id.fileNameTextView);
        }
    }
}
