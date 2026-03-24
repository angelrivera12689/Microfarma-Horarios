package MicrofarmaHorarios.News.IService;

import java.util.Optional;

import MicrofarmaHorarios.News.Entity.NewsType;

public interface INewsNewsTypeService extends INewsBaseService<NewsType> {

    Optional<NewsType> findByNameIgnoreCase(String name) throws Exception;

}