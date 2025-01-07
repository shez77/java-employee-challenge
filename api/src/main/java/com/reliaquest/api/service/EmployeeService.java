package com.reliaquest.api.service;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.exceptions.EmployeeNotFoundException;
import com.reliaquest.api.exceptions.UnableToObtainEmployeesException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

/**
 * This service will handle the interaction with the Mock Employee Server to get the Data
 * served by the API Service.
 */
@Service
@Slf4j
class EmployeeService implements IEmployeeService {
    private final EmployeeCacheService employeeCacheService;
    private final RestClient.Builder restClientBuilder;

    public EmployeeService(EmployeeCacheService employeeCacheService, RestClient.Builder restClientBuilder) {
        this.employeeCacheService = employeeCacheService;
        this.restClientBuilder = restClientBuilder;
    }

    @Override
    public List<Employee> getAllEmployees() {
        return employeeCacheService.getAllEmployees();
    }

    @Override
    public List<Employee> getEmployeesByNameSearch(String nameFragment) {
        return getAllEmployees().stream()
                .filter(emp -> Objects.nonNull(emp.name()) && emp.name().contains(nameFragment))
                .collect(toList());
    }

    @Override
    public Employee getEmployeeById(String id) {
        Response<RepositoryEmployee> response = null;
        try {
            response = restClientBuilder
                    .build()
                    .get()
                    .uri("/api/v1/employee/{id}", id)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            if (response.data() == null) {
                var msg = String.format("Employee with ID %s Does Not Exist.", id);
                log.info(msg);
                throw new EmployeeNotFoundException(msg);
            }
        } catch (HttpClientErrorException e) {
            var msg = String.format("Employee with ID %s Does Not Exist.", id);
            log.info(msg);
            throw new EmployeeNotFoundException(msg, e);
        } catch (HttpServerErrorException e) {
            log.error("Got an error from MockEmployeeService : {}", response.error());
            throw new UnableToObtainEmployeesException("Employee could not be obtained due to an error.");
        }
        return EmployeeMapper.mapToEmployee(response.data());
    }

    @Override
    @CacheEvict(value = "employees", allEntries = true)
    public String attemptToDeleteEmployeeWithId(String id) {
        Employee employeeToDelete = getEmployeeById(id);
        List<Employee> deleteCandidateEmployees = getEmployeesByExactNameMatch(employeeToDelete.name());
        if (deleteCandidateEmployees.size() == 1
                && deleteCandidateEmployees.get(0).id().equals(UUID.fromString(id))) {
            Response<Boolean> response = restClientBuilder
                    .build()
                    .method(HttpMethod.DELETE)
                    .uri("/api/v1/employee")
                    .contentType(APPLICATION_JSON)
                    .body(new DeleteRequest(employeeToDelete.name()))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            if (response != null && response.data() != null && response.data()) {
                return null;
            } else {
                String msg = String.format("Employee with id %s was NOT DELETED", id);
                log.error(msg);
                return msg;
            }
        }
        log.warn(
                "MockEmployeeController supports deletion by name only but we have {} employees that have the same name {} as the employee id {}",
                deleteCandidateEmployees.size(),
                employeeToDelete.name(),
                id);
        return "API currently does not support delete for this employee. Please reach out to App Support.";
    }

    @Override
    @CacheEvict(value = "employees", allEntries = true)
    public Employee createEmployee(Employee employeeInput) {
        EmployeeCreate employeeCreatePayload = EmployeeMapper.mapToRepositoryEmployee(employeeInput);
        Response<RepositoryEmployee> response = restClientBuilder
                .build()
                .method(HttpMethod.POST)
                .uri("/api/v1/employee")
                .contentType(APPLICATION_JSON)
                .body(employeeCreatePayload)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        return EmployeeMapper.mapToEmployee(response.data());
    }

    @Override
    public OptionalInt getHighestSalary() {
        return getAllEmployees().stream().mapToInt(Employee::salary).max();
    }

    @Override
    public List<String> getTopHighestEarningNames(int number) {
        return getAllEmployees().stream()
                .sorted(Comparator.comparing(Employee::salary).reversed())
                .limit(number)
                .map(Employee::name)
                .collect(toList());
    }

    @CacheEvict(value = "employees", allEntries = true)
    public void restEmployeeCache() {
        log.info("Removed the Employee Cache.");
    }

    private List<Employee> getEmployeesByExactNameMatch(String nameFragment) {
        return getAllEmployees().stream()
                .filter(emp -> Objects.nonNull(emp.name()) && emp.name().equalsIgnoreCase(nameFragment))
                .collect(toList());
    }

    record DeleteRequest(String name) {}
}

@Service
@Slf4j
class EmployeeCacheService {
    private final RestClient.Builder restClientBuilder;

    public EmployeeCacheService(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }

    @Cacheable(value = "employees")
    public List<Employee> getAllEmployees() {
        log.info("Called Get All Employees Cached.");
        var response = restClientBuilder
                .build()
                .get()
                .uri("/api/v1/employee")
                .retrieve()
                .body(new ParameterizedTypeReference<Response<List<RepositoryEmployee>>>() {});
        if (Response.Status.ERROR == response.status()) {
            throw new UnableToObtainEmployeesException("Employees could not be obtained due to an error.");
        }
        List<RepositoryEmployee> employees = response.data() != null ? response.data() : List.of();
        return employees.stream().map(EmployeeMapper::mapToEmployee).toList();
    }
}
