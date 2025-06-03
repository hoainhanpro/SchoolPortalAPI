<script lang="ts">
  import { onMount } from 'svelte';
  import 'bootstrap/dist/css/bootstrap.min.css';
  import { browser } from '$app/environment';

  onMount(() => {
    if (browser) {
      import('bootstrap/dist/js/bootstrap.bundle.min.js');
    }
    // Load initial data when component mounts
    fetchAvailableClasses();
    fetchRegisteredClasses();
  });

  // --- API Interfaces ---
  interface AvailableCreditClass {
    daDangKy: any;
    giangVien: any;
    lopTinChiId: number;
    id: number; // LopTinChi ID
    monTen: string;
    monSoTinChi: number;
    nhom: number;
    siSoToiDa: number;
    currentRegistrations: number;
    giangVienDisplay: string;
  }

  interface RegisteredCreditClass {
    nhom: any;
    giangVien: any;
    dangKyId: number;
    lopTinChiId: number;
    monTen: string;
    monSoTinChi: number;
    nhomLTC: number;
    tenHK: string;
    namHoc: number;
    trangThaiDangKy: string;
    ngayDangKy: string; 
    giangVienDisplay: string;
  }

  // --- State Variables ---
  let availableClasses: AvailableCreditClass[] = [];
  let registeredClasses: RegisteredCreditClass[] = [];
  let searchTerm = '';
  let isLoadingAvailable = false;
  let isLoadingRegistered = false;
  let errorAvailable: string | null = null;
  let errorRegistered: string | null = null;
  let generalError: string | null = null; // For register/unregister actions
  
  // Default HocKyId, assuming 'HK231' corresponds to an integer ID.
  // In a real app, this would come from a user selection or another source.
  // let selectedHocKyId: number = 231; // This line is now removed or commented out


  // --- Utility Functions ---
  // Placeholder for getting JWT token
  function getToken(): string | null {
    if (browser) {
      return localStorage.getItem('jwtToken'); // Example: retrieve token from localStorage
    }
    return null;
  }

  // --- API Call Functions ---
  async function fetchAvailableClasses() {
    isLoadingAvailable = true;
    errorAvailable = null;
    const token = getToken();
    if (!token) {
      errorAvailable = "Authentication token not found. Please log in.";
      isLoadingAvailable = false;
      return;
    }

    try {
      // Removed hocKyId query parameter
      const response = await fetch(`http://localhost:1234/api/student/credit-classes/available`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: response.statusText }));
        throw new Error(errorData.message || `Failed to fetch available classes: ${response.status}`);
      }
      availableClasses = await response.json();
      console.log("Available Classes:", availableClasses); // Debugging log
    } catch (err: any) {
      errorAvailable = err.message;
      availableClasses = []; // Clear previous data on error
    } finally {
      isLoadingAvailable = false;
    }
  }

  async function fetchRegisteredClasses() {
    isLoadingRegistered = true;
    errorRegistered = null;
    const token = getToken();
    if (!token) {
      errorRegistered = "Authentication token not found. Please log in.";
      isLoadingRegistered = false;
      return;
    }

    try {
      // Fetching registered classes without hocKyId
      const url = 'http://localhost:1234/api/student/credit-classes/registered';
      const response = await fetch(url, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: response.statusText }));
        throw new Error(errorData.message || `Failed to fetch registered classes: ${response.status}`);
      }
      registeredClasses = await response.json();
    } catch (err: any) {
      errorRegistered = err.message;
      registeredClasses = []; // Clear previous data on error
    } finally {
      isLoadingRegistered = false;
    }
  }

  async function handleRegister(lopTinChiId: number) {
    generalError = null;
    const token = getToken();
    if (!token) {
      generalError = "Authentication token not found. Please log in.";
      return;
    }

    try {
      const response = await fetch('http://localhost:1234/api/student/credit-classes/register', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ lopTinChiId: lopTinChiId })
      });
      if (!response.ok) {
        const errorText = await response.text(); // Read error as text
        throw new Error(errorText || `Failed to register: ${response.status} ${response.statusText}`);
      }
      // Refresh both lists after successful registration
      await fetchAvailableClasses();
      await fetchRegisteredClasses();
      // Optionally, display a success message from response.text() or a generic one
      alert("Đăng ký lớp tín chỉ thành công."); 
    } catch (err: any) {
      generalError = err.message;
      alert(`Error: ${err.message}`); // Show error in an alert for now
    }
  }

  async function handleUnregister(dangKyId: number) {
    generalError = null;
    const token = getToken();
    if (!token) {
      generalError = "Authentication token not found. Please log in.";
      return;
    }

    try {
      const response = await fetch(`http://localhost:1234/api/student/credit-classes/register?dangKyId=${dangKyId}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (!response.ok) {
        const errorText = await response.text(); // Read error as text
        throw new Error(errorText || `Failed to unregister: ${response.status} ${response.statusText}`);
      }
      // Refresh both lists after successful unregistration
      await fetchAvailableClasses();
      await fetchRegisteredClasses();
      alert("Hủy đăng ký lớp tín chỉ thành công.");
    } catch (err: any) {
      generalError = err.message;
      alert(`Error: ${err.message}`); // Show error in an alert for now
    }
  }
  
  // --- Reactive Statements ---
  $: filteredAvailableClasses = availableClasses.filter(classItem => {
    const term = searchTerm.toLowerCase();
    // Ensure properties exist and provide fallbacks to prevent errors during filtering
    const monTenMatches = (classItem.monTen || '').toLowerCase().includes(term);
    const nhomMatches = (classItem.nhom?.toString() || '').includes(term); // nhom is a number
    const idMatches = (classItem.id?.toString() || '').toLowerCase().includes(term); // id is a number, .toLowerCase() for consistency if it could be alphanumeric

    return monTenMatches || nhomMatches || idMatches;
  });

</script>

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
          <a class="nav-link active" href="/student/credit-registration"><i class="bi bi-card-checklist me-1"></i>Register Classes</a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="/student/tuition"><i class="bi bi-wallet2 me-1"></i>Tuition</a>
        </li>
      </ul>
      <ul class="navbar-nav ms-auto">
        <li class="nav-item">
          <a class="nav-link" href="/"><i class="bi bi-box-arrow-right me-1"></i>Logout</a>
        </li>
      </ul>
    </div>
  </div>
</nav>

<div class="container mt-4 mb-4">
  <div class="mb-4">
    <input type="text" class="form-control" placeholder="Search Available Classes by Subject, Group, or ID..." bind:value={searchTerm}>
  </div>

  {#if generalError}
    <div class="alert alert-danger" role="alert">
      {generalError}
    </div>
  {/if}

  <!-- Available Credit Classes Section -->
  <div class="card shadow-sm mb-4">
    <div class="card-header bg-primary-light text-white">
      <h2 class="h5 mb-0">Available Credit Classes (Current Semester)</h2>
    </div>
    <div class="card-body p-0" style="max-height: 400px; overflow-y: auto;">
      {#if isLoadingAvailable}
        <p class="text-center p-3">Loading available classes...</p>
      {:else if errorAvailable}
        <div class="alert alert-warning text-center m-3" role="alert">
          Error loading available classes: {errorAvailable}
        </div>
      {:else if filteredAvailableClasses.length === 0}
        <div class="alert alert-info text-center m-3" role="alert">
          {#if searchTerm.length > 0}
            No available classes found matching your search term: "{searchTerm}".
          {:else}
            No credit classes available for this semester.
          {/if}
        </div>
      {:else}
        <div class="table-responsive">
          <table class="table table-hover table-sm mb-0">
            <thead class="table-light">
              <tr>
                <th scope="col">Subject Name</th>
                <th scope="col">Group</th>
                <th scope="col">Credits</th>
                <th scope="col">Max Cap.</th>
                <th scope="col">Registered</th>
                <th scope="col">Lecturer(s)</th>
                <th scope="col" class="text-center">Action</th>
              </tr>
            </thead>
            <tbody>
              {#each filteredAvailableClasses as classItem}
                <tr>
                  <td>{classItem.monTen}</td>
                  <td>{classItem.nhom}</td>
                  <td>{classItem.monSoTinChi}</td>
                  <td>{classItem.siSoToiDa}</td>
                  <td>{classItem.daDangKy}</td> 
                  <td>{classItem.giangVien}</td> 
                  <td class="text-center">
                    <button
                      on:click={() => handleRegister(classItem.lopTinChiId)}
                      class="btn btn-primary btn-sm w-100"
                      disabled={isLoadingRegistered || isLoadingAvailable || registeredClasses.some(rc => rc.lopTinChiId === classItem.id)}
                    >
                      {#if registeredClasses.some(rc => rc.lopTinChiId === classItem.id)}
                        Registered
                      {:else}
                        <i class="bi bi-check-circle-fill me-1"></i>Register
                      {/if}
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

  <!-- Your Registered Classes Section -->
  <div class="card shadow-sm mt-5">
    <div class="card-header bg-success text-white">
      <h2 class="h5 mb-0">Your Registered Classes (Current Semester)</h2>
    </div>
    <div class="card-body">
      {#if isLoadingRegistered}
        <p class="text-center p-3">Loading registered classes...</p>
      {:else if errorRegistered}
        <div class="alert alert-warning text-center m-3" role="alert">
          Error loading registered classes: {errorRegistered}
        </div>
      {:else if registeredClasses.length === 0} 
        <p class="text-muted text-center mb-0">
          You have not registered for any classes in this semester yet.
        </p>
      {:else}
        <ul class="list-group list-group-flush">
          {#each registeredClasses as regClass (regClass.dangKyId)} 
            <li class="list-group-item d-flex justify-content-between align-items-center">
              <div>
                <span class="fw-semibold">{regClass.monTen}</span> (Group: {regClass.nhom}) 
                <br>
                <small class="text-muted">
                  Credits: {regClass.monSoTinChi} , Status: <span class="badge bg-info text-dark">{regClass.trangThaiDangKy}</span>
                  <br>Lecturer(s): {regClass.giangVien} 
                  <br>Registered on: {new Date(regClass.ngayDangKy).toLocaleDateString()}
                </small>
              </div>
              {#if regClass.trangThaiDangKy !== 'Da huy'}
                <button 
                  class="btn btn-danger btn-sm" 
                  on:click={() => handleUnregister(regClass.dangKyId)}
                  disabled={isLoadingRegistered || isLoadingAvailable}
                >
                  <i class="bi bi-x-circle-fill me-1"></i>Unregister
                </button>
              {/if}
            </li>
          {/each}
        </ul>
      {/if}
    </div>
  </div>

</div>

<style>
  :global(body) {
    background-color: #f8f9fa; /* Light grey background for the page */
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  }

  .bg-primary-light {
    background-color: #0056b3 !important; /* Consistent with dashboard */
  }

  .navbar-dark .navbar-nav .nav-link {
    color: rgba(255, 255, 255, 0.85);
  }
  .navbar-dark .navbar-nav .nav-link:hover,
  .navbar-dark .navbar-nav .nav-link:focus,
  .navbar-dark .navbar-nav .nav-link.active {
    color: #fff;
  }

  .table td, .table th {
    vertical-align: middle;
  }
</style>
