
        package in.dbit.csiapp.mFragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import in.dbit.csiapp.Prompts.attendance_list;
import in.dbit.csiapp.R;

public class AttendanceSBC extends Fragment {

    public static final String EXTRA_CLASS = "com.example.csimanagementsystem.EXTRA_CLASS";

    public static AttendanceSBC newInstance() {
        return new AttendanceSBC();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_attendance_sbc,container,false);
        GridLayout classGrid  = rootView.findViewById(R.id.classGrid);
        getActivity().setTitle("Attendance");

        setSingleEvent(classGrid);
        return rootView;
    }

    private void setSingleEvent(GridLayout mainGrid) {
        //Loop all child item of Main Grid
        for(int i=0; i<mainGrid.getChildCount(); i++){
            //You can see, all child item is CardView, so we just object to CardView
            CardView cardView = (CardView) mainGrid.getChildAt(i);
            final int finalI = i;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent;
                    //Toast.makeText(getActivity(), "Clicked at index "+ finalI, Toast.LENGTH_SHORT).show();
                    switch (finalI){
                        case 0:
                            intent = new Intent(getActivity(), attendance_list.class);
                            intent.putExtra(EXTRA_CLASS, "SE");
                            startActivity(intent);
                            break;
                        case 1:
                            intent = new Intent(getActivity(), attendance_list.class);
                            intent.putExtra(EXTRA_CLASS, "TE");
                            startActivity(intent);
                            break;
                        case 2:
                            intent = new Intent(getActivity(), attendance_list.class);
                            intent.putExtra(EXTRA_CLASS, "BE");
                            startActivity(intent);
                            break;
                    }
                }
            });
        }
    }

    public String toString() {
        return "Attendance";
    }
}