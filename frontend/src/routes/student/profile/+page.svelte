<script lang="ts">
  import { onMount } from 'svelte';
  import 'bootstrap/dist/css/bootstrap.min.css';
  import { browser } from '$app/environment';
  import { goto } from '$app/navigation';

  interface StudentProfile {
    mssv: string;
    hoTen: string; // Changed from ho and ten
    ngaySinh: string; // Date as string YYYY-MM-DD
    gioiTinh: string;
    diaChi: string;
    sdt: string;
    email: string;
    lop: {
      ten: string;
      khoa: {
        ten: string;
      };
    };
    // Add other fields that your /api/auth/me endpoint returns for a student
    // For example, if it returns lopTen and khoaTen directly:
    // lopTen?: string;
    // khoaTen?: string;
  }

  let studentProfile: StudentProfile | null = null;
  let isLoading = true;
  let errorMessage = '';
  let successMessage = ''; // For success feedback

  let isEditing = false;
  let editableProfileData: StudentProfile | null = null;

  // Password change fields
  let currentPassword = '';
  let newPassword = '';
  let confirmNewPassword = '';
  let passwordErrorMessage = '';

  // Mock current password for demonstration. In a real app, this check happens on the backend.
  // const MOCK_CURRENT_PASSWORD = "password123"; // Will be removed or replaced by actual backend check

  onMount(async () => {
    if (browser) {
      isLoading = true;
      errorMessage = '';
      successMessage = '';
      try {
        const token = localStorage.getItem('jwtToken');
        if (!token) {
          goto('/login'); // Redirect if no token
          return;
        }

        const response = await fetch('http://localhost:1234/api/auth/me', {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          }
        });

        if (!response.ok) {
          if (response.status === 401 || response.status === 403) {
            errorMessage = "Authentication failed. Please log in again.";
            goto('/login');
          } else {
            throw new Error(`Failed to fetch profile data. Status: ${response.status}`);
          }
        }
        
        const data = await response.json();
        // Assuming the API returns data that matches or can be mapped to StudentProfile
        // You might need to transform the data if the structure is different,
        // especially for nested properties like lop.ten and lop.khoa.ten
        // For example, if API returns lopTen and khoaTen directly at the root:
        studentProfile = {
          mssv: data.mssv,
          hoTen: data.hoTen || `${data.ho || ''} ${data.ten || ''}`.trim(), // Construct hoTen if not directly available
          ngaySinh: data.ngaySinh,
          gioiTinh: data.gioiTinh,
          diaChi: data.diaChi,
          sdt: data.sdt,
          email: data.email,
          lop: { // This assumes 'lop' is an object in the response with 'ten' and 'khoa'
            ten: data.lop?.ten || data.lopTen || 'N/A', // Adjust based on actual API response
            khoa: { // This assumes 'khoa' is an object within 'lop'
              ten: data.lop?.khoa?.ten || data.khoaTen || 'N/A' // Adjust based on actual API response
            }
          }
          // Map other fields as necessary
        };

      } catch (error) {
        console.error("Error fetching profile data:", error);
        errorMessage = error instanceof Error ? error.message : "Failed to load student profile. Please try again later.";
        successMessage = ''; // Clear success message on error
      } finally {
        isLoading = false;
      }
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

  function startEdit() {
    if (studentProfile) {
      editableProfileData = JSON.parse(JSON.stringify(studentProfile)); // Deep copy
      isEditing = true;
      errorMessage = ''; // Clear previous errors
      successMessage = ''; // Clear previous success messages
      passwordErrorMessage = ''; // Clear password errors
      currentPassword = '';
      newPassword = '';
      confirmNewPassword = '';
    }
  }

  async function saveChanges() {
    if (!editableProfileData) return;

    isLoading = true;
    errorMessage = '';
    successMessage = '';
    passwordErrorMessage = '';
    let profileUpdateSuccess = false;
    let passwordChangeSuccess = false;

    const token = browser ? localStorage.getItem('jwtToken') : null;
    if (!token) {
      errorMessage = "Authentication token not found. Please log in again.";
      goto('/login');
      isLoading = false;
      return;
    }

    try {
      // 1. Password Change Logic (if newPassword is provided)
      if (newPassword) {
        if (!currentPassword) {
          passwordErrorMessage = 'Please enter your current password to change it.';
          isLoading = false;
          return;
        }
        if (newPassword !== confirmNewPassword) {
          passwordErrorMessage = 'New passwords do not match.';
          isLoading = false;
          return;
        }

        // Step 1.1: Verify current password (Placeholder API call)
        // In a real app, you would have an endpoint like /api/auth/verify-password
        // For now, we'll assume a backend check. If you have an endpoint, integrate it here.
        // For demonstration, let's simulate a successful verification if currentPassword is not empty.
        // This part needs to be replaced with a real API call.
        const verifyResponse = await fetch('http://localhost:1234/api/auth/verify-password', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
          },
          body: JSON.stringify({ currentPassword }),
        });

        if (!verifyResponse.ok) {
          const errorData = await verifyResponse.json();
          passwordErrorMessage = errorData.message || 'Incorrect current password.';
          isLoading = false;
          return;
        }
        
        // If verification is part of the change-password endpoint, this separate call isn't needed.
        // Assuming verification passed or is handled by the change-password endpoint:

        // Step 1.2: Call API to change password
        const changePasswordResponse = await fetch('http://localhost:1234/api/auth/change-password', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
          },
          body: JSON.stringify({ currentPassword, newPassword }), // Send current and new
        });

        if (!changePasswordResponse.ok) {
          const errorData = await changePasswordResponse.json();
          passwordErrorMessage = errorData.message || 'Failed to change password.';
          // Do not return yet, allow profile update to proceed if desired, or handle as a full stop.
          // For now, we'll let it proceed to profile update but mark password change as failed.
        } else {
          passwordChangeSuccess = true;
        }
      }

      // 2. Profile Data Update Logic
      // Prepare data for profile update (excluding fields that shouldn't be directly updated or are part of other processes)
      const profileUpdateData = {
        mssv: editableProfileData.mssv, // Assuming mssv is not editable
        ngaySinh: editableProfileData.ngaySinh,
        diaChi: editableProfileData.diaChi,
        sdt: editableProfileData.sdt,
        // email is readonly, so not sending it for update
        // mssv, gioiTinh, lop, khoa are usually not editable by student directly
      };

      const profileUpdateResponse = await fetch('http://localhost:1234/api/student/profile/update', {
        method: 'PUT', // Or POST, depending on your API design
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify(profileUpdateData),
      });

      if (!profileUpdateResponse.ok) {
        profileUpdateSuccess = false; // Explicitly set
        try {
            const errorData = await profileUpdateResponse.json();
            errorMessage = errorData.message || 'Failed to update profile information.';
        } catch (jsonError) {
            // If parsing errorData fails (e.g., not JSON)
            errorMessage = `Failed to update profile information. Server responded with status ${profileUpdateResponse.status}.`;
        }
      } else { // Profile update API call was successful (status 2xx)
        profileUpdateSuccess = true; // Server confirmed the update was successful.
        try {
          const responseBodyText = await profileUpdateResponse.text();
          let successfullyAppliedServerResponse = false;

          if (profileUpdateResponse.status === 204 || !responseBodyText) {
            // HTTP 204 No Content, or other 2xx with an empty body.
            // No new data from server, UI should reflect submitted data.
            if (studentProfile && editableProfileData) {
              studentProfile = { ...(editableProfileData as StudentProfile) };
              // successfullyAppliedServerResponse remains false, but that's okay as we used local data.
            } else {
              console.warn("UI State issue: editableProfileData or studentProfile is null on 204/empty response.");
              // errorMessage can be set here if this is considered a client-side error state
              // For now, we assume data is saved, and UI might just not update if state is broken.
            }
          } else {
            // Body is present, try to parse and use it.
            try {
              const updatedProfileFromServer = JSON.parse(responseBodyText);
              if (typeof updatedProfileFromServer === 'object' && updatedProfileFromServer !== null) {
                if (studentProfile) { // Check studentProfile for safe spread
                  studentProfile = {
                    ...(studentProfile as StudentProfile),
                    ...updatedProfileFromServer,
                    hoTen: updatedProfileFromServer.hoTen || `${updatedProfileFromServer.ho || ''} ${updatedProfileFromServer.ten || ''}`.trim(),
                  };
                  successfullyAppliedServerResponse = true;
                } else {
                   console.error("UI State issue: studentProfile is null when processing valid server response.");
                   // Fallback to editableProfileData if studentProfile was null but editableProfileData exists
                   if (editableProfileData) {
                     studentProfile = { ...(editableProfileData as StudentProfile) };
                   }
                }
              } else {
                console.warn("Profile update successful, but server response was not a JSON object:", updatedProfileFromServer);
                // Fallback to using editableProfileData as server response is not a usable object.
              }
            } catch (parseError) {
              console.warn("Profile update successful, but failed to parse server response as JSON. Response text:", responseBodyText, "Error:", parseError);
              // Fallback to using editableProfileData as server response could not be parsed.
            }

            // If parsing or applying server response failed, and we haven't yet, try to use editableProfileData
            if (!successfullyAppliedServerResponse && studentProfile && editableProfileData) {
              // If studentProfile was updated from server response already, this won't run.
              // If studentProfile is null but editableProfileData exists, this might be a good place for it.
              // However, the above logic tries to handle studentProfile being null already.
              // This is more of a fallback if the server response was bad.
              console.log("Falling back to using submitted data for UI update due to unprocessable server response.");
              studentProfile = { ...(editableProfileData as StudentProfile) };
            } else if (!successfullyAppliedServerResponse && !studentProfile && editableProfileData) {
              // If studentProfile is null, and server response wasn't applied, use editableProfileData
               studentProfile = { ...(editableProfileData as StudentProfile) };
            }
          }
        } catch (e) {
          // This catch is now for unexpected errors during the studentProfile assignments
          // or other logic within the success block, AFTER server confirmed save.
          console.error("Error updating UI after successful profile save:", e);
          // Data is saved, but UI update itself failed.
          errorMessage = 'Profile data saved, but the display could not be refreshed due to an internal error. Please reload the page.';
        }
      }

      // 3. Finalize and set messages
      if (profileUpdateSuccess && passwordChangeSuccess) {
        successMessage = 'Profile and password updated successfully!';
        isEditing = false;
        currentPassword = '';
        newPassword = '';
        confirmNewPassword = '';
      } else if (profileUpdateSuccess && !newPassword) { // Profile updated, no password change attempted
        successMessage = 'Profile updated successfully!';
        isEditing = false;
      } else if (profileUpdateSuccess && newPassword && !passwordChangeSuccess) { // Profile updated, but password change failed
        successMessage = 'Profile updated successfully.';
        // passwordErrorMessage will already be set
        // Keep editing mode open for password correction
      } else if (!profileUpdateSuccess && passwordChangeSuccess) { // Profile update failed, but password changed
        successMessage = 'Password updated successfully!';
        errorMessage = errorMessage || 'Failed to update profile details.'; // Keep existing profile error
        // Keep editing mode open for profile correction
      } else {
        // Both failed or only profile failed and no password change attempted
        if (!errorMessage && !passwordErrorMessage) { // Should not happen if something failed
             errorMessage = "An unexpected error occurred during save."
        }
        // Error messages are already set. Keep editing mode.
      }
      
      if (profileUpdateSuccess && (!newPassword || passwordChangeSuccess)) {
        isEditing = false; // Exit edit mode only if all attempted changes were successful
        currentPassword = '';
        newPassword = '';
        confirmNewPassword = '';
      }


    } catch (error) {
      console.error("Error saving profile:", error);
      errorMessage = "An unexpected error occurred while saving. Please try again.";
      // successMessage will be empty
    } finally {
      isLoading = false;
    }
  }

  function cancelEdit() {
    isEditing = false;
    editableProfileData = null; // Clear the editable copy
    errorMessage = '';
    successMessage = '';
    passwordErrorMessage = '';
    currentPassword = '';
    newPassword = '';
    confirmNewPassword = '';
  }
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
          <a class="nav-link active" aria-current="page" href="/student/profile"><i class="bi bi-person-fill me-1"></i>Profile</a>
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
          <button class="nav-link btn btn-link" on:click={handleLogout}><i class="bi bi-box-arrow-right me-1"></i>Logout</button>
        </li>
      </ul>
    </div>
  </div>
