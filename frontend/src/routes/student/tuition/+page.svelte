<script lang="ts">
  import { onMount } from 'svelte';
  import 'bootstrap/dist/css/bootstrap.min.css';
  import { browser } from '$app/environment';
  import { goto } from '$app/navigation';
  import StudentApiService, { type TuitionResponse, type HocPhi as ApiHocPhi, type DotDongHocPhi as ApiDotDongHocPhi, type HocKy as ApiHocKy } from '$lib/services/studentApi';

  // Frontend interfaces (keeping existing structure for compatibility)
  interface HocKy {
    id: number;
    ten: string;
    nam_hoc: number;
    thu_tu: number;
    start_date: string | null;
    end_date: string | null;
  }

  interface HocPhi {
    id: number;
    sv_id: string;
    hoc_ky_id: number;
    tong_tien: number;
    da_dong: number;
    ngay_dong: string | null;
    trang_thai: string;
    hocKy?: HocKy;
  }

  interface DotDongHocPhi {
    id: number;
    hoc_ky_id: number;
    ngay_bat_dau: string;
    ngay_ket_thuc: string;
    hocKy?: HocKy;
  }  let studentId: string | null = null;
  let tuitionHistory: HocPhi[] = [];
  let paymentPeriods: DotDongHocPhi[] = [];
  let availableSemesters: HocKy[] = [];
  let isLoading = true;
  let errorMessage = '';
  let selectedHocKyId: number | null = null;

  // Helper functions to transform API data to frontend format
  function transformApiHocKyToFrontend(apiHocKy: ApiHocKy): HocKy {
    return {
      id: apiHocKy.id,
      ten: apiHocKy.ten,
      nam_hoc: apiHocKy.namHoc,
      thu_tu: apiHocKy.thuTu,
      start_date: apiHocKy.startDate,
      end_date: apiHocKy.endDate
    };
  }

  function transformApiHocPhiToFrontend(apiHocPhi: ApiHocPhi): HocPhi {
    const hocKy: HocKy = {
      id: apiHocPhi.hocKyId,
      ten: apiHocPhi.tenHocKy,
      nam_hoc: apiHocPhi.namHocHocKy,
      thu_tu: apiHocPhi.thuTuHocKy,
      start_date: apiHocPhi.startDateHocKy,
      end_date: apiHocPhi.endDateHocKy
    };

    return {
      id: apiHocPhi.id,
      sv_id: apiHocPhi.mssv,
      hoc_ky_id: apiHocPhi.hocKyId,
      tong_tien: apiHocPhi.tongTien,
      da_dong: apiHocPhi.daDong,
      ngay_dong: apiHocPhi.ngayDong,
      trang_thai: mapStatusToVietnamese(apiHocPhi.trangThai),
      hocKy: hocKy
    };
  }

  function transformApiDotDongHocPhiToFrontend(apiDotDong: ApiDotDongHocPhi): DotDongHocPhi {
    const hocKy: HocKy = {
      id: apiDotDong.hocKyId,
      ten: apiDotDong.tenHocKy,
      nam_hoc: apiDotDong.namHocHocKy,
      thu_tu: apiDotDong.thuTuHocKy,
      start_date: apiDotDong.startDateHocKy,
      end_date: apiDotDong.endDateHocKy
    };

    return {
      id: apiDotDong.id,
      hoc_ky_id: apiDotDong.hocKyId,
      ngay_bat_dau: apiDotDong.ngayBatDau,
      ngay_ket_thuc: apiDotDong.ngayKetThuc,
      hocKy: hocKy
    };
  }

  function mapStatusToVietnamese(status: string): string {
    switch (status) {
      case 'Da dong': return 'Đã đóng';
      case 'Chua dong': return 'Chưa đóng';
      default: return status;
    }
  }
  async function loadTuitionData() {
    try {
      isLoading = true;
      errorMessage = '';
      
      // Always load all data (don't filter by semester on API level)
      const response = await StudentApiService.getTuitionData();

      // Transform API data to frontend format
      tuitionHistory = response.tuitionHistory.map(transformApiHocPhiToFrontend);
      paymentPeriods = response.paymentPeriods.map(transformApiDotDongHocPhiToFrontend);
      availableSemesters = response.availableSemesters.map(transformApiHocKyToFrontend);

      // Get student ID from first tuition record if available
      if (tuitionHistory.length > 0) {
        studentId = tuitionHistory[0].sv_id;
      }

      // Auto-select the most recent semester if none is selected
      if (!selectedHocKyId && availableSemesters.length > 0) {
        const sortedSemesters = [...availableSemesters].sort((a, b) => {
          if (b.nam_hoc !== a.nam_hoc) return b.nam_hoc - a.nam_hoc;
          return b.thu_tu - a.thu_tu;
        });
        
        // Find the most recent semester that has tuition data
        const semesterWithTuition = sortedSemesters.find(sem => 
          tuitionHistory.some(t => t.hoc_ky_id === sem.id)
        );
        
        if (semesterWithTuition) {
          selectedHocKyId = semesterWithTuition.id;
        }
      }

    } catch (error) {
      console.error("Error fetching tuition data:", error);
      errorMessage = "Failed to load tuition information. Please try again later.";
    } finally {
      isLoading = false;
    }
  }
  onMount(async () => {
    if (browser) {
      // Load tuition data from API
      await loadTuitionData();
      import('bootstrap/dist/js/bootstrap.bundle.min.js');
    }
  });

  function handleLogout() {
    if (browser) {
      localStorage.removeItem('jwtToken');
      localStorage.removeItem('userRole');
      localStorage.removeItem('userId');
      goto('/');
    }
  }

  function formatCurrency(value: number | null | undefined) {
    if (value == null) return 'N/A';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
  }

  function formatDate(dateString: string | null | undefined) {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      return new Intl.DateTimeFormat('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' }).format(date);
    } catch (e) {
      return 'Invalid Date';
    }
  }
  
  function getStatusClass(status: string) {
    if (status === 'Đã đóng') return 'text-success fw-bold';
    if (status === 'Chưa đóng') return 'text-danger fw-bold';
    if (status === 'Đang đóng') return 'text-warning fw-bold';
    return '';
  }
  $: filteredTuition = selectedHocKyId ? tuitionHistory.filter(t => t.hoc_ky_id === selectedHocKyId) : tuitionHistory;
  $: filteredPaymentPeriods = selectedHocKyId ? paymentPeriods.filter(p => p.hoc_ky_id === selectedHocKyId) : paymentPeriods;

