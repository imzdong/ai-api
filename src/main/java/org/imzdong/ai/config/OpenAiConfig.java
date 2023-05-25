package org.imzdong.ai.config;

import org.imzdong.ai.openai.api.OpenAiApi;
import org.imzdong.ai.openai.builder.FeignClientBuilder;
import org.imzdong.ai.openai.config.ProxyProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
public class OpenAiConfig {

    @Autowired
    private ProxyProperties proxyProperties;

    @Bean
    public OpenAiApi initOpenAiApi(){
        String key = proxyProperties.getKey();
        Proxy proxy = null;
        if(proxyProperties.getUseProxy()!=null&&proxyProperties.getUseProxy()) {
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyProperties.getProxyUrl(), proxyProperties.getProxyPort()));
        }
        return FeignClientBuilder.build(proxyProperties.getBaseUrl(), OpenAiApi.class, key, proxy);
    }

}
