
<p align="center">
  <a href="https://www.uit.edu.vn/" title="Trường Đại học Công nghệ Thông tin" style="border: none;">
    <img src="https://i.imgur.com/WmMnSRt.png" alt="Trường Đại học Công nghệ Thông tin | University of Information Technology">
  </a>
</p>

<h1 align="center">Ứng dụng quản lý công việc FocusFlow</h1>
<h3 align="center">Đồ án Nhập môn ứng dụng di động SE114</h3>
<h3 align="center">Giảng viên phụ trách: Nguyễn Tấn Toàn SE114</h3>

## Members:

| STT | MSSV      | Họ và Tên            | Chức vụ     | Email                    |
|-----|-----------|----------------------|-------------|--------------------------|
| 1   | 23521834  | Nguyễn Thúy Vy       | Nhóm trưởng | 23521834@gm.uit.edu.vn   |
| 2   | 23520340  | Tôn Kim Dung         | Thành viên  | 23520340@gm.uit.edu.vn   |
| 3   | 23521826  | Đỗ Lê Khánh Vy       | Thành viên  | 23521826@gm.uit.edu.vn   |

---

# 📱 FocusFlow - Ứng dụng quản lý công việc cá nhân & nhóm

**FocusFlow** là ứng dụng di động hỗ trợ người dùng tổ chức công việc hiệu quả bằng cách kết hợp các tính năng quản lý task, nhóm, phương pháp Pomodoro, và AI đề xuất thông minh.

---

## 🚀 Tính năng chính

### ✅ Quản lý công việc cá nhân và nhóm
- Tạo, sửa, xóa công việc dễ dàng.
- Phân loại công việc theo nhóm, cá nhân.
- Giao việc cho thành viên trong nhóm.
- Phân quyền thao tác task theo vai trò (trưởng nhóm/thành viên).

### 🍅 Tích hợp Pomodoro
- Quản lý thời gian tập trung theo phương pháp Pomodoro.
- Ghi lại lịch sử và hiệu suất làm việc để tính toán độ tập trung qua các biểu đồ.

### 🔥 Theo dõi streak
- Ghi nhận chuỗi ngày hoàn thành task liên tục.
- Hiển thị lịch sử streak bằng lịch tương tác.

### 🔔 Gửi thông báo nhắc việc
- Tạo thông báo nhắc nhở qua hệ thống `AlarmManager`.
- Hỗ trợ thông báo task sắp đến hạn.
- Có thể tích hợp với WebSocket để nhận thông báo task nhóm mới.

### 👥 Quản lý nhóm làm việc
- Tạo nhóm, mời thành viên tham gia.
- Thảo luận và cộng tác hiệu quả qua task nhóm.
- Tìm kiếm task trong nhóm, đánh dấu hoàn thành theo phân quyền.

### 🧠 AI đề xuất công việc (dành cho tài khoản Pro)
- Người dùng Pro có thể **chat với AI** để được gợi ý:
  - Cách sắp xếp thời gian hợp lý.
  - Phân bổ Pomodoro phù hợp.
  - Tăng động lực và hiệu suất học tập/làm việc.
- Người dùng thường có thể dùng 5 câu chat miễn phí.

### 💼 Hệ thống tài khoản & nâng cấp Pro
- Đăng ký, đăng nhập, xác thực email Google.
- Tài khoản thường: sử dụng các tính năng cơ bản.
- Tài khoản Pro: mở khoá AI đề xuất, tùy chỉnh nâng cao các dữ liệu phân tích.
- Hỗ trợ nạp tiền để nâng cấp tài khoản.

### 🌐 Realtime với WebSocket
- Tích hợp WebSocket để cập nhật **realtime task nhóm** (thêm, sửa, xóa).
- Các thành viên trong nhóm sẽ nhận được cập nhật ngay khi có thay đổi.

---

## ⚙️ Công nghệ sử dụng

| Thành phần | Công nghệ |
|-----------|-----------|
| Frontend (Mobile) | Android (Java) |
| Backend | Spring Boot (Java) |
| Cơ sở dữ liệu | MySQL (Web Server Railway) |
| Realtime (nhóm) | WebSocket (STOMP protocol) + thông báo push nội bộ |
| Thông báo | AlarmManager |
| Thanh toán | ZaloPay |
| AI | Tích hợp mô hình OpenRouter AI |

---

## 🛠️ Cài đặt & chạy ứng dụng

1. Mở project
- Clone project:
```bash
git clone https://github.com/thvy815/FocusFlow-Frontend.git
git clone https://github.com/thvy815/FocusFlow-Backend.git
```

2. Backend (Spring Boot)
```bash
cd FocusFlow-Backend
./mvnw spring-boot:run
```
Mặc định server sẽ chạy tại http://localhost:8080

3. Frontend (Android App)
Mở project FocusFlow-Frontend bằng Android Studio.
Vào file ApiClient.java và chỉnh địa chỉ API như sau:

```java
public static final String BASE_URL = "http://10.0.2.2:8080/";
```

Nếu chạy trên thiết bị thật, hãy thay 10.0.2.2 bằng địa chỉ IP nội bộ của máy backend (ví dụ: 192.168.1.5).
Kết nối thiết bị hoặc mở AVD → Nhấn Run để chạy ứng dụng.
