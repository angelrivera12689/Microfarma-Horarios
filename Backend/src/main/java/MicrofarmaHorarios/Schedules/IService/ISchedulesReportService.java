package MicrofarmaHorarios.Schedules.IService;

import MicrofarmaHorarios.Schedules.DTO.Response.ReportResponseDto;

public interface ISchedulesReportService {
    ReportResponseDto generateReport(int month, int year) throws Exception;
}