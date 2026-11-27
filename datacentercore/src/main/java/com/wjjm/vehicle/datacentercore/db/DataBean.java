package com.wjjm.vehicle.datacentercore.db;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.wjjm.vehicle.datacentercore.Constant;

@Entity(tableName = Constant.TABLE_NAME, indices = {@Index(value = "key", unique = true)})
public class DataBean {

    //    @Expose(serialize = false, deserialize = false)
    @PrimaryKey(autoGenerate = true)
    public Long id;

    private String key;

    private String value;

    public DataBean() {

    }

    @Ignore
    public DataBean(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public Long getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public final String toString() {
        return key + ':' + value;
    }
}
