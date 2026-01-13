import { useState, useEffect } from 'react';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import roleService from '../../services/roleService';
import permissionService from '../../services/permissionService';
import authService from '../../services/authService';

const Roles = () => {
  const [roles, setRoles] = useState([]);
  const [permissions, setPermissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [permissionsModalOpen, setPermissionsModalOpen] = useState(false);
  const [editingRole, setEditingRole] = useState(null);
  const [selectedRole, setSelectedRole] = useState(null);
  const [rolePermissions, setRolePermissions] = useState([]);
  const [formData, setFormData] = useState({
    name: ''
  });

  useEffect(() => {
    const user = authService.getCurrentUser();
    console.log('Current user:', user);
    if (!user || user.role !== 'ADMIN') {
      console.error('User is not admin or not logged in');
      alert('Debes iniciar sesión como administrador para acceder a esta página');
      return;
    }
    loadRoles();
    loadPermissions();
  }, []);

  const loadRoles = async () => {
    try {
      const response = await roleService.getAllRoles();
      if (response.status) {
        setRoles(response.data || []);
      } else {
        console.error('Failed to load roles:', response.message);
        alert('Error al cargar roles: ' + (response.message || 'Error desconocido'));
      }
    } catch (error) {
      console.error('Error loading roles:', error);
      alert('Error de conexión al cargar roles');
    } finally {
      setLoading(false);
    }
  };

  const loadPermissions = async () => {
    try {
      const response = await permissionService.getAllPermissions();
      if (response.status) {
        setPermissions(response.data || []);
      } else {
        console.error('Failed to load permissions:', response.message);
      }
    } catch (error) {
      console.error('Error loading permissions:', error);
    }
  };

  const handleAdd = () => {
    setEditingRole(null);
    setFormData({
      name: ''
    });
    setModalOpen(true);
  };

  const handleEdit = (role) => {
    setEditingRole(role);
    setFormData({
      name: role.name || ''
    });
    setModalOpen(true);
  };

  const handleManagePermissions = async (role) => {
    setSelectedRole(role);
    try {
      const response = await roleService.getPermissionsForRole(role.id);
      if (response.status) {
        setRolePermissions(response.data || []);
      } else {
        setRolePermissions([]);
      }
    } catch (error) {
      console.error('Error loading role permissions:', error);
      setRolePermissions([]);
    }
    setPermissionsModalOpen(true);
  };

  const handlePermissionChange = async (permissionId, isChecked) => {
    if (!selectedRole) return;

    try {
      if (isChecked) {
        await roleService.assignPermissionToRole(selectedRole.id, permissionId);
      } else {
        await roleService.removePermissionFromRole(selectedRole.id, permissionId);
      }
      // Reload permissions for the role
      const response = await roleService.getPermissionsForRole(selectedRole.id);
      if (response.status) {
        setRolePermissions(response.data || []);
      }
    } catch (error) {
      console.error('Error updating permission:', error);
      alert('Error al actualizar el permiso');
    }
  };

  const handleDelete = async (role) => {
    if (window.confirm(`¿Estás seguro de que quieres eliminar el rol "${role.name}"?`)) {
      try {
        await roleService.deleteRole(role.id);
        await loadRoles();
      } catch (error) {
        console.error('Error deleting role:', error);
        alert('Error al eliminar el rol');
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const roleData = {
        name: formData.name
      };

      if (editingRole) {
        await roleService.updateRole(editingRole.id, roleData);
      } else {
        await roleService.createRole(roleData);
      }

      setModalOpen(false);
      await loadRoles();
    } catch (error) {
      console.error('Error saving role:', error);
      alert('Error al guardar el rol');
    }
  };

  const columns = [
    { key: 'name', header: 'Nombre', render: (value) => <span className="font-medium">{value}</span> },
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
    },
    {
      key: 'actions',
      header: 'Acciones',
      render: (value, row) => (
        <div className="flex space-x-2">
          <button
            onClick={() => handleManagePermissions(row)}
            className="px-3 py-1 text-sm bg-blue-500 text-white rounded hover:bg-blue-600"
          >
            Permisos
          </button>
        </div>
      )
    }
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Gestión de Roles</h1>
          <p className="text-gray-600 mt-1">Administra los roles del sistema</p>
        </div>
      </div>

      <DataTable
        title="Roles"
        icon="🛡️"
        columns={columns}
        data={roles}
        loading={loading}
        onAdd={handleAdd}
        onEdit={handleEdit}
        onDelete={handleDelete}
        addButtonText="Agregar Rol"
        searchPlaceholder="Buscar roles..."
        emptyMessage="No hay roles registrados en el sistema"
      />

      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editingRole ? 'Editar Rol' : 'Agregar Rol'}
        size="md"
      >
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-2">
              Nombre del Rol
            </label>
            <input
              type="text"
              id="name"
              value={formData.name}
              onChange={(e) => setFormData({...formData, name: e.target.value})}
              required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-colors duration-200"
              placeholder="Ingresa el nombre del rol"
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
              {editingRole ? 'Actualizar' : 'Crear'} Rol
            </button>
          </div>
        </form>
      </Modal>

      <Modal
        isOpen={permissionsModalOpen}
        onClose={() => setPermissionsModalOpen(false)}
        title={`Permisos para ${selectedRole?.name || ''}`}
        size="lg"
      >
        <div className="space-y-4">
          {permissions.map(permission => {
            const isAssigned = rolePermissions.some(rp => rp.id === permission.id);
            return (
              <div key={permission.id} className="flex items-center space-x-3">
                <input
                  type="checkbox"
                  id={`perm-${permission.id}`}
                  checked={isAssigned}
                  onChange={(e) => handlePermissionChange(permission.id, e.target.checked)}
                  className="w-4 h-4 text-red-600 bg-gray-100 border-gray-300 rounded focus:ring-red-500"
                />
                <label htmlFor={`perm-${permission.id}`} className="text-sm font-medium text-gray-700">
                  {permission.name}
                </label>
                {permission.description && (
                  <span className="text-sm text-gray-500">({permission.description})</span>
                )}
              </div>
            );
          })}
        </div>
        <div className="flex justify-end pt-4">
          <button
            type="button"
            onClick={() => setPermissionsModalOpen(false)}
            className="px-6 py-3 bg-gradient-to-r from-red-500 to-red-600 hover:from-red-600 hover:to-red-700 text-white rounded-lg transition-all duration-200 font-medium shadow-lg hover:shadow-xl"
          >
            Cerrar
          </button>
        </div>
      </Modal>
    </div>
  );
};

export default Roles;