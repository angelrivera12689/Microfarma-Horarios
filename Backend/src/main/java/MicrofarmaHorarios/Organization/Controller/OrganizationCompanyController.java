package MicrofarmaHorarios.Organization.Controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import MicrofarmaHorarios.Organization.Entity.Company;
import MicrofarmaHorarios.Organization.IService.IOrganizationCompanyService;
import MicrofarmaHorarios.Security.DTO.Response.ApiResponseDto;

@RestController
@RequestMapping("/api/organization/companies")
public class OrganizationCompanyController extends AOrganizationBaseController<Company, IOrganizationCompanyService> {

    @Autowired
    private IOrganizationCompanyService companyService;

    public OrganizationCompanyController(IOrganizationCompanyService service) {
        super(service, "Company");
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponseDto<Optional<Company>>> findByName(@PathVariable String name) {
        try {
            Optional<Company> company = companyService.findByName(name);
            return ResponseEntity.ok(new ApiResponseDto<Optional<Company>>("Compañía encontrada", company, true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<Optional<Company>>(e.getMessage(), null, false));
        }
    }
}