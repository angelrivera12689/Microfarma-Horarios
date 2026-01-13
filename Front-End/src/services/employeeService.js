import apiClient from './apiConfig.js';

class EmployeeService {
  async getAllEmployees() {
    const response = await apiClient.get('/api/humanresources/employees');
    return response;
  }

  async getActiveEmployees() {
    const response = await apiClient.get('/api/humanresources/employees/state/true');
    return response;
  }

  async getMyProfile() {
    const response = await apiClient.get('/api/humanresources/employees/me');
    return response;
  }

  async updateMyProfile(employeeData) {
    const response = await apiClient.put('/api/humanresources/employees/me', employeeData);
    return response;
  }

  async createEmployee(employeeData) {
    const response = await apiClient.post('/api/humanresources/employees', employeeData);
    return response;
  }

  async updateEmployee(id, employeeData) {
    const response = await apiClient.put(`/api/humanresources/employees/${id}`, employeeData);
    return response;
  }

  async deleteEmployee(id) {
    const response = await apiClient.delete(`/api/humanresources/employees/${id}`);
    return response;
  }
}

const employeeService = new EmployeeService();
export default employeeService;