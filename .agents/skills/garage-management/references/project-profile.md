# Hồ sơ skill Garage Management

Đây là bản điền form được suy ra từ repository tại `D:\Khóa Luận Cử Nhân\Garage_Management`. Khi source hoặc tài liệu thay đổi, ưu tiên bằng chứng hiện tại trong repository và cập nhật hồ sơ này nếu khác biệt ảnh hưởng quyết định.

## 1. Thông tin cơ bản

- Tên skill: `garage-management`
- Tên hiển thị: `Trợ lý đồ án Garage`
- Mục đích: giúp Codex phân tích, sửa, kiểm tra, build và duy trì đồ án quản lý garage ô tô đa chi nhánh trên web, mobile, backend và database.
- Kích hoạt: khi gọi `$garage-management`; khi yêu cầu phù hợp trong đúng workspace Garage Management; khi yêu cầu sửa lỗi, phát triển tính năng, review, test, build hoặc cập nhật tài liệu của đồ án.
- Không kích hoạt: dự án garage khác; câu hỏi Java/React/Flutter/SQL chung không gắn với repository; thiết kế độc lập không dùng source dự án; thao tác hệ thống ngoài phạm vi đồ án.

## 2. Phạm vi dự án

- Workspace: `D:\Khóa Luận Cử Nhân\Garage_Management`
- Dấu hiệu nhận biết đồng thời: `backend/pom.xml`, `frontend/package.json`, `mobile/pubspec.yaml`, `database/GarageSystemDB.sql`, `docs/PROJECT_CONTEXT.md`.
- Website: `frontend/`, React 18, TypeScript, Vite 5, React Router 6, Axios.
- App: `mobile/`, Flutter/Dart; hiện mới có nền tảng cơ bản và `mobile/lib/main.dart`.
- Backend/API: `backend/`, Java 17, Spring Boot 3.2.5, Maven, Spring Web/Data JPA/Security/Validation/WebSocket, JWT.
- Database: Microsoft SQL Server; DDL ở `database/GarageSystemDB.sql`, seed development ở `database/seed/`.
- Nguồn nghiệp vụ khảo sát: `01_DeCuong_NghiepVu.docx`, đã tóm tắt tại `references/business-workflow.md`. Dùng để bám quy trình thực tế; nếu khác source hiện hành thì coi là backlog hoặc điểm cần xác nhận.
- Toolchain: Docker Compose, Dockerfiles, npm, Maven, Flutter; local đã được tài liệu xác minh trên Windows 11. Docker dùng SQL Server 2022, Java 17 và Node 20; local README ghi Node 24/npm 11.
- Thành phần khác: `AUTO-GARAGE-UI-SUGGESTION/` chứa mockup và design reference, không phải source production.

## 3. Tài liệu cần đọc

- Bắt buộc trước khi sửa/review: `README.md`, `docs/PROJECT_CONTEXT.md`, rồi các file source/test liên quan trực tiếp.
- Nghiệp vụ/luồng chức năng: `references/business-workflow.md`, đặc biệt khi làm lịch hẹn, tiếp nhận, sửa chữa, báo giá, thanh toán, bàn giao, đánh giá, chatbot hoặc mobile.
- Tài liệu phục vụ báo cáo khóa luận: `references/thesis-documentation.md`; dùng khi code/sửa/hoàn thiện chức năng mới để tạo ghi chú module ngoài source code.
- Website/UI: `AUTO-GARAGE-UI-SUGGESTION/autocare_multi_branch/DESIGN.md`, `frontend/src/index.css`, router, types và feature liên quan.
- Backend/API: endpoint/security/module tương ứng trong `README.md` và `docs/PROJECT_CONTEXT.md`.
- Database: `database/GarageSystemDB.sql`, entity/repository liên quan và seed tương ứng; chỉ đọc phạm vi cần thiết.
- Docker/môi trường: `docs/DOCKER.md`, `.env.example`, `docker-compose.yml` và Dockerfile liên quan.
- Git: `.gitignore`, `git status`, branch/remote khi tác vụ thực sự liên quan Git.
- Từ khóa tìm kiếm: tên entity tiếng Việt, endpoint `/api/...`, tên DTO/service/repository, role, `@PreAuthorize`, `BranchAuthorizationService`, trạng thái nghiệp vụ, tên bảng/cột.
- Note phiên và ghi chú khóa luận: chỉ đọc/cập nhật khi skill `garage-management` hoặc người dùng yêu cầu ghi nhận phiên/chức năng. `ProgressReport.txt` là nhật ký tiến độ, chỉ đọc nếu người dùng yêu cầu đối chiếu lịch sử.

## 4. Kiến trúc và quy ước source

