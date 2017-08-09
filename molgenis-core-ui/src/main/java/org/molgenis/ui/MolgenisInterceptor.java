package org.molgenis.ui;

import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.util.ResourceFingerprintRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.Objects.requireNonNull;
import static org.molgenis.ui.MolgenisPluginAttributes.*;

/**
 * Interceptor that adds default model objects to all requests that return a view.
 */
public class MolgenisInterceptor extends HandlerInterceptorAdapter
{
	private final ResourceFingerprintRegistry resourceFingerprintRegistry;
	private final AppSettings appSettings;
	private final String environment;
	private final LanguageService languageService;

	@Autowired
	public MolgenisInterceptor(ResourceFingerprintRegistry resourceFingerprintRegistry, AppSettings appSettings,
			LanguageService languageService, @Value("${environment}") String environment)
	{
		this.resourceFingerprintRegistry = requireNonNull(resourceFingerprintRegistry);
		this.appSettings = requireNonNull(appSettings);
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
			modelAndView.addObject(KEY_ENVIRONMENT, environment);
			modelAndView.addObject(KEY_APP_SETTINGS, appSettings);
			modelAndView.addObject(KEY_I18N, languageService.getBundle());
		}
	}
}
