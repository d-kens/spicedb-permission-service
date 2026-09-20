package dev.onyango.permission_service.spicedb;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "authzed")
public class AuthzedProperties {
    private String target;
    private String token;
    private boolean useTls = false;

    public String getToken() {
        return token;
    }

    public String getTarget() {
        return target;
    }

    public boolean isUseTls() {
        return useTls;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUseTls(boolean useTls) {
        this.useTls = useTls;
    }
}
