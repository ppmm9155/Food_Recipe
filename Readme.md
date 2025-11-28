<div align=center style="margin-bottom:30px">
  <img src="documents/Applogo.png">
</div>

# 요리GO조리GO

<br>

> 냉장고 속 잠자고 있는 재료들을 깨워줄 똑똑한 식재료 관리 및 레시피 추천 앱입니다.
> Firebase와 Algolia를 연동하여 실시간 데이터 동기화와 강력한 검색 기능을 구현한 안드로이드 네이티브 앱입니다.

<br>

## 캡스톤 디자인 전시회

<div align=center>
  <img src="assets/images/showcase/booth_display.jpg" width="600">
</div>

<br>

## 👑 주요 기능

* **회원 시스템:** Firebase Authentication을 이용한 이메일/비밀번호 및 Google 소셜 로그인/회원가입 기능을 제공합니다.
* **나만의 냉장고:** 사용자가 보유한 식재료를 냉장/냉동/실온으로 구분하여 등록하고 유통기한을 관리할 수 있습니다.
* **유통기한 알림:** Android WorkManager를 활용하여, 유통기한이 임박한 재료가 있을 시 백그라운드에서 푸시 알림을 전송합니다.
* **레시피 추천:** 홈 화면에서 인기 레시피, 최근 본 레시피, 즐겨찾기한 레시피 등 맞춤형 목록을 제공합니다.
* **강력한 레시피 검색:**
    * **Algolia** 검색 엔진을 연동하여 10만 개의 레시피 중 빠르고 정확한 검색(오타 수정, 하이라이팅)을 지원합니다.
    * '내 냉장고 재료로 검색하기' 기능을 통해 현재 보유한 재료를 조합하여 만들 수 있는 레시피를 검색합니다.
* **즐겨찾기:** 마음에 드는 레시피를 저장하고 '즐겨찾기' 탭에서 모아볼 수 있습니다.
* **백엔드 로직:** Firebase Functions (TypeScript)를 사용하여 Firestore의 데이터 변경(추천 수, 재료)을 감지하고 Algolia 인덱스 동기화, 유통기한 알림 관리 등 서버 측 로직을 자동 수행합니다.

<br>

## 📌 프로젝트 정보

