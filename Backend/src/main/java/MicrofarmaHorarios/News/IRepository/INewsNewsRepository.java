package MicrofarmaHorarios.News.IRepository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.News.Entity.News;

@Repository
public interface INewsNewsRepository extends INewsBaseRepository<News, String> {

    List<News> findByNewsTypeId(String newsTypeId);

    List<News> findByEmployeeId(String employeeId);

    List<News> findByPublicationDateBetween(LocalDate startDate, LocalDate endDate);

}