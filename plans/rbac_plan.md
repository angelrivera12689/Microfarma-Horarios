# Plan de Implementación del Sistema RBAC (Control de Acceso Basado en Roles)

## Resumen
Implementar un sistema de control de acceso basado en roles con tres niveles: USER (usuario básico), EMPLOYEE (empleado con acceso específico), y ADMIN (administrador completo). Los empleados tendrán acceso a UserLanding con detalles completos de turnos, solicitudes y horas laborales, mientras que los usuarios comunes verán una versión simplificada.

## Roles Definidos
- **USER**: Acceso básico, versión simplificada de UserLanding sin detalles de turnos/solicitudes.
- **EMPLOYEE**: Acceso a gestión de usuarios limitada y UserLanding con detalles completos (turnos, solicitudes de cambio, horas laborales).
- **ADMIN**: Acceso completo, incluyendo asignación de roles y gestión administrativa.

## Pasos de Implementación

### 1. Actualizar DataInitializer
- Modificar `DataInitializer.java` para sembrar los roles USER, EMPLOYEE y ADMIN en la base de datos.
- Asegurar que no haya conflictos con datos existentes inconsistentes.

### 2. Modificar Registro de Usuarios
- Actualizar el controlador de autenticación para asignar automáticamente el rol 'USER' a nuevos usuarios registrados.
- Verificar que el esquema de usuario soporte esto sin violar restricciones de FK.

### 3. Endpoint para Asignación de Roles
- Crear un endpoint en `UserControllerSecurity` que permita a los admins asignar el rol 'EMPLOYEE' a usuarios 'USER' existentes.
- Incluir validaciones para evitar inconsistencias de datos.

### 4. Control de Acceso en Backend
- Agregar anotaciones `@PreAuthorize` en controladores para restringir acceso basado en roles.
- Ejemplos:
  - `@PreAuthorize("hasRole('EMPLOYEE') or hasRole('ADMIN')")` para endpoints de gestión de turnos.
  - `@PreAuthorize("hasRole('ADMIN')")` para endpoints de administración.

### 5. Actualizar UserLanding en Frontend
- Modificar `UserLanding.jsx` para renderizar contenido basado en el rol del usuario.
- **Para USER**: Mostrar solo perfil básico y versión simplificada sin estadísticas de turnos.
- **Para EMPLOYEE**: Mostrar estadísticas completas, turnos, solicitudes de cambio, horas laborales.
- **Para ADMIN**: Mostrar todo, incluyendo opciones administrativas.

### 6. Resolver Problemas de Base de Datos
- Corregir restricciones de clave foránea inconsistentes en tablas employee y shift.
- Ejecutar consultas SQL para establecer FK inválidas a NULL antes de aplicar restricciones.
- Si persisten problemas, considerar recrear la base de datos para un esquema limpio.

### 7. Pruebas
- Probar funcionalidad RBAC con usuarios de diferentes roles.
- Verificar que el acceso esté correctamente restringido.
- Asegurar consistencia de datos y manejo de errores de FK.

## Consideraciones Técnicas
- Utilizar Spring Security con `@EnableMethodSecurity` para autorizaciones basadas en métodos.
- El frontend debe obtener el rol del usuario desde el token JWT y ajustar la UI en consecuencia.
- Mantener compatibilidad con el esquema existente de base de datos.

## Riesgos y Mitigaciones
- **Datos Inconsistentes**: Resolver problemas de FK antes de implementar RBAC.
- **Cambios en UI**: Probar exhaustivamente para evitar UX degradada.
- **Seguridad**: Asegurar que las autorizaciones sean estrictas y no permitan escalada de privilegios.

¿Apruebas este plan detallado? Si sí, procedemos a la implementación; si no, sugiere modificaciones.