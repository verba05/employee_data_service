package model;

import java.time.LocalDate;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import converters.EncryptionConverter;
import lombok.Getter;
import lombok.Setter;
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

    @Column(name = "firstName", nullable = false)
    private String firstName;
    
    @Column(name = "lastName", nullable = false)
    private String lastName;

    @Column(name = "dateOfBirth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Convert(converter = EncryptionConverter.class)
    @Column(name = "encryptedSSN", nullable = false)
    private String socialSecurityNumber; 
}
