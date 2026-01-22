import { useState, useCallback, useRef } from 'react';

/**
 * Hook personalizado para manejar operaciones asíncronas con estados de carga
 * Previene múltiples ejecuciones simultáneas y proporciona feedback visual
 *
 * @param {Function} asyncFunction - La función asíncrona a ejecutar
 * @returns {Object} - { execute, isLoading, error }
 */
const useAsyncOperation = (asyncFunction) => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const isRunningRef = useRef(false);

  const execute = useCallback(async (...args) => {
    if (isRunningRef.current) {
      return; // Prevenir múltiples ejecuciones
    }

    isRunningRef.current = true;
    setIsLoading(true);
    setError(null);

    try {
      const result = await asyncFunction(...args);
      return result;
    } catch (err) {
      setError(err.message || 'Error desconocido');
      throw err; // Re-lanzar para que el componente pueda manejarlo
    } finally {
      setIsLoading(false);
      isRunningRef.current = false;
    }
  }, [asyncFunction]);

  return {
    execute,
    isLoading,
    error,
    reset: () => setError(null)
  };
};

export default useAsyncOperation;