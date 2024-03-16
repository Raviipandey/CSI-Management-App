package in.dbit.csiapp.Prompts;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import in.dbit.csiapp.R;

import java.util.ArrayList;

import in.dbit.csiapp.mFragments.MinuteManager;


public class DetailActivity extends Fragment {
    TextView mAgenda, mDate, mTime, mCreator, mPoints, mAbsentee;
    String agenda, date, time, creator, points, absentee;
    ArrayList<String> task, person;
    TableLayout tableLayout;

    //In this method collect data from Minute Manager and display that data

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {

        View rootView = inflater.inflate(R.layout.detail_layout,container,false);

        mAgenda = rootView.findViewById(R.id.final_agenda);
        mDate = rootView.findViewById(R.id.final_date);
        mTime = rootView.findViewById(R.id.final_time);
        mCreator = rootView.findViewById(R.id.final_creator);
        mPoints = rootView.findViewById(R.id.final_points);
        mAbsentee = rootView.findViewById(R.id.absentee);
        tableLayout = rootView.findViewById(R.id.display_table);

        Bundle bundle = getArguments();

        agenda = bundle.getString(MinuteManager.EXTRA_AGENDA);
        date = bundle.getString(MinuteManager.EXTRA_DATE);
        time = bundle.getString(MinuteManager.EXTRA_TIME);
        creator = bundle.getString(MinuteManager.EXTRA_CREATOR);
        points = bundle.getString(MinuteManager.EXTRA_POINTS);
        absentee = bundle.getString(MinuteManager.EXTRA_ABSENTEE);
        task = bundle.getStringArrayList(MinuteManager.EXTRA_TASK);
        person = bundle.getStringArrayList(MinuteManager.EXTRA_PERSON);
        Log.i("sankey123", task.toString() + " " + person.toString());

        Log.i("sankey", task.toString() + " " + person.toString());

        mAgenda.setText("Agenda: "+agenda);
        mDate.setText("Date: "+date);
        mTime.setText("Time: "+time);
        mCreator.setText("Creator: "+creator);
        mPoints.setText("Points: "+points);
        mAbsentee.setText("Absentee members: "+absentee);

        for (int i = 0; i < task.size(); i++) {
            TableRow tableRow = new TableRow(getContext());
            tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

            TextView tv1 = new TextView(getContext());
            TextView tv2 = new TextView(getContext());

            // Configure tv1 for tasks
            tv1.setText(task.get(i));
            tv1.setGravity(Gravity.CENTER);
            tv1.setTextColor(getResources().getColor(R.color.colorPrimary));
            tv1.setTextSize(17);
            tv1.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Set weight
            tv1.setPadding(8, 8, 8, 8); // Adjust padding as needed
             // Assuming you want borders

            // Configure tv2 similarly for persons
            tv2.setText(person.get(i));
            tv2.setGravity(Gravity.CENTER);
            tv2.setTextColor(getResources().getColor(R.color.colorPrimary));
            tv2.setTextSize(17);
            tv2.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Set weight
            tv2.setPadding(8, 8, 8, 8); // Adjust padding as needed

            // Add TextViews to the TableRow, then add the row to the TableLayout
            tableRow.addView(tv1);
            tableRow.addView(tv2);
            tableLayout.addView(tableRow);
        }


        return rootView;
    }

}