package com.example.myapplication.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.example.myapplication.core.utils.IsraeliIdValidator;
import com.example.myapplication.core.utils.PasswordHasher;
import com.example.myapplication.data.local.db.CourseLocalDatabase;
import com.example.myapplication.data.local.db.LocalUserDao;
import com.example.myapplication.data.local.db.LocalUserEntity;
import com.example.myapplication.data.models.User;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Course-local user CRUD against on-device Room DB only (not Mongo).
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

    public void register(String fullName,
                         String email,
                         String password,
                         String idNumber,
                         String phone,
                         String birthDate,
                         Callback<User> callback) {
        executor.execute(() -> {
            try {
                String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
                String normalizedId = IsraeliIdValidator.normalize(idNumber);
                if (dao.findByEmail(normalizedEmail) != null) {
                    postError(callback, "email_exists");
                    return;
                }
                if (dao.findByIdNumber(normalizedId) != null) {
                    postError(callback, "id_exists");
                    return;
                }
                long now = System.currentTimeMillis();
                LocalUserEntity entity = new LocalUserEntity();
                entity.fullName = fullName.trim();
                entity.email = normalizedEmail;
                entity.passwordHash = PasswordHasher.hash(password);
                entity.idNumber = normalizedId;
                entity.phone = phone.trim();
                entity.birthDate = birthDate.trim();
                entity.createdAt = now;
                entity.updatedAt = now;
                long rowId = dao.insert(entity);
                entity.id = rowId;
                postSuccess(callback, toUser(entity));
            } catch (Exception e) {
                postError(callback, e.getMessage() != null ? e.getMessage() : "register_failed");
            }
        });
    }

    public void login(String email, String password, Callback<User> callback) {
        executor.execute(() -> {
            try {
                String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
                LocalUserEntity entity = dao.findByEmail(normalizedEmail);
                if (entity == null || !PasswordHasher.matches(password, entity.passwordHash)) {
                    postError(callback, "invalid_credentials");
                    return;
                }
                postSuccess(callback, toUser(entity));
            } catch (Exception e) {
                postError(callback, e.getMessage() != null ? e.getMessage() : "login_failed");
            }
        });
    }

    public void getById(long id, Callback<User> callback) {
        executor.execute(() -> {
            LocalUserEntity entity = dao.findById(id);
            if (entity == null) {
                postError(callback, "not_found");
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
                LocalUserEntity byId = dao.findByIdNumber(normalizedId);
                if (byId != null && byId.id != id) {
                    postError(callback, "id_exists");
                    return;
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

    public static User toUser(LocalUserEntity entity) {
        User user = new User();
        user.setId(String.valueOf(entity.id));
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
