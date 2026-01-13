package MicrofarmaHorarios.News.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import MicrofarmaHorarios.News.Entity.NewsType;
import MicrofarmaHorarios.News.IRepository.INewsBaseRepository;
import MicrofarmaHorarios.News.IRepository.INewsNewsTypeRepository;
import MicrofarmaHorarios.News.IService.INewsNewsTypeService;

@Service
public class NewsNewsTypeService extends ANewsBaseService<NewsType> implements INewsNewsTypeService {

    @Autowired
    private INewsNewsTypeRepository newsTypeRepository;

    @Override
    protected INewsBaseRepository<NewsType, String> getRepository() {
        return newsTypeRepository;
    }

}