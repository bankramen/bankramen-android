---
name: bankramen-figma-compose
description: "Figma MCP 링크를 받아 뱅크라면 Android Compose 화면으로 구현할 때 쓰는 프로젝트 스킬. 링크 기반 화면 구현, Route/Screen 분리, Preview 추가, 디자인 토큰 반영, 금융 앱 가독성 중심 UI 작업이면 반드시 이 스킬을 사용한다."
---

# Bankramen Figma Compose

## 언제 쓰는가
- 사용자가 Figma 링크를 보냈다.
- 특정 화면을 Jetpack Compose로 구현해야 한다.
- 기존 화면을 Figma 기준으로 수정해야 한다.

## 작업 절차
1. Figma MCP에서 디자인 컨텍스트를 읽는다.
2. 레이아웃, 타이포, spacing, 색상, 주요 상태를 정리한다.
3. `Route + Screen + component` 구조가 필요한지 먼저 판단한다.
4. 스크린은 Stateless로 두고 Route에서 ViewModel을 연결한다.
5. Preview를 추가한다.

## 기본 원칙
- 금융 데이터는 장식보다 가독성을 우선한다.
- 인터랙션 요소는 48dp 이상 터치 영역을 유지한다.
- 다크 모드는 명시 요청이 없으면 추가하지 않는다.
- Figma와 100% 픽셀 복제보다 Android 자연스러움과 유지보수성을 우선한다.

## 출력 기준
- 구현 파일 경로를 명시한다.
- 디자인과 코드 사이에서 의도적으로 단순화한 부분을 적는다.
- 필요한 후속 작업이 있으면 권한, API, navigation 의존성을 적는다.

## 함께 쓰면 좋은 스킬
- `implement-screen`
- `bankramen-fast-mvvm`
- `review-template`
