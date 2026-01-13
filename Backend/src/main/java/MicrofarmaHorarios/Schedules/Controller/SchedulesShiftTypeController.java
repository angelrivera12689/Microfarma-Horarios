package MicrofarmaHorarios.Schedules.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import MicrofarmaHorarios.Schedules.Entity.ShiftType;
import MicrofarmaHorarios.Schedules.IService.ISchedulesShiftTypeService;

@RestController
@RequestMapping("/api/schedules/shifttypes")
public class SchedulesShiftTypeController extends ASchedulesBaseController<ShiftType, ISchedulesShiftTypeService> {

    public SchedulesShiftTypeController(ISchedulesShiftTypeService service) {
        super(service, "ShiftType");
    }

}