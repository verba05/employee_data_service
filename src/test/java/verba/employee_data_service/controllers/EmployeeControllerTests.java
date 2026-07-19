package verba.employee_data_service.controllers;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import verba.employee_data_service.dtos.EmployeeRecordDto;
import verba.employee_data_service.model.EmployeeRecord;
import verba.employee_data_service.model.Gender;
import verba.employee_data_service.services.EmployeeService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTests {

    @Mock
    private EmployeeService employeeService;

    private EmployeeController controller;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        controller = new EmployeeController(employeeService, validator);
    }

    private EmployeeRecord validEmployee() {
        EmployeeRecord e = new EmployeeRecord();
        e.setFirstName("Ada");
        e.setLastName("Lovelace");
        e.setDateOfBirth(LocalDate.of(1990, 1, 1));
        e.setGender(Gender.FEMALE);
        e.setSocialSecurityNumber("123-45-6789");
        return e;
    }

    @Test
    void getEmployeeRecordById_found_returnsDto() {
        EmployeeRecordDto dto = new EmployeeRecordDto();
        dto.setId(1);
        when(employeeService.getEmployeeRecordById(1)).thenReturn(dto);

        EmployeeRecordDto result = controller.getEmployeeRecordById(1);

        assertEquals(1, result.getId());
    }

    @Test
    void getEmployeeRecordById_notFound_throws404WithCorrectMessage() {
        when(employeeService.getEmployeeRecordById(99))
                .thenThrow(new NoSuchElementException("Employee not found with id: 99"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.getEmployeeRecordById(99));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Employee not found with id: 99", ex.getReason());
    }

    @Test
    void getEmployeeRecords_defaultParams_returnsExpectedMetadata() {
        EmployeeRecordDto dto = new EmployeeRecordDto();
        dto.setId(1);
        when(employeeService.getEmployeeRecords(0, 20))
                .thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1));

        Map<String, Object> body = controller.getEmployeeRecords(0, 20);

        assertEquals(0, body.get("page"));
        assertEquals(20, body.get("size"));
        assertEquals(1L, body.get("totalElements"));
        assertEquals(1, body.get("totalPages"));
        assertEquals(true, body.get("last"));
        assertEquals(1, ((List<?>) body.get("content")).size());
    }

    @Test
    void getEmployeeRecords_validPageAndSize_doesNotThrowAndCallsService() {
        when(employeeService.getEmployeeRecords(2, 10))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(2, 10), 0));

        assertDoesNotThrow(() -> controller.getEmployeeRecords(2, 10));
        verify(employeeService).getEmployeeRecords(2, 10);
    }

    @Test
    void getEmployeeRecords_pageNegativeOne_throws400WithExactMessage() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.getEmployeeRecords(-1, 20));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Page must be zero or a positive integer", ex.getReason());
        verifyNoInteractions(employeeService);
    }

    @Test
    void getEmployeeRecords_largeNegativePage_throws400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.getEmployeeRecords(-1000, 20));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Page must be zero or a positive integer", ex.getReason());
        verifyNoInteractions(employeeService);
    }

    @Test
    void getEmployeeRecords_sizeZero_throws400WithExactMessage() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.getEmployeeRecords(0, 0));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Page size must be a positive integer", ex.getReason());
        verifyNoInteractions(employeeService);
    }

    @Test
    void getEmployeeRecords_sizeNegativeOne_throws400WithExactMessage() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.getEmployeeRecords(0, -1));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Page size must be a positive integer", ex.getReason());
        verifyNoInteractions(employeeService);
    }

    @Test
    void createEmployeeRecord_validData_returnsDtoAndCallsService() {
        EmployeeRecordDto dto = new EmployeeRecordDto();
        dto.setId(1);
        when(employeeService.createEmployeeRecord(any())).thenReturn(dto);

        EmployeeRecordDto result = controller.createEmployeeRecord(validEmployee());

        assertEquals(1, result.getId());
        verify(employeeService).createEmployeeRecord(any());
    }

    @Test
    void createEmployeeRecord_nullBody_throws400AndDoesNotCallService() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createEmployeeRecord(null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Request body is required", ex.getReason());
        verifyNoInteractions(employeeService);
    }

    @Test
    void createEmployeeRecord_missingFirstName_throws400WithCreateMessagePrefix() {
        EmployeeRecord e = validEmployee();
        e.setFirstName(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createEmployeeRecord(e));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().startsWith("Given employee record can't be created, because of violated constraints:"));
        assertTrue(ex.getReason().contains("first name is required"));
        verifyNoInteractions(employeeService);
    }

    @Test
    void createEmployeeRecord_invalidFirstNameFormat_throws400() {
        EmployeeRecord e = validEmployee();
        e.setFirstName("ada");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createEmployeeRecord(e));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("first name must start with a capital letter"));
        verifyNoInteractions(employeeService);
    }

	@Test
	void createEmployeeRecord_firstNameLengthMoreThen100_throws400(){
		EmployeeRecord e = validEmployee();
        e.setFirstName("A".repeat(101));

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createEmployeeRecord(e));

		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("first name must be at most 100 characters"));
        verifyNoInteractions(employeeService);
	}

	@Test
    void createEmployeeRecord_missingLastName_throws400WithCreateMessagePrefix() {
        EmployeeRecord e = validEmployee();
        e.setLastName(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createEmployeeRecord(e));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().startsWith("Given employee record can't be created, because of violated constraints:"));
        assertTrue(ex.getReason().contains("last name is required"));
        verifyNoInteractions(employeeService);
    }

    @Test
    void createEmployeeRecord_invalidLastNameFormat_throws400() {
        EmployeeRecord e = validEmployee();
        e.setLastName("smith");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createEmployeeRecord(e));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("last name must start with a capital letter and contain only letters, spaces, apostrophes, or hyphens"));
        verifyNoInteractions(employeeService);
    }

	@Test
	void createEmployeeRecord_lastNameLengthMoreThen100_throws400(){
		EmployeeRecord e = validEmployee();
        e.setLastName("A".repeat(101));

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createEmployeeRecord(e));

		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("last name must be at most 100 characters"));
        verifyNoInteractions(employeeService);
	}

    @Test
    void createEmployeeRecord_invalidSsn_throws400() {
        EmployeeRecord e = validEmployee();
        e.setSocialSecurityNumber("666-12-3456");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createEmployeeRecord(e));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Social Security Number must be a valid SSN"));
        verifyNoInteractions(employeeService);
    }

	    @Test
    void createEmployeeRecord_emptySsn_throws400() {
        EmployeeRecord e = validEmployee();
        e.setSocialSecurityNumber(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createEmployeeRecord(e));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Social Security Number is required"));
        verifyNoInteractions(employeeService);
    }

    @Test
    void createEmployeeRecord_futureDateOfBirth_throws400() {
        EmployeeRecord e = validEmployee();
        e.setDateOfBirth(LocalDate.now().plusDays(1));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createEmployeeRecord(e));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("date of birth must be in the past"));
        verifyNoInteractions(employeeService);
    }

    @Test
    void createEmployeeRecord_missingGender_throws400() {
        EmployeeRecord e = validEmployee();
        e.setGender(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createEmployeeRecord(e));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("gender is required"));
        verifyNoInteractions(employeeService);
    }

    @Test
    void createEmployeeRecord_completelyEmptyEntity_combinesAllViolationMessages() {
        EmployeeRecord e = new EmployeeRecord();

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createEmployeeRecord(e));

        String reason = ex.getReason();
        assertTrue(reason.contains("first name is required"));
        assertTrue(reason.contains("last name is required"));
        assertTrue(reason.contains("date of birth is required"));
        assertTrue(reason.contains("gender is required"));
        assertTrue(reason.contains("Social Security Number is required"));
        verifyNoInteractions(employeeService);
    }

	@Test
	void updateEmployeeRecord_validData_returnsDtoAndCallsService() {
	    EmployeeRecordDto dto = new EmployeeRecordDto();
	    dto.setId(1);
	    when(employeeService.updateEmployeeRecord(eq(1), any())).thenReturn(dto);

	    EmployeeRecordDto result = controller.updateEmployeeRecord(1, validEmployee());

	    assertEquals(1, result.getId());
	    verify(employeeService).updateEmployeeRecord(eq(1), any());
	}

	@Test
	void updateEmployeeRecord_nullBody_throws400AndDoesNotCallService() {
	    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
	            () -> controller.updateEmployeeRecord(1, null));

	    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
	    assertEquals("Request body is required", ex.getReason());
	    verifyNoInteractions(employeeService);
	}

	@Test
	void updateEmployeeRecord_emptyEntity_isValidAndCallsService() {
	    EmployeeRecordDto dto = new EmployeeRecordDto();
	    dto.setId(1);
	    when(employeeService.updateEmployeeRecord(eq(1), any())).thenReturn(dto);

	    EmployeeRecord empty = new EmployeeRecord();

	    assertDoesNotThrow(() -> controller.updateEmployeeRecord(1, empty));
	    verify(employeeService).updateEmployeeRecord(eq(1), any());
	}

	@Test
	void updateEmployeeRecord_missingFirstName_isValidAndCallsService() {
	    EmployeeRecordDto dto = new EmployeeRecordDto();
	    dto.setId(1);
	    when(employeeService.updateEmployeeRecord(eq(1), any())).thenReturn(dto);

	    EmployeeRecord e = validEmployee();
	    e.setFirstName(null);

	    assertDoesNotThrow(() -> controller.updateEmployeeRecord(1, e));
	    verify(employeeService).updateEmployeeRecord(eq(1), any());
	}

	@Test
	void updateEmployeeRecord_invalidFirstNameFormat_throws400() {
	    EmployeeRecord e = new EmployeeRecord();
	    e.setFirstName("ada");

	    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
	            () -> controller.updateEmployeeRecord(1, e));

	    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
	    assertTrue(ex.getReason().contains("first name must start with a capital letter"));
	    verifyNoInteractions(employeeService);
	}

	@Test
	void updateEmployeeRecord_firstNameLengthMoreThen100_throws400() {
	    EmployeeRecord e = new EmployeeRecord();
	    e.setFirstName("A".repeat(101));

	    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
	            () -> controller.updateEmployeeRecord(1, e));

	    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
	    assertTrue(ex.getReason().contains("first name must be at most 100 characters"));
	    verifyNoInteractions(employeeService);
	}

	@Test
	void updateEmployeeRecord_missingLastName_isValidAndCallsService() {
	    EmployeeRecordDto dto = new EmployeeRecordDto();
	    dto.setId(1);
	    when(employeeService.updateEmployeeRecord(eq(1), any())).thenReturn(dto);

	    EmployeeRecord e = validEmployee();
	    e.setLastName(null);

	    assertDoesNotThrow(() -> controller.updateEmployeeRecord(1, e));
	    verify(employeeService).updateEmployeeRecord(eq(1), any());
	}

	@Test
	void updateEmployeeRecord_invalidLastNameFormat_throws400() {
	    EmployeeRecord e = new EmployeeRecord();
	    e.setLastName("smith");

	    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
	            () -> controller.updateEmployeeRecord(1, e));

	    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
	    assertTrue(ex.getReason().contains(
	            "last name must start with a capital letter and contain only letters, spaces, apostrophes, or hyphens"));
	    verifyNoInteractions(employeeService);
	}

	@Test
	void updateEmployeeRecord_lastNameLengthMoreThen100_throws400() {
	    EmployeeRecord e = new EmployeeRecord();
	    e.setLastName("A".repeat(101));

	    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
	            () -> controller.updateEmployeeRecord(1, e));

	    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
	    assertTrue(ex.getReason().contains("last name must be at most 100 characters"));
	    verifyNoInteractions(employeeService);
	}

	@Test
	void updateEmployeeRecord_missingSsn_isValidAndCallsService() {
	    EmployeeRecordDto dto = new EmployeeRecordDto();
	    dto.setId(1);
	    when(employeeService.updateEmployeeRecord(eq(1), any())).thenReturn(dto);

	    EmployeeRecord e = validEmployee();
	    e.setSocialSecurityNumber(null);

	    assertDoesNotThrow(() -> controller.updateEmployeeRecord(1, e));
	    verify(employeeService).updateEmployeeRecord(eq(1), any());
	}

	@Test
	void updateEmployeeRecord_invalidSsn_throws400() {
	    EmployeeRecord e = new EmployeeRecord();
	    e.setSocialSecurityNumber("666-12-3456");

	    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
	            () -> controller.updateEmployeeRecord(1, e));

	    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
	    assertTrue(ex.getReason().contains("Social Security Number must be a valid SSN"));
	    verifyNoInteractions(employeeService);
	}

	@Test
	void updateEmployeeRecord_futureDateOfBirth_throws400() {
	    EmployeeRecord e = new EmployeeRecord();
	    e.setDateOfBirth(LocalDate.now().plusDays(1));

	    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
	            () -> controller.updateEmployeeRecord(1, e));

	    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
	    assertTrue(ex.getReason().contains("date of birth must be in the past"));
	    verifyNoInteractions(employeeService);
	}

	@Test
	void updateEmployeeRecord_missingGender_isValidAndCallsService() {
	    EmployeeRecordDto dto = new EmployeeRecordDto();
	    dto.setId(1);
	    when(employeeService.updateEmployeeRecord(eq(1), any())).thenReturn(dto);

	    EmployeeRecord e = validEmployee();
	    e.setGender(null);

	    assertDoesNotThrow(() -> controller.updateEmployeeRecord(1, e));
	    verify(employeeService).updateEmployeeRecord(eq(1), any());
	}

	@Test
	void updateEmployeeRecord_onlyFirstNameProvided_valid_callsService() {
	    EmployeeRecordDto dto = new EmployeeRecordDto();
	    dto.setId(1);
	    when(employeeService.updateEmployeeRecord(eq(1), any())).thenReturn(dto);

	    EmployeeRecord e = new EmployeeRecord();
	    e.setFirstName("Ada");

	    assertDoesNotThrow(() -> controller.updateEmployeeRecord(1, e));
	    verify(employeeService).updateEmployeeRecord(eq(1), any());
	}

	@Test
	void updateEmployeeRecord_onlyLastNameProvided_valid_callsService() {
	    EmployeeRecordDto dto = new EmployeeRecordDto();
	    dto.setId(1);
	    when(employeeService.updateEmployeeRecord(eq(1), any())).thenReturn(dto);

	    EmployeeRecord e = new EmployeeRecord();
	    e.setLastName("Lovelace");

	    assertDoesNotThrow(() -> controller.updateEmployeeRecord(1, e));
	    verify(employeeService).updateEmployeeRecord(eq(1), any());
	}

	@Test
	void updateEmployeeRecord_notFound_throws404() {
	    when(employeeService.updateEmployeeRecord(eq(99), any()))
	            .thenThrow(new NoSuchElementException("Employee not found with id: 99"));

	    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
	            () -> controller.updateEmployeeRecord(99, validEmployee()));

	    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
	    assertEquals("Employee not found with id: 99", ex.getReason());
	}

    @Test
    void deleteEmployeeById_found_completesWithoutException() {
        doNothing().when(employeeService).deleteEmployeeById(1);

        assertDoesNotThrow(() -> controller.deleteEmployeeById(1));
        verify(employeeService).deleteEmployeeById(1);
    }

    @Test
    void deleteEmployeeById_notFound_throws404() {
        doThrow(new NoSuchElementException("Employee not found with id: 5"))
                .when(employeeService).deleteEmployeeById(5);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.deleteEmployeeById(5));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Employee not found with id: 5", ex.getReason());
    }
}