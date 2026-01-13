package MicrofarmaHorarios.Notification.IService;

import java.time.LocalDateTime;
import java.util.List;

import MicrofarmaHorarios.Notification.Entity.DeliveryStatus;
import MicrofarmaHorarios.Notification.Entity.EmailHistory;
import MicrofarmaHorarios.Security.IService.ISecurityBaseService;

public interface IEmailHistoryService extends ISecurityBaseService<EmailHistory> {

    List<EmailHistory> findByFilters(String recipient, String emailType, DeliveryStatus deliveryStatus,
                                     LocalDateTime sentAtFrom, LocalDateTime sentAtTo) throws Exception;

    void updateDeliveryStatus(String id, DeliveryStatus deliveryStatus, String errorMessage) throws Exception;
}