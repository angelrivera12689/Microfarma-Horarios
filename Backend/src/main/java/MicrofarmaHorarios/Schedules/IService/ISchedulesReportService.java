package MicrofarmaHorarios.Schedules.IService;

import MicrofarmaHorarios.Schedules.DTO.Response.ReportResponseDto;

public interface ISchedulesReportService {
    ReportResponseDto generateReport(int month, int year) throws Exception;
    ReportResponseDto generateReportByLocation(int month, int year, String locationId) throws Exception;
}