import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { LoginPage } from '@/pages/LoginPage';
import { DashboardPage } from '@/pages/DashboardPage';
import { UnauthorizedPage } from '@/pages/UnauthorizedPage';
import { NotFoundPage } from '@/pages/NotFoundPage';
import { PlaceholderPage } from '@/pages/PlaceholderPage';
import { AppLayout } from '@/components/layout/AppLayout';
import { ProtectedRoute } from './ProtectedRoute';
import { RoleGuard } from './RoleGuard';

import { EmployeesPage } from '@/features/employees/pages/EmployeesPage';
import { CustomersPage } from '@/features/customers/pages/CustomersPage';
import { UsersPage } from '@/features/users/pages/UsersPage';
import { VehiclesPage } from '@/features/vehicles/pages/VehiclesPage';

export const AppRouter: React.FC = () => {
  return (
    <Routes>
      {/* Public Route */}
      <Route path="/login" element={<LoginPage />} />

      {/* Root redirect */}
      <Route path="/" element={<Navigate to="/app/dashboard" replace />} />

      {/* Protected Area */}
      <Route
        path="/app"
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/app/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />

        {/* Admin only routes */}
        <Route
          path="branches"
          element={
            <RoleGuard roles={['ROLE_ADMIN']}>
              <PlaceholderPage moduleName="Quản lý chi nhánh" taskNumber="Frontend Task Chi nhánh" />
            </RoleGuard>
          }
        />
        <Route
          path="users"
          element={
            <RoleGuard roles={['ROLE_ADMIN']}>
              <UsersPage />
            </RoleGuard>
          }
        />
        <Route
          path="settings"
          element={
            <RoleGuard roles={['ROLE_ADMIN']}>
              <PlaceholderPage moduleName="Cài đặt hệ thống" taskNumber="Frontend Task Cài đặt" />
            </RoleGuard>
          }
        />

        {/* Manager & Admin routes */}
        <Route
          path="employees"
          element={
            <RoleGuard roles={['ROLE_ADMIN', 'ROLE_MANAGER']}>
              <EmployeesPage />
            </RoleGuard>
          }
        />
        <Route
          path="services"
          element={
            <RoleGuard roles={['ROLE_ADMIN', 'ROLE_MANAGER']}>
              <PlaceholderPage moduleName="Danh mục dịch vụ & giá" taskNumber="Frontend Task Dịch vụ" />
            </RoleGuard>
          }
        />
        <Route
          path="inventory"
          element={
            <RoleGuard roles={['ROLE_ADMIN', 'ROLE_MANAGER']}>
              <PlaceholderPage moduleName="Kho & Phụ tùng" taskNumber="Frontend Task Kho phụ tùng" />
            </RoleGuard>
          }
        />
        <Route
          path="quotations"
          element={
            <RoleGuard roles={['ROLE_ADMIN', 'ROLE_MANAGER']}>
              <PlaceholderPage moduleName="Báo giá dịch vụ" taskNumber="Frontend Task Báo giá" />
            </RoleGuard>
          }
        />
        <Route
          path="reports"
          element={
            <RoleGuard roles={['ROLE_ADMIN', 'ROLE_MANAGER']}>
              <PlaceholderPage moduleName="Báo cáo & Thống kê" taskNumber="Frontend Task Báo cáo" />
            </RoleGuard>
          }
        />

        {/* Receptionist, Manager, Admin routes */}
        <Route
          path="reception"
          element={
            <RoleGuard roles={['ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_FRONT_DESK']}>
              <PlaceholderPage moduleName="Tiếp nhận xe" taskNumber="Frontend Task Tiếp nhận" />
            </RoleGuard>
          }
        />
        <Route
          path="customers"
          element={
            <RoleGuard roles={['ROLE_ADMIN']}>
              <CustomersPage />
            </RoleGuard>
          }
        />

        {/* Technician specific */}
        <Route
          path="execution"
          element={
            <RoleGuard roles={['ROLE_ADMIN', 'ROLE_TECHNICIAN']}>
              <PlaceholderPage moduleName="Tiến độ thực hiện & sửa chữa" taskNumber="Frontend Task Tiến độ" />
            </RoleGuard>
          }
        />

        {/* Common protected business routes */}
        <Route
          path="appointments"
          element={<PlaceholderPage moduleName="Lịch hẹn dịch vụ" taskNumber="Frontend Task Lịch hẹn" />}
        />
        <Route
          path="repair-orders"
          element={<PlaceholderPage moduleName="Lệnh sửa chữa" taskNumber="Frontend Task Lệnh sửa chữa" />}
        />
        <Route
          path="vehicles"
          element={
            <RoleGuard roles={['ROLE_ADMIN', 'ROLE_CUSTOMER']}>
              <VehiclesPage />
            </RoleGuard>
          }
        />
        <Route
          path="invoices"
          element={<PlaceholderPage moduleName="Hóa đơn & Thanh toán" taskNumber="Frontend Task Hóa đơn" />}
        />
        <Route
          path="notifications"
          element={<PlaceholderPage moduleName="Thông báo hệ thống" taskNumber="Frontend Task Thông báo" />}
        />
        <Route
          path="chat"
          element={<PlaceholderPage moduleName="Trao đổi nội bộ & Chat" taskNumber="Frontend Task Chat" />}
        />
      </Route>

      {/* Error & Fallback Routes */}
      <Route path="/unauthorized" element={<UnauthorizedPage />} />
      <Route path="/404" element={<NotFoundPage />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};
