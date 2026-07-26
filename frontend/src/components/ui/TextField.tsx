import { useId, type InputHTMLAttributes } from 'react'

interface TextFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  hint?: string
}

export function TextField({ label, hint, id, className = '', ...rest }: TextFieldProps) {
  const autoId = useId()
  const fieldId = id ?? autoId
  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={fieldId} className="text-xs font-semibold uppercase tracking-wider text-ink-soft">
        {label}
      </label>
      <input
        id={fieldId}
        className={`rounded-lg border border-ink/15 bg-white/70 px-3.5 py-2.5 text-sm text-ink placeholder:text-ink-faint focus:border-brass-deep ${className}`}
        {...rest}
      />
      {hint && <p className="text-xs text-ink-faint">{hint}</p>}
    </div>
  )
}
