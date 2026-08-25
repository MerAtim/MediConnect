import React from 'react'

export default function ToastContainer({toasts}){
  if(toasts.length === 0) return null
  return (
    <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2 w-full max-w-sm px-4 sm:px-0">
      {toasts.map(t => (
        <div
          key={t.id}
          className={`toast ${t.type === 'success' ? 'toast-success' : 'toast-error'} ${t.leaving ? 'toast-leaving' : ''}`}
        >
          {t.message}
        </div>
      ))}
    </div>
  )
}
