package com.example.httpclient.DataBase;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity
public class LastUsedBrewingSteps {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "step_no")
    public int stepNo;

    @ColumnInfo(name = "time")
    public int time;

    @ColumnInfo(name = "temperature")
    public int temperature;

    public LastUsedBrewingSteps(int stepNo, int time, int temperature) {
        this.stepNo = stepNo;
        this.time = time;
        this.temperature = temperature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LastUsedBrewingSteps lastUsedBrewingSteps = (LastUsedBrewingSteps) o;
        return stepNo == lastUsedBrewingSteps.stepNo &&
                time == lastUsedBrewingSteps.time &&
                temperature == lastUsedBrewingSteps.temperature;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stepNo, time, temperature);
    }

}
