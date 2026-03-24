package MicrofarmaHorarios.News.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import MicrofarmaHorarios.HumanResources.Entity.ContractType;
import MicrofarmaHorarios.HumanResources.Entity.Employee;
import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesEmployeeService;
import MicrofarmaHorarios.News.Entity.News;
import MicrofarmaHorarios.News.Entity.NewsType;
import MicrofarmaHorarios.News.IRepository.INewsNewsRepository;
import MicrofarmaHorarios.News.IRepository.INewsNewsTypeRepository;

/**
 * AlertGenerationService - Servicio para generar alertas automáticas
 * 
 * Este servicio genera noticias automáticas para:
 * - Cumpleaños de empleados
 * - Contratos por vencer
 * - Contratos vencidos
 * 
 * Se ejecuta diariamente desde NotificationScheduler
 * 
 * @author Microfarma Horarios System
 * @version 1.0
 */
@Service
public class AlertGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(AlertGenerationService.class);

    // Constantes para tipos de alerta
    public static final String NEWS_TYPE_BIRTHDAY = "CUMPLEAÑOS";
    public static final String NEWS_TYPE_CONTRACT_EXPIRING = "CONTRATO_POR_VENCER";
    public static final String NEWS_TYPE_CONTRACT_EXPIRED = "CONTRATO_VENCIDO";
    
    // Días de anticipación para alertas de contrato
    private static final int CONTRACT_EXPIRING_DAYS = 30;

    @Autowired
    private IHumanResourcesEmployeeService employeeService;

    @Autowired
    private INewsNewsRepository newsRepository;

    @Autowired
    private INewsNewsTypeRepository newsTypeRepository;

    /**
     * Genera todas las alertas del día
     * Este método es llamado por el scheduler
     */
    public void generateDailyAlerts() {
        logger.info("=========================================================");
        logger.info("INICIANDO GENERACIÓN DE ALERTAS DIARIAS");
        logger.info("=========================================================");
        
        try {
            // 1. Generar alertas de cumpleaños
            generateBirthdayAlerts();
            
            // 2. Generar alertas de contratos por vencer
            generateContractExpiringAlerts();
            
            // 3. Generar alertas de contratos vencidos
            generateContractExpiredAlerts();
            
            logger.info("=========================================================");
            logger.info("GENERACIÓN DE ALERTAS COMPLETADA");
            logger.info("=========================================================");
        } catch (Exception e) {
            logger.error("ERROR DURANTE LA GENERACIÓN DE ALERTAS: {}", e.getMessage(), e);
        }
    }

    /**
     * Genera alertas de cumpleaños para el día actual
     */
    public void generateBirthdayAlerts() {
        logger.info("[1/3] Generando alertas de cumpleaños...");
        
        try {
            LocalDate today = LocalDate.now();
            List<Employee> allEmployees = employeeService.all();
            
            int birthdayCount = 0;
            
            for (Employee employee : allEmployees) {
                if (employee.getBirthDate() == null) {
                    continue;
                }
                
                // Verificar si es cumpleaños hoy (mismo mes y día)
                if (employee.getBirthDate().getMonth() == today.getMonth() &&
                    employee.getBirthDate().getDayOfMonth() == today.getDayOfMonth()) {
                    
                    // Verificar si ya existe una noticia de cumpleaños hoy
                    Optional<NewsType> newsTypeOpt = newsTypeRepository.findByNameIgnoreCase(NEWS_TYPE_BIRTHDAY);
                    if (newsTypeOpt.isEmpty()) {
                        logger.warn("    Tipo de noticia CUMPLEAÑOS no encontrado, creando...");
                        createNewsType(NEWS_TYPE_BIRTHDAY, "Alertas de cumpleaños de empleados");
                    }
                    
                    Optional<NewsType> type = newsTypeRepository.findByNameIgnoreCase(NEWS_TYPE_BIRTHDAY);
                    if (type.isPresent() && !hasNewsToday(employee.getId(), type.get().getId(), today)) {
                        createBirthdayNews(employee, type.get());
                        birthdayCount++;
                        logger.info("    ✅ Alerta de cumpleaños creada para: {}", employee.getFirstName() + " " + employee.getLastName());
                    }
                }
            }
            
            logger.info("    Alertas de cumpleaños generadas: {}", birthdayCount);
            
        } catch (Exception e) {
            logger.error("    ERROR al generar alertas de cumpleaños: {}", e.getMessage());
        }
    }

    /**
     * Genera alertas de contratos por vencer (30 días antes)
     */
    public void generateContractExpiringAlerts() {
        logger.info("[2/3] Generando alertas de contratos por vencer...");
        
        try {
            LocalDate today = LocalDate.now();
            LocalDate expiryThreshold = today.plusDays(CONTRACT_EXPIRING_DAYS);
            List<Employee> allEmployees = employeeService.all();
            
            int expiringCount = 0;
            
            for (Employee employee : allEmployees) {
                LocalDate contractEndDate = calculateContractEndDate(employee);
                
                if (contractEndDate == null) {
                    continue; // No se puede calcular fecha de fin
                }
                
                // Verificar si el contrato está por vencer (entre hoy y la fecha límite)
                if (!contractEndDate.isBefore(today) && !contractEndDate.isAfter(expiryThreshold)) {
                    
                    // Verificar si ya existe una alerta de contrato por vencer hoy
                    Optional<NewsType> newsTypeOpt = newsTypeRepository.findByNameIgnoreCase(NEWS_TYPE_CONTRACT_EXPIRING);
                    if (newsTypeOpt.isEmpty()) {
                        logger.warn("    Tipo de noticia CONTRATO_POR_VENCER no encontrado, creando...");
                        createNewsType(NEWS_TYPE_CONTRACT_EXPIRING, "Alertas de contratos por vencer");
                    }
                    
                    Optional<NewsType> type = newsTypeRepository.findByNameIgnoreCase(NEWS_TYPE_CONTRACT_EXPIRING);
                    if (type.isPresent() && !hasNewsToday(employee.getId(), type.get().getId(), today)) {
                        createContractExpiringNews(employee, contractEndDate, type.get());
                        expiringCount++;
                        
                        long daysUntilExpiry = ChronoUnit.DAYS.between(today, contractEndDate);
                        logger.info("    ✅ Alerta creada para: {} {} - Vence en {} días", 
                            employee.getFirstName(), employee.getLastName(), daysUntilExpiry);
                    }
                }
            }
            
            logger.info("    Alertas de contratos por vencer generadas: {}", expiringCount);
            
        } catch (Exception e) {
            logger.error("    ERROR al generar alertas de contratos por vencer: {}", e.getMessage());
        }
    }

    /**
     * Genera alertas de contratos vencidos
     */
    public void generateContractExpiredAlerts() {
        logger.info("[3/3] Generando alertas de contratos vencidos...");
        
        try {
            LocalDate today = LocalDate.now();
            List<Employee> allEmployees = employeeService.all();
            
            int expiredCount = 0;
            
            for (Employee employee : allEmployees) {
                LocalDate contractEndDate = calculateContractEndDate(employee);
                
                if (contractEndDate == null) {
                    continue;
                }
                
                // Verificar si el contrato está vencido
                if (contractEndDate.isBefore(today)) {
                    
                    // Verificar si ya existe una alerta de contrato vencido hoy
                    Optional<NewsType> newsTypeOpt = newsTypeRepository.findByNameIgnoreCase(NEWS_TYPE_CONTRACT_EXPIRED);
                    if (newsTypeOpt.isEmpty()) {
                        logger.warn("    Tipo de noticia CONTRATO_VENCIDO no encontrado, creando...");
                        createNewsType(NEWS_TYPE_CONTRACT_EXPIRED, "Alertas de contratos vencidos");
                    }
                    
                    Optional<NewsType> type = newsTypeRepository.findByNameIgnoreCase(NEWS_TYPE_CONTRACT_EXPIRED);
                    if (type.isPresent() && !hasNewsToday(employee.getId(), type.get().getId(), today)) {
                        createContractExpiredNews(employee, contractEndDate, type.get());
                        expiredCount++;
                        
                        long daysExpired = ChronoUnit.DAYS.between(contractEndDate, today);
                        logger.info("    ✅ Alerta creada para: {} {} - Venció hace {} días", 
                            employee.getFirstName(), employee.getLastName(), daysExpired);
                    }
                }
            }
            
            logger.info("    Alertas de contratos vencidos generadas: {}", expiredCount);
            
        } catch (Exception e) {
            logger.error("    ERROR al generar alertas de contratos vencidos: {}", e.getMessage());
        }
    }

    /**
     * Calcula la fecha de fin del contrato
     * Usa contractEndDate si existe, o calcula a partir de hireDate + durationMonths
     */
    private LocalDate calculateContractEndDate(Employee employee) {
        // Si tiene fecha de fin de contrato definida, usarla
        if (employee.getContractEndDate() != null) {
            return employee.getContractEndDate();
        }
        
        // Si no tiene tipo de contrato, no se puede calcular
        if (employee.getContractType() == null) {
            return null;
        }
        
        ContractType contractType = employee.getContractType();
        
        // Si es indefinido (durationMonths = 0), no tiene fecha de vencimiento
        if (contractType.getDurationMonths() == null || contractType.getDurationMonths() == 0) {
            return null;
        }
        
        // Calcular fecha de fin a partir de la fecha de contratación
        if (employee.getHireDate() != null) {
            return employee.getHireDate().plusMonths(contractType.getDurationMonths());
        }
        
        return null;
    }

    /**
     * Verifica si ya existe una noticia para hoy
     */
    private boolean hasNewsToday(String employeeId, String newsTypeId, LocalDate date) {
        try {
            List<News> todayNews = newsRepository.findByPublicationDateAndNewsTypeId(date, newsTypeId);
            return todayNews.stream().anyMatch(news -> 
                news.getEmployee() != null && news.getEmployee().getId().equals(employeeId));
        } catch (Exception e) {
            logger.warn("Error al verificar noticias existentes: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Crea una noticia de cumpleaños
     */
    private void createBirthdayNews(Employee employee, NewsType newsType) {
        try {
            News news = new News();
            news.setTitle("🎂 ¡Feliz Cumpleaños! - " + employee.getFirstName() + " " + employee.getLastName());
            news.setContent("Hoy celebramos el cumpleaños de " + employee.getFirstName() + " " + employee.getLastName() + 
                ". ¡Felicitaciones y muchos éxitos en este día especial!");
            news.setPublicationDate(LocalDate.now());
            news.setNewsType(newsType);
            news.setEmployee(employee);
            
            newsRepository.save(news);
            logger.debug("    Noticias de cumpleaños guardada para: {}", employee.getEmail());
        } catch (Exception e) {
            logger.error("    ERROR al crear noticia de cumpleaños: {}", e.getMessage());
        }
    }

    /**
     * Crea una noticia de contrato por vencer
     */
    private void createContractExpiringNews(Employee employee, LocalDate endDate, NewsType newsType) {
        try {
            long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
            
            String contractTypeName = employee.getContractType() != null ? 
                employee.getContractType().getName() : "Por definir";
            
            News news = new News();
            news.setTitle("⚠️ Contrato por Vencer - " + employee.getFirstName() + " " + employee.getLastName());
            news.setContent("El contrato de " + employee.getFirstName() + " " + employee.getLastName() + 
                " (" + contractTypeName + ") vencerá en " + daysUntilExpiry + " días (Fecha: " + endDate + 
                "). Por favor, gestionar la renovación a tiempo.");
            news.setPublicationDate(LocalDate.now());
            news.setNewsType(newsType);
            news.setEmployee(employee);
            
            newsRepository.save(news);
            logger.debug("    Noticias de contrato por vencer guardada para: {}", employee.getEmail());
        } catch (Exception e) {
            logger.error("    ERROR al crear noticia de contrato por vencer: {}", e.getMessage());
        }
    }

    /**
     * Crea una noticia de contrato vencido
     */
    private void createContractExpiredNews(Employee employee, LocalDate endDate, NewsType newsType) {
        try {
            long daysExpired = ChronoUnit.DAYS.between(endDate, LocalDate.now());
            
            String contractTypeName = employee.getContractType() != null ? 
                employee.getContractType().getName() : "Por definir";
            
            News news = new News();
            news.setTitle("🔴 Contrato Vencido - " + employee.getFirstName() + " " + employee.getLastName());
            news.setContent("URGENTE: El contrato de " + employee.getFirstName() + " " + employee.getLastName() + 
                " (" + contractTypeName + ") está vencido desde hace " + daysExpired + " días (Fecha de vencimiento: " + 
                endDate + "). Se requiere acción inmediata para regularizar la situación.");
            news.setPublicationDate(LocalDate.now());
            news.setNewsType(newsType);
            news.setEmployee(employee);
            
            newsRepository.save(news);
            logger.debug("    Noticias de contrato vencido guardada para: {}", employee.getEmail());
        } catch (Exception e) {
            logger.error("    ERROR al crear noticia de contrato vencido: {}", e.getMessage());
        }
    }

    /**
     * Crea un tipo de noticia si no existe
     */
    private void createNewsType(String name, String description) {
        try {
            Optional<NewsType> existing = newsTypeRepository.findByNameIgnoreCase(name);
            if (existing.isEmpty()) {
                NewsType newsType = new NewsType();
                newsType.setName(name);
                newsType.setDescription(description);
                newsTypeRepository.save(newsType);
                logger.info("    Tipo de noticia creado: {}", name);
            }
        } catch (Exception e) {
            logger.error("    ERROR al crear tipo de noticia {}: {}", name, e.getMessage());
        }
    }
}
