# 📘 MANUAL DE USO - SISTEMA MICROFARMA-HORARIOS

**Versión:** 1.1  
**Fecha:** 8 de mayo de 2026  
**Aplicación:** Microfarma Gestión de Horarios y Recursos Humanos

---

## 📑 TABLA DE CONTENIDOS

1. [Introducción](#introducción)
2. [Requisitos Previos](#requisitos-previos)
3. [Instalación y Configuración](#instalación-y-configuración)
4. [Autenticación](#autenticación)
5. [Roles y Permisos](#roles-y-permisos)
6. [Guía de Usuario por Rol](#guía-de-usuario-por-rol)
7. [Funcionalidades Principales](#funcionalidades-principales)
8. [Casos de Uso Comunes](#casos-de-uso-comunes)
9. [Guía Detallada de Uso](#guía-detallada-de-uso)
10. [Solución de Problemas](#solución-de-problemas)
11. [FAQ](#faq)

---

## 🎯 Introducción

**Microfarma-Horarios** es un sistema integral de gestión de horarios, recursos humanos y comunicaciones para la cadena de farmacias Microfarma. 

### Objetivos principales:
- ✅ Gestionar turnos y horarios de trabajo
- ✅ Administrar empleados y sus asignaciones
- ✅ Generar reportes de horas trabajadas
- ✅ Controlar acceso mediante autenticación JWT
- ✅ Facilitar comunicaciones internas
- ✅ Permitir importación masiva de datos

### Características clave:
- 🔐 Autenticación segura con JWT
- 👥 Control de acceso basado en roles (RBAC)
- 📊 Reportes avanzados con exportación PDF/Excel
- 📧 Notificaciones por correo
- 📱 Interfaz responsiva y moderna
- ⚡ API REST bien documentada

---

## 💻 Requisitos Previos

### Para ejecutar el Backend:
- **Java 17** o superior
- **Maven 3.8.1** o superior
- **MySQL 8.0** o superior
- **puerto 8082** disponible (por defecto)

### Para ejecutar el Frontend:
- **Node.js 18.x** o superior
- **npm 9.x** o superior
- **puerto 5173** disponible (Vite)

### Navegadores soportados:
- Chrome/Chromium 90+
- Firefox 88+
- Safari 14+
- Edge 90+

---

## 🚀 Instalación y Configuración

### 1. Clonar el Repositorio
```bash
git clone <repository-url>
cd Microfarma-Horarios
```

### 2. Configurar Base de Datos

#### Crear base de datos MySQL:
```sql
CREATE DATABASE microfarmahorarios;
USE microfarmahorarios;
```

#### Ejecutar script de inicialización:
```bash
mysql -u root -p microfarma_horarios < Backend/src/main/resources/setup_domiciliarios.sql
```

### 3. Configurar Backend

#### Editar archivo de propiedades:
Archivo: `Backend/src/main/resources/application.properties`

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/microfarmahorarios?zeroDateTimeBehavior=convertToNull
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect

# Server
server.port=8082
server.servlet.context-path=/

# JWT
jwt.secret=mySecretKey1234567890123456789012345678901234567890
jwt.expiration=86400000
jwt.refreshExpiration=604800000

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=
spring.mail.password=
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

spring.main.allow-circular-references=true
```

#### Compilar y ejecutar Backend:
```bash
cd Backend
mvn clean install
mvn spring-boot:run
```

El backend estará disponible en: `http://localhost:8080`

### 4. Configurar Frontend

#### Editar archivo de configuración API:
Archivo: `Front-End/src/services/apiConfig.js`

```javascript
const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

export default API_URL;
```

#### Instalar dependencias y ejecutar:
```bash
cd Front-End
npm install
npm run dev
```

El frontend estará disponible en: `http://localhost:5173`

---

## 🔐 Autenticación

> Esta guía describe cómo usar el sistema desde la interfaz web de usuario. No necesitas abrir ni modificar código para iniciar sesión ni trabajar con la aplicación.

### Proceso de Login

1. Accede a la aplicación: `http://localhost:5173`
2. Se abre la página de **Login**
3. Ingresa tus credenciales:
   - **Email:** `admin@microfarma.com`
   - **Contraseña:** `adminMF2026*`

   > Nota: el sistema preconfigura un usuario administrador al iniciar con datos de ejemplo. Si necesitas otro usuario, regístrate desde la pantalla de registro o pide a un ADMIN que cree la cuenta.

4. Si las credenciales son correctas:
   - Recibirás un **JWT Token** válido por 24 horas
   - Se guardarán en localStorage para sesiones futuras
   - Serás redirigido al Dashboard

### Gestión de Sesión

- **Token JWT:** Se envía automáticamente en cada petición HTTP
- **Expiración:** 24 horas
- **Cierre de sesión:** Limpia el token y redirige a login
- **Recuperación de contraseña:** Disponible en página de login

### Recuperación de Contraseña

Si olvidas tu contraseña, sigue estos pasos:

```
PASOS DETALLADOS:
1. En la pantalla de Login, haz clic en "¿Olvidaste tu contraseña?"
2. Aparece formulario de recuperación
3. Ingresa tu email corporativo registrado
4. Haz clic en "Enviar enlace de recuperación"
5. El sistema valida que el email existe
6. Si es válido: se envía email con enlace seguro (válido 1 hora)
7. Si no existe: mensaje "Email no encontrado" (sin revelar si existe o no)
8. Revisa tu bandeja de entrada (y spam/junk)
9. Haz clic en el enlace del email
10. Se abre página segura para nueva contraseña
11. Ingresa nueva contraseña (mínimo 8 caracteres, mayúscula, minúscula, número)
12. Confirma nueva contraseña
13. Haz clic en "Restablecer contraseña"
14. Aparece mensaje de éxito y redirección automática a login
15. Inicia sesión con nueva contraseña
```

**Tiempo aproximado:** Email llega en 1-5 minutos.  
**Notas importantes:**
- El enlace expira en 1 hora por seguridad
- Solo funciona una vez por enlace
- Si no llega el email, verifica configuración de spam
- Contacta a soporte si persiste el problema

### Primer Acceso de Empleado Nuevo

Cuando un empleado recibe credenciales temporales por email:

```
LO QUE SUCEDE:
1. Empleado hace login con email y contraseña temporal
2. Sistema detecta contraseña temporal
3. Aparece pantalla obligatoria: "Cambiar Contraseña"
4. Debe ingresar nueva contraseña segura
5. Confirma contraseña
6. Acepta términos de uso (si aplica)
7. Haz clic en "Guardar y Continuar"
8. Redirección automática al Dashboard
9. Primer login completado
```

**Importante:** No puede acceder al sistema sin cambiar la contraseña temporal primero.

### Seguridad

- ✅ Las contraseñas se hash con BCrypt
- ✅ Los tokens se validan en cada petición
- ✅ Las rutas protegidas requieren autenticación
- ✅ CORS configurado para origen permitido
- ✅ HTTPS recomendado en producción

---

## 👥 Roles y Permisos

### Estructura de Roles

| **Rol** | **Descripción** | **Módulos Permitidos** |
|---------|----------------|----------------------|
| **ADMIN** | Administrador del sistema | Todos, acceso total |
| **USER** | Recursos Humanos | HR, Schedules, Reports, News, Notifications |
| **EMPLOYEE** | Empleado | Ver propios horarios, News, Perfil |
| **GERENTE_SUCURSAL** | Gerente de Ubicación | HR, Schedules, Reports de su sede |

### Permisos Granulares

Cada módulo tiene permisos específicos:
- `VIEW` - Ver datos
- `CREATE` - Crear nuevos registros
- `EDIT` - Editar registros existentes
- `DELETE` - Eliminar registros
- `EXPORT` - Exportar reportes

### Asignación de Roles

Solo **ADMIN** puede:
1. Crear nuevos usuarios
2. Asignar/modificar roles
3. Gestionar permisos
4. Ver auditoría del sistema

---

## 👤 Guía de Usuario por Rol

### 1️⃣ ADMINISTRADOR

#### Acceso a:
- Dashboard completo con todas las métricas
- Gestión de usuarios (crear, editar, eliminar, resetear contraseña)
- Gestión de roles y permisos
- Todas las funcionalidades del sistema

#### Tareas típicas:
```
1. Crear nuevo usuario
   → Security → Users → New User
   → Completar datos y asignar rol
   
2. Asignar permisos a rol
   → Security → Roles → Editar rol
   → Seleccionar permisos
   
3. Ver reporte completo
   → Schedules → Reports → Global Report
   → Aplicar filtros y exportar
```

---

### 2️⃣ RECURSOS HUMANOS (USER)

#### Acceso a:
- Gestión de empleados
- Gestión de turnos
- Generación de reportes
- Publicación de noticias
- Notificaciones

#### Tareas típicas:

**Crear un nuevo empleado:**
```
1. Ir a: HR → Employees
2. Click en "New Employee"
3. Rellenar formulario:
   - Nombre completo
   - Email
   - Cédula
   - Posición
   - Tipo de contrato
   - Ubicación (sede)
4. Click "Save"
```

**Crear un turno:**
```
1. Ir a: Schedules → Shifts
2. Click en "New Shift"
3. Definir:
   - Empleado
   - Tipo de turno (mañana, tarde, noche)
   - Fecha inicio / Fecha fin
   - Horas
4. Click "Save"
```

**Importar horarios desde Excel:**
```
1. Ir a: Schedules → Import Schedules
2. Preparar archivo Excel con estructura:
   | Empleado | Fecha | Turno | Horas |
   |----------|-------|-------|-------|
3. Click "Upload File"
4. Validar importación
5. Confirmar
```

**Generar reporte mensual:**
```
1. Ir a: Schedules → Reports
2. Seleccionar filtros:
   - Fecha inicio/fin
   - Ubicación (opcional)
   - Empleado (opcional)
3. Click "Generate Report"
4. Exportar como PDF o CSV
```

**Publicar noticia:**
```
1. Ir a: News → News
2. Click "New News"
3. Rellenar:
   - Título
   - Descripción
   - Tipo de noticia
   - Fecha publicación
4. Click "Publish"
```

---

### 3️⃣ EMPLEADO

#### Acceso a:
- Ver propios horarios
- Ver noticias de la empresa
- Editar perfil personal
- Historial de cambios

#### Tareas típicas:

**Ver mis horarios:**
```
1. Ir a: My Schedule
2. Seleccionar mes/semana
3. Ver todos los turnos programados
4. Descargar calendario en PDF si lo necesita
```

**Ver noticias internas:**
```
1. Ir a: News
2. Filtrar por tipo
3. Leer noticias publicadas
```

**Actualizar perfil:**
```
1. Ir a: Profile
2. Editar datos personales
3. Cambiar contraseña
4. Click "Save Changes"
```

---

### 4️⃣ GERENTE DE UBICACIÓN (GERENTE_SUCURSAL)

#### Acceso a:
- Dashboard limitado a su sede
- Gestión de empleados de su ubicación
- Creación y edición de turnos para su sede
- Generación de reportes solo de su ubicación
- Ver noticias (no crear)
- Perfil personal

#### Restricciones importantes:
- **Solo ve datos de su sede asignada**
- No puede crear usuarios ni gestionar roles
- No puede acceder a reportes globales
- No puede modificar empleados de otras sedes
- No puede publicar noticias

#### Tareas típicas:

**Revisar empleados de mi sede:**
```
1. Ir a: HR → Employees
2. Filtro automático aplicado a "Mi Ubicación"
3. Ver lista de empleados activos
4. Editar datos básicos (excepto salario)
```

**Gestionar turnos de mi sede:**
```
1. Ir a: Schedules → Shifts
2. Solo aparecen empleados de mi sede
3. Crear nuevos turnos para mi equipo
4. Editar turnos existentes
5. Aprobar solicitudes de cambio de turno
```

**Generar reportes de mi sede:**
```
1. Ir a: Schedules → Reports
2. Filtros limitados a mi ubicación
3. Tipos disponibles:
   - Reporte por Ubicación (solo mi sede)
   - Reporte por Empleado (solo empleados de mi sede)
4. Exportar PDF/Excel con datos de mi sede
```

**Ver métricas de mi sede:**
```
1. Dashboard muestra automáticamente:
   - Empleados activos en mi sede
   - Horas trabajadas este mes
   - % asistencia de mi equipo
   - Próximos turnos
```

---

## 📊 Funcionalidades Principales

### 1. Dashboard Ejecutivo

**Ubicación:** Home / Dashboard  
**Acceso:** Todos los roles (información personalizada)

**Contenido:**
- 📈 Métricas de eficiencia (% asistencia, horas cumplidas)
- 👥 Cantidad de empleados por sede
- 📅 Horario semanal
- 📰 Últimas noticias
- 📧 Notificaciones recientes

---

### 2. Gestión de Empleados

**Ubicación:** HR → Employees  
**Acceso:** ADMIN, USER

**Operaciones:**
- ✏️ **Crear:** Nuevo empleado con datos básicos
- 📝 **Editar:** Cambiar datos del empleado
- 🗑️ **Eliminar:** Desactivar empleado
- 🔍 **Buscar:** Por nombre, cédula, email
- 📤 **Exportar:** Lista a Excel/PDF
- 🏢 **Asignar ubicación:** Sede donde trabaja

**Campos principales:**
- Nombre completo
- Email corporativo
- Cédula de ciudadanía
- Fecha de ingreso
- Posición/Cargo
- Tipo de contrato
- Salario (confidencial)
- Estado (Activo/Inactivo)

---

### 3. Gestión de Turnos

**Ubicación:** Schedules → Shifts  
**Acceso:** ADMIN, USER

**Tipos de turno:**
- Mañana (6am - 2pm)
- Tarde (2pm - 10pm)
- Noche (10pm - 6am)
- Personalizado

**Operaciones:**
- ✏️ **Crear:** Nuevo turno para empleado
- 📝 **Editar:** Modificar fecha/hora
- 🗑️ **Eliminar:** Cancelar turno
- 📤 **Exportar:** Calendario a Excel/PDF
- 🔄 **Duplicar:** Crear turnos recurrentes

**Campos:**
- Empleado
- Tipo de turno
- Fecha inicio
- Fecha fin
- Horas
- Ubicación

---

### 4. Importación Masiva

**Ubicación:** Schedules → Import Schedules  
**Acceso:** ADMIN, USER

**Formato esperado (Excel):**
```
| Empleado        | Fecha      | Tipo Turno | Horas | Ubicación  |
|-----------------|------------|------------|-------|------------|
| Juan Pérez      | 2026-04-25 | Mañana     | 8     | Sede Norte |
| María García    | 2026-04-25 | Tarde      | 8     | Sede Sur   |
```

**Proceso:**
1. Preparar archivo XLS/XLSX
2. Validar formato
3. Subir archivo
4. Sistema valida datos
5. Confirmar importación
6. Ver reporte de resultados

**Validaciones:**
- ✅ Empleado debe existir
- ✅ Fecha válida
- ✅ Tipo de turno reconocido
- ✅ No duplicados

**Mensajes de Error Comunes y Soluciones:**

```
ERROR: "Empleado 'Juan Pérez García' no encontrado"
SOLUCIÓN: Verifica ortografía exacta en HR → Employees. Corrige nombre en Excel y reimporta solo esa fila.

ERROR: "Fecha '32/05/2026' inválida"
SOLUCIÓN: Usa formato DD/MM/YYYY válido. Ejemplo: 15/05/2026

ERROR: "Tipo de turno 'Mañana' no reconocido. Valores válidos: Mañana, Tarde, Noche, Personalizado"
SOLUCIÓN: Corrige a mayúscula inicial exacta.

ERROR: "Turno duplicado para Juan Pérez el 15/05/2026"
SOLUCIÓN: Elimina fila duplicada o cambia fecha.

ERROR: "Archivo vacío o sin datos"
SOLUCIÓN: Asegura que haya al menos una fila de datos después de encabezados.

ERROR: "Columna 'Empleado' faltante"
SOLUCIÓN: Verifica que primera columna sea exactamente 'Empleado' (sin espacios extra).
```

**Proceso de Corrección sin Reiniciar:**
1. El sistema muestra lista de errores con fila específica
2. Corrige errores en tu archivo Excel original
3. Haz clic en "Reintentar Importación" (no necesitas subir archivo nuevo)
4. Solo filas con errores se revalidan
5. Filas correctas permanecen sin cambios

---

### 5. Reportes Avanzados

**Ubicación:** Schedules → Reports  
**Acceso:** ADMIN, USER, GERENTE_SUCURSAL

**Tipos de reporte:**

#### A) Reporte Global
- Total horas empresa
- Horas por empleado
- Horas por ubicación
- Varianza vs planificado

#### B) Reporte por Empleado
- Horas trabajadas
- Asistencia
- Tardanzas
- Ausencias

#### C) Reporte por Ubicación (Sede)
- Empleados por sede
- Horas por sede
- Costos laborales
- Efficiency índex

**Filtros disponibles:**
- 📅 Rango de fechas
- 🏢 Ubicación
- 👤 Empleado específico
- 📊 Tipo de reporte

**Exportación:**
- 📄 PDF con gráficos
- 📊 Excel con datos detallados
- 📋 CSV para análisis externo

---

### 6. Gestión de Noticias

**Ubicación:** News → News  
**Acceso:** ADMIN, USER (crear); EMPLOYEE (ver)

**Operaciones:**
- 📝 **Crear:** Nueva noticia
- ✏️ **Editar:** Modificar contenido
- 🗑️ **Eliminar:** Remover noticia
- 🔍 **Buscar:** Por título, tipo
- 📌 **Destacar:** Pin importante

**Campos:**
- Título
- Descripción/Contenido
- Tipo (Anuncio, Evento, Recordatorio, etc.)
- Imagen (opcional)
- Fecha publicación
- Vigencia

---

### 7. Notificaciones por Correo

**Ubicación:** Notification → Email History  
**Acceso:** ADMIN, USER

**Contenido visualizado:**
- 📧 Correos enviados
- 👤 Destinatario
- ⏰ Fecha/Hora envío
- 📌 Asunto
- ✅ Estado (Enviado, Error)

**Acciones:**
- 🔍 Ver detalles
- 🔄 Reenviar
- 📊 Estadísticas de entrega

---

### 8. Gestión de Plantillas de Horas

**Ubicación:** DefaultHours → Template Types  
**Acceso:** ADMIN, USER

**¿Qué son?**
Horarios estándar reutilizables para grupos de empleados

**Ejemplo:**
```
Template: "Horario Farmacéutico"
- Lunes-Viernes: 8am-5pm (8 horas)
- Sábado: 9am-2pm (5 horas)
- Domingo: Descanso
```

**Operaciones:**
- 📝 **Crear:** Nueva plantilla
- ✏️ **Editar:** Modificar horario
- 🗑️ **Eliminar:** Remover plantilla
- 👥 **Asignar:** A empleado/grupo

---

### 9. Gestión de Domiciliarios

**Ubicación:** Delivery → Delivery Shifts  
**Acceso:** ADMIN, USER

**Específico para domiciliarios:**
- Turno de entrega
- Zona asignada
- Vehículo
- Horas totales
- Reporte de entregas

---

## 📋 Casos de Uso Comunes

### 🔄 Caso 1: Configurar sistema para nueva sede

```
PASOS:
1. ADMIN crea nueva ubicación
   → Organization → Locations → New Location
   
2. ADMIN crea empleados y asigna a ubicación
   → HR → Employees → New Employee
   
3. HR crea turnos o importa masivamente
   → Schedules → Import Schedules
   
4. HR genera reporte inicial
   → Schedules → Reports → Filter by Location
   
5. HR notifica empleados
   → News → Publish news about new schedule
   
TIEMPO APROXIMADO: 2-3 horas
```

---

### 📊 Caso 2: Generar reportes mensuales para gerencia

```
PASOS:
1. HR va a: Schedules → Reports
2. Selecciona filtros:
   - Fecha: 1 - 30 abril 2026
   - Ubicación: Todas (o específica)
3. Click "Generate Report"
4. Revisa datos en preview
5. Exporta a PDF
6. Envía a gerencia por email
7. Guarda copia en servidor

ENTREGABLES:
- Reporte Global (PDF)
- Reporte por Ubicación (PDF)
- Datos detallados (Excel)

TIEMPO: 30-45 minutos
```

---

### 👤 Caso 3: Onboarding de nuevo empleado

```
PASOS:
1. ADMIN crea usuario en sistema
   → Security → Users → New User
   → Email: juan.perez@microfarma.com
   → Envía contraseña temporal
   
2. ADMIN asigna al HR que cree su perfil
   
3. HR crea perfil de empleado
   → HR → Employees → New Employee
   → Cédula, posición, contrato, ubicación
   
4. HR asigna template de horas
   → DefaultHours → Assignment
   
5. HR publica noticia de bienvenida
   → News → Welcome message
   
6. Empleado recibe email con credenciales
7. Empleado login y ve su horario

TIEMPO: 1 hora (primer empleado incluye setup)
```

---

## 📖 Guía Detallada de Uso

Esta sección explica paso a paso cómo utilizar cada funcionalidad del sistema Microfarma-Horarios. Incluye cómo crear, editar, eliminar, descargar archivos y manejar todas las operaciones comunes.

### 1. Gestión de Usuarios (Solo ADMIN)

#### Crear un Nuevo Usuario
```
PASOS DETALLADOS:
1. Inicia sesión como ADMIN
2. Ve al menú lateral: Security → Users
3. Haz clic en el botón "New User" (esquina superior derecha)
4. Completa el formulario:
   - Email: dirección de correo corporativo
   - Nombre completo
   - Rol: selecciona de la lista desplegable (ADMIN, USER, EMPLOYEE, GERENTE_SUCURSAL)
   - Ubicación: sede donde trabajará (opcional)
5. Haz clic en "Create User"
6. El sistema enviará automáticamente un email con credenciales temporales
7. El nuevo usuario podrá cambiar su contraseña en el primer login
```

#### Editar Usuario Existente
```
PASOS:
1. Security → Users
2. Busca el usuario por email o nombre
3. Haz clic en el botón "Edit" (icono de lápiz)
4. Modifica los campos necesarios
5. Haz clic en "Save Changes"
```

#### Eliminar Usuario
```
PASOS:
1. Security → Users
2. Selecciona el usuario
3. Haz clic en "Delete" (icono de papelera)
4. Confirma la eliminación en el diálogo emergente
NOTA: Los usuarios eliminados no pueden acceder, pero sus datos históricos se mantienen
```

#### Resetear Contraseña
```
PASOS:
1. Security → Users → selecciona usuario
2. Haz clic en "Reset Password"
3. El sistema envía nueva contraseña temporal por email
```

### 2. Gestión de Empleados (ADMIN, USER)

#### Crear Nuevo Empleado
```
PASOS DETALLADOS:
1. Ve a: HR → Employees
2. Haz clic en "New Employee"
3. Completa el formulario obligatorio:
   - Nombre completo
   - Email corporativo
   - Cédula de ciudadanía
   - Fecha de ingreso
   - Posición/Cargo
   - Tipo de contrato (Fijo, Temporal, etc.)
   - Ubicación (sede)
   - Salario base (solo visible para ADMIN/HR)
4. Campos opcionales:
   - Teléfono
   - Dirección
   - Fecha de nacimiento
5. Haz clic en "Save"
6. El empleado queda activo automáticamente
```

#### Editar Empleado
```
PASOS:
1. HR → Employees
2. Busca por nombre, cédula o email
3. Haz clic en "Edit" en la fila del empleado
4. Modifica los datos necesarios
5. Haz clic en "Update"
NOTA: Cambios en salario requieren aprobación adicional
```

#### Eliminar/Desactivar Empleado
```
PASOS:
1. HR → Employees → selecciona empleado
2. Haz clic en "Deactivate" (no "Delete" permanente)
3. El empleado queda inactivo pero mantiene historial
4. Para reactivar: "Activate" en empleados inactivos
```

#### Exportar Lista de Empleados
```
PASOS:
1. HR → Employees
2. Aplica filtros si necesitas (ubicación, estado)
3. Haz clic en "Export" → "Excel" o "PDF"
4. El archivo se descarga automáticamente
5. Nombre del archivo: empleados_[fecha].xlsx
```

### 3. Gestión de Turnos y Horarios

#### Crear Turno Individual
```
PASOS DETALLADOS:
1. Ve a: Schedules → Shifts
2. Haz clic en "New Shift"
3. Selecciona empleado del desplegable
4. Elige tipo de turno:
   - Mañana (6:00-14:00)
   - Tarde (14:00-22:00)
   - Noche (22:00-6:00)
   - Personalizado (define horas manualmente)
5. Selecciona fecha inicio y fin
6. Si es turno personalizado:
   - Hora inicio: ej. 09:00
   - Hora fin: ej. 17:00
   - Total horas: se calcula automáticamente
7. Asigna ubicación si es diferente a la del empleado
8. Haz clic en "Create Shift"
9. El turno aparece en el calendario semanal
```

#### Crear Horarios Recurrentes
```
PASOS:
1. Schedules → Shifts → "New Recurring Shift"
2. Selecciona empleado
3. Define patrón:
   - Días de la semana: Lunes, Miércoles, Viernes
   - Tipo de turno
   - Fecha inicio y fin del período
4. Haz clic en "Generate"
5. Revisa la preview de todos los turnos generados
6. Confirma creación
```

#### Editar Turno Existente
```
PASOS:
1. Schedules → Shifts
2. Haz clic en el turno en el calendario o lista
3. Modifica fecha, hora, tipo o empleado
4. Haz clic en "Update"
NOTA: Si cambias empleado, verifica conflictos
```

#### Eliminar Turno
```
PASOS:
1. Schedules → Shifts → selecciona turno
2. Haz clic en "Delete"
3. Confirma eliminación
NOTA: Los turnos eliminados no afectan reportes históricos
```

#### Ver Horario Semanal/Mensual
```
PASOS:
1. Schedules → Weekly View o Monthly View
2. Usa filtros por empleado o ubicación
3. Navega con flechas de fecha
4. Haz clic en turno para ver detalles
```

#### Descargar Horario en PDF
```
PASOS:
1. Schedules → Weekly/Monthly View
2. Aplica filtros deseados
3. Haz clic en "Export PDF"
4. Elige orientación (horizontal recomendado)
5. Archivo se descarga: horario_[fecha].pdf
```

### 4. Importación Masiva de Horarios

#### Preparar Archivo Excel
```
ESTRUCTURA REQUERIDA:
Columna A: Empleado (Nombre completo exacto)
Columna B: Fecha (Formato: DD/MM/YYYY)
Columna C: Tipo Turno (Mañana/Tarde/Noche/Personalizado)
Columna D: Horas (número, ej. 8)
Columna E: Ubicación (opcional)

EJEMPLO:
| Empleado      | Fecha      | Tipo Turno | Horas | Ubicación  |
|---------------|------------|------------|-------|------------|
| Juan Pérez    | 15/05/2026 | Mañana     | 8     | Sede Norte |
| María García  | 15/05/2026 | Tarde      | 8     | Sede Sur   |
```

#### Proceso de Importación
```
PASOS DETALLADOS:
1. Ve a: Schedules → Import Schedules
2. Haz clic en "Choose File" y selecciona tu Excel (.xlsx o .xls)
3. El sistema valida automáticamente:
   - Formato de archivo
   - Encabezados correctos
   - Empleados existen en sistema
   - Fechas válidas
   - No hay turnos duplicados
4. Si hay errores, corrige el archivo y reintenta
5. Si validación OK, haz clic en "Import"
6. Revisa el resumen de importación
7. Confirma para aplicar cambios
8. Descarga reporte de resultados: import_report_[fecha].xlsx
```

### 5. Generación de Reportes

#### Crear Reporte Básico
```
PASOS:
1. Ve a: Schedules → Reports
2. Selecciona tipo de reporte:
   - Global (todos los empleados)
   - Por Empleado
   - Por Ubicación
3. Aplica filtros:
   - Fecha inicio/fin (obligatorio)
   - Ubicación (opcional)
   - Empleado específico (opcional)
4. Haz clic en "Generate Report"
5. Espera procesamiento (puede tomar 1-2 minutos)
6. Revisa preview en pantalla
```

#### Exportar Reporte
```
OPCIONES DE EXPORTACIÓN:
1. PDF (con gráficos y formato profesional)
   - Haz clic en "Export PDF"
   - Se incluye: gráficos de asistencia, tablas detalladas
   
2. Excel (datos crudos para análisis)
   - Haz clic en "Export Excel"
   - Columnas: Empleado, Fecha, Horas, Tipo Turno, etc.
   
3. CSV (para importación a otros sistemas)
   - Haz clic en "Export CSV"
   - Formato plano separado por comas
```

#### Tipos de Reportes Avanzados
```
A) Reporte de Asistencia:
- % de cumplimiento de horarios
- Tardanzas por empleado
- Ausencias justificadas/no justificadas

B) Reporte de Costos:
- Costo por hora trabajada
- Costo total por ubicación
- Comparativo mensual

C) Reporte de Eficiencia:
- Horas planificadas vs reales
- Índice de productividad
- Métricas por departamento
```

### 6. Gestión de Noticias

#### Crear Nueva Noticia
```
PASOS DETALLADOS:
1. Ve a: News → News
2. Haz clic en "New News"
3. Completa formulario:
   - Título (máximo 100 caracteres)
   - Descripción/Contenido (texto largo)
   - Tipo: Anuncio, Evento, Recordatorio, Urgente
   - Fecha publicación (por defecto hoy)
   - Vigencia (hasta cuándo se muestra)
4. Opcional: subir imagen
5. Haz clic en "Publish"
6. La noticia aparece inmediatamente para todos los empleados
```

#### Editar Noticia
```
PASOS:
1. News → News → selecciona noticia
2. Haz clic en "Edit"
3. Modifica contenido
4. Haz clic en "Update"
```

#### Eliminar Noticia
```
PASOS:
1. News → News → selecciona noticia
2. Haz clic en "Delete"
3. Confirma eliminación
NOTA: Las noticias eliminadas desaparecen para todos
```

#### Destacar Noticia Importante
```
PASOS:
1. News → News → selecciona noticia
2. Haz clic en "Pin" (icono de chincheta)
3. La noticia aparece primero en la lista
```

### 7. Sistema de Notificaciones por Email

#### Ver Historial de Emails
```
PASOS:
1. Ve a: Notifications → Email History
2. Filtra por:
   - Fecha
   - Destinatario
   - Estado (Enviado, Error, Pendiente)
3. Haz clic en email para ver detalles completos
```

#### Reenviar Email Fallido
```
PASOS:
1. Notifications → Email History
2. Selecciona email con estado "Error"
3. Haz clic en "Resend"
4. El sistema intenta envío nuevamente
```

#### Ver Estadísticas de Email
```
PASOS:
1. Notifications → Email History → "Statistics"
2. Ve métricas:
   - Total enviados
   - Tasa de entrega
   - Errores por día
```

### 8. Gestión de Plantillas de Horas

#### Crear Nueva Plantilla
```
PASOS DETALLADOS:
1. Ve a: DefaultHours → Template Types
2. Haz clic en "New Template"
3. Nombre: ej. "Horario Farmacéutico Estándar"
4. Define horario por día:
   - Lunes: Mañana (8 horas)
   - Martes: Tarde (8 horas)
   - Miércoles: Mañana (8 horas)
   - etc.
5. Define descansos semanales
6. Haz clic en "Save Template"
```

#### Asignar Plantilla a Empleado
```
PASOS:
1. DefaultHours → Assignments
2. Haz clic en "New Assignment"
3. Selecciona empleado
4. Elige plantilla
5. Fecha inicio de aplicación
6. Haz clic en "Assign"
7. El sistema genera turnos automáticamente según plantilla
```

#### Editar Plantilla
```
PASOS:
1. DefaultHours → Template Types → selecciona plantilla
2. Haz clic en "Edit"
3. Modifica horarios
4. Haz clic en "Update"
NOTA: Los cambios afectan futuras asignaciones, no turnos ya creados
```

### 9. Gestión de Domiciliarios

#### Crear Turno de Domiciliario
```
PASOS:
1. Ve a: Delivery → Delivery Shifts
2. Haz clic en "New Delivery Shift"
3. Selecciona domiciliario
4. Define:
   - Zona de entrega
   - Vehículo asignado
   - Horario de turno
   - Número esperado de entregas
5. Haz clic en "Create"
```

#### Registrar Entregas
```
PASOS:
1. Delivery → Delivery Shifts → selecciona turno
2. Haz clic en "Log Delivery"
3. Ingresa detalles:
   - Hora de entrega
   - Dirección
   - Estado (Exitosa, Retrasada, Fallida)
4. Haz clic en "Save"
```

#### Generar Reporte de Domiciliarios
```
PASOS:
1. Delivery → Reports
2. Filtra por fecha y domiciliario
3. Exporta PDF con estadísticas de entregas
```

### 10. Funciones de Empleado

#### Ver Mi Horario
```
PASOS:
1. Ve a: My Schedule
2. Selecciona vista: Semana o Mes
3. Navega fechas con flechas
4. Haz clic en turno para ver detalles
5. Descarga PDF: "Download Schedule"
```

#### Editar Perfil Personal
```
PASOS:
1. Ve a: Profile
2. Modifica campos editables:
   - Teléfono
   - Dirección
   - Foto de perfil
3. Cambiar contraseña: "Change Password"
4. Haz clic en "Save Changes"
```

#### Ver Noticias
```
PASOS:
1. Ve a: News
2. Filtra por tipo si deseas
3. Lee noticias publicadas
4. Marca como leída (opcional)
```

### 11. Operaciones Avanzadas

#### Backup de Datos
```
PASOS (solo ADMIN):
1. Ve a: System → Backup
2. Selecciona tipo: Completo o Incremental
3. Haz clic en "Create Backup"
4. Descarga archivo .sql o .zip
5. Guarda en ubicación segura
```

#### Restaurar Sistema
```
PASOS (solo ADMIN):
1. System → Restore
2. Sube archivo de backup
3. Confirma restauración
4. Sistema se reinicia automáticamente
```

#### Configuración del Sistema
```
PASOS (solo ADMIN):
1. System → Settings
2. Configura:
   - SMTP para emails
   - Límites de archivos
   - Políticas de contraseña
   - Zonas horarias
3. Haz clic en "Save Configuration"
```

### Consejos Generales de Uso

- **Navegación:** Usa el menú lateral para acceder a módulos
- **Búsqueda:** La mayoría de listas tienen barra de búsqueda
- **Filtros:** Aplica filtros antes de exportar para datos específicos
- **Confirmaciones:** Siempre confirma acciones importantes (eliminar, importar)
- **Ayuda:** Cada pantalla tiene botón "?" para ayuda contextual
- **Soporte:** Contacta a soporte@microfarma.com para problemas
- **Actualizaciones:** El sistema se actualiza automáticamente, revisa changelog

---

## 🛠️ Solución de Problemas

### ❌ Problema: "No puedo acceder al sistema"

**Causa posible:** Token expirado o inválido

**Solución:**
```
1. Limpia caché del navegador
2. Borra cookies
3. Intenta login de nuevo
4. Si persiste, verifica credenciales con ADMIN
```

---

### ❌ Problema: "Error al importar archivo Excel"

**Causa posible:** Formato incorrecto

**Verificar:**
```
✅ Archivo es .xlsx o .xls
✅ Primera fila tiene encabezados
✅ No hay filas vacías entre datos
✅ Nombres de empleados coinciden exactamente
✅ Fechas en formato: DD/MM/YYYY
✅ Tipos de turno son válidos (Mañana, Tarde, Noche)
```

---

### ❌ Problema: "Los reportes tardan mucho en generar"

**Causa posible:** Base de datos grande

**Optimización:**
```
1. Reduce rango de fechas
2. Filtra por ubicación específica
3. Solicita a IT vaciar tabla de logs
4. Usa reporte por empleado en lugar de global
```

---

### ❌ Problema: "No recibo notificaciones por correo"

**Causa posible:** Configuración de SMTP

**Verificar:**
```
1. Backend admin verifica application.properties
2. Valida credenciales SMTP
3. Verifica que correo No esté en SPAM
4. Reintenta envío
5. Si persiste, contacta IT
```

---

### ❌ Problema: "No veo los cambios en tiempo real"

**Solución:**
```
1. Recarga la página (F5)
2. Vacía caché (Ctrl+Shift+Delete)
3. Logout y login nuevamente
4. Prueba en navegador diferente
```

---

## ❓ FAQ (Preguntas Frecuentes)

### **P: ¿Cuánto tiempo dura mi sesión?**
R: Tu sesión dura 24 horas con JWT. Después debes hacer login nuevamente.

---

### **P: ¿Puedo cambiar mi contraseña?**
R: Sí, ve a Profile → Change Password. Necesitas la contraseña actual.

---

### **P: ¿Qué formatos se pueden exportar?**
R: PDF (con gráficos) y Excel (con datos detallados). CSV disponible en próxima versión.

---

### **P: ¿Cuántos empleados soporta el sistema?**
R: Sin límite teórico. Probado hasta 5,000 empleados sin degradación.

---

### **P: ¿Es seguro guardar datos en el sistema?**
R: Sí, usamos encriptación BCrypt para contraseñas, JWT para tokens, y MySQL con backups diarios.

---

### **P: ¿Puedo acceder desde mi teléfono?**
R: Sí, el sistema es responsive. Funciona en iOS, Android, tablets.

---

### **P: ¿Qué navegador recomiendan?**
R: Chrome o Edge son recomendados. Firefox y Safari también soportados.

---

### **P: ¿Cómo reseteó mi contraseña si la olvido?**
R: Ve a la pantalla de login y haz clic en "¿Olvidaste tu contraseña?". Ingresa tu email, recibirás un enlace seguro en 1-5 minutos. El enlace expira en 1 hora. Sigue las instrucciones en el email para crear nueva contraseña.

---

### **P: ¿Dónde se guardan los backups?**
R: Configurado en IT. Por defecto, diarios a las 2am.

---

### **P: ¿Puedo tener múltiples roles?**
R: No, cada usuario tiene un único rol principal. Se pueden crear roles personalizados.

---

### **P: ¿Hay API disponible para terceros?**
R: Sí, API REST en `http://localhost:8080/api`. Contacta IT para documentación.

## 📝 Historial de Cambios

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0 | 24/04/2026 | Manual inicial |
| 1.1 | 08/05/2026 | Corrección de roles (HR→USER, MANAGER→GERENTE_SUCURSAL), puertos y configuración BD |

---

**Documento clasificado como:** INTERNO - Microfarma S.A.  
**Última actualización:** 24 de abril de 2026