</script>

<svelte:head>
  <title>Student Tuition Fee</title>
</svelte:head>

<!-- Navbar -->
<nav class="navbar navbar-expand-lg navbar-dark bg-primary-light">
  <div class="container-fluid">
    <a class="navbar-brand d-flex align-items-center" href="/student/dashboard">
      <img src="/images/logos/ptit-logo-inv.png" alt="PTIT Logo" style="height: 30px; width: 30px; object-fit: contain;" class="me-2">
      School Portal
    </a>
    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#studentNavbar" aria-controls="studentNavbar" aria-expanded="false" aria-label="Toggle navigation">
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="studentNavbar">
      <ul class="navbar-nav me-auto mb-2 mb-lg-0">
        <li class="nav-item">
          <a class="nav-link" href="/student/dashboard"><i class="bi bi-house-door me-1"></i>Dashboard</a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="/student/profile"><i class="bi bi-person-fill me-1"></i>Profile</a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="/student/score"><i class="bi bi-journal-text me-1"></i>Scores</a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="/student/credit-registration"><i class="bi bi-card-checklist me-1"></i>Register Classes</a>
        </li>
        <li class="nav-item">
          <a class="nav-link active" aria-current="page" href="/student/tuition"><i class="bi bi-wallet2 me-1"></i>Tuition</a>
        </li>
      </ul>
      <ul class="navbar-nav ms-auto">
        <li class="nav-item">
          <button class="nav-link btn btn-link" on:click={handleLogout}><i class="bi bi-box-arrow-right me-1"></i>Logout</button>
        </li>
      </ul>
    </div>
  </div>
</nav>

