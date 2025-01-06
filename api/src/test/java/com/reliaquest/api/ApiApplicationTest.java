package com.reliaquest.api;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.reliaquest.api.controller.EmployeeController;
import com.reliaquest.api.dto.Employee;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * THIS IS AN INTEGRATION TEST THAT NEEDS THE ServerApplication IN THE SERVER MODULE TO BE RUNNING.
 * Note that the Test Order is important for this integration test to limit the number of calls to
 * the Mock Server and reuse the Employees obtained from the first test to test the other Controller
 * methods. Given time, a more efficient setup for the tests would be created.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableCaching
class ApiApplicationTest {

    private static final Logger log = LoggerFactory.getLogger(ApiApplicationTest.class);

    @Autowired
    private TestRestTemplate testRestTemplate;

    private Employee firstEmployee;
    private List<Employee> employees;

    /**
     * Get All the Employees Successfully. This Test will run first and for expediency, will save the first Employee
     * to firstEmployee variable for use in the subsequent tests.
     */
    @Test
    @DisplayName("Get All Employees From Mock Server")
    @Order(1)
    void testGetAllEmployees_returns50EmployeesSuccessfully() {
        // When
        ResponseEntity<List<Employee>> response = testRestTemplate.exchange(
                EmployeeController.PATH,
                HttpMethod.GET,
                new HttpEntity<>(null, null),
                new ParameterizedTypeReference<>() {});

        // Save first Employee and the list of Employees for the next test cases and inputs
        this.employees = response.getBody();
        this.firstEmployee = this.employees.get(0);

        // Assert
        assertTrue(this.employees.size() >= 49);
        assertTrue(response.getStatusCode().is2xxSuccessful());

        log.info("The saved Employee for future tests is {}", firstEmployee);
    }

    @Test
    @DisplayName("Get the highest Salary of an employee")
    @Order(2)
    void testGetHighestEmployeeSalary_returnsHighestSalaryCorrectly() {
        // Get the expected highest salary
        Integer maxSalary =
                this.employees.stream().mapToInt(Employee::salary).max().getAsInt();
        log.info("Max Expected Salary is {}", maxSalary);
        // Get Highest Salary
        ResponseEntity<Integer> response = testRestTemplate.exchange(
                EmployeeController.PATH + "/highestSalary",
                HttpMethod.GET,
                new HttpEntity<>(null, null),
                new ParameterizedTypeReference<>() {});

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(maxSalary, response.getBody());
    }

    @Test
    @DisplayName("Test All Employee Get that match a given name")
    @Order(3)
    void testGetEmployeesByNameSearch_returnsMultipleEmployeesMatchingTheName() {
        // When we get an Employee searching by name
        ResponseEntity<List<Employee>> response = testRestTemplate.exchange(
                EmployeeController.PATH + format("/search/%s", firstEmployee.name()),
                HttpMethod.GET,
                new HttpEntity<>(null, null),
                new ParameterizedTypeReference<List<Employee>>() {});

        // We Assert the fields from the firstEmployee Saved Before
        assertTrue(response.getStatusCode().is2xxSuccessful());
        var matchingEmployees = response.getBody();
        Assertions.assertNotNull(matchingEmployees);
        Assertions.assertFalse(matchingEmployees.isEmpty());
        assertTrue(matchingEmployees.stream().allMatch(emp -> emp.name().contains(firstEmployee.name())));
    }

    @Test
    @DisplayName("Test Getting the Top 10 the highest Highest Earning employee names")
    @Order(4)
    void testGetTop10HighestEarningEmployeeNames_returnsTop10Correctly() {
        // Get the expected highest salary
        List<String> expectedEmployeeNamesSortedBySalary = getTop10ExpectedEmployeeNamesWithHighestSalaries();
        log.info("The Top 10 expected Employee Names are {}", expectedEmployeeNamesSortedBySalary);

        // Get the Actual Highest Salary Employee Names
        ResponseEntity<List<String>> response = testRestTemplate.exchange(
                EmployeeController.PATH + "/topTenHighestEarningEmployeeNames",
                HttpMethod.GET,
                new HttpEntity<>(null, null),
                new ParameterizedTypeReference<>() {});

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        List<String> highestSalariedEmployeeNames = response.getBody();
        assertEquals(expectedEmployeeNamesSortedBySalary, highestSalariedEmployeeNames);
    }

