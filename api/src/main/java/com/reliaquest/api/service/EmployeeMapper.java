package com.reliaquest.api.service;

import com.reliaquest.api.dto.Employee;
import java.util.UUID;

/**
 * Package protected conversion of Employee from the Mock Server to the Employee exposed in the API
 * and vice versa.
 */
class EmployeeMapper {
    static Employee mapToEmployee(final RepositoryEmployee internalEmployee) {
        return new Employee(
                UUID.fromString(internalEmployee.id()),
                internalEmployee.employee_name(),
                internalEmployee.employee_salary(),
                internalEmployee.employee_age(),
                internalEmployee.employee_title(),
                internalEmployee.employee_email());
    }

    static EmployeeCreate mapToRepositoryEmployee(final Employee inputEmployee) {
        return new EmployeeCreate(
                inputEmployee.name(), inputEmployee.salary(), inputEmployee.age(), inputEmployee.title());
    }
}

record EmployeeCreate(String name, Integer salary, Integer age, String title) {}
