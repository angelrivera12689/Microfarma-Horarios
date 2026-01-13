package MicrofarmaHorarios.News.Service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import MicrofarmaHorarios.News.Entity.News;
import MicrofarmaHorarios.News.IRepository.INewsBaseRepository;
import MicrofarmaHorarios.News.IRepository.INewsNewsRepository;
import MicrofarmaHorarios.News.IService.INewsNewsService;

@Service
public class NewsNewsService extends ANewsBaseService<News> implements INewsNewsService {

    @Autowired
    private INewsNewsRepository newsRepository;

    @Override
    protected INewsBaseRepository<News, String> getRepository() {
        return newsRepository;
    }

    @Override
    public List<News> findByNewsTypeId(String newsTypeId) throws Exception {
        return newsRepository.findByNewsTypeId(newsTypeId);
    }

    @Override
    public List<News> findByEmployeeId(String employeeId) throws Exception {
        return newsRepository.findByEmployeeId(employeeId);
    }

    @Override
    public List<News> findByPublicationDateBetween(LocalDate startDate, LocalDate endDate) throws Exception {
        return newsRepository.findByPublicationDateBetween(startDate, endDate);
    }

}