<script>
    import { page } from '$app/stores';
    import { onMount } from 'svelte';
    import { goto } from '$app/navigation';

    $: currentPath = $page.url.pathname;

    let loading = true;
    let error = null;
    let successMessage = "";
    let deleteMessage = "";
    let updateMessage = "";
    
    // Thông tin người dùng
    let userInfo = {
        name: "Đang tải...",
        role: "..."
    };
    
    // Danh sách sinh viên
    let students = [];
    // Danh sách lớp
    let classes = [];
    
    // Sinh viên mới hoặc cần cập nhật
    let newStudent = {
        mssv: "",
        ho: "",
        ten: "",
        ngaySinh: "",
        gioiTinh: "Nam",
        diaChi: "",
        sdt: "",
        email: "",
        lopId: null,
        matKhau: ""
    };
    
    // Hiển thị form
    let showAddForm = false;
    let showUpdateForm = false;
   

    // Khởi tạo trang
    onMount(async () => {
        const token = localStorage.getItem('jwtToken');
        if (!token) {
            goto('/');
            return;
        }
        
        const userDataString = localStorage.getItem('user');
        if (userDataString) {
            try {
                const userData = JSON.parse(userDataString);
                const userRole = userData.chucVu || userData.chuc_vu;
                if (userRole !== 'Nhan vien') {
                    alert('Bạn không có quyền truy cập trang này!');
                    goto('/unauthorized');
                    return;
                }
                
                userInfo = {
                    name: `${userData.ho} ${userData.ten}`,
                    role: userRole
                };
                
                console.log("Authentication successful, fetching students");
                await Promise.all([fetchStudents(), fetchClasses()]);
            } catch (error) {
                console.error("Lỗi phân tích dữ liệu người dùng:", error);
                goto('/');
            }
        } else {
            await fetchUserInfo(token);
        }
    });
    
    // Lấy thông tin người dùng
    async function fetchUserInfo(token) {
        try {
            const response = await fetch('http://localhost:1234/api/auth/me', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (response.ok) {
                const userData = await response.json();
                localStorage.setItem('user', JSON.stringify(userData));
                
                if (userData.chucVu !== 'Nhan vien') {
                    alert('Bạn không có quyền truy cập trang này!');
                    goto('/unauthorized');
                    return;
                }
                
                userInfo = {
                    name: `${userData.ho} ${userData.ten}`,
                    role: userData.chucVu,
                };
                
                await Promise.all([fetchStudents(), fetchClasses()]);
            } else {
                console.error("Lỗi tải thông tin người dùng");
                localStorage.removeItem('jwtToken');
                localStorage.removeItem('user');
                goto('/');
            }
        } catch (error) {
            console.error("Lỗi tải thông tin người dùng:", error);
            localStorage.removeItem('jwtToken');
            localStorage.removeItem('user');
            goto('/');
        }
    }
    
    // Hiện form thêm mới
    function openAddForm() {
        newStudent = {
            mssv: "",
            ho: "",
            ten: "",
            ngaySinh: "",
            gioiTinh: "Nam",
            diaChi: "",
            sdt: "",
            email: "",
            lopId: null,
            matKhau: ""
        };
        showAddForm = true;
    }
    
    // Hiện form cập nhật
    function openUpdateForm(student) {
        console.log("Student giới tính before:", student.gioiTinh);
        newStudent = {
            mssv: student.mssv,
            ho: student.ho,
            ten: student.ten,
            ngaySinh: student.ngaySinh, 
            gioiTinh: student.gioiTinh,
            diaChi: student.diaChi,    
            sdt: student.sdt,
            email: student.email,
            lopId: student.lopId , 
            matKhau: "" 
        };
        console.log("newStudent giới tính after:", newStudent.gioiTinh);
        showUpdateForm = true;
    }
    
    // Fetch danh sách lớp
    async function fetchClasses() {
        try {
            const token = localStorage.getItem('jwtToken');
            console.log("Token used for fetchClasses:", token);
            
            const response = await fetch('http://localhost:1234/api/lop/all', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            console.log("Classes API response status:", response.status);
            
            if (response.status === 401) {
                console.error("Authentication failed - Token invalid or expired");
                localStorage.removeItem('jwtToken');
                localStorage.removeItem('user');
                goto('/');
                return;
            }
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            classes = await response.json();
            console.log("Classes data received:", classes);
        } catch (err) {
            console.error("Lỗi khi tải danh sách lớp:", err);
            error = err.message;
        }
    }
    
    // Fetch danh sách sinh viên
    async function fetchStudents() {
        loading = true;
        error = null;
        
        try {
            const token = localStorage.getItem('jwtToken');
            console.log("Token used for fetchStudents:", token);
            
            const response = await fetch('http://localhost:1234/api/student', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            console.log("Student API response status:", response.status);
            
            if (response.status === 401) {
                console.error("Authentication failed - Token invalid or expired");
                localStorage.removeItem('jwtToken');
                localStorage.removeItem('user');
                goto('/');
                return;
            }
            
            if (response.status === 403) {
                throw new Error("Bạn không có quyền truy cập trang này");
            }
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const data = await response.json();
            console.log("Raw student data:", data);
            
            // Xử lý dữ liệu từ định dạng API
            if (Array.isArray(data)) {
                // Trích xuất thông tin sinh viên từ mỗi phần tử
                students = data.map(item => {
                    // Nếu có cấu trúc lồng nhau sinhVien
                    if (item.sinhVien) {
                        return {
                            mssv: item.sinhVien.mssv,
                            ho: item.sinhVien.ho,
                            ten: item.sinhVien.ten,
                            ngaySinh: item.sinhVien.ngaySinh,
                            gioiTinh: item.sinhVien.gioiTinh,
                            diaChi: item.sinhVien.diaChi,
                            sdt: item.sinhVien.sdt,
                            email: item.sinhVien.email,
                            lopId: item.sinhVien.lopId
                        };
                    }
                    // Nếu là mảng đơn giản
                    return item;
                });
            } else {
                console.error("API không trả về đúng định dạng mảng!");
                students = [];
            }
            
            console.log("Processed student data:", students);
            
        } catch (err) {
            console.error("Lỗi khi tải danh sách sinh viên:", err);
            error = err.message;
        } finally {
            loading = false;
        }
    }
    
    // Thêm sinh viên mới
    async function addStudent(event) {
        try {
            // Kiểm tra MSSV trước khi gửi yêu cầu
            if (newStudent.mssv.trim() === "") {
                alert("Vui lòng nhập MSSV!");
                return;
            }

            const token = localStorage.getItem('jwtToken');
            const response = await fetch('http://localhost:1234/api/student', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    mssv: newStudent.mssv,
                    ho: newStudent.ho,
                    ten: newStudent.ten,
                    ngaySinh: newStudent.ngaySinh,
                    gioiTinh: newStudent.gioiTinh,
                    diaChi: newStudent.diaChi,
                    sdt: newStudent.sdt,
                    email: newStudent.email,
                    lopId: Number(newStudent.lopId) || null,
                    pwdHash: newStudent.matKhau
                })
            });

            if (!response.ok) {
            let errorMessage = response.statusText;
            if (response.status === 409) {
                const errorData = await response.json();
                const details = errorData.details;
                let specificMessages = [];
                if (details.mssv) specificMessages.push("MSSV đã tồn tại!");
                if (details.email) specificMessages.push("Email đã tồn tại!");
                if (details.sdt) specificMessages.push("Số điện thoại đã tồn tại!");
                errorMessage = specificMessages.join('\n') || errorData.message || "MSSV, email hoặc số điện thoại đã tồn tại!";
            } else if (response.status === 403) {
                const errorData = await response.json();
                errorMessage = errorData.message || "Bạn không có quyền thực hiện hành động này";
            } else {
                console.error(`Lỗi khi thêm sinh viên: ${response.status} - ${response.statusText}`);
            }
            alert(errorMessage); 
            return;
        }

        successMessage = `Sinh viên "${newStudent.ho} ${newStudent.ten}" đã được thêm thành công!`;
        setTimeout(() => { successMessage = ''; }, 3000);
        showAddForm = false;
        await fetchStudents();

    } catch (error) {
        console.error("Lỗi khi thêm sinh viên (catch):", error);
        alert(`Lỗi: ${error.message}`);
    }
}

    async function updateStudent() {
        try {
            const token = localStorage.getItem('jwtToken');
            const response = await fetch(`http://localhost:1234/api/student/${newStudent.mssv}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    mssv: newStudent.mssv,
                    ho: newStudent.ho,
                    ten: newStudent.ten,
                    ngaySinh: newStudent.ngaySinh,
                    gioiTinh: newStudent.gioiTinh,
                    diaChi: newStudent.diaChi,
                    sdt: newStudent.sdt,
                    email: newStudent.email,
                    lopId: Number(newStudent.lopId) || null,
                    pwdHash: newStudent.matKhau
                })
            });

            if (!response.ok) {
                let errorMessage = response.statusText;
                if (response.status === 409) { 
                    const errorData = await response.json();
                    const details = errorData.details;
                    let specificMessages = [];
                    if (details.mssv) specificMessages.push("MSSV đã tồn tại!");
                    if (details.email) specificMessages.push("Email đã tồn tại!");
                    if (details.sdt) specificMessages.push("Số điện thoại đã tồn tại!");
                    errorMessage = specificMessages.join('\n') || errorData.message || "MSSV, email hoặc số điện thoại đã tồn tại!";
                } else if (response.status === 403) {
                    const errorData = await response.json();
                    errorMessage = errorData.message || "Bạn không có quyền thực hiện hành động này";
                } else {
                    console.error(`Lỗi khi cập nhật sinh viên: ${response.status} - ${response.statusText}`);
                }
                alert(errorMessage); 
                return;
            }

            updateMessage = `Thông tin sinh viên "${newStudent.ho} ${newStudent.ten}" đã được cập nhật!`;
            setTimeout(() => { updateMessage = ''; }, 3000);
            showUpdateForm = false;
            await fetchStudents();

        } catch (error) {
            console.error("Lỗi khi cập nhật sinh viên (catch):", error);
            alert(`Lỗi: ${error.message}`); 
        }
    }

    async function deleteStudent(mssv) {
        if (confirm("Bạn có chắc chắn muốn xóa sinh viên này không?")) {
            try {
                const token = localStorage.getItem('jwtToken');
                const response = await fetch(`http://localhost:1234/api/student/${mssv}`, {
                    method: 'DELETE',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });

                if (response.status === 403) {
                    const errorText = await response.text();
                    const errorMessage = errorText || "Bạn không có quyền thực hiện hành động này";
                    console.error("Lỗi khi xóa sinh viên (403):", errorMessage);
                    console.log(`Lỗi: ${errorMessage}`);
                    return;
                }

                if (!response.ok) {
                    let errorMessage = `HTTP error! status: ${response.status}`;
                    try {
                        const errorData = await response.json();
                        errorMessage = errorData.message || errorMessage;
                    } catch (e) {}
                    console.error("Lỗi khi xóa sinh viên:", errorMessage);
                    console.log(`Lỗi: ${errorMessage}`);
                    return;
                }

                deleteMessage = "Sinh viên đã được xóa thành công!";
                setTimeout(() => { deleteMessage = ''; }, 3000);
                await fetchStudents();

            } catch (error) {
                console.error("Lỗi khi xóa sinh viên (catch):", error);
                console.log(`Lỗi: ${error.message}`);
            }
        }
    }
    
    // Format date for display
    function formatDate(dateString) {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleDateString('vi-VN');
    }
    
    // Get class name by id
    function getClassName(classId) {
        const foundClassItem = classes.find(item => item.lop.id === classId);
        return foundClassItem ? foundClassItem.lop.ten : 'Chưa phân lớp';
    }
    
    // Đăng xuất
    function handleLogout() {
        localStorage.removeItem('jwtToken');
        localStorage.removeItem('user');
        window.location.href = '/';
    }
