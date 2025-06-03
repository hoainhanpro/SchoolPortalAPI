<script lang="ts">
  import { onMount, tick } from 'svelte';
  import 'bootstrap/dist/css/bootstrap.min.css';
  import { browser } from '$app/environment';
  import { goto } from '$app/navigation';

  interface Mon {
    id: number;
    maMon: string;
    tenMon: string;
    soTinChi: number;
  }

  interface HocKy {
    id: number;
    name: string;
    namHoc: number;
    thuTu: number;
    startDate: string;
    endDate: string;
    isCurrentSemester: boolean;
  }

  interface PhongHoc {
    id: number;
    ten: string;
    sucChua: number;
  }

  interface Buoi {
    id: number;
    ten: string;
    startTime: string;
    endTime: string;
  }

  // Dữ liệu người dùng
  let userData: any = null;
  let isLoadingUserData = true;
  let userDataError: string | null = null;
  let sidebarCollapsed = false;
  
  // Dữ liệu môn học
  let monHocList: Mon[] = [];
  let isLoadingMonHoc = false;
  let monHocError: string | null = null;
  
  // Dữ liệu học kỳ
  let hocKyList: HocKy[] = [];
  let currentSemester: HocKy | null = null;
  let isLoadingHocKy = false;
  let hocKyError: string | null = null;
  
  // Dữ liệu phòng học
  let phongHocList: PhongHoc[] = [];
  let isLoadingPhongHoc = false;
  let phongHocError: string | null = null;
  
  // Dữ liệu buổi học
  let buoiList: Buoi[] = [];
  let isLoadingBuoi = false;
  let buoiError: string | null = null;
  
  // Form dữ liệu (API mới)
  let monId: number | null = null;
  let hocKyId: number | null = null;
  let nhom: number = 1;
  let siSoToiDa: number = 50;
  let giangVienId: number | null = null;
  let loaiGiangDay: string = 'Lý thuyết'; // Đổi từ LT/TH thành tên đầy đủ
  let thuInWeek: number | null = null; // 2-8 (Thứ 2 đến Chủ nhật)
  let buoiId: number | null = null;
  let phongHocId: number | null = null;
  let ngayBatDau: string = ''; // YYYY-MM-DD format
  
  // Trạng thái form
  let isSubmitting = false;
  let formSubmitError: string | null = null;
  let successMessage: string | null = null;
  
  // Danh sách thứ trong tuần
  const daysOfWeek = [
    { value: 2, name: 'Thứ 2' },
    { value: 3, name: 'Thứ 3' },
    { value: 4, name: 'Thứ 4' },
    { value: 5, name: 'Thứ 5' },
    { value: 6, name: 'Thứ 6' },
    { value: 7, name: 'Thứ 7' },
    { value: 8, name: 'Chủ nhật' }
  ];
  
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
          giangVienId = userData.id;
          
          // Tải dữ liệu cần thiết
          await Promise.all([
            fetchMonHoc(),
            fetchHocKy(),
            fetchPhongHoc(),
            fetchBuoi()
          ]);
          
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
  
  // Lấy danh sách môn học
  async function fetchMonHoc() {
    isLoadingMonHoc = true;
    monHocError = null;
    
    try {
      const token = localStorage.getItem('jwtToken');
      const response = await fetch('http://localhost:1234/api/mon/all', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (!response.ok) {
        throw new Error(`Không thể tải danh sách môn học. Mã lỗi: ${response.status}`);
      }
      
      monHocList = await response.json();
      console.log("Danh sách môn học:", monHocList);
      
    } catch (error) {
      console.error("Lỗi khi tải danh sách môn học:", error);
      monHocError = error instanceof Error ? error.message : "Không thể tải danh sách môn học.";
    } finally {
      isLoadingMonHoc = false;
    }
  }
  
  // Lấy danh sách học kỳ
  async function fetchHocKy() {
    isLoadingHocKy = true;
    hocKyError = null;
    
    try {
      const token = localStorage.getItem('jwtToken');
      const response = await fetch('http://localhost:1234/api/semesters', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (!response.ok) {
        throw new Error(`Không thể tải danh sách học kỳ. Mã lỗi: ${response.status}`);
      }
      
      hocKyList = await response.json();
      console.log("Danh sách học kỳ:", hocKyList);
      
      // Tìm học kỳ hiện tại
      currentSemester = hocKyList.find(hk => hk.isCurrentSemester) || null;
      if (currentSemester) {
        hocKyId = currentSemester.id;
      }
      
    } catch (error) {
      console.error("Lỗi khi tải danh sách học kỳ:", error);
      hocKyError = error instanceof Error ? error.message : "Không thể tải danh sách học kỳ.";
    } finally {
      isLoadingHocKy = false;
    }
  }
  
  // Lấy danh sách phòng học
  async function fetchPhongHoc() {
    isLoadingPhongHoc = true;
    phongHocError = null;
    
    try {
      const token = localStorage.getItem('jwtToken');
      const response = await fetch('http://localhost:1234/api/phong-hoc', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (!response.ok) {
        throw new Error(`Không thể tải danh sách phòng học. Mã lỗi: ${response.status}`);
      }
      
      phongHocList = await response.json();
      console.log("Danh sách phòng học:", phongHocList);
      
    } catch (error) {
      console.error("Lỗi khi tải danh sách phòng học:", error);
      phongHocError = error instanceof Error ? error.message : "Không thể tải danh sách phòng học.";
    } finally {
      isLoadingPhongHoc = false;
    }
  }
  
  // Lấy danh sách buổi học
  async function fetchBuoi() {
    isLoadingBuoi = true;
    buoiError = null;
    
    try {
      const token = localStorage.getItem('jwtToken');
      const response = await fetch('http://localhost:1234/api/buoi', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (!response.ok) {
        throw new Error(`Không thể tải danh sách buổi học. Mã lỗi: ${response.status}`);
      }
      
      buoiList = await response.json();
      console.log("Danh sách buổi học:", buoiList);
      
    } catch (error) {
      console.error("Lỗi khi tải danh sách buổi học:", error);
      buoiError = error instanceof Error ? error.message : "Không thể tải danh sách buổi học.";
    } finally {
      isLoadingBuoi = false;
    }
  }
  
  // Đăng ký lớp tín chỉ (API mới)
  async function submitForm() {
    if (!monId || !hocKyId || !giangVienId || !thuInWeek || !buoiId || !phongHocId) {
      formSubmitError = "Vui lòng điền đầy đủ thông tin bắt buộc.";
      return;
    }
    
    isSubmitting = true;
    formSubmitError = null;
    successMessage = null;
    
    try {
      const token = localStorage.getItem('jwtToken');
      
      // Chuẩn bị request body theo API mới
      const requestBody: any = {
        monId,
        hocKyId,
        nhom,
        siSoToiDa,
        giangVienId,
        loaiGiangDay,
        thuInWeek,
        buoiId,
        phongHocId
      };
      
      // Thêm ngayBatDau nếu có
      if (ngayBatDau) {
        requestBody.ngayBatDau = ngayBatDau;
      }
      
      console.log("Request body:", requestBody);
      
      const response = await fetch('http://localhost:1234/api/lop-tin-chi', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(requestBody)
      });
      
      const data = await response.json();
      
      if (!response.ok) {
        throw new Error(data.message || `Không thể đăng ký lớp tín chỉ. Mã lỗi: ${response.status}`);
      }
      
      console.log("Kết quả đăng ký lớp:", data);
      
      // Hiển thị thông báo thành công với thông tin chi tiết
      successMessage = `Đăng ký lớp tín chỉ thành công! ${data.scheduleInfo || ''}`;
      
      // Reset form
      monId = null;
      nhom = 1;
      siSoToiDa = 50;
      loaiGiangDay = 'Lý thuyết';
      thuInWeek = null;
      buoiId = null;
      phongHocId = null;
      ngayBatDau = '';
      
    } catch (error) {
      console.error("Lỗi khi đăng ký lớp tín chỉ:", error);
      formSubmitError = error instanceof Error ? error.message : "Đã xảy ra lỗi không xác định khi đăng ký lớp tín chỉ.";
    } finally {
      isSubmitting = false;
    }
  }
  
  function toggleSidebar() {
    sidebarCollapsed = !sidebarCollapsed;
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
      <li class="sidebar-item active">
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
          <h1 class="fs-2 fw-bold">Đăng ký lớp tín chỉ</h1>
          <p class="text-muted mb-0">Tạo lớp tín chỉ mới cho học kỳ hiện tại</p>
        </div>
        
        <!-- Thông báo -->
        {#if successMessage}
          <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle-fill me-2"></i> {successMessage}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close" on:click={() => successMessage = null}></button>
          </div>
        {/if}
        
        {#if formSubmitError}
          <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2"></i> {formSubmitError}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close" on:click={() => formSubmitError = null}></button>
          </div>
        {/if}
        
        <!-- Form đăng ký lớp tín chỉ -->
        <div class="card border-0 shadow-sm mb-4">
          <div class="card-header bg-white py-3">
            <h5 class="mb-0 fw-bold">Thông tin lớp tín chỉ</h5>
          </div>
          <div class="card-body">
            <form on:submit|preventDefault={submitForm}>
              <div class="row mb-3">
                <div class="col-md-6">
                  <label for="monId" class="form-label fw-semibold">Môn học <span class="text-danger">*</span></label>
                  <select 
                    id="monId" 
                    class="form-select" 
                    bind:value={monId} 
                    required
                    disabled={isLoadingMonHoc || isSubmitting}
                  >
                    <option value={null} disabled selected>-- Chọn môn học --</option>
                    {#if isLoadingMonHoc}
                      <option disabled>Đang tải...</option>
                    {:else if monHocError}
                      <option disabled>Lỗi: {monHocError}</option>
                    {:else}
                      {#each monHocList as mon}
                        <option value={mon.id}>{mon.maMon} - {mon.tenMon} ({mon.soTinChi} tín chỉ)</option>
                      {/each}
                    {/if}
                  </select>
                </div>
                
                <div class="col-md-6">
                  <label for="hocKyId" class="form-label fw-semibold">Học kỳ <span class="text-danger">*</span></label>
                  <select 
                    id="hocKyId" 
                    class="form-select" 
                    bind:value={hocKyId} 
                    required
                    disabled={isLoadingHocKy || isSubmitting}
                  >
                    <option value={null} disabled selected>-- Chọn học kỳ --</option>
                    {#if isLoadingHocKy}
                      <option disabled>Đang tải...</option>
                    {:else if hocKyError}
                      <option disabled>Lỗi: {hocKyError}</option>
                    {:else}
                      {#each hocKyList as hocKy}
                        <option value={hocKy.id}>{hocKy.name} - Năm học {hocKy.namHoc} {hocKy.isCurrentSemester ? '(Hiện tại)' : ''}</option>
                      {/each}
                    {/if}
                  </select>
                </div>
              </div>
              
              <div class="row mb-3">
                <div class="col-md-3">
                  <label for="nhom" class="form-label fw-semibold">Nhóm <span class="text-danger">*</span></label>
                  <input 
                    type="number" 
                    id="nhom" 
                    class="form-control" 
                    bind:value={nhom} 
                    min="1" 
                    required
                    disabled={isSubmitting}
                  />
                </div>
                
                <div class="col-md-3">
                  <label for="siSoToiDa" class="form-label fw-semibold">Sĩ số tối đa <span class="text-danger">*</span></label>
                  <input 
                    type="number" 
                    id="siSoToiDa" 
                    class="form-control" 
                    bind:value={siSoToiDa} 
                    min="1" 
                    required
                    disabled={isSubmitting}
                  />
                </div>
                
                <div class="col-md-6">
                  <label for="loaiGiangDay" class="form-label fw-semibold">Loại giảng dạy <span class="text-danger">*</span></label>
                  <select 
                    id="loaiGiangDay" 
                    class="form-select" 
                    bind:value={loaiGiangDay} 
                    required
                    disabled={isSubmitting}
                  >
                    <option value="Lý thuyết">Lý thuyết</option>
                    <option value="Thực hành">Thực hành</option>
                  </select>
                </div>
              </div>
              
              <!-- Lịch học cố định theo tuần -->
              <div class="mb-3">
                <h6 class="form-label fw-semibold">Lịch học cố định <span class="text-danger">*</span></h6>
                <p class="text-muted small mb-3">Chọn một buổi học cố định, hệ thống sẽ tự động tạo lịch cho cả học kỳ</p>
                
                <div class="card border">
                  <div class="card-body">
                    <div class="row mb-3">
                      <div class="col-md-4">
                        <label for="thuInWeek" class="form-label">Thứ <span class="text-danger">*</span></label>
                        <select 
                          id="thuInWeek" 
                          class="form-select" 
                          bind:value={thuInWeek} 
                          required
                          disabled={isSubmitting}
                        >
                          <option value={null} disabled selected>-- Chọn thứ --</option>
                          {#each daysOfWeek as day}
                            <option value={day.value}>{day.name}</option>
                          {/each}
                        </select>
                      </div>
                      
                      <div class="col-md-4">
                        <label for="buoiId" class="form-label">Buổi học <span class="text-danger">*</span></label>
                        <select 
                          id="buoiId" 
                          class="form-select" 
                          bind:value={buoiId} 
                          required
                          disabled={isLoadingBuoi || isSubmitting}
                        >
                          <option value={null} disabled selected>-- Chọn buổi học --</option>
                          {#if isLoadingBuoi}
                            <option disabled>Đang tải...</option>
                          {:else if buoiError}
                            <option disabled>Lỗi: {buoiError}</option>
                          {:else}
                            {#each buoiList as buoi}
                              <option value={buoi.id}>{buoi.ten} ({buoi.startTime} - {buoi.endTime})</option>
                            {/each}
                          {/if}
                        </select>
                      </div>
                      
                      <div class="col-md-4">
                        <label for="phongHocId" class="form-label">Phòng học <span class="text-danger">*</span></label>
                        <select 
                          id="phongHocId" 
                          class="form-select" 
                          bind:value={phongHocId} 
                          required
                          disabled={isLoadingPhongHoc || isSubmitting}
                        >
                          <option value={null} disabled selected>-- Chọn phòng học --</option>
                          {#if isLoadingPhongHoc}
                            <option disabled>Đang tải...</option>
                          {:else if phongHocError}
                            <option disabled>Lỗi: {phongHocError}</option>
                          {:else}
                            {#each phongHocList as phongHoc}
                              <option value={phongHoc.id}>{phongHoc.ten}</option>
                            {/each}
                          {/if}
                        </select>
                      </div>
                    </div>
                    
                    <div class="row">
                      <div class="col-md-6">
                        <label for="ngayBatDau" class="form-label">Ngày bắt đầu <span class="text-muted">(không bắt buộc)</span></label>
                        <input 
                          type="date" 
                          id="ngayBatDau" 
                          class="form-control" 
                          bind:value={ngayBatDau}
                          disabled={isSubmitting}
                        />
                        <div class="form-text">Nếu để trống, hệ thống sẽ sử dụng ngày hiện tại hoặc ngày bắt đầu học kỳ</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="d-flex justify-content-end mt-4">
                <button 
                  type="reset" 
                  class="btn btn-light me-2" 
                  disabled={isSubmitting}
                >
                  <i class="bi bi-x-circle"></i> Hủy
                </button>
                <button 
                  type="submit" 
                  class="btn btn-primary" 
                  disabled={isSubmitting}
                >
                  {#if isSubmitting}
                    <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                    <span>Đang xử lý...</span>
                  {:else}
                    <i class="bi bi-check-circle"></i> Đăng ký lớp
                  {/if}
                </button>
              </div>
            </form>
          </div>
        </div>
      {/if}
    </div>
  </div>
</div>

<style>
  @import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css");
  
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
</style> 