- Source chính: `backend/src/main`, `frontend/src`, `mobile/lib`, `database/`.
- Toolchain/file sinh tự động hoặc dependency: `backend/target`, `frontend/node_modules`, `frontend/dist`, `mobile/build`, `.dart_tool`, các thư mục platform Flutter sinh sẵn; tránh sửa nếu yêu cầu không trực tiếp liên quan.
- Backend: controller nhận HTTP/validation/RBAC; service giữ business logic, transaction và authorization theo dữ liệu server; repository truy cập JPA; entity ánh xạ schema; DTO định nghĩa request/response; exception dùng handler chung; security và websocket tách riêng.
- Frontend: `app/` chứa provider/router/config; `features/<domain>/` chứa pages/components/services; `components/common` và `components/layout` dùng chung; `lib/` cho API/auth/websocket; `types/` cho contract dùng chung; CSS hệ thống ở `src/index.css`.
- Mobile: theo quy ước Flutter/Dart khi được phát triển thêm; không giả định kiến trúc chưa tồn tại.
- Database/config: DDL và seed trong `database/`; cấu hình Spring trong `backend/src/main/resources/application.properties`; biến môi trường qua `.env.example` và `frontend/.env.example`.
- Naming: Java class/file PascalCase; method/field camelCase; controller/service/repository/DTO dùng hậu tố tương ứng. React component/page PascalCase `.tsx`; service/type/helper dùng tên miền hiện hữu và camelCase; route URL lowercase, kebab-case và dưới `/api` hoặc `/app`; bảng/cột SQL theo PascalCase tiếng Việt hiện hữu; trạng thái/role dạng `UPPER_SNAKE_CASE`.
- Giữ constructor injection, `@Valid`, `@Transactional`, `ApiResponse`, exception miền và pattern test hiện có.
- Không trả JPA entity trực tiếp; không nhúng role/branch/ownership từ client thành authority; không viết API call rải rác ngoài service frontend khi đã có lớp service/endpoints.
- UI giữ design AutoCare, responsive state, loading/error/empty state và role-aware navigation. Tái sử dụng component/CSS token trước khi thêm inline CSS dài mới.

## 5. Quy trình làm việc

- Trước khi sửa: xác nhận workspace; đọc tài liệu bắt buộc; xem `git status`; tìm pattern tương tự; xác định contract và tác động xuyên lớp; phân biệt mockup với source chạy.
- Khi sửa: chỉ thay đổi trong phạm vi; giữ backward compatibility nếu không có yêu cầu đổi contract; bảo toàn RBAC/branch/ownership; không ghi đè thay đổi của người dùng; thêm test phù hợp.
- Sau khi sửa: xem diff; chạy targeted test/lint/analyze/build phù hợp; kiểm tra encoding; cập nhật tài liệu có ý nghĩa; tạo/cập nhật ghi chú khóa luận cho chức năng mới theo `references/thesis-documentation.md`; báo rõ phần chưa chạy.
- Phải giữ nguyên nếu không thuộc yêu cầu: schema, seed, role/status contract, API response shape, port, credentials/config production, mockup tham khảo và thay đổi đang có của người dùng.
- Không tự ý: deploy/upload, commit, push, sửa schema hoặc dữ liệu, reset Docker volume, xóa file, đổi production config, cài/nâng dependency diện rộng. Build/test cục bộ được phép vì không phá hủy; khởi động stack/service chỉ khi cần và không làm mất dữ liệu.

## 6. Kiểm tra và test

- Java/backend: trong `backend/`, ưu tiên `mvn -Dtest=<TestClass> test` cho phạm vi hẹp; chạy `mvn test` khi thay đổi dùng chung, bảo mật, persistence hoặc cần regression đầy đủ. `mvn package` chỉ khi cần xác nhận artifact/build.
- TypeScript/React/CSS: trong `frontend/`, chạy `npm run lint` cho source và `npm run build` để kiểm tra strict TypeScript + Vite. Không có test runner frontend trong `package.json`, nên không tuyên bố đã chạy unit test frontend.
- Dart/Flutter: trong `mobile/`, chạy `flutter analyze` và `flutter test`; chỉ build platform khi yêu cầu hoặc thay đổi cần kiểm chứng tích hợp platform.
- Database: kiểm tra diff SQL, mapping entity/repository và test backend liên quan; không apply schema/seed vào DB thật nếu chưa được phép.
- Docker: dùng `docker compose config` để kiểm tra cấu hình không phá hủy; `docker compose up --build`, `down` hay thao tác volume chỉ khi người dùng yêu cầu hoặc cho phép rõ. Không chạy `down -v` nếu chưa có xác nhận riêng.
- Encoding: giữ UTF-8 không BOM cho file text mới/sửa; rà ký tự thay thế `�` và các mẫu mojibake phổ biến nếu có dấu tiếng Việt.
- Có thể tự động chạy build/test cục bộ phù hợp sau khi sửa. Không tự build image/khởi động Docker hoặc release artifact nếu chưa cần.
- Hoàn thành khi hành vi yêu cầu đã được triển khai, diff đúng phạm vi, checks phù hợp pass, không làm yếu bảo mật/phân quyền, tài liệu cần thiết đồng bộ và mọi phần chưa kiểm tra được nêu rõ.

## 7. Git

