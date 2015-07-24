package com.nomad_gps.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.nomad_gps.tracker.PointRecord;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by kuandroid on 7/8/15.
 */
public class DBHelper extends OrmLiteSqliteOpenHelper {
    private static final String DB_NAME="buffer";
    private static final int DB_VERSION=1;
    private Dao<PointRecord,Long> recordDao;
    private RuntimeExceptionDao<PointRecord,Long> simpleRuntimeDao;
    private static DBHelper dbHelper;
    public static DBHelper instiniate(Context context){
        if (dbHelper==null)
            dbHelper=new DBHelper(context);
        return dbHelper;
    }
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, PointRecord.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, PointRecord.class, false);
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Dao<PointRecord, Long> getDao() throws SQLException {
        if(recordDao == null) {
            recordDao = getDao(PointRecord.class);
        }
        return recordDao;
    }
    public RuntimeExceptionDao<PointRecord,Long> getSimpleDao(){
        if (simpleRuntimeDao == null) {
            simpleRuntimeDao = getRuntimeExceptionDao(PointRecord.class);
        }
        return simpleRuntimeDao;
    }
    public int addRecord(PointRecord record){
        RuntimeExceptionDao dao = getSimpleDao();
        int i = dao.create(record);
        return i;
    }
    public void addAll(List<PointRecord> records){
        RuntimeExceptionDao dao = getSimpleDao();
        for (int i=0;i<records.size();i++)
            dao.create(records.get(i));
    }
    public List<PointRecord> getRecords(int limit){
        return getSimpleDao().queryForAll();
    }
    public void deleteAll(){
        getSimpleDao().delete(getSimpleDao().queryForAll());
    }
}
