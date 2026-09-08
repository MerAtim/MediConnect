import React from 'react'
import {useModalA11y} from '../useModalA11y.js'

export default function ConfirmModal({open, title, message, confirmLabel, cancelLabel = 'Volver', onConfirm, onCancel, danger = false}){
  const containerRef = useModalA11y(open, onCancel)
  if(!open) return null
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-neutral-900/40 px-4">
      <div ref={containerRef} className="card max-w-sm w-full" role="dialog" aria-modal="true" aria-labelledby="confirm-modal-title">
        <h3 id="confirm-modal-title" className="heading mb-2">{title}</h3>
        <p className="text-sm text-neutral-600 mb-6">{message}</p>
        <div className="flex justify-end gap-3">
          <button type="button" onClick={onCancel} className="btn-secondary">{cancelLabel}</button>
          <button type="button" onClick={onConfirm} className={danger ? 'btn-danger' : 'btn-primary'}>{confirmLabel}</button>
        </div>
      </div>
    </div>
  )
}
