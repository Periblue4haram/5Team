# 실제 SMTP (Gmail) 설정 가이드

학교 이메일 OTP 인증 메일을 **실제로 발송**하려면 `MAIL_USERNAME` / `MAIL_PASSWORD` 가 필요합니다.

## 1. Gmail 앱 비밀번호 만들기

1. Google 계정에서 **2단계 인증**을 켭니다.
2. [앱 비밀번호](https://myaccount.google.com/apppasswords) 페이지로 이동합니다.
3. 앱 이름(예: 명랑회)을 넣고 비밀번호 **16자**를 발급받습니다.
4. 공백을 제거하고 사용합니다.

> 일반 로그인 비밀번호가 아니라 **앱 비밀번호**여야 합니다.

## 2. 환경 변수 설정

프로젝트의 `BE/Myeongrang-hoe/.env.example` 을 참고하세요.

```bash
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=your_account@gmail.com
export MAIL_PASSWORD=your_16_char_app_password
export MAIL_FROM=your_account@gmail.com
```

IntelliJ / VS Code 실행 구성, 또는 셸에서 export 한 뒤:

```bash
./gradlew bootRun
```

## 3. 동작 확인

```bash
curl -sS http://127.0.0.1:8080/api/mail/status
```

- `"smtpConfigured": true` → 실제 메일 발송 모드
- `false` → 개발 모드(응답에 인증번호 포함 가능)

회원가입 화면에서 `@mju.ac.kr` 로 인증번호를 요청하면, 설정이 올바른 경우 **실제 메일함**으로 코드가 갑니다.

## 4. 보안 주의

- `MAIL_PASSWORD` 를 git 에 커밋하지 마세요.
- `.env` 는 `.gitignore` 에 포함되어 있습니다.
