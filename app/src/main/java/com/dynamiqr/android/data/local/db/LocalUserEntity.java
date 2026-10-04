package com.dynamiqr.android.data.local.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Course-only local user table. Stored in a separate SQLite DB on-device —
 * never synced to the shared Mongo/backend used by other clients.
 */
@Entity(
        tableName = "course_users",
        indices = {
                @Index(value = {"email"}, unique = true)
        }
)
public class LocalUserEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "id_number")
    public String idNumber;

    @ColumnInfo(name = "full_name")
    public String fullName;

    public String email;

    public String phone;

    @ColumnInfo(name = "birth_date")
    public String birthDate;

    @ColumnInfo(name = "password_hash")
    public String passwordHash;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;
}
