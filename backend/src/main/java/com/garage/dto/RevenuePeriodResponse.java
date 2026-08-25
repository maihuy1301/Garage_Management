package com.garage.dto;

import java.math.BigDecimal;

public class RevenuePeriodResponse {

    private String period;
    private BigDecimal revenue;
    private long invoiceCount;

    public RevenuePeriodResponse() {}

    public RevenuePeriodResponse(String period, BigDecimal revenue, long invoiceCount) {
        this.period = period;
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
        this.invoiceCount = invoiceCount;
    }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }

    public long getInvoiceCount() { return invoiceCount; }
    public void setInvoiceCount(long invoiceCount) { this.invoiceCount = invoiceCount; }
}
