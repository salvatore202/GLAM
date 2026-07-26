import type { ReactNode } from 'react'
import { AlertTriangle, CheckCircle2, Info } from 'lucide-react'

type Kind = 'success' | 'error' | 'info'

const styles: Record<Kind, { wrap: string; icon: ReactNode }> = {
  success: {
    wrap: 'bg-velvet-pale text-velvet-deep border-velvet/30',
    icon: <CheckCircle2 className="h-4 w-4 shrink-0" />,
  },
  error: {
    wrap: 'bg-wine-pale text-wine-deep border-wine/30',
    icon: <AlertTriangle className="h-4 w-4 shrink-0" />,
  },
  info: {
    wrap: 'bg-brass-pale text-ink border-brass/30',
    icon: <Info className="h-4 w-4 shrink-0" />,
  },
}

export function Alert({ kind = 'info', children }: { kind?: Kind; children: ReactNode }) {
  const s = styles[kind]
  return (
    <div className={`flex items-start gap-2.5 rounded-lg border px-4 py-3 text-sm ${s.wrap}`}>
      {s.icon}
      <div className="min-w-0">{children}</div>
    </div>
  )
}
