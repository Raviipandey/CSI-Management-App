package in.dbit.csiapp.mFragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import in.dbit.csiapp.SharedPreferenceConfig;
import in.dbit.csiapp.Prompts.AddMinute;
import in.dbit.csiapp.Prompts.DetailActivity;
import in.dbit.csiapp.R;

import in.dbit.csiapp.mAdapter.ExampleAdapter;
import in.dbit.csiapp.mAdapter.ExampleItem;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MinuteManager extends Fragment implements ExampleAdapter.OnItemClickedListener {
    private String urole1,eid , BoxStatus;

    //creating constant variables to use as keywords while sending data to DetailActivity.java
    public static final String EXTRA_AGENDA = "agenda";
    public static final String EXTRA_DATE = "date";
    public static final String EXTRA_TIME = "time";
    public static final String EXTRA_CREATOR = "creator";
    public static final String EXTRA_POINTS = "points";
    public static final String EXTRA_ABSENTEE = "absentee";
    public static final String EXTRA_TASK = "task";
    public static final String EXTRA_PERSON = "person";
    private SharedPreferenceConfig preferenceConfig;

    private RecyclerView rv;
    private ExampleAdapter mExampleAdapter;
    private ArrayList<ExampleItem> mExampleList;
    private RequestQueue mRequestQueue;
    private FloatingActionButton mAddMinute;
    private View rootView;
    EditText SearchInput;
    private String server_url, UID;
    CharSequence search="";
    SwipeRefreshLayout swipeRefreshLayout;

    public  static MinuteManager newInstance()
    {
        return new MinuteManager();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.minute_manager,null);
        getActivity().setTitle("Minute Manager");
        Bundle bundle = getArguments();
        UID = this.getArguments().getString("id");
        // Adjusted to use getActivity() for context.
        preferenceConfig = new SharedPreferenceConfig(getActivity());

        urole1 = preferenceConfig.readRoleStatus();
        //SearchBar
        SearchInput = rootView.findViewById(R.id.search_bar);
        //We are getting User ID from navigation manager to this fragment

        //REFERENCE
        rv = (RecyclerView) rootView.findViewById(R.id.recycler_view_RV);

        //LAYOUT MANAGER
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));

        swipe(); //This method is used to add swipe refresh Layout

        mExampleList = new ArrayList<>();
        mAddMinute = rootView.findViewById(R.id.add_button);

        if(!urole1.equals("Secretary")){
            mAddMinute.setVisibility(View.GONE);

        }

        mAddMinute.setOnClickListener(new View.OnClickListener() {
            Context context = rootView.getContext();
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddMinute.class);
                intent.putExtra("id",UID);
                //intent.putExtra(EXTRA_FLAG, FLAG);
                startActivity(intent);
            }
        });

        mRequestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        parseJSON(); //This method is used to get list of Agendas from server
        //Adapter
        rv.setAdapter(new ExampleAdapter(getActivity(), mExampleList));
        SearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mExampleAdapter.getFilter().filter(s);
                search = s;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return rootView;


    }

    public void parseJSON() {
        server_url = rootView.getResources().getString(R.string.server_url) + "/minutes/list";   //Main Server URL
        //server_url = "http://206.189.135.147:8081/minutes/list";
        //server_url = "http://192.168.43.84:8080/minutes/list";

        mExampleList.clear(); //We should clear our arraylist
        mExampleAdapter = new ExampleAdapter(getActivity(), mExampleList);
        //because we are calling this method is swipe refresh layout
        //So every time we send request to server for getting agenda... prev added agendas won't repeat again.

        StringRequest stringRequest =new StringRequest(Request.Method.POST,server_url,new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                swipeRefreshLayout.setRefreshing(false);
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Log.i("volleyABC" ,"got response    "+response);
                //Toast.makeText(getActivity(),response ,Toast.LENGTH_SHORT).show();
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    TextView no_minute_text = rootView.findViewById(R.id.no_minute);
                    if(jsonArray.length() > 0) {
                        rv.setVisibility(View.VISIBLE);
                        no_minute_text.setVisibility(View.GONE);
                    }
                    else {
                        no_minute_text.setText("No minutes to display");
                    }
                    for(int i=0; i< jsonArray.length(); i++) {
                        JSONObject minutes = jsonArray.getJSONObject(i);
                        String agenda = minutes.getString("minute_objective");
                        String date = minutes.getString("minute_date");
                        String time = minutes.getString("minute_time");
                        String creator = minutes.getString("creator");
                        String points = minutes.getString("minute_details");
                        String absentee = minutes.getString("core_ab_mem_name");
                        String work = minutes.getString("minute_work");
                        JSONObject obj = new JSONObject(work);
                        JSONArray obj1 = obj.getJSONArray("minutes");

                        ArrayList<String> tasks = new ArrayList<String>();
                        ArrayList<String> person = new ArrayList<String>();

                        for (int j=0;j<obj1.length();j++) {
                            JSONObject obj2 = obj1.getJSONObject(j);
                            tasks.add(obj2.getString("task"));
                            person.add(obj2.getString("person"));
                        }

                        //Log.i("sam", tasks.toString() + " " + person.toString());

                        //in the above variable date we are not getting date in DD:MM:YYYYY
                        //so we are creating new variable date1 to get our desire format
//                        String date1 = date.substring(8,10) + "/" + date.substring(5,7) + "/" + date.substring(0,4);
                        String date1 = date.substring(8,10) + "/" + date.substring(5,7) + "/" + date.substring(0,4);

                        Log.i("finaltesting", tasks.toString() + " " + person.toString());
                        mExampleList.add(new ExampleItem(agenda, date1, time, creator, points, absentee ,tasks, person));
                        Log.i("displaying", mExampleList.get(0).getTime() + " " +mExampleList.get(0).getTask().toString() + " " + mExampleList.get(0).getPerson().toString());
                    }

                    mExampleAdapter.notifyDataSetChanged();
                    rv.setAdapter(mExampleAdapter);
                    mExampleAdapter.setOnItemClickListener(MinuteManager.this);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i("Idhar aayega" , "error");
                }
            }
        },new Response.ErrorListener()  {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e("volleyABC" ,"Got error in connecting server");
                try {
                    String statusCode = String.valueOf(error.networkResponse.statusCode);
                    Log.i("volleyABC", Integer.toString(error.networkResponse.statusCode));
                    Toast.makeText(getActivity(), "Error:-" + statusCode, Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                } catch(Exception e) {
                    Toast.makeText(getActivity(), "Check Network",Toast.LENGTH_SHORT).show();
                }
            }
        });
        mRequestQueue.add(stringRequest);
    }

    public String toString() {
        return "MinuteManager";
    }

    @Override
    public void onItemClick(int position) { //This method will call Detail Activity fragment
        Bundle bundle = new Bundle();
        ExampleItem clickedItem = mExampleList.get(position);

        //Log.i("display list 0", mExampleList.get(0).getTask().toString() + " " + mExampleList.get(0).getPerson().toString());
        //Log.i("display list 1", mExampleList.get(1).getTask().toString() + " " + mExampleList.get(1).getPerson().toString());
        //Log.i("display list 2", mExampleList.get(2).getTask().toString() + " " + mExampleList.get(2).getPerson().toString());

        bundle.putString(EXTRA_AGENDA,clickedItem.getAgenda());
        bundle.putString(EXTRA_DATE,clickedItem.getDate());
        bundle.putString(EXTRA_TIME,clickedItem.getTime());
        bundle.putString(EXTRA_CREATOR,clickedItem.getCreator());
        bundle.putString(EXTRA_POINTS,clickedItem.getPoints());
        bundle.putString(EXTRA_ABSENTEE,clickedItem.getAbsentee());
        bundle.putStringArrayList(EXTRA_TASK, clickedItem.getTask());
        bundle.putStringArrayList(EXTRA_PERSON, clickedItem.getPerson());
        //Toast.makeText(getContext(), clickedItem.getTask().toString() + " " + clickedItem.getPerson().toString(), Toast.LENGTH_SHORT).show();

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        DetailActivity detailActivity = new DetailActivity();
        detailActivity.setArguments(bundle);

        fragmentTransaction.replace(R.id.containerID,detailActivity).addToBackStack(null);
        fragmentTransaction.commit();
    }

    void swipe() {
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresher);
        //swipeRefreshLayout.setColorSchemeResources(R.color.Red,R.color.OrangeRed,R.color.Yellow,R.color.GreenYellow,R.color.BlueViolet);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                swipeRefreshLayout.setRefreshing(true);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);

                        int min = 65;
                        int max = 95;

                        parseJSON();
                    }
                },1000);
            }
        });
    }
}

