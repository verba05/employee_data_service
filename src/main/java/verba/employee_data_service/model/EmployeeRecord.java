package verba.employee_data_service.model;

import java.time.LocalDate;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import verba.employee_data_service.converters.EncryptionConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@Entity
@Table(name = "employees", schema = "public")
public class EmployeeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "first name is required")
    @Size(max = 100, message = "first name must be at most 100 characters")
    @Pattern(
    regexp = "^[A-Z][a-zA-Z'-]*(?:[ ][A-Z][a-zA-Z'-]*)*$",
    message = "first name must start with a capital letter and contain only letters, spaces, apostrophes, or hyphens"
    )
    @Column(name = "firstname", nullable = false)
    private String firstName;

    @NotBlank(message = "last name is required")
    @Size(max = 100, message = "last name must be at most 100 characters")
    @Pattern(
    regexp = "^[A-Z][a-zA-Z'-]*(?:[ ][A-Z][a-zA-Z'-]*)*$",
    message = "last name must start with a capital letter and contain only letters, spaces, apostrophes, or hyphens"
    )
    @Column(name = "lastname", nullable = false)
    private String lastName;

    @NotNull(message = "date of birth is required")
    @Past(message = "date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "dateofbirth", nullable = false)
    private LocalDate dateOfBirth;

    @NotNull(message = "gender is required")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "gender", columnDefinition = "gender", nullable = false)
    private Gender gender;

    @NotBlank(message = "Social Security Number is required")
    @Pattern(
        regexp = "^(?!666|000|9\\d{2})\\d{3}-(?!00)\\d{2}-(?!0{4})\\d{4}$",
        message = "Social Security Number must be a valid SSN"
    )
    @Convert(converter = EncryptionConverter.class)
    @Column(name = "encryptedssn", nullable = false)
    private String socialSecurityNumber;
}
