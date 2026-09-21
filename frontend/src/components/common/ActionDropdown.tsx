import React, { useState, useRef, useEffect, useCallback } from 'react';

export interface ActionDropdownItem {
  key?: string;
  label: string;
  icon?: string;
  onClick: () => void;
  variant?: 'default' | 'primary' | 'success' | 'warning' | 'danger';
  disabled?: boolean;
  hidden?: boolean;
}

interface ActionDropdownProps {
  items: (ActionDropdownItem | null | undefined | false)[];
  align?: 'left' | 'right';
  triggerIcon?: string;
  triggerTitle?: string;
}

export const ActionDropdown: React.FC<ActionDropdownProps> = ({
  items,
  align = 'right',
  triggerIcon = 'more_vert',
  triggerTitle = 'Thao tác',
}) => {
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const [placement, setPlacement] = useState<'bottom' | 'top'>('bottom');
  const dropdownRef = useRef<HTMLDivElement>(null);

  // Filter out falsy items
  const validItems = items.filter(
    (item): item is ActionDropdownItem => Boolean(item) && !Boolean((item as ActionDropdownItem)?.hidden)
  );

  const handleClose = useCallback(() => {
    setIsOpen(false);
  }, []);

  const handleToggle = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (!isOpen && dropdownRef.current) {
      const rect = dropdownRef.current.getBoundingClientRect();
      const spaceBelow = window.innerHeight - rect.bottom;
      const spaceAbove = rect.top;
      const estimatedHeight = validItems.length * 40 + 20;

      // If not enough room below but enough room above, open upwards
      if (spaceBelow < estimatedHeight && spaceAbove > estimatedHeight) {
        setPlacement('top');
      } else {
        setPlacement('bottom');
      }
    }
    setIsOpen((prev) => !prev);
  };

  // Close when clicking outside
  useEffect(() => {
    if (!isOpen) return;

    const handleDocumentClick = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
        handleClose();
      }
    };

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        handleClose();
      }
    };

    document.addEventListener('mousedown', handleDocumentClick);
    document.addEventListener('keydown', handleKeyDown);

    return () => {
      document.removeEventListener('mousedown', handleDocumentClick);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, handleClose]);

  if (validItems.length === 0) return null;

  return (
    <div
      ref={dropdownRef}
      className="action-dropdown"
      style={{
        position: 'relative',
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <button
        type="button"
        className={`table-action-btn action-dropdown-trigger ${isOpen ? 'active' : ''}`}
        title={triggerTitle}
        aria-label={triggerTitle}
        aria-haspopup="true"
        aria-expanded={isOpen}
        onClick={handleToggle}
        style={{
          cursor: 'pointer',
          borderRadius: 'var(--radius-md, 8px)',
          border: '1px solid transparent',
          backgroundColor: isOpen ? 'rgba(0, 0, 0, 0.06)' : 'transparent',
          color: isOpen ? '#0f172a' : '#64748b',
          transition: 'all 0.15s ease',
        }}
      >
        <span className="material-symbols-outlined" style={{ fontSize: '20px' }}>
          {triggerIcon}
        </span>
      </button>

      {isOpen && (
        <div
          className={`action-dropdown-menu align-${align} placement-${placement}`}
          onClick={(e) => e.stopPropagation()}
          style={{
            position: 'absolute',
            ...(placement === 'top'
              ? { bottom: 'calc(100% + 6px)', top: 'auto' }
              : { top: 'calc(100% + 6px)', bottom: 'auto' }),
            [align]: 0,
            zIndex: 9999,
            minWidth: '200px',
            backgroundColor: '#ffffff',
            border: '1px solid #e2e8f0',
            borderRadius: '10px',
            boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.12), 0 8px 10px -6px rgba(0, 0, 0, 0.08), 0 0 1px 1px rgba(0,0,0,0.04)',
            padding: '6px',
            display: 'flex',
            flexDirection: 'column',
            gap: '2px',
            animation: 'fadeIn 0.12s ease-out',
          }}
        >
          {validItems.map((item, index) => {
            const isDanger = item.variant === 'danger';
            const isWarning = item.variant === 'warning';
            const isSuccess = item.variant === 'success';
            const isPrimary = item.variant === 'primary';

            let itemColor = '#1e293b'; // slate-800
            let iconColor = '#64748b'; // slate-500
            let hoverBg = '#f1f5f9';  // slate-100

            if (isDanger) {
              itemColor = '#dc2626'; // red-600
              iconColor = '#dc2626';
              hoverBg = '#fef2f2'; // red-50
            } else if (isWarning) {
              itemColor = '#d97706'; // amber-600
              iconColor = '#d97706';
              hoverBg = '#fffbeb'; // amber-50
            } else if (isSuccess) {
              itemColor = '#16a34a'; // green-600
              iconColor = '#16a34a';
              hoverBg = '#f0fdf4'; // green-50
            } else if (isPrimary) {
              itemColor = '#2563eb'; // blue-600
              iconColor = '#2563eb';
              hoverBg = '#eff6ff'; // blue-50
            }

            return (
              <button
                key={item.key || `${item.label}-${index}`}
                type="button"
                className="action-dropdown-item"
                disabled={item.disabled}
                onClick={() => {
                  if (item.disabled) return;
                  handleClose();
                  item.onClick();
                }}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '10px',
                  width: '100%',
                  padding: '9px 12px',
                  border: 'none',
                  background: 'transparent',
                  borderRadius: '6px',
                  textAlign: 'left',
                  cursor: item.disabled ? 'not-allowed' : 'pointer',
                  opacity: item.disabled ? 0.45 : 1,
                  fontSize: '0.85rem',
                  fontWeight: 500,
                  color: itemColor,
                  transition: 'background-color 0.12s ease, color 0.12s ease',
                  whiteSpace: 'nowrap',
                }}
                onMouseEnter={(e) => {
                  if (item.disabled) return;
                  e.currentTarget.style.backgroundColor = hoverBg;
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.backgroundColor = 'transparent';
                }}
              >
                {item.icon && (
                  <span
                    className="material-symbols-outlined"
                    style={{
                      fontSize: '18px',
                      color: iconColor,
                      flexShrink: 0,
                    }}
                  >
                    {item.icon}
                  </span>
                )}
                <span style={{ flex: 1, color: itemColor }}>{item.label}</span>
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
};
