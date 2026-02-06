import apiClient from './apiConfig';

const API_BASE_URL = 'http://localhost:8082';

/**
 * Import a PDF file with schedule data.
 * @param {File} file - The PDF file to import
 * @param {string} locationId - Optional location ID
 * @param {boolean} overwrite - Whether to overwrite existing shifts
 * @returns {Promise<Object>} Import result
 */
export const importPdf = async (file, locationId = null, overwrite = false) => {
  const formData = new FormData();
  formData.append('file', file);
  
  if (locationId) {
    formData.append('locationId', locationId);
  }
  formData.append('overwrite', overwrite.toString());

  const response = await fetch(`${API_BASE_URL}/api/schedules/import/pdf`, {
    method: 'POST',
    body: formData,
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`,
    },
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Error importing PDF');
  }

  return await response.json();
};

/**
 * Get template information for PDF import.
 * @returns {Promise<Object>} Template info
 */
export const getTemplateInfo = async () => {
  return apiClient.get('/api/schedules/import/template');
};

export default {
  importPdf,
  getTemplateInfo,
};
