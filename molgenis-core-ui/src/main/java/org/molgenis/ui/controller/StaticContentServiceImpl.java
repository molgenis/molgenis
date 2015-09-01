package org.molgenis.ui.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import org.molgenis.data.DataService;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.ui.settings.StaticContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Controller that handles static content pages requests.
 */
@Service
public class StaticContentServiceImpl implements StaticContentService
{
	private static final Logger LOG = LoggerFactory.getLogger(StaticContentServiceImpl.class);

	private final DataService dataService;

	@Autowired
	public StaticContentServiceImpl(DataService dataService)
	{
		this.dataService = checkNotNull(dataService);
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU','ROLE_SYSTEM')")
	@Transactional
	public boolean submitContent(String key, String content)
	{
		try
		{
			StaticContent staticContent = dataService.findOne(StaticContent.ENTITY_NAME, key, StaticContent.class);
			if (staticContent == null)
			{
				staticContent = new StaticContent(key, dataService);
				dataService.add(StaticContent.ENTITY_NAME, staticContent);
			}
			else
			{
				staticContent.setContent(content);
				dataService.update(StaticContent.ENTITY_NAME, staticContent);
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
	public boolean isCurrentUserCanEdit()
	{
		return SecurityUtils.currentUserIsAuthenticated() && SecurityUtils.currentUserIsSu();
	}

	@Override
	public String getContent(String key)
	{
		StaticContent staticContent = RunAsSystemProxy.runAsSystem(() -> {
			return dataService.findOne(StaticContent.ENTITY_NAME, key, StaticContent.class);
		});
		return staticContent != null ? staticContent.getContent() : null;
	}
}
