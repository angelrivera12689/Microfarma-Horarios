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
    const response = await apiClient.delete(`/api/schedules/shifts/${id}`);
    return response;
  }

  async downloadCalendarPdf(year, month, locationId) {
    const blob = await apiClient.get(`/api/schedules/shifts/pdf/${year}/${month}/${locationId}`, {
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
}

const shiftService = new ShiftService();
export default shiftService;