package org.molgenis.ui;

import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.twofactor.settings.AuthenticationSettings;
import org.molgenis.ui.style.ThemeFingerprintRegistry;
import org.molgenis.util.ResourceFingerprintRegistry;
import org.molgenis.util.TemplateResourceUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.Objects.requireNonNull;
import static org.molgenis.web.PluginAttributes.*;

/**
 * Interceptor that adds default model objects to all requests that return a view.
 */
public class MolgenisInterceptor extends HandlerInterceptorAdapter
{
	private final ResourceFingerprintRegistry resourceFingerprintRegistry;
	private final ThemeFingerprintRegistry themeFingerprintRegistry;
	private final TemplateResourceUtils templateResourceUtils;
	private final AppSettings appSettings;
	private final AuthenticationSettings authenticationSettings;
	private final String environment;
	private final LanguageService languageService;

	public MolgenisInterceptor(ResourceFingerprintRegistry resourceFingerprintRegistry,
			ThemeFingerprintRegistry themeFingerprintRegistry, TemplateResourceUtils templateResourceUtils, AppSettings appSettings,
			AuthenticationSettings authenticationSettings, LanguageService languageService,
			@Value("${environment}") String environment)
	{
		this.resourceFingerprintRegistry = requireNonNull(resourceFingerprintRegistry);
		this.themeFingerprintRegistry = requireNonNull(themeFingerprintRegistry);
		this.templateResourceUtils = requireNonNull(templateResourceUtils);
		this.appSettings = requireNonNull(appSettings);
		this.authenticationSettings = requireNonNull(authenticationSettings);
		this.languageService = requireNonNull(languageService);
		this.environment = requireNonNull(environment);
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception
	{
		if (modelAndView != null)
		{
			modelAndView.addObject(KEY_RESOURCE_FINGERPRINT_REGISTRY, resourceFingerprintRegistry);
			modelAndView.addObject(KEY_THEME_FINGERPRINT_REGISTRY, themeFingerprintRegistry);
			modelAndView.addObject(KEY_RESOURCE_UTILS, templateResourceUtils);
			modelAndView.addObject(KEY_ENVIRONMENT, environment);
			modelAndView.addObject(KEY_APP_SETTINGS, appSettings);
			modelAndView.addObject(KEY_AUTHENTICATION_SETTINGS, authenticationSettings);
			modelAndView.addObject(KEY_I18N, languageService.getBundle());
		}
	}
}
