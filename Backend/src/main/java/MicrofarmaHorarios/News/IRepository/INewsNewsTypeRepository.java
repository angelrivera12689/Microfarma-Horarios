package MicrofarmaHorarios.News.IRepository;

import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.News.Entity.NewsType;

@Repository
public interface INewsNewsTypeRepository extends INewsBaseRepository<NewsType, String> {

}