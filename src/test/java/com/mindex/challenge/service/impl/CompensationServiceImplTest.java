package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.CompensationService;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompensationServiceImplTest {

    private String employeeUrl;
    private String compensationUrl;
    private String compensationIdUrl;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private CompensationService compensationService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        compensationUrl = "http://localhost:" + port + "/compensation";
        compensationIdUrl = "http://localhost:" + port + "/compensation/{id}";
    }

    @Test
    public void givenCompensationWithEmployeeWithNoIDCreationReturnError() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        int testSalary = 75000;

        Date testEffectiveDate = new Date();

        Compensation testCompensation = new Compensation();
        testCompensation.setEmployee(testEmployee);
        testCompensation.setEffectiveDate(testEffectiveDate);
        testCompensation.setSalary(testSalary);

        HttpStatus test = restTemplate.postForEntity(compensationUrl, testCompensation, Compensation.class).getStatusCode();

        assertEquals(test, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Test
    public void givenCompensationWithEmployeeWithInvalidIDForCreationReturnError() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");
        testEmployee.setEmployeeId("Invalid ID");

        int testSalary = 75000;

        Date testEffectiveDate = new Date();

        Compensation testCompensation = new Compensation();
        testCompensation.setEmployee(testEmployee);
        testCompensation.setEffectiveDate(testEffectiveDate);
        testCompensation.setSalary(testSalary);

        //verify an error is thrown when given a compensation with an employee with an ID that's not in the database
        HttpStatus test = restTemplate.postForEntity(compensationUrl, testCompensation, Compensation.class).getStatusCode();
        assertEquals(test, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @Test
    public void givenCompensationWithEmployeeWithValidIDForCreationReturnCompensation() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        int testSalary = 75000;

        Date testEffectiveDate = new Date();

        //create employee in database
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);

        //create compensation with new employee
        Compensation testCompensation = new Compensation();
        testCompensation.setEmployee(createdEmployee);
        testCompensation.setEffectiveDate(testEffectiveDate);
        testCompensation.setSalary(testSalary);

        //write compensation to database
        Compensation createdCompensation = restTemplate.postForEntity(compensationUrl, testCompensation, Compensation.class).getBody();

        //confirm written compensation matches the test compensation
        assertCompensationEquivalence(createdCompensation, testCompensation);

    }

    @Test
    public void givenInvalidEmployeeIDForReadReturnError() {
        HttpStatus test = restTemplate.getForEntity(compensationIdUrl, Compensation.class, "Invalid Employee Id").getStatusCode();
        assertEquals(test, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void givenValidEmployeeIDWithoutCompensationForReadReturnError() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        int testSalary = 75000;

        Date testEffectiveDate = new Date();

        //create employee in database
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);

        //verify an error is thrown when given requesting a compensation with an valid employee
        HttpStatus test = restTemplate.getForEntity(compensationIdUrl, Compensation.class, createdEmployee.getEmployeeId()).getStatusCode();
        assertEquals(test, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void givenValidEmployeeIDWithCompensationForReadReturnCompensation() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        int testSalary = 75000;

        Date testEffectiveDate = new Date();

        //create employee in database
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);

        //create compensation with new employee
        Compensation testCompensation = new Compensation();
        testCompensation.setEmployee(createdEmployee);
        testCompensation.setEffectiveDate(testEffectiveDate);
        testCompensation.setSalary(testSalary);

        //write and retrieve compensation from database
        Compensation createdCompensation = restTemplate.postForEntity(compensationUrl, testCompensation, Compensation.class).getBody();

        Compensation retrievedCompensation = restTemplate.getForEntity(compensationIdUrl, Compensation.class, createdEmployee.getEmployeeId()).getBody();

        //verify created compensation matches retrieved compensation
        assertCompensationEquivalence(createdCompensation, retrievedCompensation);
    }

    private static void assertCompensationEquivalence(Compensation expected, Compensation actual) {
        assertEquals(expected.getSalary(), actual.getSalary());
        assertEquals(expected.getEffectiveDate(), actual.getEffectiveDate());
        assertEmployeeEquivalence(expected.getEmployee(), actual.getEmployee());
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }
}