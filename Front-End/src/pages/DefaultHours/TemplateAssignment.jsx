import { useState, useEffect } from 'react';
import Modal from '../../components/Modal';
import Button from '../../components/Button';
import hourTemplateAssignmentService from '../../services/hourTemplateAssignmentService';
import hourTemplateService from '../../services/hourTemplateService';
import employeeService from '../../services/employeeService';
import locationService from '../../services/locationService';

const TemplateAssignment = () => {
  const [assignments, setAssignments] = useState([]);
  const [templates, setTemplates] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [locations, setLocations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingAssignment, setEditingAssignment] = useState(null);
  
  // Filters
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth() + 1);
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  const [filterEmployee, setFilterEmployee] = useState('');
  const [filterLocation, setFilterLocation] = useState('');
  
  const [formData, setFormData] = useState({
    employeeId: '',
    locationId: '',
    templateId: '',
    month: new Date().getMonth() + 1,
    year: new Date().getFullYear()
  });

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
    loadData();
  }, []);

  useEffect(() => {
    filterAssignments();
  }, [selectedMonth, selectedYear, filterEmployee, filterLocation, assignments]);

  const loadData = async () => {
    try {
      // Load all data in parallel
      const [assignmentsRes, templatesRes, employeesRes, locationsRes] = await Promise.all([
        hourTemplateAssignmentService.getAllAssignments(),
        hourTemplateService.getAllTemplates(),
        employeeService.getActiveEmployees(),
        locationService.getAllLocations()
      ]);

      if (assignmentsRes.data) setAssignments(assignmentsRes.data);
      if (templatesRes.data) setTemplates(templatesRes.data);
      if (employeesRes.data) setEmployees(employeesRes.data);
      if (locationsRes.data) setLocations(locationsRes.data);
    } catch (error) {
      console.error('Error loading data:', error);
      // Load from local storage as fallback
      const [assignmentsRes, templatesRes] = await Promise.all([
        hourTemplateAssignmentService.getAllAssignments(),
        hourTemplateService.getAllTemplates()
      ]);
      if (assignmentsRes.data) setAssignments(assignmentsRes.data);
      if (templatesRes.data) setTemplates(templatesRes.data);
    } finally {
      setLoading(false);
    }
  };

  const filterAssignments = () => {
    let filtered = assignments;
    
    filtered = filtered.filter(a => 
      a.month === selectedMonth && a.year === selectedYear
    );
    
    if (filterEmployee) {
      filtered = filtered.filter(a => a.employeeId === filterEmployee);
    }
    
    if (filterLocation) {
      filtered = filtered.filter(a => a.locationId === filterLocation);
    }
    
    return filtered;
  };

  const getEmployeeName = (employeeId) => {
    const employee = employees.find(e => e.id === employeeId);
    return employee ? `${employee.firstName} ${employee.lastName}` : 'Desconocido';
  };

  const getLocationName = (locationId) => {
    const location = locations.find(l => l.id === locationId);
    return location ? location.name : 'Desconocida';
  };

  const getTemplateName = (templateId) => {
    const template = templates.find(t => t.id === templateId);
    return template ? template.name : 'Desconocida';
  };

  const getTemplateDetails = (templateId) => {
    return templates.find(t => t.id === templateId);
  };

  const handleAdd = () => {
    setEditingAssignment(null);
    setFormData({
      employeeId: '',
      locationId: '',
      templateId: '',
      month: selectedMonth,
      year: selectedYear
    });
    setModalOpen(true);
  };

  const handleEdit = (assignment) => {
    setEditingAssignment(assignment);
    setFormData({
      employeeId: assignment.employeeId,
      locationId: assignment.locationId,
      templateId: assignment.templateId,
      month: assignment.month,
      year: assignment.year
    });
    setModalOpen(true);
  };

  const handleDelete = async (assignment) => {
    if (window.confirm(`¿Estás seguro de que quieres eliminar esta asignación?`)) {
      try {
        await hourTemplateAssignmentService.deleteAssignment(assignment.id);
        setAssignments(prev => prev.filter(a => a.id !== assignment.id));
      } catch (error) {
        console.error('Error deleting assignment:', error);
        alert('Error al eliminar la asignación');
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      if (editingAssignment) {
        const updated = await hourTemplateAssignmentService.updateAssignment(
          editingAssignment.id,
          formData
        );
        setAssignments(prev => prev.map(a => 
          a.id === editingAssignment.id ? updated.data : a
        ));
      } else {
        const created = await hourTemplateAssignmentService.createAssignment(formData);
        setAssignments(prev => [...prev, created.data]);
      }
      setModalOpen(false);
    } catch (error) {
      console.error('Error saving assignment:', error);
      alert(error.message || 'Error al guardar la asignación');
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-500"></div>
      </div>
    );
  }

  const filteredAssignments = filterAssignments();

  return (
    <div className="p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Asignación de Plantillas</h1>
          <p className="text-gray-600 mt-1">
            Asigna plantillas de horarios a empleados por ubicación y período
          </p>
        </div>
        <Button onClick={handleAdd}>
          + Nueva Asignación
        </Button>
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
            <label className="block text-sm font-medium text-gray-700 mb-1">Empleado</label>
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
            <label className="block text-sm font-medium text-gray-700 mb-1">Ubicación</label>
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
      </div>

      {/* Assignments Table */}
      <div className="bg-white rounded-2xl shadow-xl border border-gray-100 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gradient-to-r from-red-500 to-red-600 text-white">
              <tr>
                <th className="px-6 py-4 text-left font-semibold">Empleado</th>
                <th className="px-6 py-4 text-left font-semibold">Ubicación</th>
                <th className="px-6 py-4 text-center font-semibold">Mes</th>
                <th className="px-6 py-4 text-center font-semibold">Año</th>
                <th className="px-6 py-4 text-left font-semibold">Plantilla</th>
                <th className="px-6 py-4 text-center font-semibold">Horas Totales</th>
                <th className="px-6 py-4 text-center font-semibold">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {filteredAssignments.length === 0 ? (
                <tr>
                  <td colSpan="7" className="px-6 py-8 text-center text-gray-500">
                    No hay asignaciones para el período seleccionado
                  </td>
                </tr>
              ) : (
                filteredAssignments.map((assignment, index) => {
                  const template = getTemplateDetails(assignment.templateId);
                  const totals = template ? hourTemplateService.calculateTotals(template) : { totalHours: 0 };
                  
                  return (
                    <tr key={assignment.id} className={index % 2 === 0 ? 'bg-white' : 'bg-gray-50'}>
                      <td className="px-6 py-4 font-medium text-gray-900">
                        {getEmployeeName(assignment.employeeId)}
                      </td>
                      <td className="px-6 py-4 text-gray-700">
                        {getLocationName(assignment.locationId)}
                      </td>
                      <td className="px-6 py-4 text-center text-gray-700">
                        {months.find(m => m.value === assignment.month)?.label}
                      </td>
                      <td className="px-6 py-4 text-center text-gray-700">
                        {assignment.year}
                      </td>
                      <td className="px-6 py-4 text-gray-700">
                        {getTemplateName(assignment.templateId)}
                      </td>
                      <td className="px-6 py-4 text-center font-semibold text-gray-900">
                        {totals.totalHours.toFixed(2)}
                      </td>
                      <td className="px-6 py-4 text-center">
                        <div className="flex justify-center space-x-2">
                          <button
                            onClick={() => handleEdit(assignment)}
                            className="text-blue-600 hover:text-blue-800 font-medium text-sm"
                          >
                            Editar
                          </button>
                          <button
                            onClick={() => handleDelete(assignment)}
                            className="text-red-600 hover:text-red-800 font-medium text-sm"
                          >
                            Eliminar
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Create/Edit Modal */}
      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editingAssignment ? 'Editar Asignación' : 'Nueva Asignación'}
        size="lg"
        footer={
          <>
            <Button variant="outline" onClick={() => setModalOpen(false)}>
              Cancelar
            </Button>
            <Button onClick={handleSubmit}>
              {editingAssignment ? 'Guardar Cambios' : 'Crear Asignación'}
            </Button>
          </>
        }
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Empleado *
            </label>
            <select
              value={formData.employeeId}
              onChange={(e) => setFormData({ ...formData, employeeId: e.target.value })}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
              required
            >
              <option value="">Seleccionar empleado...</option>
              {employees.map(e => (
                <option key={e.id} value={e.id}>
                  {e.firstName} {e.lastName}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Ubicación *
            </label>
            <select
              value={formData.locationId}
              onChange={(e) => setFormData({ ...formData, locationId: e.target.value })}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
              required
            >
              <option value="">Seleccionar ubicación...</option>
              {locations.map(l => (
                <option key={l.id} value={l.id}>{l.name}</option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Mes *
              </label>
              <select
                value={formData.month}
                onChange={(e) => setFormData({ ...formData, month: parseInt(e.target.value) })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                required
              >
                {months.map(m => (
                  <option key={m.value} value={m.value}>{m.label}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Año *
              </label>
              <select
                value={formData.year}
                onChange={(e) => setFormData({ ...formData, year: parseInt(e.target.value) })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                required
              >
                {years.map(y => (
                  <option key={y} value={y}>{y}</option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Plantilla de Horario *
            </label>
            <select
              value={formData.templateId}
              onChange={(e) => setFormData({ ...formData, templateId: parseInt(e.target.value) })}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
              required
            >
              <option value="">Seleccionar plantilla...</option>
              {templates.map(t => {
                const totals = hourTemplateService.calculateTotals(t);
                return (
                  <option key={t.id} value={t.id}>
                    {t.name} ({totals.totalHours.toFixed(2)} horas)
                  </option>
                );
              })}
            </select>
          </div>

          {/* Template Preview */}
          {formData.templateId && (
            <div className="bg-gray-50 p-4 rounded-lg">
              <h4 className="font-medium text-gray-900 mb-2">Vista previa de la plantilla</h4>
              {(() => {
                const template = getTemplateDetails(parseInt(formData.templateId));
                if (!template) return null;
                const totals = hourTemplateService.calculateTotals(template);
                return (
                  <div className="text-sm">
                    <div className="grid grid-cols-5 gap-2 text-center mb-2">
                      <div className="bg-white p-2 rounded">
                        <span className="block text-xs text-gray-500">Ordinarias</span>
                        <span className="font-medium">{template.ordinarias}</span>
                      </div>
                      <div className="bg-white p-2 rounded">
                        <span className="block text-xs text-gray-500">Diurnas</span>
                        <span className="font-medium">{template.diurnas}</span>
                      </div>
                      <div className="bg-white p-2 rounded">
                        <span className="block text-xs text-gray-500">Nocturnas</span>
                        <span className="font-medium">{template.nocturnas}</span>
                      </div>
                      <div className="bg-white p-2 rounded">
                        <span className="block text-xs text-gray-500">Extra F/Dom</span>
                        <span className="font-medium">{template.extraFDom}</span>
                      </div>
                      <div className="bg-white p-2 rounded">
                        <span className="block text-xs text-gray-500">Dominicales</span>
                        <span className="font-medium">{template.dominicales}</span>
                      </div>
                    </div>
                    <div className="flex justify-between mt-2 pt-2 border-t">
                      <span className="font-medium">Total:</span>
                      <span className="font-bold text-green-600">{totals.totalHours.toFixed(2)} horas</span>
                    </div>
                  </div>
                );
              })()}
            </div>
          )}
        </form>
      </Modal>
    </div>
  );
};

export default TemplateAssignment;
