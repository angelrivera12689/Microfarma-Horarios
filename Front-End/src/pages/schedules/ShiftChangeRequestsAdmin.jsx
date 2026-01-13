import { useState, useEffect } from 'react';
import shiftChangeRequestService from '../../services/shiftChangeRequestService';
import Modal from '../../components/Modal';

const ShiftChangeRequestsAdmin = () => {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showDecisionModal, setShowDecisionModal] = useState(false);
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [decisionData, setDecisionData] = useState({
    approved: false,
    adminDecision: ''
  });
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    loadRequests();
  }, []);

  const loadRequests = async () => {
    try {
      setLoading(true);
      const response = await shiftChangeRequestService.getPendingRequests();
      setRequests(response.data || []);
    } catch (error) {
      console.error('Error loading requests:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDecision = async (request) => {
    setSelectedRequest(request);
    setDecisionData({ approved: false, adminDecision: '' });
    setShowDecisionModal(true);
  };

  const submitDecision = async () => {
    if (!selectedRequest) return;

    setSubmitting(true);
    try {
      await shiftChangeRequestService.decideRequest(selectedRequest.id, decisionData);
      setShowDecisionModal(false);
      setSelectedRequest(null);
      loadRequests();
    } catch (error) {
      console.error('Error submitting decision:', error);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-500"></div>
      </div>
    );
  }

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Solicitudes de Cambio de Turno Pendientes</h1>

      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Empleado
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Fecha Original
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Tipo Original
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Ubicación Original
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Fecha Solicitada
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Tipo Solicitado
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Ubicación Solicitada
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Razón
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Acciones
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {requests.map((request) => (
              <tr key={request.id}>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {request.employeeName}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {request.originalDate}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {request.originalShiftType}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {request.originalLocation}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {request.requestedDate}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {request.requestedShiftType || '-'}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {request.requestedLocation || '-'}
                </td>
                <td className="px-6 py-4 text-sm text-gray-900 max-w-xs truncate">
                  {request.reason}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                  <div className="flex space-x-2">
                    <button
                      onClick={() => handleDecision(request)}
                      className="bg-green-500 hover:bg-green-600 text-white px-3 py-1 rounded text-xs"
                    >
                      Decidir
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Modal
        isOpen={showDecisionModal}
        onClose={() => setShowDecisionModal(false)}
        title="Tomar Decisión"
      >
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Decisión</label>
            <div className="space-x-4">
              <label className="inline-flex items-center">
                <input
                  type="radio"
                  name="decision"
                  checked={decisionData.approved}
                  onChange={() => setDecisionData({...decisionData, approved: true})}
                  className="form-radio h-4 w-4 text-green-600"
                />
                <span className="ml-2">Aprobar</span>
              </label>
              <label className="inline-flex items-center">
                <input
                  type="radio"
                  name="decision"
                  checked={!decisionData.approved}
                  onChange={() => setDecisionData({...decisionData, approved: false})}
                  className="form-radio h-4 w-4 text-red-600"
                />
                <span className="ml-2">Rechazar</span>
              </label>
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Comentario del Administrador</label>
            <textarea
              value={decisionData.adminDecision}
              onChange={(e) => setDecisionData({...decisionData, adminDecision: e.target.value})}
              rows={3}
              className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-red-500 focus:border-red-500"
              placeholder="Explica tu decisión..."
              required
            />
          </div>

          <div className="flex justify-end space-x-3">
            <button
              type="button"
              onClick={() => setShowDecisionModal(false)}
              className="bg-gray-300 hover:bg-gray-400 text-gray-800 px-4 py-2 rounded-lg"
            >
              Cancelar
            </button>
            <button
              onClick={submitDecision}
              disabled={submitting}
              className={`px-4 py-2 rounded-lg text-white ${
                decisionData.approved
                  ? 'bg-green-500 hover:bg-green-600'
                  : 'bg-red-500 hover:bg-red-600'
              } disabled:opacity-50`}
            >
              {submitting ? 'Procesando...' : (decisionData.approved ? 'Aprobar' : 'Rechazar')}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default ShiftChangeRequestsAdmin;