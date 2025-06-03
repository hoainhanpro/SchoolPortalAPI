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
    name: "N/A",
    department: "N/A",
    position: "N/A",
    email: "N/A"
  };

  let currentWeekOffset = 0;
  let weekDateRange = "";
  let displayableScheduleDays: Array<{ name: string; classes: Array<{ time: string; subject: string; room: string; teacher: string; classCode: string }> }> = [];
  let isLoadingSchedule = true;
  let scheduleError: string | null = null;
  let scheduleDisplayMessage: string | null = null;

  const daysOrder = ["monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"];
  const dayNames = {
    "monday": "T2",
    "tuesday": "T3",
    "wednesday": "T4",
    "thursday": "T5",
    "friday": "T6",
    "saturday": "T7",
    "sunday": "CN"
  };

  let currentMonth = new Date().getMonth() + 1;
  let currentYear = new Date().getFullYear();
  let activeTab = 'schedule'; // 'schedule' hoặc 'list'
  let sidebarCollapsed = false;

  // Hàm xác định buổi học (sáng/chiều) dựa vào thời gian
  function getSessionType(time: string): string {
    // Giả sử định dạng time là "HH:MM - HH:MM"
    const startTime = time.split(' - ')[0];
    const hour = parseInt(startTime.split(':')[0]);
    
    // Nếu giờ bắt đầu < 12, coi là buổi sáng, ngược lại là buổi chiều
    return hour < 12 ? 'Sáng' : 'Chiều';
  }

  // Hàm phân loại các lớp học theo buổi
  function getClassesBySession(classes: Array<{ time: string; subject: string; room: string; teacher: string; classCode: string }>) {
    const morning: Array<{ time: string; subject: string; room: string; teacher: string; classCode: string }> = [];
    const afternoon: Array<{ time: string; subject: string; room: string; teacher: string; classCode: string }> = [];
    
    classes.forEach(cls => {
      if (getSessionType(cls.time) === 'Sáng') {
        morning.push(cls);
      } else {
        afternoon.push(cls);
      }
    });
    
    return { morning, afternoon };
  }

  // Function to calculate and format the week date range
  function updateWeekDateRange(offset: number) {
    const today = new Date();
    const currentDay = today.getDay(); // 0 (Sun) - 6 (Sat)
    const daysToMonday = (currentDay === 0 ? -6 : 1) - currentDay; // Calculate days to get to Monday

    const mondayOfCurrentWeek = new Date(today);
    mondayOfCurrentWeek.setDate(today.getDate() + daysToMonday + (offset * 7));
    
    const sundayOfCurrentWeek = new Date(mondayOfCurrentWeek);
    sundayOfCurrentWeek.setDate(mondayOfCurrentWeek.getDate() + 6);

    currentMonth = mondayOfCurrentWeek.getMonth() + 1;
    currentYear = mondayOfCurrentWeek.getFullYear();

    const options: Intl.DateTimeFormatOptions = { month: 'short', day: 'numeric' };
    const yearOption: Intl.DateTimeFormatOptions = { year: 'numeric' };

    const startStr = mondayOfCurrentWeek.toLocaleDateString('en-US', options);
    const endStr = sundayOfCurrentWeek.toLocaleDateString('en-US', options);
    const yearStr = mondayOfCurrentWeek.toLocaleDateString('en-US', yearOption);

    weekDateRange = `${startStr} - ${endStr}, ${yearStr}`;
  }

  async function fetchLecturerSchedule(offset: number) {
    isLoadingSchedule = true;
    scheduleError = null;
    scheduleDisplayMessage = null;

    if (!lecturer || !lecturer.id || !lecturer.isLecturer) {
      console.warn("Lecturer ID not available or user is not a lecturer. Cannot fetch schedule.");
      scheduleError = lecturer.isLecturer ? "Thông tin giảng viên chưa được tải." : null;
      scheduleDisplayMessage = !lecturer.isLecturer ? "Lịch dạy chỉ dành cho giảng viên." : null;
      isLoadingSchedule = false;
      updateWeekDateRange(offset);
      displayableScheduleDays = daysOrder.map(dayKey => ({
        name: dayNames[dayKey as keyof typeof dayNames],
        classes: []
      }));
      return;
    }

    try {
      const token = localStorage.getItem('jwtToken');
      const response = await fetch(`http://localhost:1234/api/schedule/lecturer/${lecturer.id}?weekOffset=${offset}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        throw new Error(`Không thể tải lịch dạy. Mã lỗi: ${response.status}`);
      }

      const dataFromApi: Record<string, Array<{ time: string; subject: string; room: string; teacher: string; classCode: string }>> = await response.json();
      currentWeekOffset = offset;
      updateWeekDateRange(currentWeekOffset);
      
      displayableScheduleDays = daysOrder.map(dayKey => ({
        name: dayNames[dayKey as keyof typeof dayNames],
        classes: dataFromApi[dayKey.toLowerCase()] || []
      }));

    } catch (error) {
      console.error("Lỗi khi tải lịch dạy:", error);
      scheduleError = error instanceof Error ? error.message : "Đã xảy ra lỗi không xác định khi tải lịch dạy.";
      displayableScheduleDays = daysOrder.map(dayKey => ({
        name: dayNames[dayKey as keyof typeof dayNames],
        classes: []
      }));
    } finally {
      isLoadingSchedule = false;
    }
  }

  function previousWeek() {
    fetchLecturerSchedule(currentWeekOffset - 1);
  }

  function nextWeek() {
    fetchLecturerSchedule(currentWeekOffset + 1);
  }

  function setActiveTab(tab: string) {
    activeTab = tab;
  }

  function toggleSidebar() {
    sidebarCollapsed = !sidebarCollapsed;
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
            
            // Cập nhật thông tin giảng viên
            lecturerInfo = {
              id: lecturer.id,
              name: `${lecturer.ho} ${lecturer.ten}`,
              department: lecturer.khoaTen,
              position: lecturer.chucVu,
              email: lecturer.email
            };
  
            // Tải lịch dạy
            fetchLecturerSchedule(0);
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

    // Cleanup function
    return () => {
      // Cleanup if needed
    };
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
      <li class="sidebar-item active">
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
    <!-- Main Content -->
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
          <h1 class="h3 mb-1">Lịch Dạy</h1>
          <p class="text-muted">Xem và quản lý lịch dạy của bạn.</p>
        </div>
        
        <ul class="nav nav-tabs mb-4">
          <li class="nav-item">
            <button type="button" class="nav-link {activeTab === 'schedule' ? 'active' : ''}" on:click={() => setActiveTab('schedule')}>Lịch</button>
          </li>
          <li class="nav-item">
            <button type="button" class="nav-link {activeTab === 'list' ? 'active' : ''}" on:click={() => setActiveTab('list')}>Danh sách</button>
          </li>
        </ul>
        
        {#if activeTab === 'schedule'}
          <div class="card shadow-sm mb-4">
            <div class="card-header bg-primary-light text-white d-flex justify-content-between align-items-center">
              <h5 class="mb-0 fs-6">
                <i class="bi bi-calendar-week-fill me-2"></i>
                Lịch dạy tháng {currentMonth}/{currentYear}
              </h5>
              <div class="week-navigation">
                <button class="btn btn-sm btn-outline-light me-2" on:click={previousWeek} disabled={isLoadingSchedule}>
                  <i class="bi bi-arrow-left-circle-fill"></i> Trước
                </button>
                <span class="fs-7">{weekDateRange}</span>
                <button class="btn btn-sm btn-outline-light ms-2" on:click={nextWeek} disabled={isLoadingSchedule}>
                  Sau <i class="bi bi-arrow-right-circle-fill"></i>
                </button>
              </div>
            </div>
            <div class="card-body p-0">
              {#if isLoadingSchedule}
                <div class="text-center p-4">
                  <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Đang tải lịch...</span>
                  </div>
                  <p class="mt-2">Đang tải lịch dạy...</p>
                </div>
              {:else if scheduleError}
                <div class="alert alert-danger m-3" role="alert">
                  Lỗi khi tải lịch dạy: {scheduleError}
                </div>
              {:else}
                <div class="table-responsive">
                  <table class="table table-bordered mb-0">
                    <thead class="bg-light">
                      <tr>
                        <th class="session-column text-center align-middle" rowspan="2">Buổi</th>
                        {#each displayableScheduleDays as day}
                          <th class="text-center">{day.name}</th>
                        {/each}
                      </tr>
                    </thead>
                    <tbody>
                      <!-- Buổi sáng -->
                      <tr>
                        <th class="session-header bg-morning text-center align-middle">Sáng</th>
                        {#each displayableScheduleDays as day}
                          <td class="p-1" style="vertical-align: top; min-width: 150px;">
                            {#if getClassesBySession(day.classes).morning.length > 0}
                              {#each getClassesBySession(day.classes).morning as cls}
                                <div class="class-item mb-2 p-2 rounded bg-light-blue">
                                  <div class="class-code fw-bold">{cls.classCode}</div>
                                  <div class="class-subject">{cls.subject}</div>
                                  <div class="class-time small"><i class="bi bi-clock me-1"></i>{cls.time}</div>
                                  <div class="class-room small"><i class="bi bi-building me-1"></i>{cls.room}</div>
                                </div>
                              {/each}
                            {:else}
                              <div class="text-center text-muted p-2 small">Không có lớp</div>
                            {/if}
                          </td>
                        {/each}
                      </tr>
                      <!-- Buổi chiều -->
                      <tr>
                        <th class="session-header bg-afternoon text-center align-middle">Chiều</th>
                        {#each displayableScheduleDays as day}
                          <td class="p-1" style="vertical-align: top; min-width: 150px;">
                            {#if getClassesBySession(day.classes).afternoon.length > 0}
                              {#each getClassesBySession(day.classes).afternoon as cls}
                                <div class="class-item mb-2 p-2 rounded bg-light-blue">
                                  <div class="class-code fw-bold">{cls.classCode}</div>
                                  <div class="class-subject">{cls.subject}</div>
                                  <div class="class-time small"><i class="bi bi-clock me-1"></i>{cls.time}</div>
                                  <div class="class-room small"><i class="bi bi-building me-1"></i>{cls.room}</div>
                                </div>
                              {/each}
                            {:else}
                              <div class="text-center text-muted p-2 small">Không có lớp</div>
                            {/if}
                          </td>
                        {/each}
                      </tr>
                    </tbody>
                  </table>
                </div>
              {/if}
            </div>
          </div>
        {:else if activeTab === 'list'}
          <div class="card shadow-sm">
            <div class="card-header bg-primary-light text-white">
              <h5 class="mb-0 fs-6"><i class="bi bi-list-columns-reverse me-2"></i>Danh sách lớp dạy</h5>
            </div>
            <div class="card-body">
              {#if isLoadingSchedule}
                <p class="text-center p-3">
                  <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                  Đang tải danh sách lớp...
                </p>
              {:else if scheduleError}
                <div class="alert alert-danger" role="alert">
                  Lỗi khi tải danh sách lớp: {scheduleError}
                </div>
              {:else}
                <div class="table-responsive">
                  <table class="table table-hover">
                    <thead>
                      <tr>
                        <th>Mã lớp</th>
                        <th>Môn học</th>
                        <th>Buổi</th>
                        <th>Thời gian</th>
                        <th>Phòng</th>
                        <th>Thứ</th>
                      </tr>
                    </thead>
                    <tbody>
                      {#each displayableScheduleDays as day}
                        {#each day.classes as cls}
                          <tr>
                            <td>{cls.classCode}</td>
                            <td>{cls.subject}</td>
                            <td><span class="badge bg-primary-light">{getSessionType(cls.time)}</span></td>
                            <td>{cls.time}</td>
                            <td>{cls.room}</td>
                            <td>{day.name}</td>
                          </tr>
                        {/each}
                      {/each}
                    </tbody>
                  </table>
                </div>
                
                {#if displayableScheduleDays.every(day => day.classes.length === 0)}
                  <div class="alert alert-info text-center" role="alert">
                    Không có lớp nào trong lịch dạy tuần này.
                  </div>
                {/if}
              {/if}
            </div>
          </div>
        {/if}
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
  
  /* Các style khác */
  .nav-tabs .nav-link.active {
    font-weight: 500;
    color: #0056b3;
  }
  
  .nav-tabs .nav-link {
    color: #495057;
  }
  
  .week-navigation {
    font-size: 0.9rem;
  }
  
  .class-item {
    font-size: 0.85rem;
    border-left: 3px solid #0056b3;
  }
  
  .bg-primary-light {
    background-color: #0056b3 !important;
  }
  
  
  .btn-outline-light {
    color: #f8f9fa !important;
    border-color: #f8f9fa !important;
  }
  
  .btn-outline-light:hover {
    color: #0056b3 !important;
    background-color: #f8f9fa !important;
  }
  
  .bg-light-blue {
    background-color: #e6f0ff;
  }
  
  .fs-7 {
    font-size: 0.875rem;
  }
  
  .badge.bg-primary-light {
    background-color: #0056b3 !important;
    color: white;
    font-weight: normal;
    font-size: 0.75rem;
  }
  
  .session-column {
    width: 80px;
    background-color: #f0f0f0;
  }
  
  .session-header {
    width: 80px;
    color: white;
    font-weight: 500;
  }
  
  .bg-morning {
    background-color: #4a89dc;
  }
  
  .bg-afternoon {
    background-color: #e9573f;
  }
  
  /* Thiết lập chiều cao cố định cho các ô trong bảng thời khóa biểu */
  .table-responsive tbody tr td {
    height: 150px;
    vertical-align: top;
  }
  
  /* Đảm bảo nội dung không tràn ra ngoài khi có nhiều lớp học */
  .table-responsive tbody tr td {
    overflow-y: auto;
    min-width: 150px;
  }
  
  /* Cân chỉnh chiều cao của hàng khi không có lớp */
  .text-center.text-muted.p-2.small {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
</style> 