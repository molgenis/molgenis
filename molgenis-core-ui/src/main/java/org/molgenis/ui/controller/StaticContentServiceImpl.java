package org.molgenis.ui.controller;

import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.security.core.runas.RunAsSystemAspect;
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

	@Autowired
	public StaticContentServiceImpl(DataService dataService, StaticContentFactory staticContentFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.staticContentFactory = staticContentFactory;
	}

	@Override
	@Transactional
	public boolean submitContent(String key, String content)
	{
		try
		{
			StaticContent staticContent = getStaticContent(key);
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
		try
		{
			return getStaticContent(pluginId).isWritable();
		}
		catch (MolgenisDataAccessException e)
		{
			return false;
		}
	}

	@Override
	public void checkPermissions(String pluginId)
	{
		if (!this.isCurrentUserCanEdit(pluginId))
		{
			throw new MolgenisDataAccessException("No write permissions on static content page");
		}
	}

	@Override
	public String getContent(String key)
	{
		StaticContent staticContent = getStaticContentAsSystemUser(key);
		return staticContent != null ? staticContent.getContent() : null;
	}

	private StaticContent getStaticContent(String key)
	{
		return dataService.findOneById(STATIC_CONTENT, key, StaticContent.class);
	}

	private StaticContent getStaticContentAsSystemUser(String key)
	{
		return RunAsSystemAspect.runAsSystem(() -> this.getStaticContent(key));
	}
}
