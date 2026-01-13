import userService from './userService.js';
import employeeService from './employeeService.js';
import shiftService from './shiftService.js';
import calendarService from './calendarService.js';

class DashboardService {
  async getDashboardData() {
    try {
      // Fetch all data in parallel
      const [usersResponse, employeesResponse, todaysShiftsResponse, calendarsResponse] = await Promise.allSettled([
        userService.getAllUsers(),
        employeeService.getAllEmployees(),
        this.getTodaysShifts(),
        calendarService.getAllCalendars()
      ]);

      // Extract data or provide defaults
      const activeUsers = usersResponse.status === 'fulfilled' && usersResponse.value.data ? usersResponse.value.data.length : 0;
      const employees = employeesResponse.status === 'fulfilled' && employeesResponse.value.data ? employeesResponse.value.data.length : 0;
      const todaysShifts = todaysShiftsResponse.status === 'fulfilled' && todaysShiftsResponse.value.data ? todaysShiftsResponse.value.data.length : 0;
      const calendars = calendarsResponse.status === 'fulfilled' && calendarsResponse.value.data ? calendarsResponse.value.data : [];

      // For notifications, since there's no backend endpoint, we'll use a hardcoded value for now
      const notifications = 0;

      // Process weekly schedule from calendars
      const weeklySchedule = this.processWeeklySchedule(calendars);

      return {
        activeUsers,
        employees,
        todaysShifts,
        notifications,
        weeklySchedule
      };
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
      // Return default values in case of error
      return {
        activeUsers: 0,
        employees: 0,
        todaysShifts: 0,
        notifications: 0,
        weeklySchedule: this.getDefaultWeeklySchedule()
      };
    }
  }

  async getTodaysShifts() {
    const today = new Date().toISOString().split('T')[0];
    return await shiftService.getShiftsByDateRange(today, today);
  }

  processWeeklySchedule() {
    // For now, return a default schedule since the calendar structure might be complex
    // This can be enhanced once we understand the calendar data structure
    return [
      { day: 'Lun', hours: '8:00 - 17:00', status: 'active' },
      { day: 'Mar', hours: '8:00 - 17:00', status: 'active' },
      { day: 'Mié', hours: '8:00 - 17:00', status: 'active' },
      { day: 'Jue', hours: '8:00 - 17:00', status: 'active' },
      { day: 'Vie', hours: '8:00 - 17:00', status: 'active' },
      { day: 'Sáb', hours: '9:00 - 14:00', status: 'partial' },
      { day: 'Dom', hours: 'Cerrado', status: 'closed' }
    ];
  }

  getDefaultWeeklySchedule() {
    return [
      { day: 'Lun', hours: '8:00 - 17:00', status: 'active' },
      { day: 'Mar', hours: '8:00 - 17:00', status: 'active' },
      { day: 'Mié', hours: '8:00 - 17:00', status: 'active' },
      { day: 'Jue', hours: '8:00 - 17:00', status: 'active' },
      { day: 'Vie', hours: '8:00 - 17:00', status: 'active' },
      { day: 'Sáb', hours: '9:00 - 14:00', status: 'partial' },
      { day: 'Dom', hours: 'Cerrado', status: 'closed' }
    ];
  }
}

const dashboardService = new DashboardService();
export default dashboardService;