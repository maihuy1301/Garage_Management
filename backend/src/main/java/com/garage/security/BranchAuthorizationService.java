package com.garage.security;

import com.garage.repository.NhanVienRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * BranchAuthorizationService — centralized branch access check.
 *
 * Rules:
 *   SYSTEM_ADMIN → access any branch (global).
 *   BRANCH_MANAGER / RECEPTIONIST / TECHNICIAN → own branch only.
 *   CUSTOMER / others → denied for branch-employee endpoints.
 *
 * Branch is resolved from:
 *   JWT → SecurityContext → CustomUserDetails (NguoiDung)
 *       → NhanVien → ChiNhanh.maChiNhanh
 *
 * Never trust maChiNhanh sent from the client.
 */
@Service
public class BranchAuthorizationService {

    private final NhanVienRepository nhanVienRepository;

    public BranchAuthorizationService(NhanVienRepository nhanVienRepository) {
        this.nhanVienRepository = nhanVienRepository;
    }

    /**
     * Returns true if the currently authenticated user is allowed to access the given branch (by numeric PK).
     */
    public boolean isAllowedBranch(Integer requestedBranchId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        // ROLE_ADMIN bypasses branch restriction
        if (hasRole(auth, "ROLE_ADMIN")) {
            return true;
        }

        // For branch-staff roles: resolve their actual branch from DB
        if (hasRole(auth, "ROLE_MANAGER")
                || hasRole(auth, "ROLE_FRONT_DESK")
                || hasRole(auth, "ROLE_TECHNICIAN")) {
            return resolveUserBranchId(auth)
                    .map(branchId -> branchId.equals(requestedBranchId))
                    .orElse(false);
        }

        // ROLE_CUSTOMER and anything else → denied
        return false;
    }

    /**
     * Returns true if the currently authenticated user is allowed to access the given branch (by ID string).
     */
    public boolean isAllowedBranchByCode(String requestedBranchIdStr) {
        if (requestedBranchIdStr == null || requestedBranchIdStr.isBlank()) {
            return false;
        }
        try {
            Integer branchId = Integer.parseInt(requestedBranchIdStr.trim());
            return isAllowedBranch(branchId);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Resolves the branch PK (MaChiNhanh) for the currently authenticated user.
     * Returns empty if user is not a branch employee.
     */
    public Optional<Integer> resolveUserBranchId(Authentication auth) {
        if (auth == null) return Optional.empty();
        CustomUserDetails userDetails = extractUserDetails(auth);
        if (userDetails == null) return Optional.empty();
        Integer maNguoiDung = userDetails.getNguoiDung().getMaNguoiDung();
        return nhanVienRepository.findByNguoiDungMaNguoiDung(maNguoiDung)
                .map(nv -> nv.getChiNhanh() != null ? nv.getChiNhanh().getMaChiNhanh() : null);
    }

    /**
     * Convenience: resolves branch ID for authenticated user from SecurityContext.
     */
    public Optional<Integer> resolveCurrentUserBranchId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return resolveUserBranchId(auth);
    }

    // --- private helpers ---

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }

    private CustomUserDetails extractUserDetails(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }
        return null;
    }
}
