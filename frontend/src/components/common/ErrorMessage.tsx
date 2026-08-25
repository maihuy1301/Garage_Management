import React from 'react';

interface ErrorMessageProps {
  message?: string | null;
  onRetry?: () => void;
}

export const ErrorMessage: React.FC<ErrorMessageProps> = ({ message, onRetry }) => {
  if (!message) return null;

  return (
    <div className="alert alert-error animate-fade-in" role="alert">
      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flex: 1 }}>
        <svg
          style={{ width: '18px', height: '18px', flexShrink: 0 }}
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>
        <span>{message}</span>
      </div>
      {onRetry && (
        <button
          onClick={onRetry}
          className="btn btn-secondary"
          style={{ padding: '4px 10px', fontSize: '0.8rem' }}
        >
          Thử lại
        </button>
      )}
    </div>
  );
};
