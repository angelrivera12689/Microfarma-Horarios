package MicrofarmaHorarios.News.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import MicrofarmaHorarios.News.Entity.NewsType;
import MicrofarmaHorarios.News.IService.INewsNewsTypeService;

@RestController
@RequestMapping("/api/news/newstypes")
public class NewsNewsTypeController extends ANewsBaseController<NewsType, INewsNewsTypeService> {

    public NewsNewsTypeController(INewsNewsTypeService service) {
        super(service, "NewsType");
    }

}