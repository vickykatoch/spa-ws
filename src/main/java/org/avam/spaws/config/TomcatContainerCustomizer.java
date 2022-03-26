package org.avam.spaws.config;

import org.apache.coyote.http2.Http2Protocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatContainerCustomizer {
    @Bean
    public WebServerFactoryCustomizer tomcatCustomizer() {
        return (container) -> {
            if (container instanceof TomcatServletWebServerFactory) {
                ((TomcatServletWebServerFactory) container)
                        .addConnectorCustomizers((connector) -> {
                            connector.addUpgradeProtocol(new Http2Protocol());
                        });
            }
        };
    }
}
