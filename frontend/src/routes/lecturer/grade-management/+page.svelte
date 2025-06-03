<script lang="ts">
  import { onMount, tick } from 'svelte';
  import 'bootstrap/dist/css/bootstrap.min.css';
  import { browser } from '$app/environment';
  import { goto } from '$app/navigation';

  interface SinhVienDiem {
    dangKyId: number;
    mssv: string;
    ho: string;
    ten: string;
    hoTen: string;
    tenLop: string;
    diemCC: number;
    diemKT: number;
    diemThi: number;
    diemTB: number;
  }

  interface LopTinChi {
    id: number;
    tenMon: string;
    soTinChi: number;
    nhom: number;
    siSoToiDa: number;
    siSoDangKy: number;
    tenHocKy: string;
    namHoc: number;
  }

  // Dữ liệu người dùng
  let userData: any = null;
  let isLoadingUserData = true;
  let userDataError: string | null = null;
  let sidebarCollapsed = false;
  
  // Dữ liệu lớp tín chỉ
  let lopTinChiList: LopTinChi[] = [];
  let isLoadingLopTinChi = false;
  let lopTinChiError: string | null = null;
  
  // Dữ liệu sinh viên và điểm
  let selectedLopTinChiId: number | null = null;
  let sinhVienDiemList: SinhVienDiem[] = [];
  let isLoadingSinhVienDiem = false;
  let sinhVienDiemError: string | null = null;
  
  // Dữ liệu chỉnh sửa điểm
  let editingStudentId: number | null = null;
  let tempDiemCC: string = '';
  let tempDiemKT: string = '';
  let tempDiemThi: string = '';
  
  // Trạng thái cập nhật
  let isUpdating = false;
  let updateSuccess: string | null = null;
  let updateError: string | null = null;
  
  // Lấy dữ liệu người dùng
  async function fetchUserData() {
    if (browser) {
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
        console.log("Dữ liệu người dùng:", userData);
        
        if (userData && userData.chucVu === 'Giao vien') {
          // Tải danh sách lớp tín chỉ của giảng viên
          fetchLopTinChi(userData.id);
        } else {
          console.warn("Người dùng không phải là giảng viên.");
          goto('/dashboard');
        }
      } catch (error) {
        console.error("Lỗi khi tải thông tin người dùng:", error);
        userDataError = error instanceof Error ? error.message : "Đã xảy ra lỗi không xác định khi tải thông tin người dùng.";
      } finally {
        isLoadingUserData = false;
      }
    }
  }
  
  // Lấy danh sách lớp tín chỉ của giảng viên
  async function fetchLopTinChi(giangVienId: number) {
    isLoadingLopTinChi = true;
    lopTinChiError = null;
    
    try {
      const token = localStorage.getItem('jwtToken');
      // Sử dụng API mới để lấy danh sách lớp tín chỉ
      const response = await fetch(`http://localhost:1234/api/lecturers/grade-management/classes?giangVienId=${giangVienId}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (!response.ok) {
        throw new Error(`Không thể tải danh sách lớp tín chỉ. Mã lỗi: ${response.status}`);
      }
      
      lopTinChiList = await response.json();
      console.log("Danh sách lớp tín chỉ:", lopTinChiList);
      
    } catch (error) {
      console.error("Lỗi khi tải danh sách lớp tín chỉ:", error);
      lopTinChiError = error instanceof Error ? error.message : "Không thể tải danh sách lớp tín chỉ.";
    } finally {
      isLoadingLopTinChi = false;
    }
  }
  
  // Lấy danh sách sinh viên và điểm của lớp tín chỉ
  async function fetchSinhVienDiem(lopTcId: number) {
    selectedLopTinChiId = lopTcId;
    isLoadingSinhVienDiem = true;
    sinhVienDiemError = null;
    
    try {
      const token = localStorage.getItem('jwtToken');
      // Sử dụng API mới để lấy danh sách sinh viên và điểm
      const response = await fetch(`http://localhost:1234/api/lecturers/grade-management/classes/${lopTcId}/students`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (!response.ok) {
        throw new Error(`Không thể tải danh sách sinh viên và điểm. Mã lỗi: ${response.status}`);
      }
      
      sinhVienDiemList = await response.json();
      console.log("Danh sách sinh viên và điểm:", sinhVienDiemList);
      
    } catch (error) {
      console.error("Lỗi khi tải danh sách sinh viên và điểm:", error);
      sinhVienDiemError = error instanceof Error ? error.message : "Không thể tải danh sách sinh viên và điểm.";
    } finally {
      isLoadingSinhVienDiem = false;
    }
  }
  
  // Bắt đầu chỉnh sửa điểm cho sinh viên
  function startEditDiem(student: SinhVienDiem) {
    editingStudentId = student.dangKyId;
    tempDiemCC = student.diemCC.toString();
    tempDiemKT = student.diemKT.toString();
    tempDiemThi = student.diemThi.toString();
  }
  
  // Hủy chỉnh sửa điểm
  function cancelEditDiem() {
    editingStudentId = null;
  }
  
  // Lưu điểm mới
  async function saveDiem(student: SinhVienDiem) {
    if (!editingStudentId) {
      updateError = "Không đủ thông tin để cập nhật điểm.";
      return;
    }
    
    // Validate inputs
    const diemRegex = /^([0-9]|10)(\.\d)?$/;
    if (!diemRegex.test(tempDiemCC) || !diemRegex.test(tempDiemKT) || !diemRegex.test(tempDiemThi)) {
      updateError = "Điểm phải là số từ 0-10 với tối đa 1 chữ số thập phân.";
      return;
    }
    
    isUpdating = true;
    updateError = null;
    updateSuccess = null;
    
    try {
      const token = localStorage.getItem('jwtToken');
      // Sử dụng API mới để cập nhật điểm
      const response = await fetch(`http://localhost:1234/api/lecturers/grade-management/grades/${editingStudentId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          diemCC: parseFloat(tempDiemCC),
          diemKT: parseFloat(tempDiemKT),
          diemThi: parseFloat(tempDiemThi)
        })
      });
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Không thể cập nhật điểm.");
      }
      
      const updatedStudent = await response.json();
      
      // Cập nhật dữ liệu trên giao diện
      const index = sinhVienDiemList.findIndex(s => s.dangKyId === student.dangKyId);
      if (index !== -1) {
        sinhVienDiemList[index] = updatedStudent;
      }
      
      updateSuccess = `Cập nhật điểm cho sinh viên ${student.hoTen} thành công.`;
      editingStudentId = null;
      
    } catch (error) {
      console.error("Lỗi khi cập nhật điểm:", error);
      updateError = error instanceof Error ? error.message : "Đã xảy ra lỗi không xác định khi cập nhật điểm.";
    } finally {
      isUpdating = false;
    }
  }
  
  function toggleSidebar() {
    sidebarCollapsed = !sidebarCollapsed;
  }

  // Format điểm để hiển thị
  function formatDiem(diem: number): string {
    if (diem === null || diem === undefined || isNaN(diem)) return "0.0";
    return diem.toFixed(1);
  }

  onMount(() => {
    if (browser) {
      import('bootstrap/dist/js/bootstrap.bundle.min.js');
      fetchUserData();
    }
  });
</script>

<div class="app-container">
  <!-- Sidebar -->
  <div class="sidebar {sidebarCollapsed ? 'collapsed' : ''}">
    <!-- Logo -->
    <div class="sidebar-header">
      <img src="/images/logos/ptit-logo-inv.png" alt="PTIT Logo" class="sidebar-logo">
      {#if !sidebarCollapsed}
        <span class="sidebar-title">Hệ Thống Giảng Viên</span>
      {/if}
      <button class="btn toggle-btn" on:click={toggleSidebar} aria-label="Toggle sidebar">
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
      <li class="sidebar-item">
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
      <li class="sidebar-item active">
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
    <div class="container-fluid p-3 p-md-4">
      {#if isLoadingUserData}
        <div class="d-flex justify-content-center align-items-center py-5">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Đang tải...</span>
          </div>
          <p class="ms-3 mb-0">Đang tải thông tin giảng viên...</p>
        </div>
      {:else if userDataError}
        <div class="alert alert-danger" role="alert">
          <h4 class="alert-heading">Lỗi!</h4>
          <p>{userDataError}</p>
          <hr>
          <p class="mb-0">Vui lòng <a href="/" class="alert-link">đăng nhập</a> lại.</p>
        </div>
      {:else}
        <!-- Tiêu đề trang -->
        <div class="page-header mb-4">
          <h1 class="fs-2 fw-bold">Quản lý điểm sinh viên</h1>
          <p class="text-muted mb-0">Xem và cập nhật điểm số cho sinh viên trong các lớp tín chỉ</p>
        </div>
        
        <!-- Thông báo -->
        {#if updateSuccess}
          <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle-fill me-2"></i> {updateSuccess}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close" on:click={() => updateSuccess = null}></button>
          </div>
        {/if}
        
        {#if updateError}
          <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2"></i> {updateError}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close" on:click={() => updateError = null}></button>
          </div>
        {/if}
        
        <!-- Chọn lớp tín chỉ -->
        <div class="card border-0 shadow-sm mb-4">
          <div class="card-header bg-white py-3">
            <h5 class="mb-0 fw-bold">Danh sách lớp tín chỉ</h5>
          </div>
          <div class="card-body">
            {#if isLoadingLopTinChi}
              <div class="d-flex justify-content-center py-4">
                <div class="spinner-border text-primary" role="status">
                  <span class="visually-hidden">Đang tải...</span>
                </div>
                <p class="ms-3 mb-0">Đang tải danh sách lớp tín chỉ...</p>
              </div>
            {:else if lopTinChiError}
              <div class="alert alert-danger">
                <i class="bi bi-exclamation-circle-fill me-2"></i> {lopTinChiError}
              </div>
            {:else if lopTinChiList.length === 0}
              <div class="alert alert-info">
                <i class="bi bi-info-circle-fill me-2"></i> Bạn chưa có lớp tín chỉ nào. Vui lòng đăng ký lớp tín chỉ trước.
              </div>
            {:else}
              <div class="table-responsive">
                <table class="table table-hover">
                  <thead class="table-light">
                    <tr>
                      <th>STT</th>
                      <th>Mã lớp</th>
                      <th>Tên môn</th>
                      <th>Học kỳ</th>
                      <th>Nhóm</th>
                      <th>Sĩ số</th>
                      <th>Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {#each lopTinChiList as lop, index}
                      <tr class={selectedLopTinChiId === lop.id ? 'table-primary' : ''}>
                        <td>{index + 1}</td>
                        <td>{lop.id}</td>
                        <td>{lop.tenMon}</td>
                        <td>{lop.tenHocKy} {lop.namHoc}</td>
                        <td>{lop.nhom}</td>
                        <td>{lop.siSoDangKy}/{lop.siSoToiDa}</td>
                        <td>
                          <button 
                            class="btn btn-sm btn-primary" 
                            on:click={() => fetchSinhVienDiem(lop.id)}
                            disabled={isLoadingSinhVienDiem}
                          >
                            <i class="bi bi-list-check"></i> Xem điểm
                          </button>
                        </td>
                      </tr>
                    {/each}
                  </tbody>
                </table>
              </div>
            {/if}
          </div>
        </div>
        
        <!-- Danh sách sinh viên và điểm -->
        {#if selectedLopTinChiId}
          <div class="card border-0 shadow-sm mb-4">
            <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
              <h5 class="mb-0 fw-bold">
                Danh sách sinh viên và điểm
                {#if lopTinChiList.length > 0 && selectedLopTinChiId}
                  - {lopTinChiList.find(lop => lop.id === selectedLopTinChiId)?.tenMon} 
                  (Nhóm {lopTinChiList.find(lop => lop.id === selectedLopTinChiId)?.nhom})
                {/if}
              </h5>
            </div>
            <div class="card-body">
              {#if isLoadingSinhVienDiem}
                <div class="d-flex justify-content-center py-4">
                  <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Đang tải...</span>
                  </div>
                  <p class="ms-3 mb-0">Đang tải danh sách sinh viên và điểm...</p>
                </div>
              {:else if sinhVienDiemError}
                <div class="alert alert-danger">
                  <i class="bi bi-exclamation-circle-fill me-2"></i> {sinhVienDiemError}
                </div>
              {:else if sinhVienDiemList.length === 0}
                <div class="alert alert-info">
                  <i class="bi bi-info-circle-fill me-2"></i> Chưa có sinh viên nào đăng ký lớp này hoặc chưa được xác nhận.
                </div>
              {:else}
                <div class="table-responsive">
                  <table class="table table-hover">
                    <thead class="table-light">
                      <tr>
                        <th>STT</th>
                        <th>MSSV</th>
                        <th>Họ tên</th>
                        <th>Lớp</th>
                        <th>Điểm CC</th>
                        <th>Điểm KT</th>
                        <th>Điểm thi</th>
                        <th>Điểm TB</th>
                        <th>Thao tác</th>
                      </tr>
                    </thead>
                    <tbody>
                      {#each sinhVienDiemList as student, index}
                        <tr>
                          <td>{index + 1}</td>
                          <td>{student.mssv}</td>
                          <td>{student.hoTen}</td>
                          <td>{student.tenLop}</td>
                          <td>
                            {#if editingStudentId === student.dangKyId}
                              <input 
                                type="text" 
                                class="form-control form-control-sm" 
                                bind:value={tempDiemCC} 
                                placeholder="0-10"
                              />
                            {:else}
                              {formatDiem(student.diemCC)}
                            {/if}
                          </td>
                          <td>
                            {#if editingStudentId === student.dangKyId}
                              <input 
                                type="text" 
                                class="form-control form-control-sm" 
                                bind:value={tempDiemKT} 
                                placeholder="0-10"
                              />
                            {:else}
                              {formatDiem(student.diemKT)}
                            {/if}
                          </td>
                          <td>
                            {#if editingStudentId === student.dangKyId}
                              <input 
                                type="text" 
                                class="form-control form-control-sm" 
                                bind:value={tempDiemThi} 
                                placeholder="0-10"
                              />
                            {:else}
                              {formatDiem(student.diemThi)}
                            {/if}
                          </td>
                          <td>
                            <span class="badge {student.diemTB >= 5 ? 'bg-success' : 'bg-danger'}">
                              {formatDiem(student.diemTB)}
                            </span>
                          </td>
                          <td>
                            {#if editingStudentId === student.dangKyId}
                              <div class="d-flex">
                                <button 
                                  class="btn btn-sm btn-success me-1" 
                                  on:click={() => saveDiem(student)}
                                  disabled={isUpdating}
                                >
                                  <i class="bi bi-check"></i>
                                </button>
                                <button 
                                  class="btn btn-sm btn-secondary" 
                                  on:click={cancelEditDiem}
                                  disabled={isUpdating}
                                >
                                  <i class="bi bi-x"></i>
                                </button>
                              </div>
                            {:else}
                              <button 
                                class="btn btn-sm btn-outline-primary" 
                                on:click={() => startEditDiem(student)}
                              >
                                <i class="bi bi-pencil"></i> Sửa điểm
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
        {/if}
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
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
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
  
  /* Card styles */
  .card {
    border-radius: 10px;
    overflow: hidden;
    transition: box-shadow 0.3s ease;
  }
  
  .card:hover {
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08) !important;
  }
  
  /* Form styles */
  .form-label {
    font-size: 0.9rem;
    margin-bottom: 0.5rem;
  }
  
  .form-control:focus, .form-select:focus {
    border-color: #80bdff;
    box-shadow: 0 0 0 0.25rem rgba(0, 123, 255, 0.25);
  }
  
  :global(.was-validated .form-control:invalid),
  :global(.form-control.is-invalid) {
    border-color: #dc3545;
    background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 12 12' width='12' height='12' fill='none' stroke='%23dc3545'%3e%3ccircle cx='6' cy='6' r='4.5'/%3e%3cpath stroke-linejoin='round' d='M5.8 3.6h.4L6 6.5z'/%3e%3ccircle cx='6' cy='8.2' r='.6' fill='%23dc3545' stroke='none'/%3e%3c/svg%3e");
    background-repeat: no-repeat;
    background-position: right calc(0.375em + 0.1875rem) center;
    background-size: calc(0.75em + 0.375rem) calc(0.75em + 0.375rem);
  }
  
  @import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css");
</style> 