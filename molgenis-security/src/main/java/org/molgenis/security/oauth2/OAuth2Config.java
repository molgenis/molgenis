package org.molgenis.security.oauth2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@Configuration
public class OAuth2Config
{
	@Bean
	public ClientRegistration googleClientRegistration()
	{
		return CommonOAuth2Provider.GOOGLE.getBuilder("google")
										  .clientId(
												  "698164123985-sma4poob9sulopmam54up7cns8kcv76c.apps.googleusercontent.com")
										  .clientSecret("rOj0GltrOH1bJE-R2jttwNC8")
										  .build();
	}

	@Bean
	public ClientRegistration githubClientRegistration()
	{
		return CommonOAuth2Provider.GITHUB.getBuilder("github")
										  .clientId("1ab49d58aac09b6e5451")
										  .clientSecret("1d2486e22f0da33597e910f2a621b22201dbc806")
										  .build();
	}

	@Bean
	public InMemoryClientRegistrationRepository clientRegistrationRepository()
	{
		return new InMemoryClientRegistrationRepository(googleClientRegistration(), githubClientRegistration());
	}

	@Bean
	public OAuth2AuthorizedClientService authorizedClientService(
			ClientRegistrationRepository clientRegistrationRepository)
	{
		return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
	}
}
