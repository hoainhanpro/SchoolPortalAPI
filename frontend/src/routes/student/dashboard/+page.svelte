<script lang="ts">
  import { onMount, tick } from 'svelte'; // Added tick
  import 'bootstrap/dist/css/bootstrap.min.css';
  import { browser } from '$app/environment';
  import { goto } from '$app/navigation';

  let userData: any = null;
  let isLoadingUserData = true;
  let userDataError: string | null = null;
  let student: any = null; // Initialize as null, will be populated from userData

  let weeklyScheduleCardElement: HTMLElement | null = null;
  let studentInfoCardElement: HTMLElement | null = null;
  let classInfoCardElement: HTMLElement | null = null;

  function updateScheduleCardHeight() {
    if (!browser) return;

    // Query elements each time, as they might be conditionally rendered
    studentInfoCardElement = document.getElementById('student-info-card') as HTMLElement | null;
    classInfoCardElement = document.getElementById('class-info-card') as HTMLElement | null;
    weeklyScheduleCardElement = document.getElementById('weekly-schedule-card') as HTMLElement | null;

    if (weeklyScheduleCardElement) { // Ensure schedule card exists
      if (student && studentInfoCardElement && classInfoCardElement &&
          studentInfoCardElement.offsetParent !== null && classInfoCardElement.offsetParent !== null) {
        // Cards are present and visible for a student
        const studentHeight = studentInfoCardElement.offsetHeight;
        const studentCardStyles = window.getComputedStyle(studentInfoCardElement);
        const studentCardMarginBottom = parseFloat(studentCardStyles.marginBottom) || 0;
        
        const classHeight = classInfoCardElement.offsetHeight;
        
        const targetHeight = studentHeight + studentCardMarginBottom + classHeight;

        if (targetHeight > 0) {
          weeklyScheduleCardElement.style.height = `${targetHeight}px`;
        } else {
          // Fallback if calculated height is not positive (e.g. elements hidden unexpectedly)
          weeklyScheduleCardElement.style.height = 'auto'; 
        }
      } else {
        // Not a student, or dependent cards not found/visible, set schedule card height to auto
        weeklyScheduleCardElement.style.height = 'auto';
      }
    }
  }

  onMount(() => {
    const initializeAsync = async () => {
      if (browser) {
        import('bootstrap/dist/js/bootstrap.bundle.min.js');
        
        // Get element references after initial render
        studentInfoCardElement = document.getElementById('student-info-card');
        classInfoCardElement = document.getElementById('class-info-card');
        weeklyScheduleCardElement = document.getElementById('weekly-schedule-card');

        window.addEventListener('resize', updateScheduleCardHeight);

        // const token = localStorage.getItem('jwtToken');
        // if (!token) {
        //   goto('/login');
        //   return;
        // }

        try {

          const response = await fetch('http://localhost:1234/api/auth/me', {
            method: 'GET',
            headers: {
              'Content-Type': 'application/json',
              'authorization': `Bearer ${localStorage.getItem('jwtToken')}`
            }
          });

          if (!response.ok) {
            throw new Error(`Failed to fetch user data. Status: ${response.status}`);
          }

          userData = await response.json();
          student = userData; // Assuming the API returns the student object directly


          // Fetch schedule for the current week
          await fetchSchedule(currentWeekOffset);
        } catch (error) {
          console.error("Error fetching user data:", error);
          userDataError = error instanceof Error ? error.message : "An unknown error occurred while fetching user data.";
          // if (browser) {
          //     localStorage.removeItem('jwtToken');
          //     if (typeof goto === 'function') {
          //          goto('/login');
          //     }
          // }
        } finally {
          isLoadingUserData = false;
          // Wait for Svelte to update the DOM based on userData
          await tick();
          // Now that DOM is updated with student/class info, calculate and set height
          // This needs to be called when the elements are actually in the DOM and sized.
          if (student) {
              // Ensure elements are re-queried if they are conditionally rendered
              studentInfoCardElement = document.getElementById('student-info-card');
              classInfoCardElement = document.getElementById('class-info-card');
              weeklyScheduleCardElement = document.getElementById('weekly-schedule-card');
              updateScheduleCardHeight();
          }
        }
      }
    };

    initializeAsync().catch(err => {
      // This catch block handles errors from the initializeAsync promise itself,
      // for example, if an unexpected error occurs that isn't caught by the try/catch within initializeAsync.
      console.error("Error during onMount initialization sequence:", err);
      // Depending on the application, you might want to set a generic error state here.
      // The existing error handling within initializeAsync (e.g., goto('/login')) should cover most cases.
    });

    return () => {
      if (browser) {
        window.removeEventListener('resize', updateScheduleCardHeight);
      }
    };
  });

  interface ChatMessage {
    text: string;
    sender: 'student' | 'advisor';
    time: Date;
  }

  let currentWeekOffset = 0;
  let weekDateRange = ""; // New variable for the date range string
  let displayableScheduleDays: Array<{ name: string; classes: Array<{ time: string; subject: string; room: string; teacher: string }> }> = [];
  let isLoadingSchedule = true; 
  let scheduleError: string | null = null;
  let scheduleDisplayMessage: string | null = null; 

  const daysOrder = ["monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"];

  // Function to calculate and format the week date range
  function updateWeekDateRange(offset: number) {
    const today = new Date();
    const currentDay = today.getDay(); // 0 (Sun) - 6 (Sat)
    const daysToMonday = (currentDay === 0 ? -6 : 1) - currentDay; // Calculate days to get to Monday

    const mondayOfCurrentWeek = new Date(today);
    mondayOfCurrentWeek.setDate(today.getDate() + daysToMonday + (offset * 7));
    
    const sundayOfCurrentWeek = new Date(mondayOfCurrentWeek);
    sundayOfCurrentWeek.setDate(mondayOfCurrentWeek.getDate() + 6);

    const options: Intl.DateTimeFormatOptions = { month: 'short', day: 'numeric' };
    const yearOption: Intl.DateTimeFormatOptions = { year: 'numeric' };

    const startStr = mondayOfCurrentWeek.toLocaleDateString('en-US', options);
    const endStr = sundayOfCurrentWeek.toLocaleDateString('en-US', options);
    const yearStr = mondayOfCurrentWeek.toLocaleDateString('en-US', yearOption);

    weekDateRange = `${startStr} - ${endStr}, ${yearStr}`;
  }

  async function fetchSchedule(offset: number) {
    isLoadingSchedule = true;
    scheduleError = null;
    scheduleDisplayMessage = null;

    if (!student || !student.mssv) {
      console.warn("Student ID (mssv) not available. Cannot fetch schedule.");
      scheduleError = "Student information is not fully loaded.";
      scheduleDisplayMessage = null; 
      isLoadingSchedule = false;
      // Update week date range even if schedule can't be fetched, to reflect the selected offset
      updateWeekDateRange(offset); 
      displayableScheduleDays = daysOrder.map(dayKey => ({
        name: dayKey.charAt(0).toUpperCase() + dayKey.slice(1),
        classes: []
      }));
      return;
    }

    try {
      const response = await fetch('http://localhost:1234/api/schedule/current', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'authorization': `Bearer ${localStorage.getItem('jwtToken')}`
        },
        body: JSON.stringify({
          studentId: student.mssv, 
          userType: 'student', 
          weekOffset: offset,
        }),
      });

      if (!response.ok) {
        throw new Error(`Failed to fetch schedule. Status: ${response.status}`);
      }

      const dataFromApi: Record<string, Array<{ time: string; subject: string; room: string; teacher: string }>> = await response.json();
      currentWeekOffset = offset;
      updateWeekDateRange(currentWeekOffset); // Update the date range string
      displayableScheduleDays = daysOrder.map(dayKey => ({
        name: dayKey.charAt(0).toUpperCase() + dayKey.slice(1),
        classes: dataFromApi[dayKey.toLowerCase()] || []
      }));

    } catch (error) {
      console.error("Error fetching schedule:", error);
      scheduleError = error instanceof Error ? error.message : "An unknown error occurred while fetching schedule.";
      displayableScheduleDays = daysOrder.map(dayKey => ({
        name: dayKey.charAt(0).toUpperCase() + dayKey.slice(1),
        classes: []
      }));
    } finally {
      isLoadingSchedule = false;
    }
  }
  
  function previousWeek() {
    fetchSchedule(currentWeekOffset - 1);
  }
  
  function nextWeek() {
    fetchSchedule(currentWeekOffset + 1);
  }
  
  let showChatBubble = false;
  let chatMessages: ChatMessage[] = [];
  let newMessage = '';
  
  function toggleChatBubble() {
    showChatBubble = !showChatBubble;
  }
  
  function sendMessage() {
    if (newMessage.trim()) {
      chatMessages = [...chatMessages, { text: newMessage, sender: 'student', time: new Date() }];
      newMessage = '';
      
      setTimeout(() => {
        chatMessages = [...chatMessages, { 
          text: "Hello! How can I help you today?", 
          sender: 'advisor', 
          time: new Date() 
        }];
      }, 1000);
    }
  }
