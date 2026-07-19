package verba.employee_data_service.controllers;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import verba.employee_data_service.dtos.EmployeeRecordDto;
import verba.employee_data_service.model.EmployeeRecord;
import verba.employee_data_service.services.EmployeeService;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final Validator validator;

    public EmployeeController(EmployeeService employeeService, Validator validator) {
        this.employeeService = employeeService;
        this.validator = validator;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EmployeeRecordDto getEmployeeRecordById(@PathVariable Integer id) {
        try {
            return employeeService.getEmployeeRecordById(id);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found with id: " + id);
        }
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> getEmployeeRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be zero or a positive integer");
        }
        if (size < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page size must be a positive integer");
        }

        Page<EmployeeRecordDto> result = employeeService.getEmployeeRecords(page, size);

        Map<String, Object> body = new HashMap<>();
        body.put("content", result.getContent());
        body.put("page", result.getNumber());
        body.put("size", result.getSize());
        body.put("totalElements", result.getTotalElements());
        body.put("totalPages", result.getTotalPages());
        body.put("last", result.isLast());

        return body;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeRecordDto createEmployeeRecord(@RequestBody EmployeeRecord entity) {
        validate(entity);
        return employeeService.createEmployeeRecord(entity);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EmployeeRecordDto updateEmployeeRecord(@PathVariable Integer id, @RequestBody EmployeeRecord entity) {
        validateNonNullFields(entity);

        try {
            return employeeService.updateEmployeeRecord(id, entity);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found with id: " + id);
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmployeeById(@PathVariable Integer id) {
        try {
            employeeService.deleteEmployeeById(id);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found with id: " + id);
        }
    }

    private void validate(EmployeeRecord entity) {
        if (entity == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }

        Set<ConstraintViolation<EmployeeRecord>> violations = validator.validate(entity);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Given employee record can't be created, because of violated constraints: " + message);
        }
    }

    private void validateNonNullFields(EmployeeRecord entity) {
        if (entity == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
    
        Set<ConstraintViolation<EmployeeRecord>> allViolations = new java.util.HashSet<>();
    
        for (Field field : EmployeeRecord.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(entity);
                if (value != null) {
                    Set<ConstraintViolation<EmployeeRecord>> violations =
                            validator.validateProperty(entity, field.getName());
                    allViolations.addAll(violations);
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Unable to read field: " + field.getName(), e);
            }
        }
    
        if (!allViolations.isEmpty()) {
            String message = allViolations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Given employee record can't be updated, because of violated constraints: " + message);
        }
    }
}