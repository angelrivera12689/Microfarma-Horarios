import apiClient from './apiConfig.js';

class ShiftChangeRequestService {
  async createRequest(requestData) {
    return await apiClient.post('/api/schedules/shift-change-requests', requestData);
  }

  async getMyRequests() {
    return await apiClient.get('/api/schedules/shift-change-requests/me');
  }

  async getPendingRequests() {
    return await apiClient.get('/api/schedules/shift-change-requests/pending');
  }

  async decideRequest(requestId, decisionData) {
    return await apiClient.put(`/api/schedules/shift-change-requests/${requestId}/decide`, decisionData);
  }
}

const shiftChangeRequestService = new ShiftChangeRequestService();
export default shiftChangeRequestService;