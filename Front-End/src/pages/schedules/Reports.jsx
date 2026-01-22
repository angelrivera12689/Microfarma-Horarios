import { useState, useEffect } from 'react';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import Button from '../../components/Button';
import reportService from '../../services/reportService';
import useAsyncOperation from '../../hooks/useAsyncOperation';

const Reports = () => {
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(false);
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth() + 1);
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  const [expandedEmployee, setExpandedEmployee] = useState(null);

  useEffect(() => {
    loadReport();
  }, [selectedMonth, selectedYear]);

  const loadReport = async () => {
    setLoading(true);
    try {
      const response = await reportService.getMonthlyReport(selectedMonth, selectedYear);
      if (response.data) {
        setReport(response.data);
      } else {
        alert('Error al cargar el reporte: ' + (response.message || 'Error desconocido'));
      }
    } catch (error) {
      console.error('Error loading report:', error);
      alert('Error de conexión al cargar el reporte');
    } finally {
      setLoading(false);
    }
  };

  const { execute: handleExportCsv, isLoading: isExportingCsv } = useAsyncOperation(
    async (month, year) => {
      await reportService.exportCsv(month, year);
    }
  );

  const { execute: handleExportPdf, isLoading: isExportingPdf } = useAsyncOperation(
    async (month, year) => {
      await reportService.exportPdf(month, year);
    }
  );

  const toggleEmployeeDetails = (employeeId) => {
    setExpandedEmployee(expandedEmployee === employeeId ? null : employeeId);
  };

  const employeeColumns = [
    { key: 'id', header: 'ID', render: (value) => <span className="font-medium">{value}</span> },
    { key: 'fullName', header: 'Nombre Completo', render: (value) => <span className="font-medium">{value}</span> },
    { key: 'dailyAvgHours', header: 'Horas Diarias Promedio', render: (value) => `${value.toFixed(2)}h` },
    { key: 'weeklyTotalHours', header: 'Horas Semanales Totales', render: (value) => `${value.toFixed(2)}h` },
    { key: 'totalHours', header: 'Horas Totales', render: (value) => `${value.toFixed(2)}h` },
    { key: 'overtimeHours', header: 'Horas Extras', render: (value) => `${value.toFixed(2)}h` },
    { key: 'regularHours', header: 'Horas Regulares', render: (value) => `${(value || 0).toFixed(2)}h` },
    { key: 'diurnaExtraHours', header: 'Extras Diurnas', render: (value) => `${(value || 0).toFixed(2)}h` },
    { key: 'nocturnaExtraHours', header: 'Extras Nocturnas', render: (value) => `${(value || 0).toFixed(2)}h` },
    { key: 'dominicalHours', header: 'Horas Dominicales', render: (value) => `${(value || 0).toFixed(2)}h` },
    { key: 'festivoHours', header: 'Horas Festivas', render: (value) => `${(value || 0).toFixed(2)}h` },
    {
      key: 'actions',
      header: 'Acciones',
      render: (value, row) => (
        <button
          onClick={() => toggleEmployeeDetails(row.id)}
          className="text-blue-600 hover:text-blue-800 text-sm font-medium"
          aria-expanded={expandedEmployee === row.id}
          aria-controls={`details-${row.id}`}
        >
          {expandedEmployee === row.id ? 'Ocultar Detalles' : 'Ver Detalles'}
        </button>
      )
    }
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Reportes de Horas</h1>
          <p className="text-gray-600 mt-1">Visualiza reportes mensuales y anuales de horas trabajadas</p>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-2xl shadow-xl p-6 border border-gray-100">
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
                  {new Date(0, i).toLocaleDateString('es-ES', { month: 'long' })}
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
              {Array.from({ length: 10 }, (_, i) => (
                <option key={i} value={new Date().getFullYear() - 5 + i}>
                  {new Date().getFullYear() - 5 + i}
                </option>
              ))}
            </select>
          </div>
          <div className="flex space-x-2">
            <Button
              onClick={() => handleExportCsv(selectedMonth, selectedYear)}
              loading={isExportingCsv}
              variant="success"
              aria-label="Exportar a CSV"
            >
              Descargar CSV
            </Button>
            <Button
              onClick={() => handleExportPdf(selectedMonth, selectedYear)}
              loading={isExportingPdf}
              variant="primary"
              aria-label="Exportar a PDF"
            >
              Descargar PDF
            </Button>
          </div>
        </div>
      </div>

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
              <div className="text-2xl font-bold text-green-600">{report.global.totalHours.toFixed(2)}h</div>
              <div className="text-sm text-green-800">Total Horas</div>
            </div>
            <div className="bg-orange-50 p-4 rounded-lg">
              <div className="text-2xl font-bold text-orange-600">{report.global.totalOvertimeHours.toFixed(2)}h</div>
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

      {/* Employee Details */}
      <DataTable
        title="Detalles por Empleado"
        icon="📊"
        columns={employeeColumns}
        data={report ? report.employees : []}
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
                {report.employees.find(e => e.id === expandedEmployee)?.overtimeDetails.map((ot, index) => (
                  <tr key={index}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{ot.date}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{ot.hours.toFixed(2)}h</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{ot.justification || 'N/A'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default Reports;