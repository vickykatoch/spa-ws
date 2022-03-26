package org.avam.spaws.config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;

import org.apache.coyote.http2.Http2Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

@Configuration
public class RoutingAdapter implements WebMvcConfigurer {
    private final Logger _logger = LoggerFactory.getLogger(this.getClass().getName());
    private final AppConfig appConfig;
    private final ResourceResolver resourceResolver;

    public RoutingAdapter(AppConfig appConfig) {
        this.appConfig = appConfig;
        this.resourceResolver = new ResourceResolverImpl();
    }

    @Bean
    @ConditionalOnProperty(name = "spa.etag.enabled", havingValue = "true")
    public Filter shallowETagHeaderFilter() {
        _logger.info("ETag enabled");
        return new ShallowEtagHeaderFilter();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        _logger.info("App Name {}", appConfig.getAppName());
        registry.addResourceHandler("/csrf");
        registry.addResourceHandler("/**")
                .addResourceLocations(appConfig.getAssetsLoc())
                .setCacheControl(getCacheControl())
                .resourceChain(true)
                .addResolver(this.resourceResolver);
    }
    private CacheControl getCacheControl() {
        return appConfig.getCacheDuration()==0 ? CacheControl.noCache() : CacheControl.maxAge( appConfig.getCacheDuration(), TimeUnit.SECONDS);
    }
    private class ResourceResolverImpl implements ResourceResolver {
        private final Resource _index;

        public ResourceResolverImpl() {
            this._index = new FileSystemResource(String.format("%sindex.html", appConfig.getAssetsLoc()));
        }

        @Override
        public Resource resolveResource(HttpServletRequest request, String requestPath,
                List<? extends Resource> locations, ResourceResolverChain chain) {
            return resolve(requestPath, locations);
        }

        @Override
        public String resolveUrlPath(String resourcePath, List<? extends Resource> locations,
                ResourceResolverChain chain) {
            final Resource resolvedResource = resolve(resourcePath, locations);
            if (resolvedResource == null)
                return null;
            try {
                return resolvedResource.getURL().toString();
            } catch (final IOException e) {
                return resolvedResource.getFilename();
            }
        }

        private Resource resolve(final String requestPath, List<? extends Resource> locations) {
            if (isHandled(requestPath)) {
                Optional<String> token = requestPath.indexOf('/')>0 ? Arrays.stream(requestPath.split("/")).findFirst() : Optional.empty();
                Path resourcePath = Path.of(appConfig.getAssetsLoc(), requestPath);

                if(!token.isEmpty() && token.get().toLowerCase().equals(appConfig.getConfigRouteName())) {
                    resourcePath = Path.of(appConfig.getAppConfigLoc(), requestPath.replaceAll(token.get(),"").substring(1));
                }
                FileSystemResource resource = new FileSystemResource(resourcePath);
                return resource.exists() ? resource : null;
            }
            return _index;
        }

        private boolean isHandled(final String path) {
            final String xtn = StringUtils.getFilenameExtension(path);
            return appConfig.getSupportedMimeTypes().stream().anyMatch(xt -> xt.equals(xtn));
        }

    }
}
