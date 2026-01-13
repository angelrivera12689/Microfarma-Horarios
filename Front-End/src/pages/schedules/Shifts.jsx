import { useState, useEffect, useRef } from 'react';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import shiftService from '../../services/shiftService';
import shiftChangeRequestService from '../../services/shiftChangeRequestService';
import employeeService from '../../services/employeeService';
import locationService from '../../services/locationService';
import shiftTypeService from '../../services/shiftTypeService';

const Shifts = () => {
  const [shifts, setShifts] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [locations, setLocations] = useState([]);
  const [shiftTypes, setShiftTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingShift, setEditingShift] = useState(null);
  const [viewMode, setViewMode] = useState('table'); // 'table', 'calendar', or 'requests'
  const [selectedLocation, setSelectedLocation] = useState('');
  const [currentMonth, setCurrentMonth] = useState(new Date().getMonth());
  const [currentYear, setCurrentYear] = useState(new Date().getFullYear());
  const calendarRef = useRef();

  // Shift change requests state
  const [shiftChangeRequests, setShiftChangeRequests] = useState([]);
  const [loadingRequests, setLoadingRequests] = useState(false);
  const [formData, setFormData] = useState({
    date: '',
    employeeId: '',
    locationId: '',
    shiftTypeId: '',
    notes: ''
  });

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
        setShifts(Array.isArray(response.data) ? response.data : []);
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
        setEmployees(Array.isArray(response.data) ? response.data : []);
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
        setShiftTypes(Array.isArray(response.data) ? response.data : []);
      } else {
        console.error('Failed to load shift types:', response.message);
      }
    } catch (error) {
      console.error('Error loading shift types:', error);
    }
  };

  const loadShiftChangeRequests = async () => {
    try {
      setLoadingRequests(true);
      const response = await shiftChangeRequestService.getPendingRequests();
      if (response.data) {
        setShiftChangeRequests(Array.isArray(response.data) ? response.data : []);
      } else {
        console.error('Failed to load shift change requests:', response.message);
      }
    } catch (error) {
      console.error('Error loading shift change requests:', error);
    } finally {
      setLoadingRequests(false);
    }
  };

  const handleDecideRequest = async (requestId, approved, decision) => {
    try {
      const decisionData = {
        approved: approved,
        adminDecision: decision
      };

      const response = await shiftChangeRequestService.decideRequest(requestId, decisionData);
      if (response.status) {
        alert(approved ? 'Solicitud aprobada exitosamente' : 'Solicitud rechazada exitosamente');
        await loadShiftChangeRequests();
      } else {
        alert('Error al procesar la solicitud: ' + (response.message || 'Error desconocido'));
      }
    } catch (error) {
      console.error('Error deciding request:', error);
      alert('Error de conexión al procesar la solicitud');
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
    if (window.confirm(`¿Estás seguro de que quieres eliminar este turno?`)) {
      try {
        await shiftService.deleteShift(shift.id);
        await loadShifts();
      } catch (error) {
        console.error('Error deleting shift:', error);
        alert('Error al eliminar el turno');
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
    { key: 'employee', header: 'Empleado', render: (value) => value ? `${value.firstName} ${value.lastName}` : 'Sin empleado' },
    { key: 'location', header: 'Ubicación', render: (value) => value?.name || 'Sin ubicación' },
    { key: 'shiftType', header: 'Tipo de Turno', render: (value) => value?.name || 'Sin tipo' },
    { key: 'notes', header: 'Notas' }
  ];

  const filteredShifts = selectedLocation
    ? shifts.filter(shift => shift.location?.id == selectedLocation)
    : shifts;

  const exportToCSV = () => {
    const dataToExport = viewMode === 'calendar' ? filteredShifts : shifts;
    const headers = ['Fecha', 'Empleado', 'Ubicación', 'Tipo de Turno', 'Notas'];
    const csvContent = [
      headers.join(','),
      ...dataToExport.map(shift => [
        shift.date || '',
        shift.employee ? `${shift.employee.firstName} ${shift.employee.lastName}` : 'Sin empleado',
        shift.location?.name || 'Sin ubicación',
        shift.shiftType?.name || 'Sin tipo',
        shift.notes || ''
      ].map(field => `"${field.replace(/"/g, '""')}"`).join(','))
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', 'turnos.csv');
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
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
              onClick={() => {
                setViewMode('requests');
                loadShiftChangeRequests();
              }}
              className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                viewMode === 'requests'
                  ? 'bg-orange-500 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              Solicitudes de Cambio
            </button>
            <button
              onClick={exportToCSV}
              className="px-4 py-2 bg-green-500 text-white rounded-lg font-medium hover:bg-green-600 transition-colors"
            >
              Descargar CSV
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
        ) : viewMode === 'calendar' ? (
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
        ) : (
          <div className="bg-white rounded-lg shadow-sm border border-gray-200">
            <div className="p-6">
              <h2 className="text-xl font-bold text-gray-900 mb-4">Solicitudes de Cambio de Turno</h2>

              {loadingRequests ? (
                <div className="text-center py-8">
                  <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-orange-500 mx-auto"></div>
                  <p className="mt-4 text-gray-600">Cargando solicitudes...</p>
                </div>
              ) : shiftChangeRequests.length === 0 ? (
                <div className="text-center py-8 text-gray-500">
                  No hay solicitudes pendientes
                </div>
              ) : (
                <div className="space-y-4">
                  {shiftChangeRequests.map((request) => (
                    <div key={request.id} className="border border-gray-200 rounded-lg p-4 bg-gray-50">
                      <div className="flex justify-between items-start mb-3">
                        <div>
                          <h3 className="font-semibold text-gray-900">
                            {request.employeeName}
                          </h3>
                          <p className="text-sm text-gray-600">
                            Solicitado el {new Date(request.createdAt).toLocaleDateString('es-ES')}
                          </p>
                        </div>
                        <div className="flex space-x-2">
                          <button
                            onClick={() => {
                              const decision = prompt('Comentarios para aprobar la solicitud:');
                              if (decision !== null) {
                                handleDecideRequest(request.id, true, decision);
                              }
                            }}
                            className="px-3 py-1 bg-green-500 text-white text-sm rounded hover:bg-green-600"
                          >
                            ✅ Aprobar
                          </button>
                          <button
                            onClick={() => {
                              const decision = prompt('Razón del rechazo:');
                              if (decision !== null) {
                                handleDecideRequest(request.id, false, decision);
                              }
                            }}
                            className="px-3 py-1 bg-red-500 text-white text-sm rounded hover:bg-red-600"
                          >
                            ❌ Rechazar
                          </button>
                        </div>
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-3">
                        <div>
                          <h4 className="font-medium text-gray-900 mb-1">Turno Actual</h4>
                          <div className="text-sm text-gray-600 bg-white p-2 rounded">
                            <p><strong>Fecha:</strong> {request.originalDate ? request.originalDate.split('-').reverse().join('/') : ''}</p>
                            <p><strong>Tipo:</strong> {request.originalShiftType}</p>
                            <p><strong>Ubicación:</strong> {request.originalLocation}</p>
                          </div>
                        </div>

                        {(request.requestedDate || request.requestedShiftType || request.requestedLocation) && (
                          <div>
                            <h4 className="font-medium text-gray-900 mb-1">Cambios Solicitados</h4>
                            <div className="text-sm text-gray-600 bg-white p-2 rounded">
                              {request.requestedDate && <p><strong>Fecha:</strong> {request.requestedDate.split('-').reverse().join('/')}</p>}
                              {request.requestedShiftType && <p><strong>Tipo:</strong> {request.requestedShiftType}</p>}
                              {request.requestedLocation && <p><strong>Ubicación:</strong> {request.requestedLocation}</p>}
                            </div>
                          </div>
                        )}
                      </div>

                      <div>
                        <h4 className="font-medium text-gray-900 mb-1">Razón</h4>
                        <p className="text-sm text-gray-600 bg-white p-2 rounded">{request.reason}</p>
                      </div>
                    </div>
                  ))}
                </div>
              )}
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
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-colors duration-200"
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label htmlFor="employeeId" className="block text-sm font-medium text-gray-700 mb-2">
                Empleado
              </label>
              <select
                id="employeeId"
                value={formData.employeeId}
                onChange={(e) => setFormData({...formData, employeeId: e.target.value})}
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
              {editingShift ? 'Actualizar' : 'Crear'} Turno
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Shifts;