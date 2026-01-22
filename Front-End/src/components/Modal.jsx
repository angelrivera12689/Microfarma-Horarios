import { useEffect } from 'react';

const Modal = ({ isOpen, onClose, title, children, size = 'md', footer, preventClose = false }) => {
  useEffect(() => {
    const handleEscape = (e) => {
      if (e.key === 'Escape' && !preventClose) {
        onClose();
      }
    };

    if (isOpen) {
      document.addEventListener('keydown', handleEscape);
      document.body.style.overflow = 'hidden';
    }

    return () => {
      document.removeEventListener('keydown', handleEscape);
      document.body.style.overflow = 'unset';
    };
  }, [isOpen, onClose, preventClose]);

  if (!isOpen) return null;

  const sizeClasses = {
    sm: 'max-w-md',
    md: 'max-w-lg',
    lg: 'max-w-2xl',
    xl: 'max-w-4xl',
    full: 'max-w-full'
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto flex items-center justify-center p-4">
      {/* Background overlay */}
      <div
        className={`absolute inset-0 bg-black bg-opacity-50 transition-opacity ${preventClose ? 'cursor-not-allowed' : ''}`}
        onClick={preventClose ? undefined : onClose}
      ></div>

      {/* Modal panel */}
      <div className={`relative bg-white rounded-2xl text-left overflow-hidden shadow-xl transform transition-all ${sizeClasses[size]} w-full max-w-lg`}>
        {/* Header */}
        <div className="bg-gradient-to-r from-red-500 to-red-600 px-6 py-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-semibold text-white">{title}</h3>
            <button
              onClick={preventClose ? undefined : onClose}
              className={`text-white hover:text-gray-200 transition-colors duration-200 ${preventClose ? 'cursor-not-allowed opacity-50' : ''}`}
            >
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        {/* Content */}
        <div className="px-6 py-6">
          {children}
        </div>

        {/* Footer */}
        {footer && (
          <div className="bg-gray-50 px-6 py-4 flex justify-end space-x-3">
            {footer}
          </div>
        )}
      </div>
    </div>
  );
};

export default Modal;