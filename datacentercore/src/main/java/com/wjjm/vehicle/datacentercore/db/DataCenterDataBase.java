package com.wjjm.vehicle.datacentercore.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DataBean.class}, version = 1)
public abstract class DataCenterDataBase extends RoomDatabase {

    public DataCenterDataBase(){
    }

    public abstract DataDAO dataDAO();
}
