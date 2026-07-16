# Pull Request 초안

## 메타 정보

| 항목 | 값 |
|------|-----|
| **base (메인)** | `mju-myongjithon/5Team` → `main` |
| **head (포크)** | `jinhgit/5Team_JH` → `main` |
| **비교 URL** | https://github.com/mju-myongjithon/5Team/compare/main...jinhgit:5Team_JH:main?expand=1 |
| **포크 최신 커밋** | `b0cdc24` |
| **upstream 대비** | +20 commits · ~98 files · +8.4k lines |

### 권장 PR 제목

```
feat: 학교 인증·펀딩/커뮤니티 API·FE 연동 및 UX 고도화 (공유·채팅·일정·검색·지도)
```

---

## 생성 방법

### A. GitHub 웹 (권장)

1. 비교 URL 열기:  
   https://github.com/mju-myongjithon/5Team/compare/main...jinhgit:5Team_JH:main?expand=1  
2. **Create pull request**
3. 제목: 위 권장 제목
4. 본문: `docs/PR_BODY_github.md` 전체 붙여넣기
5. (가능하면) **Draft** 로 먼저 올린 뒤 리뷰 요청

### B. GitHub CLI

```bash
# 포크 최신 push 후
gh pr create \
  --repo mju-myongjithon/5Team \
  --base main \
  --head jinhgit:5Team_JH:main \
  --title "feat: 학교 인증·펀딩/커뮤니티 API·FE 연동 및 UX 고도화 (공유·채팅·일정·검색·지도)" \
  --body-file docs/PR_BODY_github.md
```

---

## PR 본문

→ **`docs/PR_BODY_github.md`** 를 GitHub 본문으로 사용하세요.  
(인증·펀딩·커뮤니티 API, FE 연동, UI/UX 전 항목을 화면·API 단위로 상세 기술)

---

## 리뷰어 체크 포인트

1. 로그인/가입 OTP (개발 모드 코드 노출 vs SMTP)
2. 홈 카카오맵 + 재생 투어 + 클러스터
3. 목록 필터(쇼핑·무료·거리)
4. 펀딩 생성(이미지 업로드) → 참여 → 확정 → 일정 → 채팅
5. 공유 / 신고·차단 / 온보딩
6. BE 파일 `/uploads` 정적 경로
