import React from 'react'

export default function SkeletonRows({columns, rows = 3}){
  return Array.from({length: rows}).map((_, i) => (
    <tr key={i}>
      {Array.from({length: columns}).map((_, j) => (
        <td key={j} className="px-4 py-3">
          <div className="skeleton" style={{width: `${60 + (j * 13) % 35}%`}} />
        </td>
      ))}
    </tr>
  ))
}
