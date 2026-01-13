import { useState, useEffect } from 'react';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import locationService from '../../services/locationService';
import companyService from '../../services/companyService';

const Locations = () => {
  const [locations, setLocations] = useState([]);
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingLocation, setEditingLocation] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    address: '',
    phone: '',
    companyId: ''
  });

  useEffect(() => {
    loadLocations();
    loadCompanies();
  }, []);

  const loadLocations = async () => {
    try {
      const response = await locationService.getAllLocations();
      if (response.data) {
        setLocations(Array.isArray(response.data) ? response.data : []);
      } else {
        console.error('Failed to load locations:', response.message);
        alert('Error al cargar ubicaciones: ' + (response.message || 'Error desconocido'));
      }
    } catch (error) {
      console.error('Error loading locations:', error);
      alert('Error de conexión al cargar ubicaciones');
    } finally {
      setLoading(false);
    }
  };

  const loadCompanies = async () => {
    try {
      const response = await companyService.getAllCompanies();
      if (response.data) {
        setCompanies(Array.isArray(response.data) ? response.data : []);
      } else {
        console.error('Failed to load companies:', response.message);
      }
    } catch (error) {
      console.error('Error loading companies:', error);
    }
  };

  const handleAdd = () => {
    setEditingLocation(null);
    setFormData({
      name: '',
      address: '',
      phone: '',
      companyId: ''
    });
    setModalOpen(true);
  };

  const handleEdit = (location) => {
    setEditingLocation(location);
    setFormData({
      name: location.name || '',
      address: location.address || '',
      phone: location.phone || '',
      companyId: location.company?.id || ''
    });
    setModalOpen(true);
  };

  const handleDelete = async (location) => {
    if (window.confirm(`¿Estás seguro de que quieres eliminar la ubicación "${location.name}"?`)) {
      try {
        await locationService.deleteLocation(location.id);
        await loadLocations();
      } catch (error) {
        console.error('Error deleting location:', error);
        alert('Error al eliminar la ubicación');
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const locationData = {
        name: formData.name,
        address: formData.address,
        phone: formData.phone,
        company: { id: formData.companyId }
      };

      if (editingLocation) {
        await locationService.updateLocation(editingLocation.id, locationData);
      } else {
        await locationService.createLocation(locationData);
      }

      setModalOpen(false);
      await loadLocations();
    } catch (error) {
      console.error('Error saving location:', error);
      alert('Error al guardar la ubicación');
    }
  };

  const columns = [
    { key: 'name', header: 'Nombre', render: (value) => <span className="font-medium">{value}</span> },
    { key: 'address', header: 'Dirección' },
    { key: 'phone', header: 'Teléfono' },
    {
      key: 'company',
      header: 'Empresa',
      render: (value) => value?.name || 'Sin empresa'
    }
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Gestión de Ubicaciones</h1>
          <p className="text-gray-600 mt-1">Administra las ubicaciones</p>
        </div>
      </div>

      <DataTable
        title="Ubicaciones"
        icon="📍"
        columns={columns}
        data={locations}
        loading={loading}
        onAdd={handleAdd}
        onEdit={handleEdit}
        onDelete={handleDelete}
        addButtonText="Agregar Ubicación"
        searchPlaceholder="Buscar ubicaciones..."
        emptyMessage="No hay ubicaciones registradas en el sistema"
      />

      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editingLocation ? 'Editar Ubicación' : 'Agregar Ubicación'}
        size="md"
      >
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-2">
              Nombre
            </label>
            <input
              type="text"
              id="name"
              value={formData.name}
              onChange={(e) => setFormData({...formData, name: e.target.value})}
              required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200"
              placeholder="Ingresa el nombre de la ubicación"
            />
          </div>

          <div>
            <label htmlFor="address" className="block text-sm font-medium text-gray-700 mb-2">
              Dirección
            </label>
            <input
              type="text"
              id="address"
              value={formData.address}
              onChange={(e) => setFormData({...formData, address: e.target.value})}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200"
              placeholder="Dirección completa"
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label htmlFor="phone" className="block text-sm font-medium text-gray-700 mb-2">
                Teléfono
              </label>
              <input
                type="tel"
                id="phone"
                value={formData.phone}
                onChange={(e) => setFormData({...formData, phone: e.target.value})}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200"
                placeholder="+57 300 123 4567"
              />
            </div>
            <div>
              <label htmlFor="companyId" className="block text-sm font-medium text-gray-700 mb-2">
                Empresa
              </label>
              <select
                id="companyId"
                value={formData.companyId}
                onChange={(e) => setFormData({...formData, companyId: e.target.value})}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200"
              >
                <option value="">Seleccionar empresa</option>
                {companies.map(company => (
                  <option key={company.id} value={company.id}>{company.name}</option>
                ))}
              </select>
            </div>
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
              className="px-6 py-3 bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white rounded-lg transition-all duration-200 font-medium shadow-lg hover:shadow-xl"
            >
              {editingLocation ? 'Actualizar' : 'Crear'} Ubicación
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Locations;