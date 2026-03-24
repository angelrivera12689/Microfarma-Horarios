package MicrofarmaHorarios.News.IRepository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import MicrofarmaHorarios.News.Entity.NewsType;

@Repository
public interface INewsNewsTypeRepository extends INewsBaseRepository<NewsType, String> {

    Optional<NewsType> findByNameIgnoreCase(String name);

}