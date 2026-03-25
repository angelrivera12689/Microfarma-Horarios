import { useState, useEffect, useCallback } from 'react';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import Button from '../../components/Button';
import reportService from '../../services/reportService';
import { exportReportToExcel } from '../../services/excelExportService';
import { getRates, saveRates, resetRates } from '../../services/rateConfigService';
import useAsyncOperation from '../../hooks/useAsyncOperation';

const Reports = () => {
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(false);
  const [filtersLoading, setFiltersLoading] = useState(true);
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth() + 1);
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  const [expandedEmployee, setExpandedEmployee] = useState(null);
  const [reportType, setReportType] = useState('general'); // 'general', 'location', 'employee'
  const [selectedLocation, setSelectedLocation] = useState('');
  const [selectedEmployee, setSelectedEmployee] = useState('');

  // Rate configuration modal
  const [configModalOpen, setConfigModalOpen] = useState(false);
  const [rateConfig, setRateConfig] = useState(() => getRates());

  // Filter options loaded from backend
  const [filterOptions, setFilterOptions] = useState({
    locations: [],
    employees: [],
    years: [],
    statuses: []
  });

  // Load filters on mount
  useEffect(() => {
    loadFilters();
  }, []);

  // Load report when filters change
  useEffect(() => {
    loadReport();
  }, [selectedMonth, selectedYear, reportType, selectedLocation, selectedEmployee]);

  const loadFilters = async () => {
    setFiltersLoading(true);
    try {
      const response = await reportService.getAvailableFilters();
      console.log('Filters response:', response);
      // The response structure is { status: true, data: {...}, message: '...' }
      if (response && (response.success === true || response.status === true) && response.data) {
        // Helper function to check if location is for delivery
        const isDeliveryLocation = (locationName) => {
          if (!locationName) return false;
          const name = locationName.toLowerCase();
          return name.includes('zona norte') || 
                 name.includes('oriente') || 
                 name.includes('sur');
        };
        
        setFilterOptions({
          locations: (response.data.locations || []).filter(loc => !isDeliveryLocation(loc.name)),
          employees: (response.data.employees || []).filter(emp => 
            !emp.position || !emp.position.toLowerCase().includes('domicili')
          ),
          years: response.data.years || [],
          statuses: response.data.statuses || []
        });
      } else {
        console.error('Error loading filters:', response?.data?.message || response?.message || 'Unknown error');
      }
    } catch (error) {
      console.error('Exception loading filters:', error);
      console.error('Error response:', error.response);
    } finally {
      setFiltersLoading(false);
    }
  };

  const loadReport = useCallback(async () => {
    setLoading(true);
    // Helper function to check if location is for delivery
    const isDeliveryLocation = (locationName) => {
      if (!locationName) return false;
      const name = locationName.toLowerCase();
      return name.includes('zona norte') || 
             name.includes('oriente') || 
             name.includes('sur');
    };
    
    try {
      let response;
      
      switch (reportType) {
        case 'location':
          if (selectedLocation) {
            response = await reportService.getMonthlyReportByLocation(selectedMonth, selectedYear, selectedLocation);
          } else {
            response = await reportService.getMonthlyReport(selectedMonth, selectedYear);
          }
          break;
        case 'employee':
          console.log('Loading employee report, selectedEmployee:', selectedEmployee);
          if (selectedEmployee) {
            response = await reportService.getMonthlyReportByEmployee(selectedMonth, selectedYear, selectedEmployee);
          } else {
            response = await reportService.getMonthlyReport(selectedMonth, selectedYear);
          }
          break;
        default:
          response = await reportService.getMonthlyReport(selectedMonth, selectedYear);
      }
      
      console.log('Report response:', response);
      // Check for both 'success' and 'status' properties for compatibility
      // The response structure is { status: true, data: {...}, message: '...' }
      if (response && (response.success === true || response.status === true) && response.data) {
        console.log('Report data:', response.data);
        console.log('Report global:', response.data.global);
        console.log('Report employees:', response.data.employees);
        console.log('Report locations:', response.data.locations);
        
        // Filter out delivery employees and locations from the report
        const filteredEmployees = (response.data.employees || []).filter(emp => 
          !emp.positionName || !emp.positionName.toLowerCase().includes('domicili')
        );
        
        // Also filter out delivery locations from the report
        const filteredLocations = (response.data.locations || []).filter(loc => 
          !isDeliveryLocation(loc.locationName)
        );
        
        // Recalculate global totals based on filtered employees
        const filteredGlobal = response.data.global ? {
          ...response.data.global,
          totalEmployees: filteredEmployees.length,
          totalHours: filteredEmployees.reduce((sum, e) => sum + (e.totalHours || 0), 0),
          totalOvertimeHours: filteredEmployees.reduce((sum, e) => sum + (e.overtimeHours || 0), 0),
          totalRegularHours: filteredEmployees.reduce((sum, e) => sum + (e.regularHours || 0), 0),
          totalDiurnaExtraHours: filteredEmployees.reduce((sum, e) => sum + (e.diurnaExtraHours || 0), 0),
          totalNocturnaExtraHours: filteredEmployees.reduce((sum, e) => sum + (e.nocturnaExtraHours || 0), 0),
          totalDominicalHours: filteredEmployees.reduce((sum, e) => sum + (e.dominicalHours || 0), 0),
          totalFestivoHours: filteredEmployees.reduce((sum, e) => sum + (e.festivoHours || 0), 0),
          totalShifts: filteredEmployees.reduce((sum, e) => sum + (e.totalShifts || 0), 0)
        } : null;
        
        setReport({
          ...response.data,
          employees: filteredEmployees,
          locations: filteredLocations,
          global: filteredGlobal
        });
      } else {
        console.error('Error loading report:', response?.data?.message || response?.message || 'Unknown error');
      }
    } catch (error) {
      console.error('Exception loading report:', error);
      console.error('Error response:', error.response);
    } finally {
      setLoading(false);
    }
  }, [selectedMonth, selectedYear, reportType, selectedLocation, selectedEmployee]);

  const { execute: handleExportCsv, isLoading: isExportingCsv } = useAsyncOperation(
    async () => {
      await reportService.exportCsv(selectedMonth, selectedYear);
    }
  );

  const { execute: handleExportPdf, isLoading: isExportingPdf } = useAsyncOperation(
    async () => {
      // Validation for location and employee reports
      if (reportType === 'location' && !selectedLocation) {
        alert('Por favor seleccione una sede para exportar el reporte');
        return;
      }
      if (reportType === 'employee' && !selectedEmployee) {
        alert('Por favor seleccione un empleado para exportar el reporte');
        return;
      }
      
      const options = {
        reportType,
        locationId: reportType === 'location' ? selectedLocation : null,
        employeeId: reportType === 'employee' ? selectedEmployee : null
      };
      await reportService.exportPdf(selectedMonth, selectedYear, options);
    }
  );

  const { execute: handleExportExcel, isLoading: isExportingExcel } = useAsyncOperation(
    async () => {
      if (!report) {
        alert('Por favor genere el reporte primero');
        return;
      }
      
      exportReportToExcel(report, selectedMonth, selectedYear);
    }
  );

  // Rate configuration handlers
  const openConfigModal = () => {
    setRateConfig(getRates());
    setConfigModalOpen(true);
  };

  const handleRateChange = (field, value) => {
    setRateConfig(prev => ({
      ...prev,
      [field]: parseFloat(value) || 0
    }));
  };

  const handleSaveRates = () => {
    saveRates(rateConfig);
    setConfigModalOpen(false);
    alert('Configuración de tarifas guardada correctamente');
  };

  const handleResetRates = () => {
    const defaultRates = resetRates();
    setRateConfig(defaultRates);
    alert('Tarifas restauradas a valores predeterminados');
  };

  const toggleEmployeeDetails = (employeeId) => {
    setExpandedEmployee(expandedEmployee === employeeId ? null : employeeId);
  };

  const employeeColumns = [
    { key: 'fullName', header: 'Nombre Completo', render: (value) => <span className="font-medium">{value}</span> },
    { key: 'totalHours', header: 'Horas Totales', render: (value) => `${(value || 0).toFixed(2)}h` },
    { key: 'regularHours', header: 'Horas Regulares', render: (value) => `${(value || 0).toFixed(2)}h` },
    { key: 'overtimeHours', header: 'Horas Extras Totales', render: (value) => <span className={(value || 0) > 0 ? 'text-orange-600 font-semibold' : ''}>{(value || 0).toFixed(2)}h</span> },
    { key: 'diurnaExtraHours', header: 'Extras Diurnas', render: (value) => <span className={(value || 0) > 0 ? 'text-orange-500' : ''}>{(value || 0).toFixed(2)}h</span> },
    { key: 'nocturnaExtraHours', header: 'Extras Nocturnas', render: (value) => <span className={(value || 0) > 0 ? 'text-orange-500' : ''}>{(value || 0).toFixed(2)}h</span> },
    { key: 'dominicalHours', header: 'Horas Dominicales', render: (value) => <span className={(value || 0) > 0 ? 'text-purple-600' : ''}>{(value || 0).toFixed(2)}h</span> },
    { key: 'festivoHours', header: 'Horas Festivas', render: (value) => <span className={(value || 0) > 0 ? 'text-red-600' : ''}>{(value || 0).toFixed(2)}h</span> },
    { key: 'dailyAvgHours', header: 'Promedio Diario', render: (value) => `${(value || 0).toFixed(2)}h` },
    { key: 'weeklyTotalHours', header: 'Total Semanal', render: (value) => `${(value || 0).toFixed(2)}h` },
    { key: 'totalShifts', header: 'Total Turnos', render: (value) => <span className="font-semibold">{value || 0}</span> },
    {
      key: 'actions',
      header: 'Acciones',
      render: (value, row) => (
        <button
          onClick={() => toggleEmployeeDetails(row.employeeId)}
          className="text-blue-600 hover:text-blue-800 text-sm font-medium"
          aria-expanded={expandedEmployee === row.employeeId}
          aria-controls={`details-${row.employeeId}`}
        >
          {expandedEmployee === row.employeeId ? 'Ocultar Detalles' : 'Ver Detalles'}
        </button>
      )
    }
  ];

  const locationColumns = [
    { key: 'locationName', header: 'Sede', render: (value) => <span className="font-medium">{value}</span> },
    { key: 'totalEmployees', header: 'Total Empleados', render: (value) => <span className="font-semibold">{value}</span> },
    { key: 'totalHours', header: 'Horas Totales', render: (value) => `${(value || 0).toFixed(2)}h` },
    { key: 'totalRegularHours', header: 'Horas Regulares', render: (value) => `${(value || 0).toFixed(2)}h` },
    { key: 'totalOvertimeHours', header: 'Horas Extras Totales', render: (value) => <span className={(value || 0) > 0 ? 'text-orange-600 font-semibold' : ''}>{(value || 0).toFixed(2)}h</span> },
    { key: 'totalDiurnaExtraHours', header: 'Extras Diurnas', render: (value) => <span className={(value || 0) > 0 ? 'text-orange-500' : ''}>{(value || 0).toFixed(2)}h</span> },
    { key: 'totalNocturnaExtraHours', header: 'Extras Nocturnas', render: (value) => <span className={(value || 0) > 0 ? 'text-orange-500' : ''}>{(value || 0).toFixed(2)}h</span> },
    { key: 'totalDominicalHours', header: 'Horas Dominicales', render: (value) => <span className={(value || 0) > 0 ? 'text-purple-600' : ''}>{(value || 0).toFixed(2)}h</span> },
    { key: 'totalFestivoHours', header: 'Horas Festivas', render: (value) => <span className={(value || 0) > 0 ? 'text-red-600' : ''}>{(value || 0).toFixed(2)}h</span> },
    { key: 'totalShifts', header: 'Total Turnos', render: (value) => <span className="font-semibold">{value || 0}</span> }
  ];

  const getMonthName = (monthIndex) => {
    const date = new Date(0, monthIndex - 1);
    return date.toLocaleDateString('es-ES', { month: 'long' });
  };

  const getReportTitle = () => {
    const monthName = getMonthName(selectedMonth);
    switch (reportType) {
      case 'location': {
        const location = filterOptions.locations?.find(l => l.id === selectedLocation);
        return `Reporte por Sede: ${location ? location.name : 'Todas'} - ${monthName} ${selectedYear}`;
      }
      case 'employee': {
        const employee = filterOptions.employees?.find(e => e.id === selectedEmployee);
        return `Reporte Individual: ${employee ? employee.fullName : 'Todos'} - ${monthName} ${selectedYear}`;
      }
      default:
        return `Reporte General - ${monthName} ${selectedYear}`;
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Reportes de Horas</h1>
          <p className="text-gray-600 mt-1">Visualiza reportes mensuales y anuales de horas trabajadas</p>
        </div>
      </div>

      {/* Report Type Selection */}
      <div className="bg-white rounded-2xl shadow-xl p-6 border border-gray-100 mb-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Tipo de Reporte</h3>
        <div className="flex space-x-4 flex-wrap gap-2">
          <Button
            onClick={() => {
              setReportType('general');
              setSelectedLocation('');
              setSelectedEmployee('');
            }}
            variant={reportType === 'general' ? 'primary' : 'secondary'}
            aria-label="Ver reporte general"
          >
            📊 Reporte General
          </Button>
          <Button
            onClick={() => {
              setReportType('location');
              setSelectedEmployee('');
            }}
            variant={reportType === 'location' ? 'primary' : 'secondary'}
            aria-label="Ver reportes por sede"
          >
            🏢 Reporte por Sede
          </Button>
          <Button
            onClick={() => {
              setReportType('employee');
              setSelectedLocation('');
            }}
            variant={reportType === 'employee' ? 'primary' : 'secondary'}
            aria-label="Ver reportes por empleado"
          >
            👤 Reporte por Empleado
          </Button>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-2xl shadow-xl p-6 border border-gray-100">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Filtros de Reporte</h3>
        <div className="flex flex-wrap gap-4 items-end">
          <div>
            <label htmlFor="month" className="block text-sm font-medium text-gray-700 mb-2">
              Mes
            </label>
            <select
              id="month"
              value={selectedMonth}
              onChange={(e) => setSelectedMonth(parseInt(e.target.value))}
              className="px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
              aria-label="Seleccionar mes"
            >
              {Array.from({ length: 12 }, (_, i) => (
                <option key={i + 1} value={i + 1}>
                  {getMonthName(i + 1)}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label htmlFor="year" className="block text-sm font-medium text-gray-700 mb-2">
              Año
            </label>
            <select
              id="year"
              value={selectedYear}
              onChange={(e) => setSelectedYear(parseInt(e.target.value))}
              className="px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
              aria-label="Seleccionar año"
            >
              {filterOptions.years.length > 0 ? (
                filterOptions.years.map((year) => (
                  <option key={year.year} value={year.year}>
                    {year.label}
                  </option>
                ))
              ) : (
                Array.from({ length: 10 }, (_, i) => (
                  <option key={i} value={new Date().getFullYear() - 5 + i}>
                    {new Date().getFullYear() - 5 + i}
                  </option>
                ))
              )}
            </select>
          </div>
          
          {reportType === 'location' && (
            <div>
              <label htmlFor="location" className="block text-sm font-medium text-gray-700 mb-2">
                Sede
              </label>
              <select
                id="location"
                value={selectedLocation}
                onChange={(e) => setSelectedLocation(e.target.value)}
                className="px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
                aria-label="Seleccionar sede"
                disabled={filtersLoading}
              >
                <option value="">Todas las sedes</option>
                {filterOptions.locations.map((loc) => (
                  <option key={loc.id} value={loc.id}>
                    {loc.name}
                  </option>
                ))}
              </select>
            </div>
          )}
          
          {reportType === 'employee' && (
            <div>
              <label htmlFor="employee" className="block text-sm font-medium text-gray-700 mb-2">
                Empleado
              </label>
              <select
                id="employee"
                value={selectedEmployee}
                onChange={(e) => setSelectedEmployee(e.target.value)}
                className="px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
                aria-label="Seleccionar empleado"
                disabled={filtersLoading}
              >
                <option value="">Todos los empleados</option>
                {filterOptions.employees.map((emp) => (
                  <option key={emp.id} value={emp.id}>
                    {emp.fullName}
                  </option>
                ))}
              </select>
            </div>
          )}
          
          <div className="flex space-x-2">
            <Button
              onClick={() => handleExportCsv()}
              loading={isExportingCsv}
              variant="success"
              aria-label="Exportar a CSV"
            >
              📄 Descargar CSV
            </Button>
            <Button
              onClick={() => handleExportPdf()}
              loading={isExportingPdf}
              variant="primary"
              aria-label="Exportar a PDF"
            >
              📑 Descargar PDF
            </Button>
            <Button
              onClick={() => handleExportExcel()}
              loading={isExportingExcel}
              variant="success"
              aria-label="Exportar a Excel"
            >
              📊 Descargar Excel
            </Button>
            <Button
              onClick={openConfigModal}
              variant="secondary"
              aria-label="Configurar tarifas"
            >
              ⚙️ Tarifas
            </Button>
          </div>
        </div>
      </div>

      {/* Report Title */}
      {report && (
        <div className="bg-white rounded-2xl shadow-xl p-6 border border-gray-100">
          <h2 className="text-xl font-bold text-gray-900 text-center">{getReportTitle()}</h2>
        </div>
      )}

      {/* Global Summary */}
      {report && report.global && (
        <div className="bg-white rounded-2xl shadow-xl p-6 border border-gray-100">
          <h2 className="text-xl font-bold text-gray-900 mb-4">Resumen Global</h2>
          <div className="grid grid-cols-1 md:grid-cols-4 lg:grid-cols-5 gap-4">
            <div className="bg-blue-50 p-4 rounded-lg">
              <div className="text-2xl font-bold text-blue-600">{report.global.totalEmployees}</div>
              <div className="text-sm text-blue-800">Total Empleados</div>
            </div>
            <div className="bg-green-50 p-4 rounded-lg">
              <div className="text-2xl font-bold text-green-600">{report.global.totalHours?.toFixed(2)}h</div>
              <div className="text-sm text-green-800">Total Horas</div>
            </div>
            <div className="bg-orange-50 p-4 rounded-lg">
              <div className="text-2xl font-bold text-orange-600">{report.global.totalOvertimeHours?.toFixed(2)}h</div>
              <div className="text-sm text-orange-800">Total Horas Extras</div>
            </div>
            <div className="bg-purple-50 p-4 rounded-lg">
              <div className="text-2xl font-bold text-purple-600">{(report.global.totalRegularHours || 0).toFixed(2)}h</div>
              <div className="text-sm text-purple-800">Horas Regulares</div>
            </div>
            <div className="bg-yellow-50 p-4 rounded-lg">
              <div className="text-2xl font-bold text-yellow-600">{(report.global.totalDiurnaExtraHours || 0).toFixed(2)}h</div>
              <div className="text-sm text-yellow-800">Extras Diurnas</div>
            </div>
            <div className="bg-indigo-50 p-4 rounded-lg">
              <div className="text-2xl font-bold text-indigo-600">{(report.global.totalNocturnaExtraHours || 0).toFixed(2)}h</div>
              <div className="text-sm text-indigo-800">Extras Nocturnas</div>
            </div>
            <div className="bg-red-50 p-4 rounded-lg">
              <div className="text-2xl font-bold text-red-600">{(report.global.totalDominicalHours || 0).toFixed(2)}h</div>
              <div className="text-sm text-red-800">Horas Dominicales</div>
            </div>
            <div className="bg-teal-50 p-4 rounded-lg">
              <div className="text-2xl font-bold text-teal-600">{(report.global.totalFestivoHours || 0).toFixed(2)}h</div>
              <div className="text-sm text-teal-800">Horas Festivas</div>
            </div>
          </div>
        </div>
      )}

      {/* Report Details */}
      {reportType === 'general' && report && report.employees && (
        <>
          <DataTable
            title="Detalles por Empleado"
            icon="📊"
            columns={employeeColumns}
            data={report.employees}
            loading={loading}
            searchPlaceholder="Buscar empleados..."
            emptyMessage="No hay datos para mostrar"
          />

          {/* Expanded Employee Details */}
          {expandedEmployee && report && (
            <div className="bg-white rounded-2xl shadow-xl p-6 border border-gray-100" id={`details-${expandedEmployee}`}>
              <h3 className="text-lg font-bold text-gray-900 mb-4">
                Detalles de Horas Extras - {report.employees.find(e => e.id === expandedEmployee)?.fullName}
              </h3>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fecha</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Horas Extras</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Justificación</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {report.employees.find(e => e.id === expandedEmployee)?.overtimeDetails?.map((ot, index) => (
                      <tr key={index}>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{ot.date}</td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{ot.hours?.toFixed(2)}h</td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{ot.justification || 'N/A'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* Locations Summary for General Report */}
          {report && report.locations && (
            <DataTable
              title="Resumen por Sede"
              icon="🏢"
              columns={locationColumns}
              data={report.locations}
              loading={loading}
              searchPlaceholder="Buscar sedes..."
              emptyMessage="No hay datos para mostrar"
            />
          )}
        </>
      )}

      {reportType === 'location' && report && report.locations && (
        <>
          <DataTable
            title="Detalles por Sede"
            icon="🏢"
            columns={locationColumns}
            data={selectedLocation ? report.locations.filter(loc => loc.locationName === filterOptions.locations?.find(l => l.id === selectedLocation)?.name) : report.locations}
            loading={loading}
            searchPlaceholder="Buscar sedes..."
            emptyMessage="No hay datos para mostrar"
          />
          
          {/* Show employees for selected location */}
          {selectedLocation && report.locations.find(loc => loc.locationName === filterOptions.locations.find(l => l.id === selectedLocation)?.name)?.employeeReports && (
            <DataTable
              title="Empleados en esta Sede"
              icon="👥"
              columns={employeeColumns}
              data={report.locations.find(loc => loc.locationName === filterOptions.locations?.find(l => l.id === selectedLocation)?.name)?.employeeReports || []}
              loading={loading}
              searchPlaceholder="Buscar empleados..."
              emptyMessage="No hay empleados en esta sede"
            />
          )}
        </>
      )}

      {reportType === 'employee' && report && report.employees && (
        <>
          <DataTable
            title="Detalles por Empleado"
            icon="👤"
            columns={employeeColumns}
            data={selectedEmployee ? report.employees.filter(emp => emp.employeeId === selectedEmployee) : report.employees}
            loading={loading}
            searchPlaceholder="Buscar empleados..."
            emptyMessage="No hay datos para mostrar"
          />

          {/* Expanded Employee Details */}
          {expandedEmployee && report && (
            <div className="bg-white rounded-2xl shadow-xl p-6 border border-gray-100" id={`details-${expandedEmployee}`}>
              <h3 className="text-lg font-bold text-gray-900 mb-4">
                Detalles de Horas Extras - {report.employees.find(e => e.id === expandedEmployee)?.fullName}
              </h3>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fecha</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Horas Extras</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Justificación</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {report.employees.find(e => e.id === expandedEmployee)?.overtimeDetails?.map((ot, index) => (
                      <tr key={index}>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{ot.date}</td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{ot.hours?.toFixed(2)}h</td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{ot.justification || 'N/A'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </>
      )}

      {/* Rate Configuration Modal */}
      <Modal
        isOpen={configModalOpen}
        onClose={() => setConfigModalOpen(false)}
        title="⚙️ Configuración de Tarifas"
        size="lg"
      >
        <div className="space-y-6">
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-4">
            <p className="text-sm text-blue-800">
              <strong>Nota:</strong> Configure las tarifas base y multiplicadores para el cálculo de nómina. 
              Los valores se guardan en el navegador y se utilizarán al exportar a Excel.
            </p>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Tarifa Base por Hora (COP)
            </label>
            <input
              type="number"
              value={rateConfig.baseRate}
              onChange={(e) => handleRateChange('baseRate', e.target.value)}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
              placeholder="5000"
            />
            <p className="text-xs text-gray-500 mt-1">Tarifa base (100%) por hora</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Multiplicador Horas Regulares
              </label>
              <input
                type="number"
                step="0.01"
                value={rateConfig.regularHours}
                onChange={(e) => handleRateChange('regularHours', e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                placeholder="1.0"
              />
              <p className="text-xs text-gray-500 mt-1">Valor: {(rateConfig.regularHours * 100).toFixed(0)}%</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Multiplicador Horas Extra Diurnas
              </label>
              <input
                type="number"
                step="0.01"
                value={rateConfig.diurnaExtraHours}
                onChange={(e) => handleRateChange('diurnaExtraHours', e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                placeholder="1.35"
              />
              <p className="text-xs text-gray-500 mt-1">Valor: {(rateConfig.diurnaExtraHours * 100).toFixed(0)}%</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Multiplicador Horas Extra Nocturnas
              </label>
              <input
                type="number"
                step="0.01"
                value={rateConfig.nocturnaExtraHours}
                onChange={(e) => handleRateChange('nocturnaExtraHours', e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                placeholder="1.50"
              />
              <p className="text-xs text-gray-500 mt-1">Valor: {(rateConfig.nocturnaExtraHours * 100).toFixed(0)}%</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Multiplicador Horas Dominicales
              </label>
              <input
                type="number"
                step="0.01"
                value={rateConfig.dominicalHours}
                onChange={(e) => handleRateChange('dominicalHours', e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                placeholder="1.75"
              />
              <p className="text-xs text-gray-500 mt-1">Valor: {(rateConfig.dominicalHours * 100).toFixed(0)}%</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Multiplicador Horas Festivas
              </label>
              <input
                type="number"
                step="0.01"
                value={rateConfig.festivoHours}
                onChange={(e) => handleRateChange('festivoHours', e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                placeholder="1.75"
              />
              <p className="text-xs text-gray-500 mt-1">Valor: {(rateConfig.festivoHours * 100).toFixed(0)}%</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Multiplicador Horas Nocturnas
              </label>
              <input
                type="number"
                step="0.01"
                value={rateConfig.nocturnaHours}
                onChange={(e) => handleRateChange('nocturnaHours', e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
                placeholder="1.35"
              />
              <p className="text-xs text-gray-500 mt-1">Valor: {(rateConfig.nocturnaHours * 100).toFixed(0)}%</p>
            </div>
          </div>

          <div className="flex justify-between pt-4 border-t border-gray-200">
            <button
              type="button"
              onClick={handleResetRates}
              className="px-4 py-2 text-red-600 hover:text-red-800 font-medium"
            >
              Restaurar Valores Predeterminados
            </button>
            <div className="flex space-x-3">
              <button
                type="button"
                onClick={() => setConfigModalOpen(false)}
                className="px-6 py-3 text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors font-medium"
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={handleSaveRates}
                className="px-6 py-3 bg-gradient-to-r from-purple-500 to-purple-600 hover:from-purple-600 hover:to-purple-700 text-white rounded-lg transition-all font-medium shadow-lg"
              >
                Guardar Configuración
              </button>
            </div>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default Reports;
