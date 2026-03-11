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
    isNightShift: false,
    isMultiRange: false,
    timeRanges: []
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

  // Helper function to format time ranges for display
  const formatTimeRanges = (shiftType) => {
    // If timeRanges exist and isMultiRange is true, show all ranges
    if (shiftType.timeRanges && shiftType.timeRanges.length > 0) {
      return shiftType.timeRanges
        .sort((a, b) => a.rangeOrder - b.rangeOrder)
        .map(range => `${range.startTime?.substring(0, 5)}-${range.endTime?.substring(0, 5)}`)
        .join(', ');
    }
    // Fallback to simple startTime-endTime for backward compatibility
    if (shiftType.startTime && shiftType.endTime) {
      return `${shiftType.startTime.substring(0, 5)}-${shiftType.endTime.substring(0, 5)}`;
    }
    return '-';
  };

  const handleAdd = () => {
    setEditingShiftType(null);
    setFormData({
      name: '',
      description: '',
      startTime: '',
      endTime: '',
      isNightShift: false,
      isMultiRange: false,
      timeRanges: [{ startTime: '', endTime: '', rangeOrder: 1 }]
    });
    setModalOpen(true);
  };

  const handleEdit = (shiftType) => {
    setEditingShiftType(shiftType);
    
    // Load existing time ranges or create from startTime/endTime
    let timeRanges = [];
    if (shiftType.timeRanges && shiftType.timeRanges.length > 0) {
      timeRanges = shiftType.timeRanges.map(range => ({
        id: range.id,
        startTime: range.startTime ? range.startTime.substring(0, 5) : '',
        endTime: range.endTime ? range.endTime.substring(0, 5) : '',
        rangeOrder: range.rangeOrder
      }));
    } else if (shiftType.startTime && shiftType.endTime) {
      // For backward compatibility - convert single range to array
      timeRanges = [{
        startTime: shiftType.startTime ? shiftType.startTime.substring(0, 5) : '',
        endTime: shiftType.endTime ? shiftType.endTime.substring(0, 5) : '',
        rangeOrder: 1
      }];
    }

    setFormData({
      name: shiftType.name || '',
      description: shiftType.description || '',
      startTime: shiftType.startTime ? shiftType.startTime.substring(0, 5) : '',
      endTime: shiftType.endTime ? shiftType.endTime.substring(0, 5) : '',
      isNightShift: shiftType.isNightShift || false,
      isMultiRange: shiftType.isMultiRange || false,
      timeRanges: timeRanges
    });
    setModalOpen(true);
  };

  // Add a new time range
  const addTimeRange = () => {
    const newOrder = formData.timeRanges.length + 1;
    setFormData({
      ...formData,
      timeRanges: [...formData.timeRanges, { startTime: '', endTime: '', rangeOrder: newOrder }]
    });
  };

  // Remove a time range
  const removeTimeRange = (index) => {
    if (formData.timeRanges.length <= 1) {
      alert('Debe tener al menos un rango de horario');
      return;
    }
    const updatedRanges = formData.timeRanges.filter((_, i) => i !== index);
    // Reorder remaining ranges
    updatedRanges.forEach((range, i) => {
      range.rangeOrder = i + 1;
    });
    setFormData({
      ...formData,
      timeRanges: updatedRanges
    });
  };

  // Update a specific time range
  const updateTimeRange = (index, field, value) => {
    const updatedRanges = [...formData.timeRanges];
    updatedRanges[index][field] = value;
    setFormData({
      ...formData,
      timeRanges: updatedRanges
    });
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
      // Prepare time ranges data
      const validTimeRanges = formData.timeRanges.filter(
        range => range.startTime && range.endTime
      );

      // Calculate total duration
      let totalDuration = 0;
      validTimeRanges.forEach(range => {
        const start = parseTime(range.startTime);
        const end = parseTime(range.endTime);
        let hours = end - start;
        if (hours < 0) hours += 24; // Handle overnight shifts
        totalDuration += hours;
      });

      const shiftTypeData = {
        name: formData.name,
        description: formData.description,
        // For backward compatibility - use first range
        startTime: validTimeRanges.length > 0 ? validTimeRanges[0].startTime : formData.startTime,
        endTime: validTimeRanges.length > 0 ? validTimeRanges[validTimeRanges.length - 1].endTime : formData.endTime,
        isNightShift: formData.isNightShift,
        isMultiRange: formData.isMultiRange || validTimeRanges.length > 1,
        // Calculate duration from ranges or use default
        duration: totalDuration > 0 ? totalDuration : calculateDurationFromTimes(formData.startTime, formData.endTime),
        // Time ranges for multi-range shifts
        timeRanges: validTimeRanges.map((range, index) => ({
          id: range.id || null,
          startTime: range.startTime,
          endTime: range.endTime,
          rangeOrder: index + 1,
          isNightRange: isNightTime(range.startTime)
        }))
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

  // Helper to parse time string to hours
  const parseTime = (timeStr) => {
    if (!timeStr) return 0;
    const [hours, minutes] = timeStr.split(':').map(Number);
    return hours + (minutes || 0) / 60;
  };

  // Calculate duration in hours
  const calculateDurationFromTimes = (start, end) => {
    if (!start || !end) return 0;
    const startHours = parseTime(start);
    const endHours = parseTime(end);
    let duration = endHours - startHours;
    if (duration < 0) duration += 24; // Handle overnight shifts
    return duration;
  };

  // Check if time is night (10 PM - 6 AM)
  const isNightTime = (timeStr) => {
    if (!timeStr) return false;
    const hour = parseInt(timeStr.split(':')[0], 10);
    return hour >= 22 || hour < 6;
  };

  const columns = [
    { key: 'name', header: 'Nombre', render: (value) => <span className="font-medium">{value}</span> },
    { 
      key: 'timeRanges', 
      header: 'Horario', 
      render: (value, row) => formatTimeRanges(row) 
    },
    { key: 'isNightShift', header: 'Nocturno', render: (value) => value ? 'Sí' : 'No' },
    { key: 'isMultiRange', header: 'Multi-rango', render: (value) => value ? 'Sí' : 'No' }
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
        size="lg"
      >
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-2">
              Nombre *
            </label>
            <input
              type="text"
              id="name"
              value={formData.name}
              onChange={(e) => setFormData({...formData, name: e.target.value})}
              required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
              placeholder="Ej: Mañana, Tarde, Partido"
            />
          </div>

          {/* Time Ranges Section */}
          <div>
            <div className="flex items-center justify-between mb-3">
              <label className="block text-sm font-medium text-gray-700">
                Rangos de Horario *
              </label>
              <button
                type="button"
                onClick={addTimeRange}
                className="text-sm text-purple-600 hover:text-purple-700 font-medium flex items-center"
              >
                <span className="mr-1">+</span> Agregar rango
              </button>
            </div>
            
            <div className="space-y-3 max-h-60 overflow-y-auto">
              {formData.timeRanges.map((range, index) => (
                <div key={index} className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                  <span className="text-sm font-medium text-gray-500 min-w-[24px]">
                    {index + 1}.
                  </span>
                  <div className="flex-1 grid grid-cols-2 gap-3">
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">Inicio</label>
                      <input
                        type="time"
                        value={range.startTime}
                        onChange={(e) => updateTimeRange(index, 'startTime', e.target.value)}
                        required
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 text-sm"
                      />
                    </div>
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">Fin</label>
                      <input
                        type="time"
                        value={range.endTime}
                        onChange={(e) => updateTimeRange(index, 'endTime', e.target.value)}
                        required
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 text-sm"
                      />
                    </div>
                  </div>
                  {formData.timeRanges.length > 1 && (
                    <button
                      type="button"
                      onClick={() => removeTimeRange(index)}
                      className="text-red-500 hover:text-red-700 p-2"
                      title="Eliminar rango"
                    >
                      ✕
                    </button>
                  )}
                </div>
              ))}
            </div>
            
            {/* Show total duration */}
            {formData.timeRanges.length > 0 && (
              <div className="mt-2 text-sm text-gray-600">
                Duración total: {formData.timeRanges.reduce((total, range) => {
                  if (!range.startTime || !range.endTime) return total;
                  const start = parseTime(range.startTime);
                  const end = parseTime(range.endTime);
                  let hours = end - start;
                  if (hours < 0) hours += 24;
                  return total + hours;
                }, 0)} horas
              </div>
            )}
          </div>

          <div>
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={formData.isNightShift}
                onChange={(e) => setFormData({...formData, isNightShift: e.target.checked})}
                className="mr-2 h-4 w-4 text-purple-600 border-gray-300 rounded focus:ring-purple-500"
              />
              <span className="text-sm text-gray-700">Es turno nocturno</span>
            </label>
            <p className="text-xs text-gray-500 mt-1 ml-6">
              Marque esta opción si el turno incluye horas entre 10 PM y 6 AM
            </p>
          </div>

          <div>
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={formData.isMultiRange}
                onChange={(e) => setFormData({...formData, isMultiRange: e.target.checked})}
                className="mr-2 h-4 w-4 text-purple-600 border-gray-300 rounded focus:ring-purple-500"
              />
              <span className="text-sm text-gray-700">Es turno multi-rango (con descanso)</span>
            </label>
            <p className="text-xs text-gray-500 mt-1 ml-6">
              Active esta opción si el turno tiene un descanso intermedio (ej: 7am-1pm, 5pm-10pm)
            </p>
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
              placeholder="Descripción opcional del tipo de turno"
            />
          </div>

          <div className="flex justify-end space-x-3 pt-4 border-t border-gray-200">
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
