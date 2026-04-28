import * as XLSX from 'xlsx';
import { getRates } from './rateConfigService';

// Corporate colors for the Excel export - Professional palette
const COLORS = {
  // Primary colors
  primary: '1F4E79',      // Dark blue - headers
  primaryLight: '2E75B6', // Medium blue
  primaryDark: '153652',  // Darker blue
  
  // Secondary colors  
  secondary: '4472C4',    // Medium blue
  secondaryLight: '8CADEB', // Light blue
  
  // Accent colors
  accent: 'FFC000',       // Gold/Yellow
  accentLight: 'FFE699',  // Light gold
  
  // Status colors
  success: '70AD47',      // Green
  successLight: 'C6E0B4', // Light green
  warning: 'ED7D31',      // Orange
  warningLight: 'F8CBAD', // Light orange
  danger: 'C00000',       // Red
  dangerLight: 'F4B084',  // Light red
  purple: '7030A0',      // Purple
  purpleLight: 'D9D9D9', // Light purple
  
  // Neutral colors
  lightBlue: 'BDD7EE',   // Light blue - totals
  lightGray: 'E7E6E6',   // Light gray - borders
  mediumGray: 'D9D9D9',  // Medium gray
  darkGray: '595959',    // Dark gray - text
  white: 'FFFFFF',
  black: '000000'
};

// Number formats
const FORMATS = {
  currency: '$#,##0.00',
  number: '#,##0.00',
  integer: '#,##0',
  percentage: '0.00%'
};

/**
 * Create a styled header cell
 */
const createHeaderCell = (value, color = COLORS.primary) => {
  return {
    v: value,
    s: {
      font: { bold: true, color: { rgb: COLORS.white }, sz: 11 },
      fill: { fgColor: { rgb: color } },
      alignment: { horizontal: 'center', vertical: 'center' },
      border: {
        left: { style: 'thin', color: { rgb: COLORS.mediumGray } },
        right: { style: 'thin', color: { rgb: COLORS.mediumGray } },
        top: { style: 'thin', color: { rgb: COLORS.mediumGray } },
        bottom: { style: 'thin', color: { rgb: COLORS.mediumGray } }
      }
    }
  };
};

/**
 * Create a styled data cell
 */
const createDataCell = (value, options = {}) => {
  const {
    bold = false,
    color = COLORS.black,
    bgColor = COLORS.white,
    align = 'left',
    format = null,
    border = false
  } = options;
  
  const cellStyle = {
    font: { bold, color: { rgb: color }, sz: 10 },
    fill: { fgColor: { rgb: bgColor } },
    alignment: { horizontal: align, vertical: 'center' },
    numFmt: format
  };
  
  if (border) {
    cellStyle.border = {
      left: { style: 'thin', color: { rgb: COLORS.lightGray } },
      right: { style: 'thin', color: { rgb: COLORS.lightGray } },
      top: { style: 'thin', color: { rgb: COLORS.lightGray } },
      bottom: { style: 'thin', color: { rgb: COLORS.lightGray } }
    };
  }
  
  return { v: value, s: cellStyle };
};

/**
 * Create a currency cell
 */
const createCurrencyCell = (value, options = {}) => {
  return createDataCell(value, { ...options, format: FORMATS.currency });
};

/**
 * Create a number cell
 */
const createNumberCell = (value, options = {}) => {
  return createDataCell(value, { ...options, format: FORMATS.number });
};

/**
 * Export shifts to Excel with professional formatting
 */
