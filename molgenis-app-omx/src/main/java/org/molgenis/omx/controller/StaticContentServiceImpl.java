package org.molgenis.omx.controller;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Controller that handles static content pages requests
 * 
 * RuntimeProperty_[KeyApp] is the way an identifier is made
 */
@Service
public class StaticContentServiceImpl implements StaticContentService
{
	public static final String DEFAULT_CONTENT = "<p>Place some content!</p>";
	public static final String PREFIX_KEY = "app.";

	private final MolgenisSettings molgenisSettings;

	@Autowired
	public StaticContentServiceImpl(final MolgenisSettings molgenisSettings)
	{
		if (molgenisSettings == null)
		{
			throw new IllegalArgumentException("molgenisSettings is null");
		}
		this.molgenisSettings = molgenisSettings;
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true)
	public boolean submitContent(final String uniqueReference, String content)
	{
		if (null == content) content = "";

		boolean succes;
		if (this.molgenisSettings.propertyExists(PREFIX_KEY + uniqueReference))
		{
			succes = this.molgenisSettings.updateProperty(PREFIX_KEY + uniqueReference, content);
		}
		else
		{
			this.molgenisSettings.setProperty(PREFIX_KEY + uniqueReference, content);
			succes = true;
		}
		return succes;
	}

	@Override
	public boolean isCurrentUserCanEdit()
	{
		return SecurityUtils.currentUserIsAuthenticated() && SecurityUtils.currentUserIsSu();
	}

	@Override
	public String getContent(final String uniqueReference)
	{
		String content = this.molgenisSettings.getProperty(PREFIX_KEY + uniqueReference, DEFAULT_CONTENT);

		if (null == content)
		{
			throw new MolgenisDataException("Content is null");
		}

		return content;
	}
}
