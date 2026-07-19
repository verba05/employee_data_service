package verba.employee_data_service.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import verba.employee_data_service.dtos.EmployeeRecordDto;
import verba.employee_data_service.model.EmployeeRecord;
import verba.employee_data_service.model.Gender;
import verba.employee_data_service.repos.EmployeeRecordRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTests{

    @Mock
    private EmployeeRecordRepository employeeRecordRepository;

    private EmployeeService employeeService;

    private EmployeeRecord existingRecord() {
        EmployeeRecord e = new EmployeeRecord();
        e.setId(1);
        e.setFirstName("Ada");
        e.setLastName("Lovelace");
        e.setDateOfBirth(LocalDate.of(1990, 1, 1));
        e.setGender(Gender.FEMALE);
        e.setSocialSecurityNumber("123-45-6789");
        return e;
    }

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(employeeRecordRepository);
    }

    @Test
    void getEmployeeRecordById_found_returnsDto() {
        when(employeeRecordRepository.findById(1)).thenReturn(Optional.of(existingRecord()));

        EmployeeRecordDto dto = employeeService.getEmployeeRecordById(1);

        assertNotNull(dto);
        assertEquals(1, dto.getId());
    }

    @Test
    void getEmployeeRecordById_notFound_throwsNoSuchElementException() {
        when(employeeRecordRepository.findById(99)).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> employeeService.getEmployeeRecordById(99));

        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    void getEmployeeRecords_noRecords_returnsEmptyPage() {
        when(employeeRecordRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        Page<EmployeeRecordDto> result = employeeService.getEmployeeRecords(0, 20);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getEmployeeRecords_withRecords_returnsPageOfDtos() {
        EmployeeRecord first = existingRecord();

        EmployeeRecord second = new EmployeeRecord();
        second.setId(2);
        second.setFirstName("Alan");
        second.setLastName("Turing");
        second.setDateOfBirth(LocalDate.of(1912, 6, 23));
        second.setGender(Gender.MALE);
        second.setSocialSecurityNumber("987-65-4321");

        when(employeeRecordRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(first, second), PageRequest.of(0, 20), 2));

        Page<EmployeeRecordDto> result = employeeService.getEmployeeRecords(0, 20);

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void createEmployeeRecord_setsIdToNullBeforeSaving() {
        EmployeeRecord incoming = existingRecord();
        incoming.setId(999);

        when(employeeRecordRepository.save(any())).thenReturn(existingRecord());

        ArgumentCaptor<EmployeeRecord> captor = ArgumentCaptor.forClass(EmployeeRecord.class);

        EmployeeRecordDto result = employeeService.createEmployeeRecord(incoming);

        verify(employeeRecordRepository).save(captor.capture());
        assertNull(captor.getValue().getId(), "id must be null when passed to repository.save");
        assertNotNull(result);
    }


    @Test
    void updateEmployeeRecord_notFound_throwsNoSuchElementException() {
        EmployeeRecord changes = new EmployeeRecord();
        changes.setLastName("Byron");

        when(employeeRecordRepository.findById(99)).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> employeeService.updateEmployeeRecord(99, changes));

        assertTrue(ex.getMessage().contains("99"));
        verify(employeeRecordRepository, never()).save(any());
    }

    @Test
    void updateEmployeeRecord_partialUpdate_onlyOverwritesProvidedFields() {
        EmployeeRecord existing = existingRecord();

        EmployeeRecord changes = new EmployeeRecord();
        changes.setLastName("Byron");

        when(employeeRecordRepository.findById(1)).thenReturn(Optional.of(existing));
        when(employeeRecordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<EmployeeRecord> captor = ArgumentCaptor.forClass(EmployeeRecord.class);

        EmployeeRecordDto result = employeeService.updateEmployeeRecord(1, changes);

        verify(employeeRecordRepository).save(captor.capture());
        EmployeeRecord saved = captor.getValue();

        assertEquals("Ada", saved.getFirstName());
        assertEquals(LocalDate.of(1990, 1, 1), saved.getDateOfBirth());
        assertEquals(Gender.FEMALE, saved.getGender());
        assertEquals("123-45-6789", saved.getSocialSecurityNumber());

        assertEquals("Byron", saved.getLastName());
        assertNotNull(result);
    }

    @Test
    void updateEmployeeRecord_validData_returnsDto() {
        EmployeeRecord existing = existingRecord();

        EmployeeRecord changes = new EmployeeRecord();
        changes.setFirstName("Grace");

        when(employeeRecordRepository.findById(1)).thenReturn(Optional.of(existing));
        when(employeeRecordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeRecordDto result = employeeService.updateEmployeeRecord(1, changes);

        assertNotNull(result);
    }

    @Test
    void deleteEmployeeById_exists_callsRepositoryDelete() {
        when(employeeRecordRepository.existsById(1)).thenReturn(true);

        employeeService.deleteEmployeeById(1);

        verify(employeeRecordRepository).deleteById(1);
    }

    @Test
    void deleteEmployeeById_notFound_throwsNoSuchElementExceptionAndDoesNotCallDelete() {
        when(employeeRecordRepository.existsById(99)).thenReturn(false);

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> employeeService.deleteEmployeeById(99));

        assertTrue(ex.getMessage().contains("99"));
        verify(employeeRecordRepository, never()).deleteById(any());
    }
}