export const exportShiftsToExcel = (shifts, filename = 'turnos') => {
  // Prepare data for Excel
  const wsData = [
    // Title row
    [createHeaderCell(`LISTADO DE TURNOS - ${new Date().toLocaleDateString('es-CO')}`, COLORS.primaryDark)],
    [''], // Empty row
    // Header row
    [
      createHeaderCell('FECHA'),
      createHeaderCell('EMPLEADO'),
      createHeaderCell('UBICACIÓN'),
      createHeaderCell('TIPO DE TURNO'),
      createHeaderCell('HORA INICIO'),
      createHeaderCell('HORA FIN'),
      createHeaderCell('NOTAS')
    ]
  ];

  // Data rows
  shifts.forEach(shift => {
    wsData.push([
      createDataCell(shift.date ? shift.date.split('-').reverse().join('/') : '', { align: 'center', border: true }),
      createDataCell(shift.employee ? `${shift.employee.firstName} ${shift.employee.lastName}` : '', { border: true }),
      createDataCell(shift.location?.name || '', { border: true }),
      createDataCell(shift.shiftType?.name || '', { border: true }),
      createDataCell(shift.shiftType?.startTime?.slice(0, 5) || '', { align: 'center', border: true }),
      createDataCell(shift.shiftType?.endTime?.slice(0, 5) || '', { align: 'center', border: true }),
      createDataCell(shift.notes || '', { border: true })
    ]);
  });

  // Summary row
  const totalShifts = shifts.length;
  wsData.push([
    createDataCell('TOTAL:', { bold: true, bgColor: COLORS.lightBlue }),
    createDataCell(totalShifts, { bold: true, bgColor: COLORS.lightBlue, align: 'center' }),
    createDataCell('', { bgColor: COLORS.lightBlue }),
    createDataCell('', { bgColor: COLORS.lightBlue }),
    createDataCell('', { bgColor: COLORS.lightBlue }),
    createDataCell('', { bgColor: COLORS.lightBlue }),
    createDataCell('', { bgColor: COLORS.lightBlue })
  ]);

  // Create workbook and worksheet
  const wb = XLSX.utils.book_new();
  const ws = XLSX.utils.aoa_to_sheet(wsData);

  // Set column widths
  ws['!cols'] = [
    { wch: 12 }, // Fecha
    { wch: 25 }, // Empleado
    { wch: 20 }, // Ubicación
    { wch: 20 }, // Tipo de turno
    { wch: 12 }, // Hora inicio
    { wch: 12 }, // Hora fin
    { wch: 30 }  // Notas
  ];

  XLSX.utils.book_append_sheet(wb, ws, 'Turnos');

  // Generate and download file
  XLSX.writeFile(wb, `${filename}_${new Date().toISOString().split('T')[0]}.xlsx`);
};

/**
 * Export report to Excel by calling the backend API with authentication
 * The backend (Java + Apache POI) generates the Excel with full styling
 */
export const exportReportToExcel = async (report, selectedMonth, selectedYear, options = {}) => {
  const { locationId, employeeId } = options;
  
  // Build the backend URL with rates and limits
  const rates = getRates();
  let url = `/api/schedules/reports/export/excel?month=${selectedMonth}&year=${selectedYear}`;
  url += `&baseRate=${rates.baseRate}`;
  url += `&regularHours=${rates.regularHours}`;
  url += `&diurnaExtraHours=${rates.diurnaExtraHours}`;
  url += `&nocturnaExtraHours=${rates.nocturnaExtraHours}`;
  url += `&dominicalHours=${rates.dominicalHours}`;
  url += `&festivoHours=${rates.festivoHours}`;
  url += `&monthlyHourLimit=${rates.monthlyHourLimit}`;
  if (locationId) {
    url += `&locationId=${locationId}`;
  }
  if (employeeId) {
    url += `&employeeId=${employeeId}`;
  }
  
  try {
    // Make authenticated request with JWT token
    const token = localStorage.getItem('token');
    const headers = {
      'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    };
    
    // Add authorization header if token exists
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    const response = await fetch(url, {
      method: 'GET',
      credentials: 'include', // Send cookies for authentication
      headers: headers
    });
    
    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Error ${response.status}: ${errorText}`);
    }
    
    // Get the blob from the response
    const blob = await response.blob();
    
    // Extract filename from Content-Disposition header or use default
    const contentDisposition = response.headers.get('Content-Disposition');
    let filename = `reporte_${selectedYear}_${selectedMonth}.xlsx`;
    if (contentDisposition) {
      const match = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
      if (match && match[1]) {
        filename = match[1].replace(/['"]/g, '');
      }
    }
    
    // Create download link and trigger download
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(downloadUrl);
    
  } catch (error) {
    console.error('Error exporting Excel:', error);
    alert('Error al generar el Excel: ' + error.message);
  }
};

export default {
  exportShiftsToExcel,
  exportReportToExcel
};
