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
 * Export report to Excel with multiple sheets, formulas, and professional formatting
 */
export const exportReportToExcel = (report, selectedMonth, selectedYear) => {
  const rates = getRates();
  const monthNames = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
  const monthName = monthNames[selectedMonth - 1];

  // Create workbook
  const wb = XLSX.utils.book_new();

  // ===== SHEET 1: RESUMEN EJECUTIVO =====
  const summaryData = [
    // Company title
    [createHeaderCell('MICROFARMA HORARIOS', COLORS.primaryDark)],
    [createHeaderCell(`REPORTE DE HORAS TRABAJADAS`, COLORS.primary)],
    [createHeaderCell(`${monthName.toUpperCase()} ${selectedYear}`, COLORS.secondary)],
    [''], // Empty row
    // Global summary header
    [createHeaderCell('RESUMEN GLOBAL', COLORS.primary)],
    // Summary data
    [
      createDataCell('Total Empleados:', { bold: true }),
      createDataCell(report?.global?.totalEmployees || 0, { bold: true, align: 'center', bgColor: COLORS.lightBlue })
    ],
    [
      createDataCell('Total Horas Trabajadas:', { bold: true }),
      createNumberCell(report?.global?.totalHours || 0, { bold: true, bgColor: COLORS.lightBlue })
    ],
    [
      createDataCell('Horas Regulares:', { bold: true }),
      createNumberCell(report?.global?.totalRegularHours || 0)
    ],
    [
      createDataCell('Horas Extras Totales:', { bold: true }),
      createNumberCell(report?.global?.totalOvertimeHours || 0, { color: COLORS.warning })
    ],
    [
      createDataCell('Extras Diurnas:', { bold: true }),
      createNumberCell(report?.global?.totalDiurnaExtraHours || 0)
    ],
    [
      createDataCell('Extras Nocturnas:', { bold: true }),
      createNumberCell(report?.global?.totalNocturnaExtraHours || 0)
    ],
    [
      createDataCell('Horas Dominicales:', { bold: true }),
      createNumberCell(report?.global?.totalDominicalHours || 0, { color: COLORS.purple })
    ],
    [
      createDataCell('Horas Festivas:', { bold: true }),
      createNumberCell(report?.global?.totalFestivoHours || 0, { color: COLORS.danger })
    ],
    [
      createDataCell('Total Turnos:', { bold: true }),
      createDataCell(report?.global?.totalShifts || 0, { bold: true, align: 'center' })
    ],
    [''], // Empty row
    // Configuration rates section
    [createHeaderCell('CONFIGURACIÓN DE TARIFAS', COLORS.secondary)],
    [
      createDataCell('Tarifa Base por Hora:', { bold: true }),
      createCurrencyCell(rates.baseRate, { bold: true, bgColor: COLORS.accentLight })
    ],
    [
      createDataCell('Recargo Horas Extra Diurnas:', { bold: true }),
      createDataCell(`${((rates.diurnaExtraHours - 1) * 100).toFixed(0)}%`, { bold: true })
    ],
    [
      createDataCell('Recargo Horas Extra Nocturnas:', { bold: true }),
      createDataCell(`${((rates.nocturnaExtraHours - 1) * 100).toFixed(0)}%`, { bold: true })
    ],
    [
      createDataCell('Recargo Horas Dominicales:', { bold: true }),
      createDataCell(`${((rates.dominicalHours - 1) * 100).toFixed(0)}%`, { bold: true })
    ],
    [
      createDataCell('Recargo Horas Festivas:', { bold: true }),
      createDataCell(`${((rates.festivoHours - 1) * 100).toFixed(0)}%`, { bold: true })
    ],
    [''], // Empty row
    // Legend
    [createHeaderCell('LEYENDA DE COLORES', COLORS.mediumGray)],
    [createDataCell('Horas Regulares:', { bold: true }), createDataCell('Color 默认', { bgColor: COLORS.white })],
    [createDataCell('Horas Extras:', { bold: true }), createDataCell('Color naranja', { bgColor: COLORS.warningLight })],
    [createDataCell('Horas Dominicales:', { bold: true }), createDataCell('Color morado', { bgColor: COLORS.purpleLight })],
    [createDataCell('Horas Festivas:', { bold: true }), createDataCell('Color rojo', { bgColor: COLORS.dangerLight })],
    [createDataCell('Total:', { bold: true }), createDataCell('Color azul claro', { bgColor: COLORS.lightBlue })]
  ];

  const wsSummary = XLSX.utils.aoa_to_sheet(summaryData);
  wsSummary['!cols'] = [{ wch: 35 }, { wch: 20 }];
  XLSX.utils.book_append_sheet(wb, wsSummary, 'Resumen');

  // ===== SHEET 2: EMPLEADOS =====
  if (report?.employees && report.employees.length > 0) {
    const employeeHeaders = [
      createHeaderCell('Nombre Completo', COLORS.primary),
      createHeaderCell('Horas Totales', COLORS.primary),
      createHeaderCell('Horas Regulares', COLORS.primary),
      createHeaderCell('Horas Extras', COLORS.warning),
      createHeaderCell('Extras Diurnas', COLORS.warning),
      createHeaderCell('Extras Nocturnas', COLORS.warning),
      createHeaderCell('Horas Dominicales', COLORS.purple),
      createHeaderCell('Horas Festivas', COLORS.danger),
      createHeaderCell('Total Turnos', COLORS.secondary),
      createHeaderCell('Valor Regulares', COLORS.success),
      createHeaderCell('Valor Extras', COLORS.success),
      createHeaderCell('Valor Dominicales', COLORS.success),
      createHeaderCell('Valor Festivas', COLORS.success),
      createHeaderCell('TOTAL A PAGAR', COLORS.primaryDark)
    ];

    const employeeData = [employeeHeaders];

    // Add employee rows with calculated values
    report.employees.forEach((emp) => {
      // Calculate values using configurable rates
      const regularHours = emp.regularHours || 0;
      const overtimeHours = emp.overtimeHours || 0;
      const diurnaExtra = emp.diurnaExtraHours || 0;
      const nocturnaExtra = emp.nocturnaExtraHours || 0;
      const dominicalHours = emp.dominicalHours || 0;
      const festivoHours = emp.festivoHours || 0;

      const regularValue = regularHours * rates.baseRate * rates.regularHours;
      const overtimeValue = (diurnaExtra + nocturnaExtra) * rates.baseRate * rates.diurnaExtraHours;
      const dominicalValue = dominicalHours * rates.baseRate * rates.dominicalHours;
      const festivoValue = festivoHours * rates.baseRate * rates.festivoHours;
      const totalPay = regularValue + overtimeValue + dominicalValue + festivoValue;

      employeeData.push([
        createDataCell(emp.fullName || '', { border: true }),
        createNumberCell(emp.totalHours || 0, { border: true }),
        createNumberCell(regularHours, { border: true }),
        createNumberCell(overtimeHours, { border: true, color: COLORS.warning }),
        createNumberCell(diurnaExtra, { border: true }),
        createNumberCell(nocturnaExtra, { border: true }),
        createNumberCell(dominicalHours, { border: true, color: COLORS.purple }),
        createNumberCell(festivoHours, { border: true, color: COLORS.danger }),
        createDataCell(emp.totalShifts || 0, { align: 'center', border: true }),
        createCurrencyCell(regularValue, { border: true }),
        createCurrencyCell(overtimeValue, { border: true, color: COLORS.warning }),
        createCurrencyCell(dominicalValue, { border: true, color: COLORS.purple }),
        createCurrencyCell(festivoValue, { border: true, color: COLORS.danger }),
        createCurrencyCell(totalPay, { bold: true, border: true, bgColor: COLORS.lightBlue })
      ]);
    });

    // Add totals row
    const totalRow = [
      createDataCell('TOTALES', { bold: true, bgColor: COLORS.lightBlue }),
      createNumberCell(report.global?.totalHours || 0, { bold: true, bgColor: COLORS.lightBlue }),
      createNumberCell(report.global?.totalRegularHours || 0, { bold: true, bgColor: COLORS.lightBlue }),
      createNumberCell(report.global?.totalOvertimeHours || 0, { bold: true, bgColor: COLORS.lightBlue, color: COLORS.warning }),
      createNumberCell(report.global?.totalDiurnaExtraHours || 0, { bold: true, bgColor: COLORS.lightBlue }),
      createNumberCell(report.global?.totalNocturnaExtraHours || 0, { bold: true, bgColor: COLORS.lightBlue }),
      createNumberCell(report.global?.totalDominicalHours || 0, { bold: true, bgColor: COLORS.lightBlue, color: COLORS.purple }),
      createNumberCell(report.global?.totalFestivoHours || 0, { bold: true, bgColor: COLORS.lightBlue, color: COLORS.danger }),
      createDataCell(report.global?.totalShifts || 0, { bold: true, align: 'center', bgColor: COLORS.lightBlue }),
      createDataCell('', { bgColor: COLORS.lightBlue }),
      createDataCell('', { bgColor: COLORS.lightBlue }),
      createDataCell('', { bgColor: COLORS.lightBlue }),
      createDataCell('', { bgColor: COLORS.lightBlue }),
      createDataCell('', { bgColor: COLORS.lightBlue })
    ];
    employeeData.push(totalRow);

    const wsEmployees = XLSX.utils.aoa_to_sheet(employeeData);
    wsEmployees['!cols'] = [
      { wch: 25 }, // Nombre
      { wch: 13 }, // Horas Totales
      { wch: 14 }, // Horas Regulares
      { wch: 13 }, // Horas Extras
      { wch: 15 }, // Extras Diurnas
      { wch: 16 }, // Extras Nocturnas
      { wch: 17 }, // Horas Dominicales
      { wch: 15 }, // Horas Festivas
      { wch: 12 }, // Total Turnos
      { wch: 16 }, // Valor Regulares
      { wch: 16 }, // Valor Extras
      { wch: 17 }, // Valor Dominicales
      { wch: 16 }, // Valor Festivas
      { wch: 18 }  // TOTAL A PAGAR
    ];
    XLSX.utils.book_append_sheet(wb, wsEmployees, 'Empleados');
  }

  // ===== SHEET 3: SEDES =====
  if (report?.locations && report.locations.length > 0) {
    const locationHeaders = [
      createHeaderCell('Sede', COLORS.primary),
      createHeaderCell('Total Empleados', COLORS.primary),
      createHeaderCell('Horas Totales', COLORS.secondary),
      createHeaderCell('Horas Regulares', COLORS.secondary),
      createHeaderCell('Horas Extras', COLORS.warning),
      createHeaderCell('Extras Diurnas', COLORS.warning),
      createHeaderCell('Extras Nocturnas', COLORS.warning),
      createHeaderCell('Horas Dominicales', COLORS.purple),
      createHeaderCell('Horas Festivas', COLORS.danger),
      createHeaderCell('Total Turnos', COLORS.primary)
    ];

    const locationData = [locationHeaders];

    report.locations.forEach(loc => {
      locationData.push([
        createDataCell(loc.locationName || '', { bold: true, border: true }),
        createDataCell(loc.totalEmployees || 0, { align: 'center', border: true }),
        createNumberCell(loc.totalHours || 0, { border: true }),
        createNumberCell(loc.totalRegularHours || 0, { border: true }),
        createNumberCell(loc.totalOvertimeHours || 0, { border: true, color: COLORS.warning }),
        createNumberCell(loc.totalDiurnaExtraHours || 0, { border: true }),
        createNumberCell(loc.totalNocturnaExtraHours || 0, { border: true }),
        createNumberCell(loc.totalDominicalHours || 0, { border: true, color: COLORS.purple }),
        createNumberCell(loc.totalFestivoHours || 0, { border: true, color: COLORS.danger }),
        createDataCell(loc.totalShifts || 0, { align: 'center', border: true })
      ]);
    });

    const wsLocations = XLSX.utils.aoa_to_sheet(locationData);
    wsLocations['!cols'] = [
      { wch: 25 },
      { wch: 16 },
      { wch: 14 },
      { wch: 15 },
      { wch: 13 },
      { wch: 15 },
      { wch: 16 },
      { wch: 17 },
      { wch: 15 },
      { wch: 13 }
    ];
    XLSX.utils.book_append_sheet(wb, wsLocations, 'Sedes');
  }

  // Generate and download
  const filename = `reporte_horas_${monthName.toLowerCase()}_${selectedYear}`;
  XLSX.writeFile(wb, `${filename}.xlsx`);
};

export default {
  exportShiftsToExcel,
  exportReportToExcel
};
