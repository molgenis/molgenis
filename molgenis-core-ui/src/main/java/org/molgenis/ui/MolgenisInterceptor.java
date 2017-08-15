package org.molgenis.ui;

import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.ui.style.ThemeFingerprintRegistry;
import org.molgenis.util.ResourceFingerprintRegistry;
import org.molgenis.util.TemplateResourceUtils;
import org.springframework.beans.factory.annotation.Value;
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
	private final TemplateResourceUtils templateResourceUtils;
	private final AuthenticationSettings authenticationSettings;
	private final AppSettings appSettings;
	private final String environment;
	private final LanguageService languageService;

	public MolgenisInterceptor(ResourceFingerprintRegistry resourceFingerprintRegistry,
			ThemeFingerprintRegistry themeFingerprintRegistry, TemplateResourceUtils templateResourceUtils,
			AppSettings appSettings, AuthenticationSettings authenticationSettings, LanguageService languageService,
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
			modelAndView.addObject(KEY_APP_SETTINGS, appSettings);
			modelAndView.addObject(KEY_AUTHENTICATION_SETTINGS, authenticationSettings);
			modelAndView.addObject(KEY_ENVIRONMENT, getEnvironmentAttributes());
			modelAndView.addObject(KEY_I18N, languageService.getBundle());
		}
	}

	/**
	 * <p>When you use the "redirect:/" pattern from Spring something strange happens when you redirect to "/".</p>
	 * <p>
	 * <ul>You can choose 3 ways of solving this issue.
	 * <li>You can make sure Spring does not add the attribute to the url, by making the environment object an {@link Object} instead of a {@link String}
	 * <pre>
	 * {@code
	 * Map<String, String> environmentAttributes = new HashMap<>();
	 * environmentAttributes.put("environmentType", environment);
	 * return environmentAttributes;
	 * }
	 * </pre>
	 * </li>
	 * <li>You can return the {@link org.springframework.web.servlet.view.RedirectView} in your Controller and check on that view in the interceptor.
	 * <pre>
	 * {@code
	 * if (modelAndView.getView() instanceof RedirectView)
	 * {
	 *      return;
	 * }
	 * }
	 * </pre>
	 * </li>
	 * <li>You can use {@link org.springframework.web.servlet.FlashMap} to make sure the attributes of a redirect request are interpreted and removed from
	 * the uri when the request is finished.
	 * <pre>
	 * {@code
	 * FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
	 * flashMap.put(KEY_ENVIRONMENT, environment);
	 * FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
	 * flashMapManager.saveOutputFlashMap(flashMap, request, response);
	 * }
	 * </pre>
	 * But this does not work in the intercepter for some reason. This would be the preferable way to deal with {@link org.springframework.web.servlet.mvc.support.RedirectAttributes}.
	 * </li>
	 * </ul>
	 * <p>We decided to go for the environment attribute map. We could not oversee the consequences when we implement the RedirectView-strategy.</p>
	 *
	 * @return environmentAttributeMap
	 */
	private Map<String, String> getEnvironmentAttributes()
	{
		Map<String, String> environmentAttributes = new HashMap<>();
		environmentAttributes.put("environmentType", environment);
		return environmentAttributes;
	}

}
