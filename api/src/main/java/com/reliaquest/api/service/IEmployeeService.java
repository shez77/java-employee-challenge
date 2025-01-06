package com.reliaquest.api.service;

import com.reliaquest.api.dto.Employee;
import java.util.List;
import java.util.OptionalInt;

public interface IEmployeeService {
    List<Employee> getAllEmployees();

    List<Employee> getEmployeesByNameSearch(String searchString);

    Employee getEmployeeById(String id);

    String attemptToDeleteEmployeeWithId(String id);

    Employee createEmployee(Employee employeeInput);

    OptionalInt getHighestSalary();

    List<String> getTopHighestEarningNames(int number);
}
