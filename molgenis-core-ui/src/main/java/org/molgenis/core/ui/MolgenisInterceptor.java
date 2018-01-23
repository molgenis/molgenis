package org.molgenis.core.ui;

import org.molgenis.core.ui.style.ThemeFingerprintRegistry;
import org.molgenis.core.util.ResourceFingerprintRegistry;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.settings.AppSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceResourceBundle;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.molgenis.web.PluginAttributes.*;

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
			ModelAndView modelAndView) throws Exception
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

}
