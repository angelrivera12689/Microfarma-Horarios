package MicrofarmaHorarios.News.IService;

import java.time.LocalDate;
import java.util.List;

import MicrofarmaHorarios.News.Entity.News;

public interface INewsNewsService extends INewsBaseService<News> {

    List<News> findByNewsTypeId(String newsTypeId) throws Exception;

    List<News> findByEmployeeId(String employeeId) throws Exception;

    List<News> findByPublicationDateBetween(LocalDate startDate, LocalDate endDate) throws Exception;

}