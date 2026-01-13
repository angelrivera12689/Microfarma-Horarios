import apiClient from './apiConfig.js';

class ScheduleService {
  async getAllSchedules() {
    return await apiClient.get('/api/schedules/schedules');
  }

  async getScheduleById(id) {
    return await apiClient.get(`/api/schedules/schedules/${id}`);
  }

  async createSchedule(scheduleData) {
    return await apiClient.post('/api/schedules/schedules', scheduleData);
  }

  async updateSchedule(id, scheduleData) {
    return await apiClient.put(`/api/schedules/schedules/${id}`, scheduleData);
  }

  async deleteSchedule(id) {
    return await apiClient.delete(`/api/schedules/schedules/${id}`);
  }

  async getSchedulesByEmployee(employeeId) {
    return await apiClient.get(`/api/schedules/schedules/employee/${employeeId}`);
  }

  async getActiveSchedulesByEmployee(employeeId) {
    return await apiClient.get(`/api/schedules/schedules/employee/${employeeId}/active`);
  }
}

const scheduleService = new ScheduleService();
export default scheduleService;