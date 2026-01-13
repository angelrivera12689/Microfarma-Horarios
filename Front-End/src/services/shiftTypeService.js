import apiClient from './apiConfig.js';

class ShiftTypeService {
  async getAllShiftTypes() {
    return await apiClient.get('/api/schedules/shifttypes');
  }

  async getShiftTypeById(id) {
    return await apiClient.get(`/api/schedules/shifttypes/${id}`);
  }

  async createShiftType(shiftTypeData) {
    return await apiClient.post('/api/schedules/shifttypes', shiftTypeData);
  }

  async updateShiftType(id, shiftTypeData) {
    return await apiClient.put(`/api/schedules/shifttypes/${id}`, shiftTypeData);
  }

  async deleteShiftType(id) {
    return await apiClient.delete(`/api/schedules/shifttypes/${id}`);
  }
}

const shiftTypeService = new ShiftTypeService();
export default shiftTypeService;