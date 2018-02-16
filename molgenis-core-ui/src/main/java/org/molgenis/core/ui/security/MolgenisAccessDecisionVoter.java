package org.molgenis.core.ui.security;

import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.web.Ui;
import org.molgenis.web.UiMenu;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MolgenisAccessDecisionVoter implements AccessDecisionVoter<FilterInvocation>
{
	private static Pattern PATTERN_MENUID = Pattern.compile("/menu/([^/]+).*");
	private static Pattern PATTERN_PLUGINID = Pattern.compile("(?:/plugin|/menu/[^/]+)/([^/^?]+).*");

	@Override
	public boolean supports(ConfigAttribute attribute)
	{
		return true;
	}

	@Override
	public boolean supports(Class<?> clazz)
	{
		return true;
	}

	@Override
	public int vote(Authentication authentication, FilterInvocation filterInvocation,
			Collection<ConfigAttribute> attributes)
	{
		String requestUrl = filterInvocation.getRequestUrl();

		Matcher pluginMatcher = PATTERN_PLUGINID.matcher(requestUrl);
		if (pluginMatcher.matches())
		{
			String pluginId = pluginMatcher.group(1);
			return getMolgenisPermissionService().hasPermission(new PluginIdentity(pluginId),
					PluginPermission.READ) ? ACCESS_GRANTED : ACCESS_DENIED;
		}

		Matcher menuMatcher = PATTERN_MENUID.matcher(requestUrl);
		if (menuMatcher.matches())
		{
			String menuId = menuMatcher.group(1);
			UiMenu menu = getMolgenisUi().getMenu(menuId);
			return menu != null ? ACCESS_GRANTED : ACCESS_DENIED;
		}

		return ACCESS_DENIED;
	}

	/**
	 * Can't be autowired due to circular dependency resolving
	 */
	private UserPermissionEvaluator getMolgenisPermissionService()
	{
		return ApplicationContextProvider.getApplicationContext().getBean(UserPermissionEvaluator.class);
	}

	/**
	 * Can't be autowired due to circular dependency resolving
	 */
	private Ui getMolgenisUi()
	{
		return ApplicationContextProvider.getApplicationContext().getBean(Ui.class);
	}
}
