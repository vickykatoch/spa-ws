package org.avam.spaws.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@ConfigurationProperties(prefix="spa")
public class AppConfig {
    private String appConfigLoc;
    private String appName;
    private String assetsLoc;
    private String configRouteName;
    private long cacheDuration=0;
    private List<String> supportedMimeTypes = Arrays.asList("html", "js", "json", "csv", "css", "png", "svg", "ttf",
            "woff", "appcache", "jpg", "jpeg", "gif", "ico", "map", "woff2","yaml");


    public String getAppConfigLoc() {
        return appConfigLoc;
    }
    public String getAssetsLoc() {
        return assetsLoc;
    }
    public void setAssetsLoc(String assetsLoc) {
        this.assetsLoc = assetsLoc;
    }
    public String getAppName() {
        return appName;
    }
    public void setAppName(String appName) {
        this.appName = appName;
    }
    public void setAppConfigLoc(String appConfigLoc) {
        this.appConfigLoc = appConfigLoc;
    }

    public String getConfigRouteName() {
        return configRouteName;
    }
    public void setConfigRouteName(String configRouteName) {
        this.configRouteName = configRouteName.toLowerCase();
    }

    public List<String> getSupportedMimeTypes() {
        return supportedMimeTypes;
    }
    public void setSupportedMimeTypes(String mimeTypes) {
        if(mimeTypes!=null) {
            this.supportedMimeTypes = Arrays.stream(mimeTypes.split(",")).map(xtn-> xtn.trim()).collect(Collectors.toList());
        }
    }
    public long getCacheDuration() {
        return cacheDuration;
    }
    public void setCacheDuration(long cacheDuration) {
        this.cacheDuration = cacheDuration;
    }
}
