// Default hourly rates in COP
const DEFAULT_RATES = {
  // Base hourly rate
  baseRate: 5000,
  
  // Hour type multipliers
  regularHours: 1.0,      // 100% of base rate
  diurnaExtraHours: 1.35, // 135% of base rate (35% extra)
  nocturnaExtraHours: 1.50, // 150% of base rate (50% extra)
  dominicalHours: 1.75,    // 175% of base rate (75% extra)
  festivoHours: 1.75,      // 175% of base rate (75% extra)
  
  // Additional rates
  nocturnaHours: 1.35,    // 135% for regular night hours
  
  // Rate names for display
  rateNames: {
    baseRate: 'Tarifa Base',
    regularHours: 'Horas Regulares',
    diurnaExtraHours: 'Horas Extra Diurnas',
    nocturnaExtraHours: 'Horas Extra Nocturnas',
    dominicalHours: 'Horas Dominicales',
    festivoHours: 'Horas Festivas',
    nocturnaHours: 'Horas Nocturnas'
  },
  
  // Rate descriptions
  rateDescriptions: {
    baseRate: 'Tarifa base por hora (100%)',
    regularHours: 'Multiplicador para horas regulares',
    diurnaExtraHours: 'Recargo para horas extras diurnas (35%)',
    nocturnaExtraHours: 'Recargo para horas extras nocturnas (50%)',
    dominicalHours: 'Recargo para horas dominicales (75%)',
    festivoHours: 'Recargo para horas festivas (75%)',
    nocturnaHours: 'Recargo para horas nocturnas regulares (35%)'
  }
};

const STORAGE_KEY = 'microfarma_hourly_rates';

/**
 * Get all configured rates
 * @returns {Object} Rate configuration object
 */
export const getRates = () => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
      return { ...DEFAULT_RATES, ...JSON.parse(stored) };
    }
  } catch (error) {
    console.error('Error loading rates from storage:', error);
  }
  return { ...DEFAULT_RATES };
};

/**
 * Save rate configuration
 * @param {Object} rates - Rate configuration to save
 */
export const saveRates = (rates) => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(rates));
    return true;
  } catch (error) {
    console.error('Error saving rates to storage:', error);
    return false;
  }
};

/**
 * Reset rates to default values
 * @returns {Object} Default rates
 */
export const resetRates = () => {
  try {
    localStorage.removeItem(STORAGE_KEY);
    return { ...DEFAULT_RATES };
  } catch (error) {
    console.error('Error resetting rates:', error);
    return { ...DEFAULT_RATES };
  }
};

/**
 * Calculate the value for a specific hour type
 * @param {string} hourType - Type of hour (e.g., 'regularHours', 'diurnaExtraHours')
 * @param {number} hours - Number of hours
 * @returns {number} Calculated value
 */
export const calculateHourValue = (hourType, hours) => {
  const rates = getRates();
  const multiplier = rates[hourType] || 1.0;
  return hours * rates.baseRate * multiplier;
};

/**
 * Calculate all hour values for an employee
 * @param {Object} employeeData - Employee data with hours
 * @returns {Object} Calculated values
 */
export const calculateEmployeeValues = (employeeData) => {
  const rates = getRates();
  
  const regularValue = (employeeData.regularHours || 0) * rates.baseRate * rates.regularHours;
  const diurnaExtraValue = (employeeData.diurnaExtraHours || 0) * rates.baseRate * rates.diurnaExtraHours;
  const nocturnaExtraValue = (employeeData.nocturnaExtraHours || 0) * rates.baseRate * rates.nocturnaExtraHours;
  const dominicalValue = (employeeData.dominicalHours || 0) * rates.baseRate * rates.dominicalHours;
  const festivoValue = (employeeData.festivoHours || 0) * rates.baseRate * rates.festivoHours;
  
  return {
    regularValue,
    diurnaExtraValue,
    nocturnaExtraValue,
    dominicalValue,
    festivoValue,
    totalValue: regularValue + diurnaExtraValue + nocturnaExtraValue + dominicalValue + festivoValue
  };
};

/**
 * Get rate display value (percentage)
 * @param {string} rateKey - Rate key
 * @returns {string} Display percentage
 */
export const getRateDisplay = (rateKey) => {
  const rates = getRates();
  const multiplier = rates[rateKey] || 1.0;
  return `${((multiplier - 1) * 100).toFixed(0)}%`;
};

export default {
  getRates,
  saveRates,
  resetRates,
  calculateHourValue,
  calculateEmployeeValues,
  getRateDisplay,
  DEFAULT_RATES
};
