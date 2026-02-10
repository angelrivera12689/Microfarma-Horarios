package MicrofarmaHorarios.Security.Config;

import java.time.LocalDate;
import java.time.LocalTime;

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

            logger.info("=========================================================");
            logger.info("INICIALIZACIÓN COMPLETADA EXITOSAMENTE");
            logger.info("=========================================================");
            logger.info("Resumen de datos creados:");
            logger.info("- Empresa: 1");
            logger.info("- Sucursales: {}", locations.size());
            logger.info("- Roles: 4 (ADMIN, GERENTE_SUCURSAL, EMPLEADO, USER)");
            logger.info("- Tipos de contrato: 4");
            logger.info("- Cargos: 5");
            logger.info("- Tipos de turno: 5");
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
        logger.info("[3/8] Inicializando 12 sucursales...");

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
            {"GIGANTE", "(601) 111-1012", "Cl. 30 #18-90, Bogotá"}
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
        createPositionIfNotExists("AUXILIAR DE FARMACIA", "Encargado de la atención al cliente y dispensación de medicamentos", 1500000.0);
        createPositionIfNotExists("CAJERO", "Responsable de las transacciones y manejo de efectivo", 1400000.0);
        createPositionIfNotExists("ASESOR COMERCIAL", "Encargado de las ventas y atención al cliente", 1300000.0);
        createPositionIfNotExists("AUXILIAR DE BODEGA", "Encargado del manejo de inventario y recepción de mercancía", 1200000.0);

        logger.info("    Cargos completados: 5");
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
        
        // Turno PARTIDO: Mañana (7am-2pm) y Tarde (6pm-10pm) - 8 horas con descanso de 4 horas
        createShiftTypeIfNotExists("PARTIDO", "Turno partido - 7am a 2pm y 6pm a 10pm (8 horas)", 
            LocalTime.of(7, 0), LocalTime.of(22, 0), false);
        
        // Turno LARGO: Extendido 7:00 AM - 10:00 PM (15 horas)
        createShiftTypeIfNotExists("LARGO", "Turno extendido - 7am a 10pm (15 horas)", 
            LocalTime.of(7, 0), LocalTime.of(22, 0), false);

        logger.info("    Tipos de turno completados: 5");
        logger.info("    - MAÑANA: 7am-2pm (7h)");
        logger.info("    - TARDE: 2pm-10pm (8h)");
        logger.info("    - NOCHE: 10pm-7am (9h)");
        logger.info("    - PARTIDO: 7am-2pm + 6pm-10pm (8h)");
        logger.info("    - LARGO: 7am-10pm (15h)");
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

        shiftTypeRepository.save(shiftType);
        logger.info("    Nuevo tipo de turno: {} | {} | {}", name, startTime + "-" + endTime, description);
    }

    /**
     * Inicializa los empleados y sus usuarios asociados
     * Basado en los datos del documento PDF
     */
    private void initializeEmployeesAndUsers(java.util.List<Location> locations) throws Exception {
        logger.info("[7/8] Inicializando empleados y usuarios...");

        // Datos de empleados extraídos del PDF
        String[][] employeeData = {
            // PINOS - 4 empleados
            {"Yilver", "", "yilver@microfarma.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "PINOS"},
            {"Stephania", "", "stephania@microfarma.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "PINOS"},
            {"Juan Manuel", "", "juan.manuel@microfarma.com", "CAJERO", "TÉRMINO FIJO", "PINOS"},
            {"Marly", "", "marly@microfarma.com", "ASESOR COMERCIAL", "TÉRMINO FIJO", "PINOS"},
            
            // IPANEMA - 4 empleados (incluye Cristo Jesus*)
            {"Keara", "", "keara@microfarma.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "IPANEMA"},
            {"Brayan Gonzalez", "", "brayan.gonzalez@microfarma.com", "CAJERO", "TÉRMINO INDEFINIDO", "IPANEMA"},
            {"Laura Sofia", "", "laura.sofia@microfarma.com", "ASESOR COMERCIAL", "TÉRMINO FIJO", "IPANEMA"},
            {"Cristo Jesus", "", "cristo.jesus@microfarma.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "IPANEMA"},
            
            // GUALANDAY - 2 empleados
            {"Joselito Bernal", "", "joselito.bernal@microfarma.com", "GERENTE DE SUCURSAL", "TÉRMINO INDEFINIDO", "GUALANDAY"},
            {"Karol", "", "karol@microfarma.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "GUALANDAY"},
            
            // BUGANVILES - 2 empleados
            {"Daniela Meñaca", "", "daniela.menaca@microfarma.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "BUGANVILES"},
            {"Luisa", "", "luisa@microfarma.com", "CAJERO", "TÉRMINO FIJO", "BUGANVILES"},
            
            // BAMBU - 2 empleados
            {"Jilber", "", "jilber@microfarma.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "BAMBU"},
            {"Leidy Bustos", "", "leidy.bustos@microfarma.com", "ASESOR COMERCIAL", "TÉRMINO FIJO", "BAMBU"},
            
            // LIMONAR - 2 empleados
            {"Alejandro Hermosa", "", "alejandro.hermosa@microfarma.com", "GERENTE DE SUCURSAL", "TÉRMINO INDEFINIDO", "LIMONAR"},
            {"Melba", "", "melba@microfarma.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "LIMONAR"},
            
            // MANZANAREZ - 3 empleados
            {"Karla", "", "karla@microfarma.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "MANZANAREZ"},
            {"Jhon", "", "jhon@microfarma.com", "CAJERO", "TÉRMINO FIJO", "MANZANAREZ"},
            {"Yesica Serrato", "", "yesica.serrato@microfarma.com", "ASESOR COMERCIAL", "TÉRMINO FIJO", "MANZANAREZ"},
            
            // MIRA RIO - 2 empleados
            {"Jesus", "", "jesus@microfarma.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "MIRA RIO"},
            {"Yessica", "", "yessica@microfarma.com", "ASESOR COMERCIAL", "TÉRMINO FIJO", "MIRA RIO"},
            
            // CAÑA BRAVA - 2 empleados
            {"Daniela Mosquera", "", "daniela.mosquera@microfarma.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "CAÑA BRAVA"},
            {"Sergio Sosa", "", "sergio.sosa@microfarma.com", "ASESOR COMERCIAL", "TÉRMINO FIJO", "CAÑA BRAVA"},
            
            // RIVERA - 2 empleados
            {"Mildred", "", "mildred@microfarma.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "RIVERA"},
            {"Yurani", "", "yurani@microfarma.com", "ASESOR COMERCIAL", "TÉRMINO FIJO", "RIVERA"},
            
            // ZULUAGA - 2 empleados
            {"Cindy", "", "cindy@microfarma.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "ZULUAGA"},
            {"Miguel", "", "miguel@microfarma.com", "ASESOR COMERCIAL", "TÉRMINO FIJO", "ZULUAGA"},
            
            // GIGANTE - 3 empleados
            {"Yudi", "", "yudi@microfarma.com", "AUXILIAR DE FARMACIA", "TÉRMINO FIJO", "GIGANTE"},
            {"Karoline", "", "karoline@microfarma.com", "ASESOR COMERCIAL", "TÉRMINO FIJO", "GIGANTE"},
            {"Johan", "", "johan@microfarma.com", "CAJERO", "TÉRMINO FIJO", "GIGANTE"},
            
            // DANIEL CALDERÓN - RH (Administrador)
            {"Daniel", "Calderón", "daniel.calderon@microfarma.com", "GERENTE DE SUCURSAL", "TÉRMINO INDEFINIDO", "PINOS"},
        };

        int successCount = 0;
        int errorCount = 0;

        for (String[] empData : employeeData) {
            try {
                String firstName = empData[0];
                String lastName = empData[1];
                String email = empData[2];
                String positionName = empData[3];
                String contractTypeName = empData[4];
                String locationName = empData[5];

                Location location = locations.stream()
                    .filter(l -> l.getName().equals(locationName))
                    .findFirst()
                    .orElse(null);

                if (location == null) {
                    logger.warn("    No se encontró la ubicación '{}' para el empleado {}", locationName, firstName);
                    errorCount++;
                    continue;
                }

                createEmployeeAndUser(firstName, lastName, email, positionName, contractTypeName, location);
                successCount++;
                logger.debug("  - Empleado '{} {}' creado en {}", firstName, lastName, locationName);
                
            } catch (Exception e) {
                logger.error("    Error al crear empleado {}: {}", empData[0], e.getMessage());
                errorCount++;
            }
        }

        logger.info("    Empleados completados: {} exitosos, {} con errores", successCount, errorCount);
    }

    /**
     * Crea un empleado y su usuario asociado
     */
    private void createEmployeeAndUser(String firstName, String lastName, String email, 
            String positionName, String contractTypeName, Location location) throws Exception {
        
        // Obtener cargo
        Position position = positionRepository.findFirstByNameIgnoreCase(positionName)
            .orElseThrow(() -> new Exception("Cargo no encontrado: " + positionName));
        
        // Obtener tipo de contrato
        ContractType contractType = contractTypeRepository.findFirstByNameIgnoreCase(contractTypeName)
            .orElseThrow(() -> new Exception("Tipo de contrato no encontrado: " + contractTypeName));
        
        // Verificar si el empleado ya existe
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
            employee.setHireDate(LocalDate.now());
            employee.setStatus(true);
            
            employee = employeeRepository.save(employee);
            logger.info("    Nuevo empleado creado: {} {}", firstName, lastName);
        }
        
        // Crear usuario para el empleado si no existe
        if (userService.findByEmail(email).isEmpty()) {
            User user = new User();
            user.setName(fullName);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode("microfarma2026"));
            user.setActive(true);
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
}
