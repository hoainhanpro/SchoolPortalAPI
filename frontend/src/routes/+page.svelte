<script lang="ts">
  import { onMount } from 'svelte';
  import 'bootstrap/dist/css/bootstrap.min.css';
  import { browser } from '$app/environment';
  import { goto } from '$app/navigation'; 
  onMount(() => {
    if (browser) {
      import('bootstrap/dist/js/bootstrap.bundle.min.js');
    }
  });

  let email = '';
  let password = '';
  let userId = ''; 
  let showPassword = false;
  let loginError: string | null = null; 
  let scheduleError: string | null = null;
  let userType: 'student' | 'lecturer' = 'student'; // Added userType state, default to student
  
  interface ScheduleEntry {
    time: string;
    subject: string;
    room: string;
    classCode: string;
  }

  let weekSchedule: Record<string, ScheduleEntry[]> | null = null;

  const daysOfWeek = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

  function togglePasswordVisibility() {
    showPassword = !showPassword;
  }

  async function handleLogin() { 
    loginError = null; 
    try {
      const response = await fetch('http://localhost:1234/api/auth/login', { 
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username: email, password: password }), 
      });

      if (response.ok) {
        const data = await response.json();
        if (data.token && browser) { 
          localStorage.setItem('jwtToken', data.token);
          if (data.type === 'ROLE_SINHVIEN') {
            await goto('student/dashboard');
          } else if (data.type === 'ROLE_GIAOVIEN') {
            await goto('lecturer/dashboard');
          } else if (data.type === 'ROLE_NHANVIEN') {
            await goto('admin/dashboard');
          } else {
            loginError = 'Unknown user type. Please contact support.';
            console.error('Unknown user type:', data.type);
          }
        } else if (!data.token) {
            loginError = 'Login successful, but no token was received from the server.';
            console.error('Login response OK, but no token:', data);
        }
      } else {
        if (response.status === 401) { 
          try {
            const errorData = await response.json();
            loginError = errorData.message || 'Invalid email or password.';
          } catch (e) {
            loginError = 'Invalid email or password.'; 
          }
        } else {
          loginError = `Login failed. Server responded with status: ${response.status}.`;
        }
        console.error('Login failed response:', response);
      }
    } catch (error) {
      loginError = 'An error occurred while trying to log in. Please check your network connection and try again.';
      console.error('Login request fetch error:', error);
    }
  }

  async function viewSchedule() {
    scheduleError = null;
    if (!userId || userId.trim() === '') {
      weekSchedule = null;
      scheduleError = 'Please enter a User ID to view their schedule.';
      return;
    }
    try {
      // Construct payload based on userType
      let payload: any = {
        userType: userType
      };

      if (userType === 'student') {
        // Check if userId looks like an email or an ID
        if (userId.includes('@')) {
          payload.email = userId;
        } else {
          payload.studentId = userId;
        }
      } else { // lecturer
        if (userId.includes('@')) {
          payload.email = userId;
        } else {
          payload.lecturerId = userId;
        }
      }

      const response = await fetch('http://localhost:1234/api/schedule/current', { 
        method: 'POST', 
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload), 
      });

      if (response.ok) {
        const data = await response.json();
        weekSchedule = data;
        if (Object.values(data).every(daySchedule => (daySchedule as ScheduleEntry[]).length === 0)) {
            // scheduleError = `No schedule data found for ${userId}.`; // Optional message
        }
      } else {
        weekSchedule = null; 
        const errorData = await response.json().catch(() => null); 
        if (response.status === 404) {
            scheduleError = `No schedule found for User ID: ${userId}. Please check the ID.`;
        } else if (errorData && errorData.message) {
            scheduleError = `Failed to fetch schedule: ${errorData.message}`;
        } else {
            scheduleError = `Failed to fetch schedule. Server responded with status: ${response.status}.`;
        }
        console.error('Failed to fetch schedule:', response);
      }
    } 
    catch (error) {
      weekSchedule = null;
      scheduleError = 'An error occurred while trying to fetch the schedule. Please check your network connection.';
      console.error('Error while viewing schedule:', error);
    }
  }
  
  $: if (userId === '' && weekSchedule !== null) {
    weekSchedule = null;
    scheduleError = null; 
  }

</script>

