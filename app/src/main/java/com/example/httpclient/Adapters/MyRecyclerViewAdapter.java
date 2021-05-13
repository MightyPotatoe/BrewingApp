package com.example.httpclient.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.httpclient.Dialogs.BrewingParametersDialog;
import com.example.httpclient.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;


    private ArrayList<Integer> desiredTemp;
    private ArrayList<Integer> timeForStep;


    // data is passed into the constructor
    public MyRecyclerViewAdapter(Context context, ArrayList<Integer> tempList, ArrayList<Integer> timeList) {
        this.mInflater = LayoutInflater.from(context);
        desiredTemp = tempList;
        timeForStep = timeList;
    }

    // inflates view
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.brewing_setting_card_layout, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(position == 0){
            holder.removeButton.setVisibility(View.INVISIBLE);
        }
        else {
            holder.removeButton.setVisibility(View.VISIBLE);
        }
        holder.tempTv.setText(desiredTemp.get(position) + "\u2103");
        holder.timeTv.setText(timeForStep.get(position) + " min");
    }



    //getTemperatureList


    public ArrayList<Integer> getDesiredTempList() {
        return desiredTemp;
    }

    public ArrayList<Integer> getTimeForStepList() {
        return timeForStep;
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return desiredTemp.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cardView;
        TextView tempTv;
        TextView timeTv;
        MaterialButton removeButton;
        MaterialButton editButton;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            tempTv = itemView.findViewById(R.id.tempValueTV);
            timeTv = itemView.findViewById(R.id.timeValueTV);
            removeButton = itemView.findViewById(R.id.removeStepButton);
            editButton = itemView.findViewById(R.id.editStepButton);
            itemView.setOnClickListener(this);

            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mClickListener != null) {
                        desiredTemp.remove(getAdapterPosition());
                        notifyItemRemoved(getAdapterPosition());
                        mClickListener.onRemoveButtonClick();
                    }
                }
            });

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mClickListener != null){
                        int index = getAdapterPosition();
                        mClickListener.onEditItemClick(desiredTemp.get(index), timeForStep.get(index), index);
                    }
                }
            });

        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null){
                mClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    // convenience method for getting data at click position
    Integer getItem(int id) {
        return desiredTemp.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public Integer getDesiredTempAtPosition(int position){
        return desiredTemp.get(position);
    }

    public void addData(int temp, int time){
        desiredTemp.add(temp);
        timeForStep.add(time);
        notifyItemInserted(desiredTemp.size()-1);
    }

    public void editData(int index, int newTemp, int newTime){
        desiredTemp.set(index, newTemp);
        timeForStep.set(index, newTime);
        notifyItemChanged(index);
    }


    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
        void onEditItemClick(int temperature, int time, int index);
        void onRemoveButtonClick();
    }
}
