import { useState, useEffect } from 'react';
import newsService from '../../services/newsService';

const NewsTypes = () => {
  const [newsTypes, setNewsTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadNewsTypes();
  }, []);

  const loadNewsTypes = async () => {
    try {
      setLoading(true);
      const response = await newsService.getNewsTypes();
      if (response.success) {
        setNewsTypes(response.data || []);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('Error al cargar los tipos de noticias');
      console.error('Error loading news types:', err);
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    const statusConfig = {
      'ACTIVE': { bg: 'bg-green-100', text: 'text-green-800', label: 'Activo' },
      'INACTIVE': { bg: 'bg-gray-100', text: 'text-gray-800', label: 'Inactivo' }
    };
    const config = statusConfig[status] || statusConfig['INACTIVE'];
    return (
      <span className={`px-2 py-1 rounded-full text-xs font-medium ${config.bg} ${config.text}`}>
        {config.label}
      </span>
    );
  };

  const getTypeColor = (name) => {
    const colors = {
      'CUMPLEAÑOS': 'bg-purple-500',
      'EVENTO': 'bg-blue-500',
      'AVISO': 'bg-yellow-500',
      'FELICITACION': 'bg-green-500',
      'DEFAULT': 'bg-gray-500'
    };
    const key = name?.toUpperCase() || 'DEFAULT';
    return colors[key] || colors['DEFAULT'];
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-500"></div>
      </div>
    );
  }

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-green-400">Gestión de Tipos de Noticias</h1>
        <button
          onClick={loadNewsTypes}
          className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
        >
          🔄 Actualizar
        </button>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {newsTypes.length === 0 ? (
        <div className="bg-gray-800 p-6 rounded-lg shadow-lg text-center">
          <p className="text-gray-300">No hay tipos de noticias registrados</p>
          <p className="text-gray-400 text-sm mt-2">Los tipos de noticias aparecerán aquí cuando se creen</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {newsTypes.map((type) => (
            <div
              key={type.id}
              className="bg-gray-800 p-6 rounded-lg shadow-lg hover:shadow-xl transition-shadow"
            >
              <div className="flex items-center gap-3 mb-3">
                <div className={`w-10 h-10 rounded-full ${getTypeColor(type.name)} flex items-center justify-center text-white font-bold`}>
                  {type.name?.charAt(0) || '?'}
                </div>
                <div>
                  <h3 className="text-xl font-bold text-white">{type.name || 'Sin nombre'}</h3>
                  {getStatusBadge(type.status)}
                </div>
              </div>
              
              {type.description && (
                <p className="text-gray-300 text-sm mt-3">
                  {type.description}
                </p>
              )}
              
              <div className="mt-4 pt-3 border-t border-gray-700 text-xs text-gray-500">
                <span>ID: {type.id}</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default NewsTypes;
