import { useState, useEffect } from 'react';
import DataTable from '../../components/DataTable';
import reportService from '../../services/reportService';
import { exportReportToExcel } from '../../services/excelExportService';

const DeliveryReports = () => {
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(false);
  const [filtersLoading, setFiltersLoading] = useState(true);
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth() + 1);
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  const [selectedLocation, setSelectedLocation] = useState('');

  const [filterOptions, setFilterOptions] = useState({
    locations: [],
    employees: []
  });

  useEffect(() => {
    loadFilters();
  }, []);

  useEffect(() => {
    loadReport();
  }, [selectedMonth, selectedYear, selectedLocation]);

  const loadFilters = async () => {
    setFiltersLoading(true);
    try {
      const response = await reportService.getAvailableFilters();
      if (response && (response.success === true || response.status === true) && response.data) {
        // Filtrar solo empleados con posición Domiciliario (búsqueda flexible)
        const deliveryEmployees = (response.data.employees || []).filter(emp => 
          emp.position?.name && emp.position.name.toLowerCase().includes('domicili')
        );
        setFilterOptions({
          locations: response.data.locations || [],
          employees: deliveryEmployees
        });
      }
    } catch (error) {
      console.error('Error loading filters:', error);
    } finally {
      setFiltersLoading(false);
    }
  };

  const loadReport = async () => {
    setLoading(true);
    try {
      const response = selectedLocation 
        ? await reportService.getMonthlyReportByLocation(selectedMonth, selectedYear, selectedLocation)
        : await reportService.getMonthlyReport(selectedMonth, selectedYear);
      
      if (response && (response.success === true || response.status === true) && response.data) {
        // Filtrar solo empleados Domiciliarios en el reporte
        const deliveryEmployees = (response.data.employees || []).filter(emp => 
          emp.position?.name === 'Domiciliario'
        );
        const deliveryLocations = (response.data.locations || []).filter(loc => 
          loc.shifts?.some(shift => shift.employee?.position?.name === 'Domiciliario')
        );
        
        setReport({
          ...response.data,
          employees: deliveryEmployees,
          locations: deliveryLocations,
          global: response.data.global // Mantener totales generales
        });
      }
    } catch (error) {
      console.error('Error loading report:', error);
    } finally {
      setLoading(false);
    }
  };

  const exportToExcel = () => {
    if (report) {
      exportReportToExcel(report, `reporte_domiciliarios_${selectedMonth}_${selectedYear}`);
    }
  };

  const formatHours = (hours) => {
    const h = Math.floor(hours);
    const m = Math.round((hours - h) * 60);
    return `${h}h ${m}m`;
  };

  const columns = [
    { 
      key: 'employee', 
      header: 'Domiciliario', 
      render: (value) => value ? `${value.firstName} ${value.lastName}` : '-' 
    },
    { 
      key: 'totalHours', 
      header: 'Horas Totales', 
      render: (value) => formatHours(value || 0) 
    },
    { 
      key: 'dayShifts', 
      header: 'Turnos Día', 
      render: (value) => value || 0 
    },
    { 
      key: 'nightShifts', 
      header: 'Turnos Noche', 
      render: (value) => value || 0 
    },
    { 
      key: 'overtimeHours', 
      header: 'Horas Extra', 
      render: (value) => formatHours(value || 0) 
    }
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">🛵 Reportes Domiciliarios</h1>
          <p className="text-gray-600 mt-1">Reporte de horas trabajadas por domiciliarios</p>
        </div>
        <button
          onClick={exportToExcel}
          disabled={!report}
          className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:bg-gray-300 disabled:cursor-not-allowed"
        >
          📊 Descargar Excel
        </button>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-2xl shadow-xl p-6 border border-gray-100">
        <div className="flex flex-wrap gap-4 items-center">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Mes</label>
            <select
              value={selectedMonth}
              onChange={(e) => setSelectedMonth(parseInt(e.target.value))}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-600"
            >
              {Array.from({ length: 12 }, (_, i) => (
                <option key={i + 1} value={i + 1}>
                  {new Date(2000, i).toLocaleDateString('es-ES', { month: 'long' })}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Año</label>
            <select
              value={selectedYear}
              onChange={(e) => setSelectedYear(parseInt(e.target.value))}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-600"
            >
              {[2024, 2025, 2026, 2027].map(year => (
                <option key={year} value={year}>{year}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Ubicación</label>
            {filtersLoading ? (
              <div className="px-4 py-2 border border-gray-300 rounded-lg bg-gray-50 text-gray-500">
                Cargando ubicaciones...
              </div>
            ) : (
              <select
                value={selectedLocation}
                onChange={(e) => setSelectedLocation(e.target.value)}
                className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-600"
              >
                <option value="">Todas las ubicaciones</option>
                {filterOptions.locations.map(location => (
                  <option key={location.id} value={location.id}>
                    {location.name}
                  </option>
                ))}
              </select>
            )}
          </div>

          <div className="ml-auto text-right">
            <div className="text-sm text-gray-500">
              Mostrando solo domiciliarios
            </div>
          </div>
        </div>
      </div>

      {/* Report Data */}
      {loading ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-green-600"></div>
        </div>
      ) : report ? (
        <div className="space-y-6">
          {/* Summary Cards */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="bg-gradient-to-br from-green-500 to-green-600 rounded-2xl p-6 text-white shadow-lg">
              <div className="text-sm opacity-80">Total Horas</div>
              <div className="text-3xl font-bold">{formatHours(report.global?.totalHours || 0)}</div>
            </div>
            <div className="bg-gradient-to-br from-blue-500 to-blue-600 rounded-2xl p-6 text-white shadow-lg">
              <div className="text-sm opacity-80">Domiciliarios</div>
              <div className="text-3xl font-bold">{report.employees?.length || 0}</div>
            </div>
            <div className="bg-gradient-to-br from-purple-500 to-purple-600 rounded-2xl p-6 text-white shadow-lg">
              <div className="text-sm opacity-80">Horas Extra</div>
              <div className="text-3xl font-bold">{formatHours(report.global?.overtimeHours || 0)}</div>
            </div>
            <div className="bg-gradient-to-br from-orange-500 to-orange-600 rounded-2xl p-6 text-white shadow-lg">
              <div className="text-sm opacity-80">Turnos Noche</div>
              <div className="text-3xl font-bold">{report.global?.nightShifts || 0}</div>
            </div>
          </div>

          {/* Employee Table */}
          <div className="bg-white rounded-2xl shadow-xl p-6 border border-gray-100">
            <h2 className="text-xl font-bold text-gray-900 mb-4">Detalle por Domiciliario</h2>
            <DataTable
              title="domiciliarios"
              columns={columns}
              data={report.employees || []}
              loading={false}
              emptyMessage="No hay datos de domiciliarios para el período seleccionado"
            />
          </div>
        </div>
      ) : (
        <div className="bg-white rounded-2xl shadow-xl p-12 border border-gray-100 text-center">
          <div className="text-gray-500">No hay datos disponibles</div>
        </div>
      )}
    </div>
  );
};

export default DeliveryReports;
