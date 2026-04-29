---
name: bankramen-fast-mvvm
description: "뱅크라면 Android 앱에서 빠른 MVVM 화면/기능 구현을 할 때 쓰는 프로젝트 스킬. ViewModel, UiState, Route/Screen 분리, Compose stateless 패턴, Room/Flow 연결, 공통 파일 구조가 필요하면 반드시 이 스킬을 사용한다. 클린 아키텍처 확장 대신 실용적인 단일 app 모듈 MVVM 구성이 목적이다."
---

# Bankramen Fast MVVM

이 프로젝트는 사용자 요청상 빠른 MVP 구현을 우선한다. 기존 `.agent/AGENTS.md`와 `.agent/ARCHITECTURE.md`에는 domain/data 분리 지향이 보이지만, 실제 구현은 과도한 레이어링보다 빠른 MVVM 흐름을 우선한다.

## 목표 구조

```text
app/src/main/java/com/uson/myapplication/
├── core/
│   ├── model/
│   ├── ui/
│   └── util/
├── data/
│   ├── api/
│   ├── local/
│   └── repository/
├── feature/
│   └── <feature>/
│       ├── <Feature>Route.kt
│       ├── <Feature>Screen.kt
│       ├── <Feature>ViewModel.kt
│       ├── <Feature>UiState.kt
│       └── component/
└── service/
```

## 구현 규칙
- Route에서만 ViewModel을 연결한다.
- Screen은 Stateless를 우선한다.
- ViewModel은 `StateFlow<UiState>`를 노출한다.
- 화면 상태는 `loading / content / error`를 기본 축으로 잡는다.
- 일회성 이벤트는 필요할 때만 `Channel` 또는 별도 event stream으로 다룬다.
- UseCase, domain interface는 실제 복잡성이 생기기 전까지 만들지 않는다.
- DTO와 UI model은 필요하면 분리하되, 작은 MVP 범위에서는 억지 추상화를 만들지 않는다.

## 파일 작성 기준
- 화면 단위 파일명은 `HomeRoute`, `HomeScreen`, `HomeViewModel`, `HomeUiState` 식으로 맞춘다.
- Preview는 최소 1개, 상태가 분기되면 가능하면 정상/로딩/에러를 추가한다.
- 상수, 색상, 문자열은 가능한 리소스 또는 상수로 추출한다.

## 하지 말 것
- 화면 하나 추가하면서 Repository/UseCase/Mapper를 기계적으로 늘리지 말 것.
- Composable 내부에서 비즈니스 로직을 처리하지 말 것.
- ViewModel이 Android UI 객체를 직접 들고 있지 말 것.

## 권장 판단
- 서버 연동 전 단계의 로컬 기능이면 feature + ViewModel + local repository 정도로 끝내라.
- 화면 요구만 있으면 `implement-screen` 스킬과 함께 사용하라.
- 템플릿 컨벤션 점검이 필요하면 `review-template`을 함께 사용하라.
