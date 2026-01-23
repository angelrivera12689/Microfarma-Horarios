import { useState, useEffect } from 'react';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import permissionService from '../../services/permissionService';
import authService from '../../services/authService';

const Permissions = () => {
  const [permissions, setPermissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingPermission, setEditingPermission] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: ''
  });

  useEffect(() => {
    const user = authService.getCurrentUser();
    if (!user || user.role !== 'ADMIN') {
      alert('Debes iniciar sesión como administrador para acceder a esta página');
      return;
    }
    loadPermissions();
  }, []);

  const loadPermissions = async () => {
    try {
      const response = await permissionService.getAllPermissions();
      if (response.status) {
        setPermissions(response.data || []);
      } else {
        alert('Error al cargar permisos: ' + (response.message || 'Error desconocido'));
      }
    } catch {
      alert('Error de conexión al cargar permisos');
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingPermission(null);
    setFormData({
      name: '',
      description: ''
    });
    setModalOpen(true);
  };

  const handleEdit = (permission) => {
    setEditingPermission(permission);
    setFormData({
      name: permission.name || '',
      description: permission.description || ''
    });
    setModalOpen(true);
  };

  const handleDelete = async (permission) => {
    if (window.confirm(`¿Estás seguro de que quieres eliminar el permiso "${permission.name}"?`)) {
      try {
        await permissionService.deletePermission(permission.id);
        await loadPermissions();
      } catch {
        alert('Error al eliminar el permiso');
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const permissionData = {
        name: formData.name,
        description: formData.description
      };

      if (editingPermission) {
        await permissionService.updatePermission(editingPermission.id, permissionData);
      } else {
        await permissionService.createPermission(permissionData);
      }

      setModalOpen(false);
      await loadPermissions();
    } catch {
      alert('Error al guardar el permiso');
    }
  };

  const columns = [
    { key: 'name', header: 'Nombre', render: (value) => <span className="font-medium">{value}</span> },
    { key: 'description', header: 'Descripción' },
    {
      key: 'status',
      header: 'Estado',
      render: (value) => (
        <span className={`px-2 py-1 text-xs rounded-full ${
          value ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
        }`}>
          {value ? 'Activo' : 'Inactivo'}
        </span>
      )
    }
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Gestión de Permisos</h1>
          <p className="text-gray-600 mt-1">Administra los permisos del sistema</p>
        </div>
      </div>

      <DataTable
        title="Permisos"
        icon="🔑"
        columns={columns}
        data={permissions}
        loading={loading}
        onAdd={handleAdd}
        onEdit={handleEdit}
        onDelete={handleDelete}
        addButtonText="Agregar Permiso"
        searchPlaceholder="Buscar permisos..."
        emptyMessage="No hay permisos registrados en el sistema"
      />

      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editingPermission ? 'Editar Permiso' : 'Agregar Permiso'}
        size="md"
      >
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-2">
              Nombre del Permiso
            </label>
            <input
              type="text"
              id="name"
              value={formData.name}
              onChange={(e) => setFormData({...formData, name: e.target.value})}
              required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-colors duration-200"
              placeholder="Ingresa el nombre del permiso"
            />
          </div>

          <div>
            <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-2">
              Descripción
            </label>
            <textarea
              id="description"
              value={formData.description}
              onChange={(e) => setFormData({...formData, description: e.target.value})}
              rows="3"
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-colors duration-200"
              placeholder="Describe el permiso"
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
              className="px-6 py-3 bg-gradient-to-r from-red-500 to-red-600 hover:from-red-600 hover:to-red-700 text-white rounded-lg transition-all duration-200 font-medium shadow-lg hover:shadow-xl"
            >
              {editingPermission ? 'Actualizar' : 'Crear'} Permiso
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Permissions;