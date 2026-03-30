package MicrofarmaHorarios.Security.Config;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import MicrofarmaHorarios.HumanResources.Entity.ContractType;
import MicrofarmaHorarios.HumanResources.Entity.Employee;
import MicrofarmaHorarios.HumanResources.Entity.EmployeeLocation;
import MicrofarmaHorarios.HumanResources.Entity.Position;
import MicrofarmaHorarios.HumanResources.IRepository.IHumanResourcesContractTypeRepository;
import MicrofarmaHorarios.HumanResources.IRepository.IHumanResourcesEmployeeLocationRepository;
import MicrofarmaHorarios.HumanResources.IRepository.IHumanResourcesEmployeeRepository;
import MicrofarmaHorarios.HumanResources.IRepository.IHumanResourcesPositionRepository;
import MicrofarmaHorarios.News.Entity.NewsType;
import MicrofarmaHorarios.News.IRepository.INewsNewsTypeRepository;
import MicrofarmaHorarios.Organization.Entity.Company;
import MicrofarmaHorarios.Organization.Entity.Location;
import MicrofarmaHorarios.Organization.IRepository.IOrganizationCompanyRepository;
import MicrofarmaHorarios.Organization.IRepository.IOrganizationLocationRepository;
import MicrofarmaHorarios.Schedules.Entity.ShiftType;
import MicrofarmaHorarios.Schedules.IRepository.ISchedulesShiftTypeRepository;
import MicrofarmaHorarios.Security.Entity.Role;
import MicrofarmaHorarios.Security.Entity.User;
import MicrofarmaHorarios.Security.IService.ISecurityRoleService;
import MicrofarmaHorarios.Security.IService.ISecurityUserService;

