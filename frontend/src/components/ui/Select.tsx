import { useId, type ReactNode, type SelectHTMLAttributes } from 'react'

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string
  children: ReactNode
}

export function Select({ label, id, className = '', children, ...rest }: SelectProps) {
  const autoId = useId()
  const fieldId = id ?? autoId
  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={fieldId} className="text-xs font-semibold uppercase tracking-wider text-ink-soft">
        {label}
      </label>
      <select
        id={fieldId}
        className={`rounded-lg border border-ink/15 bg-white/70 px-3.5 py-2.5 text-sm text-ink focus:border-brass-deep ${className}`}
        {...rest}
      >
        {children}
      </select>
    </div>
  )
}
