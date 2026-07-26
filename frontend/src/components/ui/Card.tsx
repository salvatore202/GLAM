import type { HTMLAttributes } from 'react'

export function Card({ className = '', ...rest }: HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={`rounded-2xl border border-ink/10 bg-parchment shadow-[0_1px_2px_rgba(36,28,22,0.06),0_8px_24px_-12px_rgba(36,28,22,0.25)] ${className}`}
      {...rest}
    />
  )
}
