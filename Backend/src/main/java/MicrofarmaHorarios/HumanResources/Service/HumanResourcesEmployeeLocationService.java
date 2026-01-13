package MicrofarmaHorarios.HumanResources.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import MicrofarmaHorarios.HumanResources.Entity.EmployeeLocation;
import MicrofarmaHorarios.HumanResources.IRepository.IHumanResourcesBaseRepository;
import MicrofarmaHorarios.HumanResources.IRepository.IHumanResourcesEmployeeLocationRepository;
import MicrofarmaHorarios.HumanResources.IService.IHumanResourcesEmployeeLocationService;

@Service
public class HumanResourcesEmployeeLocationService extends AHumanResourcesBaseService<EmployeeLocation> implements IHumanResourcesEmployeeLocationService {

    @Autowired
    private IHumanResourcesEmployeeLocationRepository employeeLocationRepository;

    @Override
    protected IHumanResourcesBaseRepository<EmployeeLocation, String> getRepository() {
        return employeeLocationRepository;
    }

    @Override
    public List<EmployeeLocation> findByEmployeeId(String employeeId) throws Exception {
        return employeeLocationRepository.findByEmployeeId(employeeId);
    }

    @Override
    public List<EmployeeLocation> findByLocationId(String locationId) throws Exception {
        return employeeLocationRepository.findByLocationId(locationId);
    }

}