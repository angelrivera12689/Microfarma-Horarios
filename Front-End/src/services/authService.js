import apiClient from './apiConfig.js';

class AuthService {
  async login(credentials) {
    const response = await apiClient.post('/api/auth/login', credentials);
    if (response.data) {
      // Store token in localStorage
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('refreshToken', response.data.refreshToken);
      localStorage.setItem('user', JSON.stringify({
        id: response.data.id,
        email: response.data.email,
        role: response.data.role
      }));
    }
    return response;
  }

  async register(userData) {
    return await apiClient.post('/api/auth/register', userData);
  }

  async refreshToken() {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    const response = await apiClient.post('/api/auth/refresh', { refreshToken });

    if (response.token) {
      localStorage.setItem('token', response.token);
      localStorage.setItem('refreshToken', response.refreshToken);
    }

    return response;
  }

  async forgotPassword(email) {
    return await apiClient.post('/api/auth/forgot-password', { email });
  }

  async resetPassword(data) {
    return await apiClient.post('/api/auth/reset-password', data);
  }

  async verifyToken(token) {
    return await apiClient.post('/api/auth/verify-token', { token });
  }

  async logout() {
    try {
      await apiClient.post('/api/auth/logout');
    } catch {
      // Silently handle logout error
    } finally {
      // Clear local storage regardless of API call success
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
    }
  }

  isAuthenticated() {
    const token = localStorage.getItem('token');
    return !!token;
  }

  getCurrentUser() {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }

  getToken() {
    return localStorage.getItem('token');
  }
}

const authService = new AuthService();
export default authService;