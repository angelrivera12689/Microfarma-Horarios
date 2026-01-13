package MicrofarmaHorarios.HumanResources.Entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import MicrofarmaHorarios.Security.Entity.User;

@Entity
@Table(name = "employee")
@Data
public class Employee extends AHumanResourcesBaseEntity {
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "address", length = 255)
    private String address;

    @ManyToOne
    @JoinColumn(name = "position_id", nullable = true)
    private Position position;

    @ManyToOne
    @JoinColumn(name = "contract_type_id", nullable = true)
    private ContractType contractType;

    @OneToOne(mappedBy = "employee")
    @JsonIgnore
    private User user;
}