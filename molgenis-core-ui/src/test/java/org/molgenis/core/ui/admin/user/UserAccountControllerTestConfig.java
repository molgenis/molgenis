package org.molgenis.core.ui.admin.user;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.molgenis.data.DataService;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.twofactor.service.RecoveryService;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationService;
import org.molgenis.security.user.UserAccountService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;

@Configuration
public class UserAccountControllerTestConfig
{
	@Mock
	private UserAccountService userAccountService;
	@Mock
	private RecoveryService recoveryService;
	@Mock
	private TwoFactorAuthenticationService twoFactorAuthenticationService;
	@Mock
	private AuthenticationSettings authenticationSettings;
	@Mock
	private DataService dataService;
	@Mock
	private LocaleResolver localeResolver;

	public UserAccountControllerTestConfig()
	{
		MockitoAnnotations.initMocks(this);
	}

	public void resetMocks()
	{
		Mockito.reset(userAccountService, recoveryService, twoFactorAuthenticationService, authenticationSettings,
				dataService, localeResolver);
	}

	@Bean
	public DataService dataService()
	{
		return dataService;
	}

	@Bean
	public UserAccountService userAccountService()
	{
		return userAccountService;
	}

	@Bean
	public RecoveryService recoveryService()
	{
		return recoveryService;
	}

	@Bean
	public TwoFactorAuthenticationService twoFactorAuthenticationService()
	{
		return twoFactorAuthenticationService;
	}

	@Bean
	public AuthenticationSettings authenticationSettings()
	{
		return authenticationSettings;
	}

	@Bean
	public LocaleResolver localeResolver()
	{
		return localeResolver;
	}

	@Bean
	public UserAccountController userAccountController()
	{
		return new UserAccountController(userAccountService(), recoveryService(), twoFactorAuthenticationService(),
				authenticationSettings(), localeResolver());
	}
}
