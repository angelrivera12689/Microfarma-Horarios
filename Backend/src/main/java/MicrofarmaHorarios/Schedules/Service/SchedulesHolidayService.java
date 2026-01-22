package MicrofarmaHorarios.Schedules.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class SchedulesHolidayService {

    public boolean isHoliday(LocalDate date) {
        return getHolidaysForYear(date.getYear()).contains(date);
    }

    public Set<LocalDate> getHolidaysForYear(int year) {
        Set<LocalDate> holidays = new HashSet<>();

        // Festivos fijos
        holidays.add(LocalDate.of(year, Month.JANUARY, 1)); // Año Nuevo
        holidays.add(LocalDate.of(year, Month.JANUARY, 6)); // Día de los Reyes Magos
        holidays.add(LocalDate.of(year, Month.JANUARY, 12)); // Día de los Reyes Magos (variante regional)
        holidays.add(LocalDate.of(year, Month.MARCH, 19)); // Día de San José
        holidays.add(LocalDate.of(year, Month.MAY, 1)); // Día del Trabajo
        holidays.add(LocalDate.of(year, Month.JUNE, 29)); // San Pedro y San Pablo (solo algunas regiones)
        holidays.add(LocalDate.of(year, Month.JULY, 20)); // Independencia
        holidays.add(LocalDate.of(year, Month.AUGUST, 7)); // Batalla de Boyacá
        holidays.add(LocalDate.of(year, Month.AUGUST, 15)); // Asunción (solo algunas regiones)
        holidays.add(LocalDate.of(year, Month.OCTOBER, 12)); // Día de la Raza
        holidays.add(LocalDate.of(year, Month.NOVEMBER, 1)); // Todos los Santos
        holidays.add(LocalDate.of(year, Month.NOVEMBER, 11)); // Independencia de Cartagena
        holidays.add(LocalDate.of(year, Month.DECEMBER, 8)); // Inmaculada Concepción
        holidays.add(LocalDate.of(year, Month.DECEMBER, 25)); // Navidad

        // Festivos basados en Pascua
        LocalDate easter = calculateEaster(year);
        holidays.add(easter.minusDays(3)); // Jueves Santo
        holidays.add(easter.minusDays(2)); // Viernes Santo
        holidays.add(easter.plusDays(43)); // Ascensión (40 días después de Pascua)
        holidays.add(easter.plusDays(64)); // Corpus Christi (60 días después de Pascua)

        return holidays;
    }

    /**
     * Calcula la fecha de Pascua usando el algoritmo de Meeus/Jones/Butcher
     */
    private LocalDate calculateEaster(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;

        return LocalDate.of(year, month, day);
    }
}