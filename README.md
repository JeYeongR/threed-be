# Threed Backend

Threed는 개발자들을 위한 기술 블로그 및 포스트 공유 플랫폼입니다. 개발자들이 자신의 기술 경험과 지식을 공유하고, 다른 개발자들의 포스트를 검색하고 북마크할 수 있는 서비스를 제공합니다.

## 주요 기능

- **회원 관리**: 소셜 로그인(Google, Kakao, GitHub) 지원
- **포스트 관리**: 기술 블로그 포스트 작성, 수정, 삭제, 조회
- **회사별 포스트**: 주요 기술 기업(네이버, 카카오, 토스 등)의 기술 블로그 포스트 제공
- **검색 기능**: 다양한 조건으로 포스트 검색
- **북마크 기능**: 관심 있는 포스트 북마크 저장
- **이미지 업로드**: S3를 활용한 이미지 업로드 기능

## 기술 스택

- **언어**: Java 17
- **프레임워크**: Spring Boot 3
- **데이터베이스**: MySQL
- **데이터 액세스 기술**: Spring Data JPA, QueryDSL
- **인증**: JWT 토큰 기반 인증
- **문서화**: Swagger (SpringDoc OpenAPI)
- **클라우드 스토리지**: AWS S3
- **빌드 도구**: Gradle
- **CI/CD**: GitHub Actions, AWS CodeDeploy (appspec.yml)

## 프로젝트 구조

```
src/main/java/com/example/threedbe/
├── auth/           # 인증 관련 기능
├── bookmark/       # 북마크 기능
├── common/         # 공통 유틸리티 및 설정
├── member/         # 회원 기능
├── post/           # 포스트 기능
└── ThreedBeApplication.java
```

## 설치 및 실행 방법

### 요구사항

- JDK 17 이상
- MySQL

### 로컬 개발 환경 설정

1. 저장소 클론

```bash
git clone https://github.com/yourusername/threed-be.git
cd threed-be
```

2. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 환경 변수 설정

다음 환경 변수들을 설정해야 합니다:

- 데이터베이스 연결 정보
- OAuth 클라이언트 정보 (Google, Kakao, GitHub)
- JWT 시크릿 키
- AWS S3 접근 정보

## API 문서

애플리케이션 실행 후 다음 URL에서 Swagger API 문서를 확인할 수 있습니다:

```
http://localhost:8080/swagger-ui/index.html
```

## 인증 시스템

- JWT 토큰 기반 인증 시스템 사용
- Access Token과 Refresh Token을 활용한 인증 구현
- 소셜 로그인(Google, Kakao, GitHub) 지원
