import React from 'react';

interface LoadingProps {
  message?: string;
  fullScreen?: boolean;
}

export const Loading: React.FC<LoadingProps> = ({
  message = 'Đang tải dữ liệu...',
  fullScreen = false,
}) => {
  const content = (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '16px',
        padding: '32px',
      }}
      className="animate-fade-in"
    >
      <div
        className="animate-spin"
        style={{
          width: '38px',
          height: '38px',
          borderRadius: '50%',
          border: '3px solid rgba(59, 130, 246, 0.2)',
          borderTopColor: 'var(--primary-500)',
        }}
      />
      <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', fontWeight: 500 }}>
        {message}
      </p>
    </div>
  );

  if (fullScreen) {
    return (
      <div
        style={{
          position: 'fixed',
          inset: 0,
          backgroundColor: 'var(--bg-app)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 9999,
        }}
      >
        {content}
      </div>
    );
  }

  return content;
};
