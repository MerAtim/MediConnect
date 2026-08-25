import React from 'react'
import { ESTADO_BADGE } from '../config.js'

export default function EstadoBadge({estado}){
  return <span className={ESTADO_BADGE[estado] ?? 'badge bg-neutral-100 text-neutral-600'}>{estado}</span>
}
