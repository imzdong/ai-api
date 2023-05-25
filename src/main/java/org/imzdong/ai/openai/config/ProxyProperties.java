package org.imzdong.ai.openai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "openai")
@Data
public class ProxyProperties {

    private Boolean useProxy;
    private String proxyUrl;
    private Integer proxyPort;
    private String key;
    private String baseUrl;

}
