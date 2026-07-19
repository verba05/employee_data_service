package verba.employee_data_service.services;

import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import verba.employee_data_service.model.EmployeeRecord;
import verba.employee_data_service.repos.EmployeeRecordRepository;

@Service
public class EmployeeService {

    private final EmployeeRecordRepository employeeRecordRepository;

    public EmployeeService(EmployeeRecordRepository employeeRecordRepository) {
        this.employeeRecordRepository = employeeRecordRepository;
    }

    @Transactional(readOnly = true)
    public EmployeeRecord getEmployeeRecordById(Integer id) {
        return employeeRecordRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Employee not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<EmployeeRecord> getEmployeeRecords(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return employeeRecordRepository.findAll(pageable);
    }

    @Transactional
    public EmployeeRecord createEmployeeRecord(EmployeeRecord entity) {
        entity.setId(null);
        return employeeRecordRepository.save(entity);
    }

    @Transactional
    public EmployeeRecord updateEmployeeRecord(Integer id, EmployeeRecord changes) {
        EmployeeRecord existing = getEmployeeRecordById(id);

        if (changes.getFirstName() != null) existing.setFirstName(changes.getFirstName());
        if (changes.getLastName() != null) existing.setLastName(changes.getLastName());
        if (changes.getDateOfBirth() != null) existing.setDateOfBirth(changes.getDateOfBirth());
        if (changes.getGender() != null) existing.setGender(changes.getGender());
        if (changes.getSocialSecurityNumber() != null) existing.setSocialSecurityNumber(changes.getSocialSecurityNumber());

        return employeeRecordRepository.save(existing);
    }

    @Transactional
    public void deleteEmployeeById(Integer id) {
        if (!employeeRecordRepository.existsById(id)) {
            throw new NoSuchElementException("Employee not found with id: " + id);
        }
        employeeRecordRepository.deleteById(id);
    }
}