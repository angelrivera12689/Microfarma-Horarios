import { useState, useEffect } from 'react';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import WeeklyGrid from '../../components/WeeklyGrid';
import scheduleService from '../../services/scheduleService';
import employeeService from '../../services/employeeService';

const Schedules = () => {
  const [schedules, setSchedules] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingSchedule, setEditingSchedule] = useState(null);
  const [selectedSchedule, setSelectedSchedule] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    employeeId: '',
    startDate: '',
    endDate: '',
    isActive: true
  });

  useEffect(() => {
    loadSchedules();
    loadEmployees();
  }, []);

  const loadSchedules = async () => {
    try {
      const response = await scheduleService.getAllSchedules();
      if (response.data) {
        setSchedules(Array.isArray(response.data) ? response.data : []);
      } else {
        console.error('Failed to load schedules:', response.message);
        alert('Error al cargar horarios: ' + (response.message || 'Error desconocido'));
      }
    } catch (error) {
      console.error('Error loading schedules:', error);
      alert('Error de conexión al cargar horarios');
    } finally {
      setLoading(false);
    }
  };

  const loadEmployees = async () => {
    try {
      const response = await employeeService.getAllEmployees();
      if (response.data) {
        setEmployees(Array.isArray(response.data) ? response.data : []);
      } else {
        console.error('Failed to load employees:', response.message);
      }
    } catch (error) {
      console.error('Error loading employees:', error);
    }
  };

  const handleAdd = () => {
    setEditingSchedule(null);
    setFormData({
      name: '',
      description: '',
      employeeId: '',
      startDate: '',
      endDate: '',
      isActive: true
    });
    setModalOpen(true);
  };

  const handleEdit = (schedule) => {
    setEditingSchedule(schedule);
    setFormData({
      name: schedule.name || '',
      description: schedule.description || '',
      employeeId: schedule.employee?.id || '',
      startDate: schedule.startDate ? schedule.startDate.split('T')[0] : '',
      endDate: schedule.endDate ? schedule.endDate.split('T')[0] : '',
      isActive: schedule.isActive ?? true
    });
    setModalOpen(true);
  };

  const handleDelete = async (schedule) => {
    if (window.confirm(`¿Estás seguro de que quieres eliminar el horario "${schedule.name}"?`)) {
      try {
        await scheduleService.deleteSchedule(schedule.id);
        await loadSchedules();
      } catch (error) {
        console.error('Error deleting schedule:', error);
        alert('Error al eliminar el horario');
      }
    }
  };

  const handleView = (schedule) => {
    setSelectedSchedule(schedule);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const scheduleData = {
        name: formData.name,
        description: formData.description,
        employee: { id: formData.employeeId },
        startDate: formData.startDate,
        endDate: formData.endDate,
        isActive: formData.isActive
      };

      if (editingSchedule) {
        await scheduleService.updateSchedule(editingSchedule.id, scheduleData);
      } else {
        await scheduleService.createSchedule(scheduleData);
      }

      setModalOpen(false);
      await loadSchedules();
    } catch (error) {
      console.error('Error saving schedule:', error);
      alert('Error al guardar el horario');
    }
  };

  const columns = [
    { key: 'name', header: 'Nombre', render: (value) => <span className="font-medium">{value}</span> },
    { key: 'description', header: 'Descripción' },
    {
      key: 'employee',
      header: 'Empleado',
      render: (value) => value ? `${value.firstName} ${value.lastName}` : 'Sin empleado'
    },
    {
      key: 'startDate',
      header: 'Fecha Inicio',
      render: (value) => value ? new Date(value).toLocaleDateString() : ''
    },
    {
      key: 'endDate',
      header: 'Fecha Fin',
      render: (value) => value ? new Date(value).toLocaleDateString() : ''
    },
    {
      key: 'isActive',
      header: 'Estado',
      render: (value) => (
        <span className={`px-2 py-1 rounded-full text-xs font-medium ${
          value ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
        }`}>
          {value ? 'Activo' : 'Inactivo'}
        </span>
      )
    },
    {
      key: 'actions',
      header: 'Acciones',
      render: (value, row) => (
        <button
          onClick={() => handleView(row)}
          className="text-blue-600 hover:text-blue-800 text-sm font-medium"
        >
          Ver Detalles
        </button>
      )
    }
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Gestión de Horarios</h1>
          <p className="text-gray-600 mt-1">Administra los horarios personalizados</p>
        </div>
      </div>

      <DataTable
        title="Horarios"
        icon="📅"
        columns={columns}
        data={schedules}
        loading={loading}
        onAdd={handleAdd}
        onEdit={handleEdit}
        onDelete={handleDelete}
        addButtonText="Agregar Horario"
        searchPlaceholder="Buscar horarios..."
        emptyMessage="No hay horarios registrados en el sistema"
      />

      {selectedSchedule && (
        <WeeklyGrid schedule={selectedSchedule} />
      )}

      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editingSchedule ? 'Editar Horario' : 'Agregar Horario'}
        size="md"
      >
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-2">
              Nombre
            </label>
            <input
              type="text"
              id="name"
              value={formData.name}
              onChange={(e) => setFormData({...formData, name: e.target.value})}
              required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors duration-200"
              placeholder="Ingresa el nombre del horario"
            />
          </div>

          <div>
            <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-2">
              Descripción
            </label>
            <textarea
              id="description"
              value={formData.description}
              onChange={(e) => setFormData({...formData, description: e.target.value})}
              rows={3}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors duration-200"
              placeholder="Descripción del horario"
            />
          </div>

          <div>
            <label htmlFor="employeeId" className="block text-sm font-medium text-gray-700 mb-2">
              Empleado
            </label>
            <select
              id="employeeId"
              value={formData.employeeId}
              onChange={(e) => setFormData({...formData, employeeId: e.target.value})}
              required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors duration-200"
            >
              <option value="">Seleccionar empleado</option>
              {employees.map(employee => (
                <option key={employee.id} value={employee.id}>
                  {employee.firstName} {employee.lastName}
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label htmlFor="startDate" className="block text-sm font-medium text-gray-700 mb-2">
                Fecha de Inicio
              </label>
              <input
                type="date"
                id="startDate"
                value={formData.startDate}
                onChange={(e) => setFormData({...formData, startDate: e.target.value})}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors duration-200"
              />
            </div>
            <div>
              <label htmlFor="endDate" className="block text-sm font-medium text-gray-700 mb-2">
                Fecha de Fin
              </label>
              <input
                type="date"
                id="endDate"
                value={formData.endDate}
                onChange={(e) => setFormData({...formData, endDate: e.target.value})}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors duration-200"
              />
            </div>
          </div>

          <div className="flex items-center">
            <input
              type="checkbox"
              id="isActive"
              checked={formData.isActive}
              onChange={(e) => setFormData({...formData, isActive: e.target.checked})}
              className="h-4 w-4 text-green-600 focus:ring-green-500 border-gray-300 rounded"
            />
            <label htmlFor="isActive" className="ml-2 block text-sm text-gray-900">
              Horario activo
            </label>
          </div>

          <div className="flex justify-end space-x-3 pt-4">
            <button
              type="button"
              onClick={() => setModalOpen(false)}
              className="px-6 py-3 text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors duration-200 font-medium"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="px-6 py-3 bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700 text-white rounded-lg transition-all duration-200 font-medium shadow-lg hover:shadow-xl"
            >
              {editingSchedule ? 'Actualizar' : 'Crear'} Horario
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Schedules;