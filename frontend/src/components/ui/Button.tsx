import type { ButtonHTMLAttributes, ReactNode } from 'react'

type Variant = 'primary' | 'secondary' | 'ghost' | 'danger'
type Size = 'md' | 'sm'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant
  size?: Size
  icon?: ReactNode
  loading?: boolean
}

const variantClasses: Record<Variant, string> = {
  primary: 'bg-brass text-stage-deep hover:bg-brass-deep disabled:bg-brass/50',
  secondary:
    'bg-transparent text-velvet-deep border border-velvet/40 hover:bg-velvet-pale disabled:opacity-50',
  ghost: 'bg-transparent text-ink-soft hover:bg-ink/5 disabled:opacity-50',
  danger: 'bg-wine text-parchment hover:bg-wine-deep disabled:bg-wine/50',
}

const sizeClasses: Record<Size, string> = {
  md: 'px-5 py-2.5 text-sm',
  sm: 'px-3.5 py-1.5 text-xs',
}

export function Button({
  variant = 'primary',
  size = 'md',
  icon,
  loading,
  className = '',
  children,
  disabled,
  ...rest
}: ButtonProps) {
  return (
    <button
      className={`inline-flex items-center justify-center gap-2 rounded-full font-semibold tracking-wide transition-colors duration-150 disabled:cursor-not-allowed ${variantClasses[variant]} ${sizeClasses[size]} ${className}`}
      disabled={disabled || loading}
      {...rest}
    >
      {loading ? (
        <span className="h-3.5 w-3.5 animate-spin rounded-full border-2 border-current border-t-transparent" />
      ) : (
        icon
      )}
      {children}
    </button>
  )
}
