package org.molgenis.ui;

import static org.molgenis.ui.MolgenisPluginAttributes.KEY_RESOURCE_FINGERPRINT_REGISTRY;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.util.ResourceFingerprintRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import java.util.Locale;
import java.util.ResourceBundle;

public class MolgenisInterceptor extends HandlerInterceptorAdapter
{
	private final ResourceFingerprintRegistry resourceFingerprintRegistry;
	private final MolgenisSettings molgenisSettings;
	public static final String I18N_LOCALE = "i18nLocale";

	@Autowired
	public MolgenisInterceptor(ResourceFingerprintRegistry resourceFingerprintRegistry,
			MolgenisSettings molgenisSettings)
	{
		if (resourceFingerprintRegistry == null)
		{
			throw new IllegalArgumentException("resourceFingerprintRegistry ui is null");
		}
		this.resourceFingerprintRegistry = resourceFingerprintRegistry;
		this.molgenisSettings = molgenisSettings;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception
	{
		if (modelAndView != null)
		{
			modelAndView.addObject(KEY_RESOURCE_FINGERPRINT_REGISTRY, resourceFingerprintRegistry);
			String i18nLocale = molgenisSettings.getProperty(I18N_LOCALE, "en");
			String topLogo = molgenisSettings.getProperty("app.top.logo", "");

			Locale locale = new Locale(i18nLocale, i18nLocale);
			ResourceBundle i18n = ResourceBundle.getBundle("i18n", locale);

			modelAndView.addObject("i18n", i18n);
			modelAndView.addObject("app_top_logo", topLogo);
		}
	}
}
