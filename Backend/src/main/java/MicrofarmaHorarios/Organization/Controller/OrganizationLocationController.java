package MicrofarmaHorarios.Organization.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import MicrofarmaHorarios.Organization.Entity.Location;
import MicrofarmaHorarios.Organization.IService.IOrganizationLocationService;
import MicrofarmaHorarios.Security.DTO.Response.ApiResponseDto;

@RestController
@RequestMapping("/api/organization/locations")
public class OrganizationLocationController extends AOrganizationBaseController<Location, IOrganizationLocationService> {

    public OrganizationLocationController(IOrganizationLocationService service) {
        super(service, "Location");
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponseDto<List<Location>>> findByCompanyId(@PathVariable String companyId) {
        try {
            List<Location> locations = service.findByCompanyId(companyId);
            return ResponseEntity.ok(new ApiResponseDto<List<Location>>("Ubicaciones encontradas", locations, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<List<Location>>(e.getMessage(), null, false));
        }
    }
}