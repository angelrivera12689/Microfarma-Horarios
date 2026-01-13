import { useMemo } from 'react';

const WeeklyGrid = ({ schedule }) => {
  const weeks = useMemo(() => {
    if (!schedule || !schedule.startDate || !schedule.endDate) return [];

    const start = new Date(schedule.startDate);
    const end = new Date(schedule.endDate);
    const weeks = [];

    let currentWeekStart = new Date(start);
    // Set to Monday of the week
    const dayOfWeek = currentWeekStart.getDay();
    const diff = currentWeekStart.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1);
    currentWeekStart.setDate(diff);
    currentWeekStart.setHours(0, 0, 0, 0);

    while (currentWeekStart <= end) {
      const weekEnd = new Date(currentWeekStart);
      weekEnd.setDate(currentWeekStart.getDate() + 6);

      const days = [];
      for (let i = 0; i < 7; i++) {
        const day = new Date(currentWeekStart);
        day.setDate(currentWeekStart.getDate() + i);
        days.push({
          date: day,
          isInSchedule: day >= start && day <= end,
          isToday: day.toDateString() === new Date().toDateString()
        });
      }

      weeks.push({
        weekStart: new Date(currentWeekStart),
        days
      });

      currentWeekStart.setDate(currentWeekStart.getDate() + 7);
    }

    return weeks;
  }, [schedule]);

  if (!schedule) {
    return <div className="text-gray-500">Selecciona un horario para ver los detalles</div>;
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">
        Vista Semanal: {schedule.name}
      </h3>

      <div className="space-y-4">
        {weeks.map((week, weekIndex) => (
          <div key={weekIndex} className="border border-gray-200 rounded-lg p-4">
            <h4 className="text-sm font-medium text-gray-700 mb-3">
              Semana del {week.weekStart.toLocaleDateString('es-ES', {
                day: 'numeric',
                month: 'short',
                year: 'numeric'
              })}
            </h4>

            <div className="grid grid-cols-7 gap-2">
              {['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'].map((dayName, dayIndex) => {
                const day = week.days[dayIndex];
                return (
                  <div
                    key={dayIndex}
                    className={`p-3 rounded-lg text-center text-sm ${
                      day.isInSchedule
                        ? day.isToday
                          ? 'bg-green-500 text-white'
                          : 'bg-green-100 text-green-800'
                        : 'bg-gray-50 text-gray-400'
                    }`}
                  >
                    <div className="font-medium">{dayName}</div>
                    <div className="text-xs mt-1">
                      {day.date.getDate()}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        ))}
      </div>

      <div className="mt-4 text-sm text-gray-600">
        <p><strong>Empleado:</strong> {schedule.employee ? `${schedule.employee.firstName} ${schedule.employee.lastName}` : 'N/A'}</p>
        <p><strong>Período:</strong> {new Date(schedule.startDate).toLocaleDateString()} - {new Date(schedule.endDate).toLocaleDateString()}</p>
        <p><strong>Estado:</strong> {schedule.isActive ? 'Activo' : 'Inactivo'}</p>
      </div>
    </div>
  );
};

export default WeeklyGrid;