</nav>

<svelte:head>
  <title>Student Profile</title>
</svelte:head>

<div class="container mt-4 mb-5">
  <div class="row mb-3 align-items-center">
    <div class="col">
      <h1 class="h3"><i class="bi bi-person-badge me-2"></i>Student Profile</h1>
    </div>
    {#if studentProfile && !isLoading && !errorMessage}
      <div class="col-auto">
        {#if isEditing}
          <button class="btn btn-outline-secondary me-2" on:click={cancelEdit}>
            <i class="bi bi-x-circle me-1"></i>Cancel
          </button>
          <button class="btn btn-success" on:click={saveChanges} disabled={isLoading}> <!-- Disable save button when loading -->
            <i class="bi bi-check-circle me-1"></i>Save Changes
          </button>
        {:else}
          <button class="btn btn-primary" on:click={startEdit}>
            <i class="bi bi-pencil-square me-1"></i>Edit Profile
          </button>
        {/if}
      </div>
    {/if}
  </div>

  {#if isLoading && !isEditing} <!-- Show main loading only if not already in edit mode and saving -->
    <div class="text-center mt-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
      <p class="mt-2">Loading profile...</p>
    </div>
  {:else if errorMessage && !isEditing} <!-- Show general error message only if not in edit mode (edit mode has its own error display) -->
    <div class="alert alert-danger" role="alert">
      <i class="bi bi-exclamation-triangle-fill me-2"></i>{errorMessage}
    </div>
  {/if}
  {#if successMessage && !isEditing} <!-- Show success message only if not in edit mode -->
    <div class="alert alert-success" role="alert">
      <i class="bi bi-check-circle-fill me-2"></i>{successMessage}
    </div>
  {/if}
  
  {#if !isLoading && studentProfile} <!-- Ensure studentProfile is loaded before showing card (removed !errorMessage here as edit form handles its own errors) -->
    <div class="card shadow-sm">
      <div class="card-header bg-light py-3">
        <h5 class="mb-0">
          <i class="bi bi-person-circle me-2"></i>
          {studentProfile.hoTen} - <span class="text-muted">{studentProfile.mssv}</span>
        </h5>
      </div>
      <div class="card-body p-4">
        {#if isEditing && editableProfileData}
          <form on:submit|preventDefault={saveChanges}>
            {#if errorMessage && isEditing} <!-- Error message specific to save operation -->
              <div class="alert alert-danger" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>{errorMessage}
              </div>
            {/if}
            {#if successMessage && isEditing} <!-- Success message specific to save operation -->
              <div class="alert alert-success" role="alert">
                <i class="bi bi-check-circle-fill me-2"></i>{successMessage}
              </div>
            {/if}
            <div class="row">
              <div class="col-md-6 mb-3">
                <h6 class="text-primary fw-semibold">Personal Information</h6>
                <div class="mb-3">
                  <label for="fullName" class="form-label">Full Name</label>
                  <input type="text" id="fullName" class="form-control" value={editableProfileData.hoTen} readonly disabled>
                </div>
                <div class="mb-3">
                  <label for="mssv" class="form-label">Student ID (MSSV)</label>
                  <input type="text" id="mssv" class="form-control" bind:value={editableProfileData.mssv} readonly disabled>
                </div>
                <div class="mb-3">
                  <label for="ngaySinh" class="form-label">Date of Birth</label>
                  <input type="date" id="ngaySinh" class="form-control" bind:value={editableProfileData.ngaySinh}>
                </div>
                <div class="mb-3">
                  <label for="gioiTinh" class="form-label">Gender</label>
                  <input type="text" id="gioiTinh" class="form-control" bind:value={editableProfileData.gioiTinh} readonly disabled>
                </div>
                <div class="mb-3">
                  <label for="diaChi" class="form-label">Address</label>
                  <input type="text" id="diaChi" class="form-control" bind:value={editableProfileData.diaChi}>
                </div>
                <div class="mb-3">
                  <label for="sdt" class="form-label">Phone</label>
                  <input type="tel" id="sdt" class="form-control" bind:value={editableProfileData.sdt}>
                </div>
                <div class="mb-3">
                  <label for="email" class="form-label">Email</label>
                  <input type="email" id="email" class="form-control" bind:value={editableProfileData.email} readonly disabled>
                </div>
              </div>
              <div class="col-md-6 mb-3">
                <h6 class="text-primary fw-semibold">Academic Information</h6>
                <div class="mb-3">
                  <label for="lopTen" class="form-label">Class</label>
                  <input type="text" id="lopTen" class="form-control" value={editableProfileData.lop.ten} readonly disabled>
                </div>
                <div class="mb-3">
                  <label for="khoaTen" class="form-label">Department</label>
                  <input type="text" id="khoaTen" class="form-control" value={editableProfileData.lop.khoa.ten} readonly disabled>
                </div>
              </div>
            </div>
            <h6 class="text-primary fw-semibold mt-4">Change Password</h6>
            {#if passwordErrorMessage}
              <div class="alert alert-danger p-2 small" role="alert">{passwordErrorMessage}</div>
            {/if}
            <div class="mb-3">
              <label for="currentPassword" class="form-label">Current Password</label>
              <input type="password" id="currentPassword" class="form-control" bind:value={currentPassword} placeholder="Enter current password to change">
              <small class="form-text text-muted">Leave new password fields blank if you don't want to change your password.</small>
            </div>
            <div class="mb-3">
              <label for="newPassword" class="form-label">New Password</label>
              <input type="password" id="newPassword" class="form-control" bind:value={newPassword} placeholder="Enter new password">
            </div>
            <div class="mb-3">
              <label for="confirmNewPassword" class="form-label">Confirm New Password</label>
              <input type="password" id="confirmNewPassword" class="form-control" bind:value={confirmNewPassword} placeholder="Confirm new password">
            </div>
            {#if isLoading && isEditing}
              <div class="text-center mt-3">
                <div class="spinner-border spinner-border-sm text-primary" role="status">
                  <span class="visually-hidden">Saving...</span>
                </div>
                <span class="ms-2">Saving changes...</span>
              </div>
            {/if}
             <!-- Buttons are now outside the form, handled by the top-level edit/save buttons -->
          </form>
        {:else if studentProfile} <!-- Added studentProfile check for view mode -->
          <div class="row">
            <div class="col-md-6 mb-3">
              <h6 class="text-primary fw-semibold">Personal Information</h6>
              <ul class="list-unstyled">
                <li><strong>Full Name:</strong> {studentProfile.hoTen}</li>
                <li><strong>Student ID (MSSV):</strong> {studentProfile.mssv}</li>
                <li><strong>Date of Birth:</strong> {studentProfile.ngaySinh ? new Date(studentProfile.ngaySinh).toLocaleDateString() : 'N/A'}</li>
                <li><strong>Gender:</strong> {studentProfile.gioiTinh}</li>
                <li><strong>Address:</strong> {studentProfile.diaChi || 'N/A'}</li>
                <li><strong>Phone:</strong> {studentProfile.sdt || 'N/A'}</li>
                <li><strong>Email:</strong> {studentProfile.email}</li>
              </ul>
            </div>
            <div class="col-md-6 mb-3">
              <h6 class="text-primary fw-semibold">Academic Information</h6>
              <ul class="list-unstyled">
                <li><strong>Class:</strong> {studentProfile.lop.ten}</li>
                <li><strong>Department:</strong> {studentProfile.lop.khoa.ten}</li>
              </ul>
            </div>
          </div>
        {/if}
      </div>
    </div>
  {:else if !isLoading && !errorMessage && !studentProfile} <!-- Condition for no profile data if not loading and no error -->
    <div class="alert alert-warning" role="alert">
      <i class="bi bi-info-circle-fill me-2"></i>No profile data available or failed to load.
    </div>
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
  
  .list-unstyled li {
    padding: 0.3rem 0;
  }
  .list-unstyled strong {
    min-width: 120px; /* Adjusted for better alignment */
    display: inline-block;
    font-weight: 600; /* Changed from 500 */
  }
  .form-label {
    font-weight: 500;
  }
  .form-control[readonly] {
    background-color: #e9ecef;
    opacity: 1;
  }
  .form-control:disabled { /* Ensure disabled fields also have a distinct style */
    background-color: #e9ecef;
    opacity: 0.7; /* Slightly different opacity to distinguish from just readonly */
  }
</style>
