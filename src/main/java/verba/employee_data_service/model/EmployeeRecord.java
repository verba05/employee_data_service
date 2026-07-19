package verba.employee_data_service.model;

import java.time.LocalDate;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.Getter;
import lombok.Setter;
import verba.employee_data_service.converters.EncryptionConverter;
import jakarta.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "employees", schema="public")
public class EmployeeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Integer id;

    @Column(name = "firstname", nullable = false)
    private String firstName;
    
    @Column(name = "lastname", nullable = false)
    private String lastName;

    @Column(name = "dateofbirth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "gender", columnDefinition = "gender", nullable = false)
    private Gender gender;

    @Convert(converter = EncryptionConverter.class)
    @Column(name = "encryptedssn", nullable = false)
    private String socialSecurityNumber; 
}
