import { useState, useEffect, useRef } from 'react';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import shiftService from '../../services/shiftService';
import employeeService from '../../services/employeeService';
import locationService from '../../services/locationService';
import shiftTypeService from '../../services/shiftTypeService';
import { exportShiftsToExcel } from '../../services/excelExportService';

const DeliveryShifts = () => {
  const [shifts, setShifts] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [locations, setLocations] = useState([]);
  const [shiftTypes, setShiftTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [bulkModalOpen, setBulkModalOpen] = useState(false);
  const [editingShift, setEditingShift] = useState(null);
  const [viewMode, setViewMode] = useState('table');
  const [selectedLocation, setSelectedLocation] = useState('');
  const [currentMonth, setCurrentMonth] = useState(new Date().getMonth());
  const [currentYear, setCurrentYear] = useState(new Date().getFullYear());
  const calendarRef = useRef();
  const [formData, setFormData] = useState({
    date: '',
    employeeId: '',
    locationId: '',
    shiftTypeId: '',
    notes: ''
  });

  const [bulkFormData, setBulkFormData] = useState({
    startDate: '',
    endDate: '',
    employeeId: '',
    locationId: '',
    shiftTypeId: '',
    notes: ''
  });

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
        // Filtrar solo turnos de domiciliarios (empleados con posición Domiciliario)
        const deliveryShifts = allShifts.filter(shift => 
          shift.employee?.position?.name === 'Domiciliario'
        );
        setShifts(deliveryShifts);
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
        const allEmployees = Array.isArray(response.data) ? response.data : [];
        // Solo mostrar empleados con posición Domiciliario (insensible a mayúsculas)
        const deliveryEmployees = allEmployees.filter(emp => 
          emp.position?.name && emp.position.name.toLowerCase().includes('domicili')
        );
        console.log('Cargando empleados Domiciliarios:', deliveryEmployees.length);
        console.log('Posiciones disponibles:', [...new Set(allEmployees.map(e => e.position?.name).filter(Boolean))]);
        setEmployees(deliveryEmployees);
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
        const allTypes = Array.isArray(response.data) ? response.data : [];
        // Solo mostrar tipos de turno de domiciliarios
        const deliveryTypes = allTypes.filter(type => 
          type.name?.includes('Domiciliario')
        );
        setShiftTypes(deliveryTypes);
      } else {
        console.error('Failed to load shift types:', response.message);
      }
    } catch (error) {
      console.error('Error loading shift types:', error);
    }
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
      alert('Error al guardar el turno');
    }
  };

  const columns = [
    { key: 'date', header: 'Fecha', render: (value) => value ? value.split('-').reverse().join('/') : '' },
    { key: 'employee', header: 'Domiciliario', render: (value) => value ? `${value.firstName} ${value.lastName}` : 'Sin empleado' },
    { key: 'location', header: 'Ubicación', render: (value) => value?.name || 'Sin ubicación' },
    { key: 'shiftType', header: 'Tipo de Turno', render: (value) => value?.name || 'Sin tipo' },
    { key: 'notes', header: 'Notas' }
  ];

  const filteredShifts = selectedLocation
    ? shifts.filter(shift => shift.location?.id == selectedLocation)
    : shifts;

  const exportToExcel = () => {
    const dataToExport = viewMode === 'calendar' ? filteredShifts : shifts;
    exportShiftsToExcel(dataToExport, 'turnos_domiciliarios');
  };

  const renderCalendar = () => {
    const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
    const firstDayOfMonth = new Date(currentYear, currentMonth, 1).getDay();
    const days = [];

    for (let i = 0; i < firstDayOfMonth; i++) {
      days.push(<div key={`empty-${i}`} className="h-24 border border-gray-200 bg-gray-50"></div>);
    }

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
                <div key={shift.id} className="text-xs bg-gradient-to-r from-green-100 to-green-200 text-green-900 rounded-md px-2 py-1.5 border border-green-300 shadow-sm">
                  <div className="font-semibold truncate text-green-950">
                    {shift.employee?.firstName} {shift.employee?.lastName}
                  </div>
                  <div className="text-green-800 truncate font-medium">
                    {shift.shiftType?.name}
                  </div>
                  <div className="text-green-700 text-xs font-mono">
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
          <h1 className="text-3xl font-bold text-gray-900">🛵 Turnos Domiciliarios</h1>
          <p className="text-gray-600 mt-1">Administra los turnos de los domiciliarios</p>
        </div>
      </div>

      <div className="bg-white rounded-2xl shadow-xl p-6 border border-gray-100">
        <div className="flex flex-wrap gap-4 items-center mb-4">
          <div className="flex space-x-2">
            <button
              onClick={() => setViewMode('table')}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                viewMode === 'table'
                  ? 'bg-green-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              Vista Tabla
            </button>
            <button
              onClick={() => setViewMode('calendar')}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                viewMode === 'calendar'
                  ? 'bg-green-600 text-white'
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
          </div>

          {viewMode === 'calendar' && (
            <div className="flex items-center space-x-4">
              <select
                value={selectedLocation}
                onChange={(e) => setSelectedLocation(e.target.value)}
                className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-600 focus:border-green-600"
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
            emptyMessage="No hay turnos de domiciliarios registrados"
          />
        ) : (
          <div className="bg-white">
            <div ref={calendarRef} className="grid grid-cols-7 gap-2 border border-gray-300 rounded-lg overflow-hidden shadow-lg">
              {['Domingo', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'].map(day => (
                <div key={day} className="p-4 bg-gradient-to-b from-green-600 to-green-700 border-b border-green-800 text-center font-bold text-white shadow-md">
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
              onChange={(e) => setFormData({...formData, date: e.target.value})}
              required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-600 focus:border-green-600 transition-colors duration-200"
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label htmlFor="employeeId" className="block text-sm font-medium text-gray-700 mb-2">
                Domiciliario
              </label>
              <select
                id="employeeId"
                value={formData.employeeId}
                onChange={(e) => setFormData({...formData, employeeId: e.target.value})}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-600 focus:border-green-600 transition-colors duration-200"
              >
                <option value="">Seleccionar domiciliarios</option>
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
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-600 focus:border-green-600 transition-colors duration-200"
              >
                <option value="">Seleccionar ubicación</option>
                {locations.map(location => (
                  <option key={location.id} value={location.id}>
                    {location.name}
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
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-600 focus:border-green-600 transition-colors duration-200"
            >
              <option value="">Seleccionar tipo de turno</option>
              {shiftTypes.map(shiftType => (
                <option key={shiftType.id} value={shiftType.id}>
                  {shiftType.name} ({shiftType.startTime?.slice(0, 5)} - {shiftType.endTime?.slice(0, 5)})
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
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-600 focus:border-green-600 transition-colors duration-200"
            />
          </div>

          <div className="flex justify-end space-x-3">
            <button
              type="button"
              onClick={() => setModalOpen(false)}
              className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
            >
              {editingShift ? 'Actualizar' : 'Crear'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Bulk Create Modal */}
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
                Fecha Inicio
              </label>
              <input
                type="date"
                id="startDate"
                value={bulkFormData.startDate}
                onChange={(e) => setBulkFormData({...bulkFormData, startDate: e.target.value})}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-600"
              />
            </div>
            <div>
              <label htmlFor="endDate" className="block text-sm font-medium text-gray-700 mb-2">
                Fecha Fin
              </label>
              <input
                type="date"
                id="endDate"
                value={bulkFormData.endDate}
                onChange={(e) => setBulkFormData({...bulkFormData, endDate: e.target.value})}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-600"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label htmlFor="bulkEmployeeId" className="block text-sm font-medium text-gray-700 mb-2">
                Domiciliario
              </label>
              <select
                id="bulkEmployeeId"
                value={bulkFormData.employeeId}
                onChange={(e) => setBulkFormData({...bulkFormData, employeeId: e.target.value})}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-600"
              >
                <option value="">Seleccionar domiciliarios</option>
                {employees.map(employee => (
                  <option key={employee.id} value={employee.id}>
                    {employee.firstName} {employee.lastName}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label htmlFor="bulkLocationId" className="block text-sm font-medium text-gray-700 mb-2">
                Ubicación
              </label>
              <select
                id="bulkLocationId"
                value={bulkFormData.locationId}
                onChange={(e) => setBulkFormData({...bulkFormData, locationId: e.target.value})}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-600"
              >
                <option value="">Seleccionar ubicación</option>
                {locations.map(location => (
                  <option key={location.id} value={location.id}>
                    {location.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <label htmlFor="bulkShiftTypeId" className="block text-sm font-medium text-gray-700 mb-2">
              Tipo de Turno
            </label>
            <select
              id="bulkShiftTypeId"
              value={bulkFormData.shiftTypeId}
              onChange={(e) => setBulkFormData({...bulkFormData, shiftTypeId: e.target.value})}
              required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-600"
            >
              <option value="">Seleccionar tipo de turno</option>
              {shiftTypes.map(shiftType => (
                <option key={shiftType.id} value={shiftType.id}>
                  {shiftType.name} ({shiftType.startTime?.slice(0, 5)} - {shiftType.endTime?.slice(0, 5)})
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
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-600"
            />
          </div>

          {getBulkShiftsPreview().length > 0 && (
            <div className="bg-gray-50 p-4 rounded-lg">
              <p className="text-sm font-medium text-gray-700 mb-2">
                Se crearán {getBulkShiftsPreview().length} turnos
              </p>
              <div className="text-xs text-gray-500 max-h-32 overflow-y-auto">
                {getBulkShiftsPreview().map((shift, idx) => (
                  <div key={idx}>{shift.date}</div>
                ))}
              </div>
            </div>
          )}

          <div className="flex justify-end space-x-3">
            <button
              type="button"
              onClick={() => setBulkModalOpen(false)}
              className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700"
            >
              Crear Turnos
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default DeliveryShifts;
