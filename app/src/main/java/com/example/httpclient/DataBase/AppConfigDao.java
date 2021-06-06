package com.example.httpclient.DataBase;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AppConfigDao {
    @Query("select count(1=1)")
    int fakeRead();

    @Insert
    void insertAll(AppConfig... appConfigs);

    @Query("select value from AppConfig where parameter = :parameterName")
    String getParameterValue(String parameterName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void update(AppConfig appConfig);

    @Query("delete from AppConfig")
    void delete();

}