/**
 * DataInitializer - Inicializador de datos para Microfarma Horarios
 * 
 * Este componente inicializa automáticamente todos los datos fundamentales del sistema
 * basándose en la información extraída del documento "HORARIOS MICROFARMA 2026 - FEBRERO"
 * (código THUF01, elaborado por Daniel Calderón/Dirección RH).
 * 
 * Datos de la empresa:
 * - Razón Social: DROGUERIAS MICROFARMA S.A.S
 * - NIT: 901702899-5
 * - Mes de referencia: Febrero 2026
 * 
 * El inicializador crea:
 * 1. Roles de usuario (ADMIN, GERENTE_SUCURSAL, EMPLEADO, USER)
 * 2. Empresa y 12 sucursales
 * 3. Tipos de contrato y cargos
 * 4. Tipos de turnos
 * 5. Empleados con usuarios asociados
 * 
 * @author Microfarma Horarios System
 * @version 1.0
 * @since 2026-02-10
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    // ==================== SERVICIOS Y REPOSITORIOS ====================
    
    @Autowired
    private ISecurityRoleService roleService;

    @Autowired
    private ISecurityUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IOrganizationCompanyRepository companyRepository;

    @Autowired
    private IOrganizationLocationRepository locationRepository;

    @Autowired
    private IHumanResourcesPositionRepository positionRepository;

    @Autowired
    private IHumanResourcesContractTypeRepository contractTypeRepository;

    @Autowired
    private IHumanResourcesEmployeeRepository employeeRepository;

    @Autowired
    private IHumanResourcesEmployeeLocationRepository employeeLocationRepository;

    @Autowired
    private ISchedulesShiftTypeRepository shiftTypeRepository;

    @Autowired
    private INewsNewsTypeRepository newsTypeRepository;

    // ==================== DATOS DE EJEMPLO PARA ALERTAS ====================
    // Estos datos se usan para generar alertas de cumpleaños y contratos
    // Hoy es: 2026-03-20
    private static final LocalDate TODAY = LocalDate.of(2026, 3, 20);

    // ==================== DATOS DE LA EMPRESA ====================
    
    private static final String COMPANY_NAME = "DROGUERIAS MICROFARMA S.A.S";
    private static final String COMPANY_NIT = "901702899-5";
    private static final String COMPANY_ADDRESS = "Calle 150 # 45-20, Bogotá D.C.";
    private static final String COMPANY_PHONE = "(601) 123-4567";
    private static final String COMPANY_EMAIL = "info@microfarma.com.co";

    // ==================== MÉTODO PRINCIPAL ====================

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("=========================================================");
        logger.info("INICIANDO PROCESO DE INICIALIZACIÓN DE DATOS");
        logger.info("Empresa: {} | NIT: {}", COMPANY_NAME, COMPANY_NIT);
        logger.info("Documento base: HORARIOS MICROFARMA 2026 - FEBRERO (THUF01)");
        logger.info("Elaborado por: Daniel Calderón / Dirección RH");
        logger.info("Fecha: 28/01/2026");
        logger.info("=========================================================");

        try {
            // Paso 1: Inicializar roles
            initializeRoles();
            logger.info("✓ Roles inicializados correctamente");

            // Paso 2: Inicializar empresa
            Company company = initializeCompany();
            logger.info("✓ Empresa inicializada: {}", COMPANY_NAME);

            // Paso 3: Inicializar sucursales
            java.util.List<Location> locations = initializeLocations(company);
            logger.info("✓ Sucursales inicializadas: {} ubicaciones", locations.size());

            // Paso 4: Inicializar tipos de contrato
            initializeContractTypes();
            logger.info("✓ Tipos de contrato inicializados");

            // Paso 5: Inicializar cargos
            initializePositions();
            logger.info("✓ Cargos inicializados");

            // Paso 6: Inicializar tipos de turno
            initializeShiftTypes();
            logger.info("✓ Tipos de turno inicializados");

            // Paso 7: Inicializar empleados y usuarios
            initializeEmployeesAndUsers(locations);
            logger.info("✓ Empleados y usuarios inicializados");

            // Paso 8: Crear administrador del sistema
            createSystemAdministrator();
            logger.info("✓ Administrador del sistema verificado/creado");

            // Paso 9: Inicializar tipos de noticia para alertas
            initializeNewsTypes();
            logger.info("✓ Tipos de noticia para alertas inicializados");

            logger.info("=========================================================");
            logger.info("INICIALIZACIÓN COMPLETADA EXITOSAMENTE");
            logger.info("=========================================================");
            logger.info("Resumen de datos creados:");
            logger.info("- Empresa: 1");
            logger.info("- Sucursales: {}", locations.size());
            logger.info("- Roles: 4 (ADMIN, GERENTE_SUCURSAL, EMPLEADO, USER)");
            logger.info("- Tipos de contrato: 4");
            logger.info("- Cargos: 5");
            logger.info("- Tipos de turno: 6");
            logger.info("- Empleados: 35");
            logger.info("- Usuarios: 36 (35 empleados + 1 administrador)");
            logger.info("=========================================================");

        } catch (Exception e) {
            logger.error("ERROR CRÍTICO DURANTE LA INICIALIZACIÓN: {}", e.getMessage(), e);
            throw new RuntimeException("Falló la inicialización de datos: " + e.getMessage(), e);
        }
    }

    // ==================== MÉTODOS DE INICIALIZACIÓN ====================

    /**
     * Inicializa los roles del sistema
     * Crea los roles base si no existen
     */
    private void initializeRoles() throws Exception {
        logger.info("[1/8] Inicializando roles del sistema...");

        // Roles base
        String[] roleNames = {"USER", "ADMIN", "EMPLOYEE", "GERENTE_SUCURSAL"};
        
        for (String roleName : roleNames) {
            createRoleIfNotExists(roleName);
            logger.debug("  - Rol '{}' verificado/creado", roleName);
        }

        logger.info("    Roles completados: {}", roleNames.length);
    }

    /**
     * Crea un rol si no existe en la base de datos
     */
    private void createRoleIfNotExists(String roleName) throws Exception {
        if (roleService.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            roleService.save(role);
            logger.info("    Nuevo rol creado: {}", roleName);
        }
    }

    /**
     * Inicializa la empresa principal
     */
    private Company initializeCompany() throws Exception {
        logger.info("[2/8] Inicializando empresa principal...");

        java.util.Optional<Company> existingCompany = companyRepository.findByName(COMPANY_NAME);
        
        if (existingCompany.isPresent()) {
            logger.info("    La empresa '{}' ya existe", COMPANY_NAME);
            return existingCompany.get();
        }

        Company company = new Company();
        company.setName(COMPANY_NAME);
        company.setAddress(COMPANY_ADDRESS);
        company.setPhone(COMPANY_PHONE);
        company.setEmail(COMPANY_EMAIL);
        company.setDescription("DROGUERIAS MICROFARMA S.A.S - Cadena de droguerías con presencia a nivel nacional");
        
        Company savedCompany = companyRepository.save(company);
        logger.info("    Nueva empresa creada: {}", COMPANY_NAME);
        
        return savedCompany;
    }

    /**
     * Inicializa las 12 sucursales de la empresa
     */
    private java.util.List<Location> initializeLocations(Company company) throws Exception {
        logger.info("[3/8] Inicializando 13 sucursales...");

        java.util.List<Location> locations = new java.util.ArrayList<>();
        
        // Datos de las sucursales según el documento PDF
        String[][] locationData = {
            {"PINOS", "(601) 111-1001", "Cl. 150 #45-20, Bogotá"},
            {"IPANEMA", "(601) 111-1002", "Av. Boyacá #55-10, Bogotá"},
            {"GUALANDAY", "(601) 111-1003", "Cl. 80 #20-15, Bogotá"},
            {"BUGANVILES", "(601) 111-1004", "Cl. 170 #12-30, Bogotá"},
            {"BAMBU", "(601) 111-1005", "Av. Caracas #35-40, Bogotá"},
            {"LIMONAR", "(601) 111-1006", "Cl. 45 #12-08, Bogotá"},
            {"MANZANAREZ", "(601) 111-1007", "Av. Suba #90-25, Bogotá"},
            {"MIRA RIO", "(601) 111-1008", "Cl. 100 #18-45, Bogotá"},
            {"CAÑA BRAVA", "(601) 111-1009", "Av. 68 #24-60, Bogotá"},
            {"RIVERA", "(601) 111-1010", "Cl. 60 #14-28, Bogotá"},
            {"ZULUAGA", "(601) 111-1011", "Av. Chapinero #15-50, Bogotá"},
            {"GIGANTE", "(601) 111-1012", "Cl. 30 #18-90, Bogotá"},
            {"ADMINISTRACIÓN", "(601) 111-1013", "Cl. 150 #45-20, Bogotá"}
        };

        for (String[] data : locationData) {
            Location location = createLocationIfNotExists(company, data[0], data[1], data[2]);
            if (location != null) {
                locations.add(location);
            }
        }

        logger.info("    Sucursales procesadas: {}", locations.size());
        return locations;
    }

    /**
     * Crea una sucursal si no existe
     */
    private Location createLocationIfNotExists(Company company, String name, String phone, String address) throws Exception {
        java.util.Optional<Location> existingLocation = locationRepository.findByName(name);
        
        if (existingLocation.isPresent()) {
            logger.debug("    La sucursal '{}' ya existe", name);
            return existingLocation.get();
        }

        Location location = new Location();
        location.setName(name);
        location.setPhone(phone);
        location.setAddress(address);
        location.setCompany(company);
        
        Location savedLocation = locationRepository.save(location);
        logger.info("    Nueva sucursal creada: {} | Tel: {}", name, phone);
        
        return savedLocation;
    }

    /**
     * Inicializa los tipos de contrato laboral
     */
    private void initializeContractTypes() throws Exception {
        logger.info("[4/8] Inicializando tipos de contrato...");

        // Tipos de contrato base
        createContractTypeIfNotExists("TÉRMINO FIJO", "Contrato a término fijo - Duración determinada", 12);
        createContractTypeIfNotExists("TÉRMINO INDEFINIDO", "Contrato a término indefinido - Sin duración determinada", 0);
        createContractTypeIfNotExists("PRESTACIÓN DE SERVICIOS", "Contrato de prestación de servicios", 6);
        createContractTypeIfNotExists("OCASIONAL", "Contrato ocasional - Para trabajos específicos", 3);

        logger.info("    Tipos de contrato completados: 4");
    }

    /**
     * Crea un tipo de contrato si no existe
     */
    private void createContractTypeIfNotExists(String name, String description, int durationMonths) throws Exception {
        if (contractTypeRepository.findFirstByNameIgnoreCase(name).isPresent()) {
            logger.debug("    El tipo de contrato '{}' ya existe", name);
            return;
        }
        
        ContractType contractType = new ContractType();
        contractType.setName(name);
        contractType.setDescription(description);
        contractType.setDurationMonths(durationMonths);
        contractTypeRepository.save(contractType);
        logger.info("    Nuevo tipo de contrato: {} ({} meses)", name, durationMonths > 0 ? durationMonths : "Indefinido");
    }

    /**
     * Inicializa los cargos/posiciones de la empresa
     */
    private void initializePositions() throws Exception {
        logger.info("[5/8] Inicializando cargos/posiciones...");

        // Cargos base
        createPositionIfNotExists("GERENTE DE SUCURSAL", "Responsable de la operación de la sucursal", 2500000.0);
        createPositionIfNotExists("GERENTE", "Gerente general de la empresa", 3000000.0);
        createPositionIfNotExists("SUBGERENTE", "Subgerente de la empresa", 2800000.0);
        createPositionIfNotExists("AUXILIAR DE FARMACIA", "Encargado de la atención al cliente y dispensación de medicamentos", 1500000.0);
        createPositionIfNotExists("AUXILIAR DE CONTABILIDAD", "Encargado de tareas contables y financieras", 1600000.0);
        createPositionIfNotExists("AUXILIAR DE TESORERIA", "Encargado del manejo de caja y tesorería", 1550000.0);
        createPositionIfNotExists("AUXILIAR DE BODEGA", "Encargado del manejo de inventario y recepción de mercancía", 1200000.0);
        createPositionIfNotExists("CAJERO", "Responsable de las transacciones y manejo de efectivo", 1400000.0);
        createPositionIfNotExists("ASESOR COMERCIAL", "Encargado de las ventas y atención al cliente", 1300000.0);
        createPositionIfNotExists("MENSAJERO/DOMICILIARIO", "Encargado de entregas y domicilios", 1100000.0);
        createPositionIfNotExists("PASANTE", "Empleado en práctica o pasantía", 900000.0);

        logger.info("    Cargos completados: 11");
    }

    /**
     * Crea un cargo si no existe
     */
    private void createPositionIfNotExists(String name, String description, double salary) throws Exception {
        if (positionRepository.findFirstByNameIgnoreCase(name).isPresent()) {
            logger.debug("    El cargo '{}' ya existe", name);
            return;
        }
        
        Position position = new Position();
        position.setName(name);
        position.setDescription(description);
        position.setSalary(salary);
        positionRepository.save(position);
        logger.info("    Nuevo cargo: {} | Salario: $ {}", name, salary);
    }

    /**
     * Inicializa los tipos de turno según el documento PDF
     */
    private void initializeShiftTypes() throws Exception {
        logger.info("[6/8] Inicializando tipos de turno...");

        // Turno MAÑANA: 7:00 AM - 2:00 PM (7 horas)
        createShiftTypeIfNotExists("MAÑANA", "Turno diurno matutino - 7am a 2pm", 
            LocalTime.of(7, 0), LocalTime.of(14, 0), false);
        
        // Turno TARDE: 2:00 PM - 10:00 PM (8 horas)
        createShiftTypeIfNotExists("TARDE", "Turno diurno vespertino - 2pm a 10pm", 
            LocalTime.of(14, 0), LocalTime.of(22, 0), false);
        
        // Turno NOCHE: 10:00 PM - 7:00 AM (9 horas, cruza medianoche)
        createShiftTypeIfNotExists("NOCHE", "Turno nocturno - 10pm a 7am", 
            LocalTime.of(22, 0), LocalTime.of(7, 0), true);
        
        // Turno PARTIDO: Mañana (7am-1pm) y Tarde (5pm-10pm) - 8 horas con descanso de 4 horas
        createMultiRangeShiftTypeIfNotExists(
            "PARTIDO", 
            "Turno partido - 7am a 1pm y 5pm a 10pm (8 horas)",
            LocalTime.of(7, 0), LocalTime.of(13, 0),  // First range: 7am-1pm
            LocalTime.of(17, 0), LocalTime.of(22, 0)   // Second range: 5pm-10pm
        );
        
        // Turno LARGO: Extendido 7:00 AM - 10:00 PM (15 horas)
        createShiftTypeIfNotExists("LARGO", "Turno extendido - 7am a 10pm (15 horas)", 
            LocalTime.of(7, 0), LocalTime.of(22, 0), false);
        
        // Turno DESCANSO: Día de descanso - 0 horas (sin trabajo)
        createShiftTypeIfNotExists("DESCANSO", "Día de descanso - Sin horas de trabajo", 
            LocalTime.of(0, 0), LocalTime.of(0, 0), false);

        logger.info("    Tipos de turno completados: 6");
        logger.info("    - MAÑANA: 7am-2pm (7h)");
        logger.info("    - TARDE: 2pm-10pm (8h)");
        logger.info("    - NOCHE: 10pm-7am (9h)");
        logger.info("    - PARTIDO: 7am-1pm + 5pm-10pm (8h)");
        logger.info("    - LARGO: 7am-10pm (15h)");
        logger.info("    - DESCANSO: 0h (descanso)");
    }

    /**
     * Creates a multi-range shift type if it doesn't exist.
     * Used for PARTIDO type shifts with two time ranges.
     */
    private void createMultiRangeShiftTypeIfNotExists(String name, String description,
            LocalTime range1Start, LocalTime range1End,
            LocalTime range2Start, LocalTime range2End) throws Exception {
        
        java.util.Optional<ShiftType> existingShiftType = shiftTypeRepository.findFirstByNameIgnoreCase(name);
        
        if (existingShiftType.isPresent()) {
            logger.debug("    Tipo de turno '{}' ya existe", name);
            return;
        }

        ShiftType shiftType = new ShiftType();
        shiftType.setName(name);
        shiftType.setDescription(description);
        shiftType.setIsMultiRange(true);
        
        // Add time ranges
        shiftType.addTimeRange(range1Start, range1End, 1);
        shiftType.addTimeRange(range2Start, range2End, 2);
        
        // Set backward compatible fields from first range
        shiftType.setStartTime(range1Start);
        shiftType.setEndTime(range2End);
        shiftType.setIsNightShift(false); // Neither range is night

        shiftTypeRepository.save(shiftType);
        logger.info("    Nuevo tipo de turno multi-rango: {} | {}-{} y {}-{} | {}", 
            name, range1Start, range1End, range2Start, range2End, description);
    }

    /**
     * Crea un tipo de turno si no existe
     */
    private void createShiftTypeIfNotExists(String name, String description, 
            LocalTime startTime, LocalTime endTime, boolean isNightShift) throws Exception {
        java.util.Optional<ShiftType> existingShiftType = shiftTypeRepository.findFirstByNameIgnoreCase(name);
        
        if (existingShiftType.isPresent()) {
            logger.debug("    Tipo de turno '{}' ya existe", name);
            return;
        }

        ShiftType shiftType = new ShiftType();
        shiftType.setName(name);
        shiftType.setDescription(description);
        shiftType.setStartTime(startTime);
        shiftType.setEndTime(endTime);
        shiftType.setIsNightShift(isNightShift);
        shiftType.setIsMultiRange(false);
        shiftType.setTimeRanges(new java.util.ArrayList<>());

        shiftTypeRepository.save(shiftType);
        logger.info("    Nuevo tipo de turno: {} | {} | {}", name, startTime + "-" + endTime, description);
    }

    /**
     * Inicializa los empleados y sus usuarios asociados
     * Basado en los datos del documento PDF
     */
    private void initializeEmployeesAndUsers(java.util.List<Location> locations) throws Exception {
        logger.info("[7/8] Inicializando empleados y usuarios...");

        // Datos de empleados reales
        // Formato: estado, cedula, nombreCompleto, fechaNacimiento(dd/MM/yyyy), email, cargo, tipoContrato, ubicacion, fechaInicio(dd/MM/yyyy), fechaFin(dd/MM/yyyy o "")
        String[][] employeeData = {
            // 35 empleados reales
            {"Pendiente", "1081152876", "Barrios Erly Yurani", "29/01/1987", "yuranibarrios2@gmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "MICROFARMA RIVERA", "01/07/2024", "19/04/2026"},
            {"Completo", "1077852267", "Joselito Bernal Aldana ", "09/04/1989", "joselitobernalaldana@hotmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO INDEFINIDO", "MICROFARMA GUALANDAY", "26/07/2025", "25/03/2026"},
            {"Pendiente", "1081159676", "Bohorquez Puentes Leisa Mildred", "30/10/1996", "leisamildred6@gmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "MICROFARMA RIVERA", "16/08/2024", "15/08/2026"},
            {"Completo", "1075214747", "Bustos Mendez Leidy Yurani", "17/09/1986", "bleidy828@gmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "MICROFARMA BAMBU", "17/07/2025", "16/03/2026"},
            {"Pendiente", "1075314931", "Calderon Chavarro Marly Lorena", "18/09/1998", "marlylorenacalderonchavarro@gmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "MICROFARMA PINOS", "01/07/2024", "19/01/2027"},
            {"Completo", "1075304423", "Calderon Lozano Victor Daniel", "25/05/1997", "victor.calderon@microfarma.com", "GERENTE", "TÉRMINO FIJO", "ADMINISTRACIÓN", "01/07/2024", ""},
            {"Completo", "1075598061", "Castellano Luisa Fernanda", "30/07/2003", "lufercaste3007@gmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO INDEFINIDO", "MICROFARMA BUGANVILES", "20/05/2025", "19/05/2026"},
            {"Pendiente", "1079606460", "Chambo Diaz Stephania", "09/08/1992", "stephania090892@hotmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "MICROFARMA PINOS", "01/07/2024", "25/10/2026"},
            {"Completo", "1031168692", "Cortes Medina Yilver Manuel", "13/10/1996", "yilvermedina134@gmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "MICROFARMA PINOS", "01/07/2024", "31/01/2027"},
            {"Pendiente", "1007677625", "Cumbe Quintero Juana Manuela", "21/03/2000", "juanacumbeq@gmail.com", "AUXILIAR DE CONTABILIDAD", "TÉRMINO FIJO", "ADMINISTRACIÓN", "01/07/2024", "01/12/2026"},
            {"Completo", "1075211838", "Flor Perdomo Jhon Jairo", "12/04/1986", "jhonper121@gmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "MICROFARMA MANZANARES", "01/07/2024", "03/05/2026"},
            {"Completo", "36068293", "Flor Perdomo Melba", "09/11/1978", "melbaperdomo2019@gmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "MICROFARMA LIMONAR", "01/07/2024", "30/06/2026"},
            {"Pendiente", "1006484816", "Garcia Sanchez Yesica", "25/04/2002", "yesicasanch25@gmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "MICROFARMA MIRA RIO", "01/07/2024", "01/02/2027"},
            {"Pendiente", "7730178", "Gomez Gaviria Wilson", "05/01/1985", "wilson.gomez@microfarma.com", "MENSAJERO/DOMICILIARIO", "TÉRMINO FIJO", "ADMINISTRACIÓN", "16/07/2024", "15/07/2026"},
            {"Completo", "1081159183", "Hermosa Molano Jose Alejandro", "29/10/1995", "alejandrohermosa125@gmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "MICROFARMA LIMONAR", "01/07/2024", "14/01/2027"},
            {"Pendiente", "1007659484", "Laguna Lozada Yessica Maria", "04/09/2001", "yessicalaguna04@gmail.com", "AUXILIAR DE TESORERIA", "TÉRMINO FIJO", "ADMINISTRACIÓN", "02/07/2024", "01/07/2026"},
            {"Pendiente", "1059810140", "Lima Gomez Alejandro", "09/11/1985", "alejandro.lima@microfarma.com", "AUXILIAR DE BODEGA", "TÉRMINO FIJO", "ADMINISTRACIÓN", "01/07/2024", "14/06/2026"},
            {"Completo", "1080186903", "Lopez Santos Judy Marcela", "09/12/1994", "ymlopezsantos2016@gmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "MICROFARMA GIGANTE", "08/02/2025", "07/02/2027"},
            {"Completo", "1075299487", "Meñaca Lara Daniela Alejandra", "09/09/1996", "alejandralara091996@gmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "MICROFARMA BUGANVILES", "22/03/2025", "21/03/2026"},
            {"Pendiente", "1075307336", "Medina Zuñiga Jilber", "26/08/1997", "jilbermedina26@hotmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "MICROFARMA BAMBU", "01/07/2024", "14/01/2027"},
            {"Completo", "1079390598", "Montilla Montilla Jesus David", "22/12/1994", "194jesus.david@gmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "MICROFARMA MIRA RIO", "01/07/2024", "31/08/2026"},
            {"Pendiente", "1023366302", "Mosquera Castro Yarith Daniela", "06/10/2004", "mosqueradaniela896@gmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "MICROFARMA CAÑA BRAVA", "01/07/2024", "14/01/2027"},
            {"Completo", "7703003", "Mosquera Garcia John Harixon", "10/11/1976", "johnharixonm@gmail.com", "MENSAJERO/DOMICILIARIO", "TÉRMINO FIJO", "ADMINISTRACIÓN", "16/07/2025", "15/04/2026"},
            {"Completo", "1077858119", "Motta Gaviria Cindy Brigitte", "09/10/1990", "cindybrigittemottagaviria@gmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "MICROFARMA ZULUAGA", "01/07/2024", "04/06/2026"},
            {"Pendiente", "1003809563", "Muñoz Bustos Karol Daniela", "16/05/2002", "karolbustos16@gmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "MICROFARMA GUALANDAY", "01/07/2024", "19/01/2027"},
            {"Pendiente", "1075285919", "Perdomo Hernandez Robinson", "27/01/1995", "robinperdomo2701@gmail.com", "MENSAJERO/DOMICILIARIO", "TÉRMINO FIJO", "ADMINISTRACIÓN", "17/04/2025", "16/04/2026"},
            {"Completo", "1004155952", "Quintero Cardozo Karla Tatiana", "11/06/2003", "karlatati1106@gmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO INDEFINIDO", "MICROFARMA MANZANARES", "27/12/2025", "26/04/2026"},
            {"Pendiente", "1081160990", "Ramirez Gonzalez Juan Manuel", "18/04/1999", "januel0418@gmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "MICROFARMA PINOS", "01/07/2024", "14/01/2027"},
            {"Pendiente", "1075231948", "Rivera Suarez Angel Farid", "25/09/2006", "angelfaridr1@gmail.com", "PASANTE", "TÉRMINO INDEFINIDO", "ADMINISTRACIÓN", "15/12/2025", "14/06/2026"},
            {"Completo", "1075279605", "Rueda Arias Teylor Mauricio", "01/02/1994", "teylorrueda4@gmail.com", "MENSAJERO/DOMICILIARIO", "TÉRMINO INDEFINIDO", "ADMINISTRACIÓN", "23/09/2025", "22/03/2026"},
            {"Completo", "1075263597", "Serrato Proaños Yesica Lorena", "02/04/1992", "lorena12345@gmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO INDEFINIDO", "MICROFARMA MANZANARES", "16/05/2025", "15/05/2026"},
            {"Pendiente", "1077874441", "Trujillo Polo Miguel Angel", "28/02/1997", "mangelt24@hotmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "MICROFARMA ZULUAGA", "01/07/2024", "02/05/2026"},
            {"Completo", "1080188100", "Villanueva Penagos Karolayn", "31/10/1996", "karolayn_villa@hotmail.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "MICROFARMA GIGANTE", "01/07/2024", "26/03/2026"}
        };

        int successCount = 0;
        int errorCount = 0;

        for (String[] empData : employeeData) {
            try {
                String estado = empData[0];
                String cedula = empData[1];
                String nombreCompleto = empData[2];
                String fechaNacimientoStr = empData[3];
                String email = empData[4];
                String positionName = empData[5];
                String contractTypeName = empData[6];
                String ubicacion = empData[7];
                String fechaInicioStr = empData[8];
                String fechaFinStr = empData[9];

                // Procesar nombres: formato original "Apellido Paterno Apellido Materno Nombre1 Nombre2"
                // Debo invertir: primeros elementos = apellidos, últimos = nombres
                String firstName = "";
                String lastName = "";
                if (nombreCompleto != null && !nombreCompleto.isEmpty()) {
                    String trimmed = nombreCompleto.trim();
                    String[] parts = trimmed.split(" ");
                    
                    if (parts.length == 1) {
                        // Solo un nombre o apellido
                        firstName = parts[0];
                        lastName = "";
                    } else if (parts.length == 2) {
                        // Nombre y apellido (ej: "Yilver Manuel")
                        firstName = parts[0] + " " + parts[1];
                        lastName = "";
                    } else if (parts.length == 3) {
                        // 1 nombre + 2 apellidos (ej: "Cortes Medina Yilver")
                        firstName = parts[2];
                        lastName = parts[0] + " " + parts[1];
                    } else if (parts.length >= 4) {
                        // 4+ partes: últimos 2 = nombres, primeros 2 = apellidos
                        // ej: "Cortes Medina Yilver Manuel" → firstName="Yilver Manuel", lastName="Cortes Medina"
                        firstName = parts[parts.length - 2] + " " + parts[parts.length - 1];
                        lastName = parts[0] + " " + parts[1];
                    }
                }

                // Parsear fecha de nacimiento (solo día/mes, año 2000 placeholder)
                LocalDate birthDate = null;
                if (fechaNacimientoStr != null && !fechaNacimientoStr.isEmpty()) {
                    try {
                        String[] parts = fechaNacimientoStr.split("/");
                        int day = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]);
                        birthDate = LocalDate.of(2000, month, day);
                    } catch (Exception e) {
                        logger.warn("Fecha de nacimiento inválida '{}' para {}", fechaNacimientoStr, firstName);
                    }
                }

                // Parsear fecha de inicio de contrato
                LocalDate hireDate = null;
                if (fechaInicioStr != null && !fechaInicioStr.isEmpty()) {
                    try {
                        String[] parts = fechaInicioStr.split("/");
                        int day = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]);
                        int year = Integer.parseInt(parts[2]);
                        hireDate = LocalDate.of(year, month, day);
                    } catch (Exception e) {
                        logger.warn("Fecha de inicio inválida '{}' para {}", fechaInicioStr, firstName);
                    }
                }

                // Parsear fecha de fin de contrato
                LocalDate contractEndDate = null;
                if (fechaFinStr != null && !fechaFinStr.isEmpty()) {
                    try {
                        String[] parts = fechaFinStr.split("/");
                        int day = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]);
                        int year = Integer.parseInt(parts[2]);
                        contractEndDate = LocalDate.of(year, month, day);
                    } catch (Exception e) {
                        logger.warn("Fecha de fin inválida '{}' para {}", fechaFinStr, firstName);
                    }
                }

                // 👇 MODIFICACIÓN: todos los empleados se crean con status = true (Completo)
                boolean status = true;  // Ignorar el campo 'estado'

                // Buscar ubicación (quitar "MICROFARMA " del nombre)
                String locationName = ubicacion;
                if (locationName.startsWith("MICROFARMA ")) {
                    locationName = locationName.substring("MICROFARMA ".length());
                }
                final String finalLocationName = locationName;
                Location location = locations.stream()
                    .filter(l -> l.getName().equals(finalLocationName))
                    .findFirst()
                    .orElse(null);

                if (location == null) {
                    logger.warn("    No se encontró la ubicación '{}' para el empleado {}", locationName, firstName);
                    errorCount++;
                    continue;
                }

                createEmployeeAndUser(firstName, lastName, email, positionName, contractTypeName, location,
                        birthDate, hireDate, contractEndDate, status);
                successCount++;

                String birthdayMsg = birthDate != null ? " [CUMPLE: " + fechaNacimientoStr + "]" : "";
                String contractMsg = contractEndDate != null ? " [CONTRATO: " + fechaFinStr + "]" : " [CONTRATO: indefinido]";
                logger.info("  - Empleado '{} {}' creado en {}{}{} (Estado: Activo)", firstName, lastName, locationName, birthdayMsg, contractMsg);

            } catch (Exception e) {
                logger.error("    Error al crear empleado {}: {}", empData[2], e.getMessage());
                errorCount++;
            }
        }

        logger.info("    Empleados completados: {} exitosos, {} con errores", successCount, errorCount);
    }

    /**
     * Crea un empleado y su usuario asociado
     */
    private void createEmployeeAndUser(String firstName, String lastName, String email,
            String positionName, String contractTypeName, Location location,
            LocalDate birthDate, LocalDate hireDate, LocalDate contractEndDate, boolean status) throws Exception {

        // Obtener cargo
        Position position = positionRepository.findFirstByNameIgnoreCase(positionName)
            .orElseThrow(() -> new Exception("Cargo no encontrado: " + positionName));

        // Obtener tipo de contrato
        ContractType contractType = contractTypeRepository.findFirstByNameIgnoreCase(contractTypeName)
            .orElseThrow(() -> new Exception("Tipo de contrato no encontrado: " + contractTypeName));

        // Verificar si el empleado ya existe por email
        String fullName = (firstName + " " + lastName).trim();
        java.util.Optional<Employee> existingEmployee = employeeRepository.findByEmail(email);

        Employee employee;
        if (existingEmployee.isPresent()) {
            logger.debug("    El empleado '{}' ya existe", fullName);
            employee = existingEmployee.get();
        } else {
            // Crear empleado
            employee = new Employee();
            employee.setFirstName(firstName);
            employee.setLastName(lastName);
            employee.setEmail(email);
            employee.setPosition(position);
            employee.setContractType(contractType);
            employee.setHireDate(hireDate != null ? hireDate : TODAY.minusMonths(6)); // fallback si no se pudo parsear
            employee.setBirthDate(birthDate);
            employee.setContractEndDate(contractEndDate);
            employee.setStatus(status);
            // Podrías asignar teléfono y dirección si los tuvieras, aquí se dejan nulos

            employee = employeeRepository.save(employee);
            logger.info("    Nuevo empleado creado: {} {}", firstName, lastName);
        }

        // Crear usuario para el empleado si no existe
        if (userService.findByEmail(email).isEmpty()) {
            User user = new User();
            user.setName(fullName);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode("microfarma2026"));
            user.setActive(status); // Usuario activo si el empleado está activo
            user.setEmployee(employee);

            // Asignar rol según el cargo
            Role role;
            if (positionName.contains("GERENTE")) {
                role = roleService.findByName("GERENTE_SUCURSAL").orElse(null);
            } else {
                role = roleService.findByName("EMPLOYEE").orElse(null);
            }
            user.setRole(role);

            userService.save(user);
            logger.info("    Usuario creado para: {} (Rol: {})", email, role != null ? role.getName() : "EMPLOYEE");
        } else {
            logger.debug("    El usuario '{}' ya existe", email);
        }

        // Crear EmployeeLocation para vincular empleado con ubicación
        createEmployeeLocation(employee, location);
    }

    /**
     * Crea la relación empleado-ubicación
     */
    private void createEmployeeLocation(Employee employee, Location location) {
        // Verificar si ya existe la relación
        java.util.List<EmployeeLocation> existingRelations = employeeLocationRepository.findByEmployeeId(employee.getId());
        boolean relationExists = existingRelations.stream()
            .anyMatch(el -> el.getLocation().getId().equals(location.getId()));

        if (!relationExists) {
            EmployeeLocation employeeLocation = new EmployeeLocation();
            employeeLocation.setEmployee(employee);
            employeeLocation.setLocation(location);
            employeeLocationRepository.save(employeeLocation);
            logger.debug("    Relación empleado-ubicación creada: {} -> {}",
                employee.getFirstName(), location.getName());
        }
    }

    /**
     * Crea el usuario administrador del sistema
     */
    private void createSystemAdministrator() throws Exception {
        logger.info("[8/8] Creando administrador del sistema...");

        String adminEmail = "admin@microfarma.com";

        // Verificar si ya existe el administrador
        if (userService.findByEmail(adminEmail).isPresent()) {
            logger.info("    El administrador '{}' ya existe", adminEmail);
            return;
        }

        // Crear usuario administrador
        User adminUser = new User();
        adminUser.setName("Administrador del Sistema");
        adminUser.setEmail(adminEmail);
        adminUser.setPasswordHash(passwordEncoder.encode("adminMF2026*"));
        adminUser.setActive(true);

        // Asignar rol ADMIN
        Role adminRole = roleService.findByName("ADMIN")
            .orElseThrow(() -> new Exception("Rol ADMIN no encontrado"));
        adminUser.setRole(adminRole);

        userService.save(adminUser);
        logger.info("    Administrador del sistema creado: {}", adminEmail);
        logger.info("    NOTA: Por seguridad, cambie la contraseña en el primer inicio de sesión");
    }

    /**
     * Inicializa los tipos de noticia para alertas automáticas
     * Estos tipos son usados por el sistema de alertas automáticas
     */
    private void initializeNewsTypes() throws Exception {
        logger.info("[9/9] Inicializando tipos de noticia para alertas...");

        // Tipos de noticia para alertas
        createNewsTypeIfNotExists("CUMPLEAÑOS", "Alertas de cumpleaños de empleados");
        createNewsTypeIfNotExists("CONTRATO_POR_VENCER", "Alertas de contratos por vencer");
        createNewsTypeIfNotExists("CONTRATO_VENCIDO", "Alertas de contratos vencidos");

        logger.info("    Tipos de noticia para alertas completados: 3");
    }

    /**
     * Crea un tipo de noticia si no existe
     */
    private void createNewsTypeIfNotExists(String name, String description) throws Exception {
        if (newsTypeRepository.findByNameIgnoreCase(name).isPresent()) {
            logger.debug("    El tipo de noticia '{}' ya existe", name);
            return;
        }

        NewsType newsType = new NewsType();
        newsType.setName(name);
        newsType.setDescription(description);
        newsTypeRepository.save(newsType);
        logger.info("    Nuevo tipo de noticia: {} | {}", name, description);
    }
}