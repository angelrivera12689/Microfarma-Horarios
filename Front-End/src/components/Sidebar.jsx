import { useState } from 'react';
import { Link } from 'react-router-dom';

const Sidebar = ({ className = '', 'aria-hidden': ariaHidden }) => {
  const [openSections, setOpenSections] = useState({});

  const toggleSection = (section) => {
    setOpenSections(prev => ({
      ...prev,
      [section]: !prev[section]
    }));
  };

  const sections = [
    {
      name: 'Seguridad',
      icon: '🔒',
      items: [
        { name: 'Usuarios', path: 'security/users' },
        { name: 'Roles', path: 'security/roles' },
        { name: 'Permisos', path: 'security/permissions' }
      ]
    },
    {
      name: 'Recursos Humanos',
      icon: '👥',
      items: [
        { name: 'Empleados', path: 'hr/employees' },
        { name: 'Posiciones', path: 'hr/positions' },
        { name: 'Contratos', path: 'hr/contracts' }
      ]
    },
    {
      name: 'Organización',
      icon: '🏢',
      items: [
        { name: 'Empresas', path: 'org/companies' },
        { name: 'Ubicaciones', path: 'org/locations' }
      ]
    },
    {
      name: 'Horarios',
      icon: '📅',
      items: [
        { name: 'Horarios', path: 'schedules/schedules', icon: '📅' },
        { name: 'Calendarios', path: 'schedules/calendars' },
        { name: 'Tipos de Turno', path: 'schedules/shifttypes' },
        { name: 'Turnos', path: 'schedules/shifts' },
        { name: 'Solicitudes de Cambio', path: 'schedules/shift-change-requests' }
      ]
    },
    {
      name: 'Noticias',
      icon: '📰',
      items: [
        { name: 'Noticias', path: 'news/news' },
        { name: 'Tipos', path: 'news/types' }
      ]
    },
    {
      name: 'Notificaciones',
      icon: '📧',
      items: [
        { name: 'Email', path: 'notifications/email' }
      ]
    }
  ];

  return (
    <div
      className={`bg-white text-slate-700 w-[280px] min-h-screen border-r border-slate-200 shadow-sm transition-all duration-200 ${className}`}
      aria-hidden={ariaHidden}
      role="navigation"
      aria-label="Main navigation"
    >
      <div className="p-6">
        {/* Logo Section */}
        <div className="flex items-center space-x-3 mb-8">
          <div className="w-10 h-10 bg-gradient-to-br from-red-500 to-red-600 rounded-lg flex items-center justify-center shadow-sm">
            <span className="text-white font-bold text-lg">M</span>
          </div>
          <div>
            <h2 className="text-lg font-bold text-slate-900">Microfarma</h2>
            <p className="text-sm text-slate-500">Panel Admin</p>
          </div>
        </div>

        {/* Navigation */}
        <nav className="space-y-1">
          {sections.map((section) => (
            <div key={section.name} className="mb-2">
              <button
                onClick={() => toggleSection(section.name)}
                className="w-full text-left flex items-center justify-between p-3 hover:bg-slate-50 rounded-lg transition-colors duration-200 group"
              >
                <span className="flex items-center">
                  <span className="text-slate-400 mr-3 text-lg">{section.icon}</span>
                  <span className="font-medium text-slate-700 group-hover:text-slate-900">{section.name}</span>
                </span>
                <span className="text-slate-400 group-hover:text-slate-600 transition-colors duration-200">
                  {openSections[section.name] ? '▼' : '▶'}
                </span>
              </button>
              {openSections[section.name] && (
                <div className="ml-8 mt-2 space-y-1">
                  {section.items.map((item) => (
                    <Link
                      key={item.path}
                      to={item.path}
                      className="block p-2 text-sm text-slate-600 hover:text-slate-900 hover:bg-slate-50 rounded-md transition-colors duration-200"
                    >
                      <span className="flex items-center">
                        {item.icon && <span className="text-slate-400 mr-2 text-sm">{item.icon}</span>}
                        {item.name}
                      </span>
                    </Link>
                  ))}
                </div>
              )}
            </div>
          ))}
        </nav>

      </div>
    </div>
  );
};

export default Sidebar;