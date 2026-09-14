import React from 'react'

export default function ToastContainer({toasts, onDismiss}){
  if(toasts.length === 0) return null
  return (
    <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2 w-full max-w-sm px-4 sm:px-0">
      {toasts.map(t => (
        <div
          key={t.id}
          role={t.type === 'success' ? 'status' : 'alert'}
          aria-live={t.type === 'success' ? 'polite' : 'assertive'}
          className={`toast ${t.type === 'success' ? 'toast-success' : 'toast-error'} ${t.leaving ? 'toast-leaving' : ''} flex items-start gap-2`}
        >
          <span className="flex-1">{t.message}</span>
          {onDismiss && (
            <button
              type="button"
              onClick={() => onDismiss(t.id)}
              aria-label="Descartar aviso"
              className="shrink-0 leading-none opacity-70 hover:opacity-100"
            >
              ×
            </button>
          )}
        </div>
      ))}
    </div>
  )
}
