package com.aterrizar.service.checkin.steps;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.aterrizar.service.checkin.country.HealthDeclarationConfig;
import com.aterrizar.service.core.model.RequiredField;
import com.neovisionaries.i18n.CountryCode;

import mocks.MockContext;

class HealthDeclarationStepTest {
    private HealthDeclarationStep healthDeclarationStep;
    private HealthDeclarationConfig healthConfig;

    public HealthDeclarationStepTest() {
    }



    @BeforeEach
    void setUp() {
        healthConfig = new HealthDeclarationConfig();
        healthConfig.setEnabledCountries(List.of("CN"));
        healthDeclarationStep = new HealthDeclarationStep(healthConfig);
    }

    @Test
    void shouldExecuteWhenFlightToChina() {
        var context = MockContext.initializedMock(CountryCode.US)
                .withSessionData(builder -> builder.flights(
                    List.of(MockContext.createFlightToCountry(CountryCode.CN))));
        
        var result = healthDeclarationStep.when(context);

        assertTrue(result);
    }

    @Test
    void shouldNotExecuteWhenFlightToOtherCountry() {
        var context = MockContext.initializedMock(CountryCode.AD)
                .withSessionData(builder -> builder.flights(
                    List.of(MockContext.createFlightToCountry(CountryCode.US))));
        
        var result = healthDeclarationStep.when(context);

        assertFalse(result);
    }

    @Test
    void shouldNotExecuteWhenNoFlights() {
        var context = MockContext.initializedMock(CountryCode.AD)
                .withSessionData(builder -> builder.flights(List.of()));
        
        var result = healthDeclarationStep.when(context);

        assertFalse(result);
    }

    @Test
    void shouldFailWhenHealthAcknowledgementNotProvided() {
        var context = MockContext.initializedMock(CountryCode.US)
                .withSessionData(builder -> builder.flights(
                    List.of(MockContext.createFlightToCountry(CountryCode.CN))));

        var stepResult = healthDeclarationStep.onExecute(context);

        assertFalse(stepResult.isSuccess());
        assertTrue(stepResult.isTerminal());
        assertEquals("Health declaration acknowledgement is required", stepResult.message());
    }

    @Test
    void shouldFailWhenHealthAcknowledgementIsFalse() {
		var fields = Map.of(RequiredField.HEALTH_CLEAR_ACKNOWLEDGEMENT, "false");
        var context = MockContext.initializedMock(CountryCode.US)
                .withSessionData(builder -> builder.flights(
                    List.of(MockContext.createFlightToCountry(CountryCode.CN))))
                .withCheckinRequest(builder -> builder.providedFields(
                    fields
					));

        var stepResult = healthDeclarationStep.onExecute(context);

        assertFalse(stepResult.isSuccess());
        assertTrue(stepResult.isTerminal());
        assertEquals("Health declaration acknowledgement is required", stepResult.message());
    }

    @Test
    void shouldSucceedWhenHealthAcknowledgementIsTrue() {
		var fields = Map.of(RequiredField.HEALTH_CLEAR_ACKNOWLEDGEMENT, "true"); 
        var context = MockContext.initializedMock(CountryCode.US)
                .withSessionData(builder -> builder.flights(
                    List.of(MockContext.createFlightToCountry(CountryCode.CN))))
                .withCheckinRequest(builder -> builder.providedFields(
					fields
                    ));

        var stepResult = healthDeclarationStep.onExecute(context);

        assertTrue(stepResult.isSuccess());
        assertFalse(stepResult.isTerminal());
        assertTrue(stepResult.context().session().sessionData().healthDeclarationAcknowledged());
    }

    @Test
    void shouldHandleMultipleFlightsWithChina() {
        var context = MockContext.initializedMock(CountryCode.US)
                .withSessionData(builder -> builder.flights(List.of(
                    MockContext.createFlightToCountry(CountryCode.US),
                    MockContext.createFlightToCountry(CountryCode.CN),
                    MockContext.createFlightToCountry(CountryCode.JP))));
        
        var result = healthDeclarationStep.when(context);

        assertTrue(result);
    }

    @Test
    void shouldHandleMultipleFlightsWithoutChina() {
        var context = MockContext.initializedMock(CountryCode.US)
                .withSessionData(builder -> builder.flights(List.of(
                    MockContext.createFlightToCountry(CountryCode.US),
                    MockContext.createFlightToCountry(CountryCode.JP),
                    MockContext.createFlightToCountry(CountryCode.KR))));
        
        var result = healthDeclarationStep.when(context);

        assertFalse(result);
    }

    @Test
    void shouldSupportMultipleEnabledCountries() {
        healthConfig.setEnabledCountries(List.of("CN", "KR"));
        var contextChina = MockContext.initializedMock(CountryCode.US)
                .withSessionData(builder -> builder.flights(
                    List.of(MockContext.createFlightToCountry(CountryCode.CN))));
        
        var contextKorea = MockContext.initializedMock(CountryCode.US)
                .withSessionData(builder -> builder.flights(
                    List.of(MockContext.createFlightToCountry(CountryCode.KR))));
        
        var contextJapan = MockContext.initializedMock(CountryCode.US)
                .withSessionData(builder -> builder.flights(
                    List.of(MockContext.createFlightToCountry(CountryCode.JP))));

        assertTrue(healthDeclarationStep.when(contextChina));
        assertTrue(healthDeclarationStep.when(contextKorea));
        assertFalse(healthDeclarationStep.when(contextJapan));
    }
}