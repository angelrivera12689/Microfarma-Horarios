package MicrofarmaHorarios.Schedules.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import MicrofarmaHorarios.Schedules.Entity.Shift;
import MicrofarmaHorarios.Schedules.Entity.ShiftType;
import MicrofarmaHorarios.Schedules.IRepository.ISchedulesShiftRepository;
import MicrofarmaHorarios.Schedules.DTO.Response.ReportResponseDto;

class SchedulesReportServiceTest {

    @Mock
    private ISchedulesShiftRepository shiftRepository;

    @InjectMocks
    private SchedulesReportService reportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateReport() throws Exception {
        // Arrange
        int month = 1;
        int year = 2024;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        ShiftType shiftType = new ShiftType();
        shiftType.setStartTime(LocalTime.of(8, 0));
        shiftType.setEndTime(LocalTime.of(16, 0));

        Shift shift = new Shift();
        shift.setDate(LocalDate.of(2024, 1, 15));
        shift.setShiftType(shiftType);
        shift.setStatus(true);

        List<Shift> shifts = Arrays.asList(shift);
        when(shiftRepository.findByDateBetween(startDate, endDate)).thenReturn(shifts);

        // Act
        ReportResponseDto report = reportService.generateReport(month, year);

        // Assert
        assertNotNull(report);
        assertNotNull(report.getGlobal());
        assertEquals(0, report.getEmployees().size()); // No employee set
    }
}