<div class="container mt-4 mb-5">
  <div class="row mb-3 align-items-center">
    <div class="col">
      <h1 class="h3"><i class="bi bi-cash-coin me-2"></i>Tuition Fee Information</h1>
    </div>
    <div class="col-auto">
      <select class="form-select form-select-sm" bind:value={selectedHocKyId} aria-label="Select semester">
        <option value={null}>All Semesters</option>
        {#each availableSemesters as hk (hk.id)}
          <option value={hk.id}>{hk.ten} - {hk.nam_hoc}</option>
        {/each}
      </select>
    </div>
  </div>

  {#if isLoading}
    <div class="text-center mt-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
      <p class="mt-2">Loading tuition data...</p>
    </div>
  {:else if errorMessage}
    <div class="alert alert-danger" role="alert">
      <i class="bi bi-exclamation-triangle-fill me-2"></i>{errorMessage}
    </div>
  {:else}
    {#if filteredTuition.length === 0 && filteredPaymentPeriods.length === 0}
        <div class="alert alert-info" role="alert">
            <i class="bi bi-info-circle-fill me-2"></i>
            No tuition information or payment periods found for {selectedHocKyId ? `the selected semester` : 'your account'}.
        </div>
    {/if}

    {#if filteredTuition.length > 0}
      <div class="card shadow-sm mb-4">
        <div class="card-header bg-light py-3">
          <h5 class="mb-0"><i class="bi bi-receipt me-2"></i>Tuition Details
            {#if selectedHocKyId && filteredTuition[0]?.hocKy}
             - {filteredTuition[0].hocKy.ten} ({filteredTuition[0].hocKy.nam_hoc})
            {/if}
          </h5>
        </div>
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-hover table-striped mb-0">
              <thead class="table-light">
                <tr>
                  <th scope="col">Semester</th>
                  <th scope="col" class="text-end">Total Due</th>
                  <th scope="col" class="text-end">Amount Paid</th>
                  <th scope="col" class="text-end">Remaining</th>
                  <th scope="col">Status</th>
                  <th scope="col">Payment Date</th>
                </tr>
              </thead>
              <tbody>
                {#each filteredTuition as item (item.id)}
                  <tr>
                    <td>{item.hocKy?.ten} - {item.hocKy?.nam_hoc}</td>
                    <td class="text-end">{formatCurrency(item.tong_tien)}</td>
                    <td class="text-end">{formatCurrency(item.da_dong)}</td>
                    <td class="text-end fw-bold">{formatCurrency(item.tong_tien - item.da_dong)}</td>
                    <td><span class={getStatusClass(item.trang_thai)}>{item.trang_thai}</span></td>
                    <td>{formatDate(item.ngay_dong)}</td>
                  </tr>
                {/each}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    {/if}

    {#if filteredPaymentPeriods.length > 0}
      <div class="card shadow-sm">
        <div class="card-header bg-light py-3">
          <h5 class="mb-0"><i class="bi bi-calendar-check me-2"></i>Payment Periods
            {#if selectedHocKyId && filteredPaymentPeriods[0]?.hocKy}
             - {filteredPaymentPeriods[0].hocKy.ten} ({filteredPaymentPeriods[0].hocKy.nam_hoc})
            {/if}
          </h5>
        </div>
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-hover table-striped mb-0">
              <thead class="table-light">
                <tr>
                  <th scope="col">Semester</th>
                  <th scope="col">Payment Start Date</th>
                  <th scope="col">Payment End Date</th>
                  <th scope="col">Status</th>
                </tr>
              </thead>
              <tbody>
                {#each filteredPaymentPeriods as period (period.id)}
                  {@const startDate = new Date(period.ngay_bat_dau)}
                  {@const endDate = new Date(period.ngay_ket_thuc)}
                  {@const today = new Date()}
                  {@const isActive = today >= startDate && today <= endDate}
                  {@const isUpcoming = today < startDate}
                  {@const isPast = today > endDate}
                  <tr>
                    <td>{period.hocKy?.ten} - {period.hocKy?.nam_hoc}</td>
                    <td>{formatDate(period.ngay_bat_dau)}</td>
                    <td>{formatDate(period.ngay_ket_thuc)}</td>
                    <td>
                      {#if isActive}
                        <span class="badge bg-success-soft text-success">Ongoing</span>
                      {:else if isUpcoming}
                        <span class="badge bg-warning-soft text-warning">Upcoming</span>
                      {:else if isPast}
                        <span class="badge bg-secondary-soft text-secondary">Ended</span>
                      {/if}
                    </td>
                  </tr>
                {/each}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    {/if}
  {/if}
</div>

<style>
  :global(body) {
    background-color: #f8f9fa;
  }

  .bg-primary-light {
    background-color: #0056b3 !important; /* PTIT Blue */
  }

  .navbar-dark .navbar-nav .nav-link {
    color: rgba(255, 255, 255, 0.85);
  }
  .navbar-dark .navbar-nav .nav-link:hover,
  .navbar-dark .navbar-nav .nav-link:focus,
  .navbar-dark .navbar-nav .nav-link.active {
    color: #fff;
  }
  
  .card-header h5 {
    font-weight: 500;
  }
  .table th {
    font-weight: 600;
    white-space: nowrap;
  }
  .table td {
    vertical-align: middle;
  }
  .badge.bg-success-soft {
    background-color: rgba(25, 135, 84, 0.15);
  }
  .badge.bg-warning-soft {
    background-color: rgba(255, 193, 7, 0.15);
  }
  .badge.bg-secondary-soft {
    background-color: rgba(108, 117, 125, 0.15);
  }
  .text-success { color: #198754 !important; }
  .text-warning { color: #ffc107 !important; }
  .text-danger { color: #dc3545 !important; }
  .text-secondary { color: #6c757d !important; }

  .form-select-sm {
    max-width: 250px; /* Adjust as needed */
  }
</style>
