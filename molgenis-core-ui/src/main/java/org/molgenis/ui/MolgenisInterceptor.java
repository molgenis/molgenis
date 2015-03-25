package org.molgenis.ui;

import static org.molgenis.ui.MolgenisPluginAttributes.KEY_ENVIRONMENT;
import static org.molgenis.ui.MolgenisPluginAttributes.KEY_RESOURCE_FINGERPRINT_REGISTRY;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.util.ResourceFingerprintRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class MolgenisInterceptor extends HandlerInterceptorAdapter
{
	public static final String APP_HREF_LOGO = "app.href.logo";
	private final ResourceFingerprintRegistry resourceFingerprintRegistry;
	private final MolgenisSettings molgenisSettings;
	public static final String I18N_LOCALE = "i18nLocale";
	private final String environment;

	@Autowired
	public MolgenisInterceptor(ResourceFingerprintRegistry resourceFingerprintRegistry,
			MolgenisSettings molgenisSettings, @Value("${environment}") String environment)
	{
		if (resourceFingerprintRegistry == null)
		{
			throw new IllegalArgumentException("resourceFingerprintRegistry ui is null");
		}
		if (molgenisSettings == null)
		{
			throw new IllegalArgumentException("molgenisSettings is null");
		}
		if (environment == null)
		{
			throw new IllegalArgumentException("environment is null");
		}
		this.resourceFingerprintRegistry = resourceFingerprintRegistry;
		this.molgenisSettings = molgenisSettings;
		this.environment = environment;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception
	{
		if (modelAndView != null)
		{
			modelAndView.addObject(KEY_ENVIRONMENT, environment);
			modelAndView.addObject(KEY_RESOURCE_FINGERPRINT_REGISTRY, resourceFingerprintRegistry);
			String i18nLocale = molgenisSettings.getProperty(I18N_LOCALE, "en");
			String topLogo = molgenisSettings.getProperty("app.top.logo", "");
			String homeLogo = molgenisSettings.getProperty(APP_HREF_LOGO, "");

			Locale locale = new Locale(i18nLocale, i18nLocale);
			ResourceBundle i18n = ResourceBundle.getBundle("i18n", locale);

			modelAndView.addObject("i18n", i18n);
			modelAndView.addObject("app_top_logo", topLogo);
			modelAndView.addObject("app_home_logo", homeLogo);
		}
	}
}
