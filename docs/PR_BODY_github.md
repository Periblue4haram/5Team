## 요약 한 줄

명랑회 동행 펀딩 앱의 **Spring Boot 백엔드 구축·PRD API**, **FE 전면 서버 연동**, 그리고 **지도·공유·온보딩·채팅·검색·일정·이미지 업로드 등 UI/UX 고도화**를 메인에 반영합니다.

### 요약

명랑회(동행 펀딩) 프로젝트에 대해:

1. **백엔드**: 학교 이메일 OTP 인증, 회원, 펀딩 CRUD/참여/마감/일정, 댓글·채팅·후기·찜, 파일 업로드, SMTP 준비
2. **프론트**: 토큰 기반 서버 우선 연동 + 로컬 폴백, 카카오맵, 공통 로딩/토스트/아바타
3. **UX 팩**: 공유·온보딩·신고/차단·채팅 읽음·목록 검색, 일정 확정·캘린더, 브라우저 알림, 거리/날짜 필터, 지도 클러스터·재생 투어

작업 위치: 포크 `jinhgit/5Team_JH` → 본 PR로 메인 `mju-myongjithon/5Team` 반영 요청  
**헤드 커밋**: `b0cdc24` · **upstream 대비 +20 커밋** · 약 **98 files / +8.4k lines**

---

### 배경 / 목적

- 기존 FE는 localStorage 스토어 중심 데모였고, PRD의 **학교 인증·서버 영속·커뮤니티 루프**가 필요했습니다.
- 중단된 작업을 git 체크포인트(한국어 커밋·push)로 이어받아 기능을 누적했습니다.
- 목표: 실행 가능한 BE + 연동된 FE + 발표/데모 가능한 UX 완성도

---

## 1. 백엔드 (Spring Boot 4.1 + JPA + H2)

### 1-1. 인증 · 회원

| 기능 | API / 내용 |
|------|------------|
| OTP 발송 | `POST /api/auth/send-verification-code` |
| OTP 검증 | `POST /api/auth/verify-code` (일회용 + 가입 윈도우) |
| 이메일 중복 | `GET /api/auth/check-email` |
| 회원가입 | `POST /api/auth/signup` (BCrypt, 인증 메일만) |
| 로그인 | `POST /api/auth/login` → `accessToken` + user |
| 세션 | Bearer (`auth_sessions`, 약 14일) |
| 내 정보 | `GET/PATCH /api/users/me` (프로필·관심사·아바타·알림 읽음 시각) |
| 위치 | `PATCH /api/users/me/location` |
| 공개 프로필 | `GET /api/users/profile?email=` |
| 받은 후기 | `GET /api/users/reviews?email=` |

- 학교 메일: `*@*.ac.kr` (예: `@mju.ac.kr`)
- 시드: `test1@mju.ac.kr` / `test2@mju.ac.kr`, 비밀번호 `test1234`

### 1-2. SMTP

- `MAIL_USERNAME` / `MAIL_PASSWORD` / `MAIL_FROM`
- 미설정 시 개발 모드(응답에 코드 노출 가능)
- `GET /api/mail/status`
- 가이드: `BE/Myeongrang-hoe/docs/SMTP_설정.md`

### 1-3. 펀딩

| 기능 | API |
|------|-----|
| 목록 / 주변 | `GET /api/fundings?lat&lng&radiusKm` |
| 상세 | `GET /api/fundings/{id}` |
| 생성 / 수정 | `POST`, `PUT /api/fundings/{id}` |
| 참여 / 취소 | `POST .../join`, `/leave` |
| 모집 확정(인원) | `POST .../confirm` |
| **조기 마감** | `POST .../close` (`closed=true`) |
| **삭제** | `DELETE /api/fundings/{id}` |
| **일정 확정** | `POST .../schedule` (`scheduleConfirmed`, meet/장소) |
| 넛지 문구 | `GET .../nudge` |
| 노쇼 리스크 | 생성 시 `aiRisk` (규칙 기반) |

- 목표 인원 달성 시 `matched` + 시스템 채팅
- Haversine 거리 정렬
- 엔티티 필드: `coverImage`, `matched`, **`closed`**, **`scheduleConfirmed`**

### 1-4. 커뮤니티

| 기능 | API |
|------|-----|
| 댓글 목록/작성/삭제 | `GET/POST .../comments`, `DELETE .../comments/{id}` |
| 채팅 | `GET/POST .../chat` |
| 후기 + 햇살지수 | `POST/GET .../reviews` |
| 찜 | `POST .../wishlist` |

### 1-5. 파일 · 기타

| 기능 | 내용 |
|------|------|
| **이미지 업로드** | `POST /api/files/upload` (multipart, 2MB, jpg/png/webp/gif) |
| 정적 서빙 | `/uploads/**` → `app.upload.dir` (기본 `./data/uploads`) |
| 정적 지도/지오 | `GeoController` (카카오 REST 키 옵션) |
| CORS · AuthInterceptor | `/api/**` Bearer 또는 개발용 `X-User-Email` |

---

