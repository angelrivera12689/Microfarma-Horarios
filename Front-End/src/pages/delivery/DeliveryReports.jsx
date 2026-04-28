import { useState, useEffect } from 'react';
import DataTable from '../../components/DataTable';
import reportService from '../../services/reportService';
import { exportReportToExcel } from '../../services/excelExportService';
import useAsyncOperation from '../../hooks/useAsyncOperation';

const DeliveryReports = () => {
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(false);
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth() + 1);
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  const [selectedZone, setSelectedZone] = useState('');
  const [searchName, setSearchName] = useState('');

  useEffect(() => {
    loadReport();
  }, [selectedMonth, selectedYear, selectedZone, searchName]);

  const isDeliveryLocation = (locationName) => {
    if (!locationName) return false;
    const name = locationName.toLowerCase();
    return name.includes('zona norte') || 
           name.includes('oriente') || 
           name.includes('sur');
  };

  const loadReport = async () => {
    setLoading(true);
    try {
      // Use the dedicated delivery report endpoint - filtering by zone is done in frontend
      const response = await reportService.getMonthlyDeliveryReport(selectedMonth, selectedYear);
       
      if (response && (response.success === true || response.status === true) && response.data) {
        // Filtrar solo empleados Domiciliarios en el reporte
        let deliveryEmployees = (response.data.employees || []).filter(emp => 
          emp.positionName && emp.positionName.toLowerCase().includes('domicili')
        );
        
        // Filtrar por nombre del domiciliario
        if (searchName.trim()) {
          const searchLower = searchName.toLowerCase().trim();
          deliveryEmployees = deliveryEmployees.filter(emp => {
            const fullName = (emp.fullName || '').toLowerCase();
            return fullName.includes(searchLower);
          });
        }
        
        // Filtrar solo las ubicaciones de domiciliarios (Las Américas 3 Norte, Oriente, Sur)
        let deliveryLocations = (response.data.locations || []).filter(loc => 
          isDeliveryLocation(loc.locationName)
        ).map(loc => ({
          ...loc,
          // Filtrar empleados en cada ubicación para solo mostrar domiciliarios
          employeeReports: (loc.employeeReports || []).filter(emp => 
            emp.positionName && emp.positionName.toLowerCase().includes('domicili')
          )
        })).filter(loc => loc.employeeReports && loc.employeeReports.length > 0);
        
        // Aplicar filtro por zona (Norte, Oriente, Sur)
        // Filter employees based on selected zone
        const zoneMap = {
          'norte': ['norte', 'zona norte'],
          'oriente': ['oriente', 'zona oriente'],
          'sur': ['sur', 'zona sur']
        };
        
        if (selectedZone) {
          const zoneTerms = zoneMap[selectedZone.toLowerCase()] || [selectedZone.toLowerCase()];
          
          // First filter locations by zone
          deliveryLocations = deliveryLocations.filter(loc => {
            const locName = (loc.locationName || '').toLowerCase();
            return zoneTerms.some(term => locName.includes(term));
          });
          
          // Get the zone terms for matching against employee locations
          const activeZoneTerms = zoneTerms.map(term => term.toLowerCase());
          
          // Filter employees whose locations match the selected zone
          deliveryEmployees = deliveryEmployees.filter(emp => {
            const empLoc = (emp.locations || []).map(l => l.toLowerCase());
            // Check if any employee location matches the zone terms
            return empLoc.some(loc => 
              activeZoneTerms.some(term => loc.includes(term))
            );
          });
        }
        
        // Recalculate global totals based on filtered domiciliarios
        const deliveryGlobal = deliveryEmployees.length > 0 ? {
          totalEmployees: deliveryEmployees.length,
          totalHours: deliveryEmployees.reduce((sum, e) => sum + (e.totalHours || 0), 0),
          nightShifts: deliveryEmployees.reduce((sum, e) => sum + (e.nightShifts || 0), 0),
          overtimeHours: deliveryEmployees.reduce((sum, e) => sum + (e.overtimeHours || 0), 0)
        } : null;
        
        setReport({
          ...response.data,
          employees: deliveryEmployees,
          locations: deliveryLocations,
          global: deliveryGlobal
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
      // Delivery reports use frontend filtering, so we export general report
      // In the future, backend could support zone filtering
      exportReportToExcel(report, selectedMonth, selectedYear, {});
    }
  };
  
  const { execute: handleExportPdf, isLoading: isExportingPdf } = useAsyncOperation(
    async () => {
      if (!report) return;
      // Use 'delivery' reportType for PDF export
      await reportService.exportPdf(selectedMonth, selectedYear, { reportType: 'delivery' });
    }
  );

  const formatHours = (hours) => {
    const h = Math.floor(hours);
    const m = Math.round((hours - h) * 60);
    return `${h}h ${m}m`;
  };

  const columns = [
    { 
      key: 'fullName', 
      header: 'Nombre Completo', 
      render: (value, row) => {
        // Handle both object format (from backend) and string format
        if (typeof value === 'string') return value;
        if (row.employee) return `${row.employee.firstName} ${row.employee.lastName}`;
        return '-';
      }
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
          onClick={handleExportPdf}
          disabled={!report || isExportingPdf}
          className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:bg-gray-300 disabled:cursor-not-allowed mr-2"
        >
          {isExportingPdf ? '📄 Generando...' : '📄 Descargar PDF'}
        </button>
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
            <label className="block text-sm font-medium text-gray-700 mb-1">Zona</label>
            <select
              value={selectedZone}
              onChange={(e) => setSelectedZone(e.target.value)}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-600"
            >
              <option value="">Todas las zonas</option>
              <option value="norte">Norte</option>
              <option value="oriente">Oriente</option>
              <option value="sur">Sur</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Buscar por nombre</label>
            <input
              type="text"
              value={searchName}
              onChange={(e) => setSearchName(e.target.value)}
              placeholder="Nombre del domiciliario..."
              className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-600 w-48"
            />
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
