import apiClient from './apiConfig.js';

class NewsService {
  // Obtener todas las noticias
  async getAllNews() {
    const response = await apiClient.get('/api/news/news');
    return response.data;
  }

  // Obtener mis noticias (del empleado logueado)
  async getMyNews() {
    const response = await apiClient.get('/api/news/news/me');
    return response.data;
  }

  // Obtener noticias por tipo
  async getNewsByType(newsTypeId) {
    const response = await apiClient.get(`/api/news/news/newstype/${newsTypeId}`);
    return response.data;
  }

  // Obtener noticias por empleado
  async getNewsByEmployee(employeeId) {
    const response = await apiClient.get(`/api/news/news/employee/${employeeId}`);
    return response.data;
  }

  // Obtener noticias por rango de fechas
  async getNewsByDateRange(startDate, endDate) {
    const response = await apiClient.get('/api/news/news/date-range', {
      params: { startDate, endDate }
    });
    return response.data;
  }

  // Obtener tipos de noticias
  async getNewsTypes() {
    const response = await apiClient.get('/api/news/newstypes');
    return response.data;
  }
}

const newsService = new NewsService();
export default newsService;