    @Test
    @DisplayName("Get an Employee with a given UUID saved from the previous test")
    @Order(5)
    void testGetEmployeeWithId_returnsCorrectEmployee() {
        // When we get an Employee with the firstEmployee Saved in previous Test
        ResponseEntity<Employee> response = getEmployeeWithId(firstEmployee.id().toString());

        // We Assert the fields from the firstEmployee Saved Before
        assertTrue(response.getStatusCode().is2xxSuccessful());
        Employee employee = response.getBody();
        Assertions.assertNotNull(employee);
        assertEquals(firstEmployee.id(), employee.id());
        assertEquals(firstEmployee.age(), employee.age());
        assertEquals(firstEmployee.name(), employee.name());
        assertEquals(firstEmployee.email(), employee.email());
        assertEquals(firstEmployee.salary(), employee.salary());
        assertEquals(firstEmployee.title(), employee.title());
    }

    @Test
    @DisplayName("Delete an Employee associated with the firstEmployee used earlier")
    @Order(6)
    void testDeleteEmployeeWithId_deletesEmployeeCorrectly() {
        // When we get an Employee with the firstEmployee Saved in previous Test
        ResponseEntity<String> response = testRestTemplate.exchange(
                EmployeeController.PATH + "/{id}",
                HttpMethod.DELETE,
                new HttpEntity<>(null, null),
                new ParameterizedTypeReference<String>() {},
                firstEmployee.id().toString());

        // We Assert the fields from the firstEmployee Saved Before
        assertTrue(response.getStatusCode().is2xxSuccessful());
        String message = response.getBody();
        assertEquals("Successfully Deleted", message);

        // Check Employee removed from repository
        ResponseEntity<Employee> checkResponse =
                getEmployeeWithId(firstEmployee.id().toString());
        assertEquals(HttpStatus.NOT_FOUND, checkResponse.getStatusCode());
    }

    @Test
    @DisplayName("Get an Employee with a given UUID where Employee doesnt exist")
    @Order(7)
    void testGetEmployeeWithId_whenEmployeeDoesntExistForId() {
        // When we get an Employee with the firstEmployee Saved in previous Test
        ResponseEntity<Employee> response = getEmployeeWithId("7885cf19-d5c0-4061-9620-7afd3a878b93");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Test Creation of Employee")
    @Order(8)
    void testCreateEmployee_returnsSuccessfullyCreatedEmployee() {
        // When we get an Employee to create
        Employee inputEmployee = new Employee(null, "Jane Doe", 115000, 35, "MD", null);

        // Create the Employee
        ResponseEntity<Employee> response = testRestTemplate.exchange(
                EmployeeController.PATH,
                HttpMethod.POST,
                new HttpEntity<>(inputEmployee, null),
                new ParameterizedTypeReference<Employee>() {});

        // We Assert the fields from the firstEmployee Saved Before
        assertTrue(response.getStatusCode().is2xxSuccessful());

        Employee createdEmployee = response.getBody();
        Assertions.assertNotNull(createdEmployee);
        Assertions.assertEquals(inputEmployee.name(), createdEmployee.name());
        Assertions.assertEquals(inputEmployee.salary(), createdEmployee.salary());
        Assertions.assertEquals(inputEmployee.age(), createdEmployee.age());
        Assertions.assertEquals(inputEmployee.title(), createdEmployee.title());
        Assertions.assertNotNull(createdEmployee.id());
        Assertions.assertNotNull(createdEmployee.email());
    }

    private ResponseEntity<Employee> getEmployeeWithId(String id) {
        return testRestTemplate.exchange(
                EmployeeController.PATH + "/{id}",
                HttpMethod.GET,
                new HttpEntity<>(null, null),
                new ParameterizedTypeReference<>() {},
                id);
    }

    private List<String> getTop10ExpectedEmployeeNamesWithHighestSalaries() {
        return this.employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::salary, TreeMap::new, Collectors.mapping(Employee::name, Collectors.toSet())))
                .descendingMap()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .limit(10)
                .toList();
    }
}
