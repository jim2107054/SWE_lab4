/**
 * Main Application Logic for Student-Teacher Management System
 */

// DOM Elements
let courseModal, departmentModal, deleteModal, toast;

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    // Initialize Bootstrap modals
    courseModal = new bootstrap.Modal(document.getElementById('courseModal'));
    departmentModal = new bootstrap.Modal(document.getElementById('departmentModal'));
    deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));
    toast = new bootstrap.Toast(document.getElementById('toast'));
    
    // Check authentication status
    if (TokenManager.isAuthenticated()) {
        showDashboard();
    } else {
        showLogin();
    }
});

// ==================== Navigation Functions ====================

function showLogin() {
    document.getElementById('loginSection').classList.remove('d-none');
    document.getElementById('registerSection').classList.add('d-none');
    document.getElementById('dashboardSection').classList.add('d-none');
    document.getElementById('authNav').classList.remove('d-none');
    document.getElementById('userNav').classList.add('d-none');
    
    // Clear form
    document.getElementById('loginForm').reset();
    document.getElementById('loginError').classList.add('d-none');
}

function showRegister() {
    document.getElementById('loginSection').classList.add('d-none');
    document.getElementById('registerSection').classList.remove('d-none');
    document.getElementById('dashboardSection').classList.add('d-none');
    document.getElementById('authNav').classList.remove('d-none');
    document.getElementById('userNav').classList.add('d-none');
    
    // Clear form
    document.getElementById('registerForm').reset();
    document.getElementById('registerError').classList.add('d-none');
    document.getElementById('registerSuccess').classList.add('d-none');
    toggleRoleFields();
}

function showDashboard() {
    document.getElementById('loginSection').classList.add('d-none');
    document.getElementById('registerSection').classList.add('d-none');
    document.getElementById('dashboardSection').classList.remove('d-none');
    document.getElementById('dashboardSection').classList.add('fade-in');
    document.getElementById('authNav').classList.add('d-none');
    document.getElementById('userNav').classList.remove('d-none');
    
    // Update welcome message
    const user = TokenManager.getUser();
    if (user) {
        const roleText = user.role === 'ROLE_TEACHER' ? '(Teacher)' : '(Student)';
        document.getElementById('welcomeUser').innerHTML = 
            `<i class="bi bi-person-circle me-1"></i>${user.name} <span class="badge ${user.role === 'ROLE_TEACHER' ? 'bg-success' : 'bg-info'}">${roleText}</span>`;
    }
    
    // Show/hide teacher-only elements
    updateTeacherElements();
    
    // Load data
    loadAllData();
}

function updateTeacherElements() {
    const teacherElements = document.querySelectorAll('.teacher-only');
    const isTeacher = TokenManager.isTeacher();
    
    teacherElements.forEach(el => {
        if (isTeacher) {
            el.classList.remove('d-none');
        } else {
            el.classList.add('d-none');
        }
    });
}

function showTab(tabName) {
    // Update nav links
    document.querySelectorAll('#mainTabs .nav-link').forEach(link => {
        link.classList.remove('active');
    });
    event.target.classList.add('active');
    
    // Update tab content
    const tabs = ['students', 'teachers', 'courses', 'departments'];
    tabs.forEach(tab => {
        const tabElement = document.getElementById(`${tab}Tab`);
        if (tab === tabName) {
            tabElement.classList.remove('d-none');
            tabElement.classList.add('fade-in');
        } else {
            tabElement.classList.add('d-none');
        }
    });
}

// ==================== Authentication Functions ====================

async function handleLogin(event) {
    event.preventDefault();
    
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;
    const errorDiv = document.getElementById('loginError');
    
    showLoading(true);
    
    try {
        await AuthAPI.login(username, password);
        showToast('Success', 'Login successful!', 'success');
        showDashboard();
    } catch (error) {
        errorDiv.textContent = error.message || 'Login failed. Please check your credentials.';
        errorDiv.classList.remove('d-none');
    } finally {
        showLoading(false);
    }
}

