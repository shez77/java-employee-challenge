package com.reliaquest.api.controller;

import static java.util.Arrays.asList;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.exceptions.UnableToObtainEmployeesException;
import com.reliaquest.api.service.IEmployeeService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = EmployeeController.class)
public class EmployeeControllerTest {

    private static final List<Employee> TEST_EMPLOYEES = asList(
            new Employee(
                    UUID.fromString("a5764857-ae35-34dc-8f25-a9c9e73aa898"),
                    "John Doe",
                    100000,
                    45,
                    "VP",
                    "john.doe@somewhere.org"),
            new Employee(
                    UUID.fromString("2384f927-5e2f-3998-8baa-c768616287f5"),
                    "Jane Doe",
                    115000,
                    35,
                    "MD",
                    "jane.doe@somewhere.org"));

    @MockBean
    IEmployeeService employeeService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Employees can be retrieved")
    void testGetAllEmployees_returnsEmployees() throws Exception {
        // Given
        Mockito.when(employeeService.getAllEmployees()).thenReturn(TEST_EMPLOYEES);
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(EmployeeController.PATH);

        // When
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        String responseString = mvcResult.getResponse().getContentAsString();

        // Assert
        List<Employee> employees = new ObjectMapper().readValue(responseString, new TypeReference<>() {});
        Assertions.assertEquals(2, employees.size());
    }

    @Test
    @DisplayName("Employees could not be retrieved")
    void testGetAllEmployees_returns500InternalServerErrorWhenEmployeesCouldNotBeRetrieved() throws Exception {
        // Given
        Mockito.when(employeeService.getAllEmployees()).thenThrow(UnableToObtainEmployeesException.class);
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(EmployeeController.PATH);

        // When
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();

        // Assert
        Assertions.assertEquals(500, mvcResult.getResponse().getStatus());
    }
}
