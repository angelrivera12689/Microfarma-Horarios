import { useState, useEffect } from 'react';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import contractTypeService from '../../services/contractTypeService';

const Contracts = () => {
  const [contractTypes, setContractTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingContractType, setEditingContractType] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    durationMonths: ''
  });

  useEffect(() => {
    loadContractTypes();
  }, []);

  const loadContractTypes = async () => {
    try {
      const response = await contractTypeService.getAllContractTypes();
      if (response.data) {
        setContractTypes(Array.isArray(response.data) ? response.data : []);
      } else {
        alert('Error al cargar tipos de contrato: ' + (response.message || 'Error desconocido'));
      }
    } catch {
      alert('Error de conexión al cargar tipos de contrato');
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingContractType(null);
    setFormData({
      name: '',
      description: '',
      durationMonths: ''
    });
    setModalOpen(true);
  };

  const handleEdit = (contractType) => {
    setEditingContractType(contractType);
    setFormData({
      name: contractType.name || '',
      description: contractType.description || '',
      durationMonths: contractType.durationMonths || ''
    });
    setModalOpen(true);
  };

  const handleDelete = async (contractType) => {
    if (window.confirm(`¿Estás seguro de que quieres eliminar el tipo de contrato "${contractType.name}"?`)) {
      try {
        await contractTypeService.deleteContractType(contractType.id);
        await loadContractTypes();
      } catch {
        alert('Error al eliminar el tipo de contrato');
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const contractTypeData = {
        name: formData.name,
        description: formData.description,
        durationMonths: formData.durationMonths ? parseInt(formData.durationMonths) : null
      };

      if (editingContractType) {
        await contractTypeService.updateContractType(editingContractType.id, contractTypeData);
      } else {
        await contractTypeService.createContractType(contractTypeData);
      }

      setModalOpen(false);
      await loadContractTypes();
    } catch {
      alert('Error al guardar el tipo de contrato');
    }
  };

  const columns = [
    { key: 'name', header: 'Nombre', render: (value) => <span className="font-medium">{value}</span> },
    { key: 'description', header: 'Descripción' },
    { key: 'durationMonths', header: 'Duración (Meses)', render: (value) => value ? `${value} meses` : 'Indefinido' }
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Gestión de Tipos de Contrato</h1>
          <p className="text-gray-600 mt-1">Administra los tipos de contrato</p>
        </div>
      </div>

      <DataTable
        title="Tipos de Contrato"
        icon="📄"
        columns={columns}
        data={contractTypes}
        loading={loading}
        onAdd={handleAdd}
        onEdit={handleEdit}
        onDelete={handleDelete}
        addButtonText="Agregar Tipo de Contrato"
        searchPlaceholder="Buscar tipos de contrato..."
        emptyMessage="No hay tipos de contrato registrados en el sistema"
      />

      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editingContractType ? 'Editar Tipo de Contrato' : 'Agregar Tipo de Contrato'}
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
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors duration-200"
              placeholder="Ingresa el nombre del tipo de contrato"
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
              rows={4}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors duration-200"
              placeholder="Ingresa la descripción del tipo de contrato"
            />
          </div>

          <div>
            <label htmlFor="durationMonths" className="block text-sm font-medium text-gray-700 mb-2">
              Duración en Meses
            </label>
            <input
              type="number"
              id="durationMonths"
              value={formData.durationMonths}
              onChange={(e) => setFormData({...formData, durationMonths: e.target.value})}
              min="0"
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors duration-200"
              placeholder="Deja vacío para indefinido"
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
              className="px-6 py-3 bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700 text-white rounded-lg transition-all duration-200 font-medium shadow-lg hover:shadow-xl"
            >
              {editingContractType ? 'Actualizar' : 'Crear'} Tipo de Contrato
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Contracts;