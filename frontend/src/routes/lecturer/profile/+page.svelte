<script lang="ts">
  import { onMount, tick } from 'svelte';
  import 'bootstrap/dist/css/bootstrap.min.css';
  import { browser } from '$app/environment';
  import { goto } from '$app/navigation';

  let userData: any = null;
  let isLoadingUserData = true;
  let userDataError: string | null = null;
  let lecturer: any = { isLecturer: false };

  // Thông tin giảng viên
  let lecturerInfo = {
    id: null,
    ho: "",
    ten: "",
    ngaySinh: "",
    gioiTinh: "",
    diaChi: "",
    sdt: "",
    email: "",
    chucVu: "",
    khoaId: null,
    khoaTen: ""
  };

  let sidebarCollapsed = false;
  let isEditing = false;
  let isSaving = false;
  let saveSuccess = false;
  let saveError: string | null = null;
  let formData: any = {};

  // Thêm biến cho đổi mật khẩu
  let showPasswordForm = false;
  let isChangingPassword = false;
  let passwordChangeSuccess = false;
  let passwordChangeError: string | null = null;
  let passwordData = {
    currentPassword: "",
    newPassword: "",
    confirmPassword: ""
  };
  let passwordMismatch = false;

  function toggleSidebar() {
    sidebarCollapsed = !sidebarCollapsed;
  }

  function startEditing() {
    // Tạo bản sao của dữ liệu để chỉnh sửa
    formData = { ...lecturerInfo };
    
    // Format ngày sinh để hiển thị trong input type="date"
    if (formData.ngaySinh) {
      const date = new Date(formData.ngaySinh);
      formData.ngaySinh = date.toISOString().split('T')[0];
    }
    
    isEditing = true;
  }

  function cancelEditing() {
    isEditing = false;
    saveSuccess = false;
    saveError = null;
  }

  // Thêm hàm để mở/đóng form đổi mật khẩu
  function togglePasswordForm() {
    showPasswordForm = !showPasswordForm;
    if (!showPasswordForm) {
      // Reset form khi đóng
      passwordData = {
        currentPassword: "",
        newPassword: "",
        confirmPassword: ""
      };
      passwordMismatch = false;
      passwordChangeSuccess = false;
      passwordChangeError = null;
    }
  }

  // Hàm đổi mật khẩu
  async function changePassword() {
    // Kiểm tra mật khẩu mới và xác nhận mật khẩu
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      passwordMismatch = true;
      return;
    }
    
    passwordMismatch = false;
    isChangingPassword = true;
    passwordChangeSuccess = false;
    passwordChangeError = null;
    
    try {
      const token = localStorage.getItem('jwtToken');
      const response = await fetch(`http://localhost:1234/api/auth/change-password`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          currentPassword: passwordData.currentPassword,
          newPassword: passwordData.newPassword
        })
      });
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || `Không thể đổi mật khẩu. Mã lỗi: ${response.status}`);
      }
      
      passwordChangeSuccess = true;
      
      // Reset form
      passwordData = {
        currentPassword: "",
        newPassword: "",
        confirmPassword: ""
      };
      
      // Đóng form sau 3 giây nếu thành công
      setTimeout(() => {
        if (passwordChangeSuccess) {
          showPasswordForm = false;
          passwordChangeSuccess = false;
        }
      }, 3000);
      
    } catch (error) {
      console.error("Lỗi khi đổi mật khẩu:", error);
      passwordChangeError = error instanceof Error ? error.message : "Đã xảy ra lỗi không xác định khi đổi mật khẩu.";
    } finally {
      isChangingPassword = false;
    }
  }

  async function saveLecturerInfo() {
    isSaving = true;
    saveSuccess = false;
    saveError = null;
    
    try {
      const token = localStorage.getItem('jwtToken');
      const response = await fetch(`http://localhost:1234/api/lecturers/${lecturer.id}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
      });
      
      if (!response.ok) {
        throw new Error(`Không thể cập nhật thông tin. Mã lỗi: ${response.status}`);
      }
      
      // Cập nhật thông tin lecturerInfo với dữ liệu mới
      const updatedData = await response.json();
      lecturerInfo = updatedData;
      
      saveSuccess = true;
      isEditing = false;
      
      // Cập nhật thông tin lecturer để hiển thị đúng ở sidebar
      lecturer.ho = lecturerInfo.ho;
      lecturer.ten = lecturerInfo.ten;
      lecturer.email = lecturerInfo.email;
      
    } catch (error) {
      console.error("Lỗi khi cập nhật thông tin:", error);
      saveError = error instanceof Error ? error.message : "Đã xảy ra lỗi không xác định khi cập nhật thông tin.";
    } finally {
      isSaving = false;
    }
  }

  onMount(() => {
    const initPage = async () => {
      if (browser) {
        await import('bootstrap/dist/js/bootstrap.bundle.min.js');
  
        const token = localStorage.getItem('jwtToken');
        if (!token) {
          goto('/');
          return;
        }
  
        try {
          const response = await fetch('http://localhost:1234/api/auth/me', {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          });
          
          if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
              localStorage.removeItem('jwtToken');
              goto('/');
              return; 
            }
            throw new Error(`Không thể tải thông tin người dùng. Mã lỗi: ${response.status}`);
          }
          
          userData = await response.json();
          console.log("Dữ liệu người dùng từ /me:", userData);
  
          if (userData && userData.chucVu === 'Giao vien') {
            lecturer = {
              id: userData.id,
              ho: userData.ho || "",
              ten: userData.ten || "",
              email: userData.email || "N/A",
              chucVu: userData.chucVu || "N/A",
              khoaTen: userData.khoaTen || "N/A",
              isLecturer: true
            };
            
            // Lấy thông tin chi tiết giảng viên
            await fetchLecturerDetails(lecturer.id);
          } else {
            lecturer.isLecturer = false;
            console.warn("Người dùng không phải là giảng viên. Chuyển hướng đến trang dashboard.");
            goto('/dashboard');
          }
        } catch (error) {
          console.error("Lỗi khi tải thông tin người dùng:", error);
          userDataError = error instanceof Error ? error.message : "Đã xảy ra lỗi không xác định khi tải thông tin người dùng.";
          if (browser) {
            localStorage.removeItem('jwtToken');
            goto('/');
          }
        } finally {
          isLoadingUserData = false;
          await tick();
        }
      }
    };

    initPage();
  });

  async function fetchLecturerDetails(id: number) {
    try {
      const token = localStorage.getItem('jwtToken');
      const response = await fetch(`http://localhost:1234/api/lecturers/${id}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (!response.ok) {
        throw new Error(`Không thể tải thông tin giảng viên. Mã lỗi: ${response.status}`);
      }
      
      lecturerInfo = await response.json();
      console.log("Thông tin chi tiết giảng viên:", lecturerInfo);
      
    } catch (error) {
      console.error("Lỗi khi tải thông tin giảng viên:", error);
      userDataError = error instanceof Error ? error.message : "Đã xảy ra lỗi không xác định khi tải thông tin giảng viên.";
    }
  }
