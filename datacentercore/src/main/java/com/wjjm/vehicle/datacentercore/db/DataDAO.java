package com.wjjm.vehicle.datacentercore.db;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import static com.wjjm.vehicle.datacentercore.Constant.TABLE_NAME;

@Dao
public interface DataDAO {

    @Query("SELECT * FROM " + TABLE_NAME)
    DataBean[] queryAllData();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertData(DataBean... beans);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE `key` IN (:selectionArgs) ORDER BY :sortOrder")
    Cursor queryData(String[] selectionArgs, String sortOrder);

    @Query("DELETE FROM " + TABLE_NAME + " WHERE `key` IN (:selectionArgs)")
    int deleteData(String[] selectionArgs);

    @Query("UPDATE " + TABLE_NAME + " SET `value` = :value WHERE `key` IN (:selectionArgs)")
    int updateData(String value, String[] selectionArgs);

}
