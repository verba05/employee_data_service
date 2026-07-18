package repos;

import model.EmployeeRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface EmployeeRecordRepository extends PagingAndSortingRepository<EmployeeRecord, Integer>{

}
