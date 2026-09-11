# Luồng nghiệp vụ khảo sát Garage Management

Tài liệu này tóm tắt định hướng nghiệp vụ từ `01_DeCuong_NghiepVu.docx` trong workspace dự án. Dùng để bám luồng thực tế khi phân tích, thiết kế UI/API hoặc lên kế hoạch tính năng. Source code và `database/GarageSystemDB.sql` vẫn là bằng chứng kỹ thuật hiện hành; nếu đề cương mô tả nghiệp vụ chưa có trong source, xem đó là backlog hoặc yêu cầu cần xác nhận trước khi triển khai.

## 1. Phạm vi hệ thống

- Web dành cho `ADMIN`, quản lý chi nhánh và nhân viên tiếp nhận.
- App/mobile dành cho kỹ thuật viên và khách hàng.
- Hệ thống phục vụ garage ô tô đa chi nhánh với luồng chính: khách đặt lịch -> tiếp nhận xe -> tạo phiếu sửa chữa -> phân công kỹ thuật viên -> sửa chữa/cập nhật tiến độ -> báo giá phát sinh nếu có -> hóa đơn/thanh toán -> bàn giao xe -> đánh giá/lịch sử bảo dưỡng.

## 2. Vai trò và trách nhiệm nghiệp vụ

### Admin

- Quản lý thông tin toàn hệ thống: chi nhánh, tài khoản người dùng, phân quyền.
- Quản lý danh mục dịch vụ tổng và bảng giá tham khảo chung.
- Xem báo cáo toàn hệ thống: số lịch hẹn, số phiếu sửa chữa, tổng doanh thu và doanh thu theo chi nhánh.

### Quản lý chi nhánh

- Xem các phiếu sửa chữa đang chờ xử lý trong chi nhánh phụ trách.
- Phân công phiếu sửa chữa cho kỹ thuật viên theo loại dịch vụ, tình trạng xe và khối lượng công việc hiện tại.
- Quản lý kho phụ tùng cơ bản và cấu hình giá dịch vụ riêng theo chi nhánh nếu có.
- Xem báo cáo trong phạm vi chi nhánh: xe đang sửa/đã hoàn tất, doanh thu ngày/tháng, hiệu suất kỹ thuật viên và đánh giá dịch vụ từ khách hàng.

### Nhân viên tiếp nhận

- Trực chat realtime để tư vấn tình trạng xe và giải đáp thắc mắc cho khách hàng.
- Quản lý lịch hẹn mới từ app hoặc chatbot nháp; trao đổi lại nếu cần đổi giờ; xác nhận hoặc từ chối lịch hẹn, kèm lý do khi từ chối.
- Nếu khách không muốn tạo tài khoản hoặc đến trực tiếp không có lịch hẹn, nhân viên tiếp nhận được tạo hồ sơ khách vãng lai từ giao diện nội bộ. Hệ thống ưu tiên tra cứu theo biển số xe; nếu chưa có xe thì tạo hồ sơ kỹ thuật riêng với tên hiển thị `Khách vãng lai - <biển số>`, không tạo một tài khoản `Khách lẻ` dùng chung cho nhiều người.
- Tiếp nhận xe thực tế khi khách đến: đối chiếu xe, lập phiếu tiếp nhận, ghi biển số, số km, tình trạng vỏ ngoài, lỗi khách phản ánh, dịch vụ yêu cầu và hình ảnh hiện trạng.
- Sau khi lập phiếu tiếp nhận, nghiệp vụ mong muốn là hệ thống tự tạo phiếu sửa chữa ở trạng thái chờ phân công. Nếu source hiện tại vẫn tách thao tác tạo phiếu sửa chữa, không tự ý đổi flow khi chưa xác nhận.
- Khi sửa chữa hoàn tất, kiểm tra phiếu sửa chữa, phụ tùng đã thay và báo giá phát sinh đã duyệt để tạo hóa đơn.
- Cập nhật thanh toán tiền mặt tại quầy, bàn giao xe, hướng dẫn lưu ý và cập nhật trạng thái sang đã bàn giao nếu hệ thống có trạng thái tương ứng.

### Kỹ thuật viên