## 2. 프론트엔드 (Vite + React + TS)

### 2-1. 아키텍처

- `lib/api.ts`: 전 엔드포인트 클라이언트, 토큰 저장
- `store/actions.ts`: **서버 우선, 실패 시 로컬 폴백** 동기화
- `AuthBootstrap`: 앱 기동 시 `me` 복구
- `RequireAuth`: 보호 라우트
- `ToastHost` / `setGlobalLoading` / `LoadingScreen`: 전역 UX
- `UserAvatar` / `FundingCover`: 프로필·커버 공통 표시

### 2-2. 화면별 서버 연동 · UI/UX

| 화면 | 변경 요약 |
|------|-----------|
| **Login** | OTP·가입·로그인 서버 연동, 에러 토스트 |
| **Signup 플로우** | 프로필 → 관심사(쇼핑 포함) → 위치 권한 |
| **Home** | 카카오맵, 위치, 거리 칩, 관심사 가로 스크롤, 지도 클러스터, **재생 투어 버튼**, 알림 권한, 차단 필터 |
| **FundingList** | 카테고리(쇼핑 포함), AND 검색, 정렬, 오늘/이번주, 무료만, 거리, 마감/완료 제외 |
| **FundingTab** | 참여/찜/공유/신고·차단, 일정 확정·캘린더, 호스트 마감·삭제, 댓글, 참여자 프로필 |
| **FundingForm** | 카테고리·일정 검증, **서버 이미지 업로드**, 2MB 제한 |
| **ChatList/Room** | 폴링(2s 방 / 4s 목록), 미읽음 배지, 읽음/전송됨, 프로필 시트 |
| **Notifications** | 찜 성사임박·당일 만남·후기 리마인드·일정 확정 우선 표시 |
| **MyPage / MyPosts** | 동기화, FAB, 후기·찜 |
| **ProfileSetup** | 아바타 서버 업로드 |
| **Review** | 제출 로딩 상태 |

### 2-3. UX 기능 팩 (최근 구현 상세)

#### A. 공유 · 온보딩 · 안전

| 기능 | 동작 |
|------|------|
| 펀딩 공유 | 카카오 SDK → Web Share → 클립보드 (`lib/share.ts`) |
| 온보딩 | 첫 로그인 3단계 튜토리얼 (`OnboardingOverlay`, localStorage) |
| 신고 | `ReportModal` 사유·상세·함께 차단 |
| 차단 | 호스트 차단 시 홈/목록 숨김 (`store/moderation.ts`) |

#### B. 채팅 · 알림

| 기능 | 동작 |
|------|------|
| 폴링 | 채팅방 2초, 목록 4초 |
| 읽음 | `chatRead` localStorage, 목록·하단 네비 미읽음 배지 |
| 브라우저 알림 | 찜 성사임박 등 (`browserNotify.ts`) |
| 후기 리마인드 | 만남 시각 경과 + 미작성 시 알림 피드 |
| 만남 당일 | 당일 일정 알림 |

#### C. 일정 · 호스트 운영

| 기능 | 동작 |
|------|------|
| 일정 확정 모달 | 성사 후 시간·장소 저장, 시스템 채팅 |
| 캘린더 | Google Calendar URL + `.ics` 다운로드 |
| 조기 마감 / 삭제 | 호스트 전용 메뉴 |

#### D. 검색 · 지도 발견

| 기능 | 동작 |
|------|------|
| 거리 | 1km / 3km / 캠퍼스 / 전체 |
| 날짜·요금 | 오늘, 이번 주, 무료만 |
| 관심사 홈 섹션 | 내 관심 카테고리 가로 스크롤 |
| 지도 클러스터 | 그리드 클러스터 + 탭 시 확대 |
| 재생 버튼 | 흰 원 + ▶, 주변 펀딩 순회 패닝 |
| 쇼핑 카테고리 | 목록·작성·관심사 공통 |

#### E. 미디어

| 기능 | 동작 |
|------|------|
| 커버/아바타 업로드 | 서버 파일 저장 후 URL 사용, 실패 시 data URL 폴백 |
| 위치 커버 | 사진 없을 때 지도 타일 대표 이미지 (`coverImage.ts`) |
| 사진 없을 때 뱃지 | 「위치 기반 지도」 표시 |

#### F. 품질·버그 수정

- 채팅/댓글 **IME 이중 전송** 방지, 서버 응답 기준 반영
- 본인 댓글 삭제
- 카카오맵 SDK 로더·`relayout`·위치 대기 분리
- OSM 타일 폴백 안정화
- 후기 버튼 로딩, 전역 로딩 화면(새싹 로고)

---

## 3. UI/UX 변경 체크리스트 (화면 단위)

### 홈 `/`

- [x] 카카오맵 핀·내 위치 점·줌 컨트롤
- [x] 우측 하단: 내 위치 버튼 + **재생(투어) 버튼**
- [x] 거리 필터 칩, 브라우저 「알림 켜기」
- [x] 관심사 맞춤 가로 카드
- [x] 성사 임박 넛지 배너
- [x] 지도 클러스터 숫자 원

