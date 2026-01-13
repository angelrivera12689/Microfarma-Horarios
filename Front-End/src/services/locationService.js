import apiClient from './apiConfig.js';

class LocationService {
  async getAllLocations() {
    return await apiClient.get('/api/organization/locations');
  }

  async getLocationById(id) {
    return await apiClient.get(`/api/organization/locations/${id}`);
  }

  async createLocation(locationData) {
    return await apiClient.post('/api/organization/locations', locationData);
  }

  async updateLocation(id, locationData) {
    return await apiClient.put(`/api/organization/locations/${id}`, locationData);
  }

  async deleteLocation(id) {
    return await apiClient.delete(`/api/organization/locations/${id}`);
  }
}

const locationService = new LocationService();
export default locationService;