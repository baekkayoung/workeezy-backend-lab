## ✨ 프로젝트 소개
### 워케이션 서포트 시스템 - Workeezy
_클라우드 연동 워케이션 서포트 시스템 만들기_   
   
   

<br>

🌱 Work easy, grow 2gether


최근 기업 복지의 트렌드는 단순한 휴가 제공을 넘어, 업무와 휴식을 병행할 수 있는 ‘워케이션(Workation)’으로 확장되고 있다.
그러나 실제로는 기업과 직원 모두 복잡한 예약 절차, 제한적인 정보, 관리의 어려움 등으로 인해 워케이션을 체계적으로 운영하는 데 한계가 있다.



이에 팀 2gether는 기업과 직원이 함께 성장할 수 있는 워케이션 문화를 조성하기 위해, 업무 효율과 휴식을 동시에 지원하는 통합 플랫폼 "Workeezy(워크이지)”를 기획하였다.
Workeezy는 Work + Easy의 합성어로, ‘일은 더 쉽게, 성장은 함께(Work easy, grow 2gether)’라는 의미를 담고 있으며, 이를 통해 기업은 효율적인 복지 관리를, 직원은 유연한 근무와 휴식을 경험할 수 있다.


<br>

## 📆 개발 기간
- 2025-11-06(목) ~ 2025-11-10(월): 주제, 프로젝트명, 팀장 선정
- 2025-11-10(월) ~ 2025-11-13(목): 기획의도, 유사사이트 분석, 클라이언트 요구사항분석, UseCase Diagram 작성
- 2025-11-13(목) ~ 2025-11-19(수): 사이트맵, 화면설계 회의, 메인페이지 화면설계
- 2025-11-19(수) ~ 2025-11-26(수): 도메인 엔티티 설계, DB 테이블 설계, 샘플데이터, ERD 작성
- 2025-11-26(수) ~ 2025-11-28(금): 리액트로 화면 생성, 기능구현, JPA(Hibernate) 기반 MySQL 연동 후 CRUD 최소 1개
- 2025-11-28(금) ~ 2026-01-04(일): 프로젝트 구현 및 디버깅, AWS 클라우드 배포
- 2026-01-05(월) ~ 2026-01-08(목): 베타 테스트, 최종 발표자료 준비
- 2026-01-09(금): 프로젝트 발표


<br>
<br>

## 🐵 구성원 및 역할

### 👩‍💻 조장 김혜지

🔐 인증 · 사용자 관리

- Spring Security 기반 인증/인가 구조 설계
- JWT + Redis를 활용한 로그인 / 로그아웃 및 토큰 관리
   - 로그아웃 시 토큰 무효화(블랙리스트) 처리
- 아이디 저장 및 자동 로그인 기능 구현
- 개인정보 수정 시 비밀번호 재검증 로직 적용
- 연락처 수정 및 비밀번호 변경 기능 구현

<br>

💳 예약 · 결제 흐름

- 결제 완료 시 예약 상태 변경 처리 흐름 구현
- 결제 도메인과 예약 도메인 간 상태 동기화 로직 설계

<br>

🚀 배포 · 운영

- Docker 기반 애플리케이션 컨테이너화
- GitHub Actions를 활용한 CI/CD 파이프라인 구축
- 쉘 스크립트를 통한 자동 배포 환경 구성

<br>

### 👩‍💻 조원 백가영

🔁 예약 흐름 관리

- 예약 변경, 취소 기능 구현
- 반려 사유 확인 후 재신청 가능한 예약 흐름 설계
- 예약 상태에 따른 분기 처리 로직 구현

<br>

🔒 예약 동시성 제어

- 비관적 락(Pessimistic Lock)을 활용한 예약 동시성 제어
- 중복 예약 방지 및 데이터 정합성 문제 해결

<br>

🗂 사용자 데이터 관리

- Redis를 활용한 사용자별 임시 저장 데이터 관리
- 예약 진행 중 이탈 시 데이터 유지 및 복구 처리

<br>

📜 조회 · 페이징

- 무한 스크롤 방식의 사용자별 예약 조회 기능 구현
- Cursor 기반 Pagination을 적용한 관리자 예약 조회 기능 구현

<br>

📑 문서 생성 · 파일 관리

- 예약 확정 시 PDF 문서 자동 생성(OpenPDF)
- 생성된 PDF 파일을 AWS S3에 업로드 및 관리

<br>

### 👨‍💻 조원 조민준

🔍 프로그램 조회 · 검색

- 프로그램 상세 페이지 및 검색 결과 페이지 구현
- 조건 기반 검색 기능 구현
- 검색어 기반 추천 프로그램 노출 로직 적용

<br>

📝 리뷰

- 사용자 리뷰 등록 UI 및 비즈니스 로직 구현
- 리뷰 데이터 저장 및 조회 흐름 설계

<br>

💬 챗봇

- 카카오톡 API 연동 챗봇 기능 구현
- 사용자 질의에 따른 응답 처리 로직 구성
- Docker 기반 Flask 챗봇 서버 구축
- 메인 백엔드와 분리된 마이크로 서비스 형태로 운영

<br>

## ⚙ 개발 환경 (Environment / Tools)

- OS : Windows 10
- Developer Tools : IntelliJ IDEA, WebStorm, VS Code, DBeaver
- Test Tools : Postman, Apache JMeter
- VCS : Git, GitHub, SourceTree

<br>

## 🔧 기술 스택

### Backend

- Framework : Spring Boot, Flask
- Language : Java, Python
- Security : Spring Security, JWT (Access / Refresh Token)
- Data Access : JPA (Hibernate)
- Cache / Session Store : Redis
- Build Tool : Maven, Gradle
- Connection Pool : HikariCP
- API Architecture : RESTful API
- Transaction Management : Spring @Transactional  
   
### Frontend

- Framework : React
- Markup & Styling : HTML5, CSS3
- Runtime / Package Manager : Node.js
- Scripting : JavaScript (ES6+)
- HTTP Client : Axios

<br>

## 🗄 Database

- DBMS : MySQL
- Concurrency Control : Pessimistic Lock (JPA)
- Pagination : Cursor-based Pagination

<br>

## 🚀 Server / Deployment
- Server : Apache Tomcat 9.0, Spring Boot Embedded Server
- Deployment / Environment :
   - AWS EC2, S3, RDS
   - Docker
   - GitHub Actions (CI/CD)
   - Environment Separation (local / prod)

<br>

## 📄 Document / File Handling

- PDF Generation : OpenPDF
- File Storage : AWS S3

<br>

## 💡 API 연동 & 라이브러리
- Toss payments
- kakao chatbot
- SweetAlert2

<br>


## 도메인 설계

<br>

## 엔티티 설계

<br>

## 🧩 데이터 설계
ERD: [ERD Cloud]

<br>

## 💻 프로젝트 구현
### 🙈 조장 김혜지

![기능명](이미지url)



### 🙊 조원 백가영




### 🙉 조원 조민준


<br>

## 📑 최종 보고서
![2조 workeezy 최종보고서.pdf](https://github.com/user-attachments/files/24519932/2.Workeezy.pdf)

