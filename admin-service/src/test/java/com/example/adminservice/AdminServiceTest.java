package com.example.adminservice;

import com.example.adminservice.entity.Admin;
import com.example.adminservice.entity.Designation;
import com.example.adminservice.repository.AdminRepository;
import com.example.adminservice.repository.DesignationRepository;
import com.example.adminservice.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private DesignationRepository designationRepository;

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ==================================================
    // 1. ADMIN LOGIN VALIDATION TESTS
    // ==================================================

    @Test
    public void testValidateLogin_ValidCredentials() {
        Admin admin = new Admin(1L, "admin@company.com", "admin123");
        when(adminRepository.findByEmailAndPassword("admin@company.com", "admin123")).thenReturn(Optional.of(admin));

        boolean isValid = adminService.validateLogin("admin@company.com", "admin123");

        assertTrue(isValid);
        verify(adminRepository, times(1)).findByEmailAndPassword("admin@company.com", "admin123");
    }

    @Test
    public void testValidateLogin_InvalidCredentials() {
        when(adminRepository.findByEmailAndPassword("admin@company.com", "wrongpass")).thenReturn(Optional.empty());

        boolean isValid = adminService.validateLogin("admin@company.com", "wrongpass");

        assertFalse(isValid);
        verify(adminRepository, times(1)).findByEmailAndPassword("admin@company.com", "wrongpass");
    }

    @Test
    public void shouldThrowExceptionWhenLoginEmailNull() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> adminService.validateLogin(null, "admin123"));
        assertTrue(exception.getMessage().contains("Only company email addresses ending with @company.com are allowed"));
    }

    @Test
    public void shouldThrowExceptionWhenLoginEmailDomainInvalid() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> adminService.validateLogin("admin@gmail.com", "admin123"));
        assertTrue(exception.getMessage().contains("Only company email addresses ending with @company.com are allowed"));
    }

    // ==================================================
    // 2. DESIGNATION MASTER MANAGEMENT TESTS
    // ==================================================

    @Test
    public void testAddDesignation_Success() {
        Designation desig = new Designation(null, "Spring Boot Developer", "ACTIVE");
        when(designationRepository.findByNameIgnoreCase("Spring Boot Developer")).thenReturn(Optional.empty());
        when(designationRepository.save(any(Designation.class))).thenReturn(new Designation(1L, "Spring Boot Developer", "ACTIVE"));

        Designation created = adminService.addDesignation(desig);

        assertNotNull(created);
        assertEquals(1L, created.getId());
        assertEquals("Spring Boot Developer", created.getName());
    }

    @Test
    public void shouldAddDesignationWithDefaultActiveStatusWhenNull() {
        Designation desig = new Designation(null, "React Developer", null);
        when(designationRepository.findByNameIgnoreCase("React Developer")).thenReturn(Optional.empty());
        when(designationRepository.save(any(Designation.class))).thenAnswer(i -> i.getArgument(0));

        Designation created = adminService.addDesignation(desig);

        assertNotNull(created);
        assertEquals("ACTIVE", created.getStatus());
    }

    @Test
    public void shouldThrowExceptionWhenDesignationNameNullOrEmpty() {
        Designation desig = new Designation(null, " ", "ACTIVE");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> adminService.addDesignation(desig));
        assertTrue(exception.getMessage().contains("Designation name is required"));
    }

    @Test
    public void testAddDesignation_DuplicateName() {
        Designation desig = new Designation(null, "Spring Boot Developer", "ACTIVE");
        when(designationRepository.findByNameIgnoreCase("Spring Boot Developer")).thenReturn(Optional.of(new Designation(1L, "Spring Boot Developer", "ACTIVE")));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> adminService.addDesignation(desig));
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    public void shouldGetAllDesignationsSuccessfully() {
        Designation d1 = new Designation(1L, "Java Developer", "ACTIVE");
        when(designationRepository.findAll()).thenReturn(List.of(d1));

        List<Designation> list = adminService.getAllDesignations();

        assertEquals(1, list.size());
        assertEquals("Java Developer", list.get(0).getName());
    }

    @Test
    public void testGetActiveDesignations() {
        Designation d1 = new Designation(1L, "Java Developer", "ACTIVE");
        when(designationRepository.findByStatus("ACTIVE")).thenReturn(Arrays.asList(d1));

        List<Designation> activeList = adminService.getActiveDesignations();

        assertEquals(1, activeList.size());
        assertEquals("Java Developer", activeList.get(0).getName());
    }

    @Test
    public void shouldUpdateDesignationSuccessfully() {
        Designation existing = new Designation(1L, "Java Dev", "ACTIVE");
        Designation updatedData = new Designation(null, "Senior Java Dev", "ACTIVE");

        when(designationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(designationRepository.findByNameIgnoreCase("Senior Java Dev")).thenReturn(Optional.empty());
        when(designationRepository.save(any(Designation.class))).thenAnswer(i -> i.getArgument(0));

        Designation updated = adminService.updateDesignation(1L, updatedData);

        assertEquals("Senior Java Dev", updated.getName());
    }

    @Test
    public void shouldThrowExceptionWhenUpdatingDesignationWithDuplicateName() {
        Designation existing1 = new Designation(1L, "Java Dev", "ACTIVE");
        Designation existing2 = new Designation(2L, "Angular Dev", "ACTIVE");

        Designation updateData = new Designation(null, "Angular Dev", "ACTIVE");

        when(designationRepository.findById(1L)).thenReturn(Optional.of(existing1));
        when(designationRepository.findByNameIgnoreCase("Angular Dev")).thenReturn(Optional.of(existing2));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> adminService.updateDesignation(1L, updateData));
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    public void shouldThrowExceptionWhenUpdatingNonExistentDesignation() {
        when(designationRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> adminService.updateDesignation(99L, new Designation()));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    public void shouldUpdateDesignationStatusSuccessfully() {
        Designation existing = new Designation(1L, "Java Dev", "ACTIVE");
        when(designationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(designationRepository.save(any(Designation.class))).thenAnswer(i -> i.getArgument(0));

        Designation updated = adminService.updateDesignationStatus(1L, "INACTIVE");

        assertEquals("INACTIVE", updated.getStatus());
    }

    @Test
    public void shouldThrowExceptionWhenUpdatingStatusOfNonExistentDesignation() {
        when(designationRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> adminService.updateDesignationStatus(99L, "INACTIVE"));
        assertTrue(exception.getMessage().contains("not found"));
    }
}
