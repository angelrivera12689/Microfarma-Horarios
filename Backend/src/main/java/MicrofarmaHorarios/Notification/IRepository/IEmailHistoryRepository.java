package MicrofarmaHorarios.Notification.IRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.Notification.Entity.EmailHistory;

@Repository
public interface IEmailHistoryRepository extends JpaRepository<EmailHistory, String> {
    // Custom query methods can be added here if needed
}