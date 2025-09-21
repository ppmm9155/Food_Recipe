plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.food_recipe"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.food_recipe"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}


dependencies {

    implementation(libs.appcompat) // AndroidX AppCompat 라이브러리 (이전 Android 버전과의 호환성 및 다양한 UI 요소 제공)
    implementation(libs.material) // Google의 머티리얼 디자인 가이드라인을 따르는 UI 컴포넌트 모음
    implementation(libs.activity) // Jetpack Activity 라이브러리 (Activity Result API, ComponentActivity 등 포함)
    implementation(libs.constraintlayout) // ConstraintLayout을 사용하여 유연하고 효율적인 UI 레이아웃 구성

    implementation(libs.firebase.auth) // Firebase 인증 라이브러리 (이메일, 소셜 미디어, 익명 로그인 등 다양한 인증 방식 지원)
    implementation(libs.credentials) // 사용자 인증 정보(비밀번호, 패스키 등) 관리를 위한 Credential Manager API
    implementation(libs.credentials.play.services.auth) // Google Play 서비스를 통해 제공되는 인증 정보(예: Google 비밀번호 사용) 관리 확장
    implementation(libs.googleid) // Google ID 서비스 라이브러리: Google 계정을 사용한 사용자 인증(예: 'Google로 로그인') 및 Google의 원탭(One Tap) 로그인과 같은 간소화된 로그인 환경을 구현하는 데 사용됩니다.
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore)
    implementation(libs.google.firebase.firestore) // Firebase Analytics 라이브러리: 사용자 행동 및 앱 사용 패턴을 추적하고 분석하여 앱 사용에 대한 인사이트를 얻는 데 사용됩니다.

    testImplementation(libs.junit) // 로컬 JVM 환경에서 실행되는 단위 테스트를 위한 JUnit 프레임워크
    androidTestImplementation(libs.ext.junit) // 안드로이드 기기 또는 에뮬레이터에서 실행되는 계측 테스트를 위한 JUnit 확장 라이브러리
    androidTestImplementation(libs.espresso.core) // 안드로이드 UI 테스트 자동화를 위한 Espresso 프레임워크의 핵심 라이브러리

    implementation(libs.core.splashscreen) //SplashScreen API
    implementation(libs.material.v1120)
}