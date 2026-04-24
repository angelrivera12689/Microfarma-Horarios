// Base API configuration using fetch
const API_BASE_URL = 'http://localhost:8082'; // Adjust this to your backend URL

class ApiClient {
  constructor(baseURL) {
    this.baseURL = baseURL;
  }

  async request(endpoint, options = {}) {
    const url = `${this.baseURL}${endpoint}`;
    console.log(`API Request: ${options.method || 'GET'} ${url}`);
    const config = {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    };

    // Add authorization header if token exists
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log('Token added to request');
    } else {
      console.log('No token found in localStorage');
    }

    try {
      const response = await fetch(url, config);
      console.log(`API Response status: ${response.status} ${response.statusText}`);

      // Handle blob responses for file downloads
      if (options.responseType === 'blob') {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.blob();
      }

      // Check if response is JSON
      const contentType = response.headers.get('content-type');
      console.log(`Content-Type: ${contentType}`);
      let data;

      if (contentType && contentType.includes('application/json')) {
        try {
          data = await response.json();
          console.log('Parsed JSON data:', JSON.stringify(data));
        } catch (e) {
          console.error('Error parsing JSON:', e);
          data = { status: false, message: 'Invalid JSON response', data: null };
        }
      } else {
        // Handle non-JSON responses
        console.log('Non-JSON response, status:', response.ok);
        if (response.ok) {
          const text = await response.text();
          console.log('Response text:', text);
          data = { status: true, data: text };
        } else {
          data = {
            status: false,
            message: 'Error del servidor',
            data: null
          };
        }
      }

      if (!response.ok) {
        console.error('API Error (non-2xx):', data);
        // Return a consistent error response
        return {
          status: false,
          message: data.message || `HTTP ${response.status}: ${response.statusText}`,
          data: null
        };
      }

      console.log('API Response data:', data);
      return data;
    } catch (error) {
      console.error('API Request failed:', error);
      throw error;
    }
  }

  get(endpoint, options = {}) {
    return this.request(endpoint, { ...options, method: 'GET' });
  }

  post(endpoint, data, options = {}) {
    return this.request(endpoint, {
      ...options,
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  put(endpoint, data, options = {}) {
    return this.request(endpoint, {
      ...options,
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }

  delete(endpoint, options = {}) {
    return this.request(endpoint, { ...options, method: 'DELETE' });
  }

  patch(endpoint, data, options = {}) {
    return this.request(endpoint, {
      ...options,
      method: 'PATCH',
      body: JSON.stringify(data),
    });
  }
}

const apiClient = new ApiClient(API_BASE_URL);

export default apiClient;