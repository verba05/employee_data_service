package verba.employee_data_service.dtos;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import verba.employee_data_service.model.EmployeeRecord;
import verba.employee_data_service.model.Gender;

@Getter
@Setter
public class EmployeeRecordDto {
    private Integer id;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Gender gender;

    public static EmployeeRecordDto fromRecord(EmployeeRecord employeeRecord){
        EmployeeRecordDto dto = new EmployeeRecordDto();
        dto.setId(employeeRecord.getId());
        dto.setFirstName(employeeRecord.getFirstName());
        dto.setLastName(employeeRecord.getLastName());
        dto.setDateOfBirth(employeeRecord.getDateOfBirth());
        dto.setGender(employeeRecord.getGender());

        return dto;
    }
}
