<script lang="ts">
  import { onMount } from 'svelte';
  import 'bootstrap/dist/css/bootstrap.min.css';
  import { browser } from '$app/environment';
  import { goto } from '$app/navigation';
  import { StudentApiService, type StudentScore } from '$lib/services/studentApi';

  // Group scores by semester (semester = hocKy + namHoc)
  let scoresBySemester: Record<string, StudentScore[]> = {};
  let loading = true;
  let error = '';

  onMount(async () => {
    if (browser) {
      import('bootstrap/dist/js/bootstrap.bundle.min.js');
      
      const token = localStorage.getItem('jwtToken');
      if (!token) {
        goto('/'); // Redirect to login if not authenticated
        return;
      }

      try {
        const scores = await StudentApiService.getStudentScores();
          // Group scores by semester (combine hocKy and namHoc)
        scoresBySemester = scores.reduce((acc: Record<string, StudentScore[]>, score: StudentScore) => {
          const semesterKey = `${score.hocKy} ${score.namHoc}`;
          if (!acc[semesterKey]) {
            acc[semesterKey] = [];
          }
          acc[semesterKey].push(score);
          return acc;
        }, {} as Record<string, StudentScore[]>);
        
        loading = false;
      } catch (err) {
        console.error('Error loading scores:', err);
        error = 'Failed to load scores. Please try again later.';
        loading = false;
      }
    }
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
          <a class="nav-link active" href="/student/score"><i class="bi bi-journal-text me-1"></i>Scores</a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="/student/credit-registration"><i class="bi bi-card-checklist me-1"></i>Register Classes</a>
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

<svelte:head>
  <title>Student Scores</title>
</svelte:head>

<div class="container mt-4 mb-4">
  <div class="row mb-3">
    <div class="col">
      <h1 class="h3">Student Scores</h1>
    </div>
  </div>

  {#if loading}
    <div class="d-flex justify-content-center align-items-center" style="min-height: 200px;">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
      <span class="ms-2">Loading your scores...</span>
    </div>
  {:else if error}
    <div class="alert alert-danger" role="alert">
      <i class="bi bi-exclamation-triangle me-2"></i>
      {error}
    </div>
  {:else if Object.keys(scoresBySemester).length === 0}
    <div class="alert alert-info" role="alert">
      <i class="bi bi-info-circle me-2"></i>
      No score data available.
    </div>
  {:else}
    {#each Object.entries(scoresBySemester) as [semester, scores]}
      <div class="card mb-4">
        <div class="card-header">
          <h5 class="mb-0">
            <i class="bi bi-calendar3 me-2"></i>
            {semester}
          </h5>
        </div>
        <div class="card-body">
          <div class="table-responsive">
            <table class="table table-hover table-striped">
              <thead class="table-primary">
                <tr>
                  <th scope="col">Subject Code</th>
                  <th scope="col">Subject Name</th>
                  <th scope="col" class="text-center">Credits</th>
                  <th scope="col" class="text-center">Attendance</th>
                  <th scope="col" class="text-center">Midterm</th>
                  <th scope="col" class="text-center">Final Exam</th>
                  <th scope="col" class="text-center">Final Grade</th>
                </tr>
              </thead>
              <tbody>
                {#each scores as score}
                  <tr>
                    <td><strong>{score.maMon}</strong></td>
                    <td>{score.tenMon}</td>
                    <td class="text-center">{score.soTinChi}</td>
                    <td class="text-center">
                      {score.diemCC !== null ? score.diemCC.toFixed(1) : 'N/A'}
                    </td>
                    <td class="text-center">
                      {score.diemKT !== null ? score.diemKT.toFixed(1) : 'N/A'}
                    </td>
                    <td class="text-center">
                      {score.diemThi !== null ? score.diemThi.toFixed(1) : 'N/A'}
                    </td>
                    <td class="text-center">
                      <span class="badge {score.diemTB !== null ? (score.diemTB >= 5.0 ? 'bg-success' : 'bg-danger') : 'bg-secondary'}">
                        {score.diemTB !== null ? score.diemTB.toFixed(2) : 'N/A'}
                      </span>
                    </td>
                  </tr>
                {/each}
              </tbody>
            </table>
          </div>
          
          <!-- Summary row for semester -->
          <div class="row mt-3">
            <div class="col-md-6">
              <small class="text-muted">
                <i class="bi bi-info-circle me-1"></i>
                Total courses: {scores.length}
              </small>
            </div>
            <div class="col-md-6 text-end">
              <small class="text-muted">
                <i class="bi bi-calculator me-1"></i>
                Total credits: {scores.reduce((sum, score) => sum + score.soTinChi, 0)}
              </small>
            </div>
          </div>
        </div>
      </div>
    {/each}
  {/if}
</div>

<style>
  :global(body) {
    background-color: #f8f9fa; /* Light grey background for the page */
    /* font-family is managed by +layout.svelte */
  }

  .bg-primary-light {
    background-color: #0056b3 !important; /* Consistent with other pages */
  }

  .navbar-dark .navbar-nav .nav-link {
    color: rgba(255, 255, 255, 0.85);
  }
  .navbar-dark .navbar-nav .nav-link:hover,
  .navbar-dark .navbar-nav .nav-link:focus,
  .navbar-dark .navbar-nav .nav-link.active {
    color: #fff;
  }
  
  /* Ensure bootstrap icons are available */

  /* Existing styles from score page, slightly adjusted for consistency */
  .container {
    max-width: 960px; /* Retained from original */
  }

  .card-header h5 {
    font-weight: 500; /* Retained from original */
    margin-bottom: 0; /* Common practice for card headers */
  }
  .table th {
    font-weight: 600; /* Retained from original */
  }
  .table td, .table th {
    vertical-align: middle; /* Retained from original */
  }
  .table-primary {
    --bs-table-bg: #cfe2ff; /* Example primary table head color, adjust as needed */
    --bs-table-border-color: #b9d5ff;
  }
</style>
