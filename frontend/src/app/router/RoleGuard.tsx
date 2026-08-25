import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { RoleType } from '@/types/role.types';
import { Loading } from '@/components/common/Loading';

interface RoleGuardProps {
  roles: RoleType[];
  children: React.ReactElement;
}

export const RoleGuard: React.FC<RoleGuardProps> = ({ roles, children }) => {
  const { hasAnyRole, isLoading, isAuthenticated } = useAuth();

  if (isLoading) {
    return <Loading fullScreen message="Đang kiểm tra quyền truy cập..." />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!hasAnyRole(roles)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
};
