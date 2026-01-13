import apiClient from './apiConfig.js';

class UserService {
  async getAllUsers() {
    return await apiClient.get('/api/User');
  }

  async getUserById(id) {
    return await apiClient.get(`/api/User/${id}`);
  }

  async createUser(userData) {
    return await apiClient.post('/api/User', userData);
  }

  async updateUser(id, userData) {
    return await apiClient.put(`/api/User/${id}`, userData);
  }

  async deleteUser(id) {
    return await apiClient.delete(`/api/User/${id}`);
  }

  async registerUser(userData) {
    return await apiClient.post('/api/User/register', userData);
  }
}

const userService = new UserService();
export default userService;