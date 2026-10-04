package com.example.myapplication.data.local.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface LocalUserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(LocalUserEntity user);

    @Update
    int update(LocalUserEntity user);

    @Query("DELETE FROM course_users WHERE id = :id")
    int deleteById(long id);

    @Query("SELECT * FROM course_users WHERE id = :id LIMIT 1")
    LocalUserEntity findById(long id);

    @Query("SELECT * FROM course_users WHERE email = :email LIMIT 1")
    LocalUserEntity findByEmail(String email);

    @Query("SELECT * FROM course_users WHERE id_number = :idNumber LIMIT 1")
    LocalUserEntity findByIdNumber(String idNumber);

    @Query("SELECT * FROM course_users ORDER BY created_at DESC")
    List<LocalUserEntity> getAll();
}
