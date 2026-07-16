import { API_BASE_URL } from './api'

export type CoverSource = {
  coverImage?: string | null
  lat: number
  lng: number
}

/**
 * 대표(메인) 이미지 URL 우선순위
 * 1) 사용자가 올린 coverImage
 * 2) 지정 좌표 기반 서버 정적 지도 (카카오 REST → OSM 폴백)
 */
export function getFundingCoverSrc(
  source: CoverSource,
  size: 'hero' | 'card' | 'thumb' = 'card',
): string {
  const uploaded = source.coverImage?.trim()
  if (uploaded) return uploaded

  const dims =
    size === 'hero' ? { width: 720, height: 360 } : size === 'thumb' ? { width: 160, height: 160 } : { width: 240, height: 240 }

  const qs = new URLSearchParams({
    lat: String(source.lat),
    lng: String(source.lng),
    width: String(dims.width),
    height: String(dims.height),
  })
  return `${API_BASE_URL}/api/geo/static-map?${qs.toString()}`
}

export function hasUserCoverImage(coverImage?: string | null): boolean {
  return !!coverImage?.trim()
}
