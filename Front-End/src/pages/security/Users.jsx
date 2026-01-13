import { useState, useEffect } from 'react';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import userService from '../../services/userService';
import roleService from '../../services/roleService';
import authService from '../../services/authService';

const Users = () => {
  const [users, setUsers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    roleId: ''
  });

  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    const user = authService.getCurrentUser();
    console.log('Current user:', user);
    if (!user || user.role !== 'ADMIN') {
      console.error('User is not admin or not logged in');
      alert('Debes iniciar sesión como administrador para acceder a esta página');
      return;
    }
    loadUsers();
    loadRoles();
  }, []);

  const loadUsers = async () => {
    try {
      const response = await userService.getAllUsers();
      if (response.status) {
        setUsers(response.data || []);
      } else {
        console.error('Failed to load users:', response.message);
        alert('Error al cargar usuarios: ' + (response.message || 'Error desconocido'));
      }
    } catch (error) {
      console.error('Error loading users:', error);
      alert('Error de conexión al cargar usuarios');
    } finally {
      setLoading(false);
    }
  };

  const loadRoles = async () => {
    try {
      const response = await roleService.getAllRoles();
      if (response.status) {
        setRoles(response.data || []);
      } else {
        console.error('Failed to load roles:', response.message);
      }
    } catch (error) {
      console.error('Error loading roles:', error);
    }
  };

  const handleAdd = () => {
    setEditingUser(null);
    setFormData({
      name: '',
      email: '',
      password: '',
      roleId: ''
    });
    setIsSubmitting(false);
    setModalOpen(true);
  };

  const handleEdit = (user) => {
    setEditingUser(user);
    setFormData({
      name: user.name || '',
      email: user.email || '',
      password: '',
      roleId: user.role?.id || ''
    });
    setIsSubmitting(false);
    setModalOpen(true);
  };

  const handleDelete = async (user) => {
    if (window.confirm(`¿Estás seguro de que quieres eliminar al usuario "${user.name}"?`)) {
      try {
        await userService.deleteUser(user.id);
        await loadUsers();
      } catch (error) {
        console.error('Error deleting user:', error);
        alert('Error al eliminar el usuario');
      }
    }
  };

  const handleSubmit = async (e) => {
   e.preventDefault();
   setIsSubmitting(true);

   try {
     const userData = {
       name: formData.name,
       email: formData.email,
       role: { id: formData.roleId }
     };

     let response;
     if (editingUser) {
       response = await userService.updateUser(editingUser.id, userData);
     } else {
       userData.passwordHash = formData.password; // For new users
       response = await userService.createUser(userData);
     }

     if (!response.status) {
       alert('Error al guardar el usuario: ' + (response.message || 'Error desconocido'));
       return;
     }

     setModalOpen(false);
     await loadUsers();
     setIsSubmitting(false);
   } catch (error) {
     console.error('Error saving user:', error);
     alert('Error al guardar el usuario');
     setIsSubmitting(false);
   }
 };

  const columns = [
    { key: 'name', header: 'Nombre', render: (value) => <span className="font-medium">{value}</span> },
    { key: 'email', header: 'Email' },
    {
      key: 'role',
      header: 'Rol',
      render: (value) => value?.name || 'Sin rol'
    },
    {
      key: 'active',
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
          <h1 className="text-3xl font-bold text-gray-900">Gestión de Usuarios</h1>
          <p className="text-gray-600 mt-1">Administra los usuarios del sistema</p>
        </div>
      </div>

      <DataTable
        title="Usuarios"
        icon="👥"
        columns={columns}
        data={users}
        loading={loading}
        onAdd={handleAdd}
        onEdit={handleEdit}
        onDelete={handleDelete}
        addButtonText="Agregar Usuario"
        searchPlaceholder="Buscar usuarios..."
        emptyMessage="No hay usuarios registrados en el sistema"
      />

      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editingUser ? 'Editar Usuario' : 'Agregar Usuario'}
        size="md"
      >
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-2">
              Nombre Completo
            </label>
            <input
              type="text"
              id="name"
              value={formData.name}
              onChange={(e) => setFormData({...formData, name: e.target.value})}
              required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-colors duration-200"
              placeholder="Ingresa el nombre completo"
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
              required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-colors duration-200"
              placeholder="usuario@email.com"
            />
          </div>

          {!editingUser && (
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-2">
                Contraseña
              </label>
              <input
                type="password"
                id="password"
                value={formData.password}
                onChange={(e) => setFormData({...formData, password: e.target.value})}
                required={!editingUser}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-colors duration-200"
                placeholder="••••••••"
              />
            </div>
          )}

          <div>
            <label htmlFor="roleId" className="block text-sm font-medium text-gray-700 mb-2">
              Rol
            </label>
            <select
              id="roleId"
              value={formData.roleId}
              onChange={(e) => setFormData({...formData, roleId: e.target.value})}
              required
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-colors duration-200"
            >
              <option value="">Seleccionar rol</option>
              {roles.map(role => (
                <option key={role.id} value={role.id}>{role.name}</option>
              ))}
            </select>
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
              disabled={isSubmitting}
              className="px-6 py-3 bg-gradient-to-r from-red-500 to-red-600 hover:from-red-600 hover:to-red-700 text-white rounded-lg transition-all duration-200 font-medium shadow-lg hover:shadow-xl disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isSubmitting ? 'Cargando...' : `${editingUser ? 'Actualizar' : 'Crear'} Usuario`}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Users;