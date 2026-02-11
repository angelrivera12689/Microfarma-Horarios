package MicrofarmaHorarios.Schedules.IService;

import java.time.LocalDate;
import java.util.List;

import MicrofarmaHorarios.Schedules.DTO.Response.EmployeeReportDto;
import MicrofarmaHorarios.Schedules.DTO.Response.GlobalReportDto;
import MicrofarmaHorarios.Schedules.DTO.Response.LocationReportDto;
import MicrofarmaHorarios.Schedules.DTO.Response.ReportFiltersDto;
import MicrofarmaHorarios.Schedules.DTO.Response.ReportResponseDto;

public interface ISchedulesReportService {
    ReportResponseDto generateReport(int month, int year) throws Exception;
    ReportResponseDto generateReportByLocation(int month, int year, String locationId) throws Exception;
    ReportResponseDto generateReportByEmployee(int month, int year, String employeeId) throws Exception;
    ReportFiltersDto getAvailableFilters() throws Exception;
    List<LocationReportDto> generateLocationReport(int month, int year, String locationId) throws Exception;
    GlobalReportDto generateGlobalReport(int month, int year) throws Exception;
    EmployeeReportDto generateEmployeeIndividualReport(int month, int year, String employeeId) throws Exception;
}