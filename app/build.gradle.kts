plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    // [추가] Secrets Gradle Plugin을 app 모듈에 적용합니다.
    alias(libs.plugins.secrets.gradle.plugin)
}

android {
    namespace = "com.example.food_recipe"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.food_recipe"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // [추가] buildFeatures 블록을 추가하여 BuildConfig 클래스 자동 생성을 활성화합니다.
    // 이를 통해 local.properties에 저장된 비밀 키를 코드에서 안전하게 참조할 수 있습니다.
    buildFeatures {
        buildConfig = true
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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore)
    implementation(libs.google.firebase.firestore)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.core.splashscreen)

    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.play.services.auth)
    implementation(libs.coordinatorlayout)

    implementation("com.github.bumptech.glide:glide:5.0.5")
    implementation("com.algolia:algoliasearch-android:3.+")

    val lifecycle_version = "2.8.4"
    implementation("androidx.lifecycle:lifecycle-viewmodel:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-livedata:$lifecycle_version")

    // [추가] Gson 라이브러리 의존성을 추가합니다. SharedPreferences에 객체를 저장/로드하기 위해 사용합니다.
    implementation("com.google.code.gson:gson:2.13.2")

    // [추가] Open Korean Text(Okt) 라이브러리 의존성을 추가합니다. 한국어 형태소 분석을 위해 사용합니다.
    implementation(libs.open.korean.text)

    // [추가] WorkManager 라이브러리 (신뢰성 있는 백그라운드 작업을 위해 필요)
    val work_version = "2.9.0"
    implementation(libs.work.runtime.ktx)
}