</script>

<div class="app-container">
  <!-- Sidebar - Sử dụng lại từ lecturer-schedule -->
  <div class="sidebar {sidebarCollapsed ? 'collapsed' : ''}">
    <!-- Logo -->
    <div class="sidebar-header">
      <img src="/images/logos/ptit-logo-inv.png" alt="PTIT Logo" class="sidebar-logo">
      {#if !sidebarCollapsed}
        <span class="sidebar-title">Hệ Thống Giảng Viên</span>
      {/if}
      <button class="btn toggle-btn" on:click={toggleSidebar} aria-label={sidebarCollapsed ? "Expand sidebar" : "Collapse sidebar"}>
        <i class="bi {sidebarCollapsed ? 'bi-chevron-right' : 'bi-chevron-left'}"></i>
      </button>
    </div>
    
    <!-- Menu items -->
    <ul class="sidebar-menu">
      <li class="sidebar-item">
        <a href="/lecturer/dashboard" class="sidebar-link">
          <i class="bi bi-house-door-fill"></i>
          {#if !sidebarCollapsed}<span>Tổng quan</span>{/if}
        </a>
      </li>
      <li class="sidebar-item">
        <a href="/lecturer/schedule" class="sidebar-link">
          <i class="bi bi-calendar-week-fill"></i>
          {#if !sidebarCollapsed}<span>Lịch dạy</span>{/if}
        </a>
      </li>
      <li class="sidebar-item active">
        <a href="/lecturer/profile" class="sidebar-link">
          <i class="bi bi-person-circle"></i>
          {#if !sidebarCollapsed}<span>Thông tin</span>{/if}
        </a>
      </li>
      <li class="sidebar-item">
        <a href="/lecturer/class-registration" class="sidebar-link">
          <i class="bi bi-journal-text"></i>
          {#if !sidebarCollapsed}<span>Đăng ký lớp</span>{/if}
        </a>
      </li>
      <li class="sidebar-item">
        <a href="/lecturer/grade-management" class="sidebar-link">
          <i class="bi bi-pencil-square"></i>
          {#if !sidebarCollapsed}<span>Quản lý điểm</span>{/if}
        </a>
      </li>
    </ul>
    
    <!-- Logout at bottom -->
    <div class="sidebar-footer">
      <a href="/" class="sidebar-link" on:click={() => localStorage.removeItem('jwtToken')}>
        <i class="bi bi-box-arrow-right"></i>
        {#if !sidebarCollapsed}<span>Đăng xuất</span>{/if}
      </a>
    </div>
  </div>

  <!-- Main content -->
  <div class="main-content">
    <div class="container-fluid py-3">
      {#if isLoadingUserData}
        <div class="d-flex justify-content-center align-items-center" style="height: 80vh;">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Đang tải...</span>
          </div>
          <p class="ms-3">Đang tải thông tin...</p>
        </div>
      {:else if userDataError}
        <div class="alert alert-danger" role="alert">
          Lỗi khi tải thông tin người dùng: {userDataError}. Vui lòng <a href="/" class="alert-link">đăng nhập</a> lại.
        </div>
      {:else if lecturer && lecturer.isLecturer}
        <div class="mb-4">
          <h1 class="h3 mb-1">Thông Tin Giảng Viên</h1>
          <p class="text-muted">Xem và cập nhật thông tin cá nhân của bạn.</p>
        </div>
        
        {#if saveSuccess}
          <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle-fill me-2"></i> Cập nhật thông tin thành công!
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close" on:click={() => saveSuccess = false}></button>
          </div>
        {/if}
        
        {#if saveError}
          <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2"></i> {saveError}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close" on:click={() => saveError = null}></button>
          </div>
        {/if}
        
        <div class="row">
          <div class="col-md-4 mb-4">
            <div class="card shadow-sm">
              <div class="card-body text-center">
                <div class="avatar-placeholder mb-3">
                  <i class="bi bi-person-circle" style="font-size: 80px; color: #0056b3;"></i>
                </div>
                <h5 class="mb-1">{lecturerInfo.ho} {lecturerInfo.ten}</h5>
                <p class="text-muted mb-3">{lecturerInfo.chucVu === 'Giao vien' ? 'Giảng viên' : lecturerInfo.chucVu} - {lecturerInfo.khoaTen}</p>
                <div class="d-grid gap-2">
                  {#if !isEditing}
                    <button class="btn btn-primary" on:click={startEditing}>
                      <i class="bi bi-pencil-square me-2"></i> Chỉnh sửa thông tin
                    </button>
                    <!-- Thêm nút đổi mật khẩu -->
                    <button class="btn btn-outline-primary" on:click={togglePasswordForm}>
                      <i class="bi bi-key me-2"></i> Đổi mật khẩu
                    </button>
                  {:else}
                    <button class="btn btn-secondary" on:click={cancelEditing}>
                      <i class="bi bi-x-circle me-2"></i> Hủy chỉnh sửa
                    </button>
                  {/if}
                </div>
              </div>
            </div>
            
            <!-- Form đổi mật khẩu -->
            {#if showPasswordForm}
              <div class="card shadow-sm mt-4">
                <div class="card-header bg-primary-light text-white">
                  <h5 class="mb-0 fs-6">
                    <i class="bi bi-key-fill me-2"></i>
                    Đổi mật khẩu
                  </h5>
                </div>
                <div class="card-body">
                  {#if passwordChangeSuccess}
                    <div class="alert alert-success" role="alert">
                      <i class="bi bi-check-circle-fill me-2"></i> Đổi mật khẩu thành công!
                    </div>
                  {/if}
                  
                  {#if passwordChangeError}
                    <div class="alert alert-danger" role="alert">
                      <i class="bi bi-exclamation-triangle-fill me-2"></i> {passwordChangeError}
                    </div>
                  {/if}
                  
                  <form on:submit|preventDefault={changePassword}>
                    <div class="mb-3">
                      <label for="currentPassword" class="form-label">Mật khẩu hiện tại</label>
                      <input type="password" class="form-control" id="currentPassword" bind:value={passwordData.currentPassword} required>
                    </div>
                    <div class="mb-3">
                      <label for="newPassword" class="form-label">Mật khẩu mới</label>
                      <input type="password" class="form-control" id="newPassword" bind:value={passwordData.newPassword} required>
                    </div>
                    <div class="mb-3">
                      <label for="confirmPassword" class="form-label">Xác nhận mật khẩu mới</label>
                      <input type="password" class="form-control {passwordMismatch ? 'is-invalid' : ''}" id="confirmPassword" bind:value={passwordData.confirmPassword} required>
                      {#if passwordMismatch}
                        <div class="invalid-feedback">
                          Mật khẩu xác nhận không khớp.
                        </div>
                      {/if}
                    </div>
                    <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                      <button type="button" class="btn btn-outline-secondary" on:click={togglePasswordForm}>Hủy</button>
                      <button type="submit" class="btn btn-primary" disabled={isChangingPassword}>
                        {#if isChangingPassword}
                          <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                          Đang xử lý...
                        {:else}
                          <i class="bi bi-check-circle me-2"></i> Xác nhận
                        {/if}
                      </button>
                    </div>
                  </form>
                </div>
              </div>
            {/if}
          </div>
          
          <div class="col-md-8">
            <div class="card shadow-sm">
              <div class="card-header bg-primary-light text-white">
                <h5 class="mb-0 fs-6">
                  <i class="bi bi-person-vcard-fill me-2"></i>
                  {isEditing ? 'Chỉnh Sửa Thông Tin' : 'Thông Tin Cá Nhân'}
                </h5>
              </div>
              
              <div class="card-body">
                {#if isEditing}
                  <!-- Form chỉnh sửa thông tin -->
                  <form on:submit|preventDefault={saveLecturerInfo}>
                    <div class="row g-3">
                      <div class="col-md-6">
                        <label for="ho" class="form-label">Họ</label>
                        <input type="text" class="form-control" id="ho" bind:value={formData.ho} required>
                      </div>
                      <div class="col-md-6">
                        <label for="ten" class="form-label">Tên</label>
                        <input type="text" class="form-control" id="ten" bind:value={formData.ten} required>
                      </div>
                      <div class="col-md-6">
                        <label for="ngaySinh" class="form-label">Ngày sinh</label>
                        <input type="date" class="form-control" id="ngaySinh" bind:value={formData.ngaySinh} required>
                      </div>
                      <div class="col-md-6">
                        <label for="gioiTinh" class="form-label">Giới tính</label>
                        <select class="form-select" id="gioiTinh" bind:value={formData.gioiTinh} required>
                          <option value="">Chọn giới tính</option>
                          <option value="Nam">Nam</option>
                          <option value="Nữ">Nữ</option>
                        </select>
                      </div>
                      <div class="col-12">
                        <label for="diaChi" class="form-label">Địa chỉ</label>
                        <input type="text" class="form-control" id="diaChi" bind:value={formData.diaChi} required>
                      </div>
                      <div class="col-md-6">
                        <label for="sdt" class="form-label">Số điện thoại</label>
                        <input type="tel" class="form-control" id="sdt" bind:value={formData.sdt} required>
                      </div>
                      <div class="col-md-6">
                        <label for="email" class="form-label">Email</label>
                        <input type="email" class="form-control" id="email" bind:value={formData.email} required>
                      </div>
                      <div class="col-12 mt-4">
                        <div class="d-grid gap-2 d-md-flex justify-content-md-end">
                          <button type="button" class="btn btn-outline-secondary" on:click={cancelEditing}>Hủy</button>
                          <button type="submit" class="btn btn-primary" disabled={isSaving}>
                            {#if isSaving}
                              <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                              Đang lưu...
                            {:else}
                              <i class="bi bi-check-circle me-2"></i> Lưu thay đổi
                            {/if}
                          </button>
                        </div>
                      </div>
                    </div>
                  </form>
                {:else}
                  <!-- Hiển thị thông tin -->
                  <div class="row">
                    <div class="col-md-6 mb-3">
                      <div class="info-group">
                        <span class="text-muted">Họ và tên</span>
                        <p class="info-value">{lecturerInfo.ho} {lecturerInfo.ten}</p>
                      </div>
                    </div>
                    <div class="col-md-6 mb-3">
                      <div class="info-group">
                        <span class="text-muted">Ngày sinh</span>
                        <p class="info-value">{new Date(lecturerInfo.ngaySinh).toLocaleDateString('vi-VN')}</p>
                      </div>
                    </div>
                    <div class="col-md-6 mb-3">
                      <div class="info-group">
                        <span class="text-muted">Giới tính</span>
                        <p class="info-value">{lecturerInfo.gioiTinh}</p>
                      </div>
                    </div>
                    <div class="col-md-6 mb-3">
                      <div class="info-group">
                        <span class="text-muted">Chức vụ</span>
                        <p class="info-value">{lecturerInfo.chucVu === 'Giao vien' ? 'Giảng viên' : lecturerInfo.chucVu}</p>
                      </div>
                    </div>
                    <div class="col-12 mb-3">
                      <div class="info-group">
                        <span class="text-muted">Địa chỉ</span>
                        <p class="info-value">{lecturerInfo.diaChi || 'Chưa cập nhật'}</p>
                      </div>
                    </div>
                    <div class="col-md-6 mb-3">
                      <div class="info-group">
                        <span class="text-muted">Số điện thoại</span>
                        <p class="info-value">{lecturerInfo.sdt || 'Chưa cập nhật'}</p>
                      </div>
                    </div>
                    <div class="col-md-6 mb-3">
                      <div class="info-group">
                        <span class="text-muted">Email</span>
                        <p class="info-value">{lecturerInfo.email}</p>
                      </div>
                    </div>
                    <div class="col-md-6 mb-3">
                      <div class="info-group">
                        <span class="text-muted">Khoa</span>
                        <p class="info-value">{lecturerInfo.khoaTen || 'Chưa có khoa'}</p>
                      </div>
                    </div>
                  </div>
                {/if}
              </div>
            </div>
          </div>
        </div>
      {:else}
        <div class="alert alert-warning" role="alert">
          <h4 class="alert-heading">Thông báo!</h4>
          <p>Trang này chỉ dành cho giảng viên. Bạn sẽ được chuyển đến trang chính.</p>
          <hr>
          <p class="mb-0">Đang chuyển hướng...</p>
        </div>
      {/if}
    </div>
  </div>
</div>

<style>
  :global(body) {
    background-color: #f8f9fa;
    min-height: 100vh;
    margin: 0;
    padding: 0;
  }
  
  /* App container */
  .app-container {
    display: flex;
    min-height: 100vh;
  }
  
  /* Sidebar styles */
  .sidebar {
    width: 240px;
    background-color: white;
    border-right: 1px solid #e0e0e0;
    display: flex;
    flex-direction: column;
    transition: width 0.3s ease;
    overflow: hidden;
    z-index: 100;
    height: 100vh;
    position: fixed;
  }
  
  .sidebar.collapsed {
    width: 60px;
  }
  
  .sidebar-header {
    padding: 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid #e0e0e0;
  }
  
  .sidebar-logo {
    width: 28px;
    height: 28px;
    object-fit: contain;
  }
  
  .sidebar-title {
    font-weight: 600;
    margin-left: 10px;
    white-space: nowrap;
    flex: 1;
  }
  
  .toggle-btn {
    background: none;
    border: none;
    color: #0056b3;
    padding: 0;
    font-size: 18px;
  }
  
  .sidebar-menu {
    list-style-type: none;
    padding: 0;
    margin: 0;
    flex: 1;
  }
  
  .sidebar-item {
    margin: 0;
    padding: 0;
  }
  
  .sidebar-item a {
    padding: 12px 16px;
    display: flex;
    align-items: center;
    color: #495057;
    text-decoration: none;
    transition: all 0.2s ease;
    border-left: 3px solid transparent;
  }
  
  .sidebar-item a:hover {
    background-color: #f5f5f5;
  }
  
  .sidebar-item.active a {
    background-color: #e6f0ff;
    color: #0056b3;
    border-left: 3px solid #0056b3;
  }
  
  .sidebar-item i {
    font-size: 1.1rem;
    margin-right: 10px;
    width: 22px;
    text-align: center;
  }
  
  .sidebar-footer {
    padding: 16px;
    border-top: 1px solid #e0e0e0;
  }
  
  .sidebar-footer a {
    display: flex;
    align-items: center;
    color: #495057;
    text-decoration: none;
  }
  
  .sidebar-footer a:hover {
    color: #dc3545;
  }
  
  .sidebar-footer i {
    margin-right: 10px;
  }
  
  /* Main content */
  .main-content {
    flex: 1;
    margin-left: 240px;
    transition: margin 0.3s ease;
    width: calc(100% - 240px);
  }
  
  .sidebar.collapsed + .main-content {
    margin-left: 60px;
    width: calc(100% - 60px);
  }
  
  /* Form styles */
  .form-label {
    font-weight: 500;
  }
  
  .info-group {
    margin-bottom: 0.5rem;
  }
  
  .info-group .text-muted {
    display: block;
    font-size: 0.85rem;
    margin-bottom: 0.2rem;
  }
  
  .info-value {
    font-weight: 500;
    margin-bottom: 0;
  }
  
  /* Card styles */
  .card {
    border-radius: 8px;
    border: none;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  }
  
  .card-header {
    border-bottom: 1px solid rgba(0,0,0,0.1);
    padding: 0.8rem 1rem;
  }
  
  .bg-primary-light {
    background-color: #0056b3 !important;
  }
  
  /* Button styles */
  .btn-primary {
    background-color: #0056b3;
    border-color: #0056b3;
  }
  
  .btn-primary:hover {
    background-color: #004085;
    border-color: #003770;
  }
  
  .btn-outline-primary {
    color: #0056b3;
    border-color: #0056b3;
  }
  
  .btn-outline-primary:hover {
    background-color: #0056b3;
    color: white;
  }
  
  /* Avatar placeholder */
  .avatar-placeholder {
    width: 100px;
    height: 100px;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background-color: #e6f0ff;
  }
</style> 