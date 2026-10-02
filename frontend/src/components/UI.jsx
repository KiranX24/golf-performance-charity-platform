import React from 'react';

export function Button({ children, variant='primary', loading=false, className='', ...props }) {
  return <button className={`btn btn-${variant} ${className}`} disabled={loading || props.disabled} {...props}>{loading ? 'Processing...' : children}</button>;
}

export function Card({ children, className='' }) { return <div className={`card ${className}`}>{children}</div>; }
export function Badge({ children, tone='' }) { return <span className={`badge ${tone}`}>{children}</span>; }
export function Empty({ title='Nothing here yet', text='No records are available.' }) { return <div className="empty"><div className="empty-icon">◇</div><h3>{title}</h3><p>{text}</p></div>; }
export function Spinner() { return <div className="spinner" aria-label="Loading" />; }
export function Loading() { return <div className="loading-page"><Spinner /><span>Loading...</span></div>; }
export function ErrorBox({ message }) { return message ? <div className="alert error">{message}</div> : null; }
export function SuccessBox({ message }) { return message ? <div className="alert success">{message}</div> : null; }

export function Modal({ open, title, children, onClose }) {
  if (!open) return null;
  return <div className="modal-backdrop" onMouseDown={e => e.target === e.currentTarget && onClose()}>
    <div className="modal"><div className="modal-head"><h3>{title}</h3><button className="icon-btn" onClick={onClose}>×</button></div>{children}</div>
  </div>;
}

export function StatCard({ label, value, hint }) {
  return (
    <Card className="stat-card">
      <span
        className="stat-label"
        style={{
          minHeight: '48px',
          display: 'flex',
          alignItems: 'flex-start',
          lineHeight: '24px'
        }}
      >
        {label}
      </span>

      <strong>
        {value ?? '—'}
      </strong>

      {hint && (
        <small>
          {hint}
        </small>
      )}
    </Card>
  );
}

export function formatMoney(value, currency='INR') {
  const n = Number(value || 0);
  try { return new Intl.NumberFormat('en-IN', { style:'currency', currency, maximumFractionDigits:2 }).format(n); }
  catch { return `₹${n}`; }
}
export function formatDate(value) { if (!value) return '—'; const d = new Date(value); return Number.isNaN(d.getTime()) ? value : d.toLocaleDateString('en-IN', { day:'2-digit', month:'short', year:'numeric' }); }
export function statusTone(value='') { const x = String(value).toLowerCase(); if (x.includes('active') || x.includes('approved') || x.includes('published')) return 'good'; if (x.includes('pending') || x.includes('simulated') || x.includes('draft')) return 'warn'; if (x.includes('cancel') || x.includes('reject') || x.includes('lapsed') || x.includes('past')) return 'bad'; return ''; }

export function Page({ title, subtitle, children }) {
  return (
    <section className="page">
      <div className="page-header">
        <div>
          <h1>{title}</h1>
          {subtitle && <p>{subtitle}</p>}
        </div>
      </div>

      <div className="page-content">
        {children}
      </div>
    </section>
  );
}