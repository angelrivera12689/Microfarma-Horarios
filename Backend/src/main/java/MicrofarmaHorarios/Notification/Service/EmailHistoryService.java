package MicrofarmaHorarios.Notification.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import MicrofarmaHorarios.Notification.Entity.DeliveryStatus;
import MicrofarmaHorarios.Notification.Entity.EmailHistory;
import MicrofarmaHorarios.Notification.IRepository.IEmailHistoryRepository;
import MicrofarmaHorarios.Notification.IService.IEmailHistoryService;

@Service
public class EmailHistoryService implements IEmailHistoryService {

    @Autowired
    private IEmailHistoryRepository repository;

    @Override
    public List<EmailHistory> all() throws Exception {
        return repository.findAll();
    }

    @Override
    public List<EmailHistory> findByStateTrue() throws Exception {
        return repository.findAll().stream()
                .filter(entity -> entity.getStatus() != null && entity.getStatus())
                .toList();
    }

    @Override
    public Optional<EmailHistory> findById(String id) throws Exception {
        Optional<EmailHistory> op = repository.findById(id);
        if (op.isEmpty()) {
            throw new Exception("Registro no encontrado");
        }
        return op;
    }

    @Override
    public EmailHistory save(EmailHistory entity) throws Exception {
        try {
            entity.setStatus(true);
            return repository.save(entity);
        } catch (Exception e) {
            throw new Exception("Error al guardar la entidad: " + e.getMessage());
        }
    }

    @Override
    public void update(String id, EmailHistory entity) throws Exception {
        Optional<EmailHistory> op = repository.findById(id);
        if (op.isEmpty()) {
            throw new Exception("Registro no encontrado");
        } else if (op.get().getDeletedAt() != null) {
            throw new Exception("Registro inhabilitado");
        }
        EmailHistory entityUpdate = op.get();
        entityUpdate.setRecipient(entity.getRecipient());
        entityUpdate.setSubject(entity.getSubject());
        entityUpdate.setBody(entity.getBody());
        entityUpdate.setSentAt(entity.getSentAt());
        entityUpdate.setDeliveryStatus(entity.getDeliveryStatus());
        entityUpdate.setErrorMessage(entity.getErrorMessage());
        entityUpdate.setEmailType(entity.getEmailType());
        entityUpdate.setReferenceId(entity.getReferenceId());
        repository.save(entityUpdate);
    }

    @Override
    public void delete(String id) throws Exception {
        Optional<EmailHistory> op = repository.findById(id);
        if (op.isEmpty()) {
            throw new Exception("Registro no encontrado");
        }
        EmailHistory entityUpdate = op.get();
        entityUpdate.setDeletedAt(LocalDateTime.now());
        repository.save(entityUpdate);
    }

    @Override
    public List<EmailHistory> findByFilters(String recipient, String emailType, DeliveryStatus deliveryStatus,
                                            LocalDateTime sentAtFrom, LocalDateTime sentAtTo) throws Exception {
        // For simplicity, filter in memory. In production, use custom repository methods or JPA Criteria
        return repository.findAll().stream()
                .filter(e -> (recipient == null || e.getRecipient().contains(recipient)))
                .filter(e -> (emailType == null || emailType.equals(e.getEmailType())))
                .filter(e -> (deliveryStatus == null || deliveryStatus.equals(e.getDeliveryStatus())))
                .filter(e -> (sentAtFrom == null || !e.getSentAt().isBefore(sentAtFrom)))
                .filter(e -> (sentAtTo == null || !e.getSentAt().isAfter(sentAtTo)))
                .toList();
    }

    @Override
    public void updateDeliveryStatus(String id, DeliveryStatus deliveryStatus, String errorMessage) throws Exception {
        Optional<EmailHistory> op = repository.findById(id);
        if (op.isEmpty()) {
            throw new Exception("Registro no encontrado");
        }
        EmailHistory entity = op.get();
        entity.setDeliveryStatus(deliveryStatus);
        entity.setErrorMessage(errorMessage);
        repository.save(entity);
    }
}