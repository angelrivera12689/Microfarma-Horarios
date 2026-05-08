# Sistema de Reportes - Documentación Técnica

## Visión General

Este documento describe la implementación completa del sistema de reportes para el módulo de gestión de horarios de Microfarma. El sistema incluye tres tipos de reportes (general, por sede, por empleado) con filtros dinámicos desde el backend y generación de PDFs profesionales.

---

## Arquitectura del Sistema

### Componentes Principales

1. **Backend (Java/Spring Boot)**
   - `SchedulesReportController.java`: Controlador REST con endpoints para filtros y reportes
   - `SchedulesReportService.java`: Lógica de negocio para generación de reportes
   - `ISchedulesReportService.java`: Interfaz del servicio

2. **DTOs (Data Transfer Objects)**
   - `ReportFiltersDto.java`: Filtros disponibles desde el backend
   - `ReportRequestDto.java`: Parámetros de solicitud de reporte
   - `GlobalReportDto.java`: Resumen global
   - `EmployeeReportDto.java`: Datos por empleado
   - `LocationReportDto.java`: Datos por sede
   - `OvertimeDetailDto.java`: Detalles de horas extras

3. **Frontend (React)**
   - `Reports.jsx`: Componente principal de reportes
   - `reportService.js`: Servicio de API para reportes

---

## Endpoints de API

### Filtros

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/schedules/reports/filters` | Obtiene todos los filtros disponibles |
| GET | `/api/schedules/reports/filters/locations` | Obtiene lista de sedes |
| GET | `/api/schedules/reports/filters/employees` | Obtiene lista de empleados |
| GET | `/api/schedules/reports/filters/years` | Obtiene lista de años disponibles |

### Reportes

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/schedules/reports/monthly` | Reporte general mensual |
| GET | `/api/schedules/reports/monthly/by-location` | Reporte filtrado por sede |
| GET | `/api/schedules/reports/monthly/by-employee` | Reporte filtrado por empleado |
| GET | `/api/schedules/reports/global` | Resumen global del mes |
| GET | `/api/schedules/reports/location/{id}` | Reporte de sede específica |
| GET | `/api/schedules/reports/employee/{id}` | Reporte de empleado específico |

### Exportación

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/schedules/reports/monthly/csv` | Exporta reporte a CSV |
| GET | `/api/schedules/reports/monthly/pdf` | Exporta reporte general a PDF |
| GET | `/api/schedules/reports/monthly/pdf/general` | Exporta reporte general |
| GET | `/api/schedules/reports/monthly/pdf/location` | Exporta reporte por sede |
| GET | `/api/schedules/reports/monthly/pdf/employee` | Exporta reporte por empleado |

---

## Tipos de Reporte

### 1. Reporte General

Muestra una vista consolidada de todos los datos sin filtrar:
- Resumen global con estadísticas completas
- Listado de empleados con métricas individuales
- Resumen por sede
- Total de horas trabajadas, horas extras, dominicales y festivas

**Endpoint:** `GET /api/schedules/reports/monthly?month=X&year=Y`

### 2. Reporte por Sede

Agrupa y filtra datos según la sede seleccionada:
- Métricas consolidadas de la sede
- Listado de empleados asignados a esa sede
- Comparativa de desempeño por ubicación
- Totales específicos de la sede

**Endpoint:** `GET /api/schedules/reports/monthly/by-location?month=X&year=Y&locationId=ID`

### 3. Reporte por Empleado

Presenta datos individuales de cada empleado:
- Horas trabajadas desglosadas por tipo
- Promedio diario y semanal
- Detalle de horas extras con fechas y justificaciones
- Métricas personales de desempeño

**Endpoint:** `GET /api/schedules/reports/monthly/by-employee?month=X&year=Y&employeeId=ID`

---

## Estructura de Datos

### ReportFiltersDto

```java
public class ReportFiltersDto {
    List<LocationFilterOption> locations;  // Sedes disponibles
    List<EmployeeFilterOption> employees; // Empleados disponibles
    List<YearOption> years;               // Años disponibles
    List<StatusOption> statuses;           // Estados disponibles
}
```

### ReportResponseDto

```java
public class ReportResponseDto {
    GlobalReportDto global;               // Resumen global
    List<LocationReportDto> locations;    // Reportes por sede
    List<EmployeeReportDto> employees;   // Reportes por empleado
}
```

---

## Generación de PDFs

### Plantillas PDF

1. **PDF General**: Tablas consolidadas con resumen global, por sede y por empleado
2. **PDF Sede**: Datos específicos de la sede con listados de empleados
3. **PDF Empleado**: Información detallada individual con desglose de horas

### Características de los PDFs

- Encabezado con branding de Microfarma
- Tablas con formato profesional
- Colores según la identidad visual (rojo corporativo)
- Pie de página con fecha de generación
- Diseño responsive para impresión

---

## Consultas de Base de Datos

### Optimización de Queries

Las consultas están optimizadas para manejar grandes volúmenes de datos:

```sql
-- Query para obtener turnos por rango de fechas
SELECT * FROM shift 
WHERE date BETWEEN :startDate AND :endDate
  AND status = true
  AND employee_id IS NOT NULL
  AND shift_type_id IS NOT NULL
ORDER BY date, location_id, employee_id
```

### Índices Recomendados

```sql
CREATE INDEX idx_shift_date ON shift(date);
CREATE INDEX idx_shift_location ON shift(location_id);
CREATE INDEX idx_shift_employee ON shift(employee_id);
CREATE INDEX idx_shift_date_location ON shift(date, location_id);
CREATE INDEX idx_shift_date_employee ON shift(date, employee_id);
```

---

## Uso del Frontend

### Carga de Filtros

```javascript
// Los filtros se cargan automáticamente al montar el componente
useEffect(() => {
  loadFilters();
}, []);
```

### Generación de Reportes

```javascript
// El reporte se genera automáticamente al cambiar filtros
useEffect(() => {
  loadReport();
}, [selectedMonth, selectedYear, reportType, selectedLocation, selectedEmployee]);
```

### Exportación

```javascript
// Exportar a CSV
await reportService.exportCsv(month, year);

// Exportar a PDF según tipo
await reportService.exportPdf(month, year, { 
  reportType: 'general' | 'location' | 'employee',
  locationName: '...',
  employeeId: '...'
});
```

---

## Consideraciones de Seguridad

- Todos los endpoints requieren autenticación
- Solo usuarios con rol ADMIN pueden acceder a los reportes
- Los filtros se validan en el backend antes de ejecutar consultas

---

## Mantenimiento y Escalabilidad

### Para Agregar Nuevos Filtros

1. Agregar el campo en `ReportFiltersDto.java`
2. Implementar la lógica de obtención en `SchedulesReportService.java`
3. Agregar endpoint en `SchedulesReportController.java`
4. Actualizar el frontend en `Reports.jsx`

### Para Agregar Nuevos Tipos de Reporte

1. Crear nuevo método en `ISchedulesReportService.java`
2. Implementar en `SchedulesReportService.java`
3. Agregar endpoint en `SchedulesReportController.java`
4. Crear plantilla PDF específica
5. Actualizar el frontend

---

## Historial de Cambios

| Versión | Fecha | Descripción |
|----------|-------|-------------|
| 1.0.0 | 2024-02-11 | Implementación inicial del sistema de reportes |

---

## Referencias

- Documentación iTextPDF: https://itextpdf.com/
- Spring Boot Documentation: https://spring.io/projects/spring-boot
- React Documentation: https://react.dev/
