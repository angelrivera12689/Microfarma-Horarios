import apiClient from './apiConfig.js';

class ReportService {
  // ==================== FILTERS ====================

  async getAvailableFilters() {
    return await apiClient.get('/api/schedules/reports/filters');
  }

  async getLocationFilters() {
    return await apiClient.get('/api/schedules/reports/filters/locations');
  }

  async getEmployeeFilters() {
    return await apiClient.get('/api/schedules/reports/filters/employees');
  }

  async getYearFilters() {
    return await apiClient.get('/api/schedules/reports/filters/years');
  }

  // ==================== REPORTS ====================

  async getMonthlyReport(month, year) {
    return await apiClient.get(`/api/schedules/reports/monthly?month=${month}&year=${year}`);
  }

  async getMonthlyReportByLocation(month, year, locationId) {
    return await apiClient.get(`/api/schedules/reports/monthly/by-location?month=${month}&year=${year}&locationId=${locationId}`);
  }

  async getMonthlyReportByEmployee(month, year, employeeId) {
    return await apiClient.get(`/api/schedules/reports/monthly/by-employee?month=${month}&year=${year}&employeeId=${employeeId}`);
  }

  async getGlobalReport(month, year) {
    return await apiClient.get(`/api/schedules/reports/global?month=${month}&year=${year}`);
  }

  async getLocationReport(locationId, month, year) {
    return await apiClient.get(`/api/schedules/reports/location/${locationId}?month=${month}&year=${year}`);
  }

  async getEmployeeIndividualReport(employeeId, month, year) {
    return await apiClient.get(`/api/schedules/reports/employee/${employeeId}?month=${month}&year=${year}`);
  }

  // ==================== EXPORTS ====================

  async exportCsv(month, year) {
    try {
      const blob = await apiClient.get(`/api/schedules/reports/monthly/csv?month=${month}&year=${year}`, {
        responseType: 'blob'
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `reporte_horas_${year}_${month}.csv`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error downloading CSV:', error);
      throw error;
    }
  }

  async exportPdf(month, year, options = {}) {
    try {
      const { reportType = 'general', locationId = null, employeeId = null } = options;
      
      let url = `/api/schedules/reports/monthly/pdf?month=${month}&year=${year}&reportType=${reportType}`;
      
      if (locationId) {
        url += `&locationId=${encodeURIComponent(locationId)}`;
      }
      if (employeeId) {
        url += `&employeeId=${encodeURIComponent(employeeId)}`;
      }

      const blob = await apiClient.get(url, {
        responseType: 'blob'
      });
      const urlObj = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = urlObj;
      
      // Generate filename based on report type
      let filename = 'reporte';
      if (reportType === 'location' && locationId) {
        filename = `reporte_sede_${locationId}`;
      } else if (reportType === 'employee' && employeeId) {
        filename = `reporte_empleado_${employeeId}`;
      } else {
        filename = 'reporte_general';
      }
      filename += `_${year}_${month}.pdf`;
      
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(urlObj);
    } catch (error) {
      console.error('Error downloading PDF:', error);
      throw error;
    }
  }

  async exportGeneralPdf(month, year) {
    try {
      const url = `/api/schedules/reports/monthly/pdf/general?month=${month}&year=${year}`;
      const blob = await apiClient.get(url, {
        responseType: 'blob'
      });
      const urlObj = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = urlObj;
      link.setAttribute('download', `reporte_general_${year}_${month}.pdf`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(urlObj);
    } catch (error) {
      console.error('Error downloading general PDF:', error);
      throw error;
    }
  }

  async exportLocationPdf(month, year, locationId) {
    try {
      const url = `/api/schedules/reports/monthly/pdf/location?month=${month}&year=${year}&locationId=${encodeURIComponent(locationId)}`;
      const blob = await apiClient.get(url, {
        responseType: 'blob'
      });
      const urlObj = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = urlObj;
      link.setAttribute('download', `reporte_sede_${year}_${month}.pdf`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(urlObj);
    } catch (error) {
      console.error('Error downloading location PDF:', error);
      throw error;
    }
  }

  async exportEmployeePdf(month, year, employeeId) {
    try {
      const url = `/api/schedules/reports/monthly/pdf/employee?month=${month}&year=${year}&employeeId=${encodeURIComponent(employeeId)}`;
      const blob = await apiClient.get(url, {
        responseType: 'blob'
      });
      const urlObj = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = urlObj;
      link.setAttribute('download', `reporte_empleado_${year}_${month}.pdf`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(urlObj);
    } catch (error) {
      console.error('Error downloading employee PDF:', error);
      throw error;
    }
  }
}

const reportService = new ReportService();
export default reportService;
