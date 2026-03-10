import { useState, useEffect } from 'react';
import Button from '../../components/Button';
import hourTemplateAssignmentService from '../../services/hourTemplateAssignmentService';
import hourTemplateService from '../../services/hourTemplateService';
import employeeService from '../../services/employeeService';
import locationService from '../../services/locationService';

const MonthlyReport = () => {
  const [loading, setLoading] = useState(false);
  const [reportData, setReportData] = useState(null);
  const [employees, setEmployees] = useState([]);
  const [locations, setLocations] = useState([]);
  const [templates, setTemplates] = useState([]);
  
  // Filters
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth() + 1);
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  const [filterEmployee, setFilterEmployee] = useState('');
  const [filterLocation, setFilterLocation] = useState('');

  const months = [
    { value: 1, label: 'Enero' },
    { value: 2, label: 'Febrero' },
    { value: 3, label: 'Marzo' },
    { value: 4, label: 'Abril' },
    { value: 5, label: 'Mayo' },
    { value: 6, label: 'Junio' },
    { value: 7, label: 'Julio' },
    { value: 8, label: 'Agosto' },
    { value: 9, label: 'Septiembre' },
    { value: 10, label: 'Octubre' },
    { value: 11, label: 'Noviembre' },
    { value: 12, label: 'Diciembre' }
  ];

  const years = [2024, 2025, 2026, 2027, 2028];


  useEffect(() => {
    loadFilterData();
  }, []);

  const loadFilterData = async () => {
    try {
      const [employeesRes, locationsRes, templatesRes] = await Promise.all([
        employeeService.getActiveEmployees(),
        locationService.getAllLocations(),
        hourTemplateService.getAllTemplates()
      ]);

      if (employeesRes.data) setEmployees(employeesRes.data);
      if (locationsRes.data) setLocations(locationsRes.data);
      if (templatesRes.data) setTemplates(templatesRes.data);
    } catch (error) {
      console.error('Error loading filter data:', error);
      // Fallback to local storage
      const templatesRes = await hourTemplateService.getAllTemplates();
      if (templatesRes.data) setTemplates(templatesRes.data);
    }
  };

  const generateReport = async () => {
    setLoading(true);
    try {
      // Get all assignments
      const assignmentsRes = await hourTemplateAssignmentService.getAllAssignments();
      const assignments = assignmentsRes.data || [];

      // Filter by month and year
      let filtered = assignments.filter(a => 
        a.month === selectedMonth && a.year === selectedYear
      );

      // Apply additional filters
      if (filterEmployee) {
        filtered = filtered.filter(a => a.employeeId === filterEmployee);
      }
      if (filterLocation) {
        filtered = filtered.filter(a => a.locationId === filterLocation);
      }

      // Build report data
      const reportEmployees = filtered.map(assignment => {
        const template = templates.find(t => t.id === assignment.templateId);
        const employee = employees.find(e => e.id === assignment.employeeId);
        const location = locations.find(l => l.id === assignment.locationId);
        
        const totals = template ? hourTemplateService.calculateTotals(template) : { totalHours: 0, totalValue: 0 };

        return {
          employeeId: assignment.employeeId,
          employeeName: employee ? `${employee.firstName} ${employee.lastName}` : 'Desconocido',
          locationName: location ? location.name : 'Desconocida',
          templateName: template ? template.name : 'Desconocida',
          ordinarias: template ? template.ordinarias : 0,
          diurnas: template ? template.diurnas : 0,
          nocturnas: template ? template.nocturnas : 0,
          extraFDom: template ? template.extraFDom : 0,
          dominicales: template ? template.dominicales : 0,
          totalHours: totals.totalHours,
          totalValue: totals.totalValue
        };
      });

      // Calculate grand totals
      const grandTotals = {
        ordinarias: 0,
        diurnas: 0,
        nocturnas: 0,
        extraFDom: 0,
        dominicales: 0,
        totalHours: 0,
        totalValue: 0
      };

      reportEmployees.forEach(emp => {
        grandTotals.ordinarias += emp.ordinarias;
        grandTotals.diurnas += emp.diurnas;
        grandTotals.nocturnas += emp.nocturnas;
        grandTotals.extraFDom += emp.extraFDom;
        grandTotals.dominicales += emp.dominicales;
        grandTotals.totalHours += emp.totalHours;
        grandTotals.totalValue += emp.totalValue;
      });

      setReportData({
        month: selectedMonth,
        year: selectedYear,
        employees: reportEmployees,
        grandTotals,
        generatedAt: new Date().toISOString()
      });
    } catch (error) {
      console.error('Error generating report:', error);
      alert('Error al generar el reporte');
    } finally {
      setLoading(false);
    }
  };

  const generatePDF = () => {
    if (!reportData) return;

    const monthName = months.find(m => m.value === reportData.month)?.label;
    
    // Create HTML content for printing
    const printContent = `
      <!DOCTYPE html>
      <html>
      <head>
        <title>Reporte de Horas - ${monthName} ${reportData.year}</title>
        <style>
          * { margin: 0; padding: 0; box-sizing: border-box; }
          body { font-family: Arial, sans-serif; padding: 20px; }
          .header { text-align: center; margin-bottom: 20px; border-bottom: 2px solid #dc2626; padding-bottom: 10px; }
          .header h1 { color: #dc2626; font-size: 24px; margin-bottom: 5px; }
          .header .subtitle { color: #666; font-size: 14px; }
          .filters { background: #f3f4f6; padding: 10px; margin-bottom: 20px; border-radius: 4px; }
          .filters p { margin: 2px 0; font-size: 12px; }
          table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
          th { background: #dc2626; color: white; padding: 10px; text-align: center; font-size: 12px; }
          td { padding: 8px; text-align: center; border: 1px solid #ddd; font-size: 11px; }
          tr:nth-child(even) { background: #f9fafb; }
          .totals-row { background: #fef3c7 !important; font-weight: bold; }
          .summary { display: flex; justify-content: space-around; margin-top: 20px; padding: 15px; background: #f0fdf4; border-radius: 4px; }
          .summary-item { text-align: center; }
          .summary-item .label { font-size: 12px; color: #666; }
          .summary-item .value { font-size: 18px; font-weight: bold; color: #166534; }
          .footer { text-align: center; margin-top: 20px; font-size: 10px; color: #999; }
        </style>
      </head>
      <body>
        <div class="header">
          <h1>MICROFARMA HORARIOS</h1>
          <div class="subtitle">Reporte de Horas - ${monthName} ${reportData.year}</div>
        </div>
        
        <div class="filters">
          <p><strong>Fecha de generación:</strong> ${new Date(reportData.generatedAt).toLocaleString('es-CO')}</p>
          <p><strong>Total empleados:</strong> ${reportData.employees.length}</p>
          ${filterEmployee ? `<p><strong>Filtro empleado:</strong> ${employees.find(e => e.id === filterEmployee)?.firstName} ${employees.find(e => e.id === filterEmployee)?.lastName}</p>` : ''}
          ${filterLocation ? `<p><strong>Filtro ubicación:</strong> ${locations.find(l => l.id === filterLocation)?.name}</p>` : ''}
        </div>

        <table>
          <thead>
            <tr>
              <th>EMPLEADO</th>
              <th>UBICACIÓN</th>
              <th>ORDINARIAS</th>
              <th>DIURNAS</th>
              <th>NOCTURNAS</th>
              <th>EXTRA F/DOM</th>
              <th>DOMINICALES</th>
              <th>TOTAL HORAS</th>
            </tr>
          </thead>
          <tbody>
            ${reportData.employees.map(emp => `
              <tr>
                <td style="text-align: left;">${emp.employeeName}</td>
                <td style="text-align: left;">${emp.locationName}</td>
                <td>${emp.ordinarias > 0 ? emp.ordinarias.toFixed(2) : '-'}</td>
                <td>${emp.diurnas > 0 ? emp.diurnas.toFixed(2) : '-'}</td>
                <td>${emp.nocturnas > 0 ? emp.nocturnas.toFixed(2) : '-'}</td>
                <td>${emp.extraFDom > 0 ? emp.extraFDom.toFixed(2) : '-'}</td>
                <td>${emp.dominicales > 0 ? emp.dominicales.toFixed(2) : '-'}</td>
                <td><strong>${emp.totalHours.toFixed(2)}</strong></td>
              </tr>
            `).join('')}
            <tr class="totals-row">
              <td colspan="2"><strong>TOTALES GENERALES</strong></td>
              <td>${reportData.grandTotals.ordinarias.toFixed(2)}</td>
              <td>${reportData.grandTotals.diurnas.toFixed(2)}</td>
              <td>${reportData.grandTotals.nocturnas.toFixed(2)}</td>
              <td>${reportData.grandTotals.extraFDom.toFixed(2)}</td>
              <td>${reportData.grandTotals.dominicales.toFixed(2)}</td>
              <td><strong>${reportData.grandTotals.totalHours.toFixed(2)}</strong></td>
            </tr>
          </tbody>
        </table>

        <div class="summary">
          <div class="summary-item">
            <div class="label">Total Empleados</div>
            <div class="value">${reportData.employees.length}</div>
          </div>
          <div class="summary-item">
            <div class="label">Total Horas</div>
            <div class="value">${reportData.grandTotals.totalHours.toFixed(2)}</div>
          </div>
        </div>

        <div class="footer">
          <p>Sistema de Gestión de Horarios - Microfarma</p>
        </div>
      </body>
      </html>
    `;

    // Open print window
    const printWindow = window.open('', '_blank');
    printWindow.document.write(printContent);
    printWindow.document.close();
    printWindow.print();
  };

  return (
    <div className="p-6">
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Reporte Mensual de Horas</h1>
        <p className="text-gray-600 mt-1">
          Genera reportes PDF con el desglose de horas por empleado y tipo
        </p>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-4 mb-6">
        <div className="grid grid-cols-4 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Mes</label>
            <select
              value={selectedMonth}
              onChange={(e) => setSelectedMonth(parseInt(e.target.value))}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
            >
              {months.map(m => (
                <option key={m.value} value={m.value}>{m.label}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Año</label>
            <select
              value={selectedYear}
              onChange={(e) => setSelectedYear(parseInt(e.target.value))}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
            >
              {years.map(y => (
                <option key={y} value={y}>{y}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Empleado (Opcional)</label>
            <select
              value={filterEmployee}
              onChange={(e) => setFilterEmployee(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
            >
              <option value="">Todos</option>
              {employees.map(e => (
                <option key={e.id} value={e.id}>
                  {e.firstName} {e.lastName}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Ubicación (Opcional)</label>
            <select
              value={filterLocation}
              onChange={(e) => setFilterLocation(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
            >
              <option value="">Todas</option>
              {locations.map(l => (
                <option key={l.id} value={l.id}>{l.name}</option>
              ))}
            </select>
          </div>
        </div>
        
        <div className="mt-4 flex justify-end">
          <Button onClick={generateReport} loading={loading}>
            Generar Reporte
          </Button>
        </div>
      </div>

      {/* Report Preview */}
      {reportData && (
        <div className="bg-white rounded-2xl shadow-xl border border-gray-100 overflow-hidden">
          {/* Report Header */}
          <div className="bg-gradient-to-r from-red-500 to-red-600 text-white px-6 py-4">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-xl font-bold">
                  Reporte de Horas - {months.find(m => m.value === reportData.month)?.label} {reportData.year}
                </h2>
                <p className="text-red-100 text-sm mt-1">
                  {reportData.employees.length} empleados reportados
                </p>
              </div>
              <Button 
                variant="outline" 
                onClick={generatePDF}
                className="bg-white text-red-600 hover:bg-red-50"
              >
                📄 Descargar PDF
              </Button>
            </div>
          </div>

          {/* Report Table */}
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Empleado
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Ubicación
                  </th>
                  <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                    ORDINARIAS
                  </th>
                  <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                    DIURNAS
                  </th>
                  <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                    NOCTURNAS
                  </th>
                  <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                    EXTRA F/DOM
                  </th>
                  <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                    DOMINICALES
                  </th>
                  <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                    TOTAL HORAS
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {reportData.employees.map((emp, index) => (
                  <tr key={index} className={index % 2 === 0 ? 'bg-white' : 'bg-gray-50'}>
                    <td className="px-6 py-4 whitespace-nowrap font-medium text-gray-900">
                      {emp.employeeName}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-gray-700">
                      {emp.locationName}
                    </td>
                    <td className="px-4 py-4 text-center text-gray-700">
                      {emp.ordinarias > 0 ? emp.ordinarias.toFixed(2) : '-'}
                    </td>
                    <td className="px-4 py-4 text-center text-gray-700">
                      {emp.diurnas > 0 ? emp.diurnas.toFixed(2) : '-'}
                    </td>
                    <td className="px-4 py-4 text-center text-gray-700">
                      {emp.nocturnas > 0 ? emp.nocturnas.toFixed(2) : '-'}
                    </td>
                    <td className="px-4 py-4 text-center text-gray-700">
                      {emp.extraFDom > 0 ? emp.extraFDom.toFixed(2) : '-'}
                    </td>
                    <td className="px-4 py-4 text-center text-gray-700">
                      {emp.dominicales > 0 ? emp.dominicales.toFixed(2) : '-'}
                    </td>
                    <td className="px-4 py-4 text-center font-semibold text-gray-900">
                      {emp.totalHours.toFixed(2)}
                    </td>
                  </tr>
                ))}
                
                {/* Grand Totals Row */}
                <tr className="bg-yellow-50">
                  <td colSpan="2" className="px-6 py-4 font-bold text-gray-900">
                    TOTALES GENERALES
                  </td>
                  <td className="px-4 py-4 text-center font-semibold text-gray-900">
                    {reportData.grandTotals.ordinarias.toFixed(2)}
                  </td>
                  <td className="px-4 py-4 text-center font-semibold text-gray-900">
                    {reportData.grandTotals.diurnas.toFixed(2)}
                  </td>
                  <td className="px-4 py-4 text-center font-semibold text-gray-900">
                    {reportData.grandTotals.nocturnas.toFixed(2)}
                  </td>
                  <td className="px-4 py-4 text-center font-semibold text-gray-900">
                    {reportData.grandTotals.extraFDom.toFixed(2)}
                  </td>
                  <td className="px-4 py-4 text-center font-semibold text-gray-900">
                    {reportData.grandTotals.dominicales.toFixed(2)}
                  </td>
                  <td className="px-4 py-4 text-center font-bold text-gray-900">
                    {reportData.grandTotals.totalHours.toFixed(2)}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          {/* Summary Cards */}
          <div className="bg-gray-50 px-6 py-4 border-t border-gray-200">
            <div className="grid grid-cols-2 gap-4">
              <div className="bg-white rounded-lg p-4 shadow-sm">
                <div className="text-sm text-gray-500">Total Empleados</div>
                <div className="text-2xl font-bold text-gray-900">{reportData.employees.length}</div>
              </div>
              <div className="bg-white rounded-lg p-4 shadow-sm">
                <div className="text-sm text-gray-500">Total Horas</div>
                <div className="text-2xl font-bold text-gray-900">{reportData.grandTotals.totalHours.toFixed(2)}</div>
              </div>
            </div>
          </div>

          {/* Footer */}
          <div className="px-6 py-3 border-t border-gray-200 text-xs text-gray-500 text-center">
            Generado el {new Date(reportData.generatedAt).toLocaleString('es-CO')}
          </div>
        </div>
      )}

      {/* Empty State */}
      {!reportData && !loading && (
        <div className="bg-white rounded-2xl shadow-xl border border-gray-100 p-12 text-center">
          <div className="text-6xl mb-4">📊</div>
          <h3 className="text-lg font-semibold text-gray-900 mb-2">
            Sin datos para mostrar
          </h3>
          <p className="text-gray-600 mb-4">
            Selecciona el mes y año, luego haz clic en "Generar Reporte" para ver los datos.
          </p>
          <p className="text-sm text-gray-500">
            También puedes filtrar por empleado o ubicación específica.
          </p>
        </div>
      )}
    </div>
  );
};

export default MonthlyReport;
