package com.aterrizar.service.checkin.steps;


import org.springframework.stereotype.Service;

import com.aterrizar.service.checkin.country.HealthDeclarationConfig;
import com.aterrizar.service.core.framework.flow.Step;
import com.aterrizar.service.core.framework.flow.StepResult;
import com.aterrizar.service.core.model.Context;
import com.aterrizar.service.core.model.RequiredField;

@Service
public class HealthDeclarationStep implements Step {
	private HealthDeclarationConfig healthConfig;

	public HealthDeclarationStep(HealthDeclarationConfig healthConfig) {
        this.healthConfig = healthConfig;
    }

	@Override
	public StepResult execute(Context context) {
		if (this.when(context)) {
			return this.onExecute(context);
		}

		return StepResult.success(context);
	} 

	@Override
	public boolean when(Context context){
		var flights = context.session().sessionData().flights();
		
		var enabledCountries = healthConfig.getEnabledCountryCodes();
		
		boolean result = flights.stream().anyMatch(
			f -> {
				var countryCode = f.destination().countryCode();
				return enabledCountries.contains(countryCode);
			}
		);
		
		return result;
	}


	@Override
	public StepResult onExecute(Context context) {
		
		var providedFields = context.checkinRequest().providedFields();
		
		var healthAcknowledgment = providedFields.get(RequiredField.HEALTH_CLEAR_ACKNOWLEDGEMENT);
		
		if (healthAcknowledgment == null || !Boolean.TRUE.equals(healthAcknowledgment)) {
			return StepResult.failure(context, "Health declaration acknowledgement is required");
		}
		
		var updatedContext = context.withSessionData(builder -> 
			builder.healthDeclarationAcknowledged(true)
		);
		
		return StepResult.success(updatedContext);
	}

}
