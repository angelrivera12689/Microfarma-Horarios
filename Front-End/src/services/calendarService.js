import apiClient from './apiConfig.js';

class CalendarService {
  async getAllCalendars() {
    return await apiClient.get('/api/schedules/calendars');
  }

  async getCalendarById(id) {
    return await apiClient.get(`/api/schedules/calendars/${id}`);
  }

  async createCalendar(calendarData) {
    return await apiClient.post('/api/schedules/calendars', calendarData);
  }

  async updateCalendar(id, calendarData) {
    return await apiClient.put(`/api/schedules/calendars/${id}`, calendarData);
  }

  async deleteCalendar(id) {
    return await apiClient.delete(`/api/schedules/calendars/${id}`);
  }
}

const calendarService = new CalendarService();
export default calendarService;