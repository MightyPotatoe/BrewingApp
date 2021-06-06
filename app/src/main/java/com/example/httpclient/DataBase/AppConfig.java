package com.example.httpclient.DataBase;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(indices = {@Index(value = {"parameter"},
        unique = true)})
public class AppConfig {

    public final static String STATUS = "STATUS";
    public final static String CURRENT_STEP = "CURRENT_STEP";

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "parameter")
    public String parameter;

    @ColumnInfo(name = "value")
    public String value;

    public AppConfig(String parameter, String value) {
        this.parameter = parameter;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppConfig appConfig = (AppConfig) o;
        return parameter == appConfig.parameter &&
                value == appConfig.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameter, value);
    }
}
