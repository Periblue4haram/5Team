export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://127.0.0.1:8080'

export type ApiUser = {
  id: number
  email: string
  name: string
  campus: '인문캠퍼스' | '자연캠퍼스' | string
  major: string
  age: string
  bio: string
  interests: string[]
  sunlightScore: number
  noShowCount: number
  participationCount: number
  loginable: boolean
}

async function parseJson(response: Response): Promise<Record<string, unknown> | null> {
  try {
    return (await response.json()) as Record<string, unknown>
  } catch {
    return null
  }
}

function errorMessage(payload: Record<string, unknown> | null, fallback: string): string {
  const message = payload?.message
  return typeof message === 'string' && message.trim() ? message : fallback
}

export async function sendVerificationCode(email: string): Promise<{
  message: string
  code?: string
  delivered: boolean
}> {
  const response = await fetch(`${API_BASE_URL}/api/auth/send-verification-code`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email }),
  })
  const payload = await parseJson(response)
  if (!response.ok) {
    throw new Error(errorMessage(payload, '인증번호 전송에 실패했어요.'))
  }
  return {
    message: errorMessage(payload, '인증번호를 발송했어요.'),
    code: typeof payload?.code === 'string' ? payload.code : undefined,
    delivered: Boolean(payload?.delivered),
  }
}

export async function verifyEmailCode(email: string, code: string): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/api/auth/verify-code`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, code }),
  })
  const payload = await parseJson(response)
  if (!response.ok || !payload?.verified) {
    throw new Error(errorMessage(payload, '인증번호가 올바르지 않아요.'))
  }
}

export async function loginWithApi(email: string, password: string): Promise<ApiUser> {
  const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  })
  const payload = await parseJson(response)
  if (!response.ok || !payload?.user) {
    throw new Error(errorMessage(payload, '이메일 또는 비밀번호가 올바르지 않아요.'))
  }
  return payload.user as ApiUser
}

export async function signupWithApi(input: {
  email: string
  password: string
  name: string
  campus: string
  major: string
  age: string
  bio: string
  interests: string[]
}): Promise<ApiUser> {
  const response = await fetch(`${API_BASE_URL}/api/auth/signup`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(input),
  })
  const payload = await parseJson(response)
  if (!response.ok || !payload?.user) {
    throw new Error(errorMessage(payload, '회원가입에 실패했어요.'))
  }
  return payload.user as ApiUser
}
