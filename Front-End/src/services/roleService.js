import apiClient from './apiConfig.js';

class RoleService {
  async getAllRoles() {
    return await apiClient.get('/api/Role');
  }

  async getRoleById(id) {
    return await apiClient.get(`/api/Role/${id}`);
  }

  async createRole(roleData) {
    return await apiClient.post('/api/Role', roleData);
  }

  async updateRole(id, roleData) {
    return await apiClient.put(`/api/Role/${id}`, roleData);
  }

  async deleteRole(id) {
    return await apiClient.delete(`/api/Role/${id}`);
  }

  async getPermissionsForRole(roleId) {
    return await apiClient.get(`/api/Role/${roleId}/permissions`);
  }

  async assignPermissionToRole(roleId, permissionId) {
    return await apiClient.post(`/api/Role/${roleId}/permissions/${permissionId}`);
  }

  async removePermissionFromRole(roleId, permissionId) {
    return await apiClient.delete(`/api/Role/${roleId}/permissions/${permissionId}`);
  }
}

const roleService = new RoleService();
export default roleService;