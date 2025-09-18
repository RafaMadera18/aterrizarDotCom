package com.aterrizar.service.checkin.country;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.neovisionaries.i18n.CountryCode;

@ConfigurationProperties(prefix = "feature.health.declaration")
@Component
public class HealthDeclarationConfig {
    
    private List<String> countries = new ArrayList<>();
    
    public List<String> getEnabledCountries() {
        return countries;
    }
    
    public void setEnabledCountries(List<String> enabledCountries) {
        this.countries = enabledCountries;
    }
    
    public List<CountryCode> getEnabledCountryCodes() {
        return countries.stream()
            .map(CountryCode::getByCode)
            .collect(Collectors.toList());
    }
}