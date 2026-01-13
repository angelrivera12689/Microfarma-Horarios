import apiClient from './apiConfig.js';

class CompanyService {
  async getAllCompanies() {
    return await apiClient.get('/api/organization/companies');
  }

  async getCompanyById(id) {
    return await apiClient.get(`/api/organization/companies/${id}`);
  }

  async getCompanyByName(name) {
    return await apiClient.get(`/api/organization/companies/name/${name}`);
  }

  async createCompany(companyData) {
    return await apiClient.post('/api/organization/companies', companyData);
  }

  async updateCompany(id, companyData) {
    return await apiClient.put(`/api/organization/companies/${id}`, companyData);
  }

  async deleteCompany(id) {
    return await apiClient.delete(`/api/organization/companies/${id}`);
  }
}

const companyService = new CompanyService();
export default companyService;