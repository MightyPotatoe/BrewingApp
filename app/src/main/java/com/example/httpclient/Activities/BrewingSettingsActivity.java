package com.example.httpclient.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.httpclient.Adapters.MyRecyclerViewAdapter;
import com.example.httpclient.DataBase.AppDatabase;
import com.example.httpclient.DataBase.LastUsedBrewingSteps;
import com.example.httpclient.Dialogs.BrewingParametersDialog;
import com.example.httpclient.R;
import com.example.httpclient.Utilities.IpDetector;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class BrewingSettingsActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener, BrewingParametersDialog.DialogListener {

    MyRecyclerViewAdapter adapter;
    RecyclerView recyclerView;
    TextView emptyListTV;
    BrewingParametersDialog brewingParametersDialog;
    MaterialButton startBrewingButton;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brewing_settings);

        setContentView(R.layout.content);

        emptyListTV = findViewById(R.id.noStepsTv);
        startBrewingButton = findViewById(R.id.startBrewingButton);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        sharedPreferences = getSharedPreferences("BREWING_SETTINGS", Context.MODE_PRIVATE);

        //Initializing RecyclerView with previous brewing steps
        final AppDatabase db = AppDatabase.getInstance(this);
        adapter = new MyRecyclerViewAdapter(this, new ArrayList<>(db.getAllStepsTemps()), new ArrayList<>(db.getAllStepsTimes()));
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        if(adapter.getItemCount() == 0){
            emptyListTV.setVisibility(View.VISIBLE);
            startBrewingButton.setEnabled(false);
        }
        else {
            emptyListTV.setVisibility(View.INVISIBLE);
            startBrewingButton.setEnabled(true);
        }


    }




    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public void onEditItemClick(int temperature, int time, int index) {
        brewingParametersDialog = new BrewingParametersDialog(temperature,time, true, index);
        brewingParametersDialog.setDialogListener(this);
        FragmentManager fm = getSupportFragmentManager();
        brewingParametersDialog.show(fm, "fragment_edit_name");
    }

    public void onAddBrewingSettingButtonClick(View view) {
        if(adapter.getItemCount() < 5){
            brewingParametersDialog = new BrewingParametersDialog(0,0);
            brewingParametersDialog.setDialogListener(this);
            FragmentManager fm = getSupportFragmentManager();
            brewingParametersDialog.show(fm, "fragment_edit_name");
        }
        else{
            Toast.makeText(this, "Yuy have reached maximum number of brewing steps.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onAcceptClick(int temperature, int time, boolean editMode, int editingIndex) {
        if(!editMode){
            adapter.addData(temperature, time);
        }
        else {
            adapter.editData(editingIndex, temperature, time);
        }
        emptyListTV.setVisibility(View.INVISIBLE);
        startBrewingButton.setEnabled(true);
        updateEditedSteps();
    }

    public void onStartBrewingButtonClick(View view) {
        updateEditedSteps();
        Intent intent = new Intent(this, BrewingActivity.class);
        startActivity(intent);
    }

    public void updateEditedSteps(){
        ArrayList<Integer> tempList = adapter.getDesiredTempList();
        ArrayList<Integer> timeList = adapter.getTimeForStepList();

//        for(int i = 0; i < 5 ; i++){
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            String timeKey = "Time" + i;
//            String tempKey = "Temp" + i;
//            if(i < tempList.size()){
//                editor.putInt(timeKey, timeList.get(i));
//                editor.putInt(tempKey, tempList.get(i));
//            }
//            else{
//                editor.putInt(timeKey, 0);
//                editor.putInt(tempKey, 0);
//            }
//            editor.commit();
//        }


        final AppDatabase db = AppDatabase.getInstance(this);
        db.clearLastBrewingStepDB();

        for(int i = 0; i < tempList.size() ; i++){
            LastUsedBrewingSteps lastUsedBrewingSteps = new LastUsedBrewingSteps(i+1, timeList.get(i), tempList.get(i));
            db.insertStepToLastBrewingStepsDb(lastUsedBrewingSteps);
        }


    }

    @Override
    public void onRemoveButtonClick() {
        updateEditedSteps();
    }
}