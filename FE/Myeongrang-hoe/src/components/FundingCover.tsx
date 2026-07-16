import { useState, type ReactNode } from 'react'
import { getFundingCoverSrc, type CoverSource } from '../lib/coverImage'

type Props = {
  source: CoverSource
  size?: 'hero' | 'card' | 'thumb'
  className?: string
  imgClassName?: string
  alt?: string
  /** 위치 지도 이미지 위에 올릴 오버레이 (카테고리 뱃지 등) */
  children?: ReactNode
}

/**
 * 사용자 사진이 있으면 그 사진을, 없으면 위치 기반 지도 대표 이미지를 보여줍니다.
 */
export default function FundingCover({
  source,
  size = 'card',
  className = '',
  imgClassName = 'absolute inset-0 h-full w-full object-cover',
  alt = '',
  children,
}: Props) {
  const [failed, setFailed] = useState(false)
  const src = getFundingCoverSrc(source, size)
  const hasUpload = !!source.coverImage?.trim()

  return (
    <div
      className={`relative overflow-hidden bg-[var(--hairline)] ${className}`}
      style={
        failed && !hasUpload
          ? { backgroundImage: 'linear-gradient(149.6deg, #72abfa 0%, #2777e7 71.4%)' }
          : undefined
      }
    >
      {!failed && (
        <img
          src={src}
          alt={alt}
          className={imgClassName}
          loading="lazy"
          onError={() => setFailed(true)}
        />
      )}
      {children}
    </div>
  )
}