- Không có luồng tự đăng ký tài khoản cho kỹ thuật viên. Tài khoản kỹ thuật viên phải do quản lý chi nhánh tạo/cấp trên web, gắn đúng hồ sơ `NhanVien`, vai trò `ROLE_TECHNICIAN` và chi nhánh thuộc phạm vi quản lý; sau đó kỹ thuật viên mới dùng tài khoản này để đăng nhập vào giao diện kỹ thuật.
- Nhận thông báo và xem danh sách phiếu sửa chữa được quản lý chi nhánh phân công.
- Xem chi tiết khách hàng, xe, ghi chú tiếp nhận và hình ảnh hiện trạng ban đầu.
- Cập nhật tiến độ thực tế: đang kiểm tra, đang sửa chữa, hoàn tất sửa chữa.
- Ghi nhận kết quả kiểm tra, nguyên nhân lỗi, hình ảnh lỗi xe và phụ tùng thay thế đã dùng từ kho.
- Tạo báo giá phát sinh trên app cho lỗi ngoài yêu cầu ban đầu, nêu hạng mục, lý do, chi phí phụ tùng/tiền công và hình ảnh minh chứng; chờ khách hàng duyệt trước khi sửa.

### Khách hàng

- Có thể mở giao diện đăng ký công khai trước khi đăng nhập. Mọi tài khoản được tạo từ luồng đăng ký công khai luôn là khách hàng; client không được gửi hoặc tự chọn vai trò.
- Đăng ký/đăng nhập, thêm và quản lý xe cá nhân: biển số, hãng, dòng xe, năm sản xuất, số km.
- Xem thông tin garage: chi nhánh, dịch vụ, bảng giá và đánh giá.
- Dùng AI chatbot để tư vấn lỗi tự động và tạo lịch hẹn nháp; chat realtime với nhân viên tiếp nhận khi cần hỗ trợ sâu.
- Đặt lịch sửa chữa/bảo dưỡng bằng cách chọn xe, chi nhánh, dịch vụ, ngày giờ và mô tả tình trạng lỗi.
- Theo dõi tiến độ sửa xe realtime, nhận thông báo, xem và xác nhận hoặc từ chối báo giá phát sinh.
- Xem hóa đơn chi tiết trên app, thanh toán online qua cổng thanh toán sandbox nếu được triển khai.
- Xem lịch sử bảo dưỡng của xe và gửi đánh giá sao/nhận xét sau khi nhận lại xe.

## 3. Luồng nghiệp vụ chuẩn

1. Khách hàng có thể tạo tài khoản và khai báo xe trên mobile; nếu không muốn tạo tài khoản hoặc đến trực tiếp, nhân viên tiếp nhận tạo hồ sơ khách vãng lai dựa trên biển số xe.
2. Khách hàng xem chi nhánh/dịch vụ/bảng giá, có thể hỏi chatbot hoặc chat nhân viên tiếp nhận.
3. Khách hàng gửi yêu cầu đặt lịch gồm xe, chi nhánh, dịch vụ, thời gian và mô tả lỗi.
4. Nhân viên tiếp nhận kiểm tra lịch hẹn, trao đổi lại nếu cần, sau đó xác nhận hoặc từ chối kèm lý do.
5. Khi khách đến garage, nhân viên tiếp nhận đối chiếu xe và lập phiếu tiếp nhận với số km, tình trạng ban đầu, lỗi phản ánh, dịch vụ yêu cầu và hình ảnh nếu có.
6. Hệ thống/nhân viên tạo phiếu sửa chữa ở trạng thái chờ phân công.
7. Quản lý chi nhánh phân công kỹ thuật viên phù hợp.
8. Kỹ thuật viên kiểm tra xe, cập nhật tiến độ, ghi nhận kết quả, hình ảnh và phụ tùng sử dụng.
9. Nếu phát sinh hạng mục ngoài yêu cầu ban đầu, kỹ thuật viên tạo báo giá phát sinh và chờ khách hàng duyệt.
10. Khách hàng duyệt hoặc từ chối báo giá phát sinh.
11. Sau khi sửa chữa hoàn tất, nhân viên tiếp nhận/thu ngân tạo hóa đơn từ dịch vụ, phụ tùng và báo giá đã duyệt.
12. Khách hàng thanh toán tại quầy hoặc online nếu cổng thanh toán được triển khai.
13. Nhân viên bàn giao xe, cập nhật trạng thái hoàn tất/bàn giao.
14. Khách hàng xem lịch sử bảo dưỡng và gửi đánh giá dịch vụ.

## 4. Điểm cần đối chiếu khi làm tính năng

