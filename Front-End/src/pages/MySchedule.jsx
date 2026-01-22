import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import shiftService from '../services/shiftService';
import authService from '../services/authService';

const MySchedule = () => {
  const user = authService.getCurrentUser();
  const navigate = useNavigate();
  const [shifts, setShifts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [viewMode, setViewMode] = useState('list'); // 'list' or 'calendar'
  const [currentMonth, setCurrentMonth] = useState(new Date().getMonth());
  const [currentYear, setCurrentYear] = useState(new Date().getFullYear());
  const [filter, setFilter] = useState('all'); // 'all', 'upcoming', 'past'

  useEffect(() => {
    loadMyShifts();
  }, []);

  const loadMyShifts = async () => {
    try {
      const response = await shiftService.getMyShifts();
      if (response.status) {
        setShifts(Array.isArray(response.data) ? response.data : []);
        setError(null);
      } else {
        console.error('Failed to load shifts:', response);
        setError(response.message || 'Error desconocido al cargar turnos');
      }
    } catch (error) {
      console.error('Error loading shifts:', error);
      setError('Error de conexión al cargar turnos');
    } finally {
      setLoading(false);
    }
  };

  const filteredShifts = () => {
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());

    switch (filter) {
      case 'upcoming':
        return shifts.filter(shift => {
          const [year, month, day] = shift.date.split('-');
          const shiftDate = new Date(year, month - 1, day);
          return shiftDate >= today;
        });
      case 'past':
        return shifts.filter(shift => {
          const [year, month, day] = shift.date.split('-');
          const shiftDate = new Date(year, month - 1, day);
          return shiftDate < today;
        });
      default:
        return shifts;
    }
  };

  const sortedShifts = () => {
    return filteredShifts().sort((a, b) => {
      const [yearA, monthA, dayA] = a.date.split('-');
      const [yearB, monthB, dayB] = b.date.split('-');
      const dateA = new Date(yearA, monthA - 1, dayA);
      const dateB = new Date(yearB, monthB - 1, dayB);
      return dateA - dateB;
    });
  };

  const calculateDuration = (startTime, endTime) => {
    if (!startTime || !endTime) return 'N/A';
    const start = new Date(`1970-01-01T${startTime}`);
    let end = new Date(`1970-01-01T${endTime}`);

    // If end time is before start time, it means the shift spans midnight
    if (end < start) {
      end.setDate(end.getDate() + 1); // Add one day
    }

    const diff = end - start;
    const hours = Math.floor(diff / (1000 * 60 * 60));
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
    return `${hours}h ${minutes}m`;
  };

  const renderCalendar = () => {
    const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
    const firstDayOfMonth = new Date(currentYear, currentMonth, 1).getDay();
    const days = [];
    const monthShifts = sortedShifts().filter(shift => {
      const [year, month, day] = shift.date.split('-');
      const shiftDate = new Date(year, month - 1, day);
      return shiftDate.getMonth() === currentMonth && shiftDate.getFullYear() === currentYear;
    });

    // Empty cells for days before the first day of the month
    for (let i = 0; i < firstDayOfMonth; i++) {
      days.push(<div key={`empty-${i}`} className="h-24 border border-gray-200 bg-gray-50"></div>);
    }

    // Days of the month
    for (let day = 1; day <= daysInMonth; day++) {
      const dayShifts = monthShifts.filter(shift => {
        const [year, month, dayShift] = shift.date.split('-');
        const shiftDate = new Date(year, month - 1, dayShift);
        return shiftDate.getFullYear() === currentYear && shiftDate.getMonth() === currentMonth && shiftDate.getDate() === day;
      });

      days.push(
        <div key={day} className="min-h-32 border border-gray-300 p-2 bg-white hover:bg-gray-50 transition-colors overflow-y-auto">
          <div className="text-lg font-bold text-gray-800 mb-2">{day}</div>
          <div className="space-y-1">
            {dayShifts.length === 0 ? (
              <div className="text-sm text-gray-400 italic">Sin turnos</div>
            ) : (
              dayShifts.map((shift) => (
                <div key={shift.id} className="text-xs bg-blue-100 text-blue-900 rounded p-1 border border-blue-200">
                  <div className="font-semibold truncate">
                    {shift.shiftType?.name}
                  </div>
                  <div className="text-blue-700">
                    {shift.shiftType?.startTime?.slice(0, 5)} - {shift.shiftType?.endTime?.slice(0, 5)}
                  </div>
                  <div className="text-blue-600 text-xs">
                    {shift.location?.name}
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

  const exportToPDF = async () => {
    try {
      await shiftService.downloadMyShiftsPdf();
    } catch (error) {
      console.error('Error downloading PDF:', error);
      alert('Error al descargar el PDF. Inténtalo de nuevo.');
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-green-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center justify-between w-full">
              <div className="flex items-center space-x-3">
                <div className="w-10 h-10 bg-gradient-to-br from-green-500 to-green-600 rounded-xl flex items-center justify-center shadow-lg">
                  <span className="text-white font-bold text-lg">📅</span>
                </div>
                <div>
                  <h1 className="text-xl font-bold text-gray-900">Mis Horarios</h1>
                  <p className="text-sm text-gray-500">Gestiona tus turnos de trabajo</p>
                </div>
              </div>

            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-6 py-8">
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          {/* Controls */}
          <div className="flex flex-col lg:flex-row gap-4 items-start lg:items-center mb-6">
            <div className="flex flex-wrap gap-2 w-full lg:w-auto">
              <button
                onClick={() => setViewMode('list')}
                className={`px-3 py-2 text-sm md:px-4 md:py-2 md:text-base rounded-lg font-medium transition-colors ${
                  viewMode === 'list'
                    ? 'bg-blue-500 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                <span className="hidden sm:inline">Vista Lista</span>
                <span className="sm:hidden">Lista</span>
              </button>
              <button
                onClick={() => setViewMode('calendar')}
                className={`px-3 py-2 text-sm md:px-4 md:py-2 md:text-base rounded-lg font-medium transition-colors ${
                  viewMode === 'calendar'
                    ? 'bg-blue-500 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                <span className="hidden sm:inline">Vista Calendario</span>
                <span className="sm:hidden">Calendario</span>
              </button>
            </div>

            <div className="flex flex-col sm:flex-row gap-2 w-full lg:w-auto">
              <select
                value={filter}
                onChange={(e) => setFilter(e.target.value)}
                className="px-3 py-2 text-sm md:px-4 md:py-2 md:text-base border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 w-full sm:w-auto"
              >
                <option value="all">Todos los turnos</option>
                <option value="upcoming">Próximos turnos</option>
                <option value="past">Turnos pasados</option>
              </select>

              <button
                onClick={exportToPDF}
                className="px-3 py-2 text-sm md:px-4 md:py-2 md:text-base bg-green-500 text-white rounded-lg font-medium hover:bg-green-600 transition-colors w-full sm:w-auto"
              >
                <span className="hidden sm:inline">Exportar PDF</span>
                <span className="sm:hidden">PDF</span>
              </button>
            </div>

            {viewMode === 'calendar' && (
              <div className="flex items-center space-x-2 w-full lg:w-auto justify-center lg:justify-start">
                <button
                  onClick={() => {
                    if (currentMonth === 0) {
                      setCurrentMonth(11);
                      setCurrentYear(prev => prev - 1);
                    } else {
                      setCurrentMonth(prev => prev - 1);
                    }
                  }}
                  className="px-3 py-1 bg-gray-100 hover:bg-gray-200 rounded text-lg"
                >
                  ‹
                </button>
                <span className="font-medium text-sm md:text-base text-center min-w-0 flex-1 lg:flex-none">
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
                  className="px-3 py-1 bg-gray-100 hover:bg-gray-200 rounded text-lg"
                >
                  ›
                </button>
              </div>
            )}
          </div>

          {loading ? (
            <div className="text-center py-8">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
              <p className="mt-4 text-gray-600">Cargando tus turnos...</p>
            </div>
          ) : error ? (
            <div className="text-center py-8 text-red-600">
              <p>{error}</p>
            </div>
          ) : viewMode === 'list' ? (
            <div className="space-y-4">
              {sortedShifts().length === 0 ? (
                <div className="text-center py-8 text-gray-500">
                  No tienes turnos {filter === 'upcoming' ? 'próximos' : filter === 'past' ? 'pasados' : ''} asignados
                </div>
              ) : (
                sortedShifts().map((shift) => (
                  <div key={shift.id} className="flex flex-col sm:flex-row sm:items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors gap-2 sm:gap-0">
                    <div className="flex-1">
                      <p className="font-semibold text-gray-900 text-sm md:text-base">
                        {(() => {
                          const [year, month, day] = shift.date.split('-');
                          const date = new Date(year, month - 1, day);
                          return date.toLocaleDateString('es-ES', {
                            weekday: 'long',
                            year: 'numeric',
                            month: 'long',
                            day: 'numeric'
                          });
                        })()}
                      </p>
                      <p className="text-sm text-gray-600">
                        {shift.shiftType?.name} - {shift.location?.name}
                      </p>
                      <p className="text-sm text-gray-500">
                        {shift.shiftType?.startTime?.substring(0, 5)} - {shift.shiftType?.endTime?.substring(0, 5)}
                        ({calculateDuration(shift.shiftType?.startTime, shift.shiftType?.endTime)})
                      </p>
                      {shift.notes && (
                        <p className="text-sm text-gray-500 italic">Nota: {shift.notes}</p>
                      )}
                    </div>
                    <div className="text-left sm:text-right">
                      <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        (() => {
                          const [year, month, day] = shift.date.split('-');
                          const shiftDate = new Date(year, month - 1, day);
                          const today = new Date();
                          today.setHours(0, 0, 0, 0);
                          return shiftDate >= today;
                        })() ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                      }`}>
                        {(() => {
                          const [year, month, day] = shift.date.split('-');
                          const shiftDate = new Date(year, month - 1, day);
                          const today = new Date();
                          today.setHours(0, 0, 0, 0);
                          return shiftDate >= today ? 'Próximo' : 'Pasado';
                        })()}
                      </span>
                    </div>
                  </div>
                ))
              )}
            </div>
          ) : (
            <div className="grid grid-cols-7 gap-1 border border-gray-300 rounded-lg overflow-x-auto">
              {['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'].map(day => (
                <div key={day} className="p-2 bg-blue-600 text-center font-bold text-white text-sm md:text-base">
                  <span className="hidden sm:inline">{day}</span>
                  <span className="sm:hidden">{day.substring(0, 3)}</span>
                </div>
              ))}
              {renderCalendar()}
            </div>
          )}
        </div>
      </main>
    </div>
  );
};

export default MySchedule;