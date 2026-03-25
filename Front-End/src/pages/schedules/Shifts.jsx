import { useState, useEffect, useRef } from 'react';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import shiftService from '../../services/shiftService';
import employeeService from '../../services/employeeService';
import locationService from '../../services/locationService';
import shiftTypeService from '../../services/shiftTypeService';
import { exportShiftsToExcel } from '../../services/excelExportService';

const Shifts = () => {
  const [shifts, setShifts] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [locations, setLocations] = useState([]);
  const [shiftTypes, setShiftTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [bulkModalOpen, setBulkModalOpen] = useState(false);
  const [editingShift, setEditingShift] = useState(null);
  const [viewMode, setViewMode] = useState('table'); // 'table' or 'calendar'
  const [selectedLocation, setSelectedLocation] = useState('');
  const [currentMonth, setCurrentMonth] = useState(new Date().getMonth());
  const [currentYear, setCurrentYear] = useState(new Date().getFullYear());
  const calendarRef = useRef();
  
  // Estado para mostrar advertencia de turno existente
  const [existingShiftWarning, setExistingShiftWarning] = useState(null);
  const [checkingShift, setCheckingShift] = useState(false);
  const [formError, setFormError] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    date: '',
    employeeId: '',
    locationId: '',
    shiftTypeId: '',
    notes: ''
  });

  // Bulk shift creation form data
  const [bulkFormData, setBulkFormData] = useState({
    startDate: '',
    endDate: '',
    employeeId: '',
    locationId: '',
    shiftTypeId: '',
    notes: ''
  });

  // Calculate shifts to be created for preview
  const getBulkShiftsPreview = () => {
    if (!bulkFormData.startDate || !bulkFormData.endDate) return [];
    
    const start = new Date(bulkFormData.startDate);
    const end = new Date(bulkFormData.endDate);
    const previewShifts = [];
    
    const current = new Date(start);
    while (current <= end) {
      previewShifts.push({
        date: current.toISOString().split('T')[0]
      });
      current.setDate(current.getDate() + 1);
    }
    
    return previewShifts;
  };

  useEffect(() => {
    loadShifts();
    loadEmployees();
    loadLocations();
    loadShiftTypes();
  }, []);

  const loadShifts = async () => {
    try {
      const response = await shiftService.getAllShifts();
      if (response.data) {
        const allShifts = Array.isArray(response.data) ? response.data : [];
        // Filter out delivery employees - only show non-delivery shifts in general schedule
        const nonDeliveryShifts = allShifts.filter(shift => {
          const employeePosition = shift.employee?.position?.name || '';
          return !employeePosition.toLowerCase().includes('domicili');
        });
        setShifts(nonDeliveryShifts);
      } else {
        console.error('Failed to load shifts:', response.message);
        alert('Error al cargar turnos: ' + (response.message || 'Error desconocido'));
      }
    } catch (error) {
      console.error('Error loading shifts:', error);
      alert('Error de conexión al cargar turnos');
    } finally {
      setLoading(false);
    }
  };

  const loadEmployees = async () => {
    try {
      const response = await employeeService.getAllEmployees();
      if (response.data) {
        // Filter out delivery employees - only show non-delivery employees in general schedule
        const allEmployees = Array.isArray(response.data) ? response.data : [];
        const nonDeliveryEmployees = allEmployees.filter(emp => {
          const employeePosition = emp.position?.name || '';
          return !employeePosition.toLowerCase().includes('domicili');
        });
        setEmployees(nonDeliveryEmployees);
      } else {
        console.error('Failed to load employees:', response.message);
      }
    } catch (error) {
      console.error('Error loading employees:', error);
    }
  };

  const loadLocations = async () => {
    try {
      const response = await locationService.getAllLocations();
      if (response.data) {
        setLocations(Array.isArray(response.data) ? response.data : []);
      } else {
        console.error('Failed to load locations:', response.message);
      }
    } catch (error) {
      console.error('Error loading locations:', error);
    }
  };

  const loadShiftTypes = async () => {
    try {
      const response = await shiftTypeService.getAllShiftTypes();
      if (response.data) {
        // Filter out delivery-specific shift types - only show general shift types
        const allShiftTypes = Array.isArray(response.data) ? response.data : [];
        const generalShiftTypes = allShiftTypes.filter(type => {
          const typeName = type.name || '';
          return !typeName.toLowerCase().includes('domicili');
        });
        setShiftTypes(generalShiftTypes);
      } else {
        console.error('Failed to load shift types:', response.message);
      }
    } catch (error) {
      console.error('Error loading shift types:', error);
    }
  };

  // Función para verificar si el empleado ya tiene un turno en la fecha seleccionada
  const checkExistingShift = async (employeeId, date) => {
    if (!employeeId || !date) {
      setExistingShiftWarning(null);
      return;
    }
    
    setCheckingShift(true);
    try {
      const response = await shiftService.checkExistingShift(employeeId, date);
      if (response.status && response.data) {
        // Hay un turno existente - mostrar advertencia
        setExistingShiftWarning({
          message: response.message,
          existingShift: response.data
        });
      } else {
        setExistingShiftWarning(null);
      }
    } catch (error) {
      console.error('Error checking existing shift:', error);
      setExistingShiftWarning(null);
    } finally {
      setCheckingShift(false);
    }
  };

  // Handler para cambio de empleado en el formulario
  const handleEmployeeChange = (employeeId) => {
    setFormData({...formData, employeeId});
    checkExistingShift(employeeId, formData.date);
  };

  // Handler para cambio de fecha en el formulario
  const handleDateChange = (date) => {
    setFormData({...formData, date});
    checkExistingShift(formData.employeeId, date);
  };

  const handleAdd = () => {
    setEditingShift(null);
    setFormData({
      date: '',
      employeeId: '',
      locationId: '',
      shiftTypeId: '',
      notes: ''
    });
    setExistingShiftWarning(null);
    setFormError(null);
    setSubmitting(false);
    setModalOpen(true);
  };

  const handleBulkAdd = () => {
    setBulkFormData({
      startDate: '',
      endDate: '',
      employeeId: '',
      locationId: '',
      shiftTypeId: '',
      notes: ''
    });
    setBulkModalOpen(true);
  };

  const handleBulkSubmit = async (e) => {
    e.preventDefault();

    if (!bulkFormData.startDate || !bulkFormData.endDate || !bulkFormData.employeeId || !bulkFormData.locationId || !bulkFormData.shiftTypeId) {
      alert('Por favor complete todos los campos requeridos');
      return;
    }

    const start = new Date(bulkFormData.startDate);
    const end = new Date(bulkFormData.endDate);
    
    if (start > end) {
      alert('La fecha de inicio no puede ser mayor que la fecha fin');
      return;
    }

    try {
      const shiftsToCreate = [];
      const current = new Date(start);
      
      while (current <= end) {
        shiftsToCreate.push({
          date: current.toISOString().split('T')[0],
          employee: { id: bulkFormData.employeeId },
          location: { id: bulkFormData.locationId },
          shiftType: { id: bulkFormData.shiftTypeId },
          notes: bulkFormData.notes
        });
        current.setDate(current.getDate() + 1);
      }

      await shiftService.createBulkShifts(shiftsToCreate);
      setBulkModalOpen(false);
      await loadShifts();
      alert(`Se crearon ${shiftsToCreate.length} turnos correctamente`);
    } catch (error) {
      console.error('Error creating bulk shifts:', error);
      alert('Error al crear los turnos en bulk');
    }
  };

  const handleEdit = (shift) => {
    setEditingShift(shift);
    setFormData({
      date: shift.date ? shift.date.split('T')[0] : '',
      employeeId: shift.employee?.id || '',
      locationId: shift.location?.id || '',
      shiftTypeId: shift.shiftType?.id || '',
      notes: shift.notes || ''
    });
    setModalOpen(true);
  };

  const handleDelete = async (shift) => {
    if (!shift?.id) {
      console.error('Shift ID is missing:', shift);
      alert('Error: No se puede eliminar el turno porque falta el ID');
      return;
    }
    
    if (window.confirm(`¿Estás seguro de que quieres eliminar este turno?`)) {
      try {
        console.log('Starting delete for shift:', shift.id);
        await shiftService.deleteShift(shift.id);
        console.log('Delete successful, reloading shifts...');
        await loadShifts();
        alert('Turno eliminado correctamente');
      } catch (error) {
        console.error('Error deleting shift:', error);
        alert('Error al eliminar el turno: ' + (error.message || 'Error desconocido'));
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Prevent multiple submissions
    if (submitting) return;

    // Verificar si ya existe un turno para esta fecha antes de crear
    if (existingShiftWarning) {
      setFormError('No se pudo crear el turno: Ya existe una asignación para este empleado en la fecha seleccionada.');
      return;
    }

    setFormError(null);
    setSubmitting(true);

    try {
      const shiftData = {
        date: formData.date,
        employee: { id: formData.employeeId },
        location: { id: formData.locationId },
        shiftType: { id: formData.shiftTypeId },
        notes: formData.notes
      };

      if (editingShift) {
        await shiftService.updateShift(editingShift.id, shiftData);
      } else {
        await shiftService.createShift(shiftData);
      }

      setModalOpen(false);
      await loadShifts();
    } catch (error) {
      console.error('Error saving shift:', error);
      setFormError('Error al guardar el turno');
    } finally {
      setSubmitting(false);
    }
  };

  const columns = [
    { key: 'date', header: 'Fecha', render: (value) => value ? value.split('-').reverse().join('/') : '' },
    { key: 'employee', header: 'Empleado', render: (value) => value ? `${value.firstName} ${value.lastName}` : 'Sin empleado' },
    { key: 'location', header: 'Ubicación', render: (value) => value?.name || 'Sin ubicación' },
    { key: 'shiftType', header: 'Tipo de Turno', render: (value) => value?.name || 'Sin tipo' },
    { key: 'notes', header: 'Notas' }
  ];

  const filteredShifts = selectedLocation
    ? shifts.filter(shift => shift.location?.id == selectedLocation)
    : shifts;

  const exportToExcel = () => {
    const dataToExport = viewMode === 'calendar' ? filteredShifts : shifts;
    exportShiftsToExcel(dataToExport, 'turnos');
  };




  const renderCalendar = () => {
    const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
    const firstDayOfMonth = new Date(currentYear, currentMonth, 1).getDay();
    const days = [];

    // Empty cells for days before the first day of the month
    for (let i = 0; i < firstDayOfMonth; i++) {
      days.push(<div key={`empty-${i}`} className="h-24 border border-gray-200 bg-gray-50"></div>);
    }

    // Days of the month
    for (let day = 1; day <= daysInMonth; day++) {
      const dateStr = `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
      const dayShifts = filteredShifts
        .filter(shift => shift.date?.startsWith(dateStr))
        .sort((a, b) => {
          const timeA = a.shiftType?.startTime || '00:00';
          const timeB = b.shiftType?.startTime || '00:00';
          return timeA.localeCompare(timeB);
        });

      days.push(
        <div key={day} className="min-h-40 border border-gray-300 p-2 bg-gradient-to-br from-white to-gray-50 hover:from-blue-50 hover:to-blue-100 transition-colors overflow-y-auto rounded-lg shadow-sm">
          <div className="text-lg font-bold text-gray-800 mb-2 text-center border-b border-gray-200 pb-1">{day}</div>
          <div className="space-y-2">
            {dayShifts.length === 0 ? (
              <div className="text-sm text-gray-500 italic text-center py-4">Sin turnos</div>
            ) : (
              dayShifts.map((shift) => (
                <div key={shift.id} className="text-xs bg-gradient-to-r from-blue-100 to-blue-200 text-blue-900 rounded-md px-2 py-1.5 border border-blue-300 shadow-sm">
                  <div className="font-semibold truncate text-blue-950">
                    {shift.employee?.firstName} {shift.employee?.lastName}
                  </div>
                  <div className="text-blue-800 truncate font-medium">
                    {shift.shiftType?.name}
                  </div>
                  <div className="text-blue-700 text-xs font-mono">
                    {shift.shiftType?.startTime?.slice(0, 5)} - {shift.shiftType?.endTime?.slice(0, 5)}
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      );
    }

    return days;
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Gestión de Turnos</h1>
          <p className="text-gray-600 mt-1">Administra los turnos de los empleados</p>
        </div>
      </div>

      {/* View Toggle and Filters */}
      <div className="bg-white rounded-2xl shadow-xl p-6 border border-gray-100">
        <div className="flex flex-wrap gap-4 items-center mb-4">
          <div className="flex space-x-2">
            <button
              onClick={() => setViewMode('table')}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                viewMode === 'table'
                  ? 'bg-red-500 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              Vista Tabla
            </button>
            <button
              onClick={() => setViewMode('calendar')}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                viewMode === 'calendar'
                  ? 'bg-red-500 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              Vista Calendario
            </button>
            <button
              onClick={exportToExcel}
              className="px-4 py-2 bg-green-500 text-white rounded-lg font-medium hover:bg-green-600 transition-colors"
            >
              📊 Descargar Excel
            </button>
            <button
              onClick={handleBulkAdd}
              className="px-4 py-2 bg-purple-500 text-white rounded-lg font-medium hover:bg-purple-600 transition-colors"
            >
              Crear Turnos en Serie
            </button>
            {viewMode === 'calendar' && (
              <button
                onClick={() => {
                  if (!selectedLocation) {
                    alert('Por favor selecciona una ubicación para descargar el calendario.');
                    return;
                  }
                  shiftService.downloadCalendarPdf(currentYear, currentMonth + 1, selectedLocation);
                }}
                className="px-4 py-2 bg-blue-500 text-white rounded-lg font-medium hover:bg-blue-600 transition-colors"
              >
                Descargar PDF
              </button>
            )}
          </div>

          {viewMode === 'calendar' && (
            <div className="flex items-center space-x-4">
              <select
                value={selectedLocation}
                onChange={(e) => setSelectedLocation(e.target.value)}
                className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
              >
                <option value="">Todas las ubicaciones</option>
                {locations.map(location => (
                  <option key={location.id} value={location.id}>
                    {location.name} ({location.company?.name})
                  </option>
                ))}
              </select>

              <div className="flex items-center space-x-2">
                <button
                  onClick={() => {
                    if (currentMonth === 0) {
                      setCurrentMonth(11);
                      setCurrentYear(prev => prev - 1);
                    } else {
                      setCurrentMonth(prev => prev - 1);
                    }
                  }}
                  className="px-3 py-1 bg-gray-100 hover:bg-gray-200 rounded"
                >
                  ‹
                </button>
                <span className="font-medium">
                  {new Date(currentYear, currentMonth).toLocaleDateString('es-ES', { month: 'long', year: 'numeric' })}
                </span>
                <button
                  onClick={() => {
                    if (currentMonth === 11) {
                      setCurrentMonth(0);
                      setCurrentYear(prev => prev + 1);
                    } else {
                      setCurrentMonth(prev => prev + 1);
                    }
                  }}
                  className="px-3 py-1 bg-gray-100 hover:bg-gray-200 rounded"
                >
                  ›
                </button>
              </div>
            </div>
          )}
        </div>

        {viewMode === 'table' ? (
          <DataTable
            title=""
            icon=""
            columns={columns}
            data={shifts}
            loading={loading}
            onAdd={handleAdd}
            onEdit={handleEdit}
            onDelete={handleDelete}
            addButtonText="Agregar Turno"
            searchPlaceholder="Buscar turnos..."
            emptyMessage="No hay turnos registrados en el sistema"
          />
        ) : (
          <div className="bg-white">
            <div ref={calendarRef} className="grid grid-cols-7 gap-2 border border-gray-300 rounded-lg overflow-hidden shadow-lg">
              {['Domingo', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'].map(day => (
                <div key={day} className="p-4 bg-gradient-to-b from-blue-600 to-blue-700 border-b border-blue-800 text-center font-bold text-white shadow-md">
                  {day}
                </div>
              ))}
              {renderCalendar()}
            </div>
          </div>
        )}
      </div>

      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editingShift ? 'Editar Turno' : 'Agregar Turno'}
        size="md"
      >
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label htmlFor="date" className="block text-sm font-medium text-gray-700 mb-2">
              Fecha
            </label>
            <input
              type="date"
              id="date"
              value={formData.date}
              onChange={(e) => {
                setFormData({...formData, date: e.target.value});
                handleDateChange(e.target.value);
              }}
              required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
            />
            {/* Advertencia cuando ya existe un turno para esta fecha */}
            {checkingShift && (
              <div className="mt-2 p-3 bg-blue-50 border border-blue-300 rounded-lg flex items-center">
                <svg className="animate-spin h-5 w-5 text-blue-500 mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                <span className="text-sm text-blue-800 font-medium">
                  Verificando turnos existentes...
                </span>
              </div>
            )}
            {existingShiftWarning && !checkingShift && (
              <div className="mt-2 p-3 bg-amber-50 border border-amber-300 rounded-lg">
                <div className="flex items-center">
                  <svg className="w-5 h-5 text-amber-500 mr-2" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                  </svg>
                  <span className="text-sm text-amber-800 font-medium">
                    {existingShiftWarning.message}
                  </span>
                </div>
              </div>
            )}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label htmlFor="employeeId" className="block text-sm font-medium text-gray-700 mb-2">
                Empleado
              </label>
              <select
                id="employeeId"
                value={formData.employeeId}
                onChange={(e) => handleEmployeeChange(e.target.value)}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
              >
                <option value="">Seleccionar empleado</option>
                {employees.map(employee => (
                  <option key={employee.id} value={employee.id}>
                    {employee.firstName} {employee.lastName}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label htmlFor="locationId" className="block text-sm font-medium text-gray-700 mb-2">
                Ubicación
              </label>
              <select
                id="locationId"
                value={formData.locationId}
                onChange={(e) => setFormData({...formData, locationId: e.target.value})}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
              >
                <option value="">Seleccionar ubicación</option>
                {locations.map(location => (
                  <option key={location.id} value={location.id}>
                    {location.name} ({location.company?.name})
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <label htmlFor="shiftTypeId" className="block text-sm font-medium text-gray-700 mb-2">
              Tipo de Turno
            </label>
            <select
              id="shiftTypeId"
              value={formData.shiftTypeId}
              onChange={(e) => setFormData({...formData, shiftTypeId: e.target.value})}
              required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
            >
              <option value="">Seleccionar tipo de turno</option>
              {shiftTypes.map(shiftType => (
                <option key={shiftType.id} value={shiftType.id}>
                  {shiftType.name} ({shiftType.startTime} - {shiftType.endTime})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label htmlFor="notes" className="block text-sm font-medium text-gray-700 mb-2">
              Notas
            </label>
            <textarea
              id="notes"
              value={formData.notes}
              onChange={(e) => setFormData({...formData, notes: e.target.value})}
              rows={3}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
              placeholder="Notas adicionales del turno"
            />
          </div>

          {/* Error message when submission is blocked */}
          {formError && (
            <div className="p-4 bg-red-50 border border-red-300 rounded-lg">
              <div className="flex items-center">
                <svg className="w-5 h-5 text-red-500 mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                </svg>
                <span className="text-sm text-red-800 font-medium">
                  {formError}
                </span>
              </div>
            </div>
          )}

          <div className="flex justify-end space-x-3 pt-4">
            <button
              type="button"
              onClick={() => {
                setModalOpen(false);
                setFormError(null);
                setExistingShiftWarning(null);
              }}
              className="px-6 py-3 text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors duration-200 font-medium"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={submitting}
              className={`px-6 py-3 bg-gradient-to-r from-purple-500 to-purple-600 hover:from-purple-600 hover:to-purple-700 text-white rounded-lg transition-all duration-200 font-medium shadow-lg hover:shadow-xl ${submitting ? 'opacity-50 cursor-not-allowed' : ''}`}
            >
              {submitting ? 'Creando...' : editingShift ? 'Actualizar' : 'Crear'} Turno
            </button>
          </div>
        </form>
      </Modal>

      {/* Bulk Shift Creation Modal */}
      <Modal
        isOpen={bulkModalOpen}
        onClose={() => setBulkModalOpen(false)}
        title="Crear Turnos en Serie"
        size="lg"
      >
        <form onSubmit={handleBulkSubmit} className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label htmlFor="startDate" className="block text-sm font-medium text-gray-700 mb-2">
                Fecha Inicio *
              </label>
              <input
                type="date"
                id="startDate"
                value={bulkFormData.startDate}
                onChange={(e) => setBulkFormData({...bulkFormData, startDate: e.target.value})}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
              />
            </div>
            <div>
              <label htmlFor="endDate" className="block text-sm font-medium text-gray-700 mb-2">
                Fecha Fin *
              </label>
              <input
                type="date"
                id="endDate"
                value={bulkFormData.endDate}
                onChange={(e) => setBulkFormData({...bulkFormData, endDate: e.target.value})}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label htmlFor="bulkEmployeeId" className="block text-sm font-medium text-gray-700 mb-2">
                Empleado *
              </label>
              <select
                id="bulkEmployeeId"
                value={bulkFormData.employeeId}
                onChange={(e) => setBulkFormData({...bulkFormData, employeeId: e.target.value})}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
              >
                <option value="">Seleccionar empleado</option>
                {employees.map(employee => (
                  <option key={employee.id} value={employee.id}>
                    {employee.firstName} {employee.lastName}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label htmlFor="bulkLocationId" className="block text-sm font-medium text-gray-700 mb-2">
                Ubicación *
              </label>
              <select
                id="bulkLocationId"
                value={bulkFormData.locationId}
                onChange={(e) => setBulkFormData({...bulkFormData, locationId: e.target.value})}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
              >
                <option value="">Seleccionar ubicación</option>
                {locations.map(location => (
                  <option key={location.id} value={location.id}>
                    {location.name} ({location.company?.name})
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <label htmlFor="bulkShiftTypeId" className="block text-sm font-medium text-gray-700 mb-2">
              Tipo de Turno *
            </label>
            <select
              id="bulkShiftTypeId"
              value={bulkFormData.shiftTypeId}
              onChange={(e) => setBulkFormData({...bulkFormData, shiftTypeId: e.target.value})}
              required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
            >
              <option value="">Seleccionar tipo de turno</option>
              {shiftTypes.map(shiftType => (
                <option key={shiftType.id} value={shiftType.id}>
                  {shiftType.name} ({shiftType.startTime} - {shiftType.endTime})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label htmlFor="bulkNotes" className="block text-sm font-medium text-gray-700 mb-2">
              Notas
            </label>
            <textarea
              id="bulkNotes"
              value={bulkFormData.notes}
              onChange={(e) => setBulkFormData({...bulkFormData, notes: e.target.value})}
              rows={3}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
              placeholder="Notas adicionales para todos los turnos"
            />
          </div>

          {/* Preview of shifts to be created */}
          {bulkFormData.startDate && bulkFormData.endDate && (
            <div className="bg-purple-50 rounded-lg p-4 border border-purple-200">
              <h4 className="font-medium text-purple-900 mb-2">
                Vista Previa de Turnos a Crear
              </h4>
              <div className="text-sm text-purple-700">
                {getBulkShiftsPreview().length} turnos serán creados
              </div>
              <div className="mt-2 max-h-32 overflow-y-auto text-xs text-purple-600">
                {getBulkShiftsPreview().slice(0, 10).map((shift, idx) => (
                  <div key={idx} className="py-1">
                    {shift.date}
                  </div>
                ))}
                {getBulkShiftsPreview().length > 10 && (
                  <div className="py-1 italic">
                    ... y {getBulkShiftsPreview().length - 10} más
                  </div>
                )}
              </div>
            </div>
          )}

          <div className="flex justify-end space-x-3 pt-4">
            <button
              type="button"
              onClick={() => setBulkModalOpen(false)}
              className="px-6 py-3 text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors duration-200 font-medium"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="px-6 py-3 bg-gradient-to-r from-purple-500 to-purple-600 hover:from-purple-600 hover:to-purple-700 text-white rounded-lg transition-all duration-200 font-medium shadow-lg hover:shadow-xl"
            >
              Crear {getBulkShiftsPreview().length > 0 ? getBulkShiftsPreview().length : ''} Turnos
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Shifts;