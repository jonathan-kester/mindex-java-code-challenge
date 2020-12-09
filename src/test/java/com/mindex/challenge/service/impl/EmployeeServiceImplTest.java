package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {

    private String employeeUrl;
    private String employeeIdUrl;
    private String reportingStructureUrl;

    @Autowired
    private EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
        reportingStructureUrl = "http://localhost:" + port + "/employee/ReportingStructure/{id}";
    }

    @Test
    public void testCreateReadUpdate() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        // Create checks
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);


        // Read checks
        Employee readEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(createdEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee, readEmployee);


        // Update checks
        readEmployee.setPosition("Development Manager");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(readEmployee, headers),
                        Employee.class,
                        readEmployee.getEmployeeId()).getBody();

        assertEmployeeEquivalence(readEmployee, updatedEmployee);
    }

    @Test
    public void givenEmployeeWithNoDirectReportsReturnZero() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        ReportingStructure directReports = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, createdEmployee.getEmployeeId()).getBody();

        assertEquals(directReports.getNumberOfReports(), 0);
        assertEmployeeEquivalence(createdEmployee, directReports.getEmployee());
    }

    @Test
    public void givenEmployeeWithDirectReportsReturnCount() {
        Employee testEmployeeOne = new Employee();
        testEmployeeOne.setFirstName("John");
        testEmployeeOne.setLastName("Doe");
        testEmployeeOne.setDepartment("Engineering");
        testEmployeeOne.setPosition("Developer");

        Employee createdEmployeeOne = restTemplate.postForEntity(employeeUrl, testEmployeeOne, Employee.class).getBody();

        Employee testEmployeeTwo = new Employee();
        testEmployeeTwo.setFirstName("Jane");
        testEmployeeTwo.setLastName("Doe");
        testEmployeeTwo.setDepartment("Engineering");
        testEmployeeTwo.setPosition("Project Manager");
        List<Employee> EmployeeTwoReportList = new ArrayList<Employee>();
        EmployeeTwoReportList.add(createdEmployeeOne);
        testEmployeeTwo.setDirectReports(EmployeeTwoReportList);

        Employee createdEmployeeTwo = restTemplate.postForEntity(employeeUrl, testEmployeeTwo, Employee.class).getBody();

        Employee testEmployeeThree = new Employee();
        testEmployeeThree.setFirstName("Joseph");
        testEmployeeThree.setLastName("Doe");
        testEmployeeThree.setDepartment("HR");
        testEmployeeThree.setPosition("Head Of Department");

        Employee createdEmployeeThree = restTemplate.postForEntity(employeeUrl, testEmployeeThree, Employee.class).getBody();

        Employee testEmployeeFour = new Employee();
        testEmployeeFour.setFirstName("Josephine");
        testEmployeeFour.setLastName("Doe");
        testEmployeeFour.setDepartment("Management");
        testEmployeeFour.setPosition("CEO");
        List<Employee> EmployeeFourReportList = new ArrayList<Employee>();
        EmployeeFourReportList.add(createdEmployeeTwo);
        EmployeeFourReportList.add(createdEmployeeThree);
        testEmployeeFour.setDirectReports(EmployeeFourReportList);

        Employee createdEmployeeFour = restTemplate.postForEntity(employeeUrl, testEmployeeFour, Employee.class).getBody();

        ReportingStructure directReports = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, createdEmployeeFour.getEmployeeId()).getBody();

        assertEquals(directReports.getNumberOfReports(), 3);
        assertEmployeeEquivalence(createdEmployeeFour, directReports.getEmployee());
    }

    @Test
    public void givenInvalidEmployeeIDForReportingStructureReturnError() {
        HttpStatus test = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, "Invalid Employee Id").getStatusCode();
        assertEquals(test, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }
}
