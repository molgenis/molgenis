package org.molgenis.omx.controller;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

/**
 * Controller that handles static content pages requests
 * 
 * RuntimeProperty_[KeyApp] is the way an identifier is made
 */
@Service
public class StaticContentServiceImpl implements StaticContentService
{
	public static final String DEFAULT_CONTENT = "<p>Place here some content!</p>";
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
	public String init(final String uniqueReference, final Model model)
	{
		model.addAttribute("content", this.getContent(uniqueReference, model));
		model.addAttribute("isCurrentUserAuthenticatedSu", this.isCurrentUserAuthenticatedSu());
		model.addAttribute("editHref", "/menu/main/" + uniqueReference + "/edit");
		return "view-staticcontent";
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public String initEdit(final String uniqueReference, final Model model)
	{
		if(this.isCurrentUserAuthenticatedSu()){
			model.addAttribute("content", this.getContent(uniqueReference, model));
			model.addAttribute("cancelHref", "/menu/main/" + uniqueReference);
			return "view-staticcontent-edit";
		}else{
			return this.init(uniqueReference, model);
		}
	}

	@Override
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	@Transactional(readOnly = true, rollbackFor = DatabaseException.class)
	public boolean submitContent(final String uniqueReference, final String content)
	{
		boolean succes;
		if(this.molgenisSettings.propertyExists(PREFIX_KEY + uniqueReference)){
			succes = this.molgenisSettings.updateProperty(PREFIX_KEY + uniqueReference, content);
		}else{
			this.molgenisSettings.setProperty(PREFIX_KEY + uniqueReference, content);
			succes = true;
		}
		return succes;
	}

	private boolean isCurrentUserAuthenticatedSu()
	{
		return SecurityUtils.currentUserIsAuthenticated() && SecurityUtils.currentUserIsSu();
	}

	private String getContent(final String uniqueReference, final Model model)
	{
		String content = this.molgenisSettings.getProperty(PREFIX_KEY + uniqueReference, DEFAULT_CONTENT);

		if (null == content || content.isEmpty())
		{
			throw new MolgenisDataException("content is null or empty");
		}

		return content;
	}
}
