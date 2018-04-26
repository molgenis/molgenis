package org.molgenis.core.ui.controller;

import org.molgenis.core.ui.settings.StaticContent;
import org.molgenis.core.ui.settings.StaticContentFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.core.runas.RunAsSystemAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.requireNonNull;
import static org.molgenis.core.ui.settings.StaticContentMeta.STATIC_CONTENT;

/**
 * Controller that handles static content pages requests.
 */
@Service
public class StaticContentServiceImpl implements StaticContentService
{
	private static final Logger LOG = LoggerFactory.getLogger(StaticContentServiceImpl.class);

	private final DataService dataService;
	private final StaticContentFactory staticContentFactory;

	private final UserPermissionEvaluator permissionService;

	StaticContentServiceImpl(DataService dataService, StaticContentFactory staticContentFactory,
			UserPermissionEvaluator permissionService)
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
		return permissionService.hasPermission(new EntityTypeIdentity(STATIC_CONTENT), EntityTypePermission.WRITE);
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
		if (!permissionService.hasPermission(new EntityTypeIdentity(STATIC_CONTENT), EntityTypePermission.WRITE))
		{
			throw new MolgenisDataAccessException("No write permission on static content entity type.");
		}
	}
}
