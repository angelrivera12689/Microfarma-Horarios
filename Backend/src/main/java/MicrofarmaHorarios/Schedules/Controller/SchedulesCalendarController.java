package MicrofarmaHorarios.Schedules.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import MicrofarmaHorarios.Schedules.Entity.Calendar;
import MicrofarmaHorarios.Schedules.IService.ISchedulesCalendarService;

@RestController
@RequestMapping("/api/schedules/calendars")
public class SchedulesCalendarController extends ASchedulesBaseController<Calendar, ISchedulesCalendarService> {

    public SchedulesCalendarController(ISchedulesCalendarService service) {
        super(service, "Calendar");
    }

}