package com.example.httpclient.DataBase;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

@Database(entities = {LastUsedBrewingSteps.class, AppConfig.class},  version = 2)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;
    public abstract LastUsedBrewingStepsDao lastUsedBrewingStepsDao();
    public abstract AppConfigDao appConfigDao();

    public synchronized static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = buildDatabase(context);
        }
        //---PERFORMING FAKE READ TO INITIALIZE DB
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                INSTANCE.lastUsedBrewingStepsDao().fakeRead();
                INSTANCE.appConfigDao().fakeRead();
            }
        });
        return INSTANCE;
    }

    private static AppDatabase buildDatabase(final Context context) {
        return Room.databaseBuilder(context,
                AppDatabase.class,
                "brew_it_db")
                .allowMainThreadQueries()
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        Executors.newSingleThreadExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }
                })
                .fallbackToDestructiveMigration()
                .build();
    }

    /**
     *INSERT to LastUsedBrewingSteps
     * @param
     * lastUsedBrewingSteps - | Step No | Time | Temperature|
     */
    public int insertStepToLastBrewingStepsDb(LastUsedBrewingSteps lastUsedBrewingSteps){
        //inserting BrewingStepTo DB
        lastUsedBrewingStepsDao().insertAll(lastUsedBrewingSteps);
        return 0;
    }

    /**
     *Delete all records from LastUsedBrewingSteps table
     */
    public void clearLastBrewingStepDB(){
        lastUsedBrewingStepsDao().delete();
    }

    public List<Integer> getAllStepsTemps(){
        return lastUsedBrewingStepsDao().selectAllBrewingStepsTemperatures();
    }

    public List<Integer> getAllStepsTimes(){
        return lastUsedBrewingStepsDao().selectAllBrewingStepsTimes();
    }

    /**
     * Get application status
     * If there is no status stored in db return "PENDING" status
     */
    public String getApplicationStatus(){
        String status = appConfigDao().getParameterValue(AppConfig.STATUS);
        if(status == null){
            return "PENDING";
        }
        return status;
    }

    /**
     * Set application status
     * If there is no status stored in db return "PENDING" status
     */
    public void setApplicationStatus(String status){
        appConfigDao().update(new AppConfig(AppConfig.STATUS, status));
    }

    /**
     * Get current brewing step
     * If there is no current step in db return 0
     */
    public int getCurrentBrewingStep(){
        String currentStep = appConfigDao().getParameterValue(AppConfig.CURRENT_STEP);
        if(currentStep == null){
            return 0;
        }
        return Integer.parseInt(currentStep);
    }

}
