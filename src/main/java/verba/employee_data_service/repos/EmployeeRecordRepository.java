package verba.employee_data_service.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import verba.employee_data_service.model.EmployeeRecord;

public interface EmployeeRecordRepository extends JpaRepository<EmployeeRecord, Integer>{

}
