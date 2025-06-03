<script>
    import { page } from '$app/stores';
    import { onMount } from 'svelte';
    import { goto } from '$app/navigation';
    
    $: currentPath = $page.url.pathname;

    let loading = true;
    let error = null;
    let successMessage = "";
    let paymentMessage = "";
    
    // Thông tin người dùng
    let userInfo = {
        name: "Đang tải...",
        role: "..."
    };
    
    // Danh sách học phí
    let hocPhiList = [];
    // Danh sách sinh viên
    let students = [];
    // Danh sách học kỳ
    let semesters = [];
    
    // Học phí được chọn để xem chi tiết
    let selectedHocPhi = null;
    
    // Hiển thị modal
    let showDetailModal = false;
    
    // Hàm mở modal xem chi tiết
    function openDetailModal(hocPhi) {
        // Tìm thông tin sinh viên và học kỳ từ dữ liệu đã tải
        const student = students.find(s => s.mssv === hocPhi.svId);
        
        // Làm phong phú thông tin học phí
        selectedHocPhi = {
            ...hocPhi,
            sinhVienId: hocPhi.svId,
            hoTenSinhVien: student ? student.hoTen : 'Không rõ',
            tenHocKy: hocPhi.hocKyTen || 'Không rõ',
            namHoc: hocPhi.namHoc || '',
            trangThai: hocPhi.trangThai
        };
        
        showDetailModal = true;
    }
    
    // Fetch danh sách học phí
    async function fetchHocPhi() {
        loading = true;
        error = null;
        
        try {
            // Lấy danh sách sinh viên và học kỳ trước
            await Promise.all([fetchAllStudents(), fetchAllSemesters()]);
            
            const token = localStorage.getItem('jwtToken');
            const response = await fetch('http://localhost:1234/api/hocphi', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (response.status === 403) {
                throw new Error("Bạn không có quyền truy cập trang này");
            }
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const data = await response.json();
            
            // Làm phong phú dữ liệu học phí bằng thông tin từ danh sách sinh viên và học kỳ
            hocPhiList = data.map(hocPhi => {
                // Tìm sinh viên tương ứng
                const student = students.find(s => s.mssv === hocPhi.svId);
                
                return {
                    ...hocPhi,
                    sinhVienId: hocPhi.svId,
                    hoTenSinhVien: student ? student.hoTen : 'Không rõ',
                    tenHocKy: hocPhi.hocKyTen || 'Không rõ',
                    namHoc: hocPhi.namHoc || '',
                };
            });
            
        } catch (err) {
            console.error("Lỗi khi tải danh sách học phí:", err);
            error = err.message;
        } finally {
            loading = false;
        }
    }
    
    // Fetch danh sách sinh viên
    async function fetchAllStudents() {
        try {
            const token = localStorage.getItem('jwtToken');
            const response = await fetch('http://localhost:1234/api/student', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            students = await response.json();
        } catch (error) {
            console.error("Lỗi khi tải danh sách sinh viên:", error);
        }
    }
    
    // Fetch danh sách học kỳ
    async function fetchAllSemesters() {
        try {
            const token = localStorage.getItem('jwtToken');
            const response = await fetch('http://localhost:1234/api/semesters/allHocKy', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            semesters = await response.json();
        } catch (error) {
            console.error("Lỗi khi tải danh sách học kỳ:", error);
        }
    }
    
    // Cập nhật trạng thái thành đã đóng
    async function markAsPaid(mssv, hocKyId) {
        if (confirm("Xác nhận đánh dấu học phí này là đã đóng?")) {
            try {
                const token = localStorage.getItem('jwtToken');
                const response = await fetch('http://localhost:1234/api/hocphi/markAsPaid', {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify({
                        mssv: mssv,
                        hocKyId: hocKyId
                    })
                });
                
                if (response.status === 403) {
                    throw new Error("Bạn không có quyền thực hiện hành động này");
                }
                
                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || response.statusText);
                }
                
                const updatedHocPhi = await response.json();
                
                paymentMessage = "Học phí đã được đánh dấu là đã đóng!";
                setTimeout(() => { paymentMessage = ''; }, 3000);
                
                // Cập nhật lại danh sách học phí
                await fetchHocPhi();
                
                // Nếu modal đang mở và hiển thị chính học phí vừa được cập nhật, cần cập nhật lại thông tin
                if (showDetailModal && selectedHocPhi && selectedHocPhi.sinhVienId === mssv && selectedHocPhi.hocKyId === hocKyId) {
                    // Tìm sinh viên để giữ nguyên thông tin
                    const student = students.find(s => s.mssv === mssv);
                    
                    selectedHocPhi = {
                        ...updatedHocPhi,
                        sinhVienId: mssv,
                        hoTenSinhVien: student ? student.hoTen : selectedHocPhi.hoTenSinhVien,
                        tenHocKy: selectedHocPhi.tenHocKy,
                        namHoc: selectedHocPhi.namHoc,
                        trangThai: "Da dong",
                        daDong: updatedHocPhi.tongTien,
                        ngayDong: updatedHocPhi.ngayDong || new Date().toISOString().split('T')[0]
                    };
                }
                
            } catch (error) {
                console.error("Lỗi khi cập nhật trạng thái học phí:", error);
                alert(`Lỗi khi cập nhật trạng thái học phí: ${error.message}`);
            }
        }
    }
    
    // Format currency
    function formatCurrency(amount) {
        if (!amount && amount !== 0) return "0 ₫";
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
    }
    
    // Format date
    function formatDate(dateString) {
        if (!dateString) return "Chưa đóng";
        const date = new Date(dateString);
        return new Intl.DateTimeFormat('vi-VN').format(date);
    }
    
    // Đăng xuất
    function handleLogout() {
        localStorage.removeItem('jwtToken');
        localStorage.removeItem('user');
        window.location.href = '/';
    }
    
    // Thêm hàm fetchUserInfo
    async function fetchUserInfo(token) {
        try {
            const response = await fetch('http://localhost:1234/api/auth/me', {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (response.ok) {
                const userData = await response.json();
                localStorage.setItem('user', JSON.stringify(userData));
                const userRole = userData.chucVu || userData.chuc_vu;
                
                // Kiểm tra quyền truy cập
                if (userRole !== 'Nhan vien' && userRole !== 'Admin') {
                    alert('Bạn không có quyền truy cập trang này!');
                    goto('/unauthorized');
                    return;
                }
                
                userInfo = {
                    name: `${userData.ho} ${userData.ten}`,
                    role: userRole,
                    id: userData.id
                };
                
                await fetchHocPhi();
            } else {
                console.error("Lỗi tải thông tin người dùng");
                localStorage.removeItem('jwtToken');
                localStorage.removeItem('user');
                goto('/');
            }
        } catch (error) {
            console.error("Lỗi tải thông tin người dùng:", error);
            localStorage.removeItem('jwtToken');
            localStorage.removeItem('user');
            goto('/');
        }
    }
    
    // Cập nhật onMount với xác thực người dùng
    onMount(async () => {
        const token = localStorage.getItem('jwtToken');
        if (!token) {
            goto('/');
            return;
        }
        
        const userDataString = localStorage.getItem('user');
        if (userDataString) {
            try {
                const userData = JSON.parse(userDataString);
                const userRole = userData.chucVu || userData.chuc_vu;

                if (userRole !== 'Nhan vien' && userRole !== 'Admin') {
                    alert('Bạn không có quyền truy cập trang này!');
                    goto('/unauthorized');
                    return;
                }
                
                userInfo = {
                    name: `${userData.ho} ${userData.ten}`,
                    role: userRole,
                    id: userData.id
                };
                
                await fetchHocPhi();
            } catch (error) {
                console.error("Lỗi phân tích dữ liệu người dùng:", error);
                goto('/');
            }
        } else {
            await fetchUserInfo(token);
        }
    });
</script>

<div class="app-container">
    <!-- Sidebar/Navigation -->
    <nav class="sidebar">
        <div class="logo-container">
            <img
                src="/images/logos/ptit-logo-inv.png"
                alt="PTIT Logo"
                class="logo"
            />
            <h2 class="app-name">PTIT Admin</h2>
        </div>

        <div class="nav-links">
          <!-- Mục Khoa -->
          <a href="/admin/dashboard/khoa" class:active={currentPath.includes("/admin/dashboard/khoa")}>
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M8.277.084a.5.5 0 0 0-.554 0l-7.5 5A.5.5 0 0 0 .5 6h1.875v7H1.5a.5.5 0 0 0 0 1h13a.5.5 0 1 0 0-1h-.875V6H15.5a.5.5 0 0 0 .277-.916l-7.5-5zM12.375 6v7h-1.25V6h1.25zm-2.5 0v7h-1.25V6h1.25zm-2.5 0v7h-1.25V6h1.25zm-2.5 0v7h-1.25V6h1.25zM8 4a1 1 0 1 1 0-2 1 1 0 0 1 0 2z"/>
              </svg>
              <span>Khoa</span>
          </a>
          
          <!-- Mục Lớp -->
          <a href="/admin/dashboard/lop" class:active={currentPath.includes("/admin/dashboard/lop")}>
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 16 16">
                    <path d="M14.5 3a.5.5 0 0 1 .5.5v9a.5.5 0 0 1-.5.5h-13a.5.5 0 0 1-.5-.5v-9a.5.5 0 0 1 .5-.5h13zm-13-1A1.5 1.5 0 0 0 0 3.5v9A1.5 1.5 0 0 0 1.5 14h13a1.5 1.5 0 0 0 1.5-1.5v-9A1.5 1.5 0 0 0 14.5 2h-13z"/>
                    <path d="M3 5.5a.5.5 0 0 1 .5-.5h9a.5.5 0 0 1 0 1h-9a.5.5 0 0 1-.5-.5zM3 8a.5.5 0 0 1 .5-.5h9a.5.5 0 0 1 0 1h-9A.5.5 0 0 1 3 8zm0 2.5a.5.5 0 0 1 .5-.5h6a.5.5 0 0 1 0 1h-6a.5.5 0 0 1-.5-.5z"/>
                </svg>
                <span>Lớp</span>
            </a>
          
          <!-- Mục Môn học -->
          <a href="/admin/dashboard/monhoc" class:active={currentPath.includes("/admin/dashboard/monhoc")}>
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M1 2.828c.885-.37 2.154-.769 3.388-.893 1.33-.134 2.458.063 3.112.752v9.746c-.935-.53-2.12-.603-3.213-.493-1.18.12-2.37.461-3.287.811V2.828zm7.5-.141c.654-.689 1.782-.886 3.112-.752 1.234.124 2.503.523 3.388.893v9.923c-.918-.35-2.107-.692-3.287-.81-1.094-.111-2.278-.039-3.213.492V2.687zM8 1.783C7.015.936 5.587.81 4.287.94c-1.514.153-3.042.672-3.994 1.105A.5.5 0 0 0 0 2.5v11a.5.5 0 0 0 .707.455c.882-.4 2.303-.881 3.68-1.02 1.409-.142 2.59.087 3.223.877a.5.5 0 0 0 .78 0c.633-.79 1.814-1.019 3.222-.877 1.378.139 2.8.62 3.681 1.02A.5.5 0 0 0 16 13.5v-11a.5.5 0 0 0-.293-.455c-.952-.433-2.48-.952-3.994-1.105C10.413.809 8.985.936 8 1.783z"/>
              </svg>
              <span>Môn học</span>
          </a>
          
          <!-- Mục Học kỳ -->
          <a href="/admin/dashboard/hocky" class:active={currentPath.includes("/admin/dashboard/hocky")}>
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M3.5 0a.5.5 0 0 1 .5.5V1h8V.5a.5.5 0 0 1 1 0V1h1a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V3a2 2 0 0 1 2-2h1V.5a.5.5 0 0 1 .5-.5zM1 4v10a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V4H1z"/>
              </svg>
              <span>Học kỳ</span>
          </a>
          
          <!-- Mục Phòng -->
          <a href="/admin/dashboard/phong" class:active={currentPath.includes("/admin/dashboard/phong")}>
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 16 16">
                  <path fill-rule="evenodd" d="M2 13.5V7h1v6.5a.5.5 0 0 0 .5.5h9a.5.5 0 0 0 .5-.5V7h1v6.5a1.5 1.5 0 0 1-1.5 1.5h-9A1.5 1.5 0 0 1 2 13.5zm11-11V6l-2-2V2.5a.5.5 0 0 1 .5-.5h1a.5.5 0 0 1 .5.5z"/>
                  <path fill-rule="evenodd" d="M7.293 1.5a1 1 0 0 1 1.414 0l6.647 6.646a.5.5 0 0 1-.708.708L8 2.207 1.354 8.854a.5.5 0 1 1-.708-.708L7.293 1.5z"/>
              </svg>
              <span>Phòng</span>
          </a>
                    
          <!-- Mục Đợt đăng ký lớp tín chỉ -->
          <a href="/admin/dashboard/dotdangky" class:active={currentPath.includes("/admin/dashboard/dotdangky")}>
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M14 4.5V14a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V2a2 2 0 0 1 2-2h5.5L14 4.5zm-3 0A1.5 1.5 0 0 1 9.5 3V1H4a1 1 0 0 0-1 1v12a1 1 0 0 0 1 1h8a1 1 0 0 0 1-1V4.5h-2z"/>
                  <path d="M4.5 12.5A.5.5 0 0 1 5 12h3a.5.5 0 0 1 0 1H5a.5.5 0 0 1-.5-.5zm0-2A.5.5 0 0 1 5 10h6a.5.5 0 0 1 0 1H5a.5.5 0 0 1-.5-.5z"/>
              </svg>
              <span>Đợt đăng ký lớp tín chỉ</span>
          </a>
          
          <!-- Mục Đợt đóng học phí -->
          <a href="/admin/dashboard/dotdonghocphi" class:active={currentPath.includes("/admin/dashboard/dotdonghocphi")}>
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M12.136.326A1.5 1.5 0 0 1 14 1.78V3h.5A1.5 1.5 0 0 1 16 4.5v9a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 0 13.5v-9a1.5 1.5 0 0 1 1.432-1.499L12.136.326zM5.562 3H13V1.78a.5.5 0 0 0-.621-.484L5.562 3zM1.5 4a.5.5 0 0 0-.5.5v9a.5.5 0 0 0 .5.5h13a.5.5 0 0 0 .5-.5v-9a.5.5 0 0 0-.5-.5h-13z"/>
              </svg>
              <span>Đợt đóng học phí</span>
          </a>

          <!-- Học phí -->
          <a href="/admin/dashboard/hocphi" class:active={currentPath.includes("/admin/dashboard/hocphi")}>
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M4 10.781c.148 1.667 1.513 2.85 3.591 3.003V15h1.043v-1.216c2.27-.179 3.678-1.438 3.678-3.3 0-1.59-.947-2.51-2.956-3.028l-.722-.187V3.467c1.122.11 1.879.714 2.07 1.616h1.47c-.166-1.6-1.54-2.748-3.54-2.875V1H7.591v1.233c-1.939.23-3.27 1.472-3.27 3.156 0 1.454.966 2.483 2.661 2.917l.61.162v4.031c-1.149-.17-1.94-.8-2.131-1.718H4zm3.391-3.836c-1.043-.263-1.6-.825-1.6-1.616 0-.944.704-1.641 1.8-1.828v3.495l-.2-.05zm1.591 1.872c1.287.323 1.852.859 1.852 1.769 0 1.097-.826 1.828-2.2 1.939V8.73l.348.086z"/>
              </svg>
              <span>Học phí</span>
          </a>

          <!-- Tạo thông báo -->
          <a href="/admin/dashboard/thongbao" class:active={currentPath.includes("/admin/dashboard/thongbao")}>
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M8 16a2 2 0 0 0 2-2H6a2 2 0 0 0 2 2zm.995-14.901a1 1 0 1 0-1.99 0A5.002 5.002 0 0 0 3 6c0 1.098-.5 6-2 7h14c-1.5-1-2-5.902-2-7 0-2.42-1.72-4.44-4.005-4.901z"/>
              </svg>
              <span>Tạo thông báo</span>
          </a>
          
          <a
              href="/admin/dashboard"
              class:active={currentPath === "/admin/dashboard" || currentPath === "/admin/dashboard/"}
          >
              <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="18"
                  height="18"
                  fill="currentColor"
                  viewBox="0 0 16 16"
              >
                  <path
                      d="M7 14s-1 0-1-1 1-4 5-4 5 3 5 4-1 1-1 1H7Zm4-6a3 3 0 1 0 0-6 3 3 0 0 0 0 6Zm-5.784 6A2.238 2.238 0 0 1 5 13c0-1.355.68-2.75 1.936-3.72A6.325 6.325 0 0 0 5 9c-4 0-5 3-5 4s1 1 1 1h4.216ZM4.5 8a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Z"
                  />
              </svg>
              <span>Nhân viên</span>
          </a>

          <a
                href="/admin/dashboard/sinhvien"
                class:active={currentPath.includes("/admin/dashboard/sinhvien")}
            >
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="18"
                    height="18"
                    fill="currentColor"
                    viewBox="0 0 16 16"
                >
                    <path d="M8 0a.5.5 0 0 1 .473.337L9.046 2H14a2 2 0 0 1 2 2v7a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h4.954l.572-1.663A.5.5 0 0 1 8 0zM5 5a2 2 0 1 0 0 4 2 2 0 0 0 0-4zM1.5 8.5a3 3 0 1 1 6 0 3 3 0 0 1-6 0zM9 9c0-1.5 2.5-2 4-2s4 .5 4 2v1H9V9z"/>
                </svg>
                <span>Sinh viên</span>
            </a>
      </div>

        <div class="user-section">
            <div class="user-info">
                <div class="user-avatar">
                    <span>{userInfo.name.charAt(0)}</span>
                </div>
                <div class="user-details">
                    <span class="user-name">{userInfo.name}</span>
                    <span class="user-role">{userInfo.role}</span>
                </div>
            </div>
            <button class="logout-button" on:click={handleLogout}>
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="16"
                    height="16"
                    fill="currentColor"
                    viewBox="0 0 16 16"
                >
                    <path
                        fill-rule="evenodd"
                        d="M10 12.5a.5.5 0 0 1-.5.5h-8a.5.5 0 0 1-.5-.5v-9a.5.5 0 0 1 .5-.5h8a.5.5 0 0 1 .5.5v2a.5.5 0 0 0 1 0v-2A1.5 1.5 0 0 0 9.5 2h-8A1.5 1.5 0 0 0 0 3.5v9A1.5 1.5 0 0 0 1.5 14h8a1.5 1.5 0 0 0 1.5-1.5v-2a.5.5 0 0 0-1 0v2z"
                    />
                    <path
                        fill-rule="evenodd"
                        d="M15.854 8.354a.5.5 0 0 0 0-.708l-3-3a.5.5 0 0 0-.708.708L14.293 7.5H5.5a.5.5 0 0 0 0 1h8.793l-2.147 2.146a.5.5 0 0 0 .708.708l3-3z"
                    />
                </svg>
                <span>Đăng xuất</span>
            </button>
        </div>
    </nav>

    <!-- Main Content Area -->
    <main class="content">
        <header class="top-bar">
            <h1 class="page-title">Quản Lý Học Phí</h1>
            <div class="top-actions">
                <button class="action-button">
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="16"
                        height="16"
                        fill="currentColor"
                        viewBox="0 0 16 16"
                    >
                        <path
                            d="M8 16a2 2 0 0 0 2-2H6a2 2 0 0 0 2 2zM8 1.918l-.797.161A4.002 4.002 0 0 0 4 6c0 .628-.134 2.197-.459 3.742-.16.767-.376 1.566-.663 2.258h10.244c-.287-.692-.502-1.49-.663-2.258C12.134 8.197 12 6.628 12 6a4.002 4.002 0 0 0-3.203-3.92L8 1.917zM14.22 12c.223.447.481.801.78 1H1c.299-.199.557-.553.78-1C2.68 10.2 3 6.88 3 6c0-2.42 1.72-4.44 4.005-4.901a1 1 0 1 1 1.99 0A5.002 5.002 0 0 1 13 6c0 .88.32 4.2 1.22 6z"
                        />
                    </svg>
                </button>
                <button class="action-button">
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="16"
                        height="16"
                        fill="currentColor"
                        viewBox="0 0 16 16"
                    >
                        <path
                            d="M8 4.754a3.246 3.246 0 1 0 0 6.492 3.246 3.246 0 0 0 0-6.492zM5.754 8a2.246 2.246 0 1 1 4.492 0 2.246 2.246 0 0 1-4.492 0z"
                        />
                        <path
                            d="M9.796 1.343c-.527-1.79-3.065-1.79-3.592 0l-.094.319a.873.873 0 0 1-1.255.52l-.292-.16c-1.64-.892-3.433.902-2.54 2.541l.159.292a.873.873 0 0 1-.52 1.255l-.319.094c-1.79.527-1.79 3.065 0 3.592l.319.094a.873.873 0 0 1 .52 1.255l-.16.292c-.892 1.64.901 3.434 2.541 2.54l.292-.159a.873.873 0 0 1 1.255.52l.094.319c.527 1.79 3.065 1.79 3.592 0l.094-.319a.873.873 0 0 1 1.255-.52l.292.16c1.64.893 3.434-.902 2.54-2.541l-.159-.292a.873.873 0 0 1 .52-1.255l.319-.094c1.79-.527 1.79-3.065 0-3.592l-.319-.094a.873.873 0 0 1-.52-1.255l.16-.292c.893-1.64-.902-3.433-2.541-2.54l-.292.159a.873.873 0 0 1-1.255-.52l-.094-.319z"
                        />
                    </svg>
                </button>
            </div>
        </header>

        <div class="page-content">
            <div class="users-container">
                <!-- Header -->
                <div class="section-header">
                    <h2>Danh Sách Học Phí Sinh Viên</h2>
                </div>

                {#if paymentMessage}
                    <div class="success-message">{paymentMessage}</div>
                {/if}

                <!-- Loading and error states -->
                {#if loading}
                    <div class="loading-container">
                        <div class="loading-spinner"></div>
                        <p>Đang tải danh sách học phí...</p>
                    </div>
                {:else if error}
                    <div class="error-container">
                        <p>Lỗi: {error}</p>
                        <button class="secondary-button" on:click={fetchHocPhi}>Thử lại</button>
                    </div>
                {:else if hocPhiList.length === 0}
                    <div class="empty-container">
                        <p>Không tìm thấy dữ liệu học phí nào.</p>
                    </div>
                {:else}
                    <!-- Học Phí Table -->
                    <div class="table-responsive">
                        <table class="users-table">
                            <thead>
                                <tr>
                                    <th>STT</th>
                                    <th>MSSV</th>
                                    <th>Họ Tên</th>
                                    <th>Học Kỳ</th>
                                    <th>Trạng Thái</th>
                                    <th></th>
                                </tr>
                            </thead>
                            <tbody>
                                {#each hocPhiList as hocPhi, index}
                                    <tr>
                                        <td>{index + 1}</td>
                                        <td>{hocPhi.svId}</td>
                                        <td>{hocPhi.hoTenSinhVien || 'Không rõ'}</td>
                                        <td>{hocPhi.hocKyTen} ({hocPhi.namHoc})</td>
                                        <td>
                                            <span class={`status-badge ${hocPhi.trangThai === "Da dong" ? "paid" : "unpaid"}`}>
                                                {hocPhi.trangThai === "Da dong" ? "Đã đóng" : "Chưa đóng"}
                                            </span>
                                        </td>
                                        <td class="actions">
                                            <button
                                                class="action-btn view"
                                                on:click={() => openDetailModal(hocPhi)}
                                            >
                                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                                                    <path d="M16 8s-3-5.5-8-5.5S0 8 0 8s3 5.5 8 5.5S16 8 16 8zM1.173 8a13.133 13.133 0 0 1 1.66-2.043C4.12 4.668 5.88 3.5 8 3.5c2.12 0 3.879 1.168 5.168 2.457A13.133 13.133 0 0 1 14.828 8c-.058.087-.122.183-.195.288-.335.48-.83 1.12-1.465 1.755C11.879 11.332 10.119 12.5 8 12.5c-2.12 0-3.879-1.168-5.168-2.457A13.134 13.134 0 0 1 1.172 8z"/>
                                                    <path d="M8 5.5a2.5 2.5 0 1 0 0 5 2.5 2.5 0 0 0 0-5zM4.5 8a3.5 3.5 0 1 1 7 0 3.5 3.5 0 0 1-7 0z"/>
                                                </svg>
                                            
                                            </button>
                                            {#if hocPhi.trangThai !== "Da dong"}
                                                <button
                                                    class="action-btn pay"
                                                    on:click={() => markAsPaid(hocPhi.svId, hocPhi.hocKyId)}
                                                >
                                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                                                        <path d="M12.136.326A1.5 1.5 0 0 1 14 1.78V3h.5A1.5 1.5 0 0 1 16 4.5v9a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 0 13.5v-9a1.5 1.5 0 0 1 1.5-1.5h13a.5.5 0 0 0 0-1H1.5a.5.5 0 0 0-.5.5v9a.5.5 0 0 0 .5.5h13a.5.5 0 0 0 .5-.5v-9a.5.5 0 0 0-.5-.5H9v-.5A1.5 1.5 0 0 1 7.5 2h-5a.5.5 0 0 0 0 1h5a.5.5 0 0 1 .5.5v2.5a.5.5 0 0 0 1 0Z"/>
                                                    </svg>
                                                 
                                                </button>
                                            {/if}
                                        </td>
                                    </tr>
                                {/each}
                            </tbody>
                        </table>
                    </div>
                {/if}
            </div>
        </div>
    </main>

    <!-- Chi tiết học phí Modal -->
    {#if showDetailModal && selectedHocPhi}
        <div class="modal-overlay">
            <div class="modal">
                <div class="modal-header">
                    <h3>Chi Tiết Học Phí</h3>
                    <button
                        class="close-button"
                        on:click={() => (showDetailModal = false)}>×</button
                    >
                </div>
                <div class="modal-body">
                    <div class="detail-container">
                        <div class="detail-row">
                            <div class="detail-label">MSSV:</div>
                            <div class="detail-value">{selectedHocPhi.sinhVienId}</div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-label">Họ tên:</div>
                            <div class="detail-value">{selectedHocPhi.hoTenSinhVien || 'Không rõ'}</div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-label">Học kỳ:</div>
                            <div class="detail-value">{selectedHocPhi.tenHocKy} ({selectedHocPhi.namHoc})</div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-label">Tổng tiền cần đóng:</div>
                            <div class="detail-value highlight">{formatCurrency(selectedHocPhi.tongTien)}</div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-label">Đã đóng:</div>
                            <div class="detail-value highlight">{formatCurrency(selectedHocPhi.daDong)}</div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-label">Còn thiếu:</div>
                            <div class="detail-value highlight remaining">{formatCurrency(selectedHocPhi.tongTien - selectedHocPhi.daDong)}</div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-label">Ngày đóng:</div>
                            <div class="detail-value">{formatDate(selectedHocPhi.ngayDong)}</div>
                        </div>
                        <div class="detail-row">
                            <div class="detail-label">Trạng thái:</div>
                            <div class="detail-value">
                                <span class={`status-badge ${selectedHocPhi.trangThai === "Da dong" ? "paid" : "unpaid"}`}>
                                    {selectedHocPhi.trangThai === "Da dong" ? "Đã đóng" : "Chưa đóng"}
                                </span>
                            </div>
                        </div>
                    </div>

                    <div class="modal-actions">
                        {#if selectedHocPhi.trangThai !== "Da dong"}
                            <button 
                                class="primary-button"
                                on:click={() => markAsPaid(selectedHocPhi.sinhVienId, selectedHocPhi.hocKyId)}
                            >
                                Đánh dấu đã đóng
                            </button>
                        {/if}
                        <button
                            type="button"
                            class="secondary-button"
                            on:click={() => (showDetailModal = false)}
                            >Đóng</button
                        >
                    </div>
                </div>
            </div>
        </div>
    {/if}
</div>

<style>
    :global(body) {
        margin: 0;
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
            Oxygen, Ubuntu, Cantarell, "Open Sans", "Helvetica Neue", sans-serif;
        background-color: #f1f5f9;
        color: #1e293b;
    }

    .app-container {
        display: flex;
        min-height: 100vh;
    }

    /* Sidebar styles */
    .sidebar {
        width: 260px;
        background-color: #1e3a8a;
        color: white;
        display: flex;
        flex-direction: column;
        padding: 1.5rem 1rem;
        position: fixed;
        top: 0;
        bottom: 0;
        left: 0;
        z-index: 10;
    }

    .logo-container {
        display: flex;
        align-items: center;
        margin-bottom: 2rem;
    }

    .logo {
        width: 40px;
        height: 40px;
        margin-right: 0.75rem;
    }

    .app-name {
        font-size: 1.25rem;
        font-weight: 600;
        margin: 0;
    }

    .nav-links {
        display: flex;
        flex-direction: column;
        flex-grow: 1;
    }

    .nav-links a {
        display: flex;
        align-items: center;
        padding: 0.75rem 1rem;
        color: rgba(255, 255, 255, 0.7);
        text-decoration: none;
        border-radius: 8px;
        margin-bottom: 0.25rem;
        transition: all 0.2s;
    }

    .nav-links a svg {
        margin-right: 0.75rem;
    }

    .nav-links a:hover {
        background-color: rgba(255, 255, 255, 0.1);
        color: white;
    }

    .nav-links a.active {
        background-color: rgba(255, 255, 255, 0.2);
        color: white;
    }

    .user-section {
        margin-top: auto;
        border-top: 1px solid rgba(255, 255, 255, 0.1);
        padding-top: 1rem;
    }

    .user-info {
        display: flex;
        align-items: center;
        margin-bottom: 1rem;
        padding: 0.5rem 0;
    }

    .user-avatar {
        width: 36px;
        height: 36px;
        background-color: rgba(255, 255, 255, 0.2);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 0.75rem;
        font-weight: 600;
    }

    .user-details {
        display: flex;
        flex-direction: column;
    }

    .user-name {
        font-weight: 600;
        line-height: 1.2;
    }

    .user-role {
        font-size: 0.75rem;
        opacity: 0.7;
    }

    .logout-button {
        display: flex;
        align-items: center;
        background-color: rgba(255, 255, 255, 0.1);
        border: none;
        border-radius: 8px;
        padding: 0.75rem 1rem;
        width: 100%;
        color: white;
        cursor: pointer;
        transition: background-color 0.2s;
    }

    .logout-button svg {
        margin-right: 0.75rem;
    }

    .logout-button:hover {
        background-color: rgba(255, 255, 255, 0.2);
    }

    /* Main content area */
    .content {
        flex: 1;
        padding: 1.5rem;
        margin-left: 260px;
        width: calc(100% - 260px);
    }

    .top-bar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1.5rem;
    }

    .page-title {
        margin: 0;
        font-size: 1.5rem;
        font-weight: 700;
        color: #0f172a;
    }

    .top-actions {
        display: flex;
        gap: 0.5rem;
    }

    .action-button {
        width: 36px;
        height: 36px;
        border-radius: 8px;
        background-color: white;
        border: 1px solid #e2e8f0;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #64748b;
        cursor: pointer;
        transition: all 0.2s;
    }

    .action-button:hover {
        background-color: #f8fafc;
        color: #0f172a;
    }

    .page-content {
        display: grid;
        gap: 1.5rem;
    }

    /* Additional styles for users page */
    .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1.5rem;
    }

    .section-header h2 {
        color: #1e3a8a;
        margin: 0;
        font-size: 1.2rem;
    }

    .primary-button {
        display: flex;
        align-items: center;
        background-color: #1e3a8a;
        color: white;
        border: none;
        border-radius: 6px;
        padding: 0.6rem 1rem;
        font-size: 0.9rem;
        font-weight: 500;
        cursor: pointer;
        transition: background-color 0.2s;
    }

    .primary-button:hover {
        background-color: #1e40af;
    }

    .secondary-button {
        background-color: #f3f4f6;
        color: #374151;
        border: 1px solid #d1d5db;
        border-radius: 6px;
        padding: 0.6rem 1rem;
        font-size: 0.9rem;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s;
    }

    .secondary-button:hover {
        background-color: #e5e7eb;
    }

    .users-container {
        background-color: white;
        border-radius: 10px;
        box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
        padding: 1.5rem;
    }

    .table-responsive {
        overflow-x: auto;
    }

    .users-table {
        width: 100%;
        border-collapse: collapse;
        font-size: 0.9rem;
    }

    .users-table th,
    .users-table td {
        padding: 0.8rem 1rem;
        text-align: left;
    }

    .users-table th {
        background-color: #f9fafb;
        font-weight: 600;
        color: #374151;
        border-bottom: 1px solid #e5e7eb;
    }

    .users-table tr {
        border-bottom: 1px solid #f3f4f6;
    }

    .users-table tr:last-child {
        border-bottom: none;
    }

    .users-table tr:hover {
        background-color: #f9fafb;
    }

    .actions {
        display: flex;
        gap: 0.5rem;
    }

    .action-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 32px;
        height: 32px;
        border-radius: 6px;
        border: none;
        cursor: pointer;
        transition: all 0.2s;
    }

    .action-btn.edit {
        background-color: #dbeafe;
        color: #6985e1;
    }

    .action-btn.edit:hover {
        background-color: #bfdbfe;
    }

    .action-btn.delete {
        background-color: #fee2e2;
        color: #991b1b;
    }

    .action-btn.delete:hover {
        background-color: #fecaca;
    }

    /* Loading, error and empty states */
    .loading-container,
    .error-container,
    .empty-container {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 3rem 1rem;
        text-align: center;
    }

    .loading-spinner {
        width: 40px;
        height: 40px;
        border: 3px solid #e5e7eb;
        border-top: 3px solid #1e3a8a;
        border-radius: 50%;
        animation: spin 1s linear infinite;
        margin-bottom: 1rem;
    }

    @keyframes spin {
        0% {
            transform: rotate(0deg);
        }
        100% {
            transform: rotate(360deg);
        }
    }

    /* Modal styles */
    .modal-overlay {
        position: fixed !important;
        top: 0 !important;
        left: 0 !important;
        right: 0 !important;
        bottom: 0 !important;
        background-color: rgba(0, 0, 0, 0.5) !important;
        display: flex !important;
        align-items: center !important;
        justify-content: center !important;
        z-index: 99999 !important;
        visibility: visible !important;
        opacity: 1 !important;
    }

    .modal {
        background-color: white !important;
        display: flex !important;
        flex-direction: column !important;
        border-radius: 10px !important;
        width: 500px !important;
        max-height: 80vh;
        overflow-y: auto !important;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1) !important;
        visibility: visible !important;
        opacity: 1 !important;
        z-index: 100000 !important;
        position: fixed !important;
        top: 50% !important;
        left: 50% !important;
        transform: translate(-50%, -50%) !important;
    }

    .modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1.5rem 2rem;
        border-bottom: 1px solid #e5e7eb;
    }

    .close-button {
        background: none;
        border: none;
        font-size: 1.8rem;
        color: #6b7280;
        cursor: pointer;
    }

    .modal-body {
        padding: 2rem;
        font-size: 1rem;
    }

    .form-group {
        margin-bottom: 1.5rem;
    }


    .success-message {
        background-color: #88e18b; 
        color: white;
        padding: 10px;
        margin-bottom: 10px;
        border-radius: 5px;
        text-align: center;
    }

    .delete-message {
        background-color: #e0675e; 
        color: white;
        padding: 10px;
        margin-bottom: 10px;
        border-radius: 5px;
        text-align: center;
    }

    .update-message {
        background-color: #fff3cd;
        color: #856404; 
        border: 1px solid #ffeeba;
        padding: 10px;
        margin-bottom: 10px;
        border-radius: 5px;
        text-align: center;
    }

    .modal-actions {
        display: flex;
        justify-content: flex-end;
        gap: 1rem;
        margin-top: 1.5rem;
    }

    .status-badge {
        padding: 4px 8px;
        border-radius: 4px;
        font-size: 0.85rem;
        font-weight: 500;
    }
    
    .status-badge.paid {
        background-color: #d1e7dd;
        color: #146c43;
    }
    
    .status-badge.unpaid {
        background-color: #f8d7da;
        color: #b02a37;
    }
    
    .action-btn.view {
        background-color: #e0f2fe;
        color: #0ea5e9;
        border: none;
        border-radius: 4px;
        padding: 5px 10px;
        cursor: pointer;
        display: flex;
        align-items: center;
        gap: 5px;
        
        font-size: 0.85rem;
    }
    
    .action-btn.pay {
        background-color: #dbeafe;
        color: #1e40af;
        border: none;
        border-radius: 4px;
        padding: 5px 10px;
        cursor: pointer;
        display: flex;
        align-items: center;
        gap: 5px;
        
        font-size: 0.85rem;
    }
    
    .detail-container {
        display: flex;
        flex-direction: column;
        gap: 10px;
        margin-bottom: 20px;
    }
    
    .detail-row {
        display: flex;
        padding: 8px 0;
        border-bottom: 1px solid #eee;
    }
    
    .detail-label {
        width: 40%;
        font-weight: 500;
        color: #555;
    }
    
    .detail-value {
        width: 60%;
    }
    
    .detail-value.highlight {
        font-weight: 600;
        color: #0d6efd;
    }
    
    .detail-value.remaining {
        color: #dc3545;
    }
    
    .success-message {
        background-color: #d1e7dd;
        color: #146c43;
        padding: 10px 15px;
        border-radius: 4px;
        margin-bottom: 15px;
    }
</style>