import { useState, useEffect } from 'react';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import positionService from '../../services/positionService';

const Positions = () => {
  const [positions, setPositions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingPosition, setEditingPosition] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    salary: ''
  });

  useEffect(() => {
    loadPositions();
  }, []);

  const loadPositions = async () => {
    try {
      const response = await positionService.getAllPositions();
      if (response.data) {
        setPositions(Array.isArray(response.data) ? response.data : []);
      } else {
        console.error('Failed to load positions:', response.message);
        alert('Error al cargar posiciones: ' + (response.message || 'Error desconocido'));
      }
    } catch (error) {
      console.error('Error loading positions:', error);
      alert('Error de conexión al cargar posiciones');
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingPosition(null);
    setFormData({
      name: '',
      description: '',
      salary: ''
    });
    setModalOpen(true);
  };

  const handleEdit = (position) => {
    setEditingPosition(position);
    setFormData({
      name: position.name || '',
      description: position.description || '',
      salary: position.salary || ''
    });
    setModalOpen(true);
  };

  const handleDelete = async (position) => {
    if (window.confirm(`¿Estás seguro de que quieres eliminar la posición "${position.name}"?`)) {
      try {
        await positionService.deletePosition(position.id);
        await loadPositions();
      } catch (error) {
        console.error('Error deleting position:', error);
        alert('Error al eliminar la posición');
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const positionData = {
        name: formData.name,
        description: formData.description,
        salary: parseFloat(formData.salary)
      };

      if (editingPosition) {
        await positionService.updatePosition(editingPosition.id, positionData);
      } else {
        await positionService.createPosition(positionData);
      }

      setModalOpen(false);
      await loadPositions();
    } catch (error) {
      console.error('Error saving position:', error);
      alert('Error al guardar la posición');
    }
  };

  const columns = [
    { key: 'name', header: 'Nombre', render: (value) => <span className="font-medium">{value}</span> },
    { key: 'description', header: 'Descripción' },
    { key: 'salary', header: 'Salario', render: (value) => value ? `$${value.toLocaleString()}` : '' }
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Gestión de Posiciones</h1>
          <p className="text-gray-600 mt-1">Administra las posiciones laborales</p>
        </div>
      </div>

      <DataTable
        title="Posiciones"
        icon="🏢"
        columns={columns}
        data={positions}
        loading={loading}
        onAdd={handleAdd}
        onEdit={handleEdit}
        onDelete={handleDelete}
        addButtonText="Agregar Posición"
        searchPlaceholder="Buscar posiciones..."
        emptyMessage="No hay posiciones registradas en el sistema"
      />

      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editingPosition ? 'Editar Posición' : 'Agregar Posición'}
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
              placeholder="Ingresa el nombre de la posición"
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
              placeholder="Ingresa la descripción de la posición"
            />
          </div>

          <div>
            <label htmlFor="salary" className="block text-sm font-medium text-gray-700 mb-2">
              Salario *
            </label>
            <input
              type="number"
              id="salary"
              value={formData.salary}
              onChange={(e) => setFormData({...formData, salary: e.target.value})}
              required
              min="0"
              step="0.01"
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors duration-200"
              placeholder="Ingresa el salario"
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
              {editingPosition ? 'Actualizar' : 'Crear'} Posición
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default Positions;