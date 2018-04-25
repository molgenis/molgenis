package org.molgenis.security.oauth2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import static org.springframework.security.config.oauth2.client.CommonOAuth2Provider.GITHUB;
import static org.springframework.security.config.oauth2.client.CommonOAuth2Provider.GOOGLE;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.BASIC;

@Configuration
public class OAuth2Config
{
	@Bean
	public ClientRegistration googleClientRegistration()
	{
		return GOOGLE.getBuilder("google")
					 .clientId("698164123985-sma4poob9sulopmam54up7cns8kcv76c.apps.googleusercontent.com")
					 .clientSecret("rOj0GltrOH1bJE-R2jttwNC8")
					 .build();
	}

	@Bean
	public ClientRegistration githubClientRegistration()
	{
		return GITHUB.getBuilder("github")
					 .clientId("1ab49d58aac09b6e5451")
					 .clientSecret("1d2486e22f0da33597e910f2a621b22201dbc806")
					 .build();
	}

	@Bean
	public ClientRegistration orcidClientRegistration()
	{
		return ClientRegistration.withRegistrationId("orcid")
								 .clientAuthenticationMethod(BASIC)
								 .redirectUriTemplate("{baseUrl}/login/oauth2/code/{registrationId}")
								 .authorizationGrantType(AUTHORIZATION_CODE)
								 .clientName("ORCID")
								 .scope("openid")
								 .jwkSetUri("https://orcid.org/oauth/jwks")
								 .authorizationUri("https://orcid.org/oauth/authorize")
								 .tokenUri("https://orcid.org/oauth/token")
								 .userInfoUri("https://orcid.org/oauth/userinfo")
								 .userNameAttributeName("sub")
								 .clientId("APP-FAVFU9PTFAV7Z0LL")
								 .clientSecret("b1e2d1db-cd80-4cea-8b2e-bf634216b749")
								 .build();
	}

	@Bean
	public InMemoryClientRegistrationRepository clientRegistrationRepository()
	{
		return new InMemoryClientRegistrationRepository(googleClientRegistration(), githubClientRegistration(),
				orcidClientRegistration());
	}

	@Bean
	public OAuth2AuthorizedClientService authorizedClientService(
			ClientRegistrationRepository clientRegistrationRepository)
	{
		return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
	}
}
