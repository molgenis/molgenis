package org.molgenis.ui.controller;

import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionService;
import org.molgenis.security.core.runas.RunAsSystemAspect;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.ui.settings.StaticContent;
import org.molgenis.ui.settings.StaticContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;
import static org.molgenis.ui.settings.StaticContentMeta.STATIC_CONTENT;

/**
 * Controller that handles static content pages requests.
 */
@Service
public class StaticContentServiceImpl implements StaticContentService
{
	private static final Logger LOG = LoggerFactory.getLogger(StaticContentServiceImpl.class);

	private final DataService dataService;
	private final StaticContentFactory staticContentFactory;

	private final PermissionService permissionService;

	@Autowired
	public StaticContentServiceImpl(DataService dataService, StaticContentFactory staticContentFactory,
			PermissionService permissionService)
	{
		this.permissionService = requireNonNull(permissionService);
		this.dataService = requireNonNull(dataService);
		this.staticContentFactory = staticContentFactory;
	}

	@Override
	@Transactional
	public boolean submitContent(String key, String content)
	{
		this.checkPermissions(key);
		try
		{
			StaticContent staticContent = dataService.findOneById(STATIC_CONTENT, key, StaticContent.class);
			if (staticContent == null)
			{
				staticContent = staticContentFactory.create(key);
				staticContent.setContent(content);
				dataService.add(STATIC_CONTENT, staticContent);
			}
			else
			{
				staticContent.setContent(content);
				dataService.update(STATIC_CONTENT, staticContent);
			}
			return true;
		}
		catch (RuntimeException e)
		{
			LOG.error("", e);
			return false;
		}
	}

	@Override
	public boolean isCurrentUserCanEdit(String pluginId)
	{
		return SecurityUtils.currentUserIsAuthenticated() && permissionService.hasPermissionOnPlugin(pluginId,
				Permission.WRITE);
	}

	@Override
	public String getContent(String key)
	{
		StaticContent staticContent = RunAsSystemAspect.runAsSystem(
				() -> dataService.findOneById(STATIC_CONTENT, key, StaticContent.class));
		return staticContent != null ? staticContent.getContent() : null;
	}

	public void checkPermissions(String pluginId)
	{
		if (!this.isCurrentUserCanEdit(pluginId))
		{
			throw new MolgenisDataAccessException("No write permissions on static content page");
		}
	}
}
