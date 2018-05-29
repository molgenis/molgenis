package org.molgenis.core.ui;

import com.google.common.collect.Streams;
import org.molgenis.core.ui.style.ThemeFingerprintRegistry;
import org.molgenis.core.util.ResourceFingerprintRegistry;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.settings.AppSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceResourceBundle;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static org.molgenis.web.PluginAttributes.*;
import static org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;

/**
 * Interceptor that adds default model objects to all requests that return a view.
 */
public class MolgenisInterceptor extends HandlerInterceptorAdapter
{
	private final ResourceFingerprintRegistry resourceFingerprintRegistry;
	private final ThemeFingerprintRegistry themeFingerprintRegistry;
	private final AuthenticationSettings authenticationSettings;
	private final AppSettings appSettings;
	private final String environment;
	private final MessageSource messageSource;
	@Autowired
	InMemoryClientRegistrationRepository clientRegistrationRepository;

	public static final String ATTRIBUTE_ENVIRONMENT_TYPE = "environmentType";

	public MolgenisInterceptor(ResourceFingerprintRegistry resourceFingerprintRegistry,
			ThemeFingerprintRegistry themeFingerprintRegistry, AppSettings appSettings,
			AuthenticationSettings authenticationSettings, @Value("${environment}") String environment,
			MessageSource messageSource)
	{
		this.resourceFingerprintRegistry = requireNonNull(resourceFingerprintRegistry);
		this.themeFingerprintRegistry = requireNonNull(themeFingerprintRegistry);
		this.appSettings = requireNonNull(appSettings);
		this.authenticationSettings = requireNonNull(authenticationSettings);
		this.environment = requireNonNull(environment);
		this.messageSource = requireNonNull(messageSource);
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView)
	{
		if (modelAndView != null)
		{
			modelAndView.addObject(KEY_RESOURCE_FINGERPRINT_REGISTRY, resourceFingerprintRegistry);
			modelAndView.addObject(KEY_THEME_FINGERPRINT_REGISTRY, themeFingerprintRegistry);
			modelAndView.addObject(KEY_APP_SETTINGS, appSettings);
			modelAndView.addObject(KEY_AUTHENTICATION_SETTINGS, authenticationSettings);
			modelAndView.addObject(KEY_ENVIRONMENT, getEnvironmentAttributes());
			modelAndView.addObject(KEY_I18N,
					new MessageSourceResourceBundle(messageSource, LocaleContextHolder.getLocale()));
			modelAndView.addObject(KEY_OAUTH2_CLIENT_REGISTRATIONS_ATTRIBUTE, oauth2ClientRegistrations());
			modelAndView.addObject(KEY_AUTHENTICATION, SecurityContextHolder.getContext().getAuthentication());
		}
	}

	/**
	 * <p>Make sure Spring does not add the attributes as query parameters to the url when doing a redirect.
	 * You can do this by introducing an object instead of a string key value pair.</p>
	 * <p>See <a href="https://github.com/molgenis/molgenis/issues/6515">https://github.com/molgenis/molgenis/issues/6515</a></p>
	 *
	 * @return environmentAttributeMap
	 */
	private Map<String, String> getEnvironmentAttributes()
	{
		Map<String, String> environmentAttributes = new HashMap<>();
		environmentAttributes.put(ATTRIBUTE_ENVIRONMENT_TYPE, environment);
		return environmentAttributes;
	}

	private Map<String, String> oauth2ClientRegistrations()
	{
		return Streams.stream(clientRegistrationRepository.iterator())
					  .collect(toMap(registration -> DEFAULT_AUTHORIZATION_REQUEST_BASE_URI + "/"
							  + registration.getRegistrationId(), ClientRegistration::getClientName));
	}

}
