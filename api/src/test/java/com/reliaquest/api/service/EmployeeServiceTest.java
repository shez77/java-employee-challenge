package com.reliaquest.api.service;

import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.exceptions.EmployeeNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@AutoConfigureMockRestServiceServer
@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {
    @Autowired
    @InjectMocks
    private EmployeeCacheService employeeCacheService;

    @Autowired
    @InjectMocks
    private EmployeeService employeeService;

    @SpyBean
    private RestClient.Builder restClientBuilder;

    private MockRestServiceServer mockServer;

    @Value("classpath:employees_test.json")
    private Resource resource;

    @Value("classpath:empty_test.json")
    private Resource emptyResource;

    @Value("classpath:one_employee.json")
    private Resource oneEmployee;

    @Value("classpath:90f31ba6-e718-435d-9cd2-9ef51e627b4b.json")
    private Resource oneEmployeeWithDuplicatedName;

    @Value("classpath:two_employees_having_same_name.json")
    private Resource resource_with_two_same_names;

    @BeforeEach
    public void setUp() {
        employeeService.restEmployeeCache();
    }

    @DisplayName("Test Employee Get with Multiple Employees")
    @Test
    void testGetEmployees_whenMultipleEmployeesAreFound() {
        // Set
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        mockServer
                .expect(method(HttpMethod.GET))
                .andExpect(requestTo("http://localhost:8112/api/v1/employee"))
                .andRespond(withSuccess(resource, MediaType.APPLICATION_JSON));

        List<Employee> employees = employeeService.getAllEmployees();
        mockServer.verify();
        assertEquals(50, employees.size());
    }

    @DisplayName("Test Employee Get with No Employees")
    @Test
    void testGetEmployees_whenNoEmployeesAreFound() {
        // Set
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        mockServer
                .expect(method(HttpMethod.GET))
                .andExpect(requestTo("http://localhost:8112/api/v1/employee"))
                .andRespond(withSuccess(emptyResource, MediaType.APPLICATION_JSON));

        List<Employee> employees = employeeService.getAllEmployees();
        mockServer.verify();
        assertEquals(0, employees.size());
    }

    @DisplayName("Test Employees Search by a Name Where Multiple Found")
    @Test
    void testGetEmployeesByNameSearch_whenMultipleEmployeesAreFound() {
        // Set
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        mockServer
                .expect(method(HttpMethod.GET))
                .andExpect(requestTo("http://localhost:8112/api/v1/employee"))
                .andRespond(withSuccess(resource, MediaType.APPLICATION_JSON));

        List<Employee> employees = employeeService.getEmployeesByNameSearch("Dr.");
        mockServer.verify();
        assertEquals(3, employees.size());
    }

    @DisplayName("Test Employees Search by a Name Where No Employee Found")
    @Test
    void testGetEmployeesByNameSearch_whenNoEmployeesAreFound() {
        // Set
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        mockServer
                .expect(method(HttpMethod.GET))
                .andExpect(requestTo("http://localhost:8112/api/v1/employee"))
                .andRespond(withSuccess(resource, MediaType.APPLICATION_JSON));

        List<Employee> employees = employeeService.getEmployeesByNameSearch("Dr Nobody");
        mockServer.verify();
        assertEquals(0, employees.size());
    }

    @DisplayName("Test Employees Search by a Name Where One Employee Found")
    @Test
    void testGetEmployeesByNameSearch_whenOnlyOneEmployeesIsFound() {
        // Set
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        mockServer
                .expect(method(HttpMethod.GET))
                .andExpect(requestTo("http://localhost:8112/api/v1/employee"))
                .andRespond(withSuccess(resource, MediaType.APPLICATION_JSON));

        List<Employee> employees = employeeService.getEmployeesByNameSearch("Bernetta");
        mockServer.verify();
        assertEquals(1, employees.size());
    }

    @DisplayName("Test Employees Get When 1 is found")
    @Test
    void testGetEmployeesById_whenEmployeesIsFound() {
        // Set
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        mockServer
                .expect(method(HttpMethod.GET))
                .andExpect(requestTo("http://localhost:8112/api/v1/employee/de567c35-3067-411b-957f-60f0f487cf9b"))
                .andRespond(withSuccess(oneEmployee, MediaType.APPLICATION_JSON));

        Employee employee = employeeService.getEmployeeById("de567c35-3067-411b-957f-60f0f487cf9b");
        mockServer.verify();
        assertNotNull(employee);
        assertEquals(UUID.fromString("de567c35-3067-411b-957f-60f0f487cf9b"), employee.id());
        assertEquals("Dr. Homer Conn", employee.name());
        assertEquals(422300, employee.salary());
        assertEquals(55, employee.age());
        assertEquals("Central Associate", employee.title());
        assertEquals("ronstring@company.com", employee.email());
    }

    @DisplayName("Test Employees Get When None found")
    @Test
    void testGetEmployeesById_throwsExceptionWhenEmployeeIsNotFound() {
        // Set
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        mockServer
                .expect(method(HttpMethod.GET))
                .andExpect(requestTo("http://localhost:8112/api/v1/employee/de567c35-3067-411b-957f-60f0f487cf9c"))
                .andRespond(
                        withSuccess("{\"status\":\"Successfully processed request.\"}", MediaType.APPLICATION_JSON));

        Assertions.assertThrowsExactly(EmployeeNotFoundException.class, () -> {
            employeeService.getEmployeeById("de567c35-3067-411b-957f-60f0f487cf9c");
        });
    }

    @DisplayName("Test Get Highest Salary For Employees")
    @Test
    void testGetHighestSalary_whenHighestSalaryIsPresent() {
        // Set
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        mockServer
                .expect(method(HttpMethod.GET))
                .andExpect(requestTo("http://localhost:8112/api/v1/employee"))
                .andRespond(withSuccess(resource, MediaType.APPLICATION_JSON));

        OptionalInt highest = employeeService.getHighestSalary();
        mockServer.verify();
        assertEquals(OptionalInt.of(477611), highest);
    }

    @DisplayName("Test Top 10 Salaried Employees")
    @Test
    void testGetTopHighestEarningNames_whenSuccessfullyFound() {
        // Set
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        mockServer
                .expect(method(HttpMethod.GET))
                .andExpect(requestTo("http://localhost:8112/api/v1/employee"))
                .andRespond(withSuccess(resource, MediaType.APPLICATION_JSON));

        List<String> top5 = employeeService.getTopHighestEarningNames(5);
        mockServer.verify();
        assertEquals(
                List.of(
                        "Odilia Tillman",
                        "Alton Bartoletti",
                        "Erwin Schiller MD",
                        "Ms. Ginny Auer",
                        "Merle Stiedemann DVM"),
                top5);
    }

    @DisplayName("Test Delete When a single Employee is Found with the name associated with the UUID")
    @Test
    void testAttemptToDeleteEmployeeWithId_whenOneIsFoundAndDeleted() {
        // Set
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        mockServer
                .expect(method(HttpMethod.GET))
                .andExpect(requestTo("http://localhost:8112/api/v1/employee/de567c35-3067-411b-957f-60f0f487cf9b"))
                .andRespond(withSuccess(oneEmployee, MediaType.APPLICATION_JSON));

        mockServer
                .expect(method(HttpMethod.GET))
                .andExpect(requestTo("http://localhost:8112/api/v1/employee"))
                .andRespond(withSuccess(resource, MediaType.APPLICATION_JSON));

        mockServer
                .expect(method(HttpMethod.DELETE))
                .andExpect(requestTo("http://localhost:8112/api/v1/employee"))
                .andExpect(content().json("{\"name\": \"Dr. Homer Conn\"}"))
                .andRespond(withSuccess(
                        "{\"data\": \"true\", \"status\":\"Successfully processed request.\"}",
                        MediaType.APPLICATION_JSON));

        String result = employeeService.attemptToDeleteEmployeeWithId("de567c35-3067-411b-957f-60f0f487cf9b");
        mockServer.verify();
        assertNull(result);
    }

    @DisplayName("Test Delete When multiple Employees Found with the same name As Employee being deleted with the UUID")
    @Test
    void testAttemptToDeleteEmployeeWithId_whenThereAreMultipleEmployeesWithSameNameAsOneBeingDeleted() {
        // Set
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        mockServer
                .expect(method(HttpMethod.GET))
                .andExpect(requestTo("http://localhost:8112/api/v1/employee/90f31ba6-e718-435d-9cd2-9ef51e627b4b"))
                .andRespond(withSuccess(oneEmployeeWithDuplicatedName, MediaType.APPLICATION_JSON));

        mockServer
                .expect(method(HttpMethod.GET))
                .andExpect(requestTo("http://localhost:8112/api/v1/employee"))
                .andRespond(withSuccess(resource_with_two_same_names, MediaType.APPLICATION_JSON));

        String result = employeeService.attemptToDeleteEmployeeWithId("90f31ba6-e718-435d-9cd2-9ef51e627b4b");
        mockServer.verify();
        assertEquals(
                "API currently does not support delete for this employee. Please reach out to App Support.", result);
    }
}
