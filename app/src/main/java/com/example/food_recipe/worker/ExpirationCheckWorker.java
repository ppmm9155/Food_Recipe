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
import java.text.SimpleDateFormat;
import java.util.Locale;

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

        // [추가] Worker 입력 데이터에서 사용자 UID를 가져옵니다.
        String uid = getInputData().getString("uid");

        // [추가] UID가 없으면 작업을 중단합니다.
        if (uid == null || uid.isEmpty()) {
            Log.e(TAG, "사용자 UID가 없어 작업을 중단합니다. 로그인 상태를 확인해주세요.");
            return Result.failure();
        }
        Log.d(TAG, "사용자 UID(" + uid + ")에 대한 작업을 시작합니다.");

        createNotificationChannel();

        try {
            // [수정] Repository를 호출할 때 UID를 전달합니다.
            QuerySnapshot querySnapshot = Tasks.await(repository.getExpiringIngredientsQuery(uid).get());

            if (querySnapshot.isEmpty()) {
                Log.d(TAG, "알림을 보낼 재료가 없습니다.");
                return Result.success();
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                String ingredientName = document.getString("ingredientName");
                Timestamp expirationTimestamp = document.getTimestamp("expirationDate");
                String status = document.getString("notificationStatus");

                if (ingredientName == null || ingredientName.isEmpty() || expirationTimestamp == null) {
                    Log.w(TAG, "확인 실패: 문서 " + document.getId() + "의 재료 이름 또는 유통기한 정보가 없습니다.");
                    continue;
                }

                Date expirationDate = expirationTimestamp.toDate();
                long daysRemaining = getDaysRemaining(expirationDate);

                Log.d(TAG, "확인 중: " + ingredientName + " (유통기한: " + sdf.format(expirationDate) + ", D-" + daysRemaining + ")");

                if ("PENDING".equals(status)) {
                    if (daysRemaining >= 0 && daysRemaining <= 3) {
                        String notificationText;
                        if (daysRemaining == 0) {
                            notificationText = ingredientName + "의 유통기한이 오늘 만료됩니다!";
                        } else {
                            notificationText = ingredientName + "의 유통기한이 " + daysRemaining + "일 남았습니다.";
                        }
                        Log.i(TAG, "알림 발송: " + ingredientName + " (" + notificationText + ")");
                        sendNotification(notificationText);
                        Tasks.await(repository.updateNotificationStatus(document.getId()));
                        Log.d(TAG, "상태 변경: " + ingredientName + " -> SENT");
                    } else {
                        Log.d(TAG, "알림 제외: " + ingredientName + " (사유: 유통기한이 " + daysRemaining + "일 남아 알람 대상이 아님)");
                    }
                } else {
                    Log.d(TAG, "알림 제외: " + ingredientName + " (사유: 이미 알람을 보냄)");
                }
            }

            return Result.success();

        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "WorkManager 작업 실패", e);
            return Result.retry();
        }
    }

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
                .setContentText(notificationText)
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
