package org.molgenis.security.permission;

import java.util.Collection;

import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.server.MolgenisPermissionService.Permission;
import org.molgenis.ui.MolgenisUi;
import org.molgenis.ui.MolgenisUiMenu;
import org.molgenis.ui.MolgenisUiMenuItem;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

public class MolgenisAccessDecisionVoter implements AccessDecisionVoter<FilterInvocation>
{
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
	public int vote(Authentication authentication, FilterInvocation filterInvocation, Collection<ConfigAttribute> attributes)
	{
		MolgenisPermissionService molgenisPermissionService = ApplicationContextProvider.getApplicationContext()
				.getBean(MolgenisPermissionService.class);
		MolgenisUi molgenisUi = ApplicationContextProvider.getApplicationContext().getBean(MolgenisUi.class);
		String menuId = getPluginId(filterInvocation.getRequestUrl());

		if (getMenuType(filterInvocation))
		{
			MolgenisUiMenu molgenisUiMenu = molgenisUi.getMenu(menuId);
			for (MolgenisUiMenuItem item : molgenisUiMenu.getItems())
			{
				if (molgenisPermissionService.hasPermissionOnPlugin(getPluginId(item.getUrl()), Permission.READ)) return ACCESS_GRANTED;
			}
		}

		return molgenisPermissionService.hasPermissionOnPlugin(menuId, Permission.READ) ? ACCESS_GRANTED : ACCESS_DENIED;
	}

	private boolean getMenuType(FilterInvocation object)
	{
		return object.getRequestUrl().matches("/menu/[^/]*");
	}

	private String getPluginId(String requestUrl)
	{
		String[] urlFragments = requestUrl.split("/");
		return urlFragments.length > 0 ? urlFragments[urlFragments.length - 1] : requestUrl;
	}
}