### 목록 `/list`

- [x] 검색(AND 토큰)
- [x] 카테고리 칩 (맛집~**쇼핑**)
- [x] 날짜·무료·거리·정렬·마감/완료 제외
- [x] 결과 건수 표시

### 상세 `/funding/:id`

- [x] 커버(사진/지도), 진행률, 참여자 아바타(탭→프로필)
- [x] 공유 · 찜 · `⋯`(신고/차단 또는 마감/삭제)
- [x] 일정 확정 카드 + Google 캘린더 / .ics
- [x] AI 리스크 카드, 댓글, 하단 CTA

### 채팅

- [x] 미읽음 뱃지(목록·BottomNav)
- [x] 전송됨/읽음 휴리스틱
- [x] 상대 아바타 → 프로필 시트

### 공통

- [x] 온보딩 오버레이, 토스트, 로딩 스크린
- [x] 하단 네비 채팅 미읽음

---

## 4. 문서

- `docs/BACKEND_기능_로드맵.md` — FE↔BE 기능 매핑
- `BE/Myeongrang-hoe/docs/SMTP_설정.md`
- `.env.example` (BE/FE)

---

## 5. 로컬 실행

### BE

```bash
cd BE/Myeongrang-hoe
./gradlew bootRun
# http://127.0.0.1:8080
```

### FE

```bash
cd FE/Myeongrang-hoe
cp .env.example .env   # VITE_API_BASE_URL, VITE_KAKAO_MAP_KEY
npm install
npm run dev -- --host 127.0.0.1 --port 5173
# http://127.0.0.1:5173
```

### 데모 시나리오 (권장)

1. `test1@mju.ac.kr` / `test1234` 로그인 (또는 OTP 가입)
2. 홈: 지도·거리 칩·재생 투어·관심사 섹션
3. 목록: 쇼핑 필터·무료만·검색
4. 펀딩 생성(사진 업로드) → 다른 계정 참여 → 성사/확정
5. 일정 확정 → 캘린더 링크 · 채팅 폴링·미읽음
6. 공유 · 신고/차단 · 온보딩(신규 localStorage 초기화 시)

---

## 6. 커밋 이력 (upstream/main 대비 주요)

```
b0cdc24 펀딩 closed/scheduleConfirmed 컬럼 기본값을 명시한다.
35593dc 제품 완성도·검색 발견 기능을 구현한다.
325a7e2 펀딩 목록·작성·관심사에 쇼핑 카테고리를 추가한다.
20e9f6b 공유·온보딩·신고/차단·채팅 읽음·목록 검색을 추가한다.
7e18946 프로필 사진 등록과 참여자 아바타·공통 로딩 화면을 추가한다.
d97fe80 댓글 중복 등록을 막고 본인 댓글 삭제를 추가한다.
4da08af 위치 지도 이미지 폴백을 OSM 타일로 안정화한다.
7679992 펀딩 사진이 없을 때 위치 기반 지도 대표 이미지를 보여준다.
c82ed70 채팅 중복 전송 수정, 펀딩 사진 업로드, 내글 FAB + 버튼을 개선한다.
40f6952 후기 등록 버튼 로딩 상태를 추가한다.
604e87b FE 로그인 유지·토스트·화면 서버 동기화와 폼 UX를 보강한다.
45a83a5 프론트 기반 백엔드 기능을 정리하고 FE 전면 연동한다.
bcf2b09 카카오맵이 홈에서 정상 표시되도록 로더와 렌더 조건을 수정한다.
9b27b69 SMTP 실연동 준비와 PRD 핵심 백엔드 API를 추가한다.
0145744 Add user signup/login with H2 persistence.
… (인증·체크포인트 커밋 포함, 총 +20)
```

---

## 7. 테스트 · 검증

- [x] FE `npx tsc --noEmit`
- [x] BE `./gradlew compileJava` / `bootRun` 기동
- [x] `/api/fundings` 200 확인
- [ ] 팀 리뷰어: 카카오 JS 키·플랫폼 도메인 등록 여부 확인 필요
- [ ] SMTP 실메일: 자격증명 설정 시 검증

### 주의 / 한계 (의도된 범위)

- 신고·차단·채팅 읽음·온보딩 일부는 **FE localStorage** (서버 모더레이션 API는 후속 가능)
- 채팅은 **폴링** (WebSocket은 후순위)
- AI 노쇼는 **규칙 기반** (LLM 연동은 후순위)
- H2 파일 DB 로컬 개발 기본 (팀 공유 DB는 별도)

---

## 8. PR 메타

| 항목 | 값 |
|------|-----|
| **base** | `mju-myongjithon/5Team` → `main` |
| **head** | `jinhgit/5Team_JH` → `main` |
| **비교 URL** | https://github.com/mju-myongjithon/5Team/compare/main...jinhgit:5Team_JH:main?expand=1 |

### 권장 PR 제목

```
feat: 학교 인증·펀딩/커뮤니티 API·FE 연동 및 UX 고도화 (공유·채팅·일정·검색·지도)
```
