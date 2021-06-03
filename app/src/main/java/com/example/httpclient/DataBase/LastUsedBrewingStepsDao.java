package com.example.httpclient.DataBase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface LastUsedBrewingStepsDao {

    @Query("select count(1=1)")
    int fakeRead();

    @Insert
    void insertAll(LastUsedBrewingSteps... lastUsedBrewingSteps);

    @Query("delete from LastUsedBrewingSteps")
    void delete();

    @Query("select temperature from LastUsedBrewingSteps order by step_no asc")
    List<Integer> selectAllBrewingStepsTemperatures();

    @Query("select time from LastUsedBrewingSteps order by step_no asc")
    List<Integer> selectAllBrewingStepsTimes();

}