- Lịch hẹn trong đề cương có bước xác nhận/từ chối, nhưng API hiện tại mới chắc chắn có tạo, xem và hủy. Không thêm xác nhận/từ chối nếu backend chưa có contract hoặc người dùng chưa yêu cầu mở rộng.
- Tiếp nhận xe trong đề cương có hình ảnh hiện trạng và tự động tạo phiếu sửa chữa. Source hiện tại cần được kiểm tra trước khi giả định đã hỗ trợ ảnh hoặc auto-create repair order.
- Phiếu sửa chữa trong đề cương có trạng thái đang kiểm tra và đã bàn giao xe. Source hiện tại dùng tập trạng thái riêng; không đổi enum/status nếu chưa kiểm tra backend/database.
- Báo giá phát sinh trong đề cương có ảnh minh chứng và tách chi phí phụ tùng/tiền công; source hiện tại đã có báo giá dịch vụ/phụ tùng nhưng cần kiểm tra có hỗ trợ ảnh hay không.
- Thanh toán online sandbox, chatbot AI, đánh giá dịch vụ và lịch sử bảo dưỡng là định hướng nghiệp vụ quan trọng nhưng phải xem là backlog nếu source chưa có module tương ứng.
- Khi xây UI, nên hiển thị luồng theo đời thực: đặt lịch -> tiếp nhận -> sửa chữa -> báo giá phát sinh -> hóa đơn -> bàn giao -> đánh giá, nhưng chỉ bật thao tác khi backend đã có endpoint an toàn.

## 5. Luồng tạo và cấp tài khoản đã chốt

### 5.1. Khách hàng tự đăng ký

- Giao diện tham chiếu nằm tại `D:\KLCN\stitch_stitch_garage_design_system\Giao diện đăng ký` và xuất hiện trước màn hình đăng nhập. Form hiện có họ tên, số điện thoại, email không bắt buộc, mật khẩu và xác nhận điều khoản/chính sách bảo mật.
- Endpoint đăng ký là endpoint công khai nhưng backend là authority: luôn gán duy nhất `ROLE_CUSTOMER`, không nhận `roles`, `branchId`, loại tài khoản hoặc quyền hạn từ request của client.
- Một lần đăng ký thành công phải tạo đồng bộ bản ghi `NguoiDung`, liên kết `NguoiDung_VaiTro` với `ROLE_CUSTOMER` và hồ sơ `KhachHang`. Các thao tác phải nằm trong cùng transaction để không tạo tài khoản thiếu vai trò hoặc thiếu hồ sơ khách hàng.
- Mật khẩu thô chỉ được dùng làm đầu vào cho `PasswordEncoder`; phải BCrypt hash tại backend trước khi lưu vào `NguoiDung.MatKhauHash`. Không lưu, log hoặc trả mật khẩu thô/hash trong response.
- Contract đã triển khai dùng số điện thoại hợp lệ làm `NguoiDung.TenDangNhap`; email là tùy chọn. Backend kiểm tra trùng số điện thoại/tên đăng nhập và email, còn mobile điền sẵn số điện thoại khi chuyển sang màn hình đăng nhập.
- Source ngày 2026-08-31 đã có `POST /api/auth/register` và màn hình Flutter `/register` trước đăng nhập. Endpoint tạo `NguoiDung`, `NguoiDung_VaiTro` và `KhachHang` trong cùng transaction, gán cố định `ROLE_CUSTOMER` và BCrypt hash mật khẩu.

### 5.2. Kỹ thuật viên được quản lý cấp tài khoản

- Kỹ thuật viên không được dùng endpoint tự đăng ký và không có lựa chọn tạo tài khoản kỹ thuật viên ở giao diện công khai.
- Quản lý chi nhánh tạo/cấp tài khoản trên web, gắn tài khoản với hồ sơ `NhanVien`, gán `ROLE_TECHNICIAN` và chi nhánh thuộc phạm vi quản lý. Backend phải tự kiểm tra quyền quản lý và branch isolation; không tin `role` hoặc `branchId` do client gửi ngoài phạm vi được phép.
- Sau khi được cấp tài khoản hợp lệ và đang hoạt động, kỹ thuật viên mới đăng nhập vào ứng dụng/giao diện kỹ thuật để xem công việc được phân công.
- Mật khẩu tài khoản do quản lý tạo cũng phải được BCrypt hash tại backend trước khi lưu. Cách cấp mật khẩu ban đầu và yêu cầu đổi mật khẩu lần đầu chưa được người dùng chốt; cần xác nhận khi triển khai.
- Source hiện tại đã có luồng quản trị tạo `NguoiDung` dùng `PasswordEncoder`, nhưng quyền tạo tài khoản người dùng đang được tài liệu ghi nhận ở module quản trị hệ thống. Khi triển khai yêu cầu quản lý chi nhánh cấp tài khoản kỹ thuật viên, phải đối chiếu và mở rộng API/RBAC có kiểm soát thay vì chỉ mở quyền endpoint quản trị hiện hữu.