</script>

<div class="dashboard-container">
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
            <a class="nav-link active" href="/student/dashboard"><i class="bi bi-house-door me-1"></i>Dashboard</a>
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
  
  <!-- Main Content -->
  <div class="container-fluid py-3">
    {#if isLoadingUserData}
      <div class="d-flex justify-content-center align-items-center" style="height: 80vh;">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Loading user data...</span>
        </div>
        <p class="ms-3">Loading your information...</p>
      </div>
    {:else if userDataError}
      <div class="alert alert-danger" role="alert">
        Error loading user data: {userDataError}. Please try <a href="/login" class="alert-link">logging in</a> again.
      </div>
    {:else if student}
    <div class="row">
      <!-- Student Info and Class Info Column -->
      <div class="col-lg-4 mb-4">
        <!-- Student Info Card -->
        <div id="student-info-card" class="card shadow-sm mb-3">
          <div class="card-header bg-primary-light text-white">
            <h5 class="mb-0 fs-6"><i class="bi bi-person-badge-fill me-2"></i>Student Information</h5>
          </div>
          <div class="card-body p-2">
            <div class="text-center mb-3">
              <div class="avatar-placeholder rounded-circle bg-light d-inline-flex align-items-center justify-content-center mb-2">
                <span class="display-4 text-primary-light">{student.ho ? student.ho[0] : 'S'}{student.ten ? student.ten[0] : 'D'}</span>
              </div>
              <h4 class="fs-5">{student.ho} {student.ten}</h4>
              <p class="text-muted mb-0">Student ID: {student.mssv}</p>
              <p class="text-muted">Class: {student.lopTen || 'N/A'}</p>
              <p class="text-muted">Department: {student.khoaTen || 'N/A'}</p>
            </div>
            
            <hr>
            
            <div class="student-details">
              <div class="row mb-2">
                <div class="col-sm-5 col-md-4 text-muted">Date of Birth:</div>
                <div class="col-sm-7 col-md-8">{student.ngaySinh && student.ngaySinh !== 'N/A' ? new Date(student.ngaySinh).toLocaleDateString() : 'N/A'}</div>
              </div>
              <div class="row mb-2">
                <div class="col-sm-5 col-md-4 text-muted">Gender:</div>
                <div class="col-sm-7 col-md-8">{student.gioiTinh || 'N/A'}</div>
              </div>
              <div class="row mb-2">
                <div class="col-sm-5 col-md-4 text-muted">Address:</div>
                <div class="col-sm-7 col-md-8">{student.diaChi || 'N/A'}</div>
              </div>
              <div class="row mb-2">
                <div class="col-sm-5 col-md-4 text-muted">Phone:</div>
                <div class="col-sm-7 col-md-8">{student.sdt || 'N/A'}</div>
              </div>
              <div class="row mb-2">
                <div class="col-sm-5 col-md-4 text-muted">Email:</div>
                <div class="col-sm-7 col-md-8">{student.email || 'N/A'}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Class Info Card -->
        <div id="class-info-card" class="card shadow-sm">
          <div class="card-header bg-primary-light text-white">
            <h5 class="mb-0 fs-6"><i class="bi bi-easel2-fill me-2"></i>Class Information</h5>
          </div>
          <div class="card-body p-2">
            <div class="class-details">
              <div class="row mb-2">
                <div class="col-sm-5 col-md-4 text-muted">Class Name:</div>
                <div class="col-sm-7 col-md-8 fw-medium">{userData.lopTen}</div>
              </div>
              <div class="row mb-2">
                <div class="col-sm-5 col-md-4 text-muted">Advisor:</div>
                <div class="col-sm-7 col-md-8">{userData.coVanTen}</div>
              </div>
              <div class="row mb-2">
                <div class="col-sm-5 col-md-4 text-muted">Department:</div>
                <div class="col-sm-7 col-md-8">{userData.khoaTen}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Weekly Schedule Column -->
      <div class="col-lg-8 mb-4">
        <div id="weekly-schedule-card" class="card shadow-sm d-flex flex-column"> 
          <div class="card-header bg-primary-light text-white d-flex justify-content-between align-items-center">
            <h5 class="mb-0 fs-6"><i class="bi bi-calendar3-week-fill me-2"></i>Weekly Schedule</h5>
            <div class="week-navigation">
              <button class="btn btn-sm btn-outline-light me-2" on:click={previousWeek} disabled={isLoadingSchedule}>
                <i class="bi bi-arrow-left-circle-fill"></i> Prev
              </button>
              <span class="fs-7">{weekDateRange}</span>
              <button class="btn btn-sm btn-outline-light ms-2" on:click={nextWeek} disabled={isLoadingSchedule}>
                Next <i class="bi bi-arrow-right-circle-fill"></i>
              </button>
            </div>
          </div>
          <div class="card-body schedule-content-box"> 
            {#if isLoadingSchedule}
              <p class="text-center p-3">
                <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                Loading schedule...
              </p>
            {:else if scheduleDisplayMessage}
              <div class="alert alert-info" role="alert">
                {scheduleDisplayMessage}
              </div>
            {:else if scheduleError}
              <div class="alert alert-danger" role="alert">
                Error loading schedule: {scheduleError}
              </div>
            {:else}
              {#each displayableScheduleDays as day}
                <div class="day-schedule mb-3">
                  <h6 class="border-bottom pb-2 mb-2">{day.name}</h6>
                  {#if day.classes.length > 0}
                    <ul class="list-group list-group-flush">
                      {#each day.classes as cls}
                        <li class="list-group-item small">
                          <strong>{cls.time}</strong>: {cls.subject}
                          <br>
                          <span class="text-muted">Teacher: {cls.teacher} | Room: {cls.room}</span>
                        </li>
                      {/each}
                    </ul>
                  {:else}
                    <p class="text-muted small ps-2">No classes scheduled for {day.name.toLowerCase()}.</p>
                  {/if}
                </div>
              {/each}
            {/if}
          </div>
        </div>
      </div>
    </div>
    {:else} <!-- Fallback for other errors -->
      <div class="alert alert-warning" role="alert">
        User data could not be loaded or is not in the expected format. Please ensure you are logged in correctly.
      </div>
    {/if}
  </div>
  
  <!-- Chat Bubble -->
  <div class="chat-bubble-container">
    <button class="btn btn-primary-light rounded-circle chat-toggle-btn" on:click={toggleChatBubble} aria-label="Toggle chat">
      <i class="bi {showChatBubble ? 'bi-x-lg' : 'bi-chat-dots-fill'}"></i>
    </button>
    
    {#if showChatBubble}
      <div class="chat-window card shadow-lg">
        <div class="card-header bg-primary-light text-white">
          <h6 class="mb-0">Chat with Advisor</h6>
        </div>
        <div class="card-body chat-messages">
          {#if chatMessages.length === 0}
            <div class="text-center text-muted py-4">
              <i class="bi bi-chat-dots display-4 mb-2"></i>
              <p>Start a conversation with your advisor.</p>
            </div>
          {:else}
            {#each chatMessages as message}
              <div class="message {message.sender === 'student' ? 'message-outgoing' : 'message-incoming'}">
                <div class="message-content">
                  {message.text}
                </div>
                <div class="message-time">
                  {message.time.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                </div>
              </div>
            {/each}
          {/if}
        </div>
        <div class="card-footer p-2">
          <div class="input-group">
            <input type="text" class="form-control" placeholder="Type a message..." 
                   bind:value={newMessage} on:keypress={(e) => e.key === 'Enter' && sendMessage()}>
            <button class="btn btn-primary-light" type="button" on:click={sendMessage} aria-label="Send message">
              <i class="bi bi-send-fill"></i>
            </button>
          </div>
        </div>
      </div>
    {/if}
  </div>
</div>

<style>
  :global(body) {
    background-color: #f8f9fa; /* Light grey background for the page */
    min-height: 100vh;
  }
  
  .avatar-placeholder {
    width: 100px;
    height: 100px;
  }
  
  .chat-bubble-container {
    position: fixed;
    bottom: 20px;
    right: 20px;
    z-index: 1050; /* Ensure it's above other elements */
  }
  
  .chat-toggle-btn {
    width: 50px;
    height: 50px;
    font-size: 1.25rem; /* Make icon slightly larger */
    box-shadow: 0 0.25rem 0.5rem rgba(0, 0, 0, 0.15);
  }
  
  .chat-window {
    position: absolute;
    bottom: 70px; 
    right: 0;
    width: 320px;
    max-height: 450px; /* Increased max height */
    display: flex;
    flex-direction: column;
    border-radius: 0.375rem; 
    overflow: hidden; 
  }
  
  .chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 0.5rem; /* Reduced from 1rem */
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }
  
  .message {
    max-width: 80%;
    padding: 0.5rem 0.75rem;
    border-radius: 0.75rem; 
    position: relative;
    word-wrap: break-word;
  }

  .message-incoming {
    background-color: #e9ecef; /* Lighter grey for incoming messages */
    align-self: flex-start;
    border-bottom-left-radius: 0.25rem;
  }

  .message-outgoing {
    background-color: #cfe2ff; /* Primary light blue for outgoing messages */
    align-self: flex-end;
    border-bottom-right-radius: 0.25rem;
  }
  
  .message-content {
    font-size: 0.9rem;
  }
  
  .message-time {
    font-size: 0.7rem;
    color: #6c757d; /* Muted text color for time */
    margin-top: 0.25rem;
    text-align: right; 
  }
  
  .message-incoming .message-time {
    text-align: left;
  }

  .bg-primary-light {
    background-color: #0056b3 !important; /* A slightly lighter shade of primary blue */
  }

  .text-primary-light {
    color: #0056b3 !important;
  }

  .btn-primary-light {
    background-color: #0056b3;
    border-color: #0056b3;
    color: #fff;
  }
  .btn-primary-light:hover {
    background-color: #004085; /* Darker shade for hover */
    border-color: #003770;
  }
  .btn-outline-light { /* Ensure high contrast for week navigation buttons on dark header */
    color: #f8f9fa !important;
    border-color: #f8f9fa !important;
  }
  .btn-outline-light:hover {
    color: #0056b3 !important;
    background-color: #f8f9fa !important;
  }
  .student-details .row, .class-details .row {
    padding-left: 0.5rem;
    padding-right: 0.5rem;
  }
  .student-details .text-muted, .class-details .text-muted {
    font-size: 0.9em;
  }
  .card-body.p-2 {
    padding: 0.75rem !important; /* Slightly more padding than p-2 default */
  }
  .card-header.bg-primary-light {
    padding: 0.5rem 0.75rem; /* Adjust padding for headers */
  }
  .fs-5 { font-size: 1.15rem !important; }
  .fs-6 { font-size: 1rem !important; }
  .fs-7 {
    font-size: 0.875rem; /* Or your preferred smaller size */
  }

  .schedule-content-box {
    overflow-y: auto;
    flex-grow: 1;
    padding: 0.5rem; /* Retained from existing rule */
    min-height: 0;   /* Added for proper flex item scrolling */
  }

  /* Remove bottom margin from the last direct child of schedule-content-box */
  .schedule-content-box > *:last-child {
    margin-bottom: 0 !important;
  }

  /* Adjust padding for list items if they look too cramped after parent padding change */
  .schedule-content-box .list-group-item {
    padding-top: 0.35rem;
    padding-bottom: 0.35rem;
    padding-left: 0.6rem; 
    padding-right: 0.6rem; 
  }

  /* Ensure day schedule headers have appropriate spacing if needed */
  .schedule-content-box .day-schedule h6 {
    margin-bottom: 0.5rem; /* Adjust as needed */
  }
  
  .avatar-placeholder {
    width: 80px;
    height: 80px;
    font-size: 2rem; /* Adjusted for better fit */
  }

  .student-details .row > div:first-child {
    font-weight: 500; /* Make labels slightly bolder */
  }

  .navbar-dark .navbar-nav .nav-link {
    color: rgba(255, 255, 255, 0.85);
  }
  .navbar-dark .navbar-nav .nav-link:hover,
  .navbar-dark .navbar-nav .nav-link:focus,
  .navbar-dark .navbar-nav .nav-link.active {
    color: #fff;
  }

  .bg-primary-light {
      background-color: #0056b3 !important; /* A slightly lighter shade of primary, adjust as needed */
  }

  .text-primary-light {
      color: #0056b3 !important;
  }

  /* Ensure the chat bubble doesn't cause horizontal scroll if it's too wide */
</style>