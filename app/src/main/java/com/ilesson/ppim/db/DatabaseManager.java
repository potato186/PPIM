package com.ilesson.ppim.db;

import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

public class DatabaseManager {
    private final String TAG = DatabaseManager.class.getSimpleName();
    private DbManager.DaoConfig daoConfig;
    private static DbManager dbManager;
    private final String DB_NAME = "pp_im.db";
    private final int DB_VERSION = 1;

    private DatabaseManager() {

        daoConfig = new DbManager.DaoConfig()
                .setDbName(DB_NAME)
                .setDbVersion(DB_VERSION)
                .setDbOpenListener(db -> db.getDatabase().enableWriteAheadLogging())
                .setDbUpgradeListener((db, oldVersion, newVersion) -> {
                    try {
                        db.dropDb();
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                }).setTableCreateListener((db, table) -> {
                });
        dbManager = x.getDb(daoConfig);
    }

    public static DbManager getInstance() {
        if (dbManager == null) {
            DatabaseManager databaseOpenHelper = new DatabaseManager();
        }
        return dbManager;
    }


}