### 5.3. Khách vãng lai không tạo tài khoản

- Khách có quyền từ chối tạo tài khoản mobile. Trường hợp này do nhân viên tiếp nhận xử lý trên web nội bộ; khách không nhận thông tin đăng nhập và không dùng các chức năng mobile yêu cầu xác thực.
- Nhân viên nhập tối thiểu biển số xe và chi nhánh tiếp nhận; họ tên thật, số điện thoại, hãng/model, số km và yêu cầu sửa chữa được bổ sung nếu khách đồng ý cung cấp. Không tự tạo email hoặc số điện thoại giả; các trường không bắt buộc để `NULL` hoặc giá trị mặc định đúng schema.
- Backend chuẩn hóa và tra cứu biển số trước khi tạo. Nếu xe đã tồn tại, nhân viên phải đối chiếu thông tin khách/xe trước khi dùng lại; không tự động gộp hồ sơ hoặc hiển thị dữ liệu cá nhân chỉ vì trùng biển số. Nếu chưa tồn tại, tạo hồ sơ khách vãng lai và xe mới trong cùng transaction.
- Schema hiện tại bắt buộc `KhachHang.MaNguoiDung`, `Xe.MaKhachHang` và `NguoiDung` có tên đăng nhập/mật khẩu. Khi chưa thay đổi schema, phương án tương thích là backend tạo một `NguoiDung` kỹ thuật riêng cho từng hồ sơ vãng lai: tên đăng nhập duy nhất do server sinh (ví dụ từ biển số kèm hậu tố), mật khẩu ngẫu nhiên được BCrypt hash, `ROLE_CUSTOMER` để giữ quan hệ dữ liệu, và `TrangThai = false` để không thể đăng nhập. Tên hiển thị mặc định là `Khách vãng lai - <biển số>`.
- Tuyệt đối không dùng một bản ghi/tài khoản `Khách lẻ` chung cho mọi xe vì sẽ trộn lịch sử sửa chữa, hóa đơn, báo giá và làm sai ownership. Mỗi xe/hồ sơ vãng lai phải truy vết độc lập; biển số là khóa tra cứu nghiệp vụ chứ không phải bằng chứng duy nhất về chủ sở hữu.
- Lịch hẹn hoặc phiếu tiếp nhận của khách vãng lai do nhân viên tạo trong phạm vi chi nhánh của mình. Backend phải lấy nhân viên và chi nhánh từ phiên đăng nhập, kiểm tra RBAC/branch isolation và không tin `branchId`, owner hoặc role do client tự khai báo ngoài phạm vi cho phép.
- Vì khách vãng lai không đăng nhập mobile, thông báo, duyệt báo giá và xác nhận thanh toán/bàn giao phải có luồng hỗ trợ tại quầy hoặc qua kênh liên hệ khách đã cung cấp, đồng thời lưu người thao tác, thời điểm và nội dung xác nhận để audit.
- Nếu khách muốn đăng ký sau này, cần luồng chuyển đổi hồ sơ vãng lai thành tài khoản hoạt động sau khi xác minh quyền sở hữu/số điện thoại; phải giữ nguyên xe, lịch sử sửa chữa và hóa đơn, không tạo hồ sơ trùng.
- Trạng thái source ngày 2026-09-09: nghiệp vụ này mới được chốt ở mức yêu cầu. Backend hiện chỉ cho `ADMIN`/`CUSTOMER` tạo lịch hẹn, check-in hiện cần `appointmentId`, và chưa có orchestration API tạo khách vãng lai + xe + lịch/phiếu tiếp nhận. Không triển khai chỉ bằng cách mở quyền controller hiện hữu; cần thiết kế DTO/service transaction và test RBAC, branch isolation, trùng biển số, rollback và ownership trước.
