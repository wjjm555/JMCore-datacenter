package com.wjjm.vehicle.datacentercore;

import android.content.Context;

import androidx.room.Room;

import com.wjjm.vehicle.datacentercore.db.DataCenterDataBase;
import com.wjjm.vehicle.datacentercore.db.DataDAO;

import static com.wjjm.vehicle.datacentercore.Constant.TABLE_FILE_NAME;

public class DataCenter {

    public static DataCenter getInstance() {
        return InnerHolder.INSTANCE;
    }

    private static class InnerHolder {
        private static final DataCenter INSTANCE = new DataCenter();
    }

    private DataCenterDataBase dataBase;

    private DataCenter() {

    }

    public void init(Context context) {
        synchronized (this) {
            dataBase = Room.databaseBuilder(context, DataCenterDataBase.class, TABLE_FILE_NAME)
//                .addMigrations()
                    .allowMainThreadQueries().build();
        }
    }

    public DataDAO getDao() {
        synchronized (this) {
            return dataBase.dataDAO();
        }
    }


//    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
//        @Override
//        public void migrate(SupportSQLiteDatabase database) {
//            database.execSQL("ALTER TABLE data_center ADD COLUMN alias Text");
//        }
//    };
}
