import { useState, useEffect, useRef, useMemo } from 'react';

/**
 * Componente de Autocomplete/Selector con búsqueda en tiempo real
 * 
 * @param {Object} props
 * @param {string} props.placeholder - Placeholder del input
 * @param {string} props.value - Valor seleccionado (ID o valor del item)
 * @param {Function} props.onChange - Callback cuando se selecciona un item
 * @param {Array} props.options - Array de opciones a filtrar
 * @param {Array} props.searchFields - Campos por los cuales buscar (ej: ['firstName', 'lastName', 'email'])
 * @param {string} props.displayField - Campo a mostrar (ej: 'firstName' o función que retorne string)
 * @param {string} props.idField - Campo único identificador (default: 'id')
 * @param {boolean} props.required - Si es requerido
 * @param {string} props.className - Clases CSS adicionales
 * @param {number} props.maxResults - Máximo de resultados a mostrar (default: 10)
 */
const AutocompleteInput = ({
  placeholder = 'Buscar...',
  value,
  onChange,
  options = [],
  searchFields = [],
  displayField,
  idField = 'id',
  required = false,
  className = '',
  maxResults = 10,
  disabled = false
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [isOpen, setIsOpen] = useState(false);
  const [highlightedIndex, setHighlightedIndex] = useState(-1);
  const inputRef = useRef(null);
  const dropdownRef = useRef(null);

  // Función para obtener valor anidado (ej: "position.name")
  const getNestedValue = (obj, path) => {
    if (!obj || !path) return null;
    return path.split('.').reduce((current, key) => {
      return current && current[key] !== undefined ? current[key] : null;
    }, obj);
  };

  // Filtrar opciones basado en el término de búsqueda
  const filteredOptions = useMemo(() => {
    if (!searchTerm.trim()) {
      return options.slice(0, maxResults);
    }

    const term = searchTerm.toLowerCase().trim();
    return options
      .filter(option => {
        if (!option) return false;
        
        // Buscar en los campos especificados
        return searchFields.some(field => {
          const fieldValue = getNestedValue(option, field);
          if (fieldValue == null) return false;
          return String(fieldValue).toLowerCase().includes(term);
        });
      })
      .slice(0, maxResults);
  }, [searchTerm, options, searchFields, maxResults]);

  // Obtener el label del item seleccionado
  const getSelectedLabel = () => {
    if (!value) return '';
    const selected = options.find(opt => getNestedValue(opt, idField) == value);
    if (!selected) return '';
    
    if (typeof displayField === 'function') {
      return displayField(selected);
    }
    return getNestedValue(selected, displayField) || '';
  };

  // Manejar click fuera del dropdown
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        dropdownRef.current && 
        !dropdownRef.current.contains(event.target) &&
        inputRef.current &&
        !inputRef.current.contains(event.target)
      ) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Enfocar input cuando se abre el dropdown
  useEffect(() => {
    if (isOpen && inputRef.current) {
      inputRef.current.focus();
    }
  }, [isOpen]);

  // Resetear highlightedIndex cuando cambian las opciones filtradas
  useEffect(() => {
    setHighlightedIndex(-1);
  }, [filteredOptions]);

  // Resaltar coincidencias en el texto
  const highlightMatch = (text, term) => {
    if (!term.trim() || !text) return text;
    
    const regex = new RegExp(`(${term.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
    const parts = String(text).split(regex);
    
    return parts.map((part, index) => 
      regex.test(part) ? (
        <mark key={index} className="bg-yellow-200 font-medium">
          {part}
        </mark>
      ) : (
        <span key={index}>{part}</span>
      )
    );
  };

  // Manejar teclas
  const handleKeyDown = (e) => {
    if (!isOpen) {
      if (e.key === 'ArrowDown' || e.key === 'Enter') {
        setIsOpen(true);
        e.preventDefault();
      }
      return;
    }

    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        setHighlightedIndex(prev => 
          prev < filteredOptions.length - 1 ? prev + 1 : prev
        );
        break;
      case 'ArrowUp':
        e.preventDefault();
        setHighlightedIndex(prev => prev > 0 ? prev - 1 : -1);
        break;
      case 'Enter':
        e.preventDefault();
        if (highlightedIndex >= 0 && filteredOptions[highlightedIndex]) {
          selectOption(filteredOptions[highlightedIndex]);
        }
        break;
      case 'Escape':
        setIsOpen(false);
        break;
    }
  };

  // Seleccionar una opción
  const selectOption = (option) => {
    const optionId = getNestedValue(option, idField);
    onChange(optionId);
    setSearchTerm('');
    setIsOpen(false);
    setHighlightedIndex(-1);
  };

  // Limpiar selección
  const clearSelection = () => {
    onChange('');
    setSearchTerm('');
    setIsOpen(false);
  };

  // Mostrar dropdown
  const showDropdown = () => {
    if (!disabled) {
      setIsOpen(true);
    }
  };

  return (
    <div className={`relative ${className}`}>
      <div className="relative">
        <input
          ref={inputRef}
          type="text"
          value={isOpen ? searchTerm : getSelectedLabel()}
          onChange={(e) => {
            setSearchTerm(e.target.value);
            setIsOpen(true);
          }}
          onFocus={showDropdown}
          onKeyDown={handleKeyDown}
          placeholder={placeholder}
          disabled={disabled}
          required={required}
          className={`w-full px-4 py-3 pr-10 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors duration-200 ${
            disabled ? 'bg-gray-100 cursor-not-allowed' : ''
          }`}
        />
        
        {/* Icono de dropdown / limpiar */}
        <div className="absolute right-3 top-1/2 -translate-y-1/2 flex items-center">
          {value && !disabled && (
            <button
              type="button"
              onClick={clearSelection}
              className="p-1 hover:bg-gray-100 rounded-full transition-colors"
            >
              <svg className="w-4 h-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          )}
          <svg 
            className={`w-5 h-5 text-gray-400 transition-transform ${isOpen ? 'rotate-180' : ''}`} 
            fill="none" 
            stroke="currentColor" 
            viewBox="0 0 24 24"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
          </svg>
        </div>
      </div>

      {/* Dropdown de resultados */}
      {isOpen && (
        <div
          ref={dropdownRef}
          onClick={(e) => e.stopPropagation()}
          className="absolute z-[60] w-full mt-1 bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-y-auto"
        >
          {filteredOptions.length > 0 ? (
            <ul className="py-1">
              {filteredOptions.map((option, index) => {
                const optionId = getNestedValue(option, idField);
                const label = typeof displayField === 'function' 
                  ? displayField(option) 
                  : getNestedValue(option, displayField) || '';
                
                return (
                  <li
                    key={optionId || index}
                    onClick={() => selectOption(option)}
                    onMouseEnter={() => setHighlightedIndex(index)}
                    className={`px-4 py-2 cursor-pointer transition-colors ${
                      index === highlightedIndex 
                        ? 'bg-blue-50 border-l-4 border-blue-500' 
                        : 'hover:bg-gray-50'
                    }`}
                  >
                    <div className="text-sm">
                      {searchTerm.trim() 
                        ? highlightMatch(label, searchTerm)
                        : label
                      }
                    </div>
                  </li>
                );
              })}
            </ul>
          ) : (
            <div className="px-4 py-3 text-sm text-gray-500">
              {searchTerm.trim() ? 'No se encontraron resultados' : 'Escribe para buscar...'}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default AutocompleteInput;