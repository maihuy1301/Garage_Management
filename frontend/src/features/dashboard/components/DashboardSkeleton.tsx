import React from 'react';

export const DashboardSkeleton: React.FC = () => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* Header Skeleton */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <div style={{ width: '220px', height: '28px', backgroundColor: 'var(--color-surface-container)', borderRadius: '6px', marginBottom: '8px' }} />
          <div style={{ width: '320px', height: '16px', backgroundColor: 'var(--color-surface-container)', borderRadius: '4px' }} />
        </div>
        <div style={{ display: 'flex', gap: '10px' }}>
          <div style={{ width: '100px', height: '38px', backgroundColor: 'var(--color-surface-container)', borderRadius: '8px' }} />
          <div style={{ width: '120px', height: '38px', backgroundColor: 'var(--color-surface-container)', borderRadius: '8px' }} />
        </div>
      </div>

      {/* KPI Cards Grid Skeleton */}
      <div className="kpi-grid">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="kpi-card" style={{ minHeight: '130px', opacity: 0.7 }}>
            <div className="kpi-card-header">
              <div style={{ width: '42px', height: '42px', backgroundColor: 'var(--color-surface-container)', borderRadius: 'var(--radius-md)' }} />
              <div style={{ width: '50px', height: '20px', backgroundColor: 'var(--color-surface-container)', borderRadius: 'var(--radius-full)' }} />
            </div>
            <div style={{ width: '90px', height: '14px', backgroundColor: 'var(--color-surface-container)', borderRadius: '4px', marginBottom: '8px' }} />
            <div style={{ width: '110px', height: '26px', backgroundColor: 'var(--color-surface-container)', borderRadius: '4px' }} />
          </div>
        ))}
      </div>

      {/* Panels Skeleton */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '20px' }}>
        <div className="card" style={{ height: '220px', backgroundColor: '#ffffff', opacity: 0.7 }}>
          <div style={{ width: '160px', height: '20px', backgroundColor: 'var(--color-surface-container)', borderRadius: '4px', marginBottom: '16px' }} />
          <div style={{ width: '100%', height: '120px', backgroundColor: 'var(--color-surface-container)', borderRadius: '8px' }} />
        </div>
        <div className="card" style={{ height: '220px', backgroundColor: '#ffffff', opacity: 0.7 }}>
          <div style={{ width: '160px', height: '20px', backgroundColor: 'var(--color-surface-container)', borderRadius: '4px', marginBottom: '16px' }} />
          <div style={{ width: '100%', height: '120px', backgroundColor: 'var(--color-surface-container)', borderRadius: '8px' }} />
        </div>
      </div>
    </div>
  );
};
