package com.dynamiqr.android.data.local.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Isolated on-device DB for the Android course client only.
 * Safe to delete after submission — does not touch project Mongo.
 */
@Database(entities = {LocalUserEntity.class}, version = 2, exportSchema = false)
public abstract class CourseLocalDatabase extends RoomDatabase {

    public static final String DB_NAME = "dynamiqr_course_local.db";

    private static volatile CourseLocalDatabase instance;

    public abstract LocalUserDao userDao();

    public static CourseLocalDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (CourseLocalDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    CourseLocalDatabase.class,
                                    DB_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
