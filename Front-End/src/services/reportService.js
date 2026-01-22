import apiClient from './apiConfig.js';

class ReportService {
  async getMonthlyReport(month, year) {
    return await apiClient.get(`/api/schedules/reports/monthly?month=${month}&year=${year}`);
  }

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
      console.error('Error exporting CSV:', error);
      alert('Error al descargar CSV: ' + (error.message || 'Error desconocido'));
    }
  }

  async exportPdf(month, year) {
    try {
      const blob = await apiClient.get(`/api/schedules/reports/monthly/pdf?month=${month}&year=${year}`, {
        responseType: 'blob'
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `reporte_horas_${year}_${month}.pdf`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error exporting PDF:', error);
      alert('Error al descargar PDF: ' + (error.message || 'Error desconocido'));
    }
  }
}

const reportService = new ReportService();
export default reportService;