async function handleRegister(event) {
    event.preventDefault();
    
    const role = document.getElementById('regRole').value;
    const errorDiv = document.getElementById('registerError');
    const successDiv = document.getElementById('registerSuccess');
    
    const userData = {
        username: document.getElementById('regUsername').value,
        password: document.getElementById('regPassword').value,
        email: document.getElementById('regEmail').value,
        name: document.getElementById('regName').value,
        role: role
    };
    
    // Add role-specific fields
    if (role === 'ROLE_STUDENT') {
        userData.roll = document.getElementById('regRoll').value;
        userData.program = document.getElementById('regProgram').value;
        userData.semester = parseInt(document.getElementById('regSemester').value) || 1;
    } else {
        userData.specialization = document.getElementById('regSpecialization').value;
    }
    
    showLoading(true);
    errorDiv.classList.add('d-none');
    successDiv.classList.add('d-none');
    
    try {
        await AuthAPI.register(userData);
        showToast('Success', 'Registration successful!', 'success');
        showDashboard();
    } catch (error) {
        errorDiv.textContent = error.message || 'Registration failed. Please try again.';
        errorDiv.classList.remove('d-none');
    } finally {
        showLoading(false);
    }
}

function logout() {
    AuthAPI.logout();
    showToast('Info', 'You have been logged out.', 'info');
    showLogin();
}

function toggleRoleFields() {
    const role = document.getElementById('regRole').value;
    const studentFields = document.getElementById('studentFields');
    const teacherFields = document.getElementById('teacherFields');
    
    if (role === 'ROLE_STUDENT') {
        studentFields.classList.remove('d-none');
        teacherFields.classList.add('d-none');
    } else {
        studentFields.classList.add('d-none');
        teacherFields.classList.remove('d-none');
    }
}

// ==================== Data Loading Functions ====================

async function loadAllData() {
    showLoading(true);
    
    try {
        await Promise.all([
            loadStudents(),
            loadTeachers(),
            loadCourses(),
            loadDepartments()
        ]);
    } catch (error) {
        console.error('Error loading data:', error);
        showToast('Error', 'Failed to load some data.', 'error');
    } finally {
        showLoading(false);
    }
}

async function loadStudents() {
    try {
        const students = await StudentsAPI.getAll();
        renderStudents(students);
        document.getElementById('studentCount').textContent = students.length;
    } catch (error) {
        console.error('Error loading students:', error);
        document.getElementById('studentsTableBody').innerHTML = 
            '<tr><td colspan="6" class="text-center text-muted">Failed to load students</td></tr>';
    }
}

async function loadTeachers() {
    try {
        const teachers = await TeachersAPI.getAll();
        renderTeachers(teachers);
        document.getElementById('teacherCount').textContent = teachers.length;
    } catch (error) {
        console.error('Error loading teachers:', error);
        document.getElementById('teachersTableBody').innerHTML = 
            '<tr><td colspan="5" class="text-center text-muted">Failed to load teachers</td></tr>';
    }
}

async function loadCourses() {
    try {
        const courses = await CoursesAPI.getAll();
        renderCourses(courses);
        document.getElementById('courseCount').textContent = courses.length;
    } catch (error) {
        console.error('Error loading courses:', error);
        document.getElementById('coursesTableBody').innerHTML = 
            '<tr><td colspan="7" class="text-center text-muted">Failed to load courses</td></tr>';
    }
}

async function loadDepartments() {
    try {
        const departments = await DepartmentsAPI.getAll();
        renderDepartments(departments);
        document.getElementById('departmentCount').textContent = departments.length;
        populateDepartmentSelect(departments);
    } catch (error) {
        console.error('Error loading departments:', error);
        document.getElementById('departmentsTableBody').innerHTML = 
            '<tr><td colspan="5" class="text-center text-muted">Failed to load departments</td></tr>';
    }
}

// ==================== Render Functions ====================

function renderStudents(students) {
    const tbody = document.getElementById('studentsTableBody');
    
    if (!students || students.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center">
                    <div class="empty-state">
                        <i class="bi bi-people"></i>
                        <p>No students found</p>
                    </div>
                </td>
            </tr>`;
        return;
    }
    
    tbody.innerHTML = students.map(student => `
        <tr>
            <td>${student.id}</td>
            <td><strong>${student.name || '-'}</strong></td>
            <td><span class="badge bg-primary">${student.roll || '-'}</span></td>
            <td>${student.email || '-'}</td>
            <td>${student.program || '-'}</td>
            <td><span class="badge bg-info">${student.semester || '-'}</span></td>
        </tr>
    `).join('');
}

function renderTeachers(teachers) {
    const tbody = document.getElementById('teachersTableBody');
    
    if (!teachers || teachers.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center">
                    <div class="empty-state">
                        <i class="bi bi-person-workspace"></i>
                        <p>No teachers found</p>
                    </div>
                </td>
            </tr>`;
        return;
    }
    
    tbody.innerHTML = teachers.map(teacher => `
        <tr>
            <td>${teacher.id}</td>
            <td><strong>${teacher.name || '-'}</strong></td>
            <td>${teacher.email || '-'}</td>
            <td>${teacher.specialization || '-'}</td>
            <td>${teacher.departmentName || '-'}</td>
        </tr>
    `).join('');
}

