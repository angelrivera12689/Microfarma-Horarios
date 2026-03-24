-- =============================================
-- Script para configurar Domiciliarios
-- Ejecutar en la base de datos microfarmahorarios
-- =============================================

-- 1. Insertar posición "Domiciliario"
INSERT INTO position (id, name, description, salary, created_at, updated_at, is_active) 
VALUES (UUID(), 'Domiciliario', 'Personal de entregas a domicilio', 0, NOW(), NOW(), true);

-- 2. Obtener el ID de la posición Domiciliario para usar en los inserts siguientes
-- (El ID se genera automáticamente, lo obtenemos con:)
-- SELECT * FROM position WHERE name = 'Domiciliario';

-- =============================================
-- ShiftType para Domiciliarios (seInsertan manualmente)
-- =============================================

-- ShiftType 1: Turno Mañana (8:00 AM - 4:00 PM)
INSERT INTO shift_type (id, name, description, start_time, end_time, is_night_shift, is_multi_range, created_at, updated_at, is_active)
VALUES (UUID(), 'Domiciliario Mañana', 'Turno de 8:00 AM a 4:00 PM', '08:00:00', '16:00:00', false, false, NOW(), NOW(), true);

-- ShiftType 2: Turno Tarde (2:00 PM - 10:00 PM)
INSERT INTO shift_type (id, name, description, start_time, end_time, is_night_shift, is_multi_range, created_at, updated_at, is_active)
VALUES (UUID(), 'Domiciliario Tarde', 'Turno de 2:00 PM a 10:00 PM', '14:00:00', '22:00:00', true, false, NOW(), NOW(), true);

-- ShiftType 3: Descanso (8:00 AM - 10:00 PM) - Disponible para días de descanso
INSERT INTO shift_type (id, name, description, start_time, end_time, is_night_shift, is_multi_range, created_at, updated_at, is_active)
VALUES (UUID(), 'Domiciliario Disponible', 'Día de descanso - disponible 8:00 AM a 10:00 PM', '08:00:00', '22:00:00', false, false, NOW(), NOW(), true);

-- =============================================
-- NOTAS:
-- 1. Ejecutar este script en MySQL
-- 2. Los UUID() de MySQL generan IDs únicos
-- 3. Después de ejecutar, los ShiftType aparecerán en "Tipos de Turno"
-- 4. Para asignar empleados, usar "Turnos" y filtrar por posición Domiciliario
-- =============================================
