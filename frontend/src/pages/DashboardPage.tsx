import React from 'react';
import { useAuth } from '@/hooks/useAuth';
import { RoleType } from '@/types/role.types';
import { AdminDashboard } from '@/features/dashboard/components/AdminDashboard';
import { ManagerDashboard } from '@/features/dashboard/components/ManagerDashboard';
import { ReceptionistDashboard } from '@/features/dashboard/components/ReceptionistDashboard';
import { TechnicianDashboard } from '@/features/dashboard/components/TechnicianDashboard';
import { CustomerDashboard } from '@/features/dashboard/components/CustomerDashboard';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const userRoles = (user?.roles || []) as RoleType[];

  if (userRoles.includes('ROLE_ADMIN')) {
    return <AdminDashboard />;
  }

  if (userRoles.includes('ROLE_MANAGER')) {
    return <ManagerDashboard />;
  }

  if (userRoles.includes('ROLE_FRONT_DESK')) {
    return <ReceptionistDashboard />;
  }

  if (userRoles.includes('ROLE_TECHNICIAN')) {
    return <TechnicianDashboard />;
  }

  if (userRoles.includes('ROLE_CUSTOMER')) {
    return <CustomerDashboard />;
  }

  return <AdminDashboard />;
};
