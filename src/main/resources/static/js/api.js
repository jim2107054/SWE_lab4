/**
 * API Client for Student-Teacher Management System
 */

const API_BASE_URL = '';

// Token Management
const TokenManager = {
    getToken: () => localStorage.getItem('jwt_token'),
    setToken: (token) => localStorage.setItem('jwt_token', token),
    removeToken: () => localStorage.removeItem('jwt_token'),
    
    getUser: () => {
        const user = localStorage.getItem('user_data');
        return user ? JSON.parse(user) : null;
    },
    setUser: (user) => localStorage.setItem('user_data', JSON.stringify(user)),
    removeUser: () => localStorage.removeItem('user_data'),
    
    isAuthenticated: () => !!localStorage.getItem('jwt_token'),
    
    isTeacher: () => {
        const user = TokenManager.getUser();
        return user && user.role === 'ROLE_TEACHER';
    },
    
    clear: () => {
        localStorage.removeItem('jwt_token');
        localStorage.removeItem('user_data');
    }
};

// HTTP Client
const HttpClient = {
    async request(endpoint, options = {}) {
        const url = `${API_BASE_URL}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };
        
        // Add Authorization header if token exists
        const token = TokenManager.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        
        try {
            const response = await fetch(url, {
                ...options,
                headers
            });
            
            // Handle 401 Unauthorized - redirect to login
            if (response.status === 401) {
                TokenManager.clear();
                showLogin();
                throw new Error('Session expired. Please login again.');
            }
            
            // Handle 403 Forbidden
            if (response.status === 403) {
                throw new Error('Access denied. You do not have permission to perform this action.');
            }
            
            // Parse response
            const contentType = response.headers.get('content-type');
            let data = null;
            
            if (contentType && contentType.includes('application/json')) {
                data = await response.json();
            }
            
            if (!response.ok) {
                throw new Error(data?.message || data?.error || `HTTP error! Status: ${response.status}`);
            }
            
            return data;
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    },
    
    get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    },
    
    post(endpoint, data) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },
    
    put(endpoint, data) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },
    
    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }
};

// API Services
const AuthAPI = {
    async login(username, password) {
        const data = await HttpClient.post('/api/auth/login', { username, password });
        if (data.token) {
            TokenManager.setToken(data.token);
            TokenManager.setUser({
                username: data.username,
                email: data.email,
                name: data.name,
                role: data.role
            });
        }
        return data;
    },
    
    async register(userData) {
        const data = await HttpClient.post('/api/auth/register', userData);
        if (data.token) {
            TokenManager.setToken(data.token);
            TokenManager.setUser({
                username: data.username,
                email: data.email,
                name: data.name,
                role: data.role
            });
        }
        return data;
    },
    
    logout() {
        TokenManager.clear();
    }
};

const StudentsAPI = {
    getAll() {
        return HttpClient.get('/api/students');
    },
    
    getById(id) {
        return HttpClient.get(`/api/students/${id}`);
    },
    
    getByRoll(roll) {
        return HttpClient.get(`/api/students/roll/${roll}`);
    }
};

const TeachersAPI = {
    getAll() {
        return HttpClient.get('/api/teachers');
    },
    
    getById(id) {
        return HttpClient.get(`/api/teachers/${id}`);
    }
};

const CoursesAPI = {
    getAll() {
        return HttpClient.get('/api/courses');
    },
    
    getById(id) {
        return HttpClient.get(`/api/courses/${id}`);
    },
    
    create(courseData) {
        return HttpClient.post('/api/courses', courseData);
    },
    
    update(id, courseData) {
        return HttpClient.put(`/api/courses/${id}`, courseData);
    },
    
    delete(id) {
        return HttpClient.delete(`/api/courses/${id}`);
    }
};

const DepartmentsAPI = {
    getAll() {
        return HttpClient.get('/api/departments');
    },
    
    getById(id) {
        return HttpClient.get(`/api/departments/${id}`);
    },
    
    create(departmentData) {
        return HttpClient.post('/api/departments', departmentData);
    },
    
    update(id, departmentData) {
        return HttpClient.put(`/api/departments/${id}`, departmentData);
    },
    
    delete(id) {
        return HttpClient.delete(`/api/departments/${id}`);
    }
};

// Export for use in app.js
window.TokenManager = TokenManager;
window.AuthAPI = AuthAPI;
window.StudentsAPI = StudentsAPI;
window.TeachersAPI = TeachersAPI;
window.CoursesAPI = CoursesAPI;
window.DepartmentsAPI = DepartmentsAPI;
