package cl.duoc.pedidos360.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "azure.activedirectory")
public class AzureProperties {

    private String tenantId = "";
    private String clientId = "";
    private String appIdUri = "";

    public boolean isConfigured() {
        return tenantId != null && !tenantId.isBlank()
            && clientId != null && !clientId.isBlank()
            && appIdUri != null && !appIdUri.isBlank();
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getAppIdUri() {
        return appIdUri;
    }

    public void setAppIdUri(String appIdUri) {
        this.appIdUri = appIdUri;
    }
}
