package com.example.food_recipe;

import android.app.Application;

// [추가] 비동기 작업 및 Okt 라이브러리 import
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import java.util.concurrent.CompletableFuture;

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
}
