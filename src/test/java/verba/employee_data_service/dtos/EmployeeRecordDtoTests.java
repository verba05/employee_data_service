package verba.employee_data_service.dtos;

import org.junit.jupiter.api.Test;

import verba.employee_data_service.model.EmployeeRecord;
import verba.employee_data_service.model.Gender;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeRecordDtoTests {

    @Test
    void fromRecord_copiesAllFieldsCorrectly() {
        EmployeeRecord record = new EmployeeRecord();
        record.setId(1);
        record.setFirstName("Ada");
        record.setLastName("Lovelace");
        record.setDateOfBirth(LocalDate.of(1990, 1, 1));
        record.setGender(Gender.FEMALE);
        record.setSocialSecurityNumber("123-45-6789");

        EmployeeRecordDto dto = EmployeeRecordDto.fromRecord(record);

        assertEquals(record.getId(), dto.getId());
        assertEquals(record.getFirstName(), dto.getFirstName());
        assertEquals(record.getLastName(), dto.getLastName());
        assertEquals(record.getDateOfBirth(), dto.getDateOfBirth());
        assertEquals(record.getGender(), dto.getGender());
    }

    @Test
    void fromRecord_doesNotExposeSocialSecurityNumber() {
        EmployeeRecord record = new EmployeeRecord();
        record.setId(1);
        record.setFirstName("Ada");
        record.setLastName("Lovelace");
        record.setDateOfBirth(LocalDate.of(1990, 1, 1));
        record.setGender(Gender.FEMALE);
        record.setSocialSecurityNumber("123-45-6789");

        EmployeeRecordDto dto = EmployeeRecordDto.fromRecord(record);

        boolean hasSsnField = java.util.Arrays.stream(dto.getClass().getDeclaredFields())
                .anyMatch(f -> f.getName().toLowerCase().contains("social")
                        || f.getName().toLowerCase().contains("ssn"));

        assertFalse(hasSsnField, "EmployeeRecordDto should never expose the SSN field");
    }
}