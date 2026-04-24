# 📘 MANUAL DE USO - SISTEMA MICROFARMA-HORARIOS

**Versión:** 1.0  
**Fecha:** 24 de abril de 2026  
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
9. [Solución de Problemas](#solución-de-problemas)
10. [FAQ](#faq)

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
- **puerto 8080** disponible (por defecto)

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
CREATE DATABASE microfarma_horarios;
USE microfarma_horarios;
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
spring.datasource.url=jdbc:mysql://localhost:3306/microfarma_horarios
spring.datasource.username=root
spring.datasource.password=tu_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect

# Server
server.port=8080
server.servlet.context-path=/

# JWT
jwt.secret=tu_clave_secreta_muy_larga_y_compleja_123456
jwt.expiration=86400000

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu_email@gmail.com
spring.mail.password=tu_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
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

### Proceso de Login

1. Accede a la aplicación: `http://localhost:5173`
2. Se abre la página de **Login**
3. Ingresa tus credenciales:
   - **Email:** tu_email@microfarma.com
   - **Contraseña:** tu_contraseña

4. Si las credenciales son correctas:
   - Recibirás un **JWT Token** válido por 24 horas
   - Se guardarán en localStorage para sesiones futuras
   - Serás redirigido al Dashboard

### Gestión de Sesión

- **Token JWT:** Se envía automáticamente en cada petición HTTP
- **Expiración:** 24 horas
- **Cierre de sesión:** Limpia el token y redirige a login
- **Recuperación de contraseña:** Disponible en página de login

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
| **HR** | Recursos Humanos | HR, Schedules, Reports, News, Notifications |
| **EMPLOYEE** | Empleado | Ver propios horarios, News, Perfil |
| **MANAGER** | Gerente de Ubicación | HR, Schedules, Reports de su sede |

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

### 2️⃣ RECURSOS HUMANOS (HR)

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
**Acceso:** ADMIN, HR

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
**Acceso:** ADMIN, HR

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
**Acceso:** ADMIN, HR

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

---

### 5. Reportes Avanzados

**Ubicación:** Schedules → Reports  
**Acceso:** ADMIN, HR, MANAGER

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
**Acceso:** ADMIN, HR (crear); EMPLOYEE (ver)

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
**Acceso:** ADMIN, HR

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
**Acceso:** ADMIN, HR

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
**Acceso:** ADMIN, HR

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
R: Click en "Forgot Password" en login y sigue el flujo de recuperación por email.

---

### **P: ¿Dónde se guardan los backups?**
R: Configurado en IT. Por defecto, diarios a las 2am.

---

### **P: ¿Puedo tener múltiples roles?**
R: No, cada usuario tiene un único rol principal. Se pueden crear roles personalizados.

---

### **P: ¿Hay API disponible para terceros?**
R: Sí, API REST en `http://localhost:8080/api`. Contacta IT para documentación.

---

## 📞 Soporte y Contacto

**Email de soporte:** soporte@microfarma.com  
**Horario:** Lunes - Viernes, 8am - 5pm  
**Teléfono:** +34 900 123 456 (España)

---

## 📝 Historial de Cambios

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0 | 24/04/2026 | Manual inicial |

---

**Documento clasificado como:** INTERNO - Microfarma S.A.  
**Última actualización:** 24 de abril de 2026

