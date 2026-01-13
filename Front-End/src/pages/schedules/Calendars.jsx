import { useState, useEffect } from 'react';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import calendarService from '../../services/calendarService';

const Calendars = () => {
  const [calendars, setCalendars] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingCalendar, setEditingCalendar] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    date: '',
    isHoliday: false,
    isWorkday: true
  });

  useEffect(() => {
    loadCalendars();
  }, []);

  const loadCalendars = async () => {
    try {
      const response = await calendarService.getAllCalendars();
      if (response.data) {
        setCalendars(Array.isArray(response.data) ? response.data : []);
      } else {
        console.error('Failed to load calendars:', response.message);
        alert('Error al cargar calendarios: ' + (response.message || 'Error desconocido'));
      }
    } catch (error) {
      console.error('Error loading calendars:', error);
      alert('Error de conexión al cargar calendarios');
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingCalendar(null);
    setFormData({
      name: '',
      description: '',
      date: '',
      isHoliday: false,
      isWorkday: true
    });
    setModalOpen(true);
  };

  const handleEdit = (calendar) => {
    setEditingCalendar(calendar);
    setFormData({
      name: calendar.name || '',
      description: calendar.description || '',
      date: calendar.date ? calendar.date.split('T')[0] : '',
      isHoliday: calendar.isHoliday || false,
      isWorkday: calendar.isWorkday !== false
    });
    setModalOpen(true);
  };

  const handleDelete = async (calendar) => {
    if (window.confirm(`¿Estás seguro de que quieres eliminar el calendario "${calendar.name}"?`)) {
      try {
        await calendarService.deleteCalendar(calendar.id);
        await loadCalendars();
      } catch (error) {
        console.error('Error deleting calendar:', error);
        alert('Error al eliminar el calendario');
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const calendarData = {
        name: formData.name,
        description: formData.description,
        date: formData.date,
        isHoliday: formData.isHoliday,
        isWorkday: formData.isWorkday
      };

      if (editingCalendar) {
        await calendarService.updateCalendar(editingCalendar.id, calendarData);
      } else {
        await calendarService.createCalendar(calendarData);
      }

      setModalOpen(false);
      await loadCalendars();
    } catch (error) {
      console.error('Error saving calendar:', error);
      alert('Error al guardar el calendario');
    }
  };

  const columns = [
    { key: 'name', header: 'Nombre', render: (value) => <span className="font-medium">{value}</span> },
    { key: 'date', header: 'Fecha', render: (value) => value ? new Date(value).toLocaleDateString() : '' },
    { key: 'isHoliday', header: 'Es Festivo', render: (value) => value ? 'Sí' : 'No' },
    { key: 'isWorkday', header: 'Es Día Laboral', render: (value) => value ? 'Sí' : 'No' }
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Gestión de Calendarios</h1>
          <p className="text-gray-600 mt-1">Administra los calendarios de horarios</p>
        </div>
      </div>

      <DataTable
        title="Calendarios"
        icon="📅"
        columns={columns}
        data={calendars}
        loading={loading}
        onAdd={handleAdd}
        onEdit={handleEdit}
        onDelete={handleDelete}
        addButtonText="Agregar Calendario"
        searchPlaceholder="Buscar calendarios..."
        emptyMessage="No hay calendarios registrados en el sistema"
      />

      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editingCalendar ? 'Editar Calendario' : 'Agregar Calendario'}
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
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
              placeholder="Ingresa el nombre del calendario"
            />
          </div>

          <div>
            <label htmlFor="date" className="block text-sm font-medium text-gray-700 mb-2">
              Fecha
            </label>
            <input
              type="date"
              id="date"
              value={formData.date}
              onChange={(e) => setFormData({...formData, date: e.target.value})}
              required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="flex items-center">
                <input
                  type="checkbox"
                  checked={formData.isHoliday}
                  onChange={(e) => setFormData({...formData, isHoliday: e.target.checked})}
                  className="mr-2"
                />
                Es festivo
              </label>
            </div>
            <div>
              <label className="flex items-center">
                <input
                  type="checkbox"
                  checked={formData.isWorkday}
                  onChange={(e) => setFormData({...formData, isWorkday: e.target.checked})}
                  className="mr-2"
                />
                Es día laboral
              </label>
            </div>
          </div>

          <div>
            <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-2">
              Descripción
            </label>
            <textarea
              id="description"
              value={formData.description}
              onChange={(e) => setFormData({...formData, description: e.target.value})}
              rows={4}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
              placeholder="Ingresa la descripción del calendario"
            />
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
              className="px-6 py-3 bg-gradient-to-r from-purple-500 to-purple-600 hover:from-purple-600 hover:to-purple-700 text-white rounded-lg transition-all duration-200 font-medium shadow-lg hover:shadow-xl"
            >
              {editingCalendar ? 'Actualizar' : 'Crear'} Calendario
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Calendars;