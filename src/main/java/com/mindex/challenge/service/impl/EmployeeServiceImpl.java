package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Creating employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    @Override
    public ReportingStructure reports(String id) {
        LOG.debug("Creating Reporting Structure for Employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        ReportingStructure requestedReportingStructure = new ReportingStructure();
        requestedReportingStructure.setEmployee(employee);
        //Since the Employee's direct reports field is only an employee ID, we must recurisvely look it up with each given ID
        requestedReportingStructure.setNumberOfReports(calculateDirectReports(employee));

        return requestedReportingStructure;
    }

    private int calculateDirectReports(Employee employee) {
        List<Employee> reportsList = employee.getDirectReports();
        //if an employee has no direct reports, return zero
        if (reportsList == null || reportsList.isEmpty()) {
            return 0;
        }
        ListIterator<Employee> reportsIterator = reportsList.listIterator();
        Integer totalReports = 0;
        //for each direct report, add that person, plus all the reports under them
        while (reportsIterator.hasNext()) {
            totalReports = totalReports + 1 + calculateDirectReports(employeeRepository.findByEmployeeId(reportsIterator.next().getEmployeeId()));
        }
        return totalReports;
    }
}
