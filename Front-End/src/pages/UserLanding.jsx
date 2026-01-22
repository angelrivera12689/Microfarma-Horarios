import { useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import authService from '../services/authService';
import shiftService from '../services/shiftService';

const UserLanding = () => {
  const user = authService.getCurrentUser();
  const navigate = useNavigate();
  const [shifts, setShifts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalShifts: 0,
    upcomingShifts: 0,
    totalHours: 0,
    shiftsByType: {}
  });

  useEffect(() => {
    const fetchShifts = async () => {
      try {
        const response = await shiftService.getMyShifts();
        if (response.status) {
          setShifts(response.data);
          calculateStats(response.data);
        }
      } catch (error) {
        console.error('Error fetching shifts:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchShifts();
  }, []);

  const handleLogout = async () => {
    await authService.logout();
    navigate('/login');
  };

  const handleViewSchedules = () => {
    navigate('/my-schedule');
  };

  const handleGoToDashboard = () => {
    navigate('/dashboard');
  };

  const calculateStats = (shiftList) => {
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());

    let totalShifts = shiftList.length;
    let upcomingShifts = 0;
    let totalHours = 0;
    let shiftsByType = {};

    shiftList.forEach(shift => {
      // Count upcoming shifts
      const [year, month, day] = shift.date.split('-');
      const shiftDate = new Date(year, month - 1, day);
      if (shiftDate >= today) {
        upcomingShifts++;
      }

      // Calculate hours
      if (shift.shiftType?.startTime && shift.shiftType?.endTime) {
        const start = new Date(`1970-01-01T${shift.shiftType.startTime}`);
        let end = new Date(`1970-01-01T${shift.shiftType.endTime}`);
        if (end < start) {
          end.setDate(end.getDate() + 1);
        }
        const diff = end - start;
        const hours = diff / (1000 * 60 * 60);
        totalHours += hours;
      }

      // Count by type
      const typeName = shift.shiftType?.name || 'Sin tipo';
      shiftsByType[typeName] = (shiftsByType[typeName] || 0) + 1;
    });

    setStats({
      totalShifts,
      upcomingShifts,
      totalHours: Math.round(totalHours * 10) / 10, // Round to 1 decimal
      shiftsByType
    });
  };


  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-green-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-gradient-to-br from-green-500 to-green-600 rounded-xl flex items-center justify-center shadow-lg">
                <span className="text-white font-bold text-lg">M</span>
              </div>
              <div>
                <h1 className="text-xl font-bold text-gray-900">Microfarma</h1>
                <p className="text-sm text-gray-500">Portal del Usuario</p>
              </div>
            </div>

            <div className="flex items-center space-x-4">
              <div className="flex items-center space-x-2">
                <div className="w-2 h-2 bg-emerald-400 rounded-full animate-pulse"></div>
                <span className="text-sm text-gray-600 font-medium">Usuario Activo</span>
              </div>


              <div className="flex items-center space-x-2 text-gray-700">
                <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-blue-600 rounded-full flex items-center justify-center text-white font-semibold text-sm">
                  {user?.email.charAt(0).toUpperCase()}
                </div>
                <span className="hidden md:block text-sm font-medium">{user?.email}</span>
              </div>

              <button
                onClick={handleLogout}
                className="flex items-center space-x-2 px-4 py-2 text-red-600 hover:text-red-700 hover:bg-red-50 rounded-lg transition-colors duration-200"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                </svg>
                <span className="hidden md:block text-sm font-medium">Cerrar Sesión</span>
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-6 py-12">
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold text-gray-900 mb-4">
            ¡Bienvenido a Microfarma!
          </h1>
          <p className="text-xl text-gray-600 max-w-2xl mx-auto">
            Tu portal personal para gestionar horarios y acceder a la información de la empresa.
          </p>
        </div>

        {/* Statistics Cards */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
          <div className="bg-white rounded-xl p-4 shadow-sm border border-gray-200 text-center">
            <div className="text-2xl font-bold text-blue-600">{stats.totalShifts}</div>
            <div className="text-sm text-gray-600">Total Turnos</div>
          </div>
          <div className="bg-white rounded-xl p-4 shadow-sm border border-gray-200 text-center">
            <div className="text-2xl font-bold text-green-600">{stats.upcomingShifts}</div>
            <div className="text-sm text-gray-600">Próximos</div>
          </div>
          <div className="bg-white rounded-xl p-4 shadow-sm border border-gray-200 text-center">
            <div className="text-2xl font-bold text-purple-600">{stats.totalHours}h</div>
            <div className="text-sm text-gray-600">Horas Totales</div>
          </div>
          <div className="bg-white rounded-xl p-4 shadow-sm border border-gray-200 text-center">
            <div className="text-2xl font-bold text-orange-600">{Object.keys(stats.shiftsByType).length}</div>
            <div className="text-sm text-gray-600">Tipos de Turno</div>
          </div>
        </div>

        {/* Features Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 mb-12">
          {/* Schedule Card */}
          <div className="bg-white rounded-xl p-8 shadow-sm border border-gray-200 hover:shadow-md transition-shadow duration-200">
            <div className="w-16 h-16 bg-blue-50 rounded-xl flex items-center justify-center mb-6 mx-auto">
              <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-3 text-center">Mis Turnos</h3>
            <p className="text-gray-600 text-center mb-6">
              Consulta tus turnos asignados y planifica tu semana.
            </p>
            <button
              onClick={handleViewSchedules}
              className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 px-4 rounded-lg transition-colors duration-200"
            >
              Ver Turnos Asignados
            </button>
          </div>

          {/* Profile Card */}
          <div className="bg-white rounded-xl p-8 shadow-sm border border-gray-200 hover:shadow-md transition-shadow duration-200">
            <div className="w-16 h-16 bg-green-50 rounded-xl flex items-center justify-center mb-6 mx-auto">
              <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-3 text-center">Mi Perfil</h3>
            <p className="text-gray-600 text-center mb-6">
              Actualiza tu información personal y preferencias de trabajo.
            </p>
            <button
              onClick={() => navigate('/profile')}
              className="w-full bg-green-600 hover:bg-green-700 text-white font-semibold py-3 px-4 rounded-lg transition-colors duration-200"
            >
              Editar Perfil
            </button>
          </div>

          {/* Notifications Card */}
          <div className="bg-white rounded-xl p-8 shadow-sm border border-gray-200 hover:shadow-md transition-shadow duration-200">
            <div className="w-16 h-16 bg-purple-50 rounded-xl flex items-center justify-center mb-6 mx-auto">
              <svg className="w-8 h-8 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-5 5v-5zM4.868 12.683A17.925 17.925 0 0112 21c7.962 0 12-1.21 12-2.683m-12 2.683a17.925 17.925 0 01-7.132-8.317M12 21c4.411 0 8-4.03 8-9s-3.589-9-8-9-8 4.03-8 9a9.06 9.06 0 001.832 5.445L4 21l4.868-2.317z" />
              </svg>
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-3 text-center">Notificaciones</h3>
            <p className="text-gray-600 text-center mb-6">
              Recibe actualizaciones importantes sobre cambios en horarios y anuncios.
            </p>
            <button
              onClick={() => navigate('/notifications')}
              className="w-full bg-purple-600 hover:bg-purple-700 text-white font-semibold py-3 px-4 rounded-lg transition-colors duration-200"
            >
              Ver Notificaciones
            </button>
          </div>
        </div>

        {/* Reminders Section */}
        <div className="bg-yellow-50 rounded-xl p-6 shadow-sm border border-yellow-200 mb-8">
          <h3 className="text-xl font-bold text-gray-900 mb-4 flex items-center">
            <svg className="w-5 h-5 text-yellow-600 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
            Recordatorios
          </h3>
          {stats.upcomingShifts > 0 ? (
            <div className="space-y-2">
              <p className="text-sm text-gray-700">
                Tienes <span className="font-semibold text-yellow-700">{stats.upcomingShifts}</span> turnos próximos esta semana.
              </p>
              {shifts.slice(0, 3).filter(shift => {
                const [year, month, day] = shift.date.split('-');
                const shiftDate = new Date(year, month - 1, day);
                const today = new Date();
                today.setHours(0, 0, 0, 0);
                const weekFromNow = new Date(today);
                weekFromNow.setDate(today.getDate() + 7);
                return shiftDate >= today && shiftDate <= weekFromNow;
              }).map((shift) => (
                <div key={shift.id} className="flex items-center justify-between bg-white p-3 rounded-lg border border-yellow-200">
                  <div>
                    <p className="font-medium text-gray-900 text-sm">
                      {(() => {
                        const [year, month, day] = shift.date.split('-');
                        const date = new Date(year, month - 1, day);
                        return date.toLocaleDateString('es-ES', {
                          weekday: 'short',
                          month: 'short',
                          day: 'numeric'
                        });
                      })()}
                    </p>
                    <p className="text-xs text-gray-600">
                      {shift.shiftType?.name} - {shift.location?.name}
                    </p>
                  </div>
                  <div className="text-xs text-gray-500">
                    {shift.shiftType?.startTime?.substring(0, 5)}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-sm text-gray-600">No tienes turnos próximos esta semana.</p>
          )}
        </div>

        {/* Shifts Summary */}
        <div id="shifts-section" className="bg-white rounded-xl p-8 shadow-sm border border-gray-200">
          <h3 className="text-2xl font-bold text-gray-900 mb-6 text-center">Mis Turnos</h3>
          {loading ? (
            <div className="text-center text-gray-500">Cargando turnos...</div>
          ) : shifts.length === 0 ? (
            <div className="text-center text-gray-500">No tienes turnos asignados</div>
          ) : (
            <div className="space-y-4">
              {shifts.slice(0, 5).map((shift) => (
                <div key={shift.id} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                  <div>
                    <p className="font-semibold text-gray-900">
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
                    </p>
                  </div>
                  <div className="text-right">
                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                      Asignado
                    </span>
                  </div>
                </div>
              ))}
              {shifts.length > 5 && (
                <div className="text-center">
                  <button
                    type="button"
                    onClick={(e) => { e.preventDefault(); navigate('/my-schedule'); }}
                    className="text-blue-600 hover:text-blue-800 font-medium"
                  >
                    Ver todos los turnos ({shifts.length})
                  </button>
                </div>
              )}
            </div>
          )}
        </div>
      </main>
    </div>
  );
};

export default UserLanding;