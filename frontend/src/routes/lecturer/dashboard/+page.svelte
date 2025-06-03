<script lang="ts">
  import { onMount, tick } from 'svelte';
  import 'bootstrap/dist/css/bootstrap.min.css';
  import { browser } from '$app/environment';
  import { goto } from '$app/navigation';

  interface UpcomingClass {
    id: string;
    name: string;
    time: string;
    day: string;
    room: string;
  }

  interface RecentAnnouncement {
    id: number;
    user: {
      name: string;
      avatar: string; // Hoặc một kiểu dữ liệu khác nếu bạn không dùng avatar URL
    };
    action: string;
    time: string;
  }

  let userData: any = null;
  let isLoadingUserData = true;
  let userDataError: string | null = null;
  let sidebarCollapsed = false;
  
  // Thông tin học kỳ hiện tại
  let currentSemester = {
    id: null,
    name: "",
    namHoc: null,
    thuTu: null,
    startDate: null,
    endDate: null,
    isCurrentSemester: false
  };
  let isLoadingSemester = true;
  let semesterError: string | null = null;
  
  // Thông tin lecturer
  let lecturer = {
    id: null,
    name: "N/A",
    department: "N/A",
    position: "N/A",
    email: "N/A",
    classes: 0,
    students: 0,
    thisWeekClasses: 0,
  };
  
  // Trạng thái tải dữ liệu
  let isLoadingStudentCount = false;
  let studentCountError: string | null = null;
  
  // Thêm trạng thái tải dữ liệu cho số lượng lớp
  let isLoadingClassCount = false;
  let classCountError: string | null = null;
  
  // Thêm trạng thái tải dữ liệu cho lớp cố vấn
  let advisoryClasses: any[] = [];
  let isLoadingAdvisoryClasses = false;
  let advisoryClassesError: string | null = null;
  
  // Ngày hiện tại
  let currentDate = new Date();
  let formattedDate = formatDate(currentDate);
  
  // Lịch dạy sắp tới
  let upcomingClasses: UpcomingClass[] = [];
  let isLoadingClasses = true;
  let classesError: string | null = null;
  
  // Thông báo gần đây
  let recentAnnouncements: RecentAnnouncement[] = [];
  let isLoadingAnnouncements  = true;
  let announcementsError: string | null = null;
  
  function formatDate(date: Date) {
    const days = ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy'];
    const day = days[date.getDay()];
    const dayNum = date.getDate();
    const month = date.getMonth() + 1;
    const year = date.getFullYear();
    
    return `${day}, ${dayNum} tháng ${month}, ${year}`;
  }
  
  async function fetchLecturerData() {
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
          // Cập nhật thông tin giảng viên
          lecturer = {
            id: userData.id,
            name: `${userData.ho || ''} ${userData.ten || ''}`,
            department: userData.khoaTen || 'N/A',
            position: userData.chucVu || 'N/A',
            email: userData.email || 'N/A',
            classes: 0, // Sẽ được cập nhật từ API sau
            students: 0, // Sẽ được cập nhật từ API sau
            thisWeekClasses: 0, // Sẽ được cập nhật từ API sau
          };
          
          // Tải thông tin học kỳ trước
          await fetchCurrentSemester();
          
          // Sau khi có thông tin học kỳ và giảng viên, tải dữ liệu khác
          await Promise.all([
            fetchUpcomingClasses(),
            fetchRecentAnnouncements(),
            fetchStudentCount(),
            fetchClassCount(),
            fetchAdvisoryClasses() // Thêm hàm fetch lớp cố vấn
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
  
  async function fetchCurrentSemester() {
    isLoadingSemester = true;
    semesterError = null;
    
    try {
      const token = localStorage.getItem('jwtToken');
      const response = await fetch('http://localhost:1234/api/semesters/current', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (!response.ok) {
        throw new Error(`Không thể tải thông tin học kỳ. Mã lỗi: ${response.status}`);
      }
      
      const data = await response.json();
      currentSemester = {
        id: data.id,
        name: `${data.name} ${data.namHoc}`,
        namHoc: data.namHoc,
        thuTu: data.thuTu,
        startDate: data.startDate,
        endDate: data.endDate,
        isCurrentSemester: data.currentSemester
      } as any;
      
      console.log("Học kỳ hiện tại:", currentSemester);
      
    } catch (error) {
      console.error("Lỗi khi tải thông tin học kỳ:", error);
      semesterError = error instanceof Error ? error.message : "Không thể tải thông tin học kỳ.";
      
      // Sử dụng giá trị mặc định nếu có lỗi
      currentSemester = {
        id: 1,
        name: "Học kỳ mặc định",
        namHoc: new Date().getFullYear(),
        thuTu: 1,
        startDate: null,
        endDate: null,
        isCurrentSemester: false
      } as any;
    } finally {
      isLoadingSemester = false;
    }
  }
  
  async function fetchStudentCount() {
    isLoadingStudentCount = true;
    studentCountError = null;
    
    try {
      if (!lecturer.id || !currentSemester.id) {
        throw new Error("ID giảng viên hoặc học kỳ không hợp lệ");
      }
      
      const token = localStorage.getItem('jwtToken');
      const response = await fetch(`http://localhost:1234/api/lecturers/${lecturer.id}/students/count?semesterId=${currentSemester.id}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (!response.ok) {
        throw new Error(`Không thể tải số lượng sinh viên. Mã lỗi: ${response.status}`);
      }
      
      const data = await response.json();
      lecturer.students = data.studentCount || 0;
      console.log("Số lượng sinh viên:", lecturer.students);
      
    } catch (error) {
      console.error("Lỗi khi tải số lượng sinh viên:", error);
      studentCountError = error instanceof Error ? error.message : "Không thể tải số lượng sinh viên.";
      lecturer.students = 0; // Đặt giá trị mặc định khi lỗi
    } finally {
      isLoadingStudentCount = false;
    }
  }
  
  async function fetchClassCount() {
    isLoadingClassCount = true;
    classCountError = null;
    
    try {
      if (!lecturer.id || !currentSemester.id) {
        throw new Error("ID giảng viên hoặc học kỳ không hợp lệ");
      }
      
      const token = localStorage.getItem('jwtToken');
      const response = await fetch(`http://localhost:1234/api/lecturers/${lecturer.id}/classes/count?semesterId=${currentSemester.id}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (!response.ok) {
        throw new Error(`Không thể tải số lượng lớp tín chỉ. Mã lỗi: ${response.status}`);
      }
      
      const data = await response.json();
      lecturer.classes = data.classCount || 0;
      console.log("Số lượng lớp tín chỉ:", lecturer.classes);
      
    } catch (error) {
      console.error("Lỗi khi tải số lượng lớp tín chỉ:", error);
      classCountError = error instanceof Error ? error.message : "Không thể tải số lượng lớp tín chỉ.";
      lecturer.classes = 0; // Đặt giá trị mặc định khi lỗi
    } finally {
      isLoadingClassCount = false;
    }
  }
  
  async function fetchAdvisoryClasses() {
    isLoadingAdvisoryClasses = true;
    advisoryClassesError = null;
    
    try {
      if (!lecturer.id) {
        throw new Error("ID giảng viên không hợp lệ");
      }
      
      const token = localStorage.getItem('jwtToken');
      const response = await fetch(`http://localhost:1234/api/covan/lop/namhoc-hientai`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (!response.ok) {
        throw new Error(`Không thể tải danh sách lớp cố vấn. Mã lỗi: ${response.status}`);
      }
      
      advisoryClasses = await response.json();
      console.log("Danh sách lớp cố vấn:", advisoryClasses);
      
    } catch (error) {
      console.error("Lỗi khi tải danh sách lớp cố vấn:", error);
      advisoryClassesError = error instanceof Error ? error.message : "Không thể tải danh sách lớp cố vấn.";
      advisoryClasses = []; // Đặt mảng rỗng khi có lỗi
    } finally {
      isLoadingAdvisoryClasses = false;
    }
  }
  
  async function fetchUpcomingClasses() {
    isLoadingClasses = true;
    classesError = null;
    upcomingClasses = []; // Xóa dữ liệu giả lập
    
    try {
      if (!lecturer.id) {
        throw new Error("ID giảng viên không hợp lệ");
      }
      
      // Gọi API mới để lấy lịch dạy trong 3 ngày tới
      const token = localStorage.getItem('jwtToken');
      const response = await fetch(`http://localhost:1234/api/schedule/lecturer/${lecturer.id}/upcoming`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (!response.ok) {
        throw new Error(`Không thể tải lịch dạy sắp tới. Mã lỗi: ${response.status}`);
      }
      
      upcomingClasses = await response.json();
      console.log("Lịch dạy sắp tới:", upcomingClasses);
      
      // Số lượng lớp học sắp tới
      lecturer.thisWeekClasses = upcomingClasses.length || 0;
      
    } catch (error) {
      console.error("Lỗi khi tải lịch dạy sắp tới:", error);
      classesError = error instanceof Error ? error.message : "Không thể tải lịch dạy sắp tới.";
    } finally {
      isLoadingClasses = false;
    }
  }
  
  async function fetchRecentAnnouncements() {
    isLoadingAnnouncements = true;
    announcementsError = null;
    recentAnnouncements = []; // Xóa dữ liệu giả lập
    
    try {
      // TODO: Gọi API để lấy thông báo
      
      // Giả lập thời gian tải
      await new Promise(resolve => setTimeout(resolve, 700));
      // Tương tự như fetchUpcomingClasses, xử lý isLoadingAnnouncements và announcementsError
    } catch (error) {
      console.error("Lỗi khi tải thông báo gần đây:", error);
      announcementsError = "Không thể tải thông báo gần đây.";
    } finally {
      isLoadingAnnouncements = false;
    }
  }
  
  function toggleSidebar() {
    sidebarCollapsed = !sidebarCollapsed;
  }

  onMount(() => {
    if (browser) {
      import('bootstrap/dist/js/bootstrap.bundle.min.js');
      fetchLecturerData();
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
      <button 
        class="btn toggle-btn" 
        on:click={toggleSidebar}
        aria-label={sidebarCollapsed ? "Mở rộng thanh bên" : "Thu gọn thanh bên"}>
        <i class="bi {sidebarCollapsed ? 'bi-chevron-right' : 'bi-chevron-left'}"></i>
      </button>
    </div>
    
    <!-- Menu items -->
    <ul class="sidebar-menu">
      <li class="sidebar-item active">
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
        <!-- Greeting section -->
        <div class="welcome-section mb-4">
          <h1 class="fs-2 fw-bold">Xin chào, {lecturer.name}</h1>
          <p class="text-muted mb-2">{formattedDate}</p>
          <p>Chào mừng bạn đến với hệ thống quản lý dành cho giảng viên.</p>
        </div>
        
        <!-- Stats cards -->
        <div class="row mb-4">
          <div class="col-md-6 mb-3">
            <div class="card border-0 shadow-sm h-100 stats-card">
              <div class="card-body p-3 d-flex flex-column justify-content-between">
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <h5 class="mb-1">Lớp đang dạy</h5>
                    {#if isLoadingClassCount}
                      <div class="placeholder-glow">
                        <span class="placeholder col-5"></span>
                      </div>
                    {:else if classCountError}
                      <h2 class="mb-0 fw-bold text-danger">Lỗi!</h2>
                    {:else}
                      <h2 class="mb-0 fw-bold">{lecturer.classes}</h2>
                    {/if}
                    <p class="text-muted small mb-0">{currentSemester.name}</p>
                  </div>
                  <div class="stats-icon bg-primary-subtle text-primary">
                    <i class="bi bi-journal-text"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="col-md-6 mb-3">
            <div class="card border-0 shadow-sm h-100 stats-card">
              <div class="card-body p-3 d-flex flex-column justify-content-between">
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <h5 class="mb-1">Sinh viên</h5>
                    {#if isLoadingStudentCount}
                      <div class="placeholder-glow">
                        <span class="placeholder col-5"></span>
                      </div>
                    {:else if studentCountError}
                      <h2 class="mb-0 fw-bold text-danger">Lỗi!</h2>
                    {:else}
                      <h2 class="mb-0 fw-bold">{lecturer.students}</h2>
                    {/if}
                    <p class="text-muted small mb-0">Sinh viên đang theo học</p>
                  </div>
                  <div class="stats-icon bg-success-subtle text-success">
                    <i class="bi bi-people-fill"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- Two column layout -->
        <div class="row">
          <!-- Upcoming classes -->
          <div class="col-lg-6 mb-4">
            <div class="card border-0 shadow-sm">
              <div class="card-header bg-white border-bottom-0 py-3">
                <h5 class="mb-0 fw-bold">Lịch dạy sắp tới</h5>
                <p class="text-muted small mb-0">Các lớp học trong 3 ngày tới</p>
              </div>
              <div class="card-body pt-0">
                {#if isLoadingClasses}
                  <div class="text-center py-4">
                    <div class="spinner-border text-primary spinner-border-sm" role="status">
                      <span class="visually-hidden">Đang tải...</span>
                    </div>
                    <p class="text-muted small mb-0 mt-2">Đang tải lịch dạy...</p>
                  </div>
                {:else if classesError}
                  <div class="alert alert-warning" role="alert">
                    {classesError}
                  </div>
                {:else if upcomingClasses.length === 0}
                  <div class="text-center py-4">
                    <i class="bi bi-calendar-x text-muted" style="font-size: 2rem;"></i>
                    <p class="text-muted mt-2">Không có lớp học nào trong 3 ngày tới</p>
                  </div>
                {:else}
                  <div class="upcoming-classes-list">
                    {#each upcomingClasses as cls}
                      <div class="upcoming-class-item p-3 mb-3 border rounded">
                        <div class="d-flex align-items-center">
                          <div class="class-time-icon me-3">
                            <i class="bi bi-clock-fill"></i>
                          </div>
                          <div class="flex-grow-1">
                            <h6 class="mb-1 fw-bold">{cls.name}</h6>
                            <div class="row">
                              <div class="col-md-4">
                                <p class="mb-1 small"><i class="bi bi-journal-code me-1"></i> {cls.id}</p>
                              </div>
                              <div class="col-md-4">
                                <p class="mb-1 small"><i class="bi bi-clock me-1"></i> {cls.time}</p>
                              </div>
                              <div class="col-md-4">
                                <p class="mb-1 small"><i class="bi bi-building me-1"></i> {cls.room}</p>
                              </div>
                            </div>
                            <p class="mb-0 text-primary small"><i class="bi bi-calendar-day me-1"></i> {cls.day}</p>
                          </div>
                        </div>
                      </div>
                    {/each}
                  </div>
                {/if}
              </div>
            </div>
          </div>
          
          <!-- Danh sách lớp cố vấn -->
          <div class="col-lg-6 mb-4">
            <div class="card border-0 shadow-sm">
              <div class="card-header bg-white border-bottom-0 py-3">
                <h5 class="mb-0 fw-bold">Lớp cố vấn</h5>
                <p class="text-muted small mb-0">Các lớp bạn đang làm cố vấn trong năm học hiện tại</p>
              </div>
              <div class="card-body pt-0">
                {#if isLoadingAdvisoryClasses}
                  <div class="text-center py-4">
                    <div class="spinner-border text-primary spinner-border-sm" role="status">
                      <span class="visually-hidden">Đang tải...</span>
                    </div>
                    <p class="text-muted small mb-0 mt-2">Đang tải danh sách lớp cố vấn...</p>
                  </div>
                {:else if advisoryClassesError}
                  <div class="alert alert-warning" role="alert">
                    {advisoryClassesError}
                  </div>
                {:else if advisoryClasses.length === 0}
                  <div class="text-center py-4">
                    <i class="bi bi-people text-muted" style="font-size: 2rem;"></i>
                    <p class="text-muted mt-2">Bạn hiện không phụ trách cố vấn lớp nào</p>
                  </div>
                {:else}
                  <div class="advisory-classes-list">
                    {#each advisoryClasses as cls}
                      <div class="advisory-class-item p-3 mb-3 border rounded">
                        <div class="d-flex align-items-center">
                          <div class="advisory-class-icon me-3">
                            <i class="bi bi-people-fill"></i>
                          </div>
                          <div class="flex-grow-1">
                            <h6 class="mb-1 fw-bold">{cls.ten}</h6>
                            <p class="mb-1 small"><i class="bi bi-building me-1"></i> Khoa: {cls.khoa ? cls.khoa.ten : 'N/A'}</p>
                            <p class="mb-0 text-success small"><i class="bi bi-person-badge me-1"></i> Mã lớp: {cls.id}</p>
                          </div>
                        </div>
                      </div>
                    {/each}
                  </div>
                {/if}
              </div>
            </div>
          </div>
          
          <!-- Recent announcements -->
          <div class="col-12 mb-4">
            <div class="card border-0 shadow-sm">
              <div class="card-header bg-white border-bottom-0 py-3">
                <h5 class="mb-0 fw-bold">Thông báo gần đây</h5>
                <p class="text-muted small mb-0">Các thông báo trong 7 ngày qua</p>
              </div>
              <div class="card-body pt-0">
                {#if isLoadingAnnouncements}
                  <div class="text-center py-4">
                    <div class="spinner-border text-primary spinner-border-sm" role="status">
                      <span class="visually-hidden">Đang tải...</span>
                    </div>
                    <p class="text-muted small mb-0 mt-2">Đang tải thông báo...</p>
                  </div>
                {:else if announcementsError}
                  <div class="alert alert-warning" role="alert">
                    {announcementsError}
                  </div>
                {:else if recentAnnouncements.length === 0}
                  <div class="text-center py-4">
                    <i class="bi bi-clock-history text-muted" style="font-size: 2rem;"></i>
                    <p class="text-muted mt-2">Không có thông báo nào trong 7 ngày qua</p>
                  </div>
                {:else}
                  <div class="announcements-list">
                    {#each recentAnnouncements as announcement}
                      <div class="announcement-item d-flex p-3 border-bottom">
                        <div class="announcement-avatar me-3">
                          <div class="avatar-placeholder">
                            <i class="bi bi-person-fill"></i>
                          </div>
                        </div>
                        <div class="activity-content">
                          <h6 class="mb-1 fw-bold">{announcement.user.name}</h6>
                          <p class="mb-1">{announcement.action}</p>
                          <p class="text-muted small mb-0">{announcement.time}</p>
                        </div>
                      </div>
                    {/each}
                  </div>
                {/if}
              </div>
            </div>
          </div>
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
  
  /* Card and content styles */
  .card {
    border-radius: 10px;
    overflow: hidden;
  }
  
  .card-header {
    padding: 15px 20px;
  }
  
  /* Stats cards styles */
  .stats-card {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05) !important;
    transition: transform 0.3s ease;
  }
  
  .stats-card:hover {
    transform: translateY(-5px);
  }
  
  .stats-icon {
    width: 55px;
    height: 55px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.7rem;
  }
  
  .upcoming-class-item {
    background-color: #f8f9fa;
    transition: all 0.2s ease;
    border-radius: 8px;
  }
  
  .upcoming-class-item:hover {
    background-color: #e9ecef;
  }
  
  .class-time-icon {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background-color: #e6f0ff;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #0056b3;
    font-size: 1.2rem;
  }
  
  .avatar-placeholder {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background-color: #e6f0ff;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #0056b3;
    font-size: 1.2rem;
  }
  
  .announcement-item {
    transition: all 0.2s ease;
  }
  
  .announcement-item:hover {
    background-color: #f8f9fa;
  }
  
  .welcome-section h1 {
    color: #212529;
  }
  
  /* Thêm CSS cho phần Advisory Classes */
  .advisory-class-item {
    background-color: #f8f9fa;
    transition: all 0.2s ease;
    border-radius: 8px;
  }
  
  .advisory-class-item:hover {
    background-color: #e9ecef;
  }
  
  .advisory-class-icon {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background-color: #d1e7dd;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #198754;
    font-size: 1.2rem;
  }
  
  @import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css");
</style> 