- Repository: `https://github.com/maihuy1301/Garage_Management.git`
- Nhánh mặc định hiện tại: `main`.
- Trước commit: kiểm tra status/diff, chạy checks phù hợp, xác nhận không có secrets/artifact/file ngoài phạm vi.
- Không commit: `.env`, `.env.local`, secrets, token/key/cookie, `node_modules`, `dist`, `target`, `.dart_tool`, `build`, log, database `.mdf/.ldf`, file IDE/OS.
- Chỉ commit hoặc push khi người dùng yêu cầu rõ. Không tự suy diễn các cụm từ mơ hồ như “lưu code” thành commit/push; hỏi lại nếu ý nghĩa ảnh hưởng Git remote.

## 8. Deploy/upload

- Development: local Windows hoặc Docker Compose với frontend `3001`, backend `8080`, SQL Server `1433`.
- Test/production: repository chưa mô tả pipeline hoặc đích deploy chính thức; không tự giả định.
- Phương thức hiện có: Docker Compose cho development, không phải bằng chứng về production deployment.
- Chỉ deploy/upload khi người dùng yêu cầu rõ và đã cung cấp/chọn đúng môi trường, đích và credentials. Trước deploy cần checks phù hợp và kế hoạch rollback. Xác nhận bằng health/API/UI/log theo phạm vi; không xóa volume hay rollback dữ liệu khi chưa được phép.

## 9. Bảo mật

- Không hiển thị, ghi vào source/note/log hay commit: mật khẩu, JWT/token, API key, private key, cookie/session, chuỗi kết nối và secret từ file môi trường.
- File nhạy cảm: `.env*` thực tế, `application.properties` nếu chứa override cục bộ, Docker/env config, key/certificate, log. Chỉ dùng file `.example` để mô tả tên biến và dùng placeholder an toàn.
- Nếu phát hiện secret đã vào Git: không lặp lại giá trị; báo file/vị trí ở mức cần thiết; dừng việc phát tán; đề nghị revoke/rotate, xóa khỏi lịch sử bằng quy trình được người dùng phê duyệt, rồi kiểm tra lại.
- Credentials chỉ dùng cho môi trường và thao tác người dùng cho phép; không dùng credential development cho production.
- Giữ JWT stateless, BCrypt, method security, ownership và branch isolation; thêm endpoint phải có quyết định authorization rõ ràng và test 401/403/cross-branch/ownership khi liên quan.

## 10. Ngôn ngữ và mã hóa

- Codex trả lời tiếng Việt, ngắn gọn và rõ ràng.
- Code identifier theo convention tiếng Anh/tiếng Việt đang có; thông báo người dùng và comment có thể dùng tiếng Việt, tránh comment thừa.
- File text dùng UTF-8 không BOM, line ending theo file hiện hữu. Sau sửa nội dung tiếng Việt, rà `�`, `Ã`, `Â`, `áº`, `á»` trong các dòng bị tác động và sửa mojibake nếu do thay đổi hiện tại gây ra.

## 11. Cách trả lời sau khi hoàn thành

- Độ dài: ngắn gọn.
- Báo cáo: kết quả; file đã sửa; test/build/lint/analyze đã chạy và kết quả; tài liệu phiên/ghi chú khóa luận đã tạo hoặc cập nhật; phần chưa kiểm tra; cách dùng hoặc rủi ro còn lại nếu có.
- Không tạo báo cáo riêng ngoài quy trình tài liệu phiên và ghi chú khóa luận. Chỉ cập nhật tài liệu dự án trong source khi thay đổi đòi hỏi.

## 12. Tài nguyên phụ trợ

- `references/`: có, dùng file hồ sơ này vì các ràng buộc dự án cần được tra cứu khi skill chạy.
- `scripts/`: không cần; dùng trực tiếp Maven, npm, Flutter, Docker và công cụ repository.
- `assets/`: không cần; thiết kế/mẫu đã nằm trong `AUTO-GARAGE-UI-SUGGESTION/`.
- Tự động kích hoạt: có khi yêu cầu phù hợp và đúng workspace; cũng có thể gọi rõ `$garage-management`.

## 13. Ví dụ thực tế

Skill phải xử lý tốt:

1. “Dùng `$garage-management` sửa lỗi khách hàng xem được xe của người khác và thêm test.”
2. “Hoàn thiện trang quản lý lịch hẹn theo API hiện có, giữ đúng design system.”
3. “Kiểm tra thay đổi Docker Compose và chạy các kiểm tra an toàn phù hợp.”

Skill phải từ chối hoặc hỏi lại:

1. “Sửa trực tiếp schema production và xóa dữ liệu cũ” khi chưa có phê duyệt/backup/đích cụ thể.
2. “Commit, push và deploy luôn” khi chưa xác định nhánh, môi trường hoặc quyền thao tác cần thiết.
3. Yêu cầu trong một repository khác không có đủ dấu hiệu nhận biết Garage Management.

Các lỗi skill cần ngăn chặn:

1. Tin role/branch/owner từ frontend và làm yếu authorization backend.
2. Sửa `GarageSystemDB.sql`, seed hoặc reset Docker volume ngoài phạm vi.
3. Sửa mockup trong `AUTO-GARAGE-UI-SUGGESTION/` thay vì source React, hoặc quét/ghi note tiến độ không được yêu cầu.