function renderCourses(courses) {
    const tbody = document.getElementById('coursesTableBody');
    const isTeacher = TokenManager.isTeacher();
    
    if (!courses || courses.length === 0) {
        const colspan = isTeacher ? 7 : 6;
        tbody.innerHTML = `
            <tr>
                <td colspan="${colspan}" class="text-center">
                    <div class="empty-state">
                        <i class="bi bi-book"></i>
                        <p>No courses found</p>
                    </div>
                </td>
            </tr>`;
        return;
    }
    
    tbody.innerHTML = courses.map(course => `
        <tr>
            <td>${course.id}</td>
            <td><span class="badge bg-secondary">${course.courseCode || '-'}</span></td>
            <td><strong>${course.title || '-'}</strong></td>
            <td><span class="badge bg-warning text-dark">${course.credits || '-'}</span></td>
            <td>${course.departmentName || '-'}</td>
            <td>${course.teacherName || '-'}</td>
            ${isTeacher ? `
            <td>
                <button class="btn btn-sm btn-outline-primary btn-action" onclick="editCourse(${course.id})">
                    <i class="bi bi-pencil"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger btn-action" onclick="confirmDelete('course', ${course.id})">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
            ` : ''}
        </tr>
    `).join('');
}

function renderDepartments(departments) {
    const tbody = document.getElementById('departmentsTableBody');
    const isTeacher = TokenManager.isTeacher();
    
    if (!departments || departments.length === 0) {
        const colspan = isTeacher ? 5 : 4;
        tbody.innerHTML = `
            <tr>
                <td colspan="${colspan}" class="text-center">
                    <div class="empty-state">
                        <i class="bi bi-building"></i>
                        <p>No departments found</p>
                    </div>
                </td>
            </tr>`;
        return;
    }
    
    tbody.innerHTML = departments.map(dept => `
        <tr>
            <td>${dept.id}</td>
            <td><span class="badge bg-info">${dept.code || '-'}</span></td>
            <td><strong>${dept.name || '-'}</strong></td>
            <td>${dept.description || '-'}</td>
            ${isTeacher ? `
            <td>
                <button class="btn btn-sm btn-outline-primary btn-action" onclick="editDepartment(${dept.id})">
                    <i class="bi bi-pencil"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger btn-action" onclick="confirmDelete('department', ${dept.id})">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
            ` : ''}
        </tr>
    `).join('');
}

function populateDepartmentSelect(departments) {
    const select = document.getElementById('courseDepartment');
    select.innerHTML = '<option value="">Select Department</option>' + 
        departments.map(dept => `<option value="${dept.id}">${dept.name}</option>`).join('');
}

// ==================== Course CRUD Functions ====================

function showCourseModal(courseId = null) {
    document.getElementById('courseForm').reset();
    document.getElementById('courseId').value = '';
    document.getElementById('courseError').classList.add('d-none');
    document.getElementById('courseModalTitle').textContent = courseId ? 'Edit Course' : 'Add Course';
    courseModal.show();
}

async function editCourse(id) {
    showLoading(true);
    try {
        const course = await CoursesAPI.getById(id);
        document.getElementById('courseId').value = course.id;
        document.getElementById('courseCode').value = course.courseCode || '';
        document.getElementById('courseName').value = course.title || '';
        document.getElementById('courseDescription').value = course.description || '';
        document.getElementById('courseCredits').value = course.credits || '';
        document.getElementById('courseDepartment').value = course.departmentId || '';
        document.getElementById('courseModalTitle').textContent = 'Edit Course';
        courseModal.show();
    } catch (error) {
        showToast('Error', error.message, 'error');
    } finally {
        showLoading(false);
    }
}

