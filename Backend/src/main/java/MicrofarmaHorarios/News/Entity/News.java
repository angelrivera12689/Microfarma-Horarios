package MicrofarmaHorarios.News.Entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import MicrofarmaHorarios.HumanResources.Entity.Employee;

@Entity
@Table(name = "news")
@Data
public class News extends ANewsBaseEntity {
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotBlank(message = "El contenido es obligatorio")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @NotNull(message = "La fecha de publicación es obligatoria")
    @Column(name = "publication_date", nullable = false)
    private LocalDate publicationDate;

    @NotNull(message = "El tipo de noticia es obligatorio")
    @ManyToOne
    @JoinColumn(name = "news_type_id", nullable = false)
    private NewsType newsType;

    @NotNull(message = "El empleado es obligatorio")
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}