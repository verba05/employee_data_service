package verba.employee_data_service.controllers;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import verba.employee_data_service.dtos.EmployeeRecordDto;
import verba.employee_data_service.model.EmployeeRecord;
import verba.employee_data_service.model.Gender;
import verba.employee_data_service.services.EmployeeService;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private static final String SSN_PATTERN = "^(?!666|000|9\\d{2})\\d{3}-(?!00)\\d{2}-(?!0{4})\\d{4}$";

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be zero or a positive integer");
        }
        if (size < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size must be a positive integer");
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
        List<String> errors = validate(entity);
        if (!errors.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.join("; ", errors));
        }
        return employeeService.createEmployeeRecord(entity);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EmployeeRecordDto updateEmployeeRecord(@PathVariable Integer id, @RequestBody EmployeeRecord entity) {
        List<String> errors = validate(entity);
        if (!errors.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.join("; ", errors));
        }

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

    private List<String> validate(EmployeeRecord entity) {
        List<String> errors = new ArrayList<>();

        if (entity == null) {
            errors.add("request body is required");
            return errors;
        }

        if (entity.getFirstName() == null || entity.getFirstName().isBlank()) {
            errors.add("firstName is required");
        } else if (entity.getFirstName().length() > 100) {
            errors.add("firstName must be at most 100 characters");
        }

        if (entity.getLastName() == null || entity.getLastName().isBlank()) {
            errors.add("lastName is required");
        } else if (entity.getLastName().length() > 100) {
            errors.add("lastName must be at most 100 characters");
        }

        if (entity.getDateOfBirth() == null) {
            errors.add("dateOfBirth is required");
        } else if (!entity.getDateOfBirth().isBefore(LocalDate.now())) {
            errors.add("dateOfBirth must be in the past");
        }

        if (entity.getGender() == null) {
            errors.add("gender is required, must be one of " + java.util.Arrays.toString(Gender.values()));
        }

        if (entity.getSocialSecurityNumber() == null || entity.getSocialSecurityNumber().isBlank()) {
            errors.add("socialSecurityNumber is required");
        } else if (!entity.getSocialSecurityNumber().matches(SSN_PATTERN)) {
            errors.add("socialSecurityNumber must be a valid SSN");
        }

        return errors;
    }
}