async function saveCourse() {
    const errorDiv = document.getElementById('courseError');
    errorDiv.classList.add('d-none');
    
    const courseData = {
        courseCode: document.getElementById('courseCode').value,
        title: document.getElementById('courseName').value,
        description: document.getElementById('courseDescription').value,
        credits: parseInt(document.getElementById('courseCredits').value),
        departmentId: parseInt(document.getElementById('courseDepartment').value) || null
    };
    
    const courseId = document.getElementById('courseId').value;
    
    showLoading(true);
    try {
        if (courseId) {
            await CoursesAPI.update(courseId, courseData);
            showToast('Success', 'Course updated successfully!', 'success');
        } else {
            await CoursesAPI.create(courseData);
            showToast('Success', 'Course created successfully!', 'success');
        }
        courseModal.hide();
        await loadCourses();
    } catch (error) {
        errorDiv.textContent = error.message;
        errorDiv.classList.remove('d-none');
    } finally {
        showLoading(false);
    }
}

// ==================== Department CRUD Functions ====================

function showDepartmentModal(departmentId = null) {
    document.getElementById('departmentForm').reset();
    document.getElementById('departmentId').value = '';
    document.getElementById('departmentError').classList.add('d-none');
    document.getElementById('departmentModalTitle').textContent = departmentId ? 'Edit Department' : 'Add Department';
    departmentModal.show();
}

async function editDepartment(id) {
    showLoading(true);
    try {
        const dept = await DepartmentsAPI.getById(id);
        document.getElementById('departmentId').value = dept.id;
        document.getElementById('departmentCode').value = dept.code || '';
        document.getElementById('departmentName').value = dept.name || '';
        document.getElementById('departmentDescription').value = dept.description || '';
        document.getElementById('departmentModalTitle').textContent = 'Edit Department';
        departmentModal.show();
    } catch (error) {
        showToast('Error', error.message, 'error');
    } finally {
        showLoading(false);
    }
}

async function saveDepartment() {
    const errorDiv = document.getElementById('departmentError');
    errorDiv.classList.add('d-none');
    
    const departmentData = {
        code: document.getElementById('departmentCode').value,
        name: document.getElementById('departmentName').value,
        description: document.getElementById('departmentDescription').value
    };
    
    const departmentId = document.getElementById('departmentId').value;
    
    showLoading(true);
    try {
        if (departmentId) {
            await DepartmentsAPI.update(departmentId, departmentData);
            showToast('Success', 'Department updated successfully!', 'success');
        } else {
            await DepartmentsAPI.create(departmentData);
            showToast('Success', 'Department created successfully!', 'success');
        }
        departmentModal.hide();
        await loadDepartments();
    } catch (error) {
        errorDiv.textContent = error.message;
        errorDiv.classList.remove('d-none');
    } finally {
        showLoading(false);
    }
}

// ==================== Delete Functions ====================

let deleteType = '';
let deleteId = null;

function confirmDelete(type, id) {
    deleteType = type;
    deleteId = id;
    deleteModal.show();
}

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('confirmDeleteBtn').addEventListener('click', async () => {
        showLoading(true);
        try {
            if (deleteType === 'course') {
                await CoursesAPI.delete(deleteId);
                showToast('Success', 'Course deleted successfully!', 'success');
                await loadCourses();
            } else if (deleteType === 'department') {
                await DepartmentsAPI.delete(deleteId);
                showToast('Success', 'Department deleted successfully!', 'success');
                await loadDepartments();
            }
            deleteModal.hide();
        } catch (error) {
            showToast('Error', error.message, 'error');
        } finally {
            showLoading(false);
        }
    });
});

// ==================== Utility Functions ====================

function showLoading(show) {
    const spinner = document.getElementById('loadingSpinner');
    if (show) {
        spinner.classList.remove('d-none');
    } else {
        spinner.classList.add('d-none');
    }
}

function showToast(title, message, type = 'info') {
    const toastElement = document.getElementById('toast');
    const toastTitle = document.getElementById('toastTitle');
    const toastBody = document.getElementById('toastBody');
    const toastIcon = document.getElementById('toastIcon');
    
    toastTitle.textContent = title;
    toastBody.textContent = message;
    
    // Update icon and color based on type
    toastElement.className = 'toast';
    switch (type) {
        case 'success':
            toastIcon.className = 'bi bi-check-circle-fill me-2 text-success';
            break;
        case 'error':
            toastIcon.className = 'bi bi-exclamation-circle-fill me-2 text-danger';
            break;
        case 'warning':
            toastIcon.className = 'bi bi-exclamation-triangle-fill me-2 text-warning';
            break;
        default:
            toastIcon.className = 'bi bi-info-circle-fill me-2 text-info';
    }
    
    toast.show();
}
