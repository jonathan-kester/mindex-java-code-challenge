package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.service.CompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompensationServiceImpl implements CompensationService {

    private static final Logger LOG = LoggerFactory.getLogger(CompensationServiceImpl.class);

    @Autowired
    private CompensationRepository compensationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public Compensation create(Compensation compensation) {
        LOG.debug("Creating Compensation [{}]", compensation);

        if (compensation.getEmployee().getEmployeeId() == null) {
            throw new RuntimeException("Cannot create compensation for Employee with no ID");
        }

        //ensure the employee in the compensation given to the request matches the one in the database
        Employee employee = employeeRepository.findByEmployeeId(compensation.getEmployee().getEmployeeId());

        if (employee == null) {
            throw new RuntimeException("No current Employees of ID: " + compensation.getEmployee().getEmployeeId());
        }

        compensation.setEmployee(employee);

        compensationRepository.insert(compensation);

        return compensation;
    }

    @Override
    public Compensation read(String id) {
        LOG.debug("reading compensation with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        Compensation compensation = compensationRepository.findByEmployee(employee);

        if (compensation == null) {
            throw new RuntimeException("No Compensation for employeeId: " + id);
        }

        return compensation;
    }
}
