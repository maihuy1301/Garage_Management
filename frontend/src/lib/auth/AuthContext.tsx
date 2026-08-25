import React, { createContext, useState, useEffect, useCallback, ReactNode } from 'react';
import { CurrentUser, LoginRequest } from '@/types/auth.types';
import { RoleType } from '@/types/role.types';
import { tokenStorage, parseJwtClaims } from './token-storage';
import { authService } from '@/features/auth/services/auth.service';
import { registerUnauthorizedHandler } from '@/lib/api/error-handler';

export interface AuthContextType {
  user: CurrentUser | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => void;
  refreshUser: () => Promise<void>;
  hasRole: (role: RoleType) => boolean;
  hasAnyRole: (roles: RoleType[]) => boolean;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [token, setToken] = useState<string | null>(() => tokenStorage.getToken());
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const extractRolesFromToken = (jwtToken: string): RoleType[] => {
    const claims = parseJwtClaims(jwtToken);
    if (claims && Array.isArray(claims.roles)) {
      const rawRoles = claims.roles as string[];
      const normalized: RoleType[] = [];
      rawRoles.forEach((r) => {
        if (r === 'ROLE_ADMIN' || r === 'ADMIN') {
          if (!normalized.includes('ROLE_ADMIN')) normalized.push('ROLE_ADMIN');
        } else if (r === 'ROLE_MANAGER' || r === 'MANAGER') {
          if (!normalized.includes('ROLE_MANAGER')) normalized.push('ROLE_MANAGER');
        } else if (r === 'ROLE_FRONT_DESK' || r === 'FRONT_DESK') {
          if (!normalized.includes('ROLE_FRONT_DESK')) normalized.push('ROLE_FRONT_DESK');
        } else if (r === 'ROLE_TECHNICIAN' || r === 'TECHNICIAN') {
          if (!normalized.includes('ROLE_TECHNICIAN')) normalized.push('ROLE_TECHNICIAN');
        } else if (r === 'ROLE_CUSTOMER' || r === 'CUSTOMER') {
          if (!normalized.includes('ROLE_CUSTOMER')) normalized.push('ROLE_CUSTOMER');
        } else if (
          r === 'ROLE_ADMIN' ||
          r === 'ROLE_MANAGER' ||
          r === 'ROLE_FRONT_DESK' ||
          r === 'ROLE_TECHNICIAN' ||
          r === 'ROLE_CUSTOMER'
        ) {
          normalized.push(r as RoleType);
        }
      });
      return normalized;
    }
    return [];
  };

  const logout = useCallback(() => {
    tokenStorage.removeToken();
    setToken(null);
    setUser(null);
    setError(null);
  }, []);

  const refreshUser = useCallback(async () => {
    const currentToken = tokenStorage.getToken();
    if (!currentToken) {
      setUser(null);
      setIsLoading(false);
      return;
    }

    try {
      setIsLoading(true);
      const meData = await authService.getCurrentUser();
      const roles = extractRolesFromToken(currentToken);

      setUser({
        maNguoiDung: meData.maNguoiDung,
        tenDangNhap: meData.tenDangNhap,
        hoTen: meData.hoTen,
        email: meData.email,
        roles,
      });
      setError(null);
    } catch (err: unknown) {
      console.warn('Failed to fetch current user profile:', err);
      logout();
    } finally {
      setIsLoading(false);
    }
  }, [logout]);

  useEffect(() => {
    registerUnauthorizedHandler(() => {
      logout();
    });
    refreshUser();
  }, [logout, refreshUser]);

  const login = async (credentials: LoginRequest): Promise<void> => {
    setIsLoading(true);
    setError(null);
    try {
      const loginResponse = await authService.login(credentials);
      tokenStorage.setToken(loginResponse.accessToken);
      setToken(loginResponse.accessToken);

      const roles = extractRolesFromToken(loginResponse.accessToken);

      // Hydrate via /api/auth/me
      try {
        const meData = await authService.getCurrentUser();
        setUser({
          maNguoiDung: meData.maNguoiDung,
          tenDangNhap: meData.tenDangNhap,
          hoTen: meData.hoTen,
          email: meData.email,
          roles,
        });
      } catch {
        // Fallback to login response data if /me has temporary glitch
        setUser({
          maNguoiDung: loginResponse.maNguoiDung,
          tenDangNhap: loginResponse.tenDangNhap,
          hoTen: loginResponse.hoTen,
          roles,
        });
      }
    } catch (err: unknown) {
      const message = (err && typeof err === 'object' && 'message' in err)
        ? (err as { message: string }).message
        : 'Đăng nhập thất bại';
      setError(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const hasRole = useCallback(
    (role: RoleType): boolean => {
      if (!user) return false;
      return user.roles.includes(role);
    },
    [user]
  );

  const hasAnyRole = useCallback(
    (roles: RoleType[]): boolean => {
      if (!user) return false;
      return roles.some((r) => user.roles.includes(r));
    },
    [user]
  );

  const value: AuthContextType = {
    user,
    token,
    isAuthenticated: !!token && !!user,
    isLoading,
    error,
    login,
    logout,
    refreshUser,
    hasRole,
    hasAnyRole,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
