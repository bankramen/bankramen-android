---
name: bankramen-openapi-client
description: "Swagger/OpenAPI 명세를 뱅크라면 Android 앱용 클라이언트로 자동 생성할 때 쓰는 프로젝트 스킬. OpenAPI Generator Gradle plugin, generated 코드 격리, Retrofit/OkHttp 기반 통합, hand-written wrapper 분리가 필요하면 반드시 이 스킬을 사용한다. 사용자가 Swagger 링크나 spec 파일을 주면 수동 API 작성 대신 이 스킬을 우선한다."
---

# Bankramen OpenAPI Client

## 기본 선택
- 주력 도구: `org.openapi.generator` Gradle plugin
- 기본 방향: Kotlin client 생성 + generated code 격리 + 앱 코드에서 필요한 최소 wrapper만 작성
- Swagger Codegen은 현재 우선순위가 아니다.

## 권장 생성 방향
- spec 위치: `openapi/` 또는 사용자 제공 URL/file
- 출력 위치: `app/build/generated/openapi` 또는 별도 generated source set
- 패키지 분리: `com.uson.myapplication.generated.api` 계열
- generated 코드는 직접 수정하지 않는다.

## 앱 통합 원칙
- 앱 코드는 generated DTO/API를 감싸는 작은 datasource/repository 또는 feature-level adapter만 둔다.
- 인증 헤더 주입, baseUrl, logging 같은 환경 설정은 generated 바깥에 둔다.
- spec 변경 대응은 설정/regen으로 해결하고 generated 파일 수작업 수정으로 해결하지 않는다.

## 생성기 판단 기준
- Android 앱에서 Retrofit 친화적 연결이 중요하면 `jvm-retrofit2`를 우선 검토한다.
- generated 코드와 현재 스택 일관성이 더 중요하면 `jvm-okhttp4`도 검토한다.
- 실제 선택은 현재 앱의 JSON/HTTP 스택과 충돌이 적은 쪽으로 한다.

## 출력에 반드시 포함할 것
- 플러그인/태스크 이름
- 입력 spec 위치
- 출력 경로
- sourceSet 또는 task 연결 방식
- generated 밖에서 관리할 파일 목록

## 금지
- generated 영역에 비즈니스 로직 추가 금지
- 명세 미비를 이유로 앱 전역 구조를 과도하게 복잡하게 만들지 말 것
