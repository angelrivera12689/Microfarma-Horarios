import apiClient from './apiConfig.js';

class PositionService {
  async getAllPositions() {
    return await apiClient.get('/api/humanresources/positions');
  }

  async getPositionById(id) {
    return await apiClient.get(`/api/humanresources/positions/${id}`);
  }

  async createPosition(positionData) {
    return await apiClient.post('/api/humanresources/positions', positionData);
  }

  async updatePosition(id, positionData) {
    return await apiClient.put(`/api/humanresources/positions/${id}`, positionData);
  }

  async deletePosition(id) {
    return await apiClient.delete(`/api/humanresources/positions/${id}`);
  }
}

const positionService = new PositionService();
export default positionService;