</script>

<div class="app-container">
    <!-- Sidebar/Navigation -->
    <nav class="sidebar">
        <div class="logo-container">
            <img
                src="/images/logos/ptit-logo-inv.png"
                alt="PTIT Logo"
                class="logo"
            />
            <h2 class="app-name">PTIT Admin</h2>
        </div>

        <div class="nav-links">
          <!-- Mục Khoa -->
          <a href="/admin/dashboard/khoa" class:active={currentPath.includes("/admin/dashboard/khoa")}>
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M8.277.084a.5.5 0 0 0-.554 0l-7.5 5A.5.5 0 0 0 .5 6h1.875v7H1.5a.5.5 0 0 0 0 1h13a.5.5 0 1 0 0-1h-.875V6H15.5a.5.5 0 0 0 .277-.916l-7.5-5zM12.375 6v7h-1.25V6h1.25zm-2.5 0v7h-1.25V6h1.25zm-2.5 0v7h-1.25V6h1.25zm-2.5 0v7h-1.25V6h1.25zM8 4a1 1 0 1 1 0-2 1 1 0 0 1 0 2z"/>
              </svg>
              <span>Khoa</span>
          </a>
          
          <!-- Mục Lớp -->
          <a href="/admin/dashboard/lop" class:active={currentPath.includes("/admin/dashboard/lop")}>
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 16 16">
                    <path d="M14.5 3a.5.5 0 0 1 .5.5v9a.5.5 0 0 1-.5.5h-13a.5.5 0 0 1-.5-.5v-9a.5.5 0 0 1 .5-.5h13zm-13-1A1.5 1.5 0 0 0 0 3.5v9A1.5 1.5 0 0 0 1.5 14h13a1.5 1.5 0 0 0 1.5-1.5v-9A1.5 1.5 0 0 0 14.5 2h-13z"/>
                    <path d="M3 5.5a.5.5 0 0 1 .5-.5h9a.5.5 0 0 1 0 1h-9a.5.5 0 0 1-.5-.5zM3 8a.5.5 0 0 1 .5-.5h9a.5.5 0 0 1 0 1h-9A.5.5 0 0 1 3 8zm0 2.5a.5.5 0 0 1 .5-.5h6a.5.5 0 0 1 0 1h-6a.5.5 0 0 1-.5-.5z"/>
                </svg>
                <span>Lớp</span>
            </a>
          
          <!-- Mục Môn học -->
          <a href="/admin/dashboard/monhoc" class:active={currentPath.includes("/admin/dashboard/monhoc")}>
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M1 2.828c.885-.37 2.154-.769 3.388-.893 1.33-.134 2.458.063 3.112.752v9.746c-.935-.53-2.12-.603-3.213-.493-1.18.12-2.37.461-3.287.811V2.828zm7.5-.141c.654-.689 1.782-.886 3.112-.752 1.234.124 2.503.523 3.388.893v9.923c-.918-.35-2.107-.692-3.287-.81-1.094-.111-2.278-.039-3.213.492V2.687zM8 1.783C7.015.936 5.587.81 4.287.94c-1.514.153-3.042.672-3.994 1.105A.5.5 0 0 0 0 2.5v11a.5.5 0 0 0 .707.455c.882-.4 2.303-.881 3.68-1.02 1.409-.142 2.59.087 3.223.877a.5.5 0 0 0 .78 0c.633-.79 1.814-1.019 3.222-.877 1.378.139 2.8.62 3.681 1.02A.5.5 0 0 0 16 13.5v-11a.5.5 0 0 0-.293-.455c-.952-.433-2.48-.952-3.994-1.105C10.413.809 8.985.936 8 1.783z"/>
              </svg>
              <span>Môn học</span>
          </a>
          
          <!-- Mục Học kỳ -->
          <a href="/admin/dashboard/hocky" class:active={currentPath.includes("/admin/dashboard/hocky")}>
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M3.5 0a.5.5 0 0 1 .5.5V1h8V.5a.5.5 0 0 1 1 0V1h1a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V3a2 2 0 0 1 2-2h1V.5a.5.5 0 0 1 .5-.5zM1 4v10a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V4H1z"/>
              </svg>
              <span>Học kỳ</span>
          </a>
          
          <!-- Mục Phòng -->
          <a href="/admin/dashboard/phong" class:active={currentPath.includes("/admin/dashboard/phong")}>
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 16 16">
                  <path fill-rule="evenodd" d="M2 13.5V7h1v6.5a.5.5 0 0 0 .5.5h9a.5.5 0 0 0 .5-.5V7h1v6.5a1.5 1.5 0 0 1-1.5 1.5h-9A1.5 1.5 0 0 1 2 13.5zm11-11V6l-2-2V2.5a.5.5 0 0 1 .5-.5h1a.5.5 0 0 1 .5.5z"/>
                  <path fill-rule="evenodd" d="M7.293 1.5a1 1 0 0 1 1.414 0l6.647 6.646a.5.5 0 0 1-.708.708L8 2.207 1.354 8.854a.5.5 0 1 1-.708-.708L7.293 1.5z"/>
              </svg>
              <span>Phòng</span>
          </a>
                    
          <!-- Mục Đợt đăng ký lớp tín chỉ -->
          <a href="/admin/dashboard/dotdangky" class:active={currentPath.includes("/admin/dashboard/dotdangky")}>
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M14 4.5V14a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V2a2 2 0 0 1 2-2h5.5L14 4.5zm-3 0A1.5 1.5 0 0 1 9.5 3V1H4a1 1 0 0 0-1 1v12a1 1 0 0 0 1 1h8a1 1 0 0 0 1-1V4.5h-2z"/>
                  <path d="M4.5 12.5A.5.5 0 0 1 5 12h3a.5.5 0 0 1 0 1H5a.5.5 0 0 1-.5-.5zm0-2A.5.5 0 0 1 5 10h6a.5.5 0 0 1 0 1H5a.5.5 0 0 1-.5-.5z"/>
              </svg>
              <span>Đợt đăng ký lớp tín chỉ</span>
          </a>
          
          <!-- Mục Đợt đóng học phí -->
          <a href="/admin/dashboard/dotdonghocphi" class:active={currentPath.includes("/admin/dashboard/dotdonghocphi")}>
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M12.136.326A1.5 1.5 0 0 1 14 1.78V3h.5A1.5 1.5 0 0 1 16 4.5v9a1.5 1.5 0 0 1-1.5 1.5h-13A1.5 1.5 0 0 1 0 13.5v-9a1.5 1.5 0 0 1 1.432-1.499L12.136.326zM5.562 3H13V1.78a.5.5 0 0 0-.621-.484L5.562 3zM1.5 4a.5.5 0 0 0-.5.5v9a.5.5 0 0 0 .5.5h13a.5.5 0 0 0 .5-.5v-9a.5.5 0 0 0-.5-.5h-13z"/>
              </svg>
              <span>Đợt đóng học phí</span>
          </a>

          <!-- Học phí -->
          <a href="/admin/dashboard/hocphi" class:active={currentPath.includes("/admin/dashboard/hocphi")}>
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M4 10.781c.148 1.667 1.513 2.85 3.591 3.003V15h1.043v-1.216c2.27-.179 3.678-1.438 3.678-3.3 0-1.59-.947-2.51-2.956-3.028l-.722-.187V3.467c1.122.11 1.879.714 2.07 1.616h1.47c-.166-1.6-1.54-2.748-3.54-2.875V1H7.591v1.233c-1.939.23-3.27 1.472-3.27 3.156 0 1.454.966 2.483 2.661 2.917l.61.162v4.031c-1.149-.17-1.94-.8-2.131-1.718H4zm3.391-3.836c-1.043-.263-1.6-.825-1.6-1.616 0-.944.704-1.641 1.8-1.828v3.495l-.2-.05zm1.591 1.872c1.287.323 1.852.859 1.852 1.769 0 1.097-.826 1.828-2.2 1.939V8.73l.348.086z"/>
              </svg>
              <span>Học phí</span>
          </a>

          <!-- Tạo thông báo -->
          <a href="/admin/dashboard/thongbao" class:active={currentPath.includes("/admin/dashboard/thongbao")}>
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M8 16a2 2 0 0 0 2-2H6a2 2 0 0 0 2 2zm.995-14.901a1 1 0 1 0-1.99 0A5.002 5.002 0 0 0 3 6c0 1.098-.5 6-2 7h14c-1.5-1-2-5.902-2-7 0-2.42-1.72-4.44-4.005-4.901z"/>
              </svg>
              <span>Tạo thông báo</span>
          </a>
          
          <a
              href="/admin/dashboard"
              class:active={currentPath === "/admin/dashboard" || currentPath === "/admin/dashboard/"}
          >
              <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="18"
                  height="18"
                  fill="currentColor"
                  viewBox="0 0 16 16"
              >
                  <path
                      d="M7 14s-1 0-1-1 1-4 5-4 5 3 5 4-1 1-1 1H7Zm4-6a3 3 0 1 0 0-6 3 3 0 0 0 0 6Zm-5.784 6A2.238 2.238 0 0 1 5 13c0-1.355.68-2.75 1.936-3.72A6.325 6.325 0 0 0 5 9c-4 0-5 3-5 4s1 1 1 1h4.216ZM4.5 8a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Z"
                  />
              </svg>
              <span>Nhân viên</span>
          </a>

          <a
                href="/admin/dashboard/sinhvien"
                class:active={currentPath.includes("/admin/dashboard/sinhvien")}
            >
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="18"
                    height="18"
                    fill="currentColor"
                    viewBox="0 0 16 16"
                >
                    <path d="M8 0a.5.5 0 0 1 .473.337L9.046 2H14a2 2 0 0 1 2 2v7a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h4.954l.572-1.663A.5.5 0 0 1 8 0zM5 5a2 2 0 1 0 0 4 2 2 0 0 0 0-4zM1.5 8.5a3 3 0 1 1 6 0 3 3 0 0 1-6 0zM9 9c0-1.5 2.5-2 4-2s4 .5 4 2v1H9V9z"/>
                </svg>
                <span>Sinh viên</span>
            </a>
      </div>

        <div class="user-section">
            <div class="user-info">
                <div class="user-avatar">
                    <span>{userInfo.name.charAt(0)}</span>
                </div>
                <div class="user-details">
                    <span class="user-name">{userInfo.name}</span>
                    <span class="user-role">{userInfo.role}</span>
                </div>
            </div>
            <button class="logout-button" on:click={handleLogout}>
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="16"
                    height="16"
                    fill="currentColor"
                    viewBox="0 0 16 16"
                >
                    <path
                        fill-rule="evenodd"
                        d="M10 12.5a.5.5 0 0 1-.5.5h-8a.5.5 0 0 1-.5-.5v-9a.5.5 0 0 1 .5-.5h8a.5.5 0 0 1 .5.5v2a.5.5 0 0 0 1 0v-2A1.5 1.5 0 0 0 9.5 2h-8A1.5 1.5 0 0 0 0 3.5v9A1.5 1.5 0 0 0 1.5 14h8a1.5 1.5 0 0 0 1.5-1.5v-2a.5.5 0 0 0-1 0v2z"
                    />
                    <path
                        fill-rule="evenodd"
                        d="M15.854 8.354a.5.5 0 0 0 0-.708l-3-3a.5.5 0 0 0-.708.708L14.293 7.5H5.5a.5.5 0 0 0 0 1h8.793l-2.147 2.146a.5.5 0 0 0 .708.708l3-3z"
                    />
                </svg>
                <span>Đăng xuất</span>
            </button>
        </div>
    </nav>

    <!-- Main Content Area -->
    <main class="content">
        <header class="top-bar">
            <h1 class="page-title">Quản Lý Sinh Viên</h1>
            <div class="top-actions">
                <button class="action-button">
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="16"
                        height="16"
                        fill="currentColor"
                        viewBox="0 0 16 16"
                    >
                        <path
                            d="M8 16a2 2 0 0 0 2-2H6a2 2 0 0 0 2 2zM8 1.918l-.797.161A4.002 4.002 0 0 0 4 6c0 .628-.134 2.197-.459 3.742-.16.767-.376 1.566-.663 2.258h10.244c-.287-.692-.502-1.49-.663-2.258C12.134 8.197 12 6.628 12 6a4.002 4.002 0 0 0-3.203-3.92L8 1.917zM14.22 12c.223.447.481.801.78 1H1c.299-.199.557-.553.78-1C2.68 10.2 3 6.88 3 6c0-2.42 1.72-4.44 4.005-4.901a1 1 0 1 1 1.99 0A5.002 5.002 0 0 1 13 6c0 .88.32 4.2 1.22 6z"
                        />
                    </svg>
                </button>
                <button class="action-button">
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="16"
                        height="16"
                        fill="currentColor"
                        viewBox="0 0 16 16"
                    >
                        <path
                            d="M8 4.754a3.246 3.246 0 1 0 0 6.492 3.246 3.246 0 0 0 0-6.492zM5.754 8a2.246 2.246 0 1 1 4.492 0 2.246 2.246 0 0 1-4.492 0z"
                        />
                        <path
                            d="M9.796 1.343c-.527-1.79-3.065-1.79-3.592 0l-.094.319a.873.873 0 0 1-1.255.52l-.292-.16c-1.64-.892-3.433.902-2.54 2.541l.159.292a.873.873 0 0 1-.52 1.255l-.319.094c-1.79.527-1.79 3.065 0 3.592l.319.094a.873.873 0 0 1 .52 1.255l-.16.292c-.892 1.64.901 3.434 2.541 2.54l.292-.159a.873.873 0 0 1 1.255.52l.094.319c.527 1.79 3.065 1.79 3.592 0l.094-.319a.873.873 0 0 1 1.255-.52l.292.16c1.64.893 3.434-.902 2.54-2.541l-.159-.292a.873.873 0 0 1 .52-1.255l.319-.094c1.79-.527 1.79-3.065 0-3.592l-.319-.094a.873.873 0 0 1-.52-1.255l.16-.292c.893-1.64-.902-3.433-2.541-2.54l-.292.159a.873.873 0 0 1-1.255-.52l-.094-.319z"
                        />
                    </svg>
                </button>
            </div>
        </header>

        <div class="page-content">
            <div class="users-container">
                <!-- Header with Add button -->
                <div class="section-header">
                    <h2>Danh Sách Sinh Viên</h2>
                    <button class="primary-button" on:click={openAddForm}>
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            width="16"
                            height="16"
                            fill="currentColor"
                            viewBox="0 0 16 16"
                        >
                            <path
                                d="M8 0a1 1 0 0 1 1 1v6h6a1 1 0 1 1 0 2H9v6a1 1 0 1 1-2 0V9H1a1 1 0 0 1 0-2h6V1a1 1 0 0 1 1-1z"
                            />
                        </svg>
                        Thêm Sinh Viên
                    </button>
                </div>

                {#if successMessage}
                    <div class="success-message">{successMessage}</div>
                {/if}

                {#if deleteMessage}
                    <div class="delete-message">{deleteMessage}</div>
                {/if}

                {#if updateMessage}
                    <div class="update-message">{updateMessage}</div>
                {/if}

                <!-- Loading and error states -->
                {#if loading}
                    <div class="loading-container">
                        <div class="loading-spinner"></div>
                        <p>Đang tải danh sách sinh viên...</p>
                    </div>
                {:else if error}
                    <div class="error-container">
                        <p>Lỗi: {error}</p>
                        <button class="secondary-button" on:click={fetchStudents}
                            >Thử lại</button
                        >
                    </div>
                {:else if students.length === 0}
                    <div class="empty-container">
                        <p>Không tìm thấy sinh viên nào.</p>
                    </div>
                {:else}
                    <!-- Students Table -->
                    <div class="table-responsive">
                        <table class="users-table">
                            <thead>
                                <tr>
                                    <th>MSSV</th>
                                    <th>Họ và tên</th>
                                    <th>Ngày sinh</th>
                                    <th>Giới tính</th>
                                    <th>Email</th>
                                    <th>Số điện thoại</th>
                                    <th>Lớp</th>
                                    <th></th>
                                </tr>
                            </thead>
                            <tbody>
                                {#each students as student}
                                    <tr>
                                        <td>{student.mssv}</td>
                                        <td>{student.ho} {student.ten}</td>
                                        <td>{formatDate(student.ngaySinh)}</td>
                                        <td>{student.gioiTinh === 'Nu' ? 'Nữ' : 'Nam'}</td>
                                        <td>{student.email}</td>
                                        <td>{student.sdt}</td>
                                        <td>{getClassName(student.lopId)}</td>
                                        <td class="actions">
                                            <button
                                                class="action-btn edit"
                                                on:click={() => openUpdateForm(student)}
                                            >
                                                <svg
                                                    xmlns="http://www.w3.org/2000/svg"
                                                    width="16"
                                                    height="16"
                                                    fill="currentColor"
                                                    viewBox="0 0 16 16"
                                                >
                                                    <path
                                                        d="M12.146.146a.5.5 0 0 1 .708 0l3 3a.5.5 0 0 1 0 .708l-10 10a.5.5 0 0 1-.168.11l-5 2a.5.5 0 0 1-.65-.65l2-5a.5.5 0 0 1 .11-.168l10-10zM11.207 2.5 13.5 4.793 14.793 3.5 12.5 1.207 11.207 2.5zm1.586 3L10.5 3.207 4 9.707V10h.5a.5.5 0 0 1 .5.5v.5h.5a.5.5 0 0 1 .5.5v.5h.293l6.5-6.5zm-9.761 5.175-.106.106-1.528 3.821 3.821-1.528.106-.106A.5.5 0 0 1 5 12.5V12h-.5a.5.5 0 0 1-.5-.5V11h-.5a.5.5 0 0 1-.468-.325z"
                                                    />
                                                </svg>
                                            </button>
                                            <button
                                                class="action-btn delete"
                                                on:click={() => deleteStudent(student.mssv)}
                                            >
                                                <svg
                                                    xmlns="http://www.w3.org/2000/svg"
                                                    width="16"
                                                    height="16"
                                                    fill="currentColor"
                                                    viewBox="0 0 16 16"
                                                >
                                                    <path
                                                        d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5zm3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0V6z"
                                                    />
                                                    <path
                                                        fill-rule="evenodd"
                                                        d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1v1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4H4.118zM2.5 3V2h11v1h-11z"
                                                    />
                                                </svg>
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
    </main>

    <!-- Add Student Modal -->
    {#if showAddForm}
        <div class="modal-overlay">
            <div class="modal">
                <div class="modal-header">
                    <h3>Thêm Sinh Viên Mới</h3>
                    <button
                        class="close-button"
                        on:click={() => (showAddForm = false)}>×</button
                    >
                </div>
                <div class="modal-body">
                    <form on:submit|preventDefault={addStudent}>
                        <div class="form-group">
                                <label for="mssv">MSSV</label>
                                <input type="text" id="mssv" bind:value={newStudent.mssv} required />
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="ho">Họ</label>
                                <input type="text" id="ho" bind:value={newStudent.ho} required />
                            </div>
                            <div class="form-group">
                                <label for="ten">Tên</label>
                                <input type="text" id="ten" bind:value={newStudent.ten} required />
                            </div>
                        </div>
                        
                        <div class="form-row">
                            <div class="form-group">
                                <label for="ngay_sinh">Ngày sinh</label>
                                <input type="date" id="ngay_sinh" bind:value={newStudent.ngaySinh} required />
                            </div>
                            <div class="form-group">
                                <label for="gioi_tinh">Giới tính</label>
                                <select id="gioi_tinh" bind:value={newStudent.gioiTinh} required>
                                    <option value="Nam">Nam</option>
                                    <option value="Nữ">Nữ</option>
                                    <option value="Khác">Khác</option>
                                </select>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="dia_chi">Địa chỉ</label>
                            <input type="text" id="dia_chi" bind:value={newStudent.diaChi} required />
                        </div>
                        
                        <div class="form-row">
                            <div class="form-group">
                                <label for="email">Email</label>
                                <input type="email" id="email" bind:value={newStudent.email} required />
                            </div>
                            <div class="form-group">
                                <label for="sdt">Số điện thoại</label>
                                <input type="tel" id="sdt" bind:value={newStudent.sdt} required />
                            </div>
                        </div>
                        
                        <div class="form-group">
                            <label for="lop_id">Lớp</label>
                            <select id="lop_id" bind:value={newStudent.lopId}>
                                <option value={null}>-- Chưa phân lớp --</option>
                                {#each classes as classItem}
                                    <option value={classItem.lop.id}>{classItem.lop.ten}</option>
                                {/each}
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="mat_khau">Mật khẩu</label>
                            <input type="text" id="mat_khau" bind:value={newStudent.matKhau} required />
                        </div>

                        
                        <div class="modal-actions">
                            <button type="submit" class="primary-button">Thêm Sinh Viên</button>
                            <button
                                type="button"
                                class="secondary-button"
                                on:click={() => (showAddForm = false)}
                                >Hủy</button
                            >
                        </div>
                    </form>
                </div>
            </div>
        </div>
    {/if}

    <!-- Edit Student Modal -->
    {#if showUpdateForm}
        <div class="modal-overlay">
            <div class="modal">
                <div class="modal-header">
                    <h3>Chỉnh Sửa Thông Tin Sinh Viên</h3>
                    <button
                        class="close-button"
                        on:click={() => (showUpdateForm = false)}>×</button
                    >
                </div>
                <div class="modal-body">
                    <form on:submit|preventDefault={updateStudent}>
                        <div class="form-group">
                            <label>MSSV</label>
                            <input type="text" value={newStudent.mssv} disabled />
                        </div>
                        
                        <div class="form-row">
                            <div class="form-group">
                                <label for="edit-ho">Họ</label>
                                <input type="text" id="edit-ho" bind:value={newStudent.ho} required />
                            </div>
                            <div class="form-group">
                                <label for="edit-ten">Tên</label>
                                <input type="text" id="edit-ten" bind:value={newStudent.ten} required />
                            </div>
                        </div>
                        
                        <div class="form-row">
                            <div class="form-group">
                                <label for="edit-ngay_sinh">Ngày sinh</label>
                                <input type="date" id="edit-ngay_sinh" bind:value={newStudent.ngaySinh} required />
                            </div>
                            <div class="form-group">
                                <label for="edit-gioi_tinh">Giới tính</label>
                                <select id="edit-gioi_tinh" bind:value={newStudent.gioiTinh} required>
                                    <option value="Nam">Nam</option>
                                    <option value="Nu">Nữ</option>
                                </select>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="edit-dia_chi">Địa chỉ</label>
                            <input type="text" id="edit-dia_chi" bind:value={newStudent.diaChi} required />
                        </div>
                        
                        <div class="form-row">
                            <div class="form-group">
                                <label for="edit-email">Email</label>
                                <input type="email" id="edit-email" bind:value={newStudent.email} required />
                            </div>
                            <div class="form-group">
                                <label for="edit-sdt">Số điện thoại</label>
                                <input type="tel" id="edit-sdt" bind:value={newStudent.sdt} required />
                            </div>
                        </div>
                        
                        <div class="form-group">
                            <label for="edit-lop_id">Lớp</label>
                            <select id="edit-lop_id" bind:value={newStudent.lopId}>
                                <option value={null}>-- Chưa phân lớp --</option>
                                {#each classes as classItem}
                                    <option value={classItem.lop.id}>{classItem.lop.ten}</option>
                                {/each}
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="edit-matKhau">Mật khẩu mới (để trống nếu không đổi)</label>
                            <input 
                                type="password" 
                                id="edit-matKhau" 
                                bind:value={newStudent.matKhau} 
                                placeholder="Để trống nếu không muốn đổi mật khẩu"
                            />
                        </div>
                        
                        
                        <div class="modal-actions">
                            <button type="submit" class="primary-button">Lưu Thay Đổi</button>
                            <button
                                type="button"
                                class="secondary-button"
                                on:click={() => (showUpdateForm = false)}
                                >Hủy</button
                            >
                        </div>
                    </form>
                </div>
            </div>
        </div>
    {/if}
</div>

<style>
    :global(body) {
        margin: 0;
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
            Oxygen, Ubuntu, Cantarell, "Open Sans", "Helvetica Neue", sans-serif;
        background-color: #f1f5f9;
        color: #1e293b;
    }

    .app-container {
        display: flex;
        min-height: 100vh;
    }

    /* Sidebar styles */
    .sidebar {
        width: 260px;
        background-color: #1e3a8a;
        color: white;
        display: flex;
        flex-direction: column;
        padding: 1.5rem 1rem;
        position: fixed;
        top: 0;
        bottom: 0;
        left: 0;
        z-index: 10;
    }

    .logo-container {
        display: flex;
        align-items: center;
        margin-bottom: 2rem;
    }

    .logo {
        width: 40px;
        height: 40px;
        margin-right: 0.75rem;
    }

    .app-name {
        font-size: 1.25rem;
        font-weight: 600;
        margin: 0;
    }

    .nav-links {
        display: flex;
        flex-direction: column;
        flex-grow: 1;
    }

    .nav-links a {
        display: flex;
        align-items: center;
        padding: 0.75rem 1rem;
        color: rgba(255, 255, 255, 0.7);
        text-decoration: none;
        border-radius: 8px;
        margin-bottom: 0.25rem;
        transition: all 0.2s;
    }

    .nav-links a svg {
        margin-right: 0.75rem;
    }

    .nav-links a:hover {
        background-color: rgba(255, 255, 255, 0.1);
        color: white;
    }

    .nav-links a.active {
        background-color: rgba(255, 255, 255, 0.2);
        color: white;
    }

    .user-section {
        margin-top: auto;
        border-top: 1px solid rgba(255, 255, 255, 0.1);
        padding-top: 1rem;
    }

    .user-info {
        display: flex;
        align-items: center;
        margin-bottom: 1rem;
        padding: 0.5rem 0;
    }

    .user-avatar {
        width: 36px;
        height: 36px;
        background-color: rgba(255, 255, 255, 0.2);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 0.75rem;
        font-weight: 600;
    }

    .user-details {
        display: flex;
        flex-direction: column;
    }

    .user-name {
        font-weight: 600;
        line-height: 1.2;
    }

    .user-role {
        font-size: 0.75rem;
        opacity: 0.7;
    }

    .logout-button {
        display: flex;
        align-items: center;
        background-color: rgba(255, 255, 255, 0.1);
        border: none;
        border-radius: 8px;
        padding: 0.75rem 1rem;
        width: 100%;
        color: white;
        cursor: pointer;
        transition: background-color 0.2s;
    }

    .logout-button svg {
        margin-right: 0.75rem;
    }

    .logout-button:hover {
        background-color: rgba(255, 255, 255, 0.2);
    }

    /* Main content area */
    .content {
        flex: 1;
        padding: 1.5rem;
        margin-left: 260px;
        width: calc(100% - 260px);
    }

    .top-bar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1.5rem;
    }

    .page-title {
        margin: 0;
        font-size: 1.5rem;
        font-weight: 700;
        color: #0f172a;
    }

    .top-actions {
        display: flex;
        gap: 0.5rem;
    }

    .action-button {
        width: 36px;
        height: 36px;
        border-radius: 8px;
        background-color: white;
        border: 1px solid #e2e8f0;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #64748b;
        cursor: pointer;
        transition: all 0.2s;
    }

    .action-button:hover {
        background-color: #f8fafc;
        color: #0f172a;
    }

    .page-content {
        display: grid;
        gap: 1.5rem;
    }

    /* Additional styles for users page */
    .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1.5rem;
    }

    .section-header h2 {
        color: #1e3a8a;
        margin: 0;
        font-size: 1.2rem;
    }

    .primary-button {
        display: flex;
        align-items: center;
        background-color: #1e3a8a;
        color: white;
        border: none;
        border-radius: 6px;
        padding: 0.6rem 1rem;
        font-size: 0.9rem;
        font-weight: 500;
        cursor: pointer;
        transition: background-color 0.2s;
    }

    .primary-button svg {
        margin-right: 0.5rem;
    }

    .primary-button:hover {
        background-color: #1e40af;
    }

    .secondary-button {
        background-color: #f3f4f6;
        color: #374151;
        border: 1px solid #d1d5db;
        border-radius: 6px;
        padding: 0.6rem 1rem;
        font-size: 0.9rem;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s;
    }

    .secondary-button:hover {
        background-color: #e5e7eb;
    }

    .users-container {
        background-color: white;
        border-radius: 10px;
        box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
        padding: 1.5rem;
    }

    .table-responsive {
        overflow-x: auto;
    }

    .users-table {
        width: 100%;
        border-collapse: collapse;
        font-size: 0.9rem;
    }

    .users-table th,
    .users-table td {
        padding: 0.8rem 1rem;
        text-align: left;
    }

    .users-table th {
        background-color: #f9fafb;
        font-weight: 600;
        color: #374151;
        border-bottom: 1px solid #e5e7eb;
    }

    .users-table tr {
        border-bottom: 1px solid #f3f4f6;
    }

    .users-table tr:last-child {
        border-bottom: none;
    }

    .users-table tr:hover {
        background-color: #f9fafb;
    }

    .actions {
        display: flex;
        gap: 0.5rem;
    }

    .action-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 32px;
        height: 32px;
        border-radius: 6px;
        border: none;
        cursor: pointer;
        transition: all 0.2s;
    }

    .action-btn.edit {
        background-color: #dbeafe;
        color: #1e40af;
    }

    .action-btn.edit:hover {
        background-color: #bfdbfe;
    }

    .action-btn.delete {
        background-color: #fee2e2;
        color: #991b1b;
    }

    .action-btn.delete:hover {
        background-color: #fecaca;
    }

    /* Loading, error and empty states */
    .loading-container,
    .error-container,
    .empty-container {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 3rem 1rem;
        text-align: center;
    }

    .loading-spinner {
        width: 40px;
        height: 40px;
        border: 3px solid #e5e7eb;
        border-top: 3px solid #1e3a8a;
        border-radius: 50%;
        animation: spin 1s linear infinite;
        margin-bottom: 1rem;
    }

    @keyframes spin {
        0% {
            transform: rotate(0deg);
        }
        100% {
            transform: rotate(360deg);
        }
    }

    /* Modal styles */
    .modal-overlay {
        position: fixed !important;
        top: 0 !important;
        left: 0 !important;
        right: 0 !important;
        bottom: 0 !important;
        background-color: rgba(0, 0, 0, 0.5) !important;
        display: flex !important;
        align-items: center !important;
        justify-content: center !important;
        z-index: 99999 !important;
        visibility: visible !important;
        opacity: 1 !important;
    }

    .modal {
        background-color: white !important;
        display: flex !important;
        flex-direction: column !important;
        border-radius: 10px !important;
        width: 900px !important;
        max-height: 95vh;
        overflow-y: auto !important;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1) !important;
        visibility: visible !important;
        opacity: 1 !important;
        z-index: 100000 !important;
        position: fixed !important;
        top: 50% !important;
        left: 50% !important;
        transform: translate(-50%, -50%) !important;
    }

    .modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 1.5rem 2rem;
        border-bottom: 1px solid #e5e7eb;
    }

    .close-button {
        background: none;
        border: none;
        font-size: 1.8rem;
        color: #6b7280;
        cursor: pointer;
    }

    .modal-body {
        padding: 2rem;
        font-size: 1rem;
    }

    .form-group {
        margin-bottom: 1.5rem;
    }

    .form-row {
        display: flex;
        gap: 1rem;
    }

    .form-row .form-group {
        flex: 1;
    }

    .form-group label {
        display: block;
        margin-bottom: 0.75rem;
        font-size: 1rem;
        font-weight: 500;
        color: #374151;
    }

    .form-group input,
    .form-group select {
        width: 100%;
        padding: 0.8rem;
        border: 1px solid #d1d5db;
        border-radius: 6px;
        font-size: 1rem;
    }

    .success-message {
        background-color: #88e18b; 
        color: white;
        padding: 10px;
        margin-bottom: 10px;
        border-radius: 5px;
        text-align: center;
    }

    .delete-message {
        background-color: #e0675e; 
        color: white;
        padding: 10px;
        margin-bottom: 10px;
        border-radius: 5px;
        text-align: center;
    }

    .update-message {
        background-color: #fff3cd;
        color: #856404; 
        padding: 10px;
        margin-bottom: 10px;
        border-radius: 5px;
        text-align: center;
    }

    .modal-actions {
        display: flex;
        justify-content: flex-end;
        gap: 1rem;
        margin-top: 1.5rem;
    }

    input[type="password"]::placeholder {
        color: #999;
        font-style: italic;
        font-size: 0.9em;
    }
    
</style>