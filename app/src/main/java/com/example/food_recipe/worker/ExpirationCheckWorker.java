package com.example.food_recipe.worker;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.food_recipe.R;
import com.example.food_recipe.main.MainActivity;
import com.example.food_recipe.repository.ExpirationRepository;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ExpirationCheckWorker extends Worker {

    private static final String TAG = "ExpirationCheckWorker";
    private final ExpirationRepository repository;
    private final Context context;
    private static final String CHANNEL_ID = "EXPIRATION_REMINDER_CHANNEL";
    private static final String CHANNEL_NAME = "유통기한 알림";
    private final AtomicInteger notificationId = new AtomicInteger(1);

    public ExpirationCheckWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.repository = new ExpirationRepository();
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "WorkManager 작업 실행: 유통기한 확인 로직을 시작합니다.");
        createNotificationChannel();

        try {
            QuerySnapshot querySnapshot = Tasks.await(repository.getExpiringIngredientsQuery().get());

            if (querySnapshot.isEmpty()) {
                Log.d(TAG, "알림을 보낼 재료가 없습니다.");
                return Result.success();
            }

            Log.d(TAG, "알림 보낼 재료 " + querySnapshot.size() + "개 발견.");

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                String ingredientName = document.getString("ingredientName");
                // [추가] 유통기한 날짜를 Timestamp 형태로 가져옵니다.
                Timestamp expirationTimestamp = document.getTimestamp("expirationDate");

                if (ingredientName == null || ingredientName.isEmpty() || expirationTimestamp == null) {
                    Log.w(TAG, "재료 이름 또는 유통기한 정보가 없어 알림을 건너뜁니다: " + document.getId());
                    continue;
                }

                // [추가] D-day 계산 및 알림 메시지 생성 로직
                long daysRemaining = getDaysRemaining(expirationTimestamp.toDate());
                String notificationText;
                if (daysRemaining <= 0) {
                    notificationText = ingredientName + "의 유통기한이 오늘 만료됩니다!";
                } else {
                    notificationText = ingredientName + "의 유통기한이 " + daysRemaining + "일 남았습니다.";
                }

                Log.d(TAG, notificationText + " 알림을 보냅니다.");
                sendNotification(notificationText);

                Tasks.await(repository.updateNotificationStatus(document.getId()));
                Log.d(TAG, document.getId() + "의 상태를 SENT로 변경했습니다.");
            }

            return Result.success();

        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "WorkManager 작업 실패", e);
            return Result.retry();
        }
    }

    // [추가] D-day 계산을 위한 헬퍼 메서드
    private long getDaysRemaining(Date expirationDate) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar expirationCal = Calendar.getInstance();
        expirationCal.setTime(expirationDate);
        expirationCal.set(Calendar.HOUR_OF_DAY, 0);
        expirationCal.set(Calendar.MINUTE, 0);
        expirationCal.set(Calendar.SECOND, 0);
        expirationCal.set(Calendar.MILLISECOND, 0);

        long diff = expirationCal.getTimeInMillis() - today.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(diff);
    }


    private void sendNotification(String notificationText) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("유통기한 임박 알림")
                .setContentText(notificationText) // [수정] 동적으로 생성된 메시지를 사용합니다.
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "알림 권한이 없어 알림을 보낼 수 없습니다.");
                return;
            }
        }
        notificationManager.notify(notificationId.getAndIncrement(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("유통기한이 임박한 재료에 대한 알림을 받습니다.");
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
