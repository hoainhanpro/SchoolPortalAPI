# 🏫 SchoolPortalAPI - Hệ thống Quản lý Trường Học

## 📝 Giới thiệu
SchoolPortalAPI là một hệ thống quản lý trường học toàn diện được phát triển cho Trường Đại học PTIT HCM. Hệ thống cung cấp các chức năng quản lý thông tin sinh viên, giảng viên, đăng ký tín chỉ, lịch học, điểm số và học phí.

## 🛠️ Công nghệ sử dụng

### Backend
- Java 21
- Spring Boot 3.4.5
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT Authentication
- Maven

### Frontend
- SvelteKit 
- Bootstrap 5
- TypeScript
- Vite

## 🏗️ Cấu trúc dự án

```
SchoolPortalAPI
  ├── frontend/           # Ứng dụng SvelteKit frontend
  │   ├── src/
  │   │   ├── lib/        # Components và utilities
  │   │   └── routes/     # Các trang và routes
  │   └── static/         # Static assets
  │
  ├── portal/             # Ứng dụng Spring Boot backend
  │   └── src/main/java/com/ptithcm/portal/
  │       ├── config/     # Cấu hình ứng dụng 
  │       ├── controller/ # API endpoints
  │       ├── dto/        # Data Transfer Objects
  │       ├── entity/     # JPA entities
  │       ├── repository/ # JPA repositories
  │       └── service/    # Business logic
  │
  └── scripts/            # Scripts hỗ trợ
```

## 🔍 Các tính năng chính

### Quản lý người dùng
- Quản lý thông tin sinh viên
- Quản lý thông tin giảng viên
- Quản lý thông tin nhân viên
- Hệ thống xác thực và phân quyền

### Quản lý đào tạo
- Quản lý khoa
- Quản lý lớp
- Quản lý môn học
- Quản lý lớp tín chỉ
- Quản lý đợt đăng ký học
- Quản lý lịch học
- Quản lý phòng học
- Quản lý điểm số

### Quản lý tài chính
- Quản lý học phí
- Quản lý đợt đóng học phí

### Thông báo và liên lạc
- Hệ thống thông báo
- Tin nhắn giữa các người dùng

## ⚙️ Cài đặt và chạy dự án

### Yêu cầu
- Java Development Kit (JDK) 21
- Maven
- Node.js và npm
- PostgreSQL

### Cài đặt và chạy Backend

1. Clone repository về máy:
```
git clone https://github.com/hoainhanpro/SchoolPortalAPI.git
cd SchoolPortalAPI
```

2. Cấu hình database trong file `portal/src/main/resources/application.properties`

3. Build và chạy backend với Maven:
```
cd portal
mvn clean install
mvn spring-boot:run
```

Backend sẽ chạy ở port 1234 (có thể thay đổi trong application.properties).

### Cài đặt và chạy Frontend

1. Di chuyển đến thư mục frontend:
```
cd frontend
```

2. Cài đặt các dependencies:
```
npm install
```

3. Chạy ứng dụng trong development mode:
```
npm run dev
```

4. Build ứng dụng cho production:
```
npm run build
```

## 🧪 Testing

Chạy tests backend:
```
cd portal
mvn test
```

## 👥 Các vai trò người dùng

1. **Admin**
   - Quản lý toàn bộ hệ thống
   - Quản lý người dùng và quyền truy cập
   - Cấu hình các thông số hệ thống

2. **Giảng viên**
   - Quản lý lớp học
   - Quản lý điểm số
   - Xem lịch giảng dạy
   - Quản lý hồ sơ cá nhân

3. **Sinh viên**
   - Đăng ký tín chỉ
   - Xem điểm và kết quả học tập
   - Xem và thanh toán học phí
   - Xem lịch học
   - Quản lý hồ sơ cá nhân

## 📊 Cấu trúc dữ liệu

### Các entity chính:
- SinhVien (Sinh viên)
- NhanVien (Nhân viên)
- Khoa (Khoa)
- Lop (Lớp)
- Mon (Môn học)
- LopTinChi (Lớp tín chỉ)
- DotDangKy (Đợt đăng ký học)
- DangKy (Đăng ký học)
- HocKy (Học kỳ)
- HocPhi (Học phí)
- DotDongHocPhi (Đợt đóng học phí)
- ThongBao (Thông báo)
- PhongHoc (Phòng học)
- Lich (Lịch học)
