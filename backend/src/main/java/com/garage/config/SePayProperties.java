package com.garage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "payments.sepay")
public class SePayProperties {
    private boolean enabled;
    private String environment = "test";
    private String webhookKey = "";
    private String bankCode = "";
    private String bankName = "";
    private String accountNumber = "";
    private String accountName = "";

    public boolean isReady() {
        return enabled && ("test".equals(environment) || "live".equals(environment))
                && webhookKey.length() >= 32 && !bankCode.isBlank() && !bankName.isBlank()
                && !accountNumber.isBlank() && !accountName.isBlank();
    }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean v) { enabled = v; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String v) { environment = v; }
    public String getWebhookKey() { return webhookKey; }
    public void setWebhookKey(String v) { webhookKey = v; }
    public String getBankCode() { return bankCode; }
    public void setBankCode(String v) { bankCode = v; }
    public String getBankName() { return bankName; }
    public void setBankName(String v) { bankName = v; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String v) { accountNumber = v; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String v) { accountName = v; }
}
