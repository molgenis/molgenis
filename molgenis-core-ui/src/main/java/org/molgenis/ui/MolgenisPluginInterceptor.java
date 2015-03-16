package org.molgenis.ui;

import static org.molgenis.ui.MolgenisPluginAttributes.KEY_AUTHENTICATED;
import static org.molgenis.ui.MolgenisPluginAttributes.KEY_MOLGENIS_UI;
import static org.molgenis.ui.MolgenisPluginAttributes.KEY_PLUGINID_WITH_QUERY_STRING;
import static org.molgenis.ui.MolgenisPluginAttributes.KEY_PLUGIN_ID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class MolgenisPluginInterceptor extends HandlerInterceptorAdapter
{
	private final MolgenisUi molgenisUi;

	public static final String KEY_FOOTER = "molgenis.footer";
	public static final String DEFAULT_VAL_FOOTER = "null";
	public static final String MOLGENIS_CSS_THEME = "molgenis.css.theme";
	public static final String CSS_VARIABLE = "molgeniscsstheme";
	public static final String APP_TRACKING_CODE_VARIABLE = "app_tracking_code";

	private MolgenisSettings molgenisSettings;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
	{
		MolgenisPluginController molgenisPlugin = validateHandler(handler);

		// determine context url for this plugin if no context exists
		String contextUrl = (String) request.getAttribute(MolgenisPluginAttributes.KEY_CONTEXT_URL);
		if (contextUrl == null)
		{
			request.setAttribute(MolgenisPluginAttributes.KEY_CONTEXT_URL, molgenisPlugin.getUri());
		}

		return true;
	}

	@Autowired
	public MolgenisPluginInterceptor(MolgenisUi molgenisUi, MolgenisSettings molgenisSettings)
	{
		if (molgenisUi == null) throw new IllegalArgumentException("molgenis ui is null");
		this.molgenisUi = molgenisUi;
		this.molgenisSettings = molgenisSettings;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception
	{
		if (modelAndView != null)
		{
			MolgenisPluginController molgenisPlugin = validateHandler(handler);
			String pluginId = molgenisPlugin.getId();

			// allow controllers that handle multiple plugins to set their plugin id
			if (!modelAndView.getModel().containsKey(KEY_PLUGIN_ID))
			{
				modelAndView.addObject(KEY_PLUGIN_ID, pluginId);
			}

			if (molgenisSettings.getProperty(MOLGENIS_CSS_THEME) != null)
			{
				modelAndView.addObject(CSS_VARIABLE, molgenisSettings.getProperty(MOLGENIS_CSS_THEME));
			}

			modelAndView.addObject(APP_TRACKING_CODE_VARIABLE, new AppTrackingCodeImpl(molgenisSettings));
			modelAndView.addObject("footerText", molgenisSettings.getProperty(KEY_FOOTER));
			modelAndView.addObject(KEY_MOLGENIS_UI, molgenisUi);
			modelAndView.addObject(KEY_AUTHENTICATED, SecurityUtils.currentUserIsAuthenticated());
			modelAndView.addObject(KEY_PLUGINID_WITH_QUERY_STRING, getPluginIdWithQueryString(request, pluginId));
		}
	}

	public MolgenisPluginController validateHandler(Object handler)
	{
		if (!(handler instanceof HandlerMethod))
		{
			throw new RuntimeException("handler is not of type " + HandlerMethod.class.getSimpleName());
		}
		Object bean = ((HandlerMethod) handler).getBean();
		if (!(bean instanceof MolgenisPluginController))
		{
			throw new RuntimeException("controller does not implement "
					+ MolgenisPluginController.class.getSimpleName());
		}
		return (MolgenisPluginController) bean;
	}
	
	private String getPluginIdWithQueryString(HttpServletRequest request, String pluginId)
	{
		if (null != request)
		{
			String queryString = request.getQueryString();
			StringBuilder pluginIdAndQueryStringUrlPart = new StringBuilder();
			pluginIdAndQueryStringUrlPart.append(pluginId);
			if (queryString != null && !queryString.isEmpty()) pluginIdAndQueryStringUrlPart.append('?').append(
					queryString);
			return pluginIdAndQueryStringUrlPart.toString();
		}else{
			return "";
		}
	}
}
