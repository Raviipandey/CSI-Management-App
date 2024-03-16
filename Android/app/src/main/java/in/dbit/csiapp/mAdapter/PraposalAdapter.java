package in.dbit.csiapp.mAdapter;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import in.dbit.csiapp.R;

import java.util.ArrayList;

public class PraposalAdapter extends RecyclerView.Adapter<PraposalAdapter.ExampleViewHolder> implements Filterable {

    private Context mContext;
    private ArrayList<PraposalItem> mPraposalList;
    private ArrayList<PraposalItem> mExampleListFiltered;
    private OnItemClickedListener mListener;
    private String AC="Approved By Chairperson";
    private String RC="Rejected By Chairperson";
    private String AS="Approved By SBC";
    private String RS="Rejected By SBC";
    private String AH="Approved By HOD";
    private String RH="Rejected By HOD";

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String key =  constraint.toString();
                if(key.isEmpty()){
                    mExampleListFiltered = mPraposalList;
                }
                else {
                    ArrayList<PraposalItem> lstFiltered = new ArrayList<>();
                    for (PraposalItem row  : mPraposalList){
                        if (row.getmName().toLowerCase().contains(key.toLowerCase()) || row.getDate().toLowerCase().contains(key.toLowerCase())|| row.getmStatus().toLowerCase().contains(key.toLowerCase())|| row.getmExtra().toLowerCase().contains(key.toLowerCase())){
                            lstFiltered.add(row);
                        }
                    }
                    mExampleListFiltered = lstFiltered;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values= mExampleListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mExampleListFiltered = (ArrayList<PraposalItem>) results.values;
                notifyDataSetChanged();
            }
        };
    }


    public interface OnItemClickedListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickedListener listener) {
        mListener = listener;
    }

    public PraposalAdapter(Context context, ArrayList<PraposalItem> PraposalList) {
        mContext = context;
        mPraposalList = PraposalList;
        this.mExampleListFiltered = mPraposalList;

    }

    @NonNull
    @Override
    public ExampleViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.exmple_item, viewGroup, false);

        return new ExampleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ExampleViewHolder exampleViewHolder, int i) {
        PraposalItem currentItem = mExampleListFiltered.get(i);

        String Eid = currentItem.getmEid();
        String date = currentItem.getDate();
        String Name = currentItem.getmName();
        String status = currentItem.getmStatus();
        String extra = currentItem.getmExtra();
        //Edited By Afif
        exampleViewHolder.container.setAnimation(AnimationUtils.loadAnimation(mContext,R.anim.fade_scale_animation_recycler));

        exampleViewHolder.mTextViewAgenda.setText(Name);
        exampleViewHolder.mTextViewDate.setText(date);
        exampleViewHolder.mTextViewTime.setText(extra);
        Log.i("color status",status);
        if(status.equals("1"))
        {exampleViewHolder.mTextViewE1.setText(AC); exampleViewHolder.ll.setBackgroundColor(Color.parseColor("#CF9FFF"));} //accepted by chairperson
        else if(status.equals("2"))
        {exampleViewHolder.mTextViewE1.setText(AS);exampleViewHolder.ll.setBackgroundColor(Color.parseColor("#FFFF8F"));} //accepted by sbc
        else if(status.equals("3"))
        {exampleViewHolder.mTextViewE1.setText(AH);exampleViewHolder.ll.setBackgroundColor(Color.parseColor("#D900FF00"));} //accepted by hod
        else if(status.equals("-1"))
        {exampleViewHolder.mTextViewE1.setText(RC);exampleViewHolder.ll.setBackgroundColor(Color.parseColor("#FF7F50")); }//rejected by chairperson
        else if(status.equals("-2"))
        {exampleViewHolder.mTextViewE1.setText(RS);exampleViewHolder.ll.setBackgroundColor(Color.parseColor("#D9FF0000"));} //rejected by sbc
        else if(status.equals("-3"))
        {exampleViewHolder.mTextViewE1.setText(RH);exampleViewHolder.ll.setBackgroundColor(Color.parseColor("#D9FF0000"));} //rejected by hod
        else if(status.equals("0"))
        {exampleViewHolder.mTextViewE1.setText("Latest Submitted");exampleViewHolder.ll.setBackgroundColor(Color.parseColor("#213279"));} //white

        //{exampleViewHolder.mTextViewE1.setText("Latest Submitted");exampleViewHolder.ll.setBackgroundColor(Color.parseColor("#213279"));} //white
        else {
            exampleViewHolder.mTextViewE1.setVisibility(View.GONE);
//            exampleViewHolder.ll.setBackgroundColor(Color.parseColor("#80ffffff"));//white ,this one is for creative and technical recycler
        }
        exampleViewHolder.mTextViewPoints.setText(extra );


    }

    @Override
    public int getItemCount() {
        //Edited By Afif
        return mExampleListFiltered.size();
    }

    public class ExampleViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextViewAgenda, mTextViewDate, mTextViewTime, mTextViewPoints,mTextViewE1;
        LinearLayout ll;
        CardView container;
        public ExampleViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.box);
            mTextViewAgenda = itemView.findViewById(R.id.text_view_agenda);
            mTextViewDate = itemView.findViewById(R.id.text_view_date);
            mTextViewTime = itemView.findViewById(R.id.text_view_time);
            mTextViewPoints = itemView.findViewById(R.id.text_view_points);
            mTextViewE1=itemView.findViewById(R.id.text_view_creator);
            ll=itemView.findViewById(R.id.example_item_LL);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

}
