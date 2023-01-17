package com.example.csi.mAdapter;

import java.util.ArrayList;

public class ExampleItem {

    private String mAgenda;
    private String mDate;
    private String mTime;
    private String mCreator;
    private String mPoints;
    private String mAbsentee;
    private ArrayList<String> mTask;
    private ArrayList<String> mPerson;

    public ExampleItem(String agenda, String date, String time, String creator, String points, String absentee,ArrayList<String> task, ArrayList<String> person) {

        mAgenda = agenda;
        mDate = date;
        mTime = time;
        mCreator = creator;
        mPoints = points;
        mTask = task;
        mPerson = person;
        mAbsentee = absentee;
    }

    public String getAgenda() {
        return mAgenda;
    }

    public String getDate() {
        return mDate;
    }

    public String getTime() {
        return mTime;
    }

    public String getCreator() {
        return mCreator;
    }

    public String getPoints() {
        return mPoints;
    }

    public String getAbsentee() {
        return mAbsentee;
    }

    public ArrayList<String> getTask() {
        return mTask;
    }

    public ArrayList<String> getPerson() {
        return mPerson;
    }

}
