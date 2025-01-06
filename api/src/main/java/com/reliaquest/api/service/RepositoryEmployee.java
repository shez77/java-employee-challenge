package com.reliaquest.api.service;

/**
 * The Employee from the Mocked Employee Service which will not be exposed outside this package and used only for
 * mapping the response.
 *
 * @param id
 * @param employee_name
 * @param employee_salary
 * @param employee_age
 * @param employee_title
 * @param employee_email
 */
record RepositoryEmployee(
        String id,
        String employee_name,
        Integer employee_salary,
        Integer employee_age,
        String employee_title,
        String employee_email) {}
