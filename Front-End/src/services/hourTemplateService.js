import apiClient from './apiConfig.js';

// Default templates based on the image analysis
const DEFAULT_TEMPLATES = [
  {
    id: 1,
    name: 'Plantilla 1',
    ordinarias: 120.0,
    diurnas: 0,
    nocturnas: 47.7,
    extraFDom: 0,
    dominicales: 10.0
  },
  {
    id: 2,
    name: 'Plantilla 2',
    ordinarias: 186.7,
    diurnas: 20.0,
    nocturnas: 0,
    extraFDom: 0,
    dominicales: 9.0
  },
  {
    id: 3,
    name: 'Plantilla 3',
    ordinarias: 119.36,
    diurnas: 73.0,
    nocturnas: 0,
    extraFDom: 0,
    dominicales: 9.0
  },
  {
    id: 4,
    name: 'Plantilla 4',
    ordinarias: 134.36,
    diurnas: 0,
    nocturnas: 13.0,
    extraFDom: 0,
    dominicales: 26.0
  }
];

// Hour rates (fixed values)
const HOURLY_RATES = {
  ordinaria: 7.295,
  diurna: 9.948,
  nocturna: 13.928,
  extraFDom: 17.111,
  dominical: 21.090
};

class HourTemplateService {
  // Get all templates (using local storage for now, as backend may not exist)
  async getAllTemplates() {
    // Try to get from backend first
    try {
      const response = await apiClient.get('/api/schedules/hour-templates');
      if (response.data) {
        return response;
      }
    } catch (error) {
      console.log('Using local templates (backend not available):', error.message);
    }
    
    // Use local storage or default templates
    const stored = localStorage.getItem('hourTemplates');
    if (stored) {
      return { data: JSON.parse(stored) };
    }
    return { data: DEFAULT_TEMPLATES };
  }

  // Save templates to local storage
  async saveTemplates(templates) {
    try {
      // Try to save to backend
      await apiClient.post('/api/schedules/hour-templates/bulk', templates);
    } catch (error) {
      console.log('Saving to local storage (backend not available):', error.message);
    }
    
    // Save to local storage as backup
    localStorage.setItem('hourTemplates', JSON.stringify(templates));
    return { data: templates };
  }

  // Update a single template
  async updateTemplate(id, templateData) {
    const templates = await this.getAllTemplates();
    const updatedTemplates = templates.data.map(t => 
      t.id === id ? { ...t, ...templateData } : t
    );
    return this.saveTemplates(updatedTemplates);
  }

  // Get hourly rates
  getHourlyRates() {
    return HOURLY_RATES;
  }

  // Calculate totals for a template
  calculateTotals(template) {
    const totals = {
      totalHours: 0,
      totalValue: 0
    };
    
    totals.totalHours = 
      (parseFloat(template.ordinarias) || 0) +
      (parseFloat(template.diurnas) || 0) +
      (parseFloat(template.nocturnas) || 0) +
      (parseFloat(template.extraFDom) || 0) +
      (parseFloat(template.dominicales) || 0);
    
    totals.totalValue = 
      (parseFloat(template.ordinarias) || 0) * HOURLY_RATES.ordinaria +
      (parseFloat(template.diurnas) || 0) * HOURLY_RATES.diurna +
      (parseFloat(template.nocturnas) || 0) * HOURLY_RATES.nocturna +
      (parseFloat(template.extraFDom) || 0) * HOURLY_RATES.extraFDom +
      (parseFloat(template.dominicales) || 0) * HOURLY_RATES.dominical;
    
    return totals;
  }

  // Reset to default templates
  async resetToDefaults() {
    return this.saveTemplates(DEFAULT_TEMPLATES);
  }
}

const hourTemplateService = new HourTemplateService();
export default hourTemplateService;
