package com.garage.dto;

import java.math.BigDecimal;

public class DashboardResponse {

    private BigDecimal totalRevenue;
    private long totalInvoices;
    private long totalPayments;
    private long totalAppointments;
    private long totalRepairOrders;
    private long repairOrdersInProgress;
    private long repairOrdersCompleted;
    private long totalCustomers;
    private long totalVehicles;

    public DashboardResponse() {}

    public DashboardResponse(BigDecimal totalRevenue, long totalInvoices, long totalPayments,
                             long totalAppointments, long totalRepairOrders,
                             long repairOrdersInProgress, long repairOrdersCompleted,
                             long totalCustomers, long totalVehicles) {
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.totalInvoices = totalInvoices;
        this.totalPayments = totalPayments;
        this.totalAppointments = totalAppointments;
        this.totalRepairOrders = totalRepairOrders;
        this.repairOrdersInProgress = repairOrdersInProgress;
        this.repairOrdersCompleted = repairOrdersCompleted;
        this.totalCustomers = totalCustomers;
        this.totalVehicles = totalVehicles;
    }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public long getTotalInvoices() { return totalInvoices; }
    public void setTotalInvoices(long totalInvoices) { this.totalInvoices = totalInvoices; }

    public long getTotalPayments() { return totalPayments; }
    public void setTotalPayments(long totalPayments) { this.totalPayments = totalPayments; }

    public long getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(long totalAppointments) { this.totalAppointments = totalAppointments; }

    public long getTotalRepairOrders() { return totalRepairOrders; }
    public void setTotalRepairOrders(long totalRepairOrders) { this.totalRepairOrders = totalRepairOrders; }

    public long getRepairOrdersInProgress() { return repairOrdersInProgress; }
    public void setRepairOrdersInProgress(long repairOrdersInProgress) { this.repairOrdersInProgress = repairOrdersInProgress; }

    public long getRepairOrdersCompleted() { return repairOrdersCompleted; }
    public void setRepairOrdersCompleted(long repairOrdersCompleted) { this.repairOrdersCompleted = repairOrdersCompleted; }

    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long totalCustomers) { this.totalCustomers = totalCustomers; }

    public long getTotalVehicles() { return totalVehicles; }
    public void setTotalVehicles(long totalVehicles) { this.totalVehicles = totalVehicles; }
}
