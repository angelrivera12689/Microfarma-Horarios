import apiClient from './apiConfig.js';

class ContractTypeService {
  async getAllContractTypes() {
    return await apiClient.get('/api/humanresources/contracttypes');
  }

  async getContractTypeById(id) {
    return await apiClient.get(`/api/humanresources/contracttypes/${id}`);
  }

  async createContractType(contractTypeData) {
    return await apiClient.post('/api/humanresources/contracttypes', contractTypeData);
  }

  async updateContractType(id, contractTypeData) {
    return await apiClient.put(`/api/humanresources/contracttypes/${id}`, contractTypeData);
  }

  async deleteContractType(id) {
    return await apiClient.delete(`/api/humanresources/contracttypes/${id}`);
  }
}

const contractTypeService = new ContractTypeService();
export default contractTypeService;