export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://127.0.0.1:8080'

const TOKEN_KEY = 'mh_access_token'

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

export type ApiFunding = {
  id: number
  category: string
  title: string
  locationName: string
  address: string
  lat: number
  lng: number
  meetAt: string
  meetTimeText: string
  deadlineAt: string
  deadlineText: string
  targetCount: number
  fee: number
  fillerParticipants: number
  participants: string[]
  currentCount: number
  description: string
  hostEmail: string
  aiRisk: string
  best: boolean
  matched: boolean
  createdAt: number
  distanceKm?: number | null
}

export function getAccessToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

export function setAccessToken(token: string | null) {
  try {
    if (token) localStorage.setItem(TOKEN_KEY, token)
    else localStorage.removeItem(TOKEN_KEY)
  } catch {
    // ignore
  }
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

function authHeaders(extra?: HeadersInit): HeadersInit {
  const token = getAccessToken()
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...extra,
  }
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

export async function loginWithApi(
  email: string,
  password: string,
): Promise<{ user: ApiUser; accessToken?: string }> {
  const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  })
  const payload = await parseJson(response)
  if (!response.ok || !payload?.user) {
    throw new Error(errorMessage(payload, '이메일 또는 비밀번호가 올바르지 않아요.'))
  }
  const accessToken = typeof payload.accessToken === 'string' ? payload.accessToken : undefined
  if (accessToken) setAccessToken(accessToken)
  return { user: payload.user as ApiUser, accessToken }
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
}): Promise<{ user: ApiUser; accessToken?: string }> {
  const response = await fetch(`${API_BASE_URL}/api/auth/signup`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(input),
  })
  const payload = await parseJson(response)
  if (!response.ok || !payload?.user) {
    throw new Error(errorMessage(payload, '회원가입에 실패했어요.'))
  }
  const accessToken = typeof payload.accessToken === 'string' ? payload.accessToken : undefined
  if (accessToken) setAccessToken(accessToken)
  return { user: payload.user as ApiUser, accessToken }
}

export async function fetchMailStatus(): Promise<{ smtpConfigured: boolean; mode: string; message: string }> {
  const response = await fetch(`${API_BASE_URL}/api/mail/status`)
  const payload = await parseJson(response)
  if (!response.ok) {
    throw new Error(errorMessage(payload, '메일 상태를 확인할 수 없어요.'))
  }
  return {
    smtpConfigured: Boolean(payload?.smtpConfigured),
    mode: typeof payload?.mode === 'string' ? payload.mode : 'unknown',
    message: errorMessage(payload, ''),
  }
}

export async function fetchFundings(params?: {
  lat?: number
  lng?: number
  radiusKm?: number
}): Promise<ApiFunding[]> {
  const qs = new URLSearchParams()
  if (params?.lat != null) qs.set('lat', String(params.lat))
  if (params?.lng != null) qs.set('lng', String(params.lng))
  if (params?.radiusKm != null) qs.set('radiusKm', String(params.radiusKm))
  const query = qs.toString()
  const response = await fetch(`${API_BASE_URL}/api/fundings${query ? `?${query}` : ''}`, {
    headers: authHeaders(),
  })
  const payload = await parseJson(response)
  if (!response.ok) {
    throw new Error(errorMessage(payload, '펀딩 목록을 불러오지 못했어요.'))
  }
  return (payload?.fundings as ApiFunding[]) ?? []
}

export async function createFundingApi(input: {
  category: string
  title: string
  description: string
  address: string
  locationName: string
  lat: number
  lng: number
  meetAt: string
  meetTimeText: string
  deadlineAt: string
  deadlineText: string
  targetCount: number
  fee: number
}): Promise<ApiFunding> {
  const response = await fetch(`${API_BASE_URL}/api/fundings`, {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(input),
  })
  const payload = await parseJson(response)
  if (!response.ok || !payload?.funding) {
    throw new Error(errorMessage(payload, '펀딩 생성에 실패했어요.'))
  }
  return payload.funding as ApiFunding
}

export async function joinFundingApi(fundingId: number): Promise<ApiFunding> {
  const response = await fetch(`${API_BASE_URL}/api/fundings/${fundingId}/join`, {
    method: 'POST',
    headers: authHeaders(),
  })
  const payload = await parseJson(response)
  if (!response.ok || !payload?.funding) {
    throw new Error(errorMessage(payload, '참여에 실패했어요.'))
  }
  return payload.funding as ApiFunding
}
