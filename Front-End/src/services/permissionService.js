import apiClient from './apiConfig.js';

class PermissionService {
  async getAllPermissions() {
    return await apiClient.get('/api/Permission');
  }

  async getPermissionById(id) {
    return await apiClient.get(`/api/Permission/${id}`);
  }

  async createPermission(permissionData) {
    return await apiClient.post('/api/Permission', permissionData);
  }

  async updatePermission(id, permissionData) {
    return await apiClient.put(`/api/Permission/${id}`, permissionData);
  }

  async deletePermission(id) {
    return await apiClient.delete(`/api/Permission/${id}`);
  }
}

const permissionService = new PermissionService();
export default permissionService;