* **프로젝트:** 졸업작품 및 캡스톤 디자인
* **주제:** 식재료 관리 및 레시피 추천 안드로이드 앱
* **개발자:** [박기준](https://github.com/ppmm9155), [하종수](https://github.com/sanddunn)
* **개발 기간:** 2025.09. ~ 2025.11.
* **주요 목표:**
    1.  Firebase (Auth, Firestore, Functions)와 Algolia 검색 엔진을 연동한 네이티브 안드로이드 앱 개발.
    2.  MVP (Model-View-Presenter) 아키텍처 패턴을 적용하여 비즈니스 로직과 UI 분리.
    3.  WorkManager, Navigation Component 등 최신 AndroidX 라이브러리 활용.

<br>

## 🍉 기술 스택

### 📱 App (Android)
![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Android Studio](https://img.shields.io/badge/Android_Studio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)

### ☁️ Backend & Database
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Firestore](https://img.shields.io/badge/Firestore-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Functions](https://img.shields.io/badge/Functions-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Algolia](https://img.shields.io/badge/Algolia-003DFF?style=for-the-badge&logo=algolia&logoColor=white)
![Node.js](https://img.shields.io/badge/Node.js-339933?style=for-the-badge&logo=nodedotjs&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)

<br>

## ⚙️ 설치 방법

1.  **Firebase 프로젝트 설정**
    *   Firebase Console에서 새 프로젝트를 생성하고 Android 앱을 등록합니다.
    *   **(필수)** 프로젝트 설정에서 `google-services.json` 파일을 다운로드하여 `app/` 디렉토리에 추가해야 합니다. 이 파일은 앱이 Firebase 서비스에 연결하는 데 필요합니다.
    *   Authentication (이메일/비밀번호, Google 로그인), Firestore, Functions를 활성화합니다.

2.  **Algolia 계정 설정**
    *   Algolia에서 새 애플리케이션을 생성합니다.
    *   **(필수)** API Keys 메뉴에서 **Application ID**와 **Admin API Key**를 확인해야 합니다.
    *   `functions/src/index.ts` 파일 상단의 설정 변수에 위에서 확인한 값을 입력합니다.

3.  **Android 앱 빌드**
    *   Android Studio에서 프로젝트를 엽니다.
    *   Gradle 동기화 후 앱을 빌드합니다.

<br>

## 🚀 시작 가이드

1.  **Firebase Functions 배포**
    *   `functions` 디렉토리에서 `npm install`을 실행하여 모든 종속성을 설치합니다.
    *   `firebase deploy --only functions` 명령어로 함수를 배포합니다.

2.  **데이터 파이프라인 실행 (최초 1회)**
    *   `data_pipeline` 디렉토리의 Python 스크립트를 실행하여 레시피 데이터를 Firestore와 Algolia에 색인합니다.

3.  **앱 실행**
    *   Android Studio에서 앱을 실행하여 회원가입 후 기능을 테스트합니다.

<br>

## 🎀 아키텍처

본 프로젝트는 MVP 패턴을 기반으로 하며, Firebase와 Algolia의 역할을 명확히 분리하여 데이터 흐름을 관리합니다.

### 데이터 흐름 (Firebase & Algolia)
> **시나리오 1: '검색' 외의 모든 상황 (홈 화면, 즐겨찾기 등)**
> * **주인공:** ✅ **Firebase Firestore**
> * **역할:** 정해진 규칙(추천 수, 최신순)에 따른 데이터 **'정렬'** 및 **'추천'**.
> * **동작:** 안드로이드 앱이 "인기 레시피 10개를 `recommend_count` 순으로 줘"라고 Firestore에 직접 요청합니다.
>
> **시나리오 2: 사용자가 '검색'을 실행할 때**
> * **주인공:** ✅ **Algolia**
> * **역할:** 검색어와의 '관련도' 기반 **'랭킹(Ranking)'**.
> * **동작:** 안드로이드 앱이 "김치찌개"라는 검색어를 Algolia에 요청합니다. Algolia는 자체 랭킹 공식(제목 일치, 오타 수정, 추천 수)에 따라 정렬된 결과를 반환합니다.
>
> **데이터 동기화: Firebase Functions**
> * 사용자가 레시피에 '좋아요'를 눌러 Firestore의 `recommend_count`가 10에서 11로 변경됩니다.
> * Firebase Function이 이 변경을 감지(`onDocumentUpdated`)하여 Algolia의 `recipes` 인덱스에 해당 레시피의 `recommend_count`를 11로 즉시 업데이트합니다.

---

## 🔧 주요 기능 상세

### 1\. 검색 시스템 (Algolia + Firestore)

  * **Firebase (정렬/추천):** 홈 화면의 '인기 레시피' 등 정렬된 목록을 가져오는 데 사용됩니다.
  * **Algolia (검색):** `SearchFragment`의 모든 키워드 검색은 Algolia를 통해 처리됩니다.
  * **자동 동기화:** Firebase Functions(`functions/src/index.ts`)가 Firestore 문서의 변경(예: `recommend_count` 증가)을 감지하여 Algolia 인덱스에 실시간으로 동기화합니다.

### 2\. 지능형 검색어 처리 (Okt)

사용자 검색 경험을 향상시키기 위해 한국어 형태소 분석기를 도입했습니다.

  * `FoodRecipeApplication.java`에서 앱 시작 시 Okt 라이브러리를 비동기 스레드에서 미리 초기화하여 검색 시 지연 시간을 최소화합니다.
  * `StringUtils.java`의 `extractNouns` 메서드는 사용자가 입력한 검색어(예: "맛있는 김치찌개 레시피")에서 **"김치찌개"**와 같은 핵심 명사만 추출합니다.
  * `SearchPresenter.java`는 추출된 명사들을 검색 칩(Chip)으로 변환하여, 사용자가 검색 조건을 시각적으로 조합할 수 있게 합니다.

### 3\. 냉장고 및 유통기한 알림

백그라운드 작업과 서버리스 기능을 결합하여 유통기한 알림을 구현했습니다.

1.  **데이터 저장:** 사용자의 식재료(`PantryItem`)는 `users` 컬렉션 내의 `myIngredients` 배열에 임베디드(embedded)됩니다.
2.  **데이터 동기화:** Firebase Function(`syncExpiringIngredients`)이 `myIngredients` 배열의 변경(추가/수정/삭제)을 감지하고, 유통기한이 있는 항목만 별도의 `expiringIngredients` 컬렉션으로 동기화합니다.
3.  **백그라운드 작업:** 안드로이드의 `ExpirationCheckWorker`가 `WorkManager`를 통해 24시간마다 실행됩니다.
4.  **알림 전송:** `ExpirationCheckWorker`는 `expiringIngredients` 컬렉션을 쿼리하여 유통기한이 임박(D-3)하고 아직 알림이 발송되지 않은(`PENDING`) 재료를 찾아 사용자에게 푸시 알림을 보냅니다.

### 4\. 안정적인 MVP 구조

  * `base` 패키지의 `BaseContract`와 `BasePresenter`를 통해 모든 View와 Presenter가 일관된 생명주기(attach/detach)를 갖도록 설계하여 메모리 누수를 방지합니다.
  * `AuthViewModel`을 `ViewModelProvider`를 통해 Activity 범위로 생성하여, `HomeFragment`, `PantryFragment` 등 모든 하위 Fragment가 로그인 상태(`FirebaseUser`)를 실시간으로 공유하고 UI를 즉각 변경할 수 있습니다.

---

## 🗃️ Firestore 데이터 구조

### 1\. `recipes` 컬렉션

10만 개의 레시피 원본 데이터가 저장된 기본 컬렉션입니다.

  * `RCP_SNO` (String): 레시피 고유 ID
  * `title` (String): 레시피 제목
  * `ingredients` (Array\<String\>): 정제된 재료명 배열
  * `ingredients_raw` (String): 원본 재료 텍스트
  * `cooking_steps` (Array\<Map\>): 단계별 조리 과정
  * `cooking_time` (String): 조리 시간
  * `difficulty` (String): 난이도
  * `recommend_count` (Number): 추천 수 (즐겨찾기 수)
  * `scrap_count` (Number): 스크랩 수

### 2\. `users` 컬렉션

회원 정보 및 사용자별 데이터를 저장하는 컬렉션입니다.

  * `uid` (String): Firebase Auth UID
  * `email` (String): 사용자 이메일
  * `username` (String): 사용자 닉네임
  * `bookmarked_recipes` (Array\<String\>): 즐겨찾기한 `recipes`의 ID (RCP\_SNO) 배열
  * `myIngredients` (Array\<Map\>): 사용자의 '내 냉장고' 재료 목록 (PantryItem 객체 배열)