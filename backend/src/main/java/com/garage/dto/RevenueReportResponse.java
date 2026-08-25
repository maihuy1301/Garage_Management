package com.garage.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RevenueReportResponse {

    private BigDecimal totalRevenue;
    private long totalInvoices;
    private BigDecimal totalPaid;
    private BigDecimal totalUnpaid;
    private List<RevenuePeriodResponse> periods = new ArrayList<>();

    public RevenueReportResponse() {}

    public RevenueReportResponse(BigDecimal totalRevenue, long totalInvoices, BigDecimal totalPaid,
                                 BigDecimal totalUnpaid, List<RevenuePeriodResponse> periods) {
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.totalInvoices = totalInvoices;
        this.totalPaid = totalPaid != null ? totalPaid : BigDecimal.ZERO;
        this.totalUnpaid = totalUnpaid != null ? totalUnpaid : BigDecimal.ZERO;
        this.periods = periods != null ? periods : new ArrayList<>();
    }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public long getTotalInvoices() { return totalInvoices; }
    public void setTotalInvoices(long totalInvoices) { this.totalInvoices = totalInvoices; }

    public BigDecimal getTotalPaid() { return totalPaid; }
    public void setTotalPaid(BigDecimal totalPaid) { this.totalPaid = totalPaid; }

    public BigDecimal getTotalUnpaid() { return totalUnpaid; }
    public void setTotalUnpaid(BigDecimal totalUnpaid) { this.totalUnpaid = totalUnpaid; }

    public List<RevenuePeriodResponse> getPeriods() { return periods; }
    public void setPeriods(List<RevenuePeriodResponse> periods) { this.periods = periods; }
}
