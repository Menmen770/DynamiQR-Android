package com.dynamiqr.android.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.dynamiqr.android.core.utils.IsraeliIdValidator;
import com.dynamiqr.android.core.utils.PasswordHasher;
import com.dynamiqr.android.data.local.db.CourseLocalDatabase;
import com.dynamiqr.android.data.local.db.LocalUserDao;
import com.dynamiqr.android.data.local.db.LocalUserEntity;
import com.dynamiqr.android.data.models.User;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Course-local profile extras (ת.ז. / phone / birth) in on-device Room.
 * Auth + QR storage stay on the existing backend — this does not replace Mongo.
 */
public class LocalUserRepository {

    public interface Callback<T> {
        void onSuccess(T result);

        void onError(String message);
    }

    private final LocalUserDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    public LocalUserRepository(Context context) {
        dao = CourseLocalDatabase.getInstance(context).userDao();
    }

    /** Insert or update the course profile row linked by email. */
    public void upsertCourseProfile(String fullName,
                                    String email,
                                    String passwordOrNull,
                                    String idNumber,
                                    String phone,
                                    String birthDate,
                                    Callback<User> callback) {
        executor.execute(() -> {
            try {
                String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
                String normalizedId = idNumber == null || idNumber.trim().isEmpty()
                        ? ""
                        : IsraeliIdValidator.normalize(idNumber);

                if (!normalizedId.isEmpty()) {
                    LocalUserEntity byId = dao.findByIdNumber(normalizedId);
                    LocalUserEntity byEmail = dao.findByEmail(normalizedEmail);
                    if (byId != null && (byEmail == null || byId.id != byEmail.id)) {
                        postError(callback, "id_exists");
                        return;
                    }
                }

                long now = System.currentTimeMillis();
                LocalUserEntity entity = dao.findByEmail(normalizedEmail);
                if (entity == null) {
                    entity = new LocalUserEntity();
                    entity.email = normalizedEmail;
                    entity.createdAt = now;
                    if (passwordOrNull != null && !passwordOrNull.isEmpty()) {
                        entity.passwordHash = PasswordHasher.hash(passwordOrNull);
                    } else {
                        entity.passwordHash = "";
                    }
                }

                entity.fullName = fullName != null ? fullName.trim() : "";
                entity.idNumber = normalizedId;
                entity.phone = phone != null ? phone.trim() : "";
                entity.birthDate = birthDate != null ? birthDate.trim() : "";
                if (passwordOrNull != null && !passwordOrNull.trim().isEmpty()) {
                    entity.passwordHash = PasswordHasher.hash(passwordOrNull.trim());
                }
                entity.updatedAt = now;

                if (entity.id == 0) {
                    entity.id = dao.insert(entity);
                } else {
                    dao.update(entity);
                }
                postSuccess(callback, toUser(entity));
            } catch (Exception e) {
                postError(callback, e.getMessage() != null ? e.getMessage() : "upsert_failed");
            }
        });
    }

    public void findByEmail(String email, Callback<User> callback) {
        executor.execute(() -> {
            LocalUserEntity entity = dao.findByEmail(
                    email == null ? "" : email.trim().toLowerCase());
            if (entity == null) {
                postSuccess(callback, null);
            } else {
                postSuccess(callback, toUser(entity));
            }
        });
    }

    public void updateProfile(long id,
                              String fullName,
                              String email,
                              String idNumber,
                              String phone,
                              String birthDate,
                              String newPasswordOrNull,
                              Callback<User> callback) {
        executor.execute(() -> {
            try {
                LocalUserEntity entity = dao.findById(id);
                if (entity == null) {
                    postError(callback, "not_found");
                    return;
                }
                String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
                String normalizedId = IsraeliIdValidator.normalize(idNumber);

                LocalUserEntity byEmail = dao.findByEmail(normalizedEmail);
                if (byEmail != null && byEmail.id != id) {
                    postError(callback, "email_exists");
                    return;
                }
                if (normalizedId != null && !normalizedId.isEmpty()) {
                    LocalUserEntity byId = dao.findByIdNumber(normalizedId);
                    if (byId != null && byId.id != id) {
                        postError(callback, "id_exists");
                        return;
                    }
                }

                entity.fullName = fullName.trim();
                entity.email = normalizedEmail;
                entity.idNumber = normalizedId;
                entity.phone = phone.trim();
                entity.birthDate = birthDate.trim();
                if (newPasswordOrNull != null && !newPasswordOrNull.trim().isEmpty()) {
                    entity.passwordHash = PasswordHasher.hash(newPasswordOrNull.trim());
                }
                entity.updatedAt = System.currentTimeMillis();
                dao.update(entity);
                postSuccess(callback, toUser(entity));
            } catch (Exception e) {
                postError(callback, e.getMessage() != null ? e.getMessage() : "update_failed");
            }
        });
    }

    public void deleteAccount(long id, Callback<Boolean> callback) {
        executor.execute(() -> {
            int deleted = dao.deleteById(id);
            if (deleted > 0) {
                postSuccess(callback, true);
            } else {
                postError(callback, "not_found");
            }
        });
    }

    /** Merge course-local extras onto a backend user (keeps Mongo id + JWT session identity). */
    public static User mergeServerAndLocal(User serverUser, User localUser) {
        if (serverUser == null) {
            return localUser;
        }
        if (localUser != null) {
            serverUser.setIdNumber(localUser.getIdNumber());
            serverUser.setPhone(localUser.getPhone());
            serverUser.setBirthDate(localUser.getBirthDate());
            serverUser.setLocalRoomId(localUser.getLocalRoomId());
        }
        return serverUser;
    }

    public static User toUser(LocalUserEntity entity) {
        User user = new User();
        user.setLocalRoomId(entity.id);
        user.setEmail(entity.email);
        user.setFullName(entity.fullName);
        user.setIdNumber(entity.idNumber);
        user.setPhone(entity.phone);
        user.setBirthDate(entity.birthDate);
        user.setHasPassword(true);
        return user;
    }

    private <T> void postSuccess(Callback<T> callback, T result) {
        main.post(() -> callback.onSuccess(result));
    }

    private <T> void postError(Callback<T> callback, String message) {
        main.post(() -> callback.onError(message));
    }
}
