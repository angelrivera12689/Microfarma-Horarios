import { useState, useEffect } from 'react';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import shiftTypeService from '../../services/shiftTypeService';

const ShiftTypes = () => {
  const [shiftTypes, setShiftTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingShiftType, setEditingShiftType] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    startTime: '',
    endTime: '',
    isNightShift: false
  });

  useEffect(() => {
    loadShiftTypes();
  }, []);

  const loadShiftTypes = async () => {
    try {
      const response = await shiftTypeService.getAllShiftTypes();
      if (response.data) {
        setShiftTypes(Array.isArray(response.data) ? response.data : []);
      } else {
        console.error('Failed to load shift types:', response.message);
        alert('Error al cargar tipos de turno: ' + (response.message || 'Error desconocido'));
      }
    } catch (error) {
      console.error('Error loading shift types:', error);
      alert('Error de conexión al cargar tipos de turno');
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingShiftType(null);
    setFormData({
      name: '',
      description: '',
      startTime: '',
      endTime: '',
      isNightShift: false
    });
    setModalOpen(true);
  };

  const handleEdit = (shiftType) => {
    setEditingShiftType(shiftType);
    setFormData({
      name: shiftType.name || '',
      description: shiftType.description || '',
      startTime: shiftType.startTime || '',
      endTime: shiftType.endTime || '',
      isNightShift: shiftType.isNightShift || false
    });
    setModalOpen(true);
  };

  const handleDelete = async (shiftType) => {
    if (window.confirm(`¿Estás seguro de que quieres eliminar el tipo de turno "${shiftType.name}"?`)) {
      try {
        await shiftTypeService.deleteShiftType(shiftType.id);
        await loadShiftTypes();
      } catch (error) {
        console.error('Error deleting shift type:', error);
        alert('Error al eliminar el tipo de turno');
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const shiftTypeData = {
        name: formData.name,
        description: formData.description,
        startTime: formData.startTime,
        endTime: formData.endTime,
        isNightShift: formData.isNightShift
      };

      if (editingShiftType) {
        await shiftTypeService.updateShiftType(editingShiftType.id, shiftTypeData);
      } else {
        await shiftTypeService.createShiftType(shiftTypeData);
      }

      setModalOpen(false);
      await loadShiftTypes();
    } catch (error) {
      console.error('Error saving shift type:', error);
      alert('Error al guardar el tipo de turno');
    }
  };

  const columns = [
    { key: 'name', header: 'Nombre', render: (value) => <span className="font-medium">{value}</span> },
    { key: 'startTime', header: 'Hora Inicio', render: (value) => value || '' },
    { key: 'endTime', header: 'Hora Fin', render: (value) => value || '' },
    { key: 'isNightShift', header: 'Turno Nocturno', render: (value) => value ? 'Sí' : 'No' }
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Gestión de Tipos de Turno</h1>
          <p className="text-gray-600 mt-1">Administra los tipos de turno disponibles</p>
        </div>
      </div>

      <DataTable
        title="Tipos de Turno"
        icon="🕐"
        columns={columns}
        data={shiftTypes}
        loading={loading}
        onAdd={handleAdd}
        onEdit={handleEdit}
        onDelete={handleDelete}
        addButtonText="Agregar Tipo de Turno"
        searchPlaceholder="Buscar tipos de turno..."
        emptyMessage="No hay tipos de turno registrados en el sistema"
      />

      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editingShiftType ? 'Editar Tipo de Turno' : 'Agregar Tipo de Turno'}
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
              placeholder="Ej: Mañana, Tarde, Ángel 7-1"
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label htmlFor="startTime" className="block text-sm font-medium text-gray-700 mb-2">
                Hora de Inicio
              </label>
              <input
                type="time"
                id="startTime"
                value={formData.startTime}
                onChange={(e) => setFormData({...formData, startTime: e.target.value})}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
              />
            </div>
            <div>
              <label htmlFor="endTime" className="block text-sm font-medium text-gray-700 mb-2">
                Hora de Fin
              </label>
              <input
                type="time"
                id="endTime"
                value={formData.endTime}
                onChange={(e) => setFormData({...formData, endTime: e.target.value})}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
              />
            </div>
          </div>

          <div>
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={formData.isNightShift}
                onChange={(e) => setFormData({...formData, isNightShift: e.target.checked})}
                className="mr-2"
              />
              Es turno nocturno
            </label>
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
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
              placeholder="Descripción del tipo de turno"
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
              {editingShiftType ? 'Actualizar' : 'Crear'} Tipo de Turno
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default ShiftTypes;