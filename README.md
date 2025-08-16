# 🖥️ 2025 앱개발 경진대회 프로젝트: 백엔드 파트

***

## 👍 프로젝트 개요
***
- 목표: 2025 교내 앱개발 경진대회 수상
- 툴: IntelliJ IDEA 2025.1, Git/GitHub, Postman, MySQL Workbench
- 언어: Java 21.0.5
- 프레임워크: Spring Boot 3.5.3
  - Build: Gradle (Groovy), Packaging: Jar, Java: 21
- 라이브러리: Spring Boot DevTools, Spring Data JPA, MySQL Driver, Lombok, Flyway(flyway-core, flyway-mysql)
- DB: MySQL 8.0.42
- 노션: https://www.notion.so/21bc79a2364680e38555e1e5da5bb548?source=copy_link

---

## 🛠️ Git
***g
### 😎 커밋 메시지 컨벤션
- '태그: 제목'의 형태이며, : 뒤에만 space가 있음에 유의합니다.

| 태그 | 제목 |
|---|---|
| Feat | 새로운 기능을 추가할 경우 |
| Fix | 버그를 고친 경우 |
| Docs | 문서를 수정한 경우 |
| Refactor | 코드 리팩토링 |
| Test | 테스트 (테스트 코드 추가, 수정, 삭제, 비즈니스 로직에 변경이 없는 경우) |
| Chore | 위에 걸리지 않는 기타 변경사항 (빌드 스크립트 수정, assets image, 패키지 매니저 등) |
| Design | CSS 등 사용자 UI 디자인 변경 |
| Comment | 필요한 주석 추가 및 변경 |
| Docs | 문서를 수정한 경우 |
| Init | 프로젝트 초기 생성 |
| Rename | 파일 혹은 폴더명을 수정하거나 옮기는 작업만인 경우 |
| Remove | 파일을 삭제하는 작업만 수행한 경우 |

***

## 🌳 브랜치 전략 및 협업 프로세스
- main: 최종 결과물
- develop: 개발 통합 브랜치
- 기능 브랜치: issue/#1_SessionStart, issue/#5_QuizCard, issue/#9_ScoreScreen …

작업 흐름  
1) develop에서 기능 브랜치 생성 → 작업/커밋(컨벤션 준수)  
2) PR 생성(리뷰어 지정) → 코드리뷰 반영  
3) Squash & Merge 권장, 이슈 연결 #번호

---

## 🙋‍♀️ 협업 Tip
- 항상 pull 먼저 해서 동기화
- 커밋 메시지는 무엇을/왜 했는지 드러나게
- 브랜치/커밋/PR 컨벤션 준수
- 모르면 화면·로그·스크린샷으로 질문
- 예외/에러 응답 포맷 일관성 유지

***

## ▶️ 빠른 실행 (도커 없이, 로컬)
- JDK 17 이상(권장 21), MySQL 8.0.42 설치
- DB 생성:
```sql
CREATE DATABASE slang_quiz_app
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
```
- 설정 파일: `src/main/resources/application-example.yml` → 복사해 `application.yml` 생성 후 DB 계정/비번 입력
- 실행: Windows `.\gradlew.bat bootRun` / macOS·Linux `./gradlew bootRun`

<details>
<summary>자세한 순서/확인 (펼치기)</summary>

- 확인
  - 콘솔:
    - Migrating ... to version "1 - init"
    - Migrating ... to version "2 - seed"
    - Successfully applied 2 migrations
  - 브라우저: http://localhost:8080  (루트는 500일 수 있음)
  - Swagger가 있다면: http://localhost:8080/swagger-ui/index.html
  - DB 카운트
```sql
USE slang_quiz_app;
SELECT COUNT(*) FROM quiz;       -- 22
SELECT COUNT(*) FROM hint;       -- 22
SELECT COUNT(*) FROM quizoption; -- 88
SELECT version, success FROM flyway_schema_history; -- 1,2 / success=1
```

- 예시 `application.yml`
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/slang_quiz_app?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: root
    password: 비밀번호를_여기에
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        jdbc:
          time_zone: Asia/Seoul
  flyway:
    enabled: true
    locations: classpath:db/migration
server:
  port: 8080
```
</details>

---

## 🗃️ 마이그레이션(Flyway)
<details>
<summary>정책/베이스라인 안내 (펼치기)</summary>

- 위치: `src/main/resources/db/migration/`
  - `V1__init.sql`  → DDL(테이블·제약·인덱스)
  - `V2__seed.sql`  → 시드 INSERT (덤프 문법 제거: LOCK/UNLOCK, /*! ... */ 등)
- 규칙: 기존 파일 수정하지 않음, 변경은 V3, V4… 새 파일로 추가
- 기존 DB가 비어있지 않다면(1회만):
```yaml
spring.flyway.baseline-on-migrate: true
spring.flyway.baseline-version: 1   # 데이터까지 이미 있으면 2
```
- 성공 후에는 위 설정을 제거
</details>

***

## 📁 폴더 구조
<details>
<summary>요약 보기 (펼치기)</summary>

```
src
 └─ main
    ├─ java/...                # controller / service / repository / domain
    └─ resources
       ├─ application.yml      # 로컬 개인 설정 (git 제외)
       ├─ application-example.yml
       └─ db
          └─ migration
             ├─ V1__init.sql
             └─ V2__seed.sql
build.gradle
gradlew / gradlew.bat
```
</details>

---

## 🔐 환경/보안
<details>
<summary>.gitignore와 비밀관리 (펼치기)</summary>

- 커밋 제외: `src/main/resources/application.yml` (비밀번호 노출 방지)
- `.gitignore` 예시
```
/.gradle
/build
/out
*.log
src/main/resources/application.yml
```
</details>

***

## 🧯 트러블슈팅
<details>
<summary>흔한 이슈 해결 (펼치기)</summary>

- Port 8080 already in use → `server.port: 8081` 또는 실행 인자 `--server.port=8081`
- Access denied for user → DB 계정/비밀번호/권한 확인
- Found non-empty schema without metadata table → 위 베이스라인 1회만 적용
- Cannot add or update a child row → FK/데이터 순서 점검, 빈 DB로 재실행
- Windows + OneDrive 잠김/캐시 → 동기화 일시중지, `build/`, `.gradle/` 삭제 후 재빌드
</details>

---

## 🔗 링크/레퍼런스
- ERD: https://www.notion.so/ERD-224d54218c708000ab86f167ad32ae1e
- API 명세: https://www.notion.so/API-231d54218c7080bdad41cf77dbc2bcfa

