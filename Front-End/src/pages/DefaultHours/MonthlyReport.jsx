import { useState, useEffect } from 'react';
import Button from '../../components/Button';
import hourTemplateAssignmentService from '../../services/hourTemplateAssignmentService';
import hourTemplateService from '../../services/hourTemplateService';
import employeeService from '../../services/employeeService';
import locationService from '../../services/locationService';
import jsPDF from 'jspdf';

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

  const generatePDF = async () => {
  if (!reportData) return;

  setLoading(true);
  const monthName = months.find(m => m.value === reportData.month)?.label;
  const generatedDate = new Date(reportData.generatedAt).toLocaleDateString('es-CO', {
    day: 'numeric', month: 'long', year: 'numeric'
  });
  
  try {
    const container = document.createElement('div');
    container.style.position = 'absolute';
    container.style.left = '-9999px';
    container.style.width = '780px';
    container.style.padding = '40px 50px';
    container.style.background = 'white';
    container.style.fontFamily = 'Georgia, serif';

    container.innerHTML = `
      <!-- ENCABEZADO INSTITUCIONAL -->
      <div style="display: flex; align-items: center; justify-content: space-between; border-bottom: 3px solid #991b1b; padding-bottom: 16px; margin-bottom: 24px;">
        <div>
          <div style="font-size: 22px; font-weight: bold; color: #991b1b; letter-spacing: 1px;">MICROFARMA</div>
          <div style="font-size: 11px; color: #555; margin-top: 2px;">Sistema de Gestión de Recursos Humanos</div>
        </div>
        <div style="text-align: right; font-size: 10px; color: #666; line-height: 1.6;">
          <div><strong>Documento:</strong> RH-HORAS-${reportData.year}-${String(reportData.month).padStart(2,'0')}</div>
          <div><strong>Fecha de emisión:</strong> ${generatedDate}</div>
          <div><strong>Período:</strong> ${monthName} ${reportData.year}</div>
        </div>
      </div>

      <!-- TÍTULO OFICIAL -->
      <div style="text-align: center; margin-bottom: 28px;">
        <div style="font-size: 15px; font-weight: bold; color: #111; letter-spacing: 0.5px; text-transform: uppercase;">
          Reporte Mensual de Horas Trabajadas
        </div>
        <div style="font-size: 12px; color: #555; margin-top: 6px;">
          Período correspondiente al mes de <strong>${monthName}</strong> del año <strong>${reportData.year}</strong>
        </div>
      </div>

      <!-- CUERPO DE REDACCIÓN FORMAL -->
      <div style="font-size: 11px; color: #333; line-height: 1.85; text-align: justify; margin-bottom: 28px; padding: 18px 20px; background: #fafafa; border-left: 4px solid #dc2626; border-radius: 2px;">
        El presente documento certifica el registro oficial de horas laboradas por el personal vinculado a 
        <strong>Microfarma</strong> durante el período comprendido en el mes de <strong>${monthName} de ${reportData.year}</strong>. 
        La información aquí consignada ha sido consolidada a partir de los registros de asignación de turnos y 
        plantillas horarias del sistema de gestión, discriminando las horas según su naturaleza: ordinarias, 
        horas extra diurnas, horas extra nocturnas, horas extra en festivos y dominicales, conforme a la 
        normatividad laboral vigente en la República de Colombia (Código Sustantivo del Trabajo).
        <br/><br/>
        El reporte comprende un total de <strong>${reportData.employees.length} empleado(s)</strong>, 
        con una sumatoria global de <strong>${reportData.grandTotals.totalHours.toFixed(2)} horas</strong> 
        registradas en el período. Este documento debe ser revisado, validado y firmado por el responsable 
        del área de Talento Humano y el representante legal o su delegado, como constancia de conformidad 
        con la información presentada.
      </div>

      <!-- TABLA DE DATOS -->
      <table style="width: 100%; border-collapse: collapse; margin-bottom: 8px; font-size: 9.5px;">
        <thead>
          <tr style="background: #991b1b; color: white;">
            <th style="padding: 9px 10px; text-align: left; font-weight: 600; letter-spacing: 0.3px;">EMPLEADO</th>
            <th style="padding: 9px 10px; text-align: left; font-weight: 600;">UBICACIÓN</th>
            <th style="padding: 9px 8px; text-align: center; font-weight: 600;">ORDINARIAS</th>
            <th style="padding: 9px 8px; text-align: center; font-weight: 600;">EXT. DIURNAS</th>
            <th style="padding: 9px 8px; text-align: center; font-weight: 600;">EXT. NOCT.</th>
            <th style="padding: 9px 8px; text-align: center; font-weight: 600;">EXTRA F/DOM</th>
            <th style="padding: 9px 8px; text-align: center; font-weight: 600;">DOMINICALES</th>
            <th style="padding: 9px 8px; text-align: center; font-weight: 600;">TOTAL HRS</th>
          </tr>
        </thead>
        <tbody>
          ${reportData.employees.map((emp, i) => `
            <tr style="background: ${i % 2 === 0 ? '#ffffff' : '#fef2f2'};">
              <td style="padding: 7px 10px; border-bottom: 1px solid #e5e7eb; color: #111; font-weight: 500;">${emp.employeeName}</td>
              <td style="padding: 7px 10px; border-bottom: 1px solid #e5e7eb; color: #444;">${emp.locationName}</td>
              <td style="padding: 7px 8px; border-bottom: 1px solid #e5e7eb; text-align: center; color: #333;">${emp.ordinarias > 0 ? emp.ordinarias.toFixed(2) : '—'}</td>
              <td style="padding: 7px 8px; border-bottom: 1px solid #e5e7eb; text-align: center; color: #333;">${emp.diurnas > 0 ? emp.diurnas.toFixed(2) : '—'}</td>
              <td style="padding: 7px 8px; border-bottom: 1px solid #e5e7eb; text-align: center; color: #333;">${emp.nocturnas > 0 ? emp.nocturnas.toFixed(2) : '—'}</td>
              <td style="padding: 7px 8px; border-bottom: 1px solid #e5e7eb; text-align: center; color: #333;">${emp.extraFDom > 0 ? emp.extraFDom.toFixed(2) : '—'}</td>
              <td style="padding: 7px 8px; border-bottom: 1px solid #e5e7eb; text-align: center; color: #333;">${emp.dominicales > 0 ? emp.dominicales.toFixed(2) : '—'}</td>
              <td style="padding: 7px 8px; border-bottom: 1px solid #e5e7eb; text-align: center; font-weight: bold; color: #111;">${emp.totalHours.toFixed(2)}</td>
            </tr>
          `).join('')}
          <!-- FILA TOTALES -->
          <tr style="background: #991b1b;">
            <td colspan="2" style="padding: 9px 10px; color: white; font-weight: bold; font-size: 10px; letter-spacing: 0.3px;">TOTALES GENERALES</td>
            <td style="padding: 9px 8px; text-align: center; color: white; font-weight: bold;">${reportData.grandTotals.ordinarias.toFixed(2)}</td>
            <td style="padding: 9px 8px; text-align: center; color: white; font-weight: bold;">${reportData.grandTotals.diurnas.toFixed(2)}</td>
            <td style="padding: 9px 8px; text-align: center; color: white; font-weight: bold;">${reportData.grandTotals.nocturnas.toFixed(2)}</td>
            <td style="padding: 9px 8px; text-align: center; color: white; font-weight: bold;">${reportData.grandTotals.extraFDom.toFixed(2)}</td>
            <td style="padding: 9px 8px; text-align: center; color: white; font-weight: bold;">${reportData.grandTotals.dominicales.toFixed(2)}</td>
            <td style="padding: 9px 8px; text-align: center; color: white; font-weight: bold; font-size: 11px;">${reportData.grandTotals.totalHours.toFixed(2)}</td>
          </tr>
        </tbody>
      </table>

      <div style="font-size: 9px; color: #888; text-align: right; margin-bottom: 40px;">
        * Valores expresados en horas decimales. Período: ${monthName} ${reportData.year}.
      </div>

      <!-- DECLARACIÓN DE CONFORMIDAD -->
      <div style="font-size: 10.5px; color: #333; line-height: 1.8; text-align: justify; margin-bottom: 50px;">
        En constancia de lo anterior, las partes abajo firmantes declaran haber revisado el contenido del 
        presente reporte y certifican que la información registrada corresponde fielmente a los datos 
        del sistema de gestión de horarios de <strong>Microfarma</strong> para el período indicado. 
        Este documento tiene validez como soporte para liquidación de nómina y archivo de gestión del 
        talento humano.
      </div>

      <!-- CAMPOS DE FIRMA -->
      <div style="display: flex; justify-content: space-between; margin-top: 10px; gap: 30px;">
        
        <div style="flex: 1; text-align: center;">
          <div style="border-top: 1.5px solid #333; padding-top: 10px; margin-top: 60px;">
            <div style="font-size: 10px; font-weight: bold; color: #111; margin-top: 4px;">RESPONSABLE DE TALENTO HUMANO</div>
            <div style="font-size: 9px; color: #666; margin-top: 6px;">Nombre: _______________________________</div>
            <div style="font-size: 9px; color: #666; margin-top: 3px;">C.C. No.: ______________________________</div>
            <div style="font-size: 9px; color: #666; margin-top: 3px;">Cargo: _________________________________</div>
            <div style="font-size: 9px; color: #666; margin-top: 3px;">Fecha: _________________________________</div>
          </div>
        </div>

        <div style="flex: 1; text-align: center;">
          <div style="border-top: 1.5px solid #333; padding-top: 10px; margin-top: 60px;">
            <div style="font-size: 10px; font-weight: bold; color: #111; margin-top: 4px;">EMPLEADO</div>
            <div style="font-size: 9px; color: #666; margin-top: 6px;">Nombre: _______________________________</div>
            <div style="font-size: 9px; color: #666; margin-top: 3px;">C.C. No.: ______________________________</div>
            <div style="font-size: 9px; color: #666; margin-top: 3px;">Cargo: _________________________________</div>
            <div style="font-size: 9px; color: #666; margin-top: 3px;">Fecha: _________________________________</div>
          </div>
        </div>

        <div style="flex: 1; text-align: center;">
          <div style="border-top: 1.5px solid #333; padding-top: 10px; margin-top: 60px;">
            <div style="font-size: 10px; font-weight: bold; color: #111; margin-top: 4px;">REVISOR / AUDITOR</div>
            <div style="font-size: 9px; color: #666; margin-top: 6px;">Nombre: _______________________________</div>
            <div style="font-size: 9px; color: #666; margin-top: 3px;">C.C. No.: ______________________________</div>
            <div style="font-size: 9px; color: #666; margin-top: 3px;">Cargo: _________________________________</div>
            <div style="font-size: 9px; color: #666; margin-top: 3px;">Fecha: _________________________________</div>
          </div>
        </div>
      </div>

      <!-- PIE DE PÁGINA -->
      <div style="margin-top: 40px; border-top: 1px solid #ddd; padding-top: 12px; display: flex; justify-content: space-between; font-size: 8.5px; color: #aaa;">
        <span>Microfarma — Sistema de Gestión de Horarios</span>
        <span>Documento generado el ${generatedDate} | Confidencial — Uso interno</span>
        <span>Pág. 1 de 1</span>
      </div>
    `;

    document.body.appendChild(container);

    const html2canvas = (await import('html2canvas')).default;
    const canvas = await html2canvas(container, { scale: 2, useCORS: true });

    document.body.removeChild(container);

    const pdf = new jsPDF('p', 'mm', 'letter');
    const imgData = canvas.toDataURL('image/png');
    const imgWidth = 196;
    const imgHeight = (canvas.height * imgWidth) / canvas.width;

    pdf.addImage(imgData, 'PNG', 10, 10, imgWidth, imgHeight);
    pdf.save(`reporte_horas_${monthName}_${reportData.year}.pdf`);

  } catch (error) {
    console.error('Error generating PDF:', error);
    alert('Error al generar el PDF');
  } finally {
    setLoading(false);
  }
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
