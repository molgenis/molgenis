package org.molgenis.data.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import javax.validation.Validator;

@Configuration
public class ValidationConfig
{
	@Bean
	public MethodValidationPostProcessor methodValidationPostProcessor()
	{
		MethodValidationPostProcessor methodValidationPostProcessor = new MethodValidationPostProcessor();
		methodValidationPostProcessor.setValidator(validator());
		return methodValidationPostProcessor;
	}

	@Bean
	public Validator validator()
	{
		return new LocalValidatorFactoryBean();
	}
}
