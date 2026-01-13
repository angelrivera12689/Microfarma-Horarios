import { useState, useEffect } from 'react';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import employeeService from '../../services/employeeService';
import positionService from '../../services/positionService';
import contractTypeService from '../../services/contractTypeService';

const Employees = () => {
  const [employees, setEmployees] = useState([]);
  const [positions, setPositions] = useState([]);
  const [contractTypes, setContractTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingEmployee, setEditingEmployee] = useState(null);
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    hireDate: '',
    birthDate: '',
    address: '',
    positionId: '',
    contractTypeId: ''
  });

  useEffect(() => {
    loadEmployees();
    loadPositions();
    loadContractTypes();
  }, []);

  const loadEmployees = async () => {
    try {
      const response = await employeeService.getAllEmployees();
      if (response.data) {
        const allEmployees = Array.isArray(response.data) ? response.data : [];
        const activeEmployees = allEmployees.filter(emp => emp.status === true);
        setEmployees(activeEmployees);
      } else {
        console.error('Failed to load employees:', response.message);
        alert('Error al cargar empleados: ' + (response.message || 'Error desconocido'));
      }
    } catch (error) {
      console.error('Error loading employees:', error);
      alert('Error de conexión al cargar empleados');
    } finally {
      setLoading(false);
    }
  };

  const loadPositions = async () => {
    try {
      const response = await positionService.getAllPositions();
      if (response.data) {
        setPositions(Array.isArray(response.data) ? response.data : []);
      } else {
        console.error('Failed to load positions:', response.message);
      }
    } catch (error) {
      console.error('Error loading positions:', error);
    }
  };

  const loadContractTypes = async () => {
    try {
      const response = await contractTypeService.getAllContractTypes();
      if (response.data) {
        setContractTypes(Array.isArray(response.data) ? response.data : []);
      } else {
        console.error('Failed to load contract types:', response.message);
      }
    } catch (error) {
      console.error('Error loading contract types:', error);
    }
  };

  const handleAdd = () => {
    setEditingEmployee(null);
    setFormData({
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      hireDate: '',
      birthDate: '',
      address: '',
      positionId: '',
      contractTypeId: ''
    });
    setModalOpen(true);
  };

  const handleEdit = (employee) => {
    setEditingEmployee(employee);
    setFormData({
      firstName: employee.firstName || '',
      lastName: employee.lastName || '',
      email: employee.email || '',
      phone: employee.phone || '',
      hireDate: employee.hireDate ? employee.hireDate.split('T')[0] : '',
      birthDate: employee.birthDate ? employee.birthDate.split('T')[0] : '',
      address: employee.address || '',
      positionId: employee.position?.id || '',
      contractTypeId: employee.contractType?.id || ''
    });
    setModalOpen(true);
  };

  const handleDelete = async (employee) => {
    if (window.confirm(`¿Estás seguro de que quieres eliminar al empleado "${employee.firstName} ${employee.lastName}"?`)) {
      try {
        await employeeService.deleteEmployee(employee.id);
        await loadEmployees();
      } catch (error) {
        console.error('Error deleting employee:', error);
        alert('Error al eliminar el empleado');
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const employeeData = {
        firstName: formData.firstName,
        lastName: formData.lastName,
        email: formData.email,
        phone: formData.phone,
        hireDate: formData.hireDate,
        birthDate: formData.birthDate,
        address: formData.address,
        position: { id: formData.positionId },
        contractType: { id: formData.contractTypeId }
      };

      if (editingEmployee) {
        await employeeService.updateEmployee(editingEmployee.id, employeeData);
      } else {
        await employeeService.createEmployee(employeeData);
      }

      setModalOpen(false);
      await loadEmployees();
    } catch (error) {
      console.error('Error saving employee:', error);
      alert('Error al guardar el empleado');
    }
  };

  const columns = [
    { key: 'firstName', header: 'Nombre', render: (value) => <span className="font-medium">{value}</span> },
    { key: 'lastName', header: 'Apellido', render: (value) => <span className="font-medium">{value}</span> },
    { key: 'email', header: 'Email' },
    { key: 'phone', header: 'Teléfono' },
    {
      key: 'position',
      header: 'Posición',
      render: (value) => value?.name || 'Sin posición'
    },
    {
      key: 'contractType',
      header: 'Tipo de Contrato',
      render: (value) => value?.name || 'Sin contrato'
    },
    {
      key: 'hireDate',
      header: 'Fecha de Contratación',
      render: (value) => value ? new Date(value).toLocaleDateString() : ''
    }
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Gestión de Empleados</h1>
          <p className="text-gray-600 mt-1">Administra la información de los empleados</p>
        </div>
      </div>

      <DataTable
        title="Empleados"
        icon="👥"
        columns={columns}
        data={employees}
        loading={loading}
        onAdd={handleAdd}
        onEdit={handleEdit}
        onDelete={handleDelete}
        addButtonText="Agregar Empleado"
        searchPlaceholder="Buscar empleados..."
        emptyMessage="No hay empleados registrados en el sistema"
      />

      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editingEmployee ? 'Editar Empleado' : 'Agregar Empleado'}
        size="md"
      >
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label htmlFor="firstName" className="block text-sm font-medium text-gray-700 mb-2">
                Nombre
              </label>
              <input
                type="text"
                id="firstName"
                value={formData.firstName}
                onChange={(e) => setFormData({...formData, firstName: e.target.value})}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors duration-200"
                placeholder="Ingresa el nombre"
              />
            </div>
            <div>
              <label htmlFor="lastName" className="block text-sm font-medium text-gray-700 mb-2">
                Apellido
              </label>
              <input
                type="text"
                id="lastName"
                value={formData.lastName}
                onChange={(e) => setFormData({...formData, lastName: e.target.value})}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors duration-200"
                placeholder="Ingresa el apellido"
              />
            </div>
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
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors duration-200"
              placeholder="empleado@email.com"
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
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors duration-200"
                placeholder="+57 300 123 4567"
              />
            </div>
            <div>
              <label htmlFor="hireDate" className="block text-sm font-medium text-gray-700 mb-2">
                Fecha de Contratación
              </label>
              <input
                type="date"
                id="hireDate"
                value={formData.hireDate}
                onChange={(e) => setFormData({...formData, hireDate: e.target.value})}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors duration-200"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label htmlFor="birthDate" className="block text-sm font-medium text-gray-700 mb-2">
                Fecha de Nacimiento
              </label>
              <input
                type="date"
                id="birthDate"
                value={formData.birthDate}
                onChange={(e) => setFormData({...formData, birthDate: e.target.value})}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors duration-200"
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
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors duration-200"
                placeholder="Dirección completa"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label htmlFor="positionId" className="block text-sm font-medium text-gray-700 mb-2">
                Posición
              </label>
              <select
                id="positionId"
                value={formData.positionId}
                onChange={(e) => setFormData({...formData, positionId: e.target.value})}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors duration-200"
              >
                <option value="">Seleccionar posición</option>
                {positions.map(position => (
                  <option key={position.id} value={position.id}>{position.name}</option>
                ))}
              </select>
            </div>
            <div>
              <label htmlFor="contractTypeId" className="block text-sm font-medium text-gray-700 mb-2">
                Tipo de Contrato
              </label>
              <select
                id="contractTypeId"
                value={formData.contractTypeId}
                onChange={(e) => setFormData({...formData, contractTypeId: e.target.value})}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors duration-200"
              >
                <option value="">Seleccionar tipo de contrato</option>
                {contractTypes.map(contractType => (
                  <option key={contractType.id} value={contractType.id}>{contractType.name}</option>
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
              className="px-6 py-3 bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700 text-white rounded-lg transition-all duration-200 font-medium shadow-lg hover:shadow-xl"
            >
              {editingEmployee ? 'Actualizar' : 'Crear'} Empleado
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Employees;