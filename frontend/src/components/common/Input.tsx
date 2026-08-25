import { InputHTMLAttributes, forwardRef, ReactNode } from 'react';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  helperText?: string;
  leftIcon?: ReactNode | string;
  rightAction?: ReactNode;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, helperText, leftIcon, rightAction, id, className = '', ...props }, ref) => {
    const inputId = id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined);
    const errorId = error && inputId ? `${inputId}-error` : undefined;
    const helperId = helperText && inputId ? `${inputId}-helper` : undefined;

    return (
      <div className="form-group">
        {label && (
          <label htmlFor={inputId} className="form-label">
            {label}
          </label>
        )}
        <div className="form-input-container">
          {leftIcon && (
            <div className="form-input-icon">
              {typeof leftIcon === 'string' ? (
                <span className="material-symbols-outlined">{leftIcon}</span>
              ) : (
                leftIcon
              )}
            </div>
          )}
          <input
            id={inputId}
            ref={ref}
            className={`form-input ${leftIcon ? 'has-left-icon' : ''} ${
              rightAction ? 'has-right-icon' : ''
            } ${error ? 'border-error' : ''} ${className}`}
            aria-invalid={!!error}
            aria-describedby={errorId || helperId}
            {...props}
          />
          {rightAction && <div className="form-input-action-btn">{rightAction}</div>}
        </div>
        {error && (
          <span id={errorId} style={{ color: 'var(--color-danger)', fontSize: '0.8rem', marginTop: '2px' }}>
            {error}
          </span>
        )}
        {helperText && !error && (
          <span id={helperId} style={{ color: 'var(--color-outline)', fontSize: '0.8rem', marginTop: '2px' }}>
            {helperText}
          </span>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';
