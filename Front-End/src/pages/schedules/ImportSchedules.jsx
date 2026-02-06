import React, { useState, useCallback } from 'react';
import { importPdf } from '../../services/importService';
import locationService from '../../services/locationService';
import { useNavigate } from 'react-router-dom';

const ImportSchedules = () => {
  const [file, setFile] = useState(null);
  const [locationId, setLocationId] = useState('');
  const [overwrite, setOverwrite] = useState(false);
  const [importing, setImporting] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [dragActive, setDragActive] = useState(false);
  const [locations, setLocations] = useState([]);
  const navigate = useNavigate();

  // Load locations on mount
  React.useEffect(() => {
    const loadLocations = async () => {
      try {
        const response = await locationService.getAllLocations();
        if (response.success) {
          setLocations(response.data || []);
        }
      } catch (err) {
        console.error('Error loading locations:', err);
      }
    };
    loadLocations();
  }, []);

  const handleDrag = useCallback((e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  }, []);

  const handleDrop = useCallback((e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      const droppedFile = e.dataTransfer.files[0];
      if (droppedFile.type === 'application/pdf') {
        setFile(droppedFile);
        setError(null);
      } else {
        setError('Solo se aceptan archivos PDF');
      }
    }
  }, []);

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];
    if (selectedFile) {
      if (selectedFile.type === 'application/pdf') {
        setFile(selectedFile);
        setError(null);
      } else {
        setError('Solo se aceptan archivos PDF');
      }
    }
  };

  const handleImport = async () => {
    if (!file) {
      setError('Por favor selecciona un archivo PDF');
      return;
    }

    setImporting(true);
    setError(null);
    setResult(null);

    try {
      const response = await importPdf(file, locationId, overwrite);
      setResult(response);
      if (response.success) {
        // Optionally refresh shifts data
        // await shiftService.loadShifts();
      }
    } catch (err) {
      // Show the actual error message from backend
      const errorMessage = err.message || 'Error al importar el archivo';
      setError(errorMessage);
    } finally {
      setImporting(false);
    }
  };

  const handleCancel = () => {
    navigate('/schedules/shifts');
  };

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Importar Horarios desde PDF</h1>
        <p className="text-gray-600 mt-1">
          Sube un archivo PDF con horarios de trabajo para registrar los turnos automáticamente.
        </p>
      </div>

      {/* Template Info */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
        <h3 className="font-semibold text-blue-800 mb-2">📋 Formato Esperado</h3>
        <ul className="text-sm text-blue-700 space-y-1">
          <li>• El PDF debe contener información del empleado (nombre y código)</li>
          <li>• Sede de trabajo</li>
          <li>• Mes y año del horario</li>
          <li>• Lista de turnos con fecha, hora de inicio y fin</li>
          <li>• Turnos reconocidos: Mañana (08:00-16:00), Tarde (14:00-22:00), Noche (22:00-06:00)</li>
          <li>• Días libre: "LIBRE" o "REST"</li>
        </ul>
      </div>

      {/* Error Message */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
          {error}
        </div>
      )}

      {/* Success/Result Message */}
      {result && (
        <div className={`border rounded-lg p-4 mb-6 ${result.success ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'}`}>
          <div className="flex items-center mb-2">
            <span className={`text-2xl mr-2 ${result.success ? '✅' : '❌'}`}></span>
            <h3 className={`font-semibold ${result.success ? 'text-green-800' : 'text-red-800'}`}>
              {result.success ? 'Importación Exitosa' : 'Error en la Importación'}
            </h3>
          </div>
          
          {result.success ? (
            <>
              <p className="text-green-700 mb-3">
                Se importaron <strong>{result.totalShiftsImported}</strong> turnos correctamente.
              </p>
              {result.importedShifts && result.importedShifts.length > 0 && (
                <div className="mt-3">
                  <h4 className="font-medium text-green-800 mb-2">Turnos importados:</h4>
                  <div className="max-h-40 overflow-y-auto">
                    <table className="w-full text-sm">
                      <thead>
                        <tr className="bg-green-100">
                          <th className="text-left p-2">Fecha</th>
                          <th className="text-left p-2">Turno</th>
                          <th className="text-left p-2">Estado</th>
                        </tr>
                      </thead>
                      <tbody>
                        {result.importedShifts.slice(0, 10).map((shift, idx) => (
                          <tr key={idx} className="border-b border-green-200">
                            <td className="p-2">{shift.date}</td>
                            <td className="p-2">{shift.shiftTypeName}</td>
                            <td className="p-2">
                              <span className={`px-2 py-1 rounded text-xs ${
                                shift.status === 'CREATED' ? 'bg-green-200 text-green-800' :
                                shift.status === 'UPDATED' ? 'bg-yellow-200 text-yellow-800' :
                                'bg-gray-200 text-gray-800'
                              }`}>
                                {shift.status}
                              </span>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                    {result.importedShifts.length > 10 && (
                      <p className="text-sm text-green-600 mt-2">
                        ... y {result.importedShifts.length - 10} turnos más
                      </p>
                    )}
                  </div>
                </div>
              )}
            </>
          ) : (
            <>
              <p className="text-red-700 mb-2">{result.message}</p>
              {result.errors && result.errors.length > 0 && (
                <ul className="list-disc list-inside text-sm text-red-600">
                  {result.errors.map((err, idx) => (
                    <li key={idx}>{err}</li>
                  ))}
                </ul>
              )}
            </>
          )}
        </div>
      )}

      {/* Upload Area */}
      <div className="bg-white border border-gray-200 rounded-lg p-6 mb-6">
        <div
          className={`border-2 border-dashed rounded-xl p-8 text-center transition-colors ${
            dragActive ? 'border-purple-500 bg-purple-50' : 'border-gray-300 hover:border-gray-400'
          }`}
          onDragEnter={handleDrag}
          onDragLeave={handleDrag}
          onDragOver={handleDrag}
          onDrop={handleDrop}
        >
          {file ? (
            <div className="flex items-center justify-center">
              <span className="text-4xl mr-3">📄</span>
              <div>
                <p className="font-medium text-gray-800">{file.name}</p>
                <p className="text-sm text-gray-500">
                  {(file.size / 1024).toFixed(2)} KB
                </p>
              </div>
              <button
                onClick={() => setFile(null)}
                className="ml-4 text-red-500 hover:text-red-700"
              >
                ✕
              </button>
            </div>
          ) : (
            <>
              <span className="text-5xl block mb-3">📁</span>
              <p className="text-gray-700 mb-2">
                Arrastra y suelta tu archivo PDF aquí
              </p>
              <p className="text-gray-500 text-sm mb-4">o</p>
              <label className="cursor-pointer bg-purple-600 text-white px-4 py-2 rounded-lg hover:bg-purple-700 transition-colors">
                Seleccionar Archivo
                <input
                  type="file"
                  accept=".pdf"
                  onChange={handleFileChange}
                  className="hidden"
                />
              </label>
            </>
          )}
        </div>

        {/* Options */}
        <div className="mt-6 grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Sede (Opcional)
            </label>
            <select
              value={locationId}
              onChange={(e) => setLocationId(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-purple-500"
            >
              <option value="">Seleccionar sede...</option>
              {locations.map((loc) => (
                <option key={loc.id} value={loc.id}>
                  {loc.name}
                </option>
              ))}
            </select>
          </div>
          
          <div className="flex items-center mt-6">
            <input
              type="checkbox"
              id="overwrite"
              checked={overwrite}
              onChange={(e) => setOverwrite(e.target.checked)}
              className="h-4 w-4 text-purple-600 focus:ring-purple-500 border-gray-300 rounded"
            />
            <label htmlFor="overwrite" className="ml-2 text-sm text-gray-700">
              Sobreescribir turnos existentes
            </label>
          </div>
        </div>
      </div>

      {/* Actions */}
      <div className="flex justify-end space-x-4">
        <button
          onClick={handleCancel}
          className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
          disabled={importing}
        >
          Cancelar
        </button>
        <button
          onClick={handleImport}
          disabled={!file || importing}
          className={`px-6 py-2 rounded-lg transition-colors ${
            !file || importing
              ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
              : 'bg-purple-600 text-white hover:bg-purple-700'
          }`}
        >
          {importing ? (
            <span className="flex items-center">
              <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Importando...
            </span>
          ) : (
            'Importar Horarios'
          )}
        </button>
      </div>
    </div>
  );
};

export default ImportSchedules;
