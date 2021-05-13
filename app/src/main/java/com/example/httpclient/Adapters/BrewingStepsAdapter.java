package com.example.httpclient.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.httpclient.R;
import com.example.httpclient.Utilities.SharedPreferencesEditor;

import java.util.ArrayList;

public class BrewingStepsAdapter extends RecyclerView.Adapter<BrewingStepsAdapter.ViewHolder>  {

    private LayoutInflater mInflater;

    private ArrayList<Integer> originalDesiredTemp;
    private ArrayList<Integer> originalTimeForStep;
    private ArrayList<Integer> desiredTemp;
    private ArrayList<Integer> timeForStep;

    int totalSteps;
    int currentStep;

    SharedPreferencesEditor sharedPreferencesEditor;


    // data is passed into the constructor
    public BrewingStepsAdapter(Context context, SharedPreferencesEditor sharedPreferences) {
        this.mInflater = LayoutInflater.from(context);
        this.sharedPreferencesEditor = sharedPreferences;

        //Initializing RecyclerView with previous brewing steps
        ArrayList<Integer> tempList = new ArrayList<>();
        ArrayList<Integer> timeList = new ArrayList<>();
        for(int i = 0; i<5; i++){
            int temp = sharedPreferences.getInt("Temp"+i, 0);
            int time = sharedPreferences.getInt("Time"+i, 0);
            if(temp!=0 && time !=0){
                tempList.add(temp);
                timeList.add(time);
            }
        }
        originalDesiredTemp = desiredTemp = tempList;
        originalTimeForStep = timeForStep = timeList;
        currentStep = sharedPreferences.getInt("CURRENT_STEP", 0);
        if(currentStep < 0){
            currentStep = 0;
        }
        cutToStep(currentStep);
        totalSteps = originalDesiredTemp.size();
    }

    // inflates view
    @Override
    public BrewingStepsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.next_step_card, parent, false);
        return new BrewingStepsAdapter.ViewHolder(view);
    }

    // binds the data to the TextView
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(BrewingStepsAdapter.ViewHolder holder, int position) {
        holder.tempTv.setText(desiredTemp.get(position) + "\u2103");
        holder.timeTv.setText(timeForStep.get(position) + " min");
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tempTv;
        TextView timeTv;

        ViewHolder(View itemView) {
            super(itemView);
            tempTv = itemView.findViewById(R.id.currentStepTemp);
            timeTv = itemView.findViewById(R.id.currentStepTimeTV);
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return desiredTemp.size();
    }

    private void cutToStep(int stepToCut){
        desiredTemp = new ArrayList<>(originalDesiredTemp);
        timeForStep = new ArrayList<>(originalTimeForStep);

        desiredTemp.subList(0, stepToCut+1).clear();
        timeForStep.subList(0, stepToCut+1).clear();
        notifyDataSetChanged();
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public int getCurrentStepTemperature(){
        return originalDesiredTemp.get(currentStep);
    }

    public int getCurrentStepTime(){
        return originalTimeForStep.get(currentStep);
    }

    public boolean nextStep(){
        if(hasNextStep()){
            currentStep++;
            sharedPreferencesEditor.putInt(SharedPreferencesEditor.CURRENT_STEP, currentStep);
            return true;
        }
        return false;
    }

    public boolean hasNextStep(){
        return currentStep + 1 <= totalSteps - 1;
    }

    public void updateSteps (){
        cutToStep(currentStep);
        notifyDataSetChanged();
    }

    public void reset(){
        currentStep = 0;
        cutToStep(currentStep);
        notifyDataSetChanged();
    }


}
