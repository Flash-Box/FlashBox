# 📦 FlashBox

FlashBox는 사용자가 지정한 기간 동안 사진을 저장하고, 일정 기간 이후엔 자동으로 삭제되도록 하여 **보안성과 편의성을 높인 사진 저장 시스템**입니다. 사진은 모임별로 구분되어 일괄 다운로드 또한 지원됩니다.

---

## 🧩 프로젝트 개요

- **주제**: 사용자가 지정한 기간 동안 사진을 저장하고, 이후 자동 삭제되어 관리 부담을 줄이며, 보안성을 높인 시스템 구축.
- **출발 배경**: 구글 드라이브와 같은 서비스의 저장 용량 한계와 보안 우려 개선을 목표로 출발.

---

## 🎯 개발 목표

### ✅ 사용자 제시 목표
- **편의성 증가**: 사용자가 직접 삭제할 필요 없이 자동 삭제되어 관리 부담 감소
- **보안 강화**: 삭제 기한이 지난 사진은 자동 삭제되어 민감한 정보 보호
- **스토리지 절약**: 불필요한 파일은 저장하지 않고, 필요 시에만 저장되는 구조

### ⚙️ 기술적 구현 목표
- JWT 토큰 기반 사용자 인증 및 로그인 시스템
- AWS S3, CloudFront를 이용한 사진 업로드 및 저장
- Spring Scheduler를 활용한 자동 삭제 기능
- Thymeleaf 기반 사용자 인터페이스
- Spring Boot, MySQL 기반 백엔드 및 DB 설계

---

## 🚀 주요 기능

- 사용자 로그인 및 인증
- 사진 업로드 및 기간 설정
- 자동 삭제 스케줄 설정
- 사진 다운로드 (모임별 정리)
- 관리 페이지를 통한 사용자 설정

---

## 🛠 기술 스택

| 영역       | 사용 기술                              |
|------------|----------------------------------------|
| 프론트엔드 | Thymeleaf                               |
| 백엔드     | Spring Boot, Spring Scheduler           |
| 데이터베이스 | MySQL                                  |
| 인프라     | AWS S3, CloudFront, EKS,            |
| 인증       | JWT 토큰 기반 인증                       |

---

## 📂 프로젝트 구조

flashbox/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/flashbox/
│   │   └── resources/
│   │       └── templates/  # Thymeleaf templates
│   └── test/
├── build.gradle
└── README.md


## 📌 설치 및 실행 방법
# 1. 프로젝트 클론
git clone https://github.com/your-org/flashbox.git
cd flashbox

# 2. 환경변수 설정
# application.yml 또는 .env 파일에 DB, AWS 자격 정보 입력

# 3. 빌드 및 실행
./gradlew bootRun
