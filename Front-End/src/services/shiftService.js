import apiClient from './apiConfig.js';

class ShiftService {
  async getAllShifts() {
    const response = await apiClient.get('/api/schedules/shifts');
    return response;
  }

  async getMyShifts() {
    const response = await apiClient.get('/api/schedules/shifts/me');
    return response;
  }

  async createShift(shiftData) {
    const response = await apiClient.post('/api/schedules/shifts', shiftData);
    return response;
  }

  async updateShift(id, shiftData) {
    const response = await apiClient.put(`/api/schedules/shifts/${id}`, shiftData);
    return response;
  }

  async deleteShift(id) {
    try {
      console.log(`Attempting to delete shift with id: ${id}`);
      const response = await apiClient.delete(`/api/schedules/shifts/${id}`);
      console.log('Delete response raw:', JSON.stringify(response));
      console.log('Delete response status field:', response.status, typeof response.status);
      
      // Backend uses 'status' field, not 'success'
      if (response && response.status === true) {
        console.log('Delete successful');
        return true;
      } else {
        console.error('Error deleting shift:', response?.message || 'Unknown error');
        console.error('Full response:', response);
        throw new Error(response?.message || 'Error deleting shift');
      }
    } catch (error) {
      console.error('Error deleting shift:', error);
      throw error;
    }
  }

  async downloadCalendarPdf(year, month, locationId) {
    let endpoint = `/api/schedules/shifts/pdf/${year}/${month}`;
    if (locationId) {
      endpoint += `?locationId=${locationId}`;
    }
    
    const blob = await apiClient.get(endpoint, {
      responseType: 'blob'
    });

    if (blob) {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `calendario_turnos_${year}_${String(month).padStart(2, '0')}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    }
  }

  async downloadMyShiftsPdf() {
    const blob = await apiClient.get('/api/schedules/shifts/me/pdf', {
      responseType: 'blob'
    });

    if (blob) {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'mis_turnos.pdf');
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    }
  }

  async createBulkShifts(shifts) {
    const response = await apiClient.post('/api/schedules/shifts/bulk', shifts);
    return response;
  }
}

const shiftService = new ShiftService();
export default shiftService;