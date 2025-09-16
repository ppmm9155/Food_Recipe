// 최상위 빌드 파일: 모든 하위 프로젝트/모듈에 공통 설정을 추가할 수 있습니다.
plugins {
    // Android 애플리케이션 플러그인을 프로젝트의 하위 모듈에서 사용할 수 있도록 정의합니다.
    // 'apply false'는 이 플러그인을 프로젝트 전체에 직접 적용하는 것이 아니라,
    // 각 모듈(예: app 모듈)의 build.gradle.kts 파일에서 필요에 따라 'alias(libs.plugins.android.application)'와 같이 하여
    // 실제로 적용할 수 있게 해줍니다. 버전은 libs.versions.toml을 통해 관리됩니다.
    alias(libs.plugins.android.application) apply false

    // Google 서비스 플러그인을 프로젝트의 하위 모듈에서 사용할 수 있도록 정의합니다.
    // Firebase와 같은 Google 서비스를 사용하기 위해 필요합니다.
    // 'apply false'의 의미는 위와 동일하며, 모듈 수준에서 실제로 적용합니다.
    // 버전은 libs.versions.toml을 통해 관리됩니다.
    alias(libs.plugins.google.gms.google.services) apply false
    

}
