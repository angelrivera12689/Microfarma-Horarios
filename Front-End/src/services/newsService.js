import apiClient from './apiConfig.js';

class NewsService {
  async getMyNews() {
    const response = await apiClient.get('/api/news/news/me');
    return response.data;
  }
}

const newsService = new NewsService();
export default newsService;