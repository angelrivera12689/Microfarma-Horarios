import { useState, useEffect } from 'react';
import Modal from '../../components/Modal';
import Button from '../../components/Button';
import hourTemplateService from '../../services/hourTemplateService';

const TemplateTypes = () => {
  const [templates, setTemplates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editingTemplate, setEditingTemplate] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    ordinarias: 0,
    diurnas: 0,
    nocturnas: 0,
    extraFDom: 0,
    dominicales: 0
  });

  const hourlyRates = hourTemplateService.getHourlyRates();

  useEffect(() => {
    loadTemplates();
  }, []);

  const loadTemplates = async () => {
    try {
      const response = await hourTemplateService.getAllTemplates();
      if (response.data) {
        setTemplates(response.data);
      }
    } catch (error) {
      console.error('Error loading templates:', error);
      alert('Error al cargar las plantillas');
    } finally {
      setLoading(false);
    }
  };

  const calculateRowTotal = (template) => {
    return hourTemplateService.calculateTotals(template);
  };

  const handleAdd = () => {
    setEditingTemplate(null);
    setFormData({
      name: '',
      ordinarias: 0,
      diurnas: 0,
      nocturnas: 0,
      extraFDom: 0,
      dominicales: 0
    });
    setModalOpen(true);
  };

  const handleEdit = (template) => {
    setEditingTemplate(template);
    setFormData({
      name: template.name,
      ordinarias: template.ordinarias,
      diurnas: template.diurnas,
      nocturnas: template.nocturnas,
      extraFDom: template.extraFDom,
      dominicales: template.dominicales
    });
    setModalOpen(true);
  };

  const handleDelete = async (template) => {
    if (window.confirm(`¿Estás seguro de que quieres eliminar la plantilla "${template.name}"?`)) {
      const updatedTemplates = templates.filter(t => t.id !== template.id);
      await hourTemplateService.saveTemplates(updatedTemplates);
      setTemplates(updatedTemplates);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      let updatedTemplates;
      
      if (editingTemplate) {
        // Update existing template
        updatedTemplates = templates.map(t => 
          t.id === editingTemplate.id 
            ? { ...t, ...formData } 
            : t
        );
      } else {
        // Create new template
        const newTemplate = {
          ...formData,
          id: Date.now()
        };
        updatedTemplates = [...templates, newTemplate];
      }

      await hourTemplateService.saveTemplates(updatedTemplates);
      setTemplates(updatedTemplates);
      setModalOpen(false);
    } catch (error) {
      console.error('Error saving template:', error);
      alert('Error al guardar la plantilla');
    }
  };

  const handleReset = async () => {
    if (window.confirm('¿Estás seguro de que quieres restaurar las plantillas por defecto?')) {
      const response = await hourTemplateService.resetToDefaults();
      setTemplates(response.data);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: parseFloat(value) || 0
    }));
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
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Plantillas de Horas</h1>
          <p className="text-gray-600 mt-1">Administra las plantillas de horarios con sus tipos de horas</p>
        </div>
        <div className="flex space-x-3">
          <Button variant="outline" onClick={handleReset}>
            Restaurar Valores
          </Button>
          <Button onClick={handleAdd}>
            + Nueva Plantilla
          </Button>
        </div>
      </div>

      {/* Hourly Rates Info */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
        <h3 className="font-semibold text-blue-900 mb-2">Valores Unitarios por Hora</h3>
        <div className="grid grid-cols-5 gap-4 text-sm">
          <div className="text-center">
            <span className="block font-medium text-blue-700">ORDINARIA</span>
            <span className="text-blue-900">${hourlyRates.ordinaria.toFixed(3)}</span>
          </div>
          <div className="text-center">
            <span className="block font-medium text-blue-700">DIURNA</span>
            <span className="text-blue-900">${hourlyRates.diurna.toFixed(3)}</span>
          </div>
          <div className="text-center">
            <span className="block font-medium text-blue-700">NOCTURNA</span>
            <span className="text-blue-900">${hourlyRates.nocturna.toFixed(3)}</span>
          </div>
          <div className="text-center">
            <span className="block font-medium text-blue-700">EXTRA F/DOM</span>
            <span className="text-blue-900">${hourlyRates.extraFDom.toFixed(3)}</span>
          </div>
          <div className="text-center">
            <span className="block font-medium text-blue-700">DOMINICAL</span>
            <span className="text-blue-900">${hourlyRates.dominical.toFixed(3)}</span>
          </div>
        </div>
      </div>

      {/* Templates Table */}
      <div className="bg-white rounded-2xl shadow-xl border border-gray-100 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gradient-to-r from-red-500 to-red-600 text-white">
              <tr>
                <th className="px-6 py-4 text-left font-semibold">Plantilla</th>
                <th className="px-6 py-4 text-center font-semibold">ORDINARIA</th>
                <th className="px-6 py-4 text-center font-semibold">DIURNA</th>
                <th className="px-6 py-4 text-center font-semibold">NOCTURNA</th>
                <th className="px-6 py-4 text-center font-semibold">EXTRA F O DOM</th>
                <th className="px-6 py-4 text-center font-semibold">DOMINICAL</th>
                <th className="px-6 py-4 text-center font-semibold">TOTAL HORAS</th>
                <th className="px-6 py-4 text-center font-semibold">TOTAL VALOR</th>
                <th className="px-6 py-4 text-center font-semibold">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {templates.map((template, index) => {
                const totals = calculateRowTotal(template);
                return (
                  <tr key={template.id} className={index % 2 === 0 ? 'bg-white' : 'bg-gray-50'}>
                    <td className="px-6 py-4 font-medium text-gray-900">{template.name}</td>
                    <td className="px-6 py-4 text-center text-gray-700">
                      {template.ordinarias > 0 ? template.ordinarias.toFixed(2) : '-'}
                    </td>
                    <td className="px-6 py-4 text-center text-gray-700">
                      {template.diurnas > 0 ? template.diurnas.toFixed(2) : '-'}
                    </td>
                    <td className="px-6 py-4 text-center text-gray-700">
                      {template.nocturnas > 0 ? template.nocturnas.toFixed(2) : '-'}
                    </td>
                    <td className="px-6 py-4 text-center text-gray-700">
                      {template.extraFDom > 0 ? template.extraFDom.toFixed(2) : '-'}
                    </td>
                    <td className="px-6 py-4 text-center text-gray-700">
                      {template.dominicales > 0 ? template.dominicales.toFixed(2) : '-'}
                    </td>
                    <td className="px-6 py-4 text-center font-semibold text-gray-900">
                      {totals.totalHours.toFixed(2)}
                    </td>
                    <td className="px-6 py-4 text-center font-semibold text-green-600">
                      {totals.totalValue.toFixed(3)}
                    </td>
                    <td className="px-6 py-4 text-center">
                      <div className="flex justify-center space-x-2">
                        <button
                          onClick={() => handleEdit(template)}
                          className="text-blue-600 hover:text-blue-800 font-medium text-sm"
                        >
                          Editar
                        </button>
                        <button
                          onClick={() => handleDelete(template)}
                          className="text-red-600 hover:text-red-800 font-medium text-sm"
                        >
                          Eliminar
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Edit/Create Modal */}
      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editingTemplate ? 'Editar Plantilla' : 'Nueva Plantilla'}
        size="lg"
        footer={
          <>
            <Button variant="outline" onClick={() => setModalOpen(false)}>
              Cancelar
            </Button>
            <Button onClick={handleSubmit}>
              {editingTemplate ? 'Guardar Cambios' : 'Crear Plantilla'}
            </Button>
          </>
        }
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Nombre de la Plantilla
            </label>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
              required
            />
          </div>

          <div className="grid grid-cols-5 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Horas Ordinarias
              </label>
              <input
                type="number"
                name="ordinarias"
                value={formData.ordinarias}
                onChange={handleInputChange}
                step="0.01"
                min="0"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
              />
              <span className="text-xs text-gray-500">${hourlyRates.ordinaria}/hr</span>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Horas Diurnas
              </label>
              <input
                type="number"
                name="diurnas"
                value={formData.diurnas}
                onChange={handleInputChange}
                step="0.01"
                min="0"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
              />
              <span className="text-xs text-gray-500">${hourlyRates.diurna}/hr</span>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Horas Nocturnas
              </label>
              <input
                type="number"
                name="nocturnas"
                value={formData.nocturnas}
                onChange={handleInputChange}
                step="0.01"
                min="0"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
              />
              <span className="text-xs text-gray-500">${hourlyRates.nocturna}/hr</span>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Extra F/Dom
              </label>
              <input
                type="number"
                name="extraFDom"
                value={formData.extraFDom}
                onChange={handleInputChange}
                step="0.01"
                min="0"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
              />
              <span className="text-xs text-gray-500">${hourlyRates.extraFDom}/hr</span>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Horas Dominicales
              </label>
              <input
                type="number"
                name="dominicales"
                value={formData.dominicales}
                onChange={handleInputChange}
                step="0.01"
                min="0"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
              />
              <span className="text-xs text-gray-500">${hourlyRates.dominical}/hr</span>
            </div>
          </div>

          {/* Preview totals */}
          <div className="bg-gray-50 p-4 rounded-lg mt-4">
            <div className="flex justify-between text-sm">
              <span className="font-medium">Total Horas:</span>
              <span className="font-bold">
                {(
                  (parseFloat(formData.ordinarias) || 0) +
                  (parseFloat(formData.diurnas) || 0) +
                  (parseFloat(formData.nocturnas) || 0) +
                  (parseFloat(formData.extraFDom) || 0) +
                  (parseFloat(formData.dominicales) || 0)
                ).toFixed(2)}
              </span>
            </div>
            <div className="flex justify-between text-sm mt-2">
              <span className="font-medium">Total Valor:</span>
              <span className="font-bold text-green-600">
                {(
                  (parseFloat(formData.ordinarias) || 0) * hourlyRates.ordinaria +
                  (parseFloat(formData.diurnas) || 0) * hourlyRates.diurna +
                  (parseFloat(formData.nocturnas) || 0) * hourlyRates.nocturna +
                  (parseFloat(formData.extraFDom) || 0) * hourlyRates.extraFDom +
                  (parseFloat(formData.dominicales) || 0) * hourlyRates.dominical
                ).toFixed(3)}
              </span>
            </div>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default TemplateTypes;
