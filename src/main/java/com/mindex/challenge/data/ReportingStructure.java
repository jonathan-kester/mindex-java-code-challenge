package com.mindex.challenge.data;

import java.util.List;
import java.util.ListIterator;

public class ReportingStructure {
    private Employee employee;
    private int numberOfReports;

    public ReportingStructure() {

    }

    public void setEmployee(Employee employee) { this.employee = employee; }

    public void setNumberOfReports(int numberOfReports) { this.numberOfReports = numberOfReports; }

    public Employee getEmployee() { return this.employee; }

    public int getNumberOfReports() { return this.numberOfReports; }
}
