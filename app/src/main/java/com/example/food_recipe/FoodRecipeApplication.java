package com.example.food_recipe;

import android.app.Application;

// [추가] 비동기 작업 및 Okt 라이브러리 import
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

// [추가] WorkManager 관련 클래스를 가져옵니다.
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.example.food_recipe.worker.ExpirationCheckWorker;

/**
 * [추가] 앱의 전역적인 상태와 리소스를 관리하는 커스텀 Application 클래스입니다.
 * 앱 프로세스 시작 시 가장 먼저 실행됩니다.
 */
public class FoodRecipeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // [추가] 앱 시작 시 백그라운드에서 Okt 라이브러리를 미리 초기화합니다.
        initializeOkt();
    }

    /**
     * [추가] Okt 라이브러리의 사전 로딩을 백그라운드 스레드에서 미리 실행합니다.
     * 이 작업은 결과를 기다리지 않는 "Fire and Forget" 방식으로 동작하여,
     * 앱 시작을 지연시키지 않으면서 사용자가 검색을 시도할 때의 지연 시간을 제거합니다.
     */
    private void initializeOkt() {
        CompletableFuture.runAsync(() -> {
            // 간단한 텍스트로 분석을 한번 실행시켜 내부 사전을 메모리에 로드합니다.
            OpenKoreanTextProcessorJava.tokenize("미리 실행");
        });
    }

    /**
     * [수정] 유통기한 확인 작업을 스케줄링할 때 사용자 UID를 입력 데이터로 받습니다.
     * @param uid 작업을 예약할 사용자의 ID
     */
    public void scheduleExpirationCheck(String uid) {
        // [추가] Worker에 전달할 데이터(uid)를 생성합니다.
        Data inputData = new Data.Builder()
                .putString("uid", uid)
                .build();

        // [수정] 유통기한 확인 주기를 24시간으로 설정하고, 최신 WorkManager API를 사용합니다.
        PeriodicWorkRequest expirationCheckWorkRequest =
                new PeriodicWorkRequest.Builder(ExpirationCheckWorker.class, Duration.ofHours(24))
                        .setInputData(inputData) // [추가] Worker에 입력 데이터를 설정합니다.
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "expirationCheckWork",
                ExistingPeriodicWorkPolicy.REPLACE, // [수정] 항상 최신 사용자 정보로 작업을 갱신하도록 정책을 되돌립니다.
                expirationCheckWorkRequest);
    }

    /**
     * [추가] 예약된 유통기한 확인 작업을 취소합니다.
     * 사용자가 알림 권한을 명시적으로 거부했을 때 호출됩니다.
     */
    public void cancelExpirationCheck() {
        WorkManager.getInstance(this).cancelUniqueWork("expirationCheckWork");
    }
}
