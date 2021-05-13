package com.example.httpclient.Dialogs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.httpclient.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class BrewingParametersDialog extends DialogFragment {

    DialogListener dialogListener;

    TextInputEditText tempInput;
    TextInputEditText timeInput;

    ImageView timeWarningIcon;
    ImageView tempWarningIcon;

    TextView timeWarningTV;
    TextView tempWarningTV;

    int defaultTemp;
    int defaultTime;

    boolean editMode = false;
    int editingIndex;

    public BrewingParametersDialog(int defaultTemp, int defaultTime, boolean editMode, int editingIndex) {
        this(defaultTemp, defaultTime);
        this.editMode = editMode;
        this.editingIndex = editingIndex;
    }

    public BrewingParametersDialog(int defaulTemp, int defaultTime) {
        this.defaultTemp = defaulTemp;
        this.defaultTime = defaultTime;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_brewing_parameters, container, false);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //---Temp Input
        tempInput = view.findViewById(R.id.tempInput);
        //---TimeInput
        timeInput = view.findViewById(R.id.timeInput);

        //--SettingDefaultValues if provided
        if(defaultTime!=0 && defaultTemp!=0){
            tempInput.setText(String.valueOf(defaultTemp));
            timeInput.setText(String.valueOf(defaultTime));
        }

        //Time Warning
        timeWarningIcon = view.findViewById(R.id.timeWarningIcon);
        timeWarningTV = view.findViewById(R.id.timeWarningTV);
        //Temp Warning
        tempWarningIcon = view.findViewById(R.id.tempWarningIcon);
        tempWarningTV = view.findViewById(R.id.tempWarningTV);
        //---Cancel Button
        MaterialButton cancelButton = view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        //---AddButton
        MaterialButton addButton = view.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogListener != null){
                    boolean tempOk = false;
                    boolean timeOk = false;

                    int temperature = getValueFromEditText(tempInput);
                    int time = getValueFromEditText(timeInput);
                    //---Checking temp
                    if(temperature <= 0 || temperature > 100){
                        tempWarningIcon.setVisibility(View.VISIBLE);
                        tempWarningTV.setVisibility(View.VISIBLE);
                        tempOk = false;
                    }
                    else{
                        tempWarningIcon.setVisibility(View.INVISIBLE);
                        tempWarningTV.setVisibility(View.INVISIBLE);
                        tempOk = true;
                    }

                    //---Checking time
                    if(time <= 0 || time > 120){
                        timeWarningIcon.setVisibility(View.VISIBLE);
                        timeWarningTV.setVisibility(View.VISIBLE);
                        timeOk = false;
                    }
                    else{
                        timeWarningIcon.setVisibility(View.INVISIBLE);
                        timeWarningTV.setVisibility(View.INVISIBLE);
                        timeOk = true;
                    }
                    if(tempOk & timeOk){
                        dialogListener.onAcceptClick(temperature,time, editMode, editingIndex);
                        dismiss();
                    }

                }
            }
        });
    }

    private int getValueFromEditText(TextInputEditText input){
        int value;
        try {
            value = Integer.parseInt(Objects.requireNonNull(input.getText()).toString());
            return value;
        }
        catch (NumberFormatException e){
            return 0;
        }
    }




    public void setDialogListener(BrewingParametersDialog.DialogListener dialogListener){
        this.dialogListener = dialogListener;
    }

    public interface DialogListener {
        void onAcceptClick(int temperature, int time, boolean editMode, int editingIndex);
    }

}