<div class="container-fluid bg-light p-3 p-md-4 d-flex align-items-center" style="min-height: 100vh;">
  <div class="row w-100 mx-auto" style="max-width: 1400px;">
    <!-- Login Card Column -->
    <div class="col-lg-4 mb-4 mb-lg-0">
      <div class="card shadow-lg border-0">
        <div class="card-body p-4">
          <div class="text-center mb-4">
            <div class="logo-placeholder mx-auto mb-3">
              <img src="/images/logos/ptit-logo-inv.png" alt="PTIT Logo" style="width: 100%; height: 100%; object-fit: contain;">
            </div>
            <h2 class="text-primary fw-bold">School Portal System</h2>
            <p class="text-muted">Please login to continue</p>
          </div>
          <form on:submit|preventDefault={handleLogin}>
            <div class="mb-3">
              <label for="email" class="form-label">Email</label>
              <input type="text" class="form-control" id="email" bind:value={email}
                     placeholder="student@example.com" required>
            </div>
            <div class="mb-3">
              <label for="password" class="form-label">Password</label>
              <div class="input-group">
                {#if showPassword}
                  <input type="text" class="form-control" id="password-visible"
                         bind:value={password} required={true}>
                {:else}
                  <input type="password" class="form-control" id="password-hidden"
                         bind:value={password} required={true}>
                {/if}
                <button class="btn btn-outline-secondary" type="button"
                        on:click={togglePasswordVisibility}>
                  {#key showPassword} 
                    {#if showPassword}
                      <i class="bi bi-eye-slash"></i>
                    {:else}
                      <i class="bi bi-eye"></i>
                    {/if}
                  {/key}
                </button>
              </div>
            </div>
            <div class="mb-3 text-end">
              <a href="#!" class="text-decoration-none">Forgot password?</a>
            </div>
            <div class="d-grid">
              <button type="submit" class="btn btn-primary py-2">Login</button>
            </div>
            {#if loginError}
              <div class="alert alert-danger mt-3" role="alert">
                {loginError}
              </div>
            {/if}
          </form>
        </div>
      </div>
    </div>

    <!-- Schedule Card Column -->
    <div class="col-lg-8">
      <div class="card shadow-lg border-0 h-100">
        <div class="card-body p-4 d-flex flex-column">
          <h3 class="text-primary mb-4">View Schedule</h3>
          
          <div class="mb-3">
            <!-- svelte-ignore a11y_label_has_associated_control -->
            <label class="form-label">User Type</label>
            <div>
              <div class="form-check form-check-inline">
                <input class="form-check-input" type="radio" name="userTypeOptions" id="studentRadio" value="student" bind:group={userType}>
                <label class="form-check-label" for="studentRadio">Student</label>
              </div>
              <div class="form-check form-check-inline">
                <input class="form-check-input" type="radio" name="userTypeOptions" id="lecturerRadio" value="lecturer" bind:group={userType}>
                <label class="form-check-label" for="lecturerRadio">Lecturer</label>
              </div>
            </div>
          </div>

          <div class="mb-4">
            <label for="userIdInput" class="form-label">{userType === 'student' ? 'Student ID / Email' : 'Lecturer ID / Email'}</label>
            <div class="input-group">
              <input type="text" class="form-control" id="userIdInput"
                     bind:value={userId} placeholder={userType === 'student' ? 'Enter Student ID or Email' : 'Enter Lecturer ID or Email'}>
              <button class="btn btn-primary" type="button" on:click={viewSchedule}>
                <i class="bi bi-calendar-check"></i> View Schedule
              </button>
            </div>
          </div>
          {#if scheduleError}
            <div class="alert alert-warning mt-3" role="alert">
              {scheduleError}
            </div>
          {/if}

          <h5 class="mb-3">
            Weekly Schedule for: <span class="text-primary">{userId || "N/A"} ({userType})</span>
          </h5>
          <div class="table-responsive flex-grow-1">
            <table class="table table-bordered table-hover">
              <thead class="table-primary">
                <tr>
                  <th style="width: 15%; min-width: 100px;">Thứ</th>
                  <th style="width: 15%; min-width: 100px;">Thời gian</th>
                  <th style="width: 15%; min-width: 100px;">Mã lớp</th>
                  <th style="width: 35%; min-width: 150px;">Môn học</th>
                  <th style="width: 20%; min-width: 100px;">Phòng</th>
                </tr>
              </thead>
              <tbody>
                {#if weekSchedule && Object.keys(weekSchedule).length > 0}
                  {#each daysOfWeek as day (day)}
                    {@const dayKey = day.toLowerCase()}
                    {@const classesForDay = weekSchedule[dayKey] || []}
                    
                    {#if classesForDay.length > 0}
                      {#each classesForDay as cls, i (cls.time + cls.subject + cls.room)}
                        <tr>
                          {#if i === 0}
                            <td rowspan={classesForDay.length} class="align-middle text-capitalize fw-medium bg-light-subtle">{day}</td>
                          {/if}
                          <td>{cls.time}</td>
                          <td>{cls.classCode || 'N/A'}</td>
                          <td>{cls.subject}</td>
                          <td>{cls.room}</td>
                        </tr>
                      {/each}
                    {:else}
                      <tr>
                        <td class="align-middle text-capitalize fw-medium bg-light-subtle">{day}</td>
                        <td colspan="4" class="text-center text-muted fst-italic">No classes scheduled</td>
                      </tr>
                    {/if}
                  {/each}
                {:else if !scheduleError}
                  <tr>
                    <td colspan="5" class="text-center text-muted p-5">
                      <i class="bi bi-info-circle fs-3 d-block mb-2"></i>
                      Enter a {userType} ID/Email and click "View Schedule" to display the weekly timetable.
                    </td>
                  </tr>
                {/if}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<style>
  :global(body) {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    background-color: #eef2f7; 
    color: #333;
  }

  .logo-placeholder {
    width: 150px;
    height: 150px;
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 2rem;
    overflow: hidden; 
  }

  .text-primary {
    color: #0d6efd !important;
  }

  .btn-primary {
    background-color: #0d6efd;
    border-color: #0d6efd;
    transition: background-color 0.2s ease-in-out;
  }

  .btn-primary:hover {
    background-color: #0b5ed7; 
    border-color: #0a58ca;
  }

  .card {
    border-radius: 0.75rem; 
  }

  .form-control:focus {
    border-color: #86b7fe; 
    box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
  }

  .table-primary {
    background-color: #cfe2ff; 
    border-color: #b6d4fe;
  }

  .table-hover tbody tr:hover {
    background-color: #f8f9fa; 
  }

  .bg-light-subtle {
    background-color: #fcfcfd !important; 
  }

</style>