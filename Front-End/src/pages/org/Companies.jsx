import { useState, useEffect } from 'react';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import companyService from '../../services/companyService';

const Companies = () => {
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingCompany, setEditingCompany] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    address: '',
    phone: '',
    email: ''
  });

  useEffect(() => {
    loadCompanies();
  }, []);

  const loadCompanies = async () => {
    try {
      const response = await companyService.getAllCompanies();
      if (response.data) {
        setCompanies(Array.isArray(response.data) ? response.data : []);
      } else {
        console.error('Failed to load companies:', response.message);
        alert('Error al cargar empresas: ' + (response.message || 'Error desconocido'));
      }
    } catch (error) {
      console.error('Error loading companies:', error);
      alert('Error de conexión al cargar empresas');
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingCompany(null);
    setFormData({
      name: '',
      description: '',
      address: '',
      phone: '',
      email: ''
    });
    setModalOpen(true);
  };

  const handleEdit = (company) => {
    setEditingCompany(company);
    setFormData({
      name: company.name || '',
      description: company.description || '',
      address: company.address || '',
      phone: company.phone || '',
      email: company.email || ''
    });
    setModalOpen(true);
  };

  const handleDelete = async (company) => {
    if (window.confirm(`¿Estás seguro de que quieres eliminar la empresa "${company.name}"?`)) {
      try {
        await companyService.deleteCompany(company.id);
        await loadCompanies();
      } catch (error) {
        console.error('Error deleting company:', error);
        alert('Error al eliminar la empresa');
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const companyData = {
        name: formData.name,
        description: formData.description,
        address: formData.address,
        phone: formData.phone,
        email: formData.email
      };

      if (editingCompany) {
        await companyService.updateCompany(editingCompany.id, companyData);
      } else {
        await companyService.createCompany(companyData);
      }

      setModalOpen(false);
      await loadCompanies();
    } catch (error) {
      console.error('Error saving company:', error);
      alert('Error al guardar la empresa');
    }
  };

  const columns = [
    { key: 'name', header: 'Nombre', render: (value) => <span className="font-medium">{value}</span> },
    { key: 'email', header: 'Email' },
    { key: 'phone', header: 'Teléfono' },
    { key: 'address', header: 'Dirección' }
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Gestión de Empresas</h1>
          <p className="text-gray-600 mt-1">Administra las empresas</p>
        </div>
      </div>

      <DataTable
        title="Empresas"
        icon="🏢"
        columns={columns}
        data={companies}
        loading={loading}
        onAdd={handleAdd}
        onEdit={handleEdit}
        onDelete={handleDelete}
        addButtonText="Agregar Empresa"
        searchPlaceholder="Buscar empresas..."
        emptyMessage="No hay empresas registradas en el sistema"
      />

      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editingCompany ? 'Editar Empresa' : 'Agregar Empresa'}
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
              placeholder="Ingresa el nombre de la empresa"
            />
          </div>

          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
              Correo Electrónico
            </label>
            <input
              type="email"
              id="email"
              value={formData.email}
              onChange={(e) => setFormData({...formData, email: e.target.value})}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200"
              placeholder="empresa@email.com"
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
          </div>

          <div>
            <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-2">
              Descripción
            </label>
            <textarea
              id="description"
              value={formData.description}
              onChange={(e) => setFormData({...formData, description: e.target.value})}
              rows={4}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200"
              placeholder="Ingresa la descripción de la empresa"
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
              className="px-6 py-3 bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white rounded-lg transition-all duration-200 font-medium shadow-lg hover:shadow-xl"
            >
              {editingCompany ? 'Actualizar' : 'Crear'} Empresa
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Companies;