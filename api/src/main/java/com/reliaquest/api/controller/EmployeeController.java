package com.reliaquest.api.controller;

import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.service.IEmployeeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(EmployeeController.PATH)
@RequiredArgsConstructor
public class EmployeeController implements IEmployeeController<Employee, Employee> {

    public static final String PATH = "/api/employeeDetails/v1";
    private final IEmployeeService employeeService;

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        List<Employee> employees = employeeService.getEmployeesByNameSearch(searchString);
        return new ResponseEntity<>(employees, employees.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(String id) {
        return new ResponseEntity<>(employeeService.getEmployeeById(id), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        return new ResponseEntity<>(employeeService.getHighestSalary().orElse(0), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        List<String> highestEarningEmployeeNames = employeeService.getTopHighestEarningNames(10);
        return new ResponseEntity<>(
                highestEarningEmployeeNames,
                highestEarningEmployeeNames.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Employee> createEmployee(Employee employeeInput) {
        Employee employee = employeeService.createEmployee(employeeInput);
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        String result = employeeService.attemptToDeleteEmployeeWithId(id);
        if (result == null) {
            return new ResponseEntity<>("Successfully Deleted", HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.NOT_IMPLEMENTED);
        }
    }
}
