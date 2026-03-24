import { useState, useEffect } from 'react';
import newsService from '../../services/newsService';

const News = () => {
  const [news, setNews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedNews, setSelectedNews] = useState(null);

  useEffect(() => {
    loadNews();
  }, []);

  const loadNews = async () => {
    try {
      setLoading(true);
      const response = await newsService.getAllNews();
      if (response.success) {
        setNews(response.data || []);
      } else {
        setError(response.message);
      }
    } catch (err) {
      setError('Error al cargar las noticias');
      console.error('Error loading news:', err);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'Sin fecha';
    const date = new Date(dateString);
    return date.toLocaleDateString('es-CO', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
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
        <h1 className="text-3xl font-bold text-green-400">Gestión de Noticias</h1>
        <button
          onClick={loadNews}
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

      {news.length === 0 ? (
        <div className="bg-gray-800 p-6 rounded-lg shadow-lg text-center">
          <p className="text-gray-300">No hay noticias registradas</p>
          <p className="text-gray-400 text-sm mt-2">Las noticias aparecerán aquí cuando se creen</p>
        </div>
      ) : (
        <div className="grid gap-4">
          {news.map((item) => (
            <div
              key={item.id}
              className="bg-gray-800 p-6 rounded-lg shadow-lg hover:shadow-xl transition-shadow cursor-pointer"
              onClick={() => setSelectedNews(item)}
            >
              <div className="flex justify-between items-start mb-3">
                <div>
                  <h3 className="text-xl font-bold text-white">{item.title || 'Sin título'}</h3>
                  <p className="text-gray-400 text-sm">
                    Tipo: {item.newsType?.name || 'Sin tipo'}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  {getStatusBadge(item.status)}
                </div>
              </div>
              
              <p className="text-gray-300 mb-3 line-clamp-2">
                {item.content || item.description || 'Sin contenido'}
              </p>
              
              <div className="flex justify-between items-center text-sm text-gray-500">
                <span>📅 {formatDate(item.publicationDate || item.createdAt)}</span>
                {item.employee && (
                  <span>👤 {item.employee.firstName} {item.employee.lastName}</span>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Modal de detalle */}
      {selectedNews && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-gray-800 rounded-lg p-6 max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-start mb-4">
              <h2 className="text-2xl font-bold text-white">{selectedNews.title || 'Sin título'}</h2>
              <button
                onClick={() => setSelectedNews(null)}
                className="text-gray-400 hover:text-white text-2xl"
              >
                ×
              </button>
            </div>
            
            <div className="space-y-3">
              <div className="flex items-center gap-2">
                <span className="text-gray-400">Tipo:</span>
                <span className="text-white">{selectedNews.newsType?.name || 'Sin tipo'}</span>
              </div>
              
              <div className="flex items-center gap-2">
                <span className="text-gray-400">Estado:</span>
                {getStatusBadge(selectedNews.status)}
              </div>
              
              <div className="flex items-center gap-2">
                <span className="text-gray-400">Fecha de publicación:</span>
                <span className="text-white">{formatDate(selectedNews.publicationDate)}</span>
              </div>
              
              {selectedNews.employee && (
                <div className="flex items-center gap-2">
                  <span className="text-gray-400">Publicado por:</span>
                  <span className="text-white">{selectedNews.employee.firstName} {selectedNews.employee.lastName}</span>
                </div>
              )}
              
              <div className="border-t border-gray-700 pt-3">
                <span className="text-gray-400 block mb-2">Contenido:</span>
                <p className="text-white whitespace-pre-wrap">{selectedNews.content || selectedNews.description || 'Sin contenido'}</p>
              </div>
            </div>
            
            <div className="mt-6 flex justify-end">
              <button
                onClick={() => setSelectedNews(null)}
                className="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors"
              >
                Cerrar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default News;
