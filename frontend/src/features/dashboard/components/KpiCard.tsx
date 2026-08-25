import React from 'react';

interface KpiCardProps {
  label: string;
  value: string | number;
  icon: string;
  trend?: string;
  trendUp?: boolean;
  colorTheme?: 'default' | 'orange' | 'green' | 'slate';
  isLoading?: boolean;
}

export const KpiCard: React.FC<KpiCardProps> = ({
  label,
  value,
  icon,
  trend,
  trendUp = true,
  colorTheme = 'default',
  isLoading = false,
}) => {
  if (isLoading) {
    return (
      <div className="kpi-card" style={{ minHeight: '130px', animation: 'pulseGlow 1.5s infinite' }}>
        <div className="kpi-card-header">
          <div style={{ width: '42px', height: '42px', backgroundColor: 'var(--color-surface-container)', borderRadius: 'var(--radius-md)' }} />
          <div style={{ width: '50px', height: '20px', backgroundColor: 'var(--color-surface-container)', borderRadius: 'var(--radius-full)' }} />
        </div>
        <div style={{ width: '80px', height: '14px', backgroundColor: 'var(--color-surface-container)', borderRadius: '4px', marginBottom: '8px' }} />
        <div style={{ width: '120px', height: '28px', backgroundColor: 'var(--color-surface-container)', borderRadius: '4px' }} />
      </div>
    );
  }

  return (
    <div className="kpi-card">
      <div className="kpi-card-header">
        <div className={`kpi-icon-box ${colorTheme}`}>
          <span className="material-symbols-outlined">{icon}</span>
        </div>
        {trend && (
          <span className={`kpi-trend-tag ${trendUp ? '' : 'danger'}`}>
            <span className="material-symbols-outlined" style={{ fontSize: '14px' }}>
              {trendUp ? 'trending_up' : 'trending_down'}
            </span>{' '}
            {trend}
          </span>
        )}
      </div>
      <div>
        <div className="kpi-label">{label}</div>
        <div className="kpi-value">{value}</div>
      </div>
    </div>
  );
};
