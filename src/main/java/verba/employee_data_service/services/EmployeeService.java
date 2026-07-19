package verba.employee_data_service.services;

import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import verba.employee_data_service.dtos.EmployeeRecordDto;
import verba.employee_data_service.model.EmployeeRecord;
import verba.employee_data_service.repos.EmployeeRecordRepository;

@Service
public class EmployeeService {

    private final EmployeeRecordRepository employeeRecordRepository;

    public EmployeeService(EmployeeRecordRepository employeeRecordRepository) {
        this.employeeRecordRepository = employeeRecordRepository;
    }

    public EmployeeRecordDto getEmployeeRecordById(Integer id) {
        EmployeeRecord record = employeeRecordRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Employee not found with id: " + id));
        return EmployeeRecordDto.fromRecord(record);
    }

    public Page<EmployeeRecordDto> getEmployeeRecords(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return employeeRecordRepository.findAll(pageable).map(EmployeeRecordDto::fromRecord);
    }

    public EmployeeRecordDto createEmployeeRecord(EmployeeRecord entity) {
        entity.setId(null);
        EmployeeRecord saved = employeeRecordRepository.save(entity);
        return EmployeeRecordDto.fromRecord(saved);
    }

    public EmployeeRecordDto updateEmployeeRecord(Integer id, EmployeeRecord changes) {
        EmployeeRecord existing = employeeRecordRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Employee not found with id: " + id));

        if (changes.getFirstName() != null) existing.setFirstName(changes.getFirstName());
        if (changes.getLastName() != null) existing.setLastName(changes.getLastName());
        if (changes.getDateOfBirth() != null) existing.setDateOfBirth(changes.getDateOfBirth());
        if (changes.getGender() != null) existing.setGender(changes.getGender());
        if (changes.getSocialSecurityNumber() != null) existing.setSocialSecurityNumber(changes.getSocialSecurityNumber());

        EmployeeRecord updated = employeeRecordRepository.save(existing);
        return EmployeeRecordDto.fromRecord(updated);
    }

    public void deleteEmployeeById(Integer id) {
        if (!employeeRecordRepository.existsById(id)) {
            throw new NoSuchElementException("Employee not found with id: " + id);
        }
        employeeRecordRepository.deleteById(id);
    }
}