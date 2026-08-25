package com.garage.service;

import com.garage.dto.*;
import com.garage.entity.*;
import com.garage.exception.BadRequestException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.*;
import com.garage.security.BranchAuthorizationService;
import com.garage.websocket.WebSocketEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final CuocHoiThoaiRepository cuocHoiThoaiRepository;
    private final TinNhanRepository tinNhanRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final PhieuTiepNhanRepository phieuTiepNhanRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final BranchAuthorizationService branchAuthorizationService;
    private final WebSocketEventPublisher webSocketEventPublisher;

    public ChatService(CuocHoiThoaiRepository cuocHoiThoaiRepository,
                       TinNhanRepository tinNhanRepository,
                       KhachHangRepository khachHangRepository,
                       NhanVienRepository nhanVienRepository,
                       PhieuTiepNhanRepository phieuTiepNhanRepository,
                       NguoiDungRepository nguoiDungRepository,
                       BranchAuthorizationService branchAuthorizationService,
                       WebSocketEventPublisher webSocketEventPublisher) {
        this.cuocHoiThoaiRepository = cuocHoiThoaiRepository;
        this.tinNhanRepository = tinNhanRepository;
        this.khachHangRepository = khachHangRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.phieuTiepNhanRepository = phieuTiepNhanRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.branchAuthorizationService = branchAuthorizationService;
        this.webSocketEventPublisher = webSocketEventPublisher;
    }

    /**
     * Tạo cuộc hội thoại mới
     */
    @Transactional
    public ConversationResponse createConversation(CreateConversationRequest req) {
        NguoiDung currentUser = getCurrentAuthenticatedUser();
        KhachHang khachHang;

        if (isCustomer()) {
            khachHang = khachHangRepository.findByNguoiDungMaNguoiDung(currentUser.getMaNguoiDung())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ khách hàng cho tài khoản hiện tại"));
        } else {
            if (req.getMaKhachHang() == null) {
                throw new BadRequestException("Bắt buộc cung cấp maKhachHang khi nhân viên tạo cuộc hội thoại");
            }
            khachHang = khachHangRepository.findById(req.getMaKhachHang())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng ID: " + req.getMaKhachHang()));
        }

        PhieuTiepNhan phieuTiepNhan = null;
        if (req.getMaTiepNhan() != null) {
            phieuTiepNhan = phieuTiepNhanRepository.findById(req.getMaTiepNhan())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu tiếp nhận ID: " + req.getMaTiepNhan()));

            // Nếu là customer, kiểm tra phiếu tiếp nhận có thuộc xe của mình không
            if (isCustomer() && !phieuTiepNhan.getXe().getKhachHang().getMaKhachHang().equals(khachHang.getMaKhachHang())) {
                throw new AccessDeniedException("Forbidden: Phiếu tiếp nhận không thuộc sở hữu của bạn");
            }

            // Chống duplicate: Nếu đã có cuộc hội thoại cho phiếu tiếp nhận này, trả về cuộc hội thoại hiện tại
            Optional<CuocHoiThoai> existing = cuocHoiThoaiRepository
                    .findByKhachHangMaKhachHangAndPhieuTiepNhanMaTiepNhan(khachHang.getMaKhachHang(), phieuTiepNhan.getMaTiepNhan());
            if (existing.isPresent()) {
                return mapToConversationResponse(existing.get(), currentUser.getMaNguoiDung());
            }
        }

        NhanVien nhanVien = null;
        if (req.getMaNhanVien() != null) {
            nhanVien = nhanVienRepository.findById(req.getMaNhanVien())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên ID: " + req.getMaNhanVien()));
        } else if (!isCustomer()) {
            // Nếu nhân viên tạo thì gán chính mình vào cuộc hội thoại
            nhanVien = nhanVienRepository.findByNguoiDungMaNguoiDung(currentUser.getMaNguoiDung()).orElse(null);
        }

        CuocHoiThoai cht = new CuocHoiThoai();
        cht.setKhachHang(khachHang);
        cht.setNhanVien(nhanVien);
        cht.setPhieuTiepNhan(phieuTiepNhan);
        cht.setTrangThai("DANG_MO");
        cht.setNgayCapNhatCuoi(LocalDateTime.now());

        CuocHoiThoai saved = cuocHoiThoaiRepository.save(cht);

        if (req.getInitialMessage() != null && !req.getInitialMessage().isBlank()) {
            TinNhan tn = new TinNhan();
            tn.setCuocHoiThoai(saved);
            tn.setNguoiGui(currentUser);
            tn.setNoiDung(req.getInitialMessage().trim());
            tn.setDaDoc(false);
            tinNhanRepository.save(tn);
        }

        return mapToConversationResponse(saved, currentUser.getMaNguoiDung());
    }

    /**
     * Lấy danh sách cuộc hội thoại của user hiện tại
     */
    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations() {
        NguoiDung currentUser = getCurrentAuthenticatedUser();
        List<CuocHoiThoai> list;

        if (isCustomer()) {
            KhachHang kh = khachHangRepository.findByNguoiDungMaNguoiDung(currentUser.getMaNguoiDung())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ khách hàng"));
            list = cuocHoiThoaiRepository.findByKhachHangMaKhachHangOrderByNgayCapNhatCuoiDesc(kh.getMaKhachHang());
        } else if (isSystemAdmin()) {
            list = cuocHoiThoaiRepository.findAllByOrderByNgayCapNhatCuoiDesc();
        } else {
            // Nhân viên chi nhánh (MANAGER, RECEPTIONIST, TECHNICIAN)
            Optional<Integer> branchIdOpt = branchAuthorizationService.resolveUserBranchId(SecurityContextHolder.getContext().getAuthentication());
            if (branchIdOpt.isPresent()) {
                list = cuocHoiThoaiRepository.findByBranchId(branchIdOpt.get());
            } else {
                Optional<NhanVien> nvOpt = nhanVienRepository.findByNguoiDungMaNguoiDung(currentUser.getMaNguoiDung());
                list = nvOpt.map(nv -> cuocHoiThoaiRepository.findByNhanVienMaNhanVienOrderByNgayCapNhatCuoiDesc(nv.getMaNhanVien()))
                        .orElse(List.of());
            }
        }

        return list.stream()
                .map(c -> mapToConversationResponse(c, currentUser.getMaNguoiDung()))
                .collect(Collectors.toList());
    }

    /**
     * Xem chi tiết cuộc hội thoại theo ID
     */
    @Transactional(readOnly = true)
    public ConversationResponse getConversationById(Integer conversationId) {
        CuocHoiThoai cht = cuocHoiThoaiRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hội thoại ID: " + conversationId));

        validateConversationAccess(cht);

        NguoiDung currentUser = getCurrentAuthenticatedUser();
        return mapToConversationResponse(cht, currentUser.getMaNguoiDung());
    }

    /**
     * Lấy lịch sử tin nhắn của cuộc hội thoại
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getConversationMessages(Integer conversationId) {
        CuocHoiThoai cht = cuocHoiThoaiRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hội thoại ID: " + conversationId));

        validateConversationAccess(cht);

        return tinNhanRepository.findByCuocHoiThoaiMaCuocHoiThoaiOrderByThoiGianGuiAsc(conversationId)
                .stream()
                .map(this::mapToChatMessageResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gửi tin nhắn mới vào cuộc hội thoại
     */
    @Transactional
    public ChatMessageResponse sendMessage(Integer conversationId, SendMessageRequest req) {
        CuocHoiThoai cht = cuocHoiThoaiRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hội thoại ID: " + conversationId));

        validateConversationAccess(cht);

        NguoiDung sender = getCurrentAuthenticatedUser();

        TinNhan tn = new TinNhan();
        tn.setCuocHoiThoai(cht);
        tn.setNguoiGui(sender);
        tn.setNoiDung(req.getNoiDung().trim());
        tn.setDuongDanTep(req.getDuongDanTep());
        tn.setDaDoc(false);

        TinNhan saved = tinNhanRepository.save(tn);

        cht.setNgayCapNhatCuoi(LocalDateTime.now());
        cuocHoiThoaiRepository.save(cht);

        ChatMessageResponse response = mapToChatMessageResponse(saved);

        // Push realtime event qua WebSocket
        pushRealtimeChatMessage(cht, sender, response);

        return response;
    }

    /**
     * Đánh dấu các tin nhắn trong cuộc hội thoại là đã đọc
     */
    @Transactional
    public void markConversationAsRead(Integer conversationId) {
        CuocHoiThoai cht = cuocHoiThoaiRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hội thoại ID: " + conversationId));

        validateConversationAccess(cht);

        NguoiDung currentUser = getCurrentAuthenticatedUser();
        List<TinNhan> unreadMessages = tinNhanRepository
                .findByCuocHoiThoaiMaCuocHoiThoaiAndDaDocFalseAndNguoiGuiMaNguoiDungNot(conversationId, currentUser.getMaNguoiDung());

        for (TinNhan tn : unreadMessages) {
            tn.setDaDoc(true);
        }
        tinNhanRepository.saveAll(unreadMessages);
    }

    // --- Helpers & Security ---

    private void validateConversationAccess(CuocHoiThoai cht) {
        if (isSystemAdmin()) {
            return;
        }

        NguoiDung currentUser = getCurrentAuthenticatedUser();

        if (isCustomer()) {
            if (cht.getKhachHang() == null || cht.getKhachHang().getNguoiDung() == null ||
                    !cht.getKhachHang().getNguoiDung().getMaNguoiDung().equals(currentUser.getMaNguoiDung())) {
                throw new AccessDeniedException("Forbidden: Bạn không có quyền truy cập cuộc hội thoại của khách hàng khác");
            }
            return;
        }

        // Branch Staff: Check assigned staff or matching branch
        if (cht.getNhanVien() != null && cht.getNhanVien().getNguoiDung() != null &&
                cht.getNhanVien().getNguoiDung().getMaNguoiDung().equals(currentUser.getMaNguoiDung())) {
            return; // Assigned staff
        }

        if (cht.getPhieuTiepNhan() != null && cht.getPhieuTiepNhan().getChiNhanh() != null) {
            Integer branchId = cht.getPhieuTiepNhan().getChiNhanh().getMaChiNhanh();
            if (branchAuthorizationService.isAllowedBranch(branchId)) {
                return;
            }
        }

        // Also check if assigned staff's branch matches
        if (cht.getNhanVien() != null && cht.getNhanVien().getChiNhanh() != null) {
            Integer branchId = cht.getNhanVien().getChiNhanh().getMaChiNhanh();
            if (branchAuthorizationService.isAllowedBranch(branchId)) {
                return;
            }
        }

        throw new AccessDeniedException("Forbidden: Bạn không có quyền truy cập cuộc hội thoại này");
    }

    private void pushRealtimeChatMessage(CuocHoiThoai cht, NguoiDung sender, ChatMessageResponse messageRes) {
        try {
            RealtimeEvent event = RealtimeEvent.of(
                    "CHAT_MESSAGE",
                    "CUOC_HOI_THOAI",
                    cht.getMaCuocHoiThoai(),
                    "Tin nhắn mới từ " + sender.getHoTen(),
                    messageRes
            );

            // Gửi đến Customer nếu sender không phải là customer
            if (cht.getKhachHang() != null && cht.getKhachHang().getNguoiDung() != null) {
                String custUsername = cht.getKhachHang().getNguoiDung().getTenDangNhap();
                if (!custUsername.equalsIgnoreCase(sender.getTenDangNhap())) {
                    webSocketEventPublisher.sendToUser(custUsername, "/queue/chat", event);
                }
            }

            // Gửi đến NhanVien nếu có và sender không phải là nhân viên đó
            if (cht.getNhanVien() != null && cht.getNhanVien().getNguoiDung() != null) {
                String staffUsername = cht.getNhanVien().getNguoiDung().getTenDangNhap();
                if (!staffUsername.equalsIgnoreCase(sender.getTenDangNhap())) {
                    webSocketEventPublisher.sendToUser(staffUsername, "/queue/chat", event);
                }
            }

            // Gửi đến topic chi nhánh nếu có gắn tiếp nhận
            if (cht.getPhieuTiepNhan() != null && cht.getPhieuTiepNhan().getChiNhanh() != null) {
                String branchCode = cht.getPhieuTiepNhan().getChiNhanh().getMaChiNhanhCode();
                if (branchCode != null) {
                    webSocketEventPublisher.sendToBranch(branchCode, event);
                }
            }
        } catch (Exception ignored) {
            // Offline safe: WebSocket failure does not rollback DB message
        }
    }

    private boolean isCustomer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
    }

    private boolean isSystemAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private NguoiDung getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Forbidden: Yêu cầu đăng nhập");
        }
        return nguoiDungRepository.findByTenDangNhapOrEmail(auth.getName(), auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Forbidden: Không tìm thấy thông tin tài khoản người dùng"));
    }

    private ConversationResponse mapToConversationResponse(CuocHoiThoai c, Integer currentUserId) {
        long unread = tinNhanRepository.countByCuocHoiThoaiMaCuocHoiThoaiAndDaDocFalseAndNguoiGuiMaNguoiDungNot(
                c.getMaCuocHoiThoai(), currentUserId
        );

        List<TinNhan> msgs = tinNhanRepository.findByCuocHoiThoaiMaCuocHoiThoaiOrderByThoiGianGuiAsc(c.getMaCuocHoiThoai());
        ChatMessageResponse lastMsg = msgs.isEmpty() ? null : mapToChatMessageResponse(msgs.get(msgs.size() - 1));

        String bienSo = null;
        if (c.getPhieuTiepNhan() != null && c.getPhieuTiepNhan().getXe() != null) {
            bienSo = c.getPhieuTiepNhan().getXe().getBienSo();
        }

        return new ConversationResponse(
                c.getMaCuocHoiThoai(),
                c.getKhachHang() != null ? c.getKhachHang().getMaKhachHang() : null,
                c.getKhachHang() != null && c.getKhachHang().getNguoiDung() != null ? c.getKhachHang().getNguoiDung().getHoTen() : null,
                c.getNhanVien() != null ? c.getNhanVien().getMaNhanVien() : null,
                c.getNhanVien() != null && c.getNhanVien().getNguoiDung() != null ? c.getNhanVien().getNguoiDung().getHoTen() : null,
                c.getPhieuTiepNhan() != null ? c.getPhieuTiepNhan().getMaTiepNhan() : null,
                bienSo,
                c.getTrangThai(),
                c.getNgayTao(),
                c.getNgayCapNhatCuoi(),
                unread,
                lastMsg
        );
    }

    private ChatMessageResponse mapToChatMessageResponse(TinNhan tn) {
        return new ChatMessageResponse(
                tn.getMaTinNhan(),
                tn.getCuocHoiThoai() != null ? tn.getCuocHoiThoai().getMaCuocHoiThoai() : null,
                tn.getNguoiGui() != null ? tn.getNguoiGui().getMaNguoiDung() : null,
                tn.getNguoiGui() != null ? tn.getNguoiGui().getTenDangNhap() : null,
                tn.getNguoiGui() != null ? tn.getNguoiGui().getHoTen() : null,
                tn.getNoiDung(),
                tn.getDuongDanTep(),
                tn.getDaDoc(),
                tn.getThoiGianGui()
        );
    }
}
