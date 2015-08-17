package org.molgenis.ui;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.molgenis.ui.MolgenisPluginAttributes.KEY_APP_SETTINGS;
import static org.molgenis.ui.MolgenisPluginAttributes.KEY_ENVIRONMENT;
import static org.molgenis.ui.MolgenisPluginAttributes.KEY_I18N;
import static org.molgenis.ui.MolgenisPluginAttributes.KEY_RESOURCE_FINGERPRINT_REGISTRY;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.data.settings.AppSettings;
import org.molgenis.util.ResourceFingerprintRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Interceptor that adds default model objects to all requests that return a view.
 */
public class MolgenisInterceptor extends HandlerInterceptorAdapter
{
	private final ResourceFingerprintRegistry resourceFingerprintRegistry;
	private final AppSettings appSettings;
	private final String environment;

	@Autowired
	public MolgenisInterceptor(ResourceFingerprintRegistry resourceFingerprintRegistry, AppSettings appSettings,
			@Value("${environment}") String environment)
	{
		this.resourceFingerprintRegistry = checkNotNull(resourceFingerprintRegistry);
		this.appSettings = checkNotNull(appSettings);
		this.environment = checkNotNull(environment);
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception
	{
		if (modelAndView != null)
		{
			// retrieve resource bundle for the configured language
			String languageCode = appSettings.getLanguageCode();
			Locale locale = new Locale(languageCode, languageCode);
			ResourceBundle i18n = ResourceBundle.getBundle("i18n", locale);

			modelAndView.addObject(KEY_RESOURCE_FINGERPRINT_REGISTRY, resourceFingerprintRegistry);
			modelAndView.addObject(KEY_ENVIRONMENT, environment);
			modelAndView.addObject(KEY_APP_SETTINGS, appSettings);
			modelAndView.addObject(KEY_I18N, i18n);